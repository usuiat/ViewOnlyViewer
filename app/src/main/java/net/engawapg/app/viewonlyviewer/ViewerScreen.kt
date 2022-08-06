package net.engawapg.app.viewonlyviewer

import android.media.MediaPlayer
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay

@Composable
fun ViewerScreen(viewModel: ViewerViewModel = viewModel(), index: Int) {
    val uiState by viewModel.uiState.collectAsState()
    ViewerContent(
        uiState = uiState,
        index = index,
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ViewerContent(uiState: ViewerUiState, index: Int) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(Color.Black)
    }

    val items = uiState.galleryItems
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

@Composable
fun ImageViewer(item: GalleryItem) {
    AsyncImage(
        model = item.uri,
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
                        mediaPlayer = mp ?: null
                    }
                    setOnInfoListener { _, what, _ ->
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            onVideoRendering(true)
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
    var isVideoPlaying = false
    var duration = 0
    var curPos = 0
    try {
        isVideoPlaying = mediaPlayer?.isPlaying ?: false
        duration = (mediaPlayer?.duration ?: 0)
        curPos = (mediaPlayer?.currentPosition ?: 0)
    } catch (e: IllegalStateException) {
        // Nothing to do until the MediaPlayer will be prepared.
    }

    val durMin = (duration / 60000).toString()
    val durSec = ((duration / 1000) % 60000).toString().padStart(2, '0')
    val curMin = (curPos / 60000).toString()
    val curSec = ((curPos / 1000) % 60).toString().padStart(2, '0')

    // Recompose every 100 msec while playing.
    var updateKey by remember { mutableStateOf(0) }
    LaunchedEffect(updateKey) {
        delay(30)
        updateKey++
    }

    Column(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x80000000)))),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text ( // Current Position
                text = "$curMin:$curSec",
                modifier = Modifier.padding(20.dp)
            )
            Slider(
                value = curPos.toFloat(),
                onValueChange = {
                    mediaPlayer?.pause()
                    mediaPlayer?.seekTo(it.toInt())
                },
                modifier = Modifier
                    .weight(1f),    // Fill the width left after other components are placed.
                valueRange = 0f..duration.toFloat(),
                onValueChangeFinished = {
                    mediaPlayer?.start()
                },
            )
            Text( // Duration
                text = "$durMin:$durSec",
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}
