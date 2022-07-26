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
        MainUiState(
            loading = false,
            darkTheme = settings.darkTheme,
            colorTheme = settings.colorTheme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiStateDefault
    )
}

data class MainUiState(
    val loading: Boolean,
    val darkTheme: DarkThemeSetting,
    val colorTheme: ColorThemeSetting,
)

private val MainUiStateDefault = MainUiState(
    loading = true,
    /* The following values are not used when loading is true. */
    darkTheme = DarkThemeSetting.UseSystemSettings,
    colorTheme = ColorThemeSetting.Wallpaper,
)