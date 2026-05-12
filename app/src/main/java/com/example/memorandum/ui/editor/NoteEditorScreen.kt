package com.example.memorandum.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memorandum.ui.components.ErrorScreen
import com.example.memorandum.ui.components.LoadingScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Int,
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let { targetUri ->
            val currentNote = (uiState as? NoteEditorUiState.Success)?.note
            currentNote?.let { note ->
                scope.launch {
                    val success = viewModel.exportNote(context, note.id, targetUri)
                    snackbarHostState.showSnackbar(
                        if (success) "Export reușit!" else "Eroare la export"
                    )
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            scope.launch {
                val newId = viewModel.importNote(context, sourceUri)

                if (newId != null) {
                    viewModel.loadNote(newId)
                    snackbarHostState.showSnackbar("Import reușit! Nota este deschisă.")
                } else {
                    snackbarHostState.showSnackbar("Eroare la import")
                }
            }
        }
    }

    val handleSave = {
        val currentNote = (uiState as? NoteEditorUiState.Success)?.note
        val tags = currentNote?.tags ?: emptyList()
        viewModel.saveNote(title, content, tags)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == -1) "Notă nouă" else "Editează") },
                navigationIcon = {
                    IconButton(onClick = {
                        handleSave()
                        onBack()
                    }) {
                        Icon(Icons.Default.West, contentDescription = "Înapoi")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Meniu")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export as .txt") },
                                leadingIcon = { Icon(Icons.Default.Upload, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    val currentNote = (uiState as? NoteEditorUiState.Success)?.note
                                    currentNote?.let { note ->
                                        val fileName = (note.title.ifBlank { "nota" }).take(20) + ".txt"
                                        exportLauncher.launch(fileName)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import from .txt") },
                                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    importLauncher.launch("text/*")
                                }
                            )
                        }
                    }

                    IconButton(onClick = {
                        handleSave()
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Salvează")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is NoteEditorUiState.Loading -> LoadingScreen()
            is NoteEditorUiState.Error -> ErrorScreen(message = state.message)
            is NoteEditorUiState.Success -> {
                LaunchedEffect(noteId) {
                    viewModel.loadNote(noteId)
                }

                LaunchedEffect(state.note?.id) {
                    title = state.note?.title ?: ""
                    content = state.note?.content ?: ""

                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Numele notei") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("Începe să scrii...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
            else -> {}
        }
    }
}