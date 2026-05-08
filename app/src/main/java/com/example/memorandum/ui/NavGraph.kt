package com.example.memorandum.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.memorandum.ui.editor.NoteEditorScreen
import com.example.memorandum.ui.list.NoteListScreen
import com.example.memorandum.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object NoteList : Screen("note_list")
    object NoteEditor : Screen("note_editor/{noteId}") {
        fun createRoute(noteId: String = "new") = "note_editor/$noteId"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.NoteList.route
    ) {
        composable(Screen.NoteList.route) {
            NoteListScreen(
                onNoteClick = { id ->
                    navController.navigate(Screen.NoteEditor.createRoute(id))
                },
                onAddNote = {
                    navController.navigate(Screen.NoteEditor.createRoute())
                }
            )
        }
        composable(Screen.NoteEditor.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteEditorScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}