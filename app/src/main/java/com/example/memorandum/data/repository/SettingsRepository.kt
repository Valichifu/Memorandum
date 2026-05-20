package com.example.memorandum.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*
import androidx.core.content.edit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context)  {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val IS_TILE_LAYOUT = booleanPreferencesKey("is_tile_layout")
        val LANGUAGE = stringPreferencesKey("language")
        val TRASH_AUTO_DELETE_DAYS = intPreferencesKey("trash_auto_delete_days")

        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DARK_MODE] ?: false
        }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    val isTileLayout: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_TILE_LAYOUT] ?: false
        }

    suspend fun setLayout(isTile: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_TILE_LAYOUT] = isTile
        }
    }

    val language: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE] ?: "English"
        }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = lang
        }

        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit { putString("language", lang) }
    }
    val trashAutoDeleteDays: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TRASH_AUTO_DELETE_DAYS] ?: 30
        }

    suspend fun setTrashAutoDeleteDays(days: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TRASH_AUTO_DELETE_DAYS] = days
        }
    }

    val dynamicColor: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
        }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }
}