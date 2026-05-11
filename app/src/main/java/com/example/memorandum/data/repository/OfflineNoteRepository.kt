package com.example.memorandum.data.repository

import com.example.memorandum.data.local.NoteDao
import com.example.memorandum.data.local.NoteMapper
import com.example.memorandum.domain.model.Note
import com.example.memorandum.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
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
}