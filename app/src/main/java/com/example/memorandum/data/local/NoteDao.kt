package com.example.memorandum.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE isDeleted = 0")
    fun getAllNotes(): Flow<List<NoteEntity>>


    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND isDeleted = 0")
    fun searchNotes(query: String): Flow<List<NoteEntity>>


    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getNotesSortedByCreatedAt(): Flow<List<NoteEntity>>


    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND isDeleted = 0 ORDER BY createdAt DESC")
    fun searchNotesSortedByCreatedAt(query: String): Flow<List<NoteEntity>>


    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getNotesSortedByUpdatedAt(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE folderId = :folderId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getNotesInFolder(folderId: Int): Flow<List<NoteEntity>>


    @Query("SELECT * FROM notes WHERE folderId IS NULL AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getRootNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun deleteAllDeletedNotes()

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int)

    ////FTS4

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchNote(searchNote: NoteSearchEntity)

    @Query("DELETE FROM notes_search WHERE rowid = :noteId")
    suspend fun deleteSearchNote(noteId: Int)

    // Search avansat  cu MATCH +Highlighting Manage
    @Query(
        """
    SELECT 
        notes.id,
snippet(notes_search, '[start]', '[end]', '...', 0, -10) as title, 
snippet(notes_search, '[start]', '[end]', '...', 1, -30) as content, 
        notes.tags,
        notes.createdAt,
        notes.updatedAt,
        notes.isFavorite,
        notes.folderId,
        notes.isDeleted,
        notes.deletedAt
    FROM notes 
    JOIN notes_search ON notes.id = notes_search.rowid 
    WHERE notes_search MATCH :query AND notes.isDeleted = 0
    ORDER BY notes.createdAt DESC
"""
    )
    fun searchNotesWithHighlight(query: String): Flow<List<NoteEntity>>
}