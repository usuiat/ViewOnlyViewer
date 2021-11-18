package net.engawapg.app.viewonlyviewer

import android.media.MediaPlayer
import android.widget.VideoView
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
import androidx.compose.ui.viewinterop.AndroidView
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
    if (items.isNotEmpty()) {
        Surface(color = Color.Black) {
            val pagerState = rememberPagerState(initialPage = index)

            HorizontalPager(
                count = items.size,
                state = pagerState,
                itemSpacing = 10.dp
            ) { pageIndex ->
                Viewer(
                    item = items[pageIndex],
                    isCurrentPage = (pagerState.currentPage == pageIndex)
                )
            }
        }
    }
}

@ExperimentalCoilApi
@Composable
fun Viewer(item: GalleryItem, isCurrentPage: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (item.isVideo) {
            var videoPlaying by remember { mutableStateOf(false) }
            if (isCurrentPage) {
                VideoPlayer(item = item) {
                    videoPlaying = true
                }
            }
            if (!isCurrentPage || !videoPlaying) {
                ImageViewer(item)
            }
        } else {
            ImageViewer(item)
        }
    }
}

@ExperimentalCoilApi
@Composable
fun ImageViewer(item: GalleryItem) {
    Image(
        painter = rememberImagePainter(data = item.uri),
        contentDescription = "Image",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun VideoPlayer(item: GalleryItem, onStart: ()->Unit) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            VideoView(context).apply {
                setVideoURI(item.uri)
                setOnPreparedListener {
                    start()
                }
                setOnInfoListener { _, what, _ ->
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        onStart()
                    }
                    false
                }
            }
        }
    )
}
