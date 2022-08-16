package net.engawapg.app.viewonlyviewer.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import net.engawapg.app.viewonlyviewer.data.GalleryItem
import net.engawapg.app.viewonlyviewer.data.GalleryModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

data class ViewerUiState(
    val galleryItems: List<GalleryItem>,
)

class ViewerViewModel: ViewModel(), KoinComponent {
    private val model: GalleryModel = get()

    val uiState: StateFlow<ViewerUiState> = model.galleryItemsFlow.map { galleryItems ->
        ViewerUiState(
            galleryItems = galleryItems,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ViewerUiState(listOf()),
    )
}