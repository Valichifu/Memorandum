package com.example.memorandum.data.repository

import com.example.memorandum.data.local.FolderDao
import com.example.memorandum.data.local.FolderMapper
import com.example.memorandum.domain.model.Folder
import com.example.memorandum.data.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFolderRepository @Inject constructor(
    private val folderDao: FolderDao,
    private val folderMapper: FolderMapper
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> {
        return folderDao.getAllFolders().map { entities ->
            entities.map { folderMapper.toDomain(it) }
        }
    }

    override fun getFolderById(id: Int): Flow<Folder?> {
        return folderDao.getFolderById(id).map { entity ->
            entity?.let { folderMapper.toDomain(it) }
        }
    }

    override suspend fun createFolder(folder: Folder) {
        folderDao.createFolder(folderMapper.toEntity(folder))
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(folderMapper.toEntity(folder))
    }

    override suspend fun deleteFolder(folder: Folder) {
        folderDao.deleteFolder(folderMapper.toEntity(folder))
    }
}