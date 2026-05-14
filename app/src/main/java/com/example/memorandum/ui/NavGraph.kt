package com.example.memorandum.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.memorandum.ui.list.FolderListScreen
import com.example.memorandum.ui.list.NoteListViewModel

// Definirea rutelor pentru navigare
sealed class Screen(val route: String) {
    object FolderList : Screen("folder_list")
    object NoteEditor : Screen("note_editor/{noteId}") {
        fun passId(id: Int) = "note_editor/$id"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.FolderList.route
    ) {
        // 1. Ecranul Principal (Lista de Foldere/Note)
        composable(route = Screen.FolderList.route) {
            val viewModel: NoteListViewModel = hiltViewModel()

            FolderListScreen(
                viewModel = viewModel,
                onFolderClick = { noteId ->
                    // Navighează către editor pentru a vedea/edita nota
                    navController.navigate(Screen.NoteEditor.passId(noteId))
                },
                onAddNote = {
                    // Navighează către editor cu ID -1 pentru o notă nouă
                    navController.navigate(Screen.NoteEditor.passId(-1))
                },
                onAddFolder = {
                    // Poți adăuga logică aici sau naviga către un alt ecran
                },
                onSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 2. Ecranul de Editare Note
        composable(
            route = Screen.NoteEditor.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
            // Aici apelezi NoteEditorScreen(noteId = noteId, ...)
            // Deocamdată lăsăm un placeholder dacă nu ai fișierul creat
        }

        // 3. Ecranul de Setări
        composable(route = Screen.Settings.route) {
            // Aici apelezi SettingsScreen(...)
        }
    }
}
//acesta este