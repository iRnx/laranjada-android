package com.rnx.laranjada.feature.player

import android.graphics.Color as AndroidColor
import androidx.media3.common.TrackGroup
import java.util.Locale

internal const val PLAYER_LOG_TAG = "PlayerScreen"

internal const val AUTO_AUDIO_ID = "audio_auto"
internal const val SUBTITLE_OFF_ID = "subtitle_off"

internal const val PLAYER_PREFS_NAME = "laranjada_player_prefs"

internal const val KEY_SUBTITLE_SETTINGS_VERSION = "subtitle_settings_version"
internal const val CURRENT_SUBTITLE_SETTINGS_VERSION = 3

internal const val KEY_SUBTITLE_BOTTOM_PADDING = "subtitle_bottom_padding"
internal const val KEY_SUBTITLE_TEXT_SIZE = "subtitle_text_size"
internal const val KEY_SUBTITLE_TEXT_COLOR = "subtitle_text_color"
internal const val KEY_SUBTITLE_BACKGROUND_COLOR = "subtitle_background_color"

internal const val DEFAULT_SUBTITLE_BOTTOM_PADDING = 0.117f
internal const val DEFAULT_SUBTITLE_TEXT_SIZE = 0.046f

internal const val MIN_SUBTITLE_BOTTOM_PADDING = 0.04f
internal const val MAX_SUBTITLE_BOTTOM_PADDING = 0.55f

internal const val MIN_SUBTITLE_TEXT_SIZE = 0.035f
internal const val MAX_SUBTITLE_TEXT_SIZE = 0.078f

internal val BrazilianPortugueseLocale: Locale = Locale.forLanguageTag("pt-BR")

internal val SubtitleYellow: Int = AndroidColor.rgb(255, 235, 59)
internal val SubtitleOrange: Int = AndroidColor.rgb(253, 107, 0)
internal val SubtitleAppleBackground: Int = AndroidColor.argb(120, 0, 0, 0)
internal val SubtitleBlackBackground: Int = AndroidColor.argb(190, 0, 0, 0)

internal enum class TrackMenuType {
    AUDIO,
    SUBTITLE,
    SUBTITLE_SETTINGS,
    SPEED,
    EPISODES
}

internal data class PlayerTrackOption(
    val id: String,
    val label: String,
    val language: String? = null,
    val trackGroup: TrackGroup? = null,
    val trackIndex: Int = -1,
    val isAuto: Boolean = false,
    val isOff: Boolean = false
)

internal data class SubtitleVisualSettings(
    val bottomPaddingFraction: Float = DEFAULT_SUBTITLE_BOTTOM_PADDING,
    val textSizeFraction: Float = DEFAULT_SUBTITLE_TEXT_SIZE,
    val textColorArgb: Int = AndroidColor.WHITE,
    val backgroundColorArgb: Int = SubtitleAppleBackground
)

internal data class SubtitleChoice(
    val label: String,
    val colorArgb: Int
)