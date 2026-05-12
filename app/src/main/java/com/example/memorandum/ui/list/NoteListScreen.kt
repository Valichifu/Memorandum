package com.example.memorandum.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.memorandum.domain.model.Note
import com.example.memorandum.ui.components.NoteTileCard
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onNoteClick: (Int) -> Unit,
    onAddNote: () -> Unit,
    onSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTileLayout by viewModel.isTileLayout.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 3.dp,
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NOTES",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sortare")
                        }

                        IconButton(onClick = onSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Setari")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is NoteListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                is NoteListUiState.Success -> {
                    if (state.notes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Note, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(16.dp))
                                Text("Nu există note", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Apasă pe + pentru a crea una nouă", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        if (isTileLayout) {
                            LazyVerticalGrid(
                                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize().weight(1f),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.notes, key = { it.id }) { note ->
                                    NoteTileCard(note = note, onClick = { onNoteClick(note.id) })
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().weight(1f),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (state.notes.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = SimpleDateFormat("dd.MM.yyyy", LocalLocale.current.platformLocale).format(Date(state.notes.first().createdAt)),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                items(state.notes, key = { it.id }) { note ->
                                    NoteItem(
                                        note = note,
                                        onClick = { onNoteClick(note.id) },
                                        onDelete = { viewModel.deleteNote(note) },
                                        onToggleFavorite = { viewModel.toggleFavorite(note) }
                                    )
                                }
                            }
                        }
                    }
                }

                is NoteListUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .imePadding(), // ✅ Previne acoperirea de către tastatură
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.onSearchQueryChanged(it)
                    },
                    placeholder = { Text("Search...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Caută") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Default.Clear, "Șterge")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                FloatingActionButton(
                    onClick = onAddNote,
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Adaugă", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
                Text("Sortează", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                // TODO: Adaugă opțiuni reale de sortare/tag-uri
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note.title.ifBlank { "Name" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "Meniu")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (note.isFavorite) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if (note.isFavorite) "Remove from favorites" else "Add to favorites")
                            }
                        },
                        onClick = {
                            onToggleFavorite()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun SortDialog(
    currentSortType: SortType,
    onDismiss: () -> Unit,
    onSortSelected: (SortType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sortează după") },
        text = {
            Column {
                SortOption(
                    text = "Data creării (recente primele)",
                    selected = currentSortType == SortType.CREATED_AT_DESC
                ) {
                    onSortSelected(SortType.CREATED_AT_DESC)
                }
                SortOption(
                    text = "Data modificării (recente primele)",
                    selected = currentSortType == SortType.UPDATED_AT_DESC
                ) {
                    onSortSelected(SortType.UPDATED_AT_DESC)
                }
                SortOption(
                    text = "Alfabetic (A-Z)",
                    selected = currentSortType == SortType.ALPHABETICAL_ASC
                ) {
                    onSortSelected(SortType.ALPHABETICAL_ASC)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Închide")
            }
        }
    )
}

@Composable
fun SortOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}