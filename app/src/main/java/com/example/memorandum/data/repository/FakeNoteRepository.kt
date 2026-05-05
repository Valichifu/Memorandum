package com.example.memorandum.data.repository

import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeNoteRepository : NoteRepository {

    private val notes = mutableListOf(
        Note(id = 1, title = "Laborator ASEM", content = "De terminat proiectul la Practică", tags = listOf("asem", "urgent")),
        Note(id = 2, title = "Idei App", content = "Să adăugăm și Dark Mode neapărat", tags = listOf("design")),
        Note(id = 3, title = "Cumpărături", content = "Pâine, lapte, baterii", tags = listOf("personal"))
    )

    override fun getAllNotes(): Flow<List<Note>> = flow { emit(notes.toList()) }

    override fun getNoteById(id: Int): Flow<Note?> = flow {
        emit(notes.find { it.id == id })
    }

    override suspend fun insertNote(note: Note) {
        val newId = (notes.maxOfOrNull { it.id } ?: 0) + 1
        notes.add(note.copy(id = newId))
    }

    override suspend fun updateNote(note: Note) {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) notes[index] = note
    }

    override suspend fun deleteNote(note: Note) {
        notes.removeIf { it.id == note.id }
    }

    override fun searchNotes(query: String): Flow<List<Note>> = flow {
        emit(notes.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
        })
    }

    override fun getNotesByTag(tag: String): Flow<List<Note>> = flow {
        emit(notes.filter { it.tags.contains(tag) })
    }
}