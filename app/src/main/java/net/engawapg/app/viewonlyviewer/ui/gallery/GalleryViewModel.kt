package net.engawapg.app.viewonlyviewer.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.engawapg.app.viewonlyviewer.data.GalleryItem
import net.engawapg.app.viewonlyviewer.data.GalleryModel
import net.engawapg.app.viewonlyviewer.data.SettingsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

sealed interface GalleryUiState {
    object Loading: GalleryUiState

    data class Success(
        val galleryItems: List<GalleryItem>,
        val tapCountToOpenSettings: Int,
        val multiGoBack: Int,
    ): GalleryUiState
}

class GalleryViewModel: ViewModel(), KoinComponent {
    private val model: GalleryModel = get()
    private val settingsRepo: SettingsRepository = get()
    private var ignoreFolderIds: Set<String>? = null

    /* Images shown in gallery depend on the settings. */
    private val appSettingsStream = settingsRepo.appSettingsFlow.onEach { appSettings ->
        ignoreFolderIds = appSettings.ignoreFolderIds
        viewModelScope.launch(Dispatchers.IO) {
            /* load() emits new items list to model.galleryItemsFlow. */
            model.load(appSettings.ignoreFolderIds)
        }
    }

    val uiState: StateFlow<GalleryUiState> = combine(
        appSettingsStream,
        model.galleryItemsFlow,
    ) { appSettings, items ->
        GalleryUiState.Success(
            galleryItems = items,
            tapCountToOpenSettings = appSettings.tapCountToOpenSettings,
            multiGoBack = appSettings.multiGoBack,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GalleryUiState.Loading
    )

    fun loadGallery() {
        viewModelScope.launch(Dispatchers.IO) {
            val ids = ignoreFolderIds
            if (ids != null) {
                model.load(ids)
            }
        }
    }
}