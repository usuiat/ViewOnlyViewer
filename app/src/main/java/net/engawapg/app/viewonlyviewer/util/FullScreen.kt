package net.engawapg.app.viewonlyviewer.util

import android.content.Context
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun enableFullScreen(context: Context) {
    val window = context.findActivity().window
    val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
}

fun disableFullScreen(context: Context) {
    val window = context.findActivity().window
    val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
}
