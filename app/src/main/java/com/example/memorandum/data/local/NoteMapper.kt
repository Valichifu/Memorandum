package com.example.memorandum.data.local


import com.example.memorandum.domain.model.Note
import javax.inject.Inject

class NoteMapper @Inject constructor() {

    fun toDomain(entity: NoteEntity): Note {
        return Note(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            tags = entity.tags.split(",").filter { it.isNotBlank() },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isFavorite = entity.isFavorite,
            folderId = entity.folderId,
            isDeleted = entity.isDeleted,
            deletedAt = entity.deletedAt

        )
    }

    fun toEntity(domain: Note): NoteEntity {
        return NoteEntity(
            id = domain.id,
            title = domain.title,
            content = domain.content,
            tags = domain.tags.joinToString(","),
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            isFavorite = domain.isFavorite,
            folderId = domain.folderId,
            isDeleted = domain.isDeleted,
            deletedAt = domain.deletedAt

        )
    }
}