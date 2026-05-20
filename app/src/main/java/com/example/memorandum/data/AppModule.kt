package com.example.memorandum.data

import android.content.Context
import com.example.memorandum.data.local.FolderDao
import com.example.memorandum.data.local.FolderMapper
import com.example.memorandum.data.local.NoteDao
import com.example.memorandum.data.local.NoteDatabase
import com.example.memorandum.data.local.NoteMapper
import com.example.memorandum.data.repository.FakeFolderRepository
import com.example.memorandum.data.repository.FakeNoteRepository
import com.example.memorandum.data.repository.FolderRepository
import com.example.memorandum.data.repository.OfflineFolderRepository
import com.example.memorandum.data.repository.OfflineNoteRepository
import com.example.memorandum.data.repository.SettingsRepository
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

    private const val USE_FAKE = false

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
        return if (USE_FAKE) FakeNoteRepository()
        else OfflineNoteRepository(noteDao, noteMapper)
    }

    @Provides
    @Singleton
    fun provideFolderDao(database: NoteDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    @Singleton
    fun provideFolderRepository(
        folderDao: FolderDao,
        folderMapper: FolderMapper
    ): FolderRepository {
        return if (USE_FAKE) FakeFolderRepository()
        else OfflineFolderRepository(folderDao, folderMapper)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }
}