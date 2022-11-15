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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.engawapg.app.viewonlyviewer.LocalNavController
import net.engawapg.app.viewonlyviewer.R
import net.engawapg.app.viewonlyviewer.data.GalleryItem
import net.engawapg.app.viewonlyviewer.util.*

private val ViewerScreenBarColor = Color(0x70000000)

@Composable
fun ViewerScreen(
    viewModel: ViewerViewModel = viewModel(),
    index: Int,
    isDark: Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()
    val videoPlayerState = remember { VideoPlayerState(isPlayable = false) }
    ViewerContent(
        uiState = uiState,
        index = index,
        videoPlayerState = videoPlayerState,
        isDark = isDark,
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ViewerContent(
    uiState: ViewerUiState,
    index: Int,
    videoPlayerState: VideoPlayerState,
    isDark: Boolean,
) {
    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController) {
        /* Same color as shown by system when we swipe */
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = false,
        )

        onDispose {
            // Recover icon color depends on dark mode.
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = !isDark,
            )
        }
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
                AsyncImage(
                    model = item.uri,
                    contentDescription = "Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    } else {
        Box(
            // Prevent zoomed image from going beyond the page bounds.
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            ImageViewer(item)
        }
    }
}

@Composable
fun ImageViewer(item: GalleryItem) {
    val zoomState = rememberContentZoomState(maxScale = 8f)
    val scope = rememberCoroutineScope()

    AsyncImage(
        model = item.uri,
        contentDescription = "Image",
        contentScale = ContentScale.Fit,
        onSuccess = { state ->
            zoomState.setContentSize(state.painter.intrinsicSize)
        },
        modifier = Modifier
            .onSizeChanged { size ->
                zoomState.setElementSize(size.toSize())
            }
            .pointerInput(Unit) {
                detectTransformGesturesWithoutConsuming(
                    onGestureStart = {
                        zoomState.startGesture()
                    },
                    onGesture = { event, centroid, pan, zoom, _ ->
                        if (zoomState.canConsumeGesture(pan, zoom)) {
                            event.consumeChanges()
                            scope.launch {
                                zoomState.applyGesture(
                                    centroid,
                                    pan,
                                    zoom,
                                    event.changes[0].uptimeMillis
                                )
                            }
                        }
                    },
                    onGestureEnd = {
                        scope.launch {
                            zoomState.fling()
                        }
                    }
                )
            }
            .graphicsLayer(
                scaleX = zoomState.scale,
                scaleY = zoomState.scale,
                translationX = zoomState.offset.x,
                translationY = zoomState.offset.y,
            )
            .fillMaxSize(),
    )
}

@Composable
fun VideoPlayer(item: GalleryItem, showControllers: Boolean, onVideoRendering: (Boolean)->Unit) {
    val mediaPlayerState = rememberMediaPlayerState()
    Box(modifier = Modifier.fillMaxSize()) {
        val scope = rememberCoroutineScope()
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                VideoView(context).apply {
                    setOnPreparedListener { mp ->
                        mediaPlayerState.mediaPlayer = mp
                        mediaPlayerState.start()
                    }
                    setOnInfoListener { _, what, _ ->
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            onVideoRendering(true)
                        }
                        false
                    }
                    scope.launch {
                        delay(100) // Avoid the problem of OnPrepare not being called.
                        setVideoURI(item.uri)
                    }
                }
            },
        )
        if (showControllers) {
            VideoController(
                modifier = Modifier.align(Alignment.BottomCenter),
                isPlaying = mediaPlayerState.isPlaying,
                duration = mediaPlayerState.duration,
                position = mediaPlayerState.currentPosition,
                onClickPlayPause = mediaPlayerState::toggle,
                onPositionChange = { pos ->
                    mediaPlayerState.pause()
                    mediaPlayerState.seekTo(pos)
                },
                onPositionChangeFinished = mediaPlayerState::start,
            )
        }
    }
}

@Composable
private fun VideoController(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    duration: Int,
    position: Int,
    onClickPlayPause: () -> Unit,
    onPositionChange: (Int) -> Unit,
    onPositionChangeFinished: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow
        IconButton(
            onClick = onClickPlayPause,
            modifier = Modifier
                .align(Alignment.Start)
                .size(64.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(id = R.string.desc_playpause),
                modifier = Modifier
                    .padding(12.dp)
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
                text = "${position / 60000}:${"%02d".format((position / 1000) % 60)}",
                modifier = Modifier.padding(20.dp)
            )
            Slider(
                value = position.toFloat(),
                onValueChange = { onPositionChange(it.toInt()) },
                modifier = Modifier
                    .weight(1f),    // Fill the width left after other components are placed.
                valueRange = 0f..duration.toFloat(),
                onValueChangeFinished = onPositionChangeFinished,
            )
            Text( // Duration
                text = "${duration / 60000}:${"%02d".format((duration / 1000) % 60)}",
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}

@Composable
private fun rememberMediaPlayerState(): MediaPlayerState {
    val mediaPlayerState = remember { MediaPlayerState() }
    LaunchedEffect(Unit) {
        mediaPlayerState.monitor()
    }
    return mediaPlayerState
}

private class MediaPlayerState {
    private var _mediaPlayer: MediaPlayer? by mutableStateOf(null)
    var mediaPlayer: MediaPlayer?
        get() = _mediaPlayer
        set(value) {
            _mediaPlayer = value
            duration = _mediaPlayer?.duration ?: 0
            currentPosition = 0
        }

    var isPlaying by mutableStateOf(false)
        private set

    var duration by mutableStateOf(0)
        private set

    var currentPosition by mutableStateOf(0)
        private set

    suspend fun monitor() {
        while (true) {
            delay(30)
            try {
                isPlaying = mediaPlayer?.isPlaying ?: false
                currentPosition = mediaPlayer?.currentPosition ?: 0
            } catch (e: IllegalStateException) {
                isPlaying = false
                currentPosition = 0
            }
        }
    }

    fun start() {
        mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun toggle() {
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
            } else {
                start()
            }
        }
    }

    fun seekTo(msec: Int) {
        mediaPlayer?.seekTo(msec)
    }
}

@Stable
data class VideoPlayerState(var isPlayable: Boolean)
