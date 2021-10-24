package net.engawapg.app.viewonlyviewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@ExperimentalCoilApi
@ExperimentalPagerApi
@Composable
fun ViewerScreen(viewModel: MainViewModel, index: Int) {
    val items: List<GalleryItem> by viewModel.galleryItems.observeAsState(listOf())
    Surface(color = Color.Black) {
        val pagerState = rememberPagerState(initialPage = index)

        HorizontalPager(count = items.size, state = pagerState, itemSpacing = 10.dp) { pageIndex ->
            Box {
                var videoPlaying by remember { mutableStateOf(false) }
                if (!videoPlaying) {
                    Viewer(items[pageIndex])
                }
            }
        }
    }
}

@ExperimentalCoilApi
@Composable
fun Viewer(item: GalleryItem) {
    Image(
        painter = rememberImagePainter(data = item.uri),
        contentDescription = "Image",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}