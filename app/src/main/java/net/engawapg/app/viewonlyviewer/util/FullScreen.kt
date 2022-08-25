package net.engawapg.app.viewonlyviewer.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}
