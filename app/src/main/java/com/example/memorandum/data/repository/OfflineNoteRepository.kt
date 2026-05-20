package com.example.memorandum.data.repository

import android.content.Context
import android.net.Uri
import com.example.memorandum.data.local.NoteDao
import com.example.memorandum.data.local.NoteEntity
import com.example.memorandum.data.local.NoteMapper
import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteMapper: NoteMapper
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { it.map(noteMapper::toDomain) }

    override fun getNoteById(id: Int): Flow<Note?> =
        noteDao.getNoteById(id).map { it?.let(noteMapper::toDomain) }

    override suspend fun insertNote(note: Note) =
        noteDao.createNote(noteMapper.toEntity(note))

    override suspend fun updateNote(note: Note) =
        noteDao.updateNote(noteMapper.toEntity(note))

    override suspend fun deleteNote(note: Note) =
        noteDao.deleteNote(noteMapper.toEntity(note))

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query).map { it.map(noteMapper::toDomain) }

    override fun getNotesByTag(tag: String): Flow<List<Note>> =
        noteDao.getAllNotes().map { entities ->
            entities
                .filter { it.tags.split(",").contains(tag) }
                .map(noteMapper::toDomain)
        }

    override fun getNotesSortedByCreatedAt(): Flow<List<Note>> =
        noteDao.getNotesSortedByCreatedAt().map { it.map(noteMapper::toDomain) }

    override fun getNotesSortedByUpdatedAt(): Flow<List<Note>> =
        noteDao.getNotesSortedByUpdatedAt().map { it.map(noteMapper::toDomain) }

    override fun searchNotesSortedByCreatedAt(query: String): Flow<List<Note>> =
        noteDao.searchNotesSortedByCreatedAt(query).map { it.map(noteMapper::toDomain) }


    override fun getNotesByFolderId(folderId: Int): Flow<List<Note>> =
        noteDao.getNotesByFolderId(folderId).map { it.map(noteMapper::toDomain) }

    override fun getNotesWithoutFolder(): Flow<List<Note>> =
        noteDao.getNotesWithoutFolder().map { it.map(noteMapper::toDomain) }

    override fun getRootNotes(): Flow<List<Note>> =
        noteDao.getRootNotes().map { it.map(noteMapper::toDomain) }

    override fun getNotesInFolder(folderId: Int): Flow<List<Note>> =
        noteDao.getNotesInFolder(folderId).map { it.map(noteMapper::toDomain) }

    /* note in folder*/
    override suspend fun createNoteInFolder(folderId: Int): Int {
        val entity = NoteEntity(
            title = "",
            content = "",
            folderId = folderId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return noteDao.insertNoteAndGetId(entity).toInt()
    }

    override suspend fun assignNotesToFolder(noteIds: Set<Int>, folderId: Int) =
        noteDao.assignNotesToFolder(noteIds, folderId)

    override suspend fun removeNotesFromFolder(noteIds: Set<Int>) =
        noteDao.removeNotesFromFolder(noteIds)

    override suspend fun deleteNotesByIds(noteIds: Set<Int>) =
        noteDao.deleteNotesByIds(noteIds)

    override fun getDeletedNotes(): Flow<List<Note>> =
        noteDao.getDeletedNotes().map { it.map(noteMapper::toDomain) }

    override suspend fun restoreNote(noteId: Int) {
        val entity = noteDao.getNoteById(noteId).firstOrNull() ?: return
        noteDao.updateNote(entity.copy(isDeleted = false, deletedAt = null))
    }

    override suspend fun permanentDeleteNote(noteId: Int) =
        noteDao.deleteNoteById(noteId)

    override suspend fun emptyTrash() =
        noteDao.deleteAllDeletedNotes()

    override suspend fun exportNoteToTxt(context: Context, noteId: Int, uri: Uri): Boolean {
        return try {
            val entity = noteDao.getNoteById(noteId).firstOrNull() ?: return false
            val note = noteMapper.toDomain(entity)
            val content = buildString {
                appendLine("Title: ${note.title}")
                appendLine("Date: ${note.createdAt}")
                appendLine()
                appendLine(note.content)
            }
            context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun importNoteFromTxt(context: Context, uri: Uri): Int? {
        return try {
            val text = context.contentResolver
                .openInputStream(uri)?.bufferedReader()?.readText() ?: return null

            val lines = text.lines()
            var title = "Notă importată"
            var startIndex = 0

            if (lines.isNotEmpty() && lines[0].startsWith("Title:", ignoreCase = true)) {
                title = lines[0].substringAfter("Title:").trim()
                startIndex = 1
            }
            if (startIndex < lines.size && lines[startIndex].startsWith("Date:", ignoreCase = true)) {
                startIndex++
            }
            if (startIndex < lines.size && lines[startIndex].isBlank()) {
                startIndex++
            }

            val content = lines.drop(startIndex).joinToString("\n").trim()

            val newNote = Note(
                id = 0,
                title = title,
                content = content,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                isFavorite = false,
                folderId = null,
                deletedAt = null,
                tags = emptyList()
            )

            noteDao.createNote(noteMapper.toEntity(newNote))
            noteDao.getAllNotes().first().maxByOrNull { it.id }?.id

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}