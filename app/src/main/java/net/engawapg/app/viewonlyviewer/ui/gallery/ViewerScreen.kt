package net.engawapg.app.viewonlyviewer.ui.gallery

import android.media.MediaPlayer
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import net.engawapg.app.viewonlyviewer.LocalNavController
import net.engawapg.app.viewonlyviewer.R
import net.engawapg.app.viewonlyviewer.data.GalleryItem
import net.engawapg.app.viewonlyviewer.util.disableFullScreen
import net.engawapg.app.viewonlyviewer.util.enableFullScreen

private val ViewerScreenBarColor = Color(0x70000000)

@Composable
fun ViewerScreen(viewModel: ViewerViewModel = viewModel(), index: Int) {
    val uiState by viewModel.uiState.collectAsState()
    val videoPlayerState = remember { VideoPlayerState(isPlayable = false) }
    ViewerContent(
        uiState = uiState,
        index = index,
        videoPlayerState = videoPlayerState,
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ViewerContent(
    uiState: ViewerUiState,
    index: Int,
    videoPlayerState: VideoPlayerState,
) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        /* Same color as shown by system when we swipe */
        systemUiController.setSystemBarsColor(Color.Transparent)
    }

    val context = LocalContext.current
    var isFullScreen by remember { mutableStateOf(false) }
    var showControllers by remember { mutableStateOf(true) }
    val items = uiState.galleryItems
    if (items.isNotEmpty()) {
        Surface(
            color = Color.Black,
            contentColor = Color.White,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,  // Disable ripple effect
            ) {
                if (isFullScreen) {
                    disableFullScreen(context)
                } else {
                    enableFullScreen(context)
                }
                isFullScreen = !isFullScreen
                showControllers = !showControllers
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val pagerState = rememberPagerState(initialPage = index)

                // We want the page number when scrolling stops.
                // activePage is also updated when scrolling starts but it's same as when scrolling
                // stops, so that's no problem.
                val activePage = remember(pagerState.isScrollInProgress) {
                    videoPlayerState.isPlayable = true
                    pagerState.currentPage
                }
                // pagerState.currentPage changes when it scrolls half of the screen width.
                // At that time pagerState.currentPageOffset also changes.
                // But we need an offset of the activePage until it scroll to the end of the screen width.
                val activePageOffset = pagerState.currentPageOffset + pagerState.currentPage - activePage
                if ((activePageOffset < -0.9) || (0.9 < activePageOffset)) {
                    videoPlayerState.isPlayable = false
                }

                HorizontalPager(
                    count = items.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    itemSpacing = 10.dp
                ) { pageIndex ->
                    val showPlayer = (activePage == pageIndex) && videoPlayerState.isPlayable
                    Viewer(
                        item = items[pageIndex],
                        showPlayer = showPlayer,
                        showControllers = showControllers,
                    )
                }

                if (showControllers) {
                    val navController = LocalNavController.current
                    IconButton(
                        modifier = Modifier
                            .safeDrawingPadding()
                            .size(64.dp),
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.desc_back),
                            modifier = Modifier
                                .padding(12.dp)
                                .background(color = ViewerScreenBarColor, shape = CircleShape)
                                .padding(8.dp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(ViewerScreenBarColor)
                            .statusBarsPadding()
                            .fillMaxWidth()
                            .height(0.dp)
                    ) {
                        // This is a dummy Box to make the status bar visible.
                    }
                }
            }
        }
    }
}

@Composable
fun Viewer(
    item: GalleryItem,
    showPlayer: Boolean,
    showControllers: Boolean,
) {
    if (item.isVideo) {
        Box(modifier = Modifier.fillMaxSize()) {
            var isVideoRendering by remember { mutableStateOf(false) }
            if (showPlayer) {
                VideoPlayer(
                    item = item,
                    showControllers = showControllers,
                    onVideoRendering = { isVideoRendering = it },
                )
            }
            if (!showPlayer || !isVideoRendering) {
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
fun VideoPlayer(item: GalleryItem, showControllers: Boolean, onVideoRendering: (Boolean)->Unit) {
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
        if (showControllers) {
            VideoController(
                mediaPlayer = mediaPlayer,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
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
    val durSec = ((duration / 1000) % 60).toString().padStart(2, '0')
    val curMin = (curPos / 60000).toString()
    val curSec = ((curPos / 1000) % 60).toString().padStart(2, '0')

    // Recompose every 100 msec while playing.
    var updateKey by remember { mutableStateOf(0) }
    LaunchedEffect(updateKey) {
        delay(30)
        updateKey++
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val icon = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow
        IconButton(
            onClick = {
                if (isVideoPlaying) mediaPlayer?.pause() else mediaPlayer?.start()
            },
            modifier = Modifier
                .align(Alignment.Start)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(id = R.string.desc_playpause),
                modifier = Modifier
                    .background(color = ViewerScreenBarColor, shape = CircleShape)
                    .padding(8.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(ViewerScreenBarColor)
                .navigationBarsPadding()
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

@Stable
data class VideoPlayerState(var isPlayable: Boolean)
