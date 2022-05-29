package net.engawapg.app.viewonlyviewer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.core.content.PermissionChecker
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.engawapg.app.viewonlyviewer.ui.theme.ViewOnlyViewerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


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
            SettingsScreen()
        }
    }
}
