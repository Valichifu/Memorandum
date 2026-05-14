package com.example.memorandum.data.repository

import android.content.Context
import android.net.Uri
import com.example.memorandum.data.local.NoteDao
import com.example.memorandum.data.local.NoteMapper
import com.example.memorandum.data.local.NoteSearchEntity
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
        val entity = noteMapper.toEntity(note)
        noteDao.createNote(entity)

        // Sincronizare cu @FTS
        val savedNote = noteDao.getAllNotes().first().maxByOrNull { it.id }
        savedNote?.let {
            noteDao.insertSearchNote(
                NoteSearchEntity(rowId = it.id, title = it.title, content = it.content)
            )
        }
    }

    override suspend fun updateNote(note: Note) {
        val entity = noteMapper.toEntity(note)
        noteDao.updateNote(entity)
        // Обновляем поисковый индекс
        noteDao.insertSearchNote(
            NoteSearchEntity(rowId = entity.id, title = entity.title, content = entity.content)
        )
    }

    override suspend fun deleteNote(note: Note) {
        val entity = noteMapper.toEntity(note)
        noteDao.deleteNote(entity)

        noteDao.deleteSearchNote(entity.id)
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return if (query.isBlank()) {
            getAllNotes()
        } else {
            //Folosim FTS
            noteDao.searchNotesFts(query).map { entities ->
                entities.map { noteMapper.toDomain(it) }
            }
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
            noteEntity.copy(isDeleted = false, deletedAt = null)
        )
    }

    override suspend fun permanentDeleteNote(noteId: Int) {
        noteDao.deleteNoteById(noteId)
        noteDao.deleteSearchNote(noteId)
    }

    override suspend fun emptyTrash() {
        noteDao.deleteAllDeletedNotes()
    }

    override suspend fun exportNoteToTxt(context: Context, noteId: Int, uri: Uri): Boolean {
        return try {
            val noteEntity = noteDao.getNoteById(noteId).firstOrNull() ?: return false
            val note = noteMapper.toDomain(noteEntity)
            val content = "Title: ${note.title}\nDate: ${note.createdAt}\n\n${note.content}"
            context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun importNoteFromTxt(context: Context, uri: Uri): Int? {
        return try {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return null
            val lines = text.lines().filter { it.isNotBlank() }
            val title = lines.firstOrNull()?.replace("Title: ", "") ?: "Imported"
            val content = lines.drop(1).joinToString("\n")
            val newNote = Note(id = 0, title = title.trim(), content = content.trim())
            insertNote(newNote) // Используем наш insertNote для авто-синхронизации с FTS
            noteDao.getAllNotes().first().maxByOrNull { it.id }?.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}