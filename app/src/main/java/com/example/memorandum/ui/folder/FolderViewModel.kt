package com.example.memorandum.ui.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorandum.domain.model.Folder
import com.example.memorandum.data.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FolderUiState {
    object Loading : FolderUiState
    data class Success(val folders: List<Folder>) : FolderUiState
    data class Error(val message: String) : FolderUiState
}

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val repository: FolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FolderUiState>(FolderUiState.Loading)

    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()

    init {
        loadFolders()
    }

    private fun loadFolders() {
        viewModelScope.launch {
            repository.getAllFolders()
                .onStart { _uiState.value = FolderUiState.Loading }
                .catch { e -> _uiState.value = FolderUiState.Error(e.message ?: "Unknown error") }
                .collect { folders ->
                    _uiState.value = FolderUiState.Success(folders)
                }
        }
    }

    fun createFolder(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            val newFolder = Folder(
                name = name,
                createdAt = System.currentTimeMillis()
            )
            repository.createFolder(newFolder)
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            repository.deleteFolder(folder)
        }
    }
}