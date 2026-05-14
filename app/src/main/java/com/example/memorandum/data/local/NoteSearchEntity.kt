package com.example.memorandum.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Fts4 //Cautarea prin notite cu  indexe
@Entity(tableName = "notes_search")
data class NoteSearchEntity(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowId: Int,
    val title: String,
    val content: String
)
