package com.example.memorandum.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteListUiState>(NoteListUiState.Loading)
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _sortType = MutableStateFlow(SortType.CREATED_AT_DESC)

    init {
        observeNotesWithSearch()
    }

    @OptIn(FlowPreview::class)
    private fun observeNotesWithSearch() {
        viewModelScope.launch {
            combine(_searchQuery, _sortType) { query, sort -> query to sort }
                .debounce(300L)
                .distinctUntilChanged()
                .flatMapLatest { (query, sort) ->
                    when {
                        query.isNotBlank() && sort == SortType.CREATED_AT_DESC -> {
                            repository.searchNotesSortedByCreatedAt(query)
                        }

                        query.isBlank() && sort == SortType.CREATED_AT_DESC -> {
                            repository.getNotesSortedByCreatedAt()
                        }

                        query.isBlank() && sort == SortType.UPDATED_AT_DESC -> {
                            repository.getNotesSortedByUpdatedAt()
                        }

                        else -> {
                            val baseFlow = if (query.isBlank()) repository.getAllNotes() else repository.searchNotes(query)

                            baseFlow.map { notes ->
                                when (sort) {
                                    SortType.ALPHABETICAL_ASC -> notes.sortedBy { it.title.lowercase() }
                                    SortType.UPDATED_AT_DESC -> notes.sortedByDescending { it.updatedAt }
                                    else -> notes
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

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try { repository.deleteNote(note) } catch (_: Exception) { }
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            try { repository.updateNote(note.copy(isFavorite = !note.isFavorite)) } catch (_: Exception) { }
        }
    }
}