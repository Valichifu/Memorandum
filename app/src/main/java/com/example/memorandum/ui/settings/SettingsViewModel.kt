package com.example.memorandum.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val language: String = "English",
    val isDarkMode: Boolean = false,
    val isTileLayout: Boolean = false,
    val trashAutoDeleteDays: Int = 30
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(language = language)
            // TODO: Save to DataStore
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isDarkMode = !_state.value.isDarkMode)
            // TODO: Save to DataStore
        }
    }

    fun toggleLayout(isTile: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isTileLayout = isTile)
            // TODO: Save to DataStore
        }
    }

    fun setTrashAutoDelete(days: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(trashAutoDeleteDays = days)
            // TODO: Save to DataStore
        }
    }
}