package com.rnx.laranjada.feature.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.TextureView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.graphics.createBitmap
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun enterPictureInPictureMode(
    context: Context
) {
    val activity = context.findActivity()

    if (activity == null) {
        Toast.makeText(
            context,
            "Não foi possível abrir o picture-in-picture.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()

            activity.enterPictureInPictureMode(params)
        } else {
            activity.enterPictureInPictureMode()
        }
    } catch (exception: Exception) {
        Log.w(
            PLAYER_LOG_TAG,
            "Não foi possível abrir o picture-in-picture.",
            exception
        )

        Toast.makeText(
            context,
            "Não foi possível abrir o picture-in-picture.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@OptIn(UnstableApi::class)
internal fun capturePlayerFrame(
    context: Context,
    playerView: PlayerView
) {
    val videoSurfaceView = playerView.videoSurfaceView

    when (videoSurfaceView) {
        is TextureView -> {
            val bitmap = videoSurfaceView.bitmap

            if (bitmap != null) {
                saveBitmapAndNotify(
                    context = context,
                    bitmap = bitmap
                )
            } else {
                captureViewFallback(
                    context = context,
                    playerView = playerView
                )
            }
        }

        is SurfaceView -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (
                    videoSurfaceView.width <= 0 ||
                    videoSurfaceView.height <= 0 ||
                    !videoSurfaceView.holder.surface.isValid
                ) {
                    Toast.makeText(
                        context,
                        "Não foi possível capturar esse frame.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                val bitmap = createBitmap(
                    videoSurfaceView.width,
                    videoSurfaceView.height
                )

                PixelCopy.request(
                    videoSurfaceView,
                    bitmap,
                    { result ->
                        if (result == PixelCopy.SUCCESS) {
                            saveBitmapAndNotify(
                                context = context,
                                bitmap = bitmap
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "Não foi possível capturar esse frame.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
            } else {
                captureViewFallback(
                    context = context,
                    playerView = playerView
                )
            }
        }

        else -> {
            captureViewFallback(
                context = context,
                playerView = playerView
            )
        }
    }
}

private fun captureViewFallback(
    context: Context,
    playerView: PlayerView
) {
    if (playerView.width <= 0 || playerView.height <= 0) {
        Toast.makeText(
            context,
            "Player ainda não está pronto para captura.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val bitmap = createBitmap(
        playerView.width,
        playerView.height
    )

    val canvas = Canvas(bitmap)
    playerView.draw(canvas)

    saveBitmapAndNotify(
        context = context,
        bitmap = bitmap
    )
}

private fun saveBitmapAndNotify(
    context: Context,
    bitmap: Bitmap
) {
    val uri = saveBitmapToPictures(
        context = context,
        bitmap = bitmap
    )

    if (uri != null) {
        Toast.makeText(
            context,
            "Captura salva na galeria.",
            Toast.LENGTH_SHORT
        ).show()
    } else {
        Toast.makeText(
            context,
            "Não foi possível salvar a captura.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun saveBitmapToPictures(
    context: Context,
    bitmap: Bitmap
): Uri? {
    val timestamp = SimpleDateFormat(
        "yyyyMMdd_HHmmss",
        Locale.US
    ).format(Date())

    val fileName = "laranjada_$timestamp.jpg"

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Laranjada"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver

            val uri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: return null

            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    95,
                    outputStream
                )
            } ?: return null

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)

            resolver.update(
                uri,
                values,
                null,
                null
            )

            uri
        } else {
            val directory = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "Laranjada"
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    95,
                    outputStream
                )
            }

            Uri.fromFile(file)
        }
    } catch (exception: Exception) {
        Log.w(
            PLAYER_LOG_TAG,
            "Não foi possível salvar a captura.",
            exception
        )

        null
    }
}

internal tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}