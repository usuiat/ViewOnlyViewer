package net.engawapg.app.viewonlyviewer

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

class SettingFolderViewModel: ViewModel(), KoinComponent {
    private val galleryModel: GalleryModel = get()
    private val settingsRepo: SettingsRepository = get()

    var folders = mutableStateListOf<SettingFolderItem>()
        private set

    init {
        viewModelScope.launch {

            launch(Dispatchers.IO) {
                galleryModel.loadFolders()
            }.join()

            val appSettings = settingsRepo.appSettingsFlow.first()
            val hideFolderIds = appSettings.hideFolderIds.map { it.toInt() }
            val settingFolderItems = galleryModel.folders.map { item ->
                SettingFolderItem(
                    name = item.name,
                    id = item.id,
                    parentPath = item.path,
                    thumbnailUri = item.thumbnailUri,
                    visibility = !hideFolderIds.contains(item.id)
                )
            }
            folders.addAll(settingFolderItems)
        }
    }

    fun setFolderVisibility(folder: SettingFolderItem, visibility: Boolean) {
        viewModelScope.launch {
            if (visibility) {
                settingsRepo.removeHideFolderId(folder.id.toString())
            }
            else {
                settingsRepo.addHideFolderId(folder.id.toString())
            }
        }
        val index = folders.indexOf(folder)
        folders[index] = folders[index].copy(visibility = visibility)
    }
}