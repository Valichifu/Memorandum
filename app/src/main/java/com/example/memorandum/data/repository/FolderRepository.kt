package com.example.memorandum.data.repository

import com.example.memorandum.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getAllFolders(): Flow<List<Folder>>
    fun getFolderById(id: Int): Flow<Folder?>
    suspend fun createFolder(folder: Folder)
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folder: Folder)
}