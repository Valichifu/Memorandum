package com.example.memorandum.data.repository

import com.example.memorandum.domain.model.Folder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeFolderRepository : FolderRepository {

    private val _folders = MutableStateFlow(
        listOf(
            Folder(id = 1, name = "Facultate", createdAt = System.currentTimeMillis() - 14 * 86400000L),
            Folder(id = 2, name = "Idei App",  createdAt = System.currentTimeMillis() - 10 * 86400000L),
            Folder(id = 3, name = "Personal",  createdAt = System.currentTimeMillis() -  7 * 86400000L)
        )
    )

    override fun getAllFolders(): Flow<List<Folder>> = _folders.asStateFlow()

    override fun getFolderById(id: Int): Flow<Folder?> =
        _folders.map { it.find { f -> f.id == id } }

    override suspend fun createFolder(folder: Folder) {
        _folders.update { current ->
            val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
            current + folder.copy(id = newId)
        }
    }

    override suspend fun updateFolder(folder: Folder) {
        _folders.update { current ->
            current.map { if (it.id == folder.id) folder else it }
        }
    }

    override suspend fun deleteFolder(folder: Folder) {
        _folders.update { current ->
            current.filter { it.id != folder.id }
        }
    }
}