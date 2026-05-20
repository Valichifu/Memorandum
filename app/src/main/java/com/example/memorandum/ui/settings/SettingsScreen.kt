package com.example.memorandum.ui.settings

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memorandum.R

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

    val context = LocalContext.current

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 3.dp,
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.settings)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
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
                SettingsSection(title = stringResource(R.string.language)) {
                    SettingsOption(
                        text = uiState.language,
                        onClick = { showLanguageDialog = true },
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.appearance)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.dark_mode))
                        Switch(
                            checked = uiState.isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode() }
                        )
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.material_you_colors))
                            Switch(
                                checked = uiState.dynamicColor,
                                onCheckedChange = { viewModel.toggleDynamicColor() }
                            )
                        }
                    }
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.recent_delete)) {
                    SettingsOption(
                        text = stringResource(R.string.open_trash),
                        onClick = onTrashClick,
                        trailingIcon = Icons.Default.Delete
                    )
                    Spacer(Modifier.height(8.dp))
                    SettingsOption(
                        text = stringResource(R.string.auto_delete_after_days, uiState.trashAutoDeleteDays),
                        onClick = { showTrashDaysDialog = true },
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.layout)) {
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
                            Text(stringResource(R.string.rows))
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
                            Text(stringResource(R.string.tiles))
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
                title = { Text(stringResource(R.string.select_language)) },
                text = {
                    Column {
                        listOf("English", "Romanian", "Ukrainian", "Spanish", "Portuguese", "Russian").forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setLanguageAndRestart(lang) {
                                            showLanguageDialog = false
                                            (context as? ComponentActivity)?.recreate()
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.language == lang,
                                    onClick = {
                                        viewModel.setLanguageAndRestart(lang) {
                                            showLanguageDialog = false
                                            (context as? ComponentActivity)?.recreate()
                                        }
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
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        if (showTrashDaysDialog) {
            AlertDialog(
                onDismissRequest = { showTrashDaysDialog = false },
                title = { Text(stringResource(R.string.auto_delete_period)) },
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
                                Text(stringResource(R.string.days, days))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTrashDaysDialog = false }) {
                        Text(stringResource(R.string.cancel))
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
        HorizontalDivider()
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