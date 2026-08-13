package com.rnx.laranjada.feature.player

import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import java.util.Locale

internal fun ExoPlayer.safeDuration(): Long {
    val playerDuration = duration

    return if (playerDuration == C.TIME_UNSET || playerDuration < 0L) {
        0L
    } else {
        playerDuration
    }
}

internal fun formatTime(
    timeMs: Long
): String {
    val safeTimeMs = timeMs.coerceAtLeast(0L)
    val totalSeconds = safeTimeMs / 1000L

    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L

    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

internal fun formatSpeed(
    speed: Float
): String {
    return if (speed % 1f == 0f) {
        speed.toInt().toString()
    } else {
        speed.toString().trimEnd('0').trimEnd('.')
    }
}

internal fun buildTrackLabel(
    fallback: String,
    language: String?,
    name: String?
): String {
    val cleanName = name
        ?.trim()
        ?.takeIf { it.isNotBlank() }

    val languageName = languageDisplayName(language)

    return when {
        cleanName != null && languageName != null -> {
            if (cleanName.contains(languageName, ignoreCase = true)) {
                cleanName
            } else {
                "$cleanName ($languageName)"
            }
        }

        cleanName != null -> cleanName
        languageName != null -> languageName
        else -> fallback
    }
}

private fun languageDisplayName(language: String?): String? {
    val cleanLanguage = language
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: return null

    val lower = cleanLanguage.lowercase(Locale.US)

    val mapped = when (lower) {
        "pt", "pt-br", "por", "ptb" -> "Português"
        "en", "eng" -> "Inglês"
        "es", "spa" -> "Espanhol"
        "ja", "jpn" -> "Japonês"
        "ko", "kor" -> "Coreano"
        "fr", "fre", "fra" -> "Francês"
        "de", "ger", "deu" -> "Alemão"
        "it", "ita" -> "Italiano"
        else -> null
    }

    if (mapped != null) {
        return mapped
    }

    val locale = Locale.forLanguageTag(cleanLanguage)

    val displayName = locale
        .getDisplayLanguage(BrazilianPortugueseLocale)
        .trim()
        .takeIf { it.isNotBlank() && it != cleanLanguage }

    return displayName?.replaceFirstChar { char ->
        if (char.isLowerCase()) {
            char.titlecase(BrazilianPortugueseLocale)
        } else {
            char.toString()
        }
    } ?: cleanLanguage
}