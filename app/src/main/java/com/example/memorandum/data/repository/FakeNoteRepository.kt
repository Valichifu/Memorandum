package com.example.memorandum.data.repository

import android.content.Context
import android.net.Uri
import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeNoteRepository : NoteRepository {

    private val _notes = MutableStateFlow(
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

    override fun getDeletedNotes(): Flow<List<Note>> {
        return _notes.map { notes ->
            notes.filter { it.isDeleted }
                .sortedByDescending { it.deletedAt ?: 0 }
        }
    }

    override suspend fun restoreNote(noteId: Int) {
        _notes.update { notes ->
            notes.map { note ->
                if (note.id == noteId) note.copy(isDeleted = false, deletedAt = null)
                else note
            }
        }
    }

    override suspend fun permanentDeleteNote(noteId: Int) {
        _notes.update { notes ->
            notes.filter { it.id != noteId }
        }
    }

    override suspend fun emptyTrash() {
        _notes.update { notes ->
            notes.filter { !it.isDeleted }
        }
    }

    override suspend fun exportNoteToTxt(
        context: Context,
        noteId: Int,
        uri: Uri
    ): Boolean {
        return true
    }

    override suspend fun importNoteFromTxt(
        context: Context,
        uri: Uri
    ): Int {
        val newNote = Note(
            id = (_notes.value.maxOfOrNull { it.id } ?: 0) + 1,
            title = "Imported Note",
            content = "Content from imported file",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        _notes.update { current -> current + newNote }
        return newNote.id
    }

    override fun getNotesByFolderId(folderId: Int): Flow<List<Note>> {
        TODO("Not yet implemented")
    }

    override fun getNotesWithoutFolder(): Flow<List<Note>> {
        TODO("Not yet implemented")
    }

    override suspend fun createNoteInFolder(folderId: Int): Int {
        TODO("Not yet implemented")
    }

    override suspend fun assignNotesToFolder(
        noteIds: Set<Int>,
        folderId: Int
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun removeNotesFromFolder(noteIds: Set<Int>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteNotesByIds(noteIds: Set<Int>) {
        TODO("Not yet implemented")
    }
}