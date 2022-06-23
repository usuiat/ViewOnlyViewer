package net.engawapg.app.viewonlyviewer

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GalleryScreenEvent {
    SelectSettings
}

private const val COLUMN_NUM = 4

/* Time out (msec) to cancel invoking button actions for each tap. */
private const val TIMEOUT_TO_CANCEL_ACTION_PER_TAP = 300L
/* Time out (msec) to cancel invoking go back actions for each operation. */
private const val TIMEOUT_TO_CANCEL_ACTION_PER_BACK = 500L

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: MainViewModel,
    onItemSelected: (Int)->Unit = {},
    onEvent: (GalleryScreenEvent)->Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarScrollState())
    val statusBarColor = TopAppBarDefaults.centerAlignedTopAppBarColors()
        .containerColor(scrollFraction = scrollBehavior.scrollFraction).value
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(statusBarColor)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val tapCount = SettingTapCountToOpenSettings.getState(LocalContext.current)
    val backCount = SettingMultiGoBack.getState(LocalContext.current)
    val failedMessage = stringResource(R.string.message_when_opening_settings_failed, tapCount.value)
    val cancelGoBackMsg = stringResource(R.string.message_when_go_back_canceled, backCount.value)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                     MultiTapIconButton(
                         tapCount = tapCount.value,
                         onTapComplete = { success ->
                             if (success) {
                                 onEvent(GalleryScreenEvent.SelectSettings)
                             } else {
                                 scope.launch {
                                     snackbarHostState.showSnackbar(failedMessage)
                                 }
                             }
                         }
                     ) {
                         Icon(
                             imageVector = Icons.Default.Settings,
                             contentDescription = stringResource(id = R.string.desc_settings),
                         )
                     }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            /* Contents depends on the permission state */
            var permissionRequested by rememberSaveable { mutableStateOf(false) }
            val ps = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE) {
                permissionRequested = true
            }
            when {
                ps.status.isGranted -> {
                    val items: List<GalleryItem> by viewModel.galleryItems.observeAsState(listOf())
                    Gallery(items, onItemSelected)
                }
                ps.status.shouldShowRationale -> RequestPermission(shouldShowRational = true) {
                    ps.launchPermissionRequest()
                }
                permissionRequested -> {
                    AskPermissionInSettingApp()
                }
                else -> RequestPermission(shouldShowRational = false) {
                    ps.launchPermissionRequest()
                }
            }
        }
    }

    // Multiple go back operation to exit app.
    MultiGoBackHandler(backCount.value) {
        scope.launch {
            snackbarHostState.showSnackbar(cancelGoBackMsg)
        }
    }
}

@Composable
fun MultiTapIconButton(
    tapCount: Int,
    onTapComplete: (Boolean)->Unit,
    content: @Composable ()->Unit
) {
    val scope = rememberCoroutineScope()
    var job: Job? = remember{ null }
    var count = remember { 0 }
    IconButton(
        onClick = {
            if (count == 0) {
                job = scope.launch {
                    delay(TIMEOUT_TO_CANCEL_ACTION_PER_TAP * tapCount)
                    onTapComplete(false)
                    count = 0
                }
            }
            count++
            if (count >= tapCount) {
                job?.cancel()
                onTapComplete(true)
                count = 0
            }
        },
        content = content
    )
}

@Composable
fun Gallery(items: List<GalleryItem>, onItemSelected: (Int)->Unit = {}) {
    if (items.isNotEmpty()) {
        LazyVerticalGrid(columns = GridCells.Fixed(COLUMN_NUM)) {
            itemsIndexed(items) { index, item ->
                GalleryItem(item = item, onSelected = { onItemSelected(index) })
            }
        }
    }
}

@Composable
fun GalleryItem(item: GalleryItem, onSelected: ()->Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clickable { onSelected() }
    ) {

        /* Videoは白いアイコンをオーバーレイするので、見えやすいようにサムネイル画像を少し暗くする */
        val filter = if (item.isVideo) {
            ColorFilter.tint(Color(0xffdddddd), BlendMode.Multiply)
        } else {
            null
        }

        AsyncImage(
            model = item.uri,
            contentDescription = "Image",
            contentScale = ContentScale.Crop,
            colorFilter = filter,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )

        if (item.isVideo) {
            Image(
                painter = painterResource(id = R.drawable.videoindicator),
                contentDescription = "videoIndicator",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun AskPermissionInSettingApp() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.request_to_grant_permission),
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
fun RequestPermission(shouldShowRational: Boolean, onClick: ()->Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val text = if (shouldShowRational) {
            stringResource(id = R.string.rationale_permission)
        } else {
            stringResource(id = R.string.request_permission)
        }
        Text(
            text = text,
            modifier = Modifier.padding(20.dp)
        )
        Button(onClick = onClick) {
            Text(stringResource(id = R.string.button_continue))
        }
    }
}

/**
 * GoBackHandler that invoke go back action when multiple go back operations are performed.
 */
@Composable
fun MultiGoBackHandler(requiredCount: Int, onCancel: ()->Unit) {
    if (requiredCount > 1) {
        val scope = rememberCoroutineScope()
        var job by remember<MutableState<Job?>> { mutableStateOf(null) }
        var count = remember { 0 }
        var enableHandler by remember { mutableStateOf(true) }
        val lifecycleOwner = LocalLifecycleOwner.current

        BackHandler(enableHandler) {
            count++
            if (count == 1) {
                // At first onBack, start timer.
                job = scope.launch {
                    delay(TIMEOUT_TO_CANCEL_ACTION_PER_BACK * requiredCount)
                    // Reset counter and enable BackHandler
                    count = 0
                    enableHandler = true
                    onCancel()
                }
            }
            if (count == (requiredCount - 1)) {
                // Disable this BackHandler to exit app in the next go back operation.
                enableHandler = false
            }
        }

        // Detect the final go back operation and cancel the timer.
        DisposableEffect(true) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE) {
                    job?.cancel()
                    count = 0
                    enableHandler = true
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
}
