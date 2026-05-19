package com.example.memorandum.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.memorandum.ui.editor.NoteEditorScreen
import com.example.memorandum.ui.list.NoteListScreen
import com.example.memorandum.ui.list.NoteListViewModel
import com.example.memorandum.ui.settings.SettingsScreen
import com.example.memorandum.ui.settings.SettingsViewModel
import com.example.memorandum.ui.welcome.WelcomeLogoScreen
import com.example.memorandum.ui.folder.FolderListScreen
import com.example.memorandum.ui.folder.FolderDetailScreen
import com.example.memorandum.ui.trash.TrashScreen
import com.example.memorandum.ui.trash.TrashViewModel

sealed class Screen(val route: String) {
    object WelcomeLogo : Screen("welcome_logo")
    object NoteList : Screen("note_list")
    object NoteEditor : Screen("note_editor/{noteId}") {
        fun createRoute(noteId: String = "new") = "note_editor/$noteId"
    }
    object FolderList : Screen("folder_list")
    object FolderDetail : Screen("folder_detail/{folderId}") {
        fun createRoute(folderId: Int) = "folder_detail/$folderId"
    }
    object Settings : Screen("settings")
    object Trash : Screen("trash")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.WelcomeLogo.route
    ) {
        composable(Screen.WelcomeLogo.route) {
            WelcomeLogoScreen(
                onAnimationComplete = {
                    navController.navigate(Screen.NoteList.route) {
                        popUpTo(Screen.WelcomeLogo.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.NoteList.route) {
            val viewModel: NoteListViewModel = hiltViewModel()
            NoteListScreen(
                viewModel = viewModel,
                onNoteClick = { id ->
                    navController.navigate(Screen.NoteEditor.createRoute(id.toString()))
                },
                onAddNote = {
                    navController.navigate(Screen.NoteEditor.createRoute())
                },
                onSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onFolderClick = {
                    navController.navigate(Screen.FolderList.route)
                }
            )
        }

        composable(Screen.NoteEditor.route) { backStackEntry ->
            val noteIdStr = backStackEntry.arguments?.getString("noteId")
            val noteId = noteIdStr?.toIntOrNull() ?: -1
            NoteEditorScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onTrashClick = {
                    navController.navigate(Screen.Trash.route)
                },
                onLayoutChange = { isTiles ->
                    viewModel.setLayout(isTiles)
                }
            )
        }

        composable(Screen.FolderList.route) {
            FolderListScreen(
                onFolderClick = { folderId ->
                    navController.navigate(Screen.FolderDetail.createRoute(folderId))
                },
                onAddFolder = { /* TODO: dialog pentru folder nou */ },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.FolderDetail.route) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")?.toIntOrNull() ?: -1
            FolderDetailScreen(
                folderId = folderId,
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteEditor.createRoute(noteId.toString()))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Trash.route) {
            val viewModel: TrashViewModel = hiltViewModel()
            TrashScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}