package net.engawapg.app.viewonlyviewer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

data class SettingFolderUiState(
    val folderList: List<FolderItem> = emptyList()
)

class SettingFolderViewModel: ViewModel(), KoinComponent {
    private val galleryModel: GalleryModel = get()
    var uiState by mutableStateOf(SettingFolderUiState())
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            galleryModel.loadFolders()
            uiState = SettingFolderUiState(galleryModel.folders)
        }
    }
}