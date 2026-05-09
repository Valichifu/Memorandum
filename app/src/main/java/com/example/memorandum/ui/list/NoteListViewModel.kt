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

@OptIn(FlowPreview::class)
@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteListUiState>(NoteListUiState.Loading)
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        observeNotesWithSearch()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeNotesWithSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300L)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        repository.getAllNotes()
                    } else {
                        repository.searchNotes(query)
                    }
                }
                .onStart { _uiState.value = NoteListUiState.Loading }
                .catch { e ->
                    _uiState.value = NoteListUiState.Error("Eroare la încărcare: ${e.localizedMessage}")
                }
                .collect { notes ->
                    _uiState.value = NoteListUiState.Success(notes)
                }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

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