package com.rnx.laranjada.feature.player

import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
internal fun PlayerImmersiveMode(
    preferLandscape: Boolean,
    orientationLocked: Boolean
) {
    val view = LocalView.current
    val activity = view.context.findActivity()

    DisposableEffect(view) {
        val window = activity?.window
        val previousOrientation = activity?.requestedOrientation

        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val controller = WindowInsetsControllerCompat(
                window,
                window.decorView
            )

            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            controller.hide(WindowInsetsCompat.Type.systemBars())

            onDispose {
                controller.show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(window, true)

                if (previousOrientation != null) {
                    activity.requestedOrientation = previousOrientation
                }
            }
        } else {
            onDispose {}
        }
    }

    LaunchedEffect(activity, preferLandscape, orientationLocked) {
        if (activity == null) return@LaunchedEffect

        activity.requestedOrientation = when {
            orientationLocked && preferLandscape -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            orientationLocked && !preferLandscape -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            preferLandscape -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }
}