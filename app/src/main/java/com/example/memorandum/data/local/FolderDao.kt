package com.example.memorandum.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Insert
    suspend fun createFolder(folder: FolderEntity)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("SELECT * FROM folders")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM notes WHERE folderId = :folderId")
    fun getNotesInFolder(folderId: Int): Flow<List<NoteEntity>>
    @Query("SELECT * FROM folders WHERE id = :id")
    fun getFolderById(id: Int): Flow<FolderEntity?>

}