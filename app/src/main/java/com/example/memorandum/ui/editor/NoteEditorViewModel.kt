package com.example.memorandum.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NoteEditorUiState {
    object Idle : NoteEditorUiState
    object Loading : NoteEditorUiState
    object Saved : NoteEditorUiState
    data class Success(val note: Note?) : NoteEditorUiState
    data class Error(val message: String) : NoteEditorUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _noteId = MutableStateFlow(-1)

    val uiState: StateFlow<NoteEditorUiState> = _noteId
        .flatMapLatest { id ->
            if (id == -1) {
                flowOf(NoteEditorUiState.Success(null))
            } else {
                repository.getNoteById(id).map { note ->
                    if (note != null) NoteEditorUiState.Success(note)
                    else NoteEditorUiState.Error("Notița nu există.")
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NoteEditorUiState.Loading
        )

    fun loadNote(id: Int) {
        _noteId.value = id
    }

    fun saveNote(title: String, content: String, tags: List<String>) {
        if (title.isBlank() && content.isBlank()) {
            return
        }

        val currentNote = (uiState.value as? NoteEditorUiState.Success)?.note

        viewModelScope.launch {
            try {
                if (currentNote == null) {
                    repository.insertNote(
                        Note(
                            title = title,
                            content = content,
                            tags = tags,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    repository.updateNote(
                        currentNote.copy(
                            title = title,
                            content = content,
                            tags = tags,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    fun deleteNote() {
        val currentNote = (uiState.value as? NoteEditorUiState.Success)?.note ?: return
        viewModelScope.launch {
            try {
                repository.deleteNote(currentNote)
            } catch (e: Exception) {}
        }
    }
}