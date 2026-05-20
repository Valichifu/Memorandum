package com.example.memorandum.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memorandum.R
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

    var showMenu by remember { mutableStateOf(false) }

    val exportSuccessMsg = stringResource(R.string.export_success)
    val exportErrorMsg = stringResource(R.string.export_error)
    val importSuccessMsg = stringResource(R.string.import_success)
    val importErrorMsg = stringResource(R.string.import_error)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let { targetUri ->
            val currentNote = (uiState as? NoteEditorUiState.Success)?.note
            currentNote?.let { note ->
                scope.launch {
                    val success = viewModel.exportNote(context, note.id, targetUri)
                    snackbarHostState.showSnackbar(if (success) exportSuccessMsg else exportErrorMsg)
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
                    snackbarHostState.showSnackbar(importSuccessMsg)
                } else {
                    snackbarHostState.showSnackbar(importErrorMsg)
                }
            }
        }
    }

    val handleSave = {
        val currentNote = (uiState as? NoteEditorUiState.Success)?.note
        val tags = currentNote?.tags ?: emptyList()
        viewModel.saveNote(title, content.trim(), tags)
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 3.dp,
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = {
                        Text(stringResource(if (noteId == -1) R.string.new_note else R.string.edit_note))
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            handleSave()
                            onBack()
                        }) {
                            Icon(
                                imageVector = Icons.Default.West,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.menu)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.export_txt)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Upload, contentDescription = null)
                                    },
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
                                    text = { Text(stringResource(R.string.import_txt)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Download, contentDescription = null)
                                    },
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
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.save)
                            )
                        }
                    }
                )
            }
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

                LaunchedEffect(key1 = state.note?.id) {
                    title = state.note?.title ?: ""
                    content = (state.note?.content ?: "").trim()
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
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.note_title_placeholder),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        TextField(
                            value = content,
                            onValueChange = { content = it },
                            placeholder = { Text(stringResource(R.string.note_content_placeholder)) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 28.dp),
                            textStyle = TextStyle(fontSize = 20.sp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        Text(
                            text = "${content.length}",
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 12.dp, bottom = 8.dp),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            else -> {}
        }
    }
}