package com.example.memorandum.ui.trash

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memorandum.domain.model.Note
import com.example.memorandum.ui.components.EmptyScreen
import com.example.memorandum.ui.components.ErrorScreen
import com.example.memorandum.ui.components.LoadingScreen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onRestoreNote: (Int) -> Unit,
    onDeletePermanent: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showConfirmEmpty by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trashscreen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if ((uiState as? TrashUiState.Success)?.deletedNotes?.isNotEmpty() == true) {
                        IconButton(onClick = {
                            val notes = (uiState as TrashUiState.Success).deletedNotes
                            viewModel.restoreAllNotes(notes)
                        }) {
                            Icon(Icons.Default.Restore, "Restore all")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if ((uiState as? TrashUiState.Success)?.deletedNotes?.isNotEmpty() == true) {
                FloatingActionButton(
                    onClick = { showConfirmEmpty = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(Icons.Default.DeleteForever, "Clear trash")
                }
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is TrashUiState.Loading -> LoadingScreen()

            is TrashUiState.Error -> ErrorScreen(message = state.message)

            is TrashUiState.Success -> {
                if (state.deletedNotes.isEmpty()) {
                    EmptyScreen(message = "Trash screen")
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.deletedNotes, key = { it.id }) { note ->
                            TrashNoteItem(
                                note = note,
                                onRestore = { viewModel.restoreNoteById(note.id) },
                                onDeletePermanent = { viewModel.permanentDeleteNoteById(note.id) }
                            )
                        }
                    }
                }
            }
        }

        if (showConfirmEmpty) {
            AlertDialog(
                onDismissRequest = { showConfirmEmpty = false },
                title = { Text("Clear trash?") },
                text = { Text("All deleted notes will be permanently removed.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val notes = (uiState as TrashUiState.Success).deletedNotes
                            viewModel.emptyTrash(notes)
                            showConfirmEmpty = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete all")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmEmpty = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TrashNoteItem(
    note: Note,
    onRestore: () -> Unit,
    onDeletePermanent: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title.ifBlank { "No title" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (note.content.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = note.content.take(80) + if (note.content.length > 80) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onRestore) {
                    Icon(
                        Icons.Default.Restore,
                        contentDescription = "Restore",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deleted: ${note.deletedAt?.let {
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it)
                    } ?: "Unknown"
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = onDeletePermanent,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}