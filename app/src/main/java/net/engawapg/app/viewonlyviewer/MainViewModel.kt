package net.engawapg.app.viewonlyviewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val model: GalleryModel, app: Application): AndroidViewModel(app) {
    val galleryItems = MutableLiveData<List<GalleryItem>>().apply { value = listOf() }

    fun loadGallery() {
        viewModelScope.launch(Dispatchers.IO) {
            model.load(getApplication())
            galleryItems.postValue(model.items)
        }
    }
}