package com.example.memorandum.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
    fun NoteEditorScreen(
        noteId: String?,
        onBack: () -> Unit
    ) {
        // TODO: Lucian  Implement the UI for the note editor screen, including fields for the note title and content, and buttons for saving and deleting the note.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("NoteEditorScreen - TODO")
        }
    }
