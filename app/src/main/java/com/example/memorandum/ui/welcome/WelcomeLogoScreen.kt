package com.example.memorandum.ui.folder

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
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

// 1. Modelul pentru Folder (îl punem aici sus în fișier)
data class FolderModel(val name: String, val date: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    onFolderClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    // 2. Stările pentru Search și Listă
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    // Lista de foldere (se va reseta la repornire dacă nu e salvată în DB)
    val folders = remember {
        mutableStateListOf(
            FolderModel("Documente", "06.05.2026"),
            FolderModel("Proiecte", "12.05.2026")
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF3E3A3A))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            // Header (Butonul de Meniu/Setări din desenul tău)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Menu, null, tint = Color.Gray, modifier = Modifier.size(35.dp))
                }
                Icon(Icons.Default.SwapVert, null, tint = Color.Gray, modifier = Modifier.size(35.dp))
            }

            // 3. Lista de Foldere (Aici apare ce ai desenat tu)
            LazyColumn(modifier = Modifier.weight(1f)) {
                // Filtrăm lista în funcție de ce scrii la Search
                val filteredList = folders.filter { it.name.contains(searchQuery, ignoreCase = true) }

                items(filteredList) { folder ->
                    FolderItemUI(
                        folder = folder,
                        onClick = { onFolderClick(folder.name) },
                        onDelete = { folders.remove(folder) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Spațiu pentru bara de jos
        }

        // 4. Bara de Search și Butonul Plus (Fixate jos)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search...", color = Color.LightGray) },
                modifier = Modifier.weight(1f).height(55.dp),
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.colors(
                    // Pentru fundal
                    focusedContainerColor = Color.Gray.copy(alpha = 0.3f),
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.3f),
                    // Pentru textul scris
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    // Eliminăm liniile de sub search
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

    // 5. Dialogul pentru folder nou (apare peste ecran)
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nume Folder Nou") },
            text = {
                TextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newFolderName.isNotBlank()) {
                        val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                        folders.add(FolderModel(newFolderName, date))
                        newFolderName = ""
                        showDialog = false
                    }
                }) { Text("Adaugă") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Anulează") }
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
            Icon(Icons.Default.Folder, null, tint = Color.Yellow, modifier = Modifier.size(28.dp))
            Text(
                text = folder.name,
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 12.dp).weight(1f)
            )

            // Meniul cu 3 puncte (Rename, Delete, etc.)
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreHoriz, null, tint = Color.White)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = { expanded = false },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { onDelete(); expanded = false },
                        leadingIcon = { Icon(Icons.Default.Delete, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Lock") },
                        onClick = { expanded = false },
                        leadingIcon = { Icon(Icons.Default.Lock, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Favorite") },
                        onClick = { expanded = false },
                        leadingIcon = { Icon(Icons.Default.Favorite, null) }
                    )
                }
            }
        }
    }
}