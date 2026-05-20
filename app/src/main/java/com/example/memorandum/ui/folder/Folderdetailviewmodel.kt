package com.example.memorandum.ui.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorandum.data.repository.FolderRepository
import com.example.memorandum.data.repository.SettingsRepository
import com.example.memorandum.domain.model.Folder
import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FolderDetailUiState {
    object Loading : FolderDetailUiState
    data class Success(val folder: Folder, val notes: List<Note>) : FolderDetailUiState
    data class Error(val message: String) : FolderDetailUiState
}

@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FolderDetailUiState>(FolderDetailUiState.Loading)
    val uiState: StateFlow<FolderDetailUiState> = _uiState.asStateFlow()

    val isTileLayout: StateFlow<Boolean> = settingsRepository.isTileLayout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allUnassignedNotes: StateFlow<List<Note>> = noteRepository.getNotesWithoutFolder()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedNoteIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNoteIds: StateFlow<Set<Int>> = _selectedNoteIds.asStateFlow()

    fun loadFolder(folderId: Int) {
        viewModelScope.launch {
            combine(
                folderRepository.getFolderById(folderId),
                noteRepository.getNotesByFolderId(folderId)
            ) { folder, notes ->
                if (folder == null) FolderDetailUiState.Error("Folderul nu a fost găsit")
                else FolderDetailUiState.Success(folder, notes)
            }
                .catch { e -> emit(FolderDetailUiState.Error(e.message ?: "Eroare")) }
                .collect { _uiState.value = it }
        }
    }

    fun renameFolder(folderId: Int, newName: String) {
        viewModelScope.launch {
            val current = (uiState.value as? FolderDetailUiState.Success)?.folder ?: return@launch
            folderRepository.updateFolder(current.copy(name = newName))
        }
    }

    fun toggleLayout() {
        viewModelScope.launch {
            settingsRepository.setLayout(!isTileLayout.value)
        }
    }

    /** Creates a new note pre-assigned to this folder, returns the new note id */
    fun createNoteInFolder(folderId: Int): Int {
        var newId = -1
        viewModelScope.launch {
            newId = noteRepository.createNoteInFolder(folderId)
        }
        return newId
    }

    /** Assigns existing notes to this folder */
    fun addNotesToFolder(noteIds: Set<Int>, folderId: Int) {
        viewModelScope.launch {
            noteRepository.assignNotesToFolder(noteIds, folderId)
        }
    }

    /** Removes a single note from the folder (sets folderId = null) */
    fun removeNoteFromFolder(note: Note) {
        viewModelScope.launch {
            noteRepository.updateNote(note.copy(folderId = null))
        }
    }

    /** Removes multiple notes from the folder */
    fun removeNotesFromFolder(noteIds: Set<Int>) {
        viewModelScope.launch {
            noteRepository.removeNotesFromFolder(noteIds)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }

    fun deleteSelectedNotes() {
        viewModelScope.launch {
            val ids = _selectedNoteIds.value
            noteRepository.deleteNotesByIds(ids)
            exitSelectionMode()
        }
    }

    fun enterSelectionMode() { _isSelectionMode.value = true }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedNoteIds.value = emptySet()
    }

    fun toggleNoteSelection(id: Int) {
        _selectedNoteIds.update { current ->
            if (current.contains(id)) current - id else current + id
        }
    }
}