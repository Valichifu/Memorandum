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

    val isTileLayout: StateFlow<Boolean> = settingsRepository.isTileLayout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        observeNotesWithSearch()
    }

    @OptIn(FlowPreview::class)
    private fun observeNotesWithSearch() {
        viewModelScope.launch {
            combine(_searchQuery, _sortType, _selectedTag) { query, sort, tag -> Triple(query, sort, tag) }
                .debounce(300L)
                .flatMapLatest { (query, sort, tag) ->
                    repository.getAllNotes().map { notes ->
                        val activeNotes = notes.filter { !it.isDeleted }
                        val filtered = if (query.isBlank()) activeNotes else activeNotes.filter {
                            it.title.contains(query, ignoreCase = true)
                        }
                        NoteListUiState.Success(filtered)
                    }
                }
                .collect { _uiState.value = it }
        }
    }

    fun onSearchQueryChanged(newQuery: String) { _searchQuery.value = newQuery }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isDeleted = true, deletedAt = System.currentTimeMillis()))
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isFavorite = !note.isFavorite))
        }
    }

    // ACUM ESTE ÎN INTERIORUL CLASEI (Înainte de ultima acoladă)
    fun renameNote(note: Note, newTitle: String) {
        viewModelScope.launch {
            try {
                repository.updateNote(note.copy(title = newTitle))
            } catch (e: Exception) {
                _uiState.value = NoteListUiState.Error("Eroare la redenumire: ${e.localizedMessage}")
            }
        }
    }
}
//acesta este