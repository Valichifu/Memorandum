package com.example.memorandum.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorandum.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val isTileLayout: Boolean = false,
    val language: String = "English",
    val trashAutoDeleteDays: Int = 30  // ✅ Câmpul există
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // ✅ FIX: Adaugă settingsRepository.trashAutoDeleteDays în combine
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.isDarkMode,
        settingsRepository.isTileLayout,
        settingsRepository.language,
        settingsRepository.trashAutoDeleteDays  // <--- ASTA LIPSEA!
    ) { darkMode, tileLayout, language, trashDays ->  // <--- ȘI AICI (4 parametri)
        SettingsUiState(darkMode, tileLayout, language, trashDays)  // <--- ȘI AICI (4 argumente)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun toggleDarkMode() {
        viewModelScope.launch {
            settingsRepository.setDarkMode(!uiState.value.isDarkMode)
        }
    }

    fun setLayout(isTile: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLayout(isTile)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(lang)
        }
    }

    fun setTrashAutoDelete(days: Int) {
        viewModelScope.launch {
            settingsRepository.setTrashAutoDeleteDays(days)
        }
    }
}