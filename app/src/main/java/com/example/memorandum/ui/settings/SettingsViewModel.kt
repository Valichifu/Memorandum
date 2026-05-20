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
    val trashAutoDeleteDays: Int = 30,
    val dynamicColor: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.isDarkMode,
        settingsRepository.isTileLayout,
        settingsRepository.language,
        settingsRepository.trashAutoDeleteDays,
        settingsRepository.dynamicColor
    ) { darkMode, tileLayout, language, trashDays,dynamic ->
        SettingsUiState(darkMode, tileLayout, language, trashDays, dynamic)
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
    fun setLanguageAndRestart(lang: String, onRestart: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setLanguage(lang)
            onRestart()
        }
    }

    fun setTrashAutoDelete(days: Int) {
        viewModelScope.launch {
            settingsRepository.setTrashAutoDeleteDays(days)
        }
    }
    fun toggleDynamicColor() {
        viewModelScope.launch {
            settingsRepository.setDynamicColor(!uiState.value.dynamicColor)
        }
    }
}