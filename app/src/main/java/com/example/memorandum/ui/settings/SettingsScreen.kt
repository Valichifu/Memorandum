package com.example.memorandum.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onTrashClick: () -> Unit,
    onLayoutChange: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showTrashDaysDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 3.dp,
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = { Text("Setări") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Înapoi")
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSection(title = "Language") {
                    SettingsOption(
                        text = uiState.language,
                        onClick = { showLanguageDialog = true },
                        trailingIcon = Icons.Default.ArrowForward
                    )
                }
            }

            item {
                SettingsSection(title = "Appearance") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Mode")
                        Switch(
                            checked = uiState.isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode() }
                        )
                    }
                }
            }

            item {
                SettingsSection(title = "Recent Delete") {
                    SettingsOption(
                        text = "Open Trash",
                        onClick = onTrashClick,
                        trailingIcon = Icons.Default.Delete
                    )
                    Spacer(Modifier.height(8.dp))
                    SettingsOption(
                        text = "Auto-delete after ${uiState.trashAutoDeleteDays} days",
                        onClick = { showTrashDaysDialog = true },
                        trailingIcon = Icons.Default.ArrowForward
                    )
                }
            }

            item {
                SettingsSection(title = "Layout") {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLayout(false)
                                    onLayoutChange(false)
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rows")
                            RadioButton(
                                selected = !uiState.isTileLayout,
                                onClick = {
                                    viewModel.setLayout(false)
                                    onLayoutChange(false)
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLayout(true)
                                    onLayoutChange(true)
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tiles")
                            RadioButton(
                                selected = uiState.isTileLayout,
                                onClick = {
                                    viewModel.setLayout(true)
                                    onLayoutChange(true)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text("Select Language") },
                text = {
                    Column {
                        listOf("English", "Romanian", "Ukrainian", "Spanish", "Portuguese").forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setLanguage(lang)
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.language == lang,
                                    onClick = {
                                        viewModel.setLanguage(lang)
                                        showLanguageDialog = false
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(lang)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showTrashDaysDialog) {
            AlertDialog(
                onDismissRequest = { showTrashDaysDialog = false },
                title = { Text("Auto-delete Period") },
                text = {
                    Column {
                        listOf(7, 14, 30, 60, 90).forEach { days ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setTrashAutoDelete(days)
                                        showTrashDaysDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.trashAutoDeleteDays == days,
                                    onClick = {
                                        viewModel.setTrashAutoDelete(days)
                                        showTrashDaysDialog = false
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("$days days")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTrashDaysDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Divider()
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
fun SettingsOption(
    text: String,
    onClick: () -> Unit,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text)
        if (trailingIcon != null) {
            Icon(
                trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}