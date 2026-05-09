package com.example.memorandum.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun NoteListScreen(
    onNoteClick: (Int) -> Unit,
    onAddNote: () -> Unit
) {
    // TODO: Lucian   Implement the UI for the note list screen, including a list of notes and an "Add Note" button.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("NoteListScreen - TODO")
    }
}