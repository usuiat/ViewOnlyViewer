package net.engawapg.app.viewonlyviewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

enum class PermissionState {
    UNKNOWN,
    GRANTED,    /* 承認された */
    DENIED,     /* 拒否された */
    EXPLAINING  /* 追加説明が必要 */
}

class MainViewModel(private val model: GalleryModel, app: Application): AndroidViewModel(app) {
    val permissionState = MutableLiveData<PermissionState>().apply { value = PermissionState.UNKNOWN }

    fun loadGallery() {
        if (permissionState.value == PermissionState.GRANTED) {
            model.load(getApplication())
        }
    }
}