package net.engawapg.app.viewonlyviewer

import android.media.MediaPlayer
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ViewerScreen(viewModel: MainViewModel, index: Int) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(Color.Black)
    }

    val items: List<GalleryItem> by viewModel.galleryItems.observeAsState(listOf())
    if (items.isNotEmpty()) {
        Surface(
            color = Color.Black,
            contentColor = Color.White
        ) {
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

@Composable
fun Viewer(item: GalleryItem, isCurrentPage: Boolean) {
    if (item.isVideo) {
        Box(modifier = Modifier.fillMaxSize()) {
            var isVideoRendering by remember { mutableStateOf(false) }
            if (isCurrentPage) {
                VideoPlayer(
                    item = item,
                    onVideoRendering = { isVideoRendering = it },
                )
            }
            if (!isCurrentPage || !isVideoRendering) {
                ImageViewer(item)
            }
        }
    } else {
        ImageViewer(item)
    }
}

@OptIn(ExperimentalCoilApi::class)
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
fun VideoPlayer(item: GalleryItem, onVideoRendering: (Boolean)->Unit) {
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(item.uri)
                    setOnPreparedListener { mp ->
                        mp?.start()
                    }
                    setOnInfoListener { mp, what, _ ->
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            onVideoRendering(true)
                            mediaPlayer = mp ?: null
                        }
                        false
                    }
                }
            },
        )
        VideoController(
            mediaPlayer = mediaPlayer,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun VideoController(
    mediaPlayer: MediaPlayer?,
    modifier: Modifier = Modifier
) {
    val isVideoPlaying = mediaPlayer?.isPlaying ?: false

    val duration = (mediaPlayer?.duration ?: 0) / 1000
    val durMin = (duration / 60).toString()
    val durSec = (duration % 60).toString().padStart(2, '0')
    val curPos = (mediaPlayer?.currentPosition ?: 0) / 1000
    val curMin = (curPos / 60).toString()
    val curSec = (curPos % 60).toString().padStart(2, '0')

    // Recompose every 100 msec while playing.
    var updateKey by remember { mutableStateOf(0) }
    LaunchedEffect(updateKey) {
        delay(100)
        updateKey++
    }

    Column(modifier = modifier) {
        Icon(
            painter = painterResource(if (isVideoPlaying) R.drawable.pause else R.drawable.play),
            contentDescription = stringResource(id = R.string.desc_playpause),
            modifier = Modifier
                .padding(20.dp)
                .clickable {
                    if (isVideoPlaying) mediaPlayer?.pause() else mediaPlayer?.start()
                },
            tint = Color.Unspecified
        )
        Row {
            Text ( // Current Position
                text = "$curMin:$curSec",
                modifier = Modifier.padding(20.dp)
            )
            Text( // Duration
                text = "$durMin:$durSec",
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}
