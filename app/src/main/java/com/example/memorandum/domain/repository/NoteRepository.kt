package com.example.memorandum.domain.repository

import com.example.memorandum.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNoteById(id: Int): Flow<Note?>
    suspend fun insertNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun updateNote(note: Note)

    fun searchNotes(query: String): Flow<List<Note>>
    fun getNotesByTag(tag: String): Flow<List<Note>>

    fun getNotesSortedByCreatedAt(): Flow<List<Note>>

    fun getNotesSortedByUpdatedAt(): Flow<List<Note>>

    fun searchNotesSortedByCreatedAt(query: String): Flow<List<Note>>


}