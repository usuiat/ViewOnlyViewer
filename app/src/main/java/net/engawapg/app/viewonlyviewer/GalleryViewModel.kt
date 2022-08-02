package net.engawapg.app.viewonlyviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

data class GalleryUiState(
    val loading: Boolean,
    /* The following properties are used only when loading is false. */
    val galleryItems: List<GalleryItem>?,
    val tapCountToOpenSettings: Int?,
    val multiGoBack: Int?,
)

private val GalleryUiStateDefault = GalleryUiState(
    loading = true,
    /* The following properties are used only when loading is false. */
    galleryItems = null,
    tapCountToOpenSettings = null,
    multiGoBack = null,
)

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
        GalleryUiState(
            loading = false,
            galleryItems = items,
            tapCountToOpenSettings = appSettings.tapCountToOpenSettings,
            multiGoBack = appSettings.multiGoBack,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GalleryUiStateDefault
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