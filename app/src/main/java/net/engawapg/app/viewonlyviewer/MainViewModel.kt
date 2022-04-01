package net.engawapg.app.viewonlyviewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel(private val model: GalleryModel, app: Application): AndroidViewModel(app) {
    val galleryItems = MutableLiveData<List<GalleryItem>>().apply { value = listOf() }

    suspend fun loadGallery() {
        withContext(Dispatchers.IO) {
            model.load(getApplication())
        }
        galleryItems.value = model.items
    }
}