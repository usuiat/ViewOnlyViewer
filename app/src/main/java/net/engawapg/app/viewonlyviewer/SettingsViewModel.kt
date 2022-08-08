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

sealed interface SettingsUiState {
    object Loading: SettingsUiState

    data class Success(
        val darkTheme: DarkThemeSetting,
        val colorTheme: ColorThemeSetting,
        val tapCountToOpenSettings: Int,
        val multiGoBack: Int,
    ): SettingsUiState
}

class SettingsViewModel: ViewModel(), KoinComponent {
    private val settingsRepo: SettingsRepository = get()

    val uiState: StateFlow<SettingsUiState> = settingsRepo.appSettingsFlow.map { appSettings ->
        SettingsUiState.Success(
            darkTheme = appSettings.darkTheme,
            colorTheme = appSettings.colorTheme,
            tapCountToOpenSettings = appSettings.tapCountToOpenSettings,
            multiGoBack = appSettings.multiGoBack,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState.Loading
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

    fun setTapCountToOpenSettings(tapCount: Int) {
        viewModelScope.launch {
            settingsRepo.setTapCountToOpenSettings(tapCount)
        }
    }

    fun setMultiGoBack(multiGoBack: Int) {
        viewModelScope.launch {
            settingsRepo.setMultiGoBack(multiGoBack)
        }
    }
}
