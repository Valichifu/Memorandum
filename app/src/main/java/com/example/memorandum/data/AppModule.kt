package com.example.memorandum.data

import android.content.Context
import com.example.memorandum.data.local.NoteDao
import com.example.memorandum.data.local.NoteDatabase
import com.example.memorandum.data.local.NoteMapper
import com.example.memorandum.data.repository.OfflineNoteRepository
import com.example.memorandum.domain.repository.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(@ApplicationContext context: Context): NoteDatabase {
        return NoteDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: NoteDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDao: NoteDao,
        noteMapper: NoteMapper
    ): NoteRepository {
        return OfflineNoteRepository(noteDao, noteMapper)
    }
}