package net.engawapg.app.viewonlyviewer

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val SettingTapCountToOpenSettings = SettingDefinition(
    intPreferencesKey("TapCountToOpenSettings"), 1
)

val SettingMultiGoBack = SettingDefinition(
    intPreferencesKey("MultiGoBack"), 1
)

class SettingDefinition<T>(private val key: Preferences.Key<T>, private val default: T) {
    @Composable
    fun getState(context: Context): State<T> =
        get(context).collectAsState(default)

    private fun get(context: Context): Flow<T> =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: default
        }

    suspend fun set(value: T, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}
