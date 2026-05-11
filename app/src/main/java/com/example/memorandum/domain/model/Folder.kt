package com.example.memorandum.domain.model

data class Folder(
    val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    //val parentId: Int? = null
)