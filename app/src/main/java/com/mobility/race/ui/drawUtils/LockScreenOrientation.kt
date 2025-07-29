package com.mobility.race.ui.drawUtils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

enum class Orientation(val value: Int) {
    LANDSCAPE(0),
    PORTRAIT(1)
}

@Composable
fun LockScreenOrientation(orientation: Orientation) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity ?: return@DisposableEffect onDispose {  }
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation.value
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }
}
