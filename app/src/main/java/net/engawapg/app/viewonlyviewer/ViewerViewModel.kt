package net.engawapg.app.viewonlyviewer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

data class ViewerUiState(
    val galleryItems: List<GalleryItem>,
)

class ViewerViewModel: ViewModel(), KoinComponent {
    private val model: GalleryModel = get()

    val uiState: StateFlow<ViewerUiState> = MutableStateFlow(
        ViewerUiState(
            galleryItems = model.galleryItemsFlow.value,
        )
    )
}