package net.engawapg.app.viewonlyviewer

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AppSettings(
    val ignoreFolderIds: Set<String>,
)

class SettingsRepository(private val context: Context) {
    private object PreferenceKeys {
        val IGNORE_FOLDERS = stringSetPreferencesKey("ignore_folders")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val ignoreFolderIds = preferences[PreferenceKeys.IGNORE_FOLDERS] ?: setOf()
        AppSettings(ignoreFolderIds)
    }

    suspend fun addIgnoreFolderId(id: String) {
        context.dataStore.edit { preferences ->
            val ids = preferences[PreferenceKeys.IGNORE_FOLDERS] ?: setOf()
            preferences[PreferenceKeys.IGNORE_FOLDERS] = ids + id
        }
    }

    suspend fun removeIgnoreFolderId(id: String) {
        context.dataStore.edit { preferences ->
            val ids = preferences[PreferenceKeys.IGNORE_FOLDERS] ?: setOf()
            preferences[PreferenceKeys.IGNORE_FOLDERS] = ids - id
        }
    }
}