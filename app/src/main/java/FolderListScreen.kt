package com.example.memorandum.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.memorandum.domain.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    viewModel: NoteListViewModel,
    onFolderClick: (Int) -> Unit,
    onAddNote: () -> Unit,
    onAddFolder: () -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTileLayout by viewModel.isTileLayout.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!isSearchExpanded) Text("MEMORANDUM")
                    else {
                        TextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.onSearchQueryChanged(it)
                            },
                            placeholder = { Text("Caută...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Înapoi")
                    }
                },
                actions = {
                    // Buton Căutare
                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search, null)
                    }
                    // Buton Setări
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, "Setări")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showFabMenu) {
                    SmallFloatingActionButton(
                        onClick = {
                            onAddFolder()
                            showFabMenu = false
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, "Folder Nou")
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            onAddNote()
                            showFabMenu = false
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.NoteAdd, "Notă Nouă")
                    }
                }
                FloatingActionButton(onClick = { showFabMenu = !showFabMenu }) {
                    Icon(if (showFabMenu) Icons.Default.Close else Icons.Default.Add, null)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.White)) {
            when (val state = uiState) {
                is NoteListUiState.Success -> {
                    if (isTileLayout) {
                        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
                            items(state.notes, key = { it.id }) { note ->
                                FolderItem(
                                    note = note,
                                    onClick = { onFolderClick(note.id) },
                                    onDelete = { viewModel.deleteNote(note) },
                                    onToggleFavorite = { viewModel.toggleFavorite(note) },
                                    onRename = { newName -> viewModel.renameNote(note, newName) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp)) {
                            items(state.notes, key = { it.id }) { note ->
                                FolderItem(
                                    note = note,
                                    onClick = { onFolderClick(note.id) },
                                    onDelete = { viewModel.deleteNote(note) },
                                    onToggleFavorite = { viewModel.toggleFavorite(note) },
                                    onRename = { newName -> viewModel.renameNote(note, newName) }
                                )
                            }
                        }
                    }
                }
                is NoteListUiState.Error -> Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                else -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun FolderItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var currentName by remember { mutableStateOf(note.title) }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Redenumește") },
            text = { TextField(value = currentName, onValueChange = { currentName = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = { onRename(currentName); showRenameDialog = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Anulează") }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Iconiță de tip folder pentru aspect
            Icon(Icons.Default.Folder, null, modifier = Modifier.padding(end = 12.dp), tint = MaterialTheme.colorScheme.primary)

            Text(note.title, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)

            // Buton rapid pentru Favorite
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Favorite",
                    tint = if (note.isFavorite) Color(0xFFFFD700) else Color.Gray
                )
            }

            // Meniul cu restul opțiunilor (Rename, Delete)
            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null) }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Redenumește") },
                    onClick = { showRenameDialog = true; showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                )
                DropdownMenuItem(
                    text = { Text("Șterge", color = Color.Red) },
                    onClick = { onDelete(); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                )
            }
        }
    }
}