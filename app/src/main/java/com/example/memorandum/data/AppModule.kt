package com.example.memorandum.data

import com.example.memorandum.data.repository.FakeNoteRepository
import com.example.memorandum.domain.repository.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNoteRepository(): NoteRepository {
        return FakeNoteRepository()
    }
}