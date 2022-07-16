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
    private var ignoreFolderIds: Set<String>? = null
    val galleryItems = MutableLiveData<List<GalleryItem>>().apply { value = listOf() }

    init {
        viewModelScope.launch {
            settingsRepo.appSettingsFlow.collect { appSettings ->
                viewModelScope.launch(Dispatchers.IO) {
                    model.load(appSettings.ignoreFolderIds)
                    galleryItems.postValue(model.items)
                }
                ignoreFolderIds = appSettings.ignoreFolderIds
            }
        }
    }

    fun loadGallery() {
        viewModelScope.launch(Dispatchers.IO) {
            val ids = ignoreFolderIds
            if (ids != null) {
                model.load(ids)
                galleryItems.postValue(model.items)
            }
        }
    }
}