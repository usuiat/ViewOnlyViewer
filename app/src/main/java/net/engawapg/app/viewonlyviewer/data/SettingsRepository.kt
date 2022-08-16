package net.engawapg.app.viewonlyviewer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class DarkThemeSetting(val value: Int) {
    Off(0),
    On(1),
    UseSystemSettings(2);

    companion object {
        fun fromInt(value: Int) = DarkThemeSetting.values().first { it.value == value }
    }
    fun toInt() = value
}

enum class ColorThemeSetting(val value: Int) {
    AppTheme(0),
    Wallpaper(1);

    companion object {
        fun fromInt(value: Int) = ColorThemeSetting.values().first { it.value == value }
    }
    fun toInt() = value
}

data class AppSettings(
    val ignoreFolderIds: Set<String>,
    val darkTheme: DarkThemeSetting,
    val colorTheme: ColorThemeSetting,
    val tapCountToOpenSettings: Int,
    val multiGoBack: Int,
)

class SettingsRepository(private val context: Context) {
    private object PreferenceKeys {
        val IGNORE_FOLDERS = stringSetPreferencesKey("ignore_folders")
        val DARK_THEME = intPreferencesKey("DarkTheme")
        val COLOR_THEME = intPreferencesKey("ColorTheme")
        val TAP_COUNT_TO_OPEN_SETTINGS = intPreferencesKey("TapCountToOpenSettings")
        val MULTI_GO_BACK = intPreferencesKey("MultiGoBack")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val ignoreFolderIds = preferences[PreferenceKeys.IGNORE_FOLDERS] ?: setOf()
        val darkTheme = preferences[PreferenceKeys.DARK_THEME]?.let {
            DarkThemeSetting.fromInt(it)
        } ?: DarkThemeSetting.UseSystemSettings
        val colorTheme = preferences[PreferenceKeys.COLOR_THEME]?.let {
            ColorThemeSetting.fromInt(it)
        } ?: ColorThemeSetting.Wallpaper
        val tapCountToOpenSettings = preferences[PreferenceKeys.TAP_COUNT_TO_OPEN_SETTINGS] ?: 1
        val multiGoBack = preferences[PreferenceKeys.MULTI_GO_BACK] ?: 1

        AppSettings(
            ignoreFolderIds,
            darkTheme,
            colorTheme,
            tapCountToOpenSettings,
            multiGoBack,
        )
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

    suspend fun setDarkTheme(darkTheme: DarkThemeSetting) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DARK_THEME] = darkTheme.toInt()
        }
    }

    suspend fun setColorTheme(colorTheme: ColorThemeSetting) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.COLOR_THEME] = colorTheme.toInt()
        }
    }

    suspend fun setTapCountToOpenSettings(tapCount: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.TAP_COUNT_TO_OPEN_SETTINGS] = tapCount
        }
    }

    suspend fun setMultiGoBack(multiGoBack: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.MULTI_GO_BACK] = multiGoBack
        }
    }
}