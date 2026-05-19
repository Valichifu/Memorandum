package com.example.memorandum.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.memorandum.R
import com.example.memorandum.domain.model.Note
import com.example.memorandum.ui.components.NoteTileCard
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.PaddingValues

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onNoteClick: (Int) -> Unit,
    onAddNote: () -> Unit,
    onSettings: () -> Unit,
    onFolderClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTileLayout by viewModel.isTileLayout.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedNoteIds by viewModel.selectedNoteIds.collectAsState()
    val currentSortType by viewModel.currentSortType.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSortDialog by remember { mutableStateOf(false) }

    // Stări temporare doar pentru design (schiță)
    var filterFavoritesOnly by remember { mutableStateOf(false) }
    var filterNoTitleOnly by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 3.dp,
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                if (isSelectionMode) {
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(R.string.selected_count, selectedNoteIds.size),
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.exitSelectionMode() }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.deleteSelectedNotes() }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                } else {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = onFolderClick) {
                                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = stringResource(R.string.folders))
                            }
                        },
                        title = {
                            Text(stringResource(R.string.notes_title), style = MaterialTheme.typography.titleLarge)
                        },
                        actions = {
                            IconButton(onClick = { showSortDialog = true }) {
                                Icon(Icons.Default.Sort, contentDescription = stringResource(R.string.sort_by))
                            }
                            IconButton(onClick = onSettings) {
                                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {

            // --- SCHIȚĂ FILTRE (DESIGN DOAR) ---
            if (!isSelectionMode) {
                Spacer(modifier = Modifier.height(4.dp)) // spațiu mic de sus

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = filterFavoritesOnly,
                            onClick = {
                                filterFavoritesOnly = !filterFavoritesOnly
                            },
                            label = { Text("Favorite") },
                            leadingIcon = {
                                if (filterFavoritesOnly) {
                                    Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                                } else {
                                    Icon(Icons.Default.Star, null, Modifier.size(18.dp))
                                }
                            }
                        )
                    }

                    item {
                        FilterChip(
                            selected = filterNoTitleOnly,
                            onClick = {
                                filterNoTitleOnly = !filterNoTitleOnly
                            },
                            label = { Text("Fără titlu") },
                            leadingIcon = {
                                if (filterNoTitleOnly) {
                                    Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                                } else {
                                    Icon(Icons.Default.TextFormat, null, Modifier.size(18.dp))
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(0.dp)) // spațiu mic până la notițe
            }

            when (val state = uiState) {
                is NoteListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                is NoteListUiState.Success -> {
                    // Între timp, lista rămâne neschimbată (ia toate notele direct din state)
                    // TODO CÂND ADĂUGĂM FUNCȚIONALITATEA: Aici vom folosi o listă filtrată venită din ViewModel
                    val notesToShow = state.notes

                    if (notesToShow.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Note, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(16.dp))
                                Text(stringResource(R.string.no_notes_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(stringResource(R.string.no_notes_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                items(notesToShow, key = { it.id }) { note ->
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
                                        },
                                        onToggleFavorite = {
                                            if (!isSelectionMode) {
                                                viewModel.toggleFavorite(note)
                                            }
                                        }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().weight(1f),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(notesToShow, key = { it.id }) { note ->
                                    NoteItem(
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
                                        onDelete = { viewModel.deleteNote(note) },
                                        onToggleFavorite = { viewModel.toggleFavorite(note) }
                                    )
                                }
                            }
                        }
                    }
                }

                is NoteListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.onSearchQueryChanged(it)
                    },
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Default.Clear, null)
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
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_note), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        if (showSortDialog) {
            SortDialog(
                currentSortType = currentSortType,
                onDismiss = { showSortDialog = false },
                onSortSelected = { sortType ->
                    viewModel.sortBy(sortType)
                    showSortDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false
) {
    val cardColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            note.isFavorite -> Color(0xFFFFF8E1) // galben deschis
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "cardColor"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (note.isFavorite && !isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "glowAlpha"
    )

    // Fix pentru culori text în Dark Mode când nota este favorită
    val textColor = if (note.isFavorite && !isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = if (note.isFavorite && !isSelected) Color.Black.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .then(
                if (note.isFavorite && !isSelected)
                    Modifier.border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = glowAlpha),
                                Color(0xFFFFA000).copy(alpha = glowAlpha)
                            )
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                else Modifier
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (note.isFavorite && !isSelected) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { stringResource(R.string.no_title) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor // Forțat negru dacă e favorită
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = remember(note.createdAt) {
                        SimpleDateFormat("dd.MM.yyyy • HH:mm", Locale.getDefault())
                            .format(Date(note.createdAt))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = secondaryTextColor // Forțat gri închis/negru dacă e favorită
                )
            }

            Box {
                if (!isSelectionMode) {
                    val starColor by animateColorAsState(
                        targetValue = if (note.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 300),
                        label = "starColor"
                    )
                    val starScale by animateFloatAsState(
                        targetValue = if (note.isFavorite) 1.2f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "starScale"
                    )

                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = starColor,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(starScale)
                        )
                    }
                } else {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(2.dp)
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
        title = { Text(stringResource(R.string.sort_by)) },
        text = {
            Column {
                SortOption(stringResource(R.string.sort_created_desc), currentSortType == SortType.CREATED_AT_DESC) { onSortSelected(SortType.CREATED_AT_DESC) }
                SortOption(stringResource(R.string.sort_updated_desc), currentSortType == SortType.UPDATED_AT_DESC) { onSortSelected(SortType.UPDATED_AT_DESC) }
                SortOption(stringResource(R.string.sort_alphabetical), currentSortType == SortType.ALPHABETICAL_ASC) { onSortSelected(SortType.ALPHABETICAL_ASC) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
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
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}