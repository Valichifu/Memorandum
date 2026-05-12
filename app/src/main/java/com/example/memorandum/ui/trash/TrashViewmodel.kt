package com.example.memorandum.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrashUiState {
    object Loading : TrashUiState
    data class Success(val deletedNotes: List<Note>) : TrashUiState
    data class Error(val message: String) : TrashUiState
}

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrashUiState>(TrashUiState.Loading)
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    init {
        loadDeletedNotes()
    }

    private fun loadDeletedNotes() {
        viewModelScope.launch {
            repository.getDeletedNotes()
                .onStart { _uiState.value = TrashUiState.Loading }
                .catch { e -> _uiState.value = TrashUiState.Error(e.message ?: "Unknown error") }
                .collect { notes ->
                    _uiState.value = TrashUiState.Success(notes)
                }
        }
    }

    fun restoreNoteById(noteId: Int) {
        viewModelScope.launch {
            repository.restoreNote(noteId)
        }
    }

    fun permanentDeleteNoteById(noteId: Int) {
        viewModelScope.launch {
            repository.permanentDeleteNote(noteId)
        }
    }

    fun restoreAllNotes(notes: List<Note>) {
        viewModelScope.launch {
            notes.forEach { repository.restoreNote(it.id) }
        }
    }

    fun emptyTrash(notes: List<Note>) {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }
}