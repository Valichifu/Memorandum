package com.example.memorandum.data.repository

import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
class FakeNoteRepository : NoteRepository {

    private val _notes = MutableStateFlow<List<Note>>(
        listOf(
            Note(id = 1, title = "Laborator ASEM", content = "De terminat proiectul la Practica", tags = listOf("asem", "urgent")),
            Note(id = 2, title = "Idei App", content = "Sa adaugam si Dark Mode neaparat", tags = listOf("design")),
            Note(id = 3, title = "Cumparaturi", content = "Paine, lapte, baterii", tags = listOf("personal"))
        )
    )

    override fun getAllNotes(): Flow<List<Note>> =
        _notes.asStateFlow()

    override fun getNoteById(id: Int): Flow<Note?> =
        _notes.map { notes -> notes.find { it.id == id } }

    override suspend fun insertNote(note: Note) {
        _notes.update { current ->
            val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
            current + note.copy(id = newId)
        }
    }

    override suspend fun updateNote(note: Note) {
        _notes.update { current ->
            current.map { if (it.id == note.id) note else it }
        }
    }

    override suspend fun deleteNote(note: Note) {
        _notes.update { current ->
            current.filter { it.id != note.id }
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> =
        _notes.map { notes ->
            notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }

    override fun getNotesByTag(tag: String): Flow<List<Note>> =
        _notes.map { notes ->
            notes.filter { it.tags.contains(tag) }
        }

    override fun getNotesSortedByCreatedAt(): Flow<List<Note>> {
        return _notes.map { notes ->
            notes.sortedByDescending { it.createdAt }
        }
    }

    override fun getNotesSortedByUpdatedAt(): Flow<List<Note>> {
        return _notes.map { notes ->
            notes.sortedByDescending { it.updatedAt }
        }
    }

    override fun searchNotesSortedByCreatedAt(query: String): Flow<List<Note>> {
        return _notes.map { notes ->
            notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }.sortedByDescending { it.createdAt }
        }
    }
    override fun getNotesInFolder(folderId: Int): Flow<List<Note>> {
        return _notes.map { notes ->
            notes.filter { it.folderId == folderId }
                .sortedByDescending { it.createdAt }
        }
    }

    override fun getRootNotes(): Flow<List<Note>> {
        return _notes.map { notes ->
            notes.filter { it.folderId == null }
                .sortedByDescending { it.createdAt }
        }
    }
}