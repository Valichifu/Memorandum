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
            Note(
                id = 1,
                title = "Laborator ASEM",
                content = "De terminat proiectul la Practica până vineri.\n\n- Diagrama UML ✓\n- Documentația ✗\n- Prezentarea ✗",
                tags = listOf("asem", "urgent"),
                folderId = 1,
                createdAt = System.currentTimeMillis() - 3 * 86400000L,
                updatedAt = System.currentTimeMillis() - 86400000L
            ),
            Note(
                id = 2,
                title = "Curs Baze de Date",
                content = "Normalizare: 1NF, 2NF, 3NF\nJOIN-uri: INNER, LEFT, RIGHT\nExamen: 15 iunie",
                tags = listOf("asem", "bd"),
                folderId = 1,
                createdAt = System.currentTimeMillis() - 7 * 86400000L,
                updatedAt = System.currentTimeMillis() - 2 * 86400000L
            ),
            Note(
                id = 3,
                title = "Proiect Android",
                content = "Memorandum — aplicație de notițe\nStack: Kotlin, Jetpack Compose, Room, Hilt\nDeadline: sesiune",
                tags = listOf("android", "kotlin"),
                folderId = 1,
                createdAt = System.currentTimeMillis() - 14 * 86400000L,
                updatedAt = System.currentTimeMillis() - 3 * 86400000L
            ),

            Note(
                id = 4,
                title = "Idei Features",
                content = "- Dark Mode ✓\n- Foldere ✓\n- Export PDF\n- Sync cloud\n- Widget Android",
                tags = listOf("design", "features"),
                folderId = 2,
                createdAt = System.currentTimeMillis() - 5 * 86400000L,
                updatedAt = System.currentTimeMillis() - 86400000L
            ),
            Note(
                id = 5,
                title = "UI Improvements",
                content = "Tile layout arată bine pe homepage\nAnimații la delete\nCulori pastelate pentru tag-uri",
                tags = listOf("design", "ui"),
                folderId = 2,
                createdAt = System.currentTimeMillis() - 4 * 86400000L,
                updatedAt = System.currentTimeMillis() - 4 * 86400000L
            ),

            Note(
                id = 6,
                title = "Cumpărături",
                content = "- Pâine\n- Lapte\n- Baterii AA\n- Cafea\n- Detergent",
                tags = listOf("personal"),
                folderId = 3,
                createdAt = System.currentTimeMillis() - 86400000L,
                updatedAt = System.currentTimeMillis() - 86400000L
            ),
            Note(
                id = 7,
                title = "Cărți de citit",
                content = "1. Clean Code - Robert Martin\n2. The Pragmatic Programmer\n3. Kotlin in Action",
                tags = listOf("personal", "reading"),
                folderId = 3,
                createdAt = System.currentTimeMillis() - 10 * 86400000L,
                updatedAt = System.currentTimeMillis() - 10 * 86400000L
            ),

            Note(
                id = 8,
                title = "Meeting standup",
                content = "- Ce am făcut ieri\n- Ce fac azi\n- Blocaje",
                tags = listOf("work"),
                folderId = null,
                createdAt = System.currentTimeMillis() - 3600000L,
                updatedAt = System.currentTimeMillis() - 3600000L
            ),
            Note(
                id = 9,
                title = "Parolă WiFi birou",
                content = "memorandum2024!",
                tags = emptyList(),
                folderId = null,
                createdAt = System.currentTimeMillis() - 20 * 86400000L,
                updatedAt = System.currentTimeMillis() - 20 * 86400000L
            ),
            Note(
                id = 10,
                title = "Idee de startup",
                content = "Aplicație care îți amintește să bei apă bazată pe activitatea ta zilnică.",
                tags = listOf("idei"),
                folderId = null,
                isFavorite = true,
                createdAt = System.currentTimeMillis() - 2 * 86400000L,
                updatedAt = System.currentTimeMillis() - 2 * 86400000L
            ),

            Note(
                id = 11,
                title = "Draft vechi",
                content = "De șters oricum...",
                tags = emptyList(),
                folderId = null,
                isDeleted = true,
                deletedAt = System.currentTimeMillis() - 86400000L,
                createdAt = System.currentTimeMillis() - 30 * 86400000L,
                updatedAt = System.currentTimeMillis() - 5 * 86400000L
            )
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

    override fun getNotesByFolderId(folderId: Int): Flow<List<Note>> =
        _notes.map { notes ->
            notes.filter { it.folderId == folderId }
                .sortedByDescending { it.createdAt }
        }

    override fun getNotesWithoutFolder(): Flow<List<Note>> =
        _notes.map { notes ->
            notes.filter { it.folderId == null }
                .sortedByDescending { it.createdAt }
        }

    override suspend fun createNoteInFolder(folderId: Int): Int {
        val newId = (_notes.value.maxOfOrNull { it.id } ?: 0) + 1
        val note = Note(
            id = newId,
            title = "",
            content = "",
            folderId = folderId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        _notes.update { current -> current + note }
        return newId
    }

    override suspend fun assignNotesToFolder(noteIds: Set<Int>, folderId: Int) {
        _notes.update { notes ->
            notes.map { if (it.id in noteIds) it.copy(folderId = folderId) else it }
        }
    }

    override suspend fun removeNotesFromFolder(noteIds: Set<Int>) {
        _notes.update { notes ->
            notes.map { if (it.id in noteIds) it.copy(folderId = null) else it }
        }
    }

    override suspend fun deleteNotesByIds(noteIds: Set<Int>) {
        _notes.update { notes ->
            notes.filter { it.id !in noteIds }
        }
    }
}