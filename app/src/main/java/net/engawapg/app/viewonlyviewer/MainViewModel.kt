package net.engawapg.app.viewonlyviewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class PermissionState {
    UNKNOWN,
    GRANTED,    /* 承認された */
    DENIED,     /* 拒否された */
    EXPLAINING  /* 追加説明が必要 */
}

class MainViewModel(private val model: GalleryModel, app: Application): AndroidViewModel(app) {
    val permissionState = MutableLiveData<PermissionState>().apply { value = PermissionState.UNKNOWN }
    val galleryItems = MutableLiveData<List<GalleryItem>>().apply { value = listOf() }

    fun loadGallery() {
        viewModelScope.launch(Dispatchers.IO) {
            if (permissionState.value == PermissionState.GRANTED) {
                model.load(getApplication())
                galleryItems.postValue(model.items)
            }
        }
    }
}