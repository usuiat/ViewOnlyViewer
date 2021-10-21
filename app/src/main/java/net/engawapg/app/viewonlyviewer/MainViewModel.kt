package net.engawapg.app.viewonlyviewer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class PermissionState {
    UNKNOWN,
    GRANTED,    /* 承認された */
    DENIED,     /* 拒否された */
    EXPLAINING  /* 追加説明が必要 */
}

class MainViewModel(model: GalleryModel): ViewModel() {
    val permissionState = MutableLiveData<PermissionState>().apply { value = PermissionState.UNKNOWN }
}