package net.engawapg.app.viewonlyviewer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class MainViewModel(private val model: GalleryModel): ViewModel(), KoinComponent {
    private val settingsRepo: SettingsRepository = get()
    private var hideFolderIds: Set<String>? = null
    val galleryItems = MutableLiveData<List<GalleryItem>>().apply { value = listOf() }

    init {
        viewModelScope.launch {
            settingsRepo.appSettingsFlow.collect { appSettings ->
                viewModelScope.launch(Dispatchers.IO) {
                    model.load(appSettings.hideFolderIds)
                    galleryItems.postValue(model.items)
                }
                hideFolderIds = appSettings.hideFolderIds
            }
        }
    }

    fun loadGallery() {
        viewModelScope.launch(Dispatchers.IO) {
            val ids = hideFolderIds
            if (ids != null) {
                model.load(ids)
                galleryItems.postValue(model.items)
            }
        }
    }
}