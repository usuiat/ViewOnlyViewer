package net.engawapg.app.viewonlyviewer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.PermissionChecker
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.VideoFrameDecoder
import coil.fetch.VideoFrameFileFetcher
import coil.fetch.VideoFrameUriFetcher
import com.google.accompanist.pager.ExperimentalPagerApi
import net.engawapg.app.viewonlyviewer.ui.theme.ViewOnlyViewerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    companion object {
        const val REQ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val viewModel: MainViewModel by viewModel()

    @ExperimentalPagerApi
    @ExperimentalCoilApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission()

        val imageLoader = ImageLoader.Builder(this)
            .componentRegistry {
                add(VideoFrameFileFetcher(this@MainActivity))
                add(VideoFrameUriFetcher(this@MainActivity))
                add(VideoFrameDecoder(this@MainActivity))
            }
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)

        setContent {
            ViewOnlyViewerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppScreen(viewModel = viewModel) { rationaleResult ->
                        if (rationaleResult) {
                            /* Permissionが必要な理由を説明し、了承されたので再度Permission要求 */
                            viewModel.permissionState.value = PermissionState.UNKNOWN
                            permissionRequest.launch(REQ_PERMISSION)
                        } else {
                            /* Permissionが必要な理由を説明したが、拒否された */
                            viewModel.permissionState.value = PermissionState.DENIED
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadGallery()
    }

    /* Permissionの結果を受け取る */
    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ granted ->
        viewModel.permissionState.value = when {
            /* 承認 */
            granted -> PermissionState.GRANTED
            /* 拒否(1回目) Permissionが必要な理由を説明 */
            shouldShowRequestPermissionRationale(REQ_PERMISSION) -> PermissionState.EXPLAINING
            /* 拒否(2回目以降) */
            else -> PermissionState.DENIED
        }
    }

    /* 必要なPermissionが与えられているかどうかを確認 */
    private fun checkPermission() {
        val result = PermissionChecker.checkSelfPermission(this, REQ_PERMISSION)
        if (result == PermissionChecker.PERMISSION_GRANTED) {
            /* 承認済み */
            viewModel.permissionState.value = PermissionState.GRANTED
        } else {
            /* 要求する */
            permissionRequest.launch(REQ_PERMISSION)
        }
    }
}

@ExperimentalPagerApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@Composable
fun AppScreen(viewModel: MainViewModel, onRationaleDialogResult: (Boolean) -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "gallery") {
        composable("gallery") {
            GalleryScreen(
                viewModel = viewModel,
                onItemSelected = { index -> navController.navigate("viewer/$index") },
                onRationaleDialogResult = onRationaleDialogResult
            )
        }
        composable(
            route = "viewer/{index}",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            ViewerScreen(viewModel = viewModel, index = index)
        }
    }
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@Composable
fun GalleryScreen(
    viewModel: MainViewModel,
    onItemSelected: (Int)->Unit = {},
    onRationaleDialogResult: (Boolean)->Unit
) {
    /* Permissionの取得状況によって表示内容を変える */
    val permissionState: PermissionState by viewModel.permissionState.observeAsState(PermissionState.UNKNOWN)
    val items: List<GalleryItem> by viewModel.galleryItems.observeAsState(listOf())
    when (permissionState) {
        /* 承認: ギャラリー表示 */
        PermissionState.GRANTED -> Gallery(items, onItemSelected)
        /* 拒否: 設定で許可するように説明 */
        PermissionState.DENIED -> AskPermissionInSettingApp()
        /* 拒否（1回目）: Permissionが必要な理由をダイアログで説明 */
        PermissionState.EXPLAINING -> PermissionRationaleDialog(onRationaleDialogResult)
        else -> {}
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
fun PermissionRationaleDialog(onDialogResult: (Boolean)->Unit) {
    AlertDialog(
        text = { Text(stringResource(R.string.rationale_permission)) },
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = {
                onDialogResult(true)
            }) {
                Text(stringResource(R.string.ok))
            }},
        dismissButton = {
            TextButton(onClick = {
                onDialogResult(false)
            }) {
                Text(stringResource(R.string.cancel))
            }},
    )
}