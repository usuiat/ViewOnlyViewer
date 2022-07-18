package net.engawapg.app.viewonlyviewer

import android.Manifest
import android.os.Bundle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import net.engawapg.app.viewonlyviewer.ui.theme.ViewOnlyViewerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val darkTheme = SettingDarkTheme.get(this)
        val colorTheme = SettingColorTheme.get(this)

        setContent {
            /* Set status bar color to transparent until theme settings are loaded */
            val systemUiController = rememberSystemUiController()
            systemUiController.setStatusBarColor(Color.Transparent)

            /* Load theme settings */
            val darkThemeState = darkTheme.collectAsState(initial = DarkThemeValue.Undefined)
            val colorThemeState = colorTheme.collectAsState(initial = ColorThemeValue.Undefined)

            /* After theme settings are loaded, display screen with applied theme */
            if ((darkThemeState.value != DarkThemeValue.Undefined) &&
                (colorThemeState.value != ColorThemeValue.Undefined)) {
                val isDark = when (darkThemeState.value) {
                    DarkThemeValue.Off -> false
                    DarkThemeValue.On -> true
                    else -> isSystemInDarkTheme()
                }
                val isDynamicColor = colorThemeState.value == ColorThemeValue.wallpaper
                ViewOnlyViewerTheme(isDark, isDynamicColor) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        AppScreen()
                    }
                }
            }
        }
    }
}

val LocalNavController = staticCompositionLocalOf<NavController> { error("No NavController.") }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppScreen() {
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

