package com.example.memorandum.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
                .catch { e ->
                    _uiState.value = TrashUiState.Error(
                        e.message ?: "Eroare necunoscută"
                    )
                }
                .collect { notes ->
                    _uiState.value = TrashUiState.Success(notes)
                }
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            repository.restoreNote(note)
        }
    }

    fun restoreAllNotes(notes: List<Note>) {
        viewModelScope.launch {
            notes.forEach { repository.restoreNote(it) }
        }
    }

    fun permanentDeleteNote(note: Note) {
        viewModelScope.launch {
            repository.permanentDeleteNote(note)
        }
    }

    fun emptyTrash(notes: List<Note>) {
        viewModelScope.launch {
            notes.forEach { repository.permanentDeleteNote(it) }
        }
    }
}