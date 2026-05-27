package com.example.memorandum.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorandum.data.repository.SettingsRepository
import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NoteListUiState {
    object Loading : NoteListUiState
    data class Success(val notes: List<Note>) : NoteListUiState
    data class Error(val message: String) : NoteListUiState
}

enum class SortType { CREATED_AT_DESC, UPDATED_AT_DESC, ALPHABETICAL_ASC }

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteListUiState>(NoteListUiState.Loading)
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _sortType = MutableStateFlow(SortType.CREATED_AT_DESC)
    private val _selectedTag = MutableStateFlow<String?>(null)

    val currentSortType: StateFlow<SortType> = _sortType.asStateFlow()
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val isTileLayout: StateFlow<Boolean> = settingsRepository.isTileLayout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _selectedNoteIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNoteIds: StateFlow<Set<Int>> = _selectedNoteIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    init {
        observeNotesWithSearch()
    }

    @OptIn(FlowPreview::class)
    private fun observeNotesWithSearch() {
        viewModelScope.launch {
            combine(_searchQuery, _sortType, _selectedTag) { query, sort, tag -> Triple(query, sort, tag) }
                .debounce(300L)
                .distinctUntilChanged()
                .flatMapLatest { (query, sort, tag) ->
                    if (tag != null) {
                        repository.getNotesByTag(tag).map { notes ->
                            val activeNotes = notes.filter { !it.isDeleted }
                            val filtered = if (query.isBlank()) activeNotes else activeNotes.filter {
                                it.title.contains(query, ignoreCase = true) ||
                                        it.content.contains(query, ignoreCase = true)
                            }
                            when (sort) {
                                SortType.CREATED_AT_DESC -> filtered.sortedByDescending { it.createdAt }
                                SortType.UPDATED_AT_DESC -> filtered.sortedByDescending { it.updatedAt }
                                SortType.ALPHABETICAL_ASC -> filtered.sortedBy { it.title.lowercase() }
                            }
                        }
                    } else {
                        if (query.isNotBlank()) {
                            repository.searchNotesSortedByCreatedAt(query)
                                .map { notes ->
                                    val activeNotes = notes.filter { !it.isDeleted }
                                    when (sort) {
                                        SortType.UPDATED_AT_DESC -> activeNotes.sortedByDescending { it.updatedAt }
                                        SortType.ALPHABETICAL_ASC -> activeNotes.sortedBy { it.title.lowercase() }
                                        SortType.CREATED_AT_DESC -> activeNotes
                                    }
                                }
                        } else {
                            val baseFlow = when (sort) {
                                SortType.CREATED_AT_DESC -> repository.getNotesSortedByCreatedAt()
                                SortType.UPDATED_AT_DESC -> repository.getNotesSortedByUpdatedAt()
                                SortType.ALPHABETICAL_ASC -> repository.getAllNotes()
                            }
                            baseFlow.map { notes ->
                                notes.filter { !it.isDeleted }.let { active ->
                                    if (sort == SortType.ALPHABETICAL_ASC) active.sortedBy { it.title.lowercase() } else active
                                }
                            }
                        }
                    }
                }
                .onStart { _uiState.value = NoteListUiState.Loading }
                .catch { e -> _uiState.value = NoteListUiState.Error("Eroare: ${e.localizedMessage}") }
                .collect { notes ->
                    _uiState.value = NoteListUiState.Success(notes)
                }
        }
    }

    fun onSearchQueryChanged(newQuery: String) { _searchQuery.value = newQuery }
    fun sortBy(type: SortType) { _sortType.value = type }
    fun selectTag(tag: String?) { _selectedTag.value = tag }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.updateNote(
                    note.copy(
                        isDeleted = true,
                        deletedAt = System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) { }
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            try { repository.updateNote(note.copy(isFavorite = !note.isFavorite)) } catch (_: Exception) { }
        }
    }

    fun toggleNoteSelection(noteId: Int) {
        val currentSelection = _selectedNoteIds.value
        _selectedNoteIds.value = if (noteId in currentSelection) {
            currentSelection - noteId
        } else {
            currentSelection + noteId
        }
        if (_selectedNoteIds.value.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun enterSelectionMode() {
        _isSelectionMode.value = true
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedNoteIds.value = emptySet()
    }

    fun selectAllNotes(notes: List<Note>) {
        _selectedNoteIds.value = notes.map { it.id }.toSet()
        _isSelectionMode.value = true
    }

    fun deleteSelectedNotes() {
        viewModelScope.launch {
            val selectedIds = _selectedNoteIds.value
            selectedIds.forEach { id ->
                try {
                    val currentState = _uiState.value
                    if (currentState is NoteListUiState.Success) {
                        val note = currentState.notes.find { it.id == id }
                        note?.let {
                            repository.updateNote(
                                it.copy(
                                    isDeleted = true,
                                    deletedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                } catch (_: Exception) { }
            }
            exitSelectionMode()
        }
    }

    fun toggleFavoriteSelectedNotes(isFavorite: Boolean) {
        viewModelScope.launch {
            val selectedIds = _selectedNoteIds.value
            selectedIds.forEach { id ->
                try {
                } catch (_: Exception) { }
            }
            exitSelectionMode()
        }
    }
}