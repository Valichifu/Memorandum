package com.example.memorandum.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NoteEditorUiState {
    object Idle : NoteEditorUiState
    object Loading : NoteEditorUiState
    object Saved : NoteEditorUiState
    data class Success(val note: Note?) : NoteEditorUiState
    data class Error(val message: String) : NoteEditorUiState
}

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteEditorUiState>(NoteEditorUiState.Idle)
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private var currentNote: Note? = null

    fun loadNote(id: Int) {
        if (id == -1) {
            _uiState.value = NoteEditorUiState.Success(null)
            return
        }

        viewModelScope.launch {
            _uiState.value = NoteEditorUiState.Loading
            try {
                val note = repository.getNoteById(id).first()
                if (note != null) {
                    currentNote = note
                    _uiState.value = NoteEditorUiState.Success(note)
                } else {
                    _uiState.value = NoteEditorUiState.Error("Notița nu există.")
                }
            } catch (e: Exception) {
                _uiState.value = NoteEditorUiState.Error("Eroare: ${e.localizedMessage}")
            }
        }
    }

    fun saveNote(title: String, content: String, tags: List<String>) {
        if (title.isBlank() && content.isBlank()) {
            _uiState.value = NoteEditorUiState.Error("Nu poți salva o notiță goală.")
            return
        }

        viewModelScope.launch {
            try {
                if (currentNote == null) {
                    val newNote = Note(
                        title = title,
                        content = content,
                        tags = tags,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.insertNote(newNote)
                } else {
                    val updatedNote = currentNote!!.copy(
                        title = title,
                        content = content,
                        tags = tags,
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateNote(updatedNote)
                }
                _uiState.value = NoteEditorUiState.Saved
            } catch (e: Exception) {
                _uiState.value = NoteEditorUiState.Error("Eroare la salvare: ${e.localizedMessage}")
            }
        }
    }

    fun deleteNote() {
        val noteToDelete = currentNote ?: return
        viewModelScope.launch {
            try {
                repository.deleteNote(noteToDelete)
                _uiState.value = NoteEditorUiState.Saved
            } catch (e: Exception) {
                _uiState.value = NoteEditorUiState.Error("Eroare la ștergere: ${e.localizedMessage}")
            }
        }
    }
}