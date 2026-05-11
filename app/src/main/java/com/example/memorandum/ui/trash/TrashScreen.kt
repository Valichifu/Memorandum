package com.example.memorandum.ui.trash

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memorandum.ui.components.EmptyScreen
import com.example.memorandum.ui.components.ErrorScreen
import com.example.memorandum.ui.components.LoadingScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onRestoreNote: (Int) -> Unit,
    onDeletePermanent: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coș de gunoi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Înapoi")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Restore all */ }
            ) {
                Icon(Icons.Default.Restore, "Restaurează tot")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is TrashUiState.Loading -> LoadingScreen()

            is TrashUiState.Error -> ErrorScreen(
                message = state.message
            )

            is TrashUiState.Success -> {
                if (state.deletedNotes.isEmpty()) {
                    EmptyScreen(message = "Coșul este gol")
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(state.deletedNotes) { note ->
                                Text(text = note.title)
                        }
                    }
                }
            }
        }
    }
}