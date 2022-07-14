package net.engawapg.app.viewonlyviewer

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AppSettings(
    val hideFolderIds: Set<String>,
)

class SettingsRepository(private val context: Context) {
    private object PreferenceKeys {
        val HIDE_FOLDERS = stringSetPreferencesKey("hide_folders")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val hideFolderIds = preferences[PreferenceKeys.HIDE_FOLDERS] ?: setOf()
        AppSettings(hideFolderIds)
    }

    suspend fun addHideFolderId(id: String) {
        context.dataStore.edit { preferences ->
            val ids = preferences[PreferenceKeys.HIDE_FOLDERS] ?: setOf()
            preferences[PreferenceKeys.HIDE_FOLDERS] = ids + id
        }
    }

    suspend fun removeHideFolderId(id: String) {
        context.dataStore.edit { preferences ->
            val ids = preferences[PreferenceKeys.HIDE_FOLDERS] ?: setOf()
            preferences[PreferenceKeys.HIDE_FOLDERS] = ids - id
        }
    }
}