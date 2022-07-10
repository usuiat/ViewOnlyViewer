package net.engawapg.app.viewonlyviewer

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Stable
data class SettingFolderItem(
    val name: String,
    val parentPath: String,
    val thumbnailUri: Uri,
    val visibility: Boolean
)

class SettingFolderViewModel: ViewModel(), KoinComponent {
    private val galleryModel: GalleryModel = get()

    var folders = mutableStateListOf<SettingFolderItem>()
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            galleryModel.loadFolders()
            val settingFolderItems = galleryModel.folders.map {
                SettingFolderItem(
                    name = it.name,
                    parentPath = it.path,
                    thumbnailUri = it.thumbnailUri,
                    visibility = true,
                )
            }
            folders.addAll(settingFolderItems)
        }
    }

    fun setFolderVisibility(folder: SettingFolderItem, visibility: Boolean) {
        val index = folders.indexOf(folder)
        folders[index] = folders[index].copy(visibility = visibility)
    }
}