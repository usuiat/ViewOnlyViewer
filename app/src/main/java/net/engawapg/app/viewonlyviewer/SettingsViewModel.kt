package net.engawapg.app.viewonlyviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class SettingsViewModel: ViewModel(), KoinComponent {
    private val settingsRepo: SettingsRepository = get()

    val uiState: StateFlow<SettingsUiState> = settingsRepo.appSettingsFlow.map { appSettings ->
        SettingsUiState(
            loading = false,
            darkTheme = appSettings.darkTheme,
            colorTheme = appSettings.colorTheme,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiStateDefault
    )

    fun setDarkTheme(darkTheme: DarkThemeSetting) {
        viewModelScope.launch {
            settingsRepo.setDarkTheme(darkTheme)
        }
    }

    fun setColorTheme(colorTheme: ColorThemeSetting) {
        viewModelScope.launch {
            settingsRepo.setColorTheme(colorTheme)
        }
    }
}

data class SettingsUiState(
    val loading: Boolean,
    val darkTheme: DarkThemeSetting?,
    val colorTheme: ColorThemeSetting?,
)

private val SettingsUiStateDefault = SettingsUiState(
    loading = true,
    /* The following values are not used when loading. */
    darkTheme = null,
    colorTheme = null,
)