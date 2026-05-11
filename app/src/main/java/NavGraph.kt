import com.example.memorandum.ui.WelcomeLogo.WelcomeScreen

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
        startDestination = "welcome" // Etapa 9: Aplicația pornește cu Logo
    ) {
        // Etapa 10: Ruta pentru ecranul de bun venit
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }

        // Etapa 11: Rutele existente rămân aici
        composable(Screen.NoteList.route) {
            NoteListScreen(
                onNoteClick = { id -> navController.navigate(Screen.NoteEditor.createRoute(id)) },
                onAddNote = { navController.navigate(Screen.NoteEditor.createRoute()) }
            )
        }
        // ... restul rutelelor (Editor, Settings)
    }
}