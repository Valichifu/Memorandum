package com.example.memorandum.data.local

import com.example.memorandum.domain.model.Folder
import javax.inject.Inject

class FolderMapper @Inject constructor() {

    fun toDomain(entity: FolderEntity): Folder {
        return Folder(
            id = entity.id,
            name = entity.name,
            createdAt = entity.createdAt,
            //parentId = entity.parentId
        )
    }

    fun toEntity(domain: Folder): FolderEntity {
        return FolderEntity(
            id = domain.id,
            name = domain.name,
            createdAt = domain.createdAt,
            //parentId = domain.parentId
        )
    }
}