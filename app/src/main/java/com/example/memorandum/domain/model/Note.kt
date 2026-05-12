package com.example.memorandum.domain.model

data class Note(
    val id: Int = 0,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val folderId: Int? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)