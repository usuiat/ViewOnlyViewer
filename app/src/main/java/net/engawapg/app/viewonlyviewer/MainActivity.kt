package net.engawapg.app.viewonlyviewer

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import net.engawapg.app.viewonlyviewer.data.ColorThemeSetting
import net.engawapg.app.viewonlyviewer.data.DarkThemeSetting
import net.engawapg.app.viewonlyviewer.ui.gallery.GalleryScreen
import net.engawapg.app.viewonlyviewer.ui.gallery.GalleryScreenEvent
import net.engawapg.app.viewonlyviewer.ui.gallery.ViewerScreen
import net.engawapg.app.viewonlyviewer.ui.settings.SettingFolderScreen
import net.engawapg.app.viewonlyviewer.ui.settings.SettingsScreen
import net.engawapg.app.viewonlyviewer.ui.theme.ViewOnlyViewerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Throughout the app, stretch the content area to the edge of the window to achieve
           smooth transition between fullscreen and non-fullscreen content. */
        WindowCompat.setDecorFitsSystemWindows(window, false)

        /* Disable the system gestures while full screen mode. */
        WindowInsetsControllerCompat(window, window.decorView).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        /* Enable displaying around the display cutout */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        window.statusBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            val viewModel: MainViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            AppScreen(uiState)
        }
    }
}

@Composable
fun AppScreen(uiState: MainUiState) {
    when (uiState) {
        is MainUiState.Loading -> {
        }
        is MainUiState.Success -> {
            val isDark = when (uiState.darkTheme) {
                DarkThemeSetting.Off -> false
                DarkThemeSetting.On -> true
                DarkThemeSetting.UseSystemSettings -> isSystemInDarkTheme()
            }
            val isDynamicColor = uiState.colorTheme == ColorThemeSetting.Wallpaper
            ViewOnlyViewerTheme(isDark, isDynamicColor) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppContent()
                }
            }
        }
    }
}

val LocalNavController = staticCompositionLocalOf<NavController> { error("No NavController.") }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppContent() {
    /* Contents depends on the permission state */
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    val ps = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE) {
        permissionRequested = true
    }
    when {
        ps.status.isGranted -> {
            AppNavigation()
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(navController = navController, startDestination = "gallery") {
            composable("gallery") {
                GalleryScreen(
                    onItemSelected = { index -> navController.navigate("viewer/$index") },
                    onEvent = { event ->
                        when(event) {
                            GalleryScreenEvent.SelectSettings ->
                                navController.navigate("settings")
                        }
                    }
                )
            }
            composable(
                route = "viewer/{index}",
                arguments = listOf(navArgument("index") { type = NavType.IntType })
            ) { backStackEntry ->
                val index = backStackEntry.arguments?.getInt("index") ?: 0
                ViewerScreen(index = index)
            }
            composable("settings") { SettingsScreen() }
            composable("setting_folder") { SettingFolderScreen() }
        }
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

