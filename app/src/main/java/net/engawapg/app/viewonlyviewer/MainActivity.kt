package net.engawapg.app.viewonlyviewer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.core.content.PermissionChecker
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import net.engawapg.app.viewonlyviewer.ui.theme.ViewOnlyViewerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

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
                        AppScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PermissionChecker.PERMISSION_GRANTED) {
            viewModel.loadGallery()
        }
    }
}

@Composable
fun AppScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "gallery") {
        composable("gallery") {
            GalleryScreen(
                viewModel = viewModel,
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
            ViewerScreen(viewModel = viewModel, index = index)
        }
        composable("settings") {
            SettingsScreen(
                onEvent = { event ->
                    when(event) {
                        SettingsScreenEvent.SelectBack -> navController.navigateUp()
                    }
                }
            )
        }
    }
}
