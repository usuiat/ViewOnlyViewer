package net.engawapg.app.viewonlyviewer

import android.media.MediaPlayer
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ViewerScreen(viewModel: MainViewModel, index: Int) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(Color.Black)
    }

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

enum class PlayState {
    Preparing
    {
        override fun toggle() = Preparing
    },
    Playing
    {
        override fun toggle() = Pausing
    },
    Pausing
    {
        override fun toggle() = Playing
    };

    abstract fun toggle(): PlayState
}

@Composable
fun Viewer(item: GalleryItem, isCurrentPage: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (item.isVideo) {
            var playState by remember { mutableStateOf(PlayState.Preparing) }
            if (isCurrentPage) {
                VideoPlayer(
                    item = item,
                    playState = playState,
                    onPreparing = { playState = PlayState.Preparing },
                    onStart = { playState = PlayState.Playing },
                    onComplete = { playState = PlayState.Pausing }
                )
            }
            if (!isCurrentPage || (playState == PlayState.Preparing)) {
                ImageViewer(item)
            }
            VideoController(
                playState = playState,
                onPlayControl = { playState = playState.toggle() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else {
            ImageViewer(item)
        }
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
fun VideoPlayer(item: GalleryItem, playState: PlayState, onPreparing: ()->Unit, onStart: ()->Unit, onComplete: ()->Unit) {
    var prepared by remember { mutableStateOf( false) }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            VideoView(context).apply {
                setVideoURI(item.uri)
                setOnPreparedListener {
                    prepared = true
                }
                setOnInfoListener { _, what, _ ->
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        onStart()
                    }
                    false
                }
                setOnCompletionListener {
                    onComplete()
                }
                onPreparing()
//                Log.d("Viewer", "VideoView: factory playState=$playState, isPlaying=$isPlaying")
            }
        },
        update = { videoView ->
//            Log.d("Viewer", "VideoView: update playState=$playState, isPlaying=${videoView.isPlaying}")
            if (prepared && (playState != PlayState.Pausing)) {
                videoView.start()
            } else if (videoView.isPlaying && (playState == PlayState.Pausing)) {
                videoView.pause()
            }
        }
    )
}

@Composable
fun VideoController(playState: PlayState, onPlayControl: ()->Unit, modifier: Modifier = Modifier) {
    val iconResId = if (playState == PlayState.Playing) R.drawable.pause else R.drawable.play

    Box(modifier = modifier) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = stringResource(id = R.string.desc_playpause),
            modifier = Modifier
                .padding(20.dp)
                .clickable { onPlayControl() },
            tint = Color.Unspecified
        )
    }
}
