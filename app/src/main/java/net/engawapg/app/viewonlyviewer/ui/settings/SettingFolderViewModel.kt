package net.engawapg.app.viewonlyviewer.ui.settings

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.engawapg.app.viewonlyviewer.data.GalleryModel
import net.engawapg.app.viewonlyviewer.data.SettingsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Stable
data class SettingFolderItem(
    val name: String,
    val id: Int,
    val parentPath: String,
    val thumbnailUri: Uri,
    val visibility: Boolean
)

sealed interface SettingFolderUiState {
    object Loading: SettingFolderUiState

    data class Loaded(
        val settingFolderItems: List<SettingFolderItem>,
    ): SettingFolderUiState
}

class SettingFolderViewModel: ViewModel(), KoinComponent {
    private val galleryModel: GalleryModel = get()
    private val settingsRepo: SettingsRepository = get()

    val uiState: StateFlow<SettingFolderUiState> = combine(
        galleryModel.folderItemsFlow,
        settingsRepo.appSettingsFlow,
    ) { folderItems, appSettings ->
        val ignoreFolderIds = appSettings.ignoreFolderIds.map { it.toInt() }
        val settingFolderItems = folderItems.map { item ->
            SettingFolderItem(
                name = item.name,
                id = item.id,
                parentPath = item.path,
                thumbnailUri = item.thumbnailUri,
                visibility = !ignoreFolderIds.contains(item.id),
            )
        }
        SettingFolderUiState.Loaded(
            settingFolderItems = settingFolderItems,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingFolderUiState.Loading,
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            galleryModel.loadFolders()
        }
    }

    fun setFolderVisibility(id: Int, visibility: Boolean) {
        viewModelScope.launch {
            if (visibility) {
                settingsRepo.removeIgnoreFolderId(id.toString())
            }
            else {
                settingsRepo.addIgnoreFolderId(id.toString())
            }
        }
    }
}