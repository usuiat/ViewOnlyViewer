package net.engawapg.app.viewonlyviewer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ViewerViewModel: ViewModel(), KoinComponent {
    private val model: GalleryModel = get()
    val galleryItems = MutableLiveData<List<GalleryItem>>().apply { value = model.items }
}