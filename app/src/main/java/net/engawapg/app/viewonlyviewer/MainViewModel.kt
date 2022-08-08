package net.engawapg.app.viewonlyviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class MainViewModel: ViewModel(), KoinComponent {
    private val settingsRepo: SettingsRepository = get()

    val uiState: StateFlow<MainUiState> = settingsRepo.appSettingsFlow.map { settings ->
        MainUiState.Success(
            darkTheme = settings.darkTheme,
            colorTheme = settings.colorTheme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState.Loading
    )
}

sealed interface MainUiState {
    object Loading: MainUiState

    data class Success(
        val darkTheme: DarkThemeSetting,
        val colorTheme: ColorThemeSetting,
    ): MainUiState
}