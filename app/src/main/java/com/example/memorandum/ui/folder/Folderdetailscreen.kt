package com.example.memorandum.ui.folder

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memorandum.domain.model.Note
import com.example.memorandum.ui.components.NoteTileCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FolderDetailScreen(
    folderId: Int,
    onNoteClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: FolderDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(folderId) {
        viewModel.loadFolder(folderId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val isTileLayout by viewModel.isTileLayout.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedNoteIds by viewModel.selectedNoteIds.collectAsState()

    var showFabMenu by remember { mutableStateOf(false) }
    var showNotePicker by remember { mutableStateOf(false) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var titleDraft by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val folder = (uiState as? FolderDetailUiState.Success)?.folder

    LaunchedEffect(folder) {
        if (folder != null && titleDraft.isEmpty()) {
            titleDraft = folder.name
        }
    }

    LaunchedEffect(isEditingTitle) {
        if (isEditingTitle) focusRequester.requestFocus()
    }

    if (showNotePicker) {
        val allNotes by viewModel.allUnassignedNotes.collectAsState()
        NotePickerDialog(
            notes = allNotes,
            onConfirm = { selectedIds ->
                viewModel.addNotesToFolder(selectedIds, folderId)
                showNotePicker = false
            },
            onDismiss = { showNotePicker = false }
        )
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedNoteIds.size} selectate") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.exitSelectionMode() }) {
                            Icon(Icons.Default.Close, contentDescription = "Anulselecție")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.removeNotesFromFolder(selectedNoteIds)
                            viewModel.exitSelectionMode()
                        }) {
                            Icon(Icons.Default.FolderOff, contentDescription = "Scoate din folder")
                        }
                        IconButton(onClick = {
                            viewModel.deleteSelectedNotes()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Șterge", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        if (isEditingTitle) {
                            TextField(
                                value = titleDraft,
                                onValueChange = { titleDraft = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                                )
                            )
                        } else {
                            Text(
                                text = folder?.name ?: "Folder",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.combinedClickable(
                                    onClick = {
                                        titleDraft = folder?.name ?: ""
                                        isEditingTitle = true
                                    }
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Înapoi")
                        }
                    },
                    actions = {
                        if (isEditingTitle) {
                            IconButton(onClick = {
                                if (titleDraft.isNotBlank()) {
                                    viewModel.renameFolder(folderId, titleDraft)
                                }
                                isEditingTitle = false
                            }) {
                                Icon(Icons.Default.Check, "Salvează")
                            }
                        } else {
                            IconButton(onClick = { viewModel.toggleLayout() }) {
                                Icon(
                                    if (isTileLayout) Icons.Default.ViewList else Icons.Default.GridView,
                                    contentDescription = "Schimbă layout"
                                )
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                Column(horizontalAlignment = Alignment.End) {
                    if (showFabMenu) {
                        SmallFloatingActionButton(
                            onClick = {
                                showNotePicker = true
                                showFabMenu = false
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.PlaylistAdd, "Adaugă note existente")
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                viewModel.createNoteInFolder(folderId)
                                    .let { newNoteId -> onNoteClick(newNoteId) }
                                showFabMenu = false
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.NoteAdd, "Notă nouă")
                        }
                    }
                    FloatingActionButton(onClick = { showFabMenu = !showFabMenu }) {
                        Icon(
                            if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is FolderDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is FolderDetailUiState.Success -> {
                    if (state.notes.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Folderul este gol",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Apasă + pentru a adăuga note",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        if (isTileLayout) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.notes, key = { it.id }) { note ->
                                    NoteTileCard(
                                        note = note,
                                        isSelected = selectedNoteIds.contains(note.id),
                                        onClick = {
                                            if (isSelectionMode) viewModel.toggleNoteSelection(note.id)
                                            else onNoteClick(note.id)
                                        },
                                        onLongClick = {
                                            viewModel.enterSelectionMode()
                                            viewModel.toggleNoteSelection(note.id)
                                        }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.notes, key = { it.id }) { note ->
                                    FolderNoteItem(
                                        note = note,
                                        isSelected = selectedNoteIds.contains(note.id),
                                        isSelectionMode = isSelectionMode,
                                        onClick = {
                                            if (isSelectionMode) viewModel.toggleNoteSelection(note.id)
                                            else onNoteClick(note.id)
                                        },
                                        onLongClick = {
                                            viewModel.enterSelectionMode()
                                            viewModel.toggleNoteSelection(note.id)
                                        },
                                        onRemoveFromFolder = { viewModel.removeNoteFromFolder(note) },
                                        onDelete = { viewModel.deleteNote(note) }
                                    )
                                }
                            }
                        }
                    }
                }

                is FolderDetailUiState.Error -> {
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FolderNoteItem(
    note: Note,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemoveFromFolder: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note.title.ifBlank { "(fără titlu)" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (!isSelectionMode) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Scoate din folder") },
                            leadingIcon = { Icon(Icons.Default.FolderOff, null) },
                            onClick = { onRemoveFromFolder(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Șterge", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { onDelete(); showMenu = false }
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}