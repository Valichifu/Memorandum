package com.example.memorandum.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.memorandum.R
import com.example.memorandum.ui.editor.NoteEditorScreen
import com.example.memorandum.ui.list.NoteListScreen
import com.example.memorandum.ui.settings.SettingsScreen
import com.example.memorandum.ui.trash.TrashScreen
import com.airbnb.lottie.compose.*
import java.text.SimpleDateFormat
import java.util.*

// --- CONFIGURAȚIA RUTELOR ---
sealed class Screen(val route: String) {
    object WelcomeLogo : Screen("welcome_logo")
    object NoteList : Screen("note_list")
    object NoteEditor : Screen("note_editor/{noteId}") {
        fun createRoute(noteId: String = "new") = "note_editor/$noteId"
    }
    object FolderList : Screen("folder_list")
    object FolderEditor : Screen("folder_editor/{folderId}") {
        fun createRoute(folderId: String = "new") = "folder_editor/$folderId"
    }
    object Settings : Screen("settings")
    object Trash : Screen("trash")
}

// --- NAVGRAPH PRINCIPAL ---
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.WelcomeLogo.route
    ) {
        // 1. Ecranul de animație (Welcome)
        composable(Screen.WelcomeLogo.route) {
            WelcomeScreen(navController = navController)
        }

        // 2. Ecranul cu listă de Foldere
        composable(Screen.FolderList.route) {
            FolderListScreen(
                onFolderClick = { folderName ->
                    // Aici poți modifica să trimită folderName dacă vrei note specifice
                    navController.navigate(Screen.NoteList.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // 3. Ecranul cu listă de Note
        composable(Screen.NoteList.route) {
            NoteListScreen(
                onNoteClick = { id -> navController.navigate(Screen.NoteEditor.createRoute(id.toString())) },
                onAddNote = { navController.navigate(Screen.NoteEditor.createRoute()) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // 4. Ecranul Editor Note
        composable(Screen.NoteEditor.route) { backStackEntry ->
            val noteIdStr = backStackEntry.arguments?.getString("noteId")
            val noteId = noteIdStr?.toIntOrNull() ?: -1
            NoteEditorScreen(noteId = noteId, onBack = { navController.popBackStack() })
        }

        // 5. Ecranul Setări
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onTrashClick = { navController.navigate(Screen.Trash.route) },
                onLayoutChange = { /* Modificare layout */ }
            )
        }

        // 6. Ecranul Coș de Gunoi
        composable(Screen.Trash.route) {
            TrashScreen(
                onBack = { navController.popBackStack() },
                onRestoreNote = { /* TODO */ },
                onDeletePermanent = { /* TODO */ }
            )
        }
    }
}

// --- ECRANUL DE BINE AI VENIT (ANIMAȚIA) ---
@Composable
fun WelcomeScreen(navController: NavHostController) {
    // Încarcă animația din res/raw/logo.json
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.logo))
    val progress by animateLottieCompositionAsState(composition)

    // Când animația ajunge la final (100%), navigăm la Foldere
    LaunchedEffect(progress) {
        if (progress >= 1f) {
            navController.navigate(Screen.FolderList.route) {
                popUpTo(Screen.WelcomeLogo.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3E3A3A)), // Fundalul gri închis să se potrivească
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
    }
}

// --- ECRANUL DE FOLDERE ---
data class FolderModel(val name: String, val date: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    onFolderClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    val folders = remember {
        mutableStateListOf(
            FolderModel("Documente", "12.05.2026"),
            FolderModel("Facultate", "10.05.2026")
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF3E3A3A))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Menu, null, tint = Color.Gray, modifier = Modifier.size(35.dp))
                }
                Icon(Icons.Default.SwapVert, null, tint = Color.Gray, modifier = Modifier.size(35.dp))
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                val filteredList = folders.filter { it.name.contains(searchQuery, ignoreCase = true) }
                items(filteredList) { folder ->
                    FolderItemUI(
                        folder = folder,
                        onClick = { onFolderClick(folder.name) },
                        onDelete = { folders.remove(folder) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search...", color = Color.LightGray) },
                modifier = Modifier.weight(1f).height(55.dp),
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(alpha = 0.3f),
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) }
            )

            Spacer(modifier = Modifier.width(10.dp))

            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF532B2B),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, null, tint = Color.Red, modifier = Modifier.size(30.dp))
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Crează Folder") },
            text = {
                TextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = { Text("Nume...") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newFolderName.isNotBlank()) {
                        val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                        folders.add(FolderModel(newFolderName, date))
                        newFolderName = ""; showDialog = false
                    }
                }) { Text("Adaugă") }
            }
        )
    }
}

@Composable
fun FolderItemUI(folder: FolderModel, onClick: (String) -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
        Text(folder.date, color = Color.White, fontSize = 12.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .background(Color.Gray.copy(0.4f), RoundedCornerShape(15.dp))
                .clickable { onClick(folder.name) }
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Folder, null, tint = Color.Yellow)
            Text(folder.name, color = Color.White, modifier = Modifier.padding(start = 12.dp).weight(1f), fontSize = 18.sp)

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreHoriz, null, tint = Color.White)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Rename") }, onClick = { expanded = false }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete(); expanded = false }, leadingIcon = { Icon(Icons.Default.Delete, null) })
                    DropdownMenuItem(text = { Text("Lock") }, onClick = { expanded = false }, leadingIcon = { Icon(Icons.Default.Lock, null) })
                    DropdownMenuItem(text = { Text("Favorite") }, onClick = { expanded = false }, leadingIcon = { Icon(Icons.Default.Favorite, null) })
                }
            }
        }
    }
}