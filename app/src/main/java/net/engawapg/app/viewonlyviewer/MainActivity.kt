package net.engawapg.app.viewonlyviewer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.fetch.VideoFrameFileFetcher
import coil.fetch.VideoFrameUriFetcher
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import net.engawapg.app.viewonlyviewer.ui.theme.ViewOnlyViewerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        viewModel.loadGallery() // TODO
    }
}

@Composable
fun AppScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "gallery") {
        composable("gallery") {
            GalleryScreen(
                viewModel = viewModel,
                onItemSelected = { index -> navController.navigate("viewer/$index") }
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GalleryScreen(
    viewModel: MainViewModel,
    onItemSelected: (Int)->Unit = {}
) {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colorScheme.primaryContainer
    SideEffect {
        systemUiController.setStatusBarColor(statusBarColor)
    }

    /* Permissionの取得状況によって表示内容を変える */
    val ps = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    when {
        ps.hasPermission -> {
            val items: List<GalleryItem> by viewModel.galleryItems.observeAsState(listOf())
            LaunchedEffect(ps) {
                viewModel.loadGallery()
            }
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
            }},
    )
}