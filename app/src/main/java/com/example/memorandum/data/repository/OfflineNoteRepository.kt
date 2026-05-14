package com.example.memorandum.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.memorandum.data.local.NoteDao
import com.example.memorandum.data.local.NoteMapper
import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class OfflineNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteMapper: NoteMapper
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { noteMapper.toDomain(it) }
        }
    }

    override fun getNoteById(id: Int): Flow<Note?> {
        return noteDao.getNoteById(id).map { entity ->
            entity?.let { noteMapper.toDomain(it) }
        }
    }

    override suspend fun insertNote(note: Note) {
        noteDao.createNote(noteMapper.toEntity(note))
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(noteMapper.toEntity(note))
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(noteMapper.toEntity(note))
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query).map { entities ->
            entities.map { noteMapper.toDomain(it) }
        }
    }

    override fun getNotesByTag(tag: String): Flow<List<Note>> {
        return noteDao.getAllNotes()
            .map { entities ->
                entities.filter { it.tags.split(",").contains(tag) }
            }
            .map { entities ->
                entities.map { noteMapper.toDomain(it) }
            }
    }

    override fun getNotesSortedByCreatedAt(): Flow<List<Note>> {
        return noteDao.getNotesSortedByCreatedAt().map { entities ->
            entities.map { noteMapper.toDomain(it) }
        }
    }

    override fun getNotesSortedByUpdatedAt(): Flow<List<Note>> {
        return noteDao.getNotesSortedByUpdatedAt().map { entities ->
            entities.map { noteMapper.toDomain(it) }
        }
    }

    override fun searchNotesSortedByCreatedAt(query: String): Flow<List<Note>> {
        return noteDao.searchNotesSortedByCreatedAt(query).map { entities ->
            entities.map { noteMapper.toDomain(it) }
        }
    }

    override fun getNotesInFolder(folderId: Int): Flow<List<Note>> {
        return noteDao.getNotesInFolder(folderId).map { entities ->
            entities.map { noteMapper.toDomain(it) }
        }
    }

    override fun getRootNotes(): Flow<List<Note>> {
        return noteDao.getRootNotes().map { entities ->
            entities.map { noteMapper.toDomain(it) }
        }
    }

    override fun getDeletedNotes(): Flow<List<Note>> {
        return noteDao.getDeletedNotes().map { entities ->
            entities.map { noteMapper.toDomain(it) }
        }
    }

    override suspend fun restoreNote(noteId: Int) {
        val noteEntity = noteDao.getNoteById(noteId).firstOrNull() ?: return
        noteDao.updateNote(
            noteEntity.copy(
                isDeleted = false,
                deletedAt = null
            )
        )
    }

    override suspend fun permanentDeleteNote(noteId: Int) {
        noteDao.deleteNoteById(noteId)
    }

    override suspend fun emptyTrash() {
        noteDao.deleteAllDeletedNotes()
    }

    override suspend fun exportNoteToTxt(context: Context, noteId: Int, uri: Uri): Boolean {
        return try {
            val noteEntity = noteDao.getNoteById(noteId).firstOrNull() ?: return false
            val note = noteMapper.toDomain(noteEntity)

            val content = buildString {
                appendLine("Title: ${note.title}")
                appendLine("Date: ${note.createdAt}")
                appendLine()
                appendLine(note.content)
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun importNoteFromTxt(context: Context, uri: Uri): Int? {
        return try {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return null

            val lines = text.lines()

            var title = "Notă importată"
            var contentStartIndex = 0

            if (lines.isNotEmpty() && lines[0].startsWith("Title:", ignoreCase = true)) {
                title = lines[0].substringAfter("Title:").trim()
                contentStartIndex = 1
            }
            if (contentStartIndex < lines.size && lines[contentStartIndex].startsWith("Date:", ignoreCase = true)) {
                contentStartIndex++
            }
            if (contentStartIndex < lines.size && lines[contentStartIndex].isBlank()) {
                contentStartIndex++
            }
            val content = if (contentStartIndex < lines.size) {
                lines.drop(contentStartIndex).joinToString("\n").trim()
            } else {
                ""
            }

            val newNote = Note(
                id = 0,
                title = title.trim(),
                content = content.trim(),
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