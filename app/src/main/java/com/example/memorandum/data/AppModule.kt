package com.example.memorandum.data

import com.example.memorandum.data.repository.FakeNoteRepository
import com.example.memorandum.domain.repository.NoteRepository

object AppModule {
    val noteRepository: NoteRepository = FakeNoteRepository()
}
// TODO: switch to OfflineNoteRepository