package com.rnx.laranjada.feature.player

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import java.util.Locale
import kotlin.math.roundToInt

internal fun defaultSubtitleVisualSettings(): SubtitleVisualSettings {
    return SubtitleVisualSettings(
        bottomPaddingFraction = DEFAULT_SUBTITLE_BOTTOM_PADDING,
        textSizeFraction = DEFAULT_SUBTITLE_TEXT_SIZE,
        textColorArgb = AndroidColor.WHITE,
        backgroundColorArgb = SubtitleAppleBackground
    )
}

internal fun SubtitleVisualSettings.coerced(): SubtitleVisualSettings {
    return copy(
        bottomPaddingFraction = bottomPaddingFraction.coerceIn(
            MIN_SUBTITLE_BOTTOM_PADDING,
            MAX_SUBTITLE_BOTTOM_PADDING
        ),
        textSizeFraction = textSizeFraction.coerceIn(
            MIN_SUBTITLE_TEXT_SIZE,
            MAX_SUBTITLE_TEXT_SIZE
        )
    )
}

internal fun loadSubtitleVisualSettings(
    context: Context
): SubtitleVisualSettings {
    val prefs = context.getSharedPreferences(
        PLAYER_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    val savedVersion = prefs.getInt(
        KEY_SUBTITLE_SETTINGS_VERSION,
        0
    )

    if (savedVersion < CURRENT_SUBTITLE_SETTINGS_VERSION) {
        val defaultSettings = defaultSubtitleVisualSettings()
        saveSubtitleVisualSettings(context, defaultSettings)
        return defaultSettings
    }

    return SubtitleVisualSettings(
        bottomPaddingFraction = prefs.getFloat(
            KEY_SUBTITLE_BOTTOM_PADDING,
            DEFAULT_SUBTITLE_BOTTOM_PADDING
        ),
        textSizeFraction = prefs.getFloat(
            KEY_SUBTITLE_TEXT_SIZE,
            DEFAULT_SUBTITLE_TEXT_SIZE
        ),
        textColorArgb = prefs.getInt(
            KEY_SUBTITLE_TEXT_COLOR,
            AndroidColor.WHITE
        ),
        backgroundColorArgb = prefs.getInt(
            KEY_SUBTITLE_BACKGROUND_COLOR,
            SubtitleAppleBackground
        )
    ).coerced()
}

internal fun saveSubtitleVisualSettings(
    context: Context,
    settings: SubtitleVisualSettings
) {
    context
        .getSharedPreferences(
            PLAYER_PREFS_NAME,
            Context.MODE_PRIVATE
        )
        .edit {
            putInt(KEY_SUBTITLE_SETTINGS_VERSION, CURRENT_SUBTITLE_SETTINGS_VERSION)
            putFloat(KEY_SUBTITLE_BOTTOM_PADDING, settings.bottomPaddingFraction)
            putFloat(KEY_SUBTITLE_TEXT_SIZE, settings.textSizeFraction)
            putInt(KEY_SUBTITLE_TEXT_COLOR, settings.textColorArgb)
            putInt(KEY_SUBTITLE_BACKGROUND_COLOR, settings.backgroundColorArgb)
        }
}

internal fun resetSubtitleVisualSettings(
    context: Context
): SubtitleVisualSettings {
    val defaultSettings = defaultSubtitleVisualSettings()

    context
        .getSharedPreferences(
            PLAYER_PREFS_NAME,
            Context.MODE_PRIVATE
        )
        .edit {
            putInt(KEY_SUBTITLE_SETTINGS_VERSION, CURRENT_SUBTITLE_SETTINGS_VERSION)
            putFloat(KEY_SUBTITLE_BOTTOM_PADDING, defaultSettings.bottomPaddingFraction)
            putFloat(KEY_SUBTITLE_TEXT_SIZE, defaultSettings.textSizeFraction)
            putInt(KEY_SUBTITLE_TEXT_COLOR, defaultSettings.textColorArgb)
            putInt(KEY_SUBTITLE_BACKGROUND_COLOR, defaultSettings.backgroundColorArgb)
        }

    return defaultSettings
}

internal fun subtitlePositionLabel(
    bottomPaddingFraction: Float
): String {
    val percentage = (
            (bottomPaddingFraction - MIN_SUBTITLE_BOTTOM_PADDING) /
                    (MAX_SUBTITLE_BOTTOM_PADDING - MIN_SUBTITLE_BOTTOM_PADDING) *
                    100f
            )
        .coerceIn(0f, 100f)
        .roundToInt()

    return "$percentage%"
}

internal fun subtitleSizeLabel(
    textSizeFraction: Float
): String {
    return when {
        textSizeFraction < 0.048f -> "Pequena"
        textSizeFraction <= 0.064f -> "Normal"
        else -> "Grande"
    }
}

internal fun subtitleFontSize(
    textSizeFraction: Float
): Float {
    return (textSizeFraction * 520f).coerceIn(18f, 42f)
}

internal fun subtitleTextColorLabel(
    colorArgb: Int
): String {
    return when (colorArgb) {
        AndroidColor.WHITE -> "Branco"
        SubtitleYellow -> "Amarelo"
        SubtitleOrange -> "Laranja"
        else -> "Livre"
    }
}

internal fun subtitleBackgroundLabel(
    colorArgb: Int
): String {
    return when (colorArgb) {
        SubtitleAppleBackground -> "Transparente"
        AndroidColor.TRANSPARENT -> "Sem fundo"
        SubtitleBlackBackground -> "Preto"
        else -> "Livre"
    }
}

internal fun backgroundAlphaLabel(
    colorArgb: Int
): String {
    val alpha = AndroidColor.alpha(colorArgb)
    val percentage = ((alpha / 255f) * 100f).roundToInt()

    return "$percentage%"
}

internal fun colorLabel(
    colorArgb: Int
): String {
    val alpha = AndroidColor.alpha(colorArgb)
    val red = AndroidColor.red(colorArgb)
    val green = AndroidColor.green(colorArgb)
    val blue = AndroidColor.blue(colorArgb)

    return if (alpha == 255) {
        String.format(Locale.US, "#%02X%02X%02X", red, green, blue)
    } else {
        String.format(Locale.US, "#%02X%02X%02X%02X", alpha, red, green, blue)
    }
}

internal fun hueFromColor(
    colorArgb: Int
): Float {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(colorArgb, hsv)
    return hsv[0].coerceIn(0f, 360f)
}

internal fun colorWithHue(
    colorArgb: Int,
    hue: Float,
    includeAlpha: Boolean
): Int {
    val alpha = if (includeAlpha) {
        AndroidColor.alpha(colorArgb)
    } else {
        255
    }

    return AndroidColor.HSVToColor(
        alpha,
        floatArrayOf(
            hue.coerceIn(0f, 360f),
            1f,
            1f
        )
    )
}

internal fun colorWithAlpha(
    colorArgb: Int,
    alpha: Int
): Int {
    return AndroidColor.argb(
        alpha.coerceIn(0, 255),
        AndroidColor.red(colorArgb),
        AndroidColor.green(colorArgb),
        AndroidColor.blue(colorArgb)
    )
}

internal fun spectrumBrush(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red
        )
    )
}