package net.engawapg.app.viewonlyviewer

import android.Manifest
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val COLUMN_NUM = 4

/* Tap count to invoke button actions. e.g. moving to setting screen. */
private const val TAP_COUNT_TO_BUTTON_ACTION = 3
/* Time out (msec) to cancel invoking button actions. */
private const val TIMEOUT_TO_CANCEL_ACTION = 700L

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: MainViewModel,
    onItemSelected: (Int)->Unit = {}
) {
    val scrollBehavior = remember {TopAppBarDefaults.enterAlwaysScrollBehavior()}
    val statusBarColor = TopAppBarDefaults.centerAlignedTopAppBarColors()
        .containerColor(scrollFraction = scrollBehavior.scrollFraction).value
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(statusBarColor)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                     TripleTapIconButton(
                         onTapComplete = { success ->
                             Log.d("GalleryComposables", "onTapComplete $success")
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
    ) {
        /* Permissionの取得状況によって表示内容を変える */
        val ps = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
        when {
            ps.hasPermission -> {
                val items: List<GalleryItem> by viewModel.galleryItems.observeAsState(listOf())
                Gallery(items, onItemSelected)
            }

            ps.shouldShowRationale -> PermissionRationaleDialog {
                ps.launchPermissionRequest()
            }

            ps.permissionRequested -> AskPermissionInSettingApp()

            else -> SideEffect {
                ps.launchPermissionRequest()
            }
        }
    }
}

@Composable
fun TripleTapIconButton(onTapComplete: (Boolean)->Unit, content: @Composable ()->Unit) {
    val scope = rememberCoroutineScope()
    var job: Job? = remember{ null }
    var tapCount = remember { 0 }
    IconButton(
        onClick = {
            if (tapCount == 0) {
                job = scope.launch {
                    delay(TIMEOUT_TO_CANCEL_ACTION)
                    onTapComplete(false)
                    tapCount = 0
                }
            }
            tapCount++
            if (tapCount >= TAP_COUNT_TO_BUTTON_ACTION) {
                job?.cancel()
                onTapComplete(true)
                tapCount = 0
            }
        },
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Gallery(items: List<GalleryItem>, onItemSelected: (Int)->Unit = {}) {
    if (items.isNotEmpty()) {
        LazyVerticalGrid(cells = GridCells.Fixed(COLUMN_NUM)) {
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
fun PermissionRationaleDialog(onDialogResult: ()->Unit) {
    AlertDialog(
        text = { Text(stringResource(R.string.rationale_permission)) },
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onDialogResult) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}
