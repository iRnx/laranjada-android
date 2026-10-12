package com.rnx.laranjada.feature.support

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal val SupportPanel =
    Color(
        0xFF101417
    )

internal val SupportPanelSecondary =
    Color(
        0xFF171A1E
    )

internal val SupportOrangeDark =
    Color(
        0xFF3A210C
    )

internal val SupportGreen =
    Color(
        0xFF3FB950
    )

internal val SupportRed =
    Color(
        0xFFFF6B6B
    )

internal val SupportWarning =
    Color(
        0xFFFFB547
    )

internal val SupportBlue =
    Color(
        0xFF65A9FF
    )

internal fun supportStatusColor(
    code: String
): Color {

    return when (
        code
            .trim()
            .lowercase()
    ) {
        "new" ->
            SupportWarning

        "in_progress" ->
            SupportBlue

        "waiting_user" ->
            SupportWarning

        "resolved" ->
            SupportGreen

        "rejected" ->
            SupportRed

        "closed" ->
            Color(
                0xFF94979C
            )

        else ->
            Color(
                0xFF94979C
            )
    }
}

internal fun supportDecisionColor(
    code: String
): Color {

    return when (
        code
            .trim()
            .lowercase()
    ) {
        "approved" ->
            SupportGreen

        "rejected" ->
            SupportRed

        else ->
            SupportWarning
    }
}

internal fun supportCategoryIcon(
    code: String
): ImageVector {

    return when (
        code
            .trim()
            .lowercase()
    ) {
        "content_request" ->
            Icons.Rounded.Movie

        "complaint" ->
            Icons.Rounded.ErrorOutline

        "technical_issue" ->
            Icons.Rounded.Build

        "playback_issue" ->
            Icons.Rounded.PlayCircleOutline

        "account_profiles" ->
            Icons.Rounded.AccountCircle

        "subscription_payment" ->
            Icons.Rounded.Payments

        "suggestion" ->
            Icons.Rounded.Lightbulb

        else ->
            Icons.Rounded.ChatBubbleOutline
    }
}

internal fun formatSupportDateTime(
    value: String?
): String {

    val date =
        parseSupportDate(
            value
        )
            ?: return "Não informado"

    return SimpleDateFormat(
        "dd/MM/yyyy 'às' HH:mm",
        Locale(
            "pt",
            "BR"
        )
    )
        .format(
            date
        )
}

internal fun formatSupportListDate(
    value: String?
): String {

    val date =
        parseSupportDate(
            value
        )
            ?: return ""

    return SimpleDateFormat(
        "dd/MM/yyyy • HH:mm",
        Locale(
            "pt",
            "BR"
        )
    )
        .format(
            date
        )
}

internal fun formatSupportTime(
    value: String?
): String {

    val date =
        parseSupportDate(
            value
        )
            ?: return ""

    return SimpleDateFormat(
        "HH:mm",
        Locale(
            "pt",
            "BR"
        )
    )
        .format(
            date
        )
}

private fun parseSupportDate(
    value: String?
): java.util.Date? {

    val raw =
        value
            ?.trim()
            .orEmpty()

    if (
        raw.isBlank()
    ) {
        return null
    }

    val normalized =
        raw.replace(
            Regex(
                """\.(\d{3})\d+(?=Z$|[+-]\d{2}:\d{2}$)"""
            )
        ) {
                match ->

            ".${match.groupValues[1]}"
        }

    val patterns =
        listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )

    for (
    pattern in
    patterns
    ) {
        val parser =
            SimpleDateFormat(
                pattern,
                Locale.US
            )
                .apply {
                    isLenient =
                        false

                    if (
                        pattern.endsWith(
                            "'Z'"
                        )
                    ) {
                        timeZone =
                            TimeZone.getTimeZone(
                                "UTC"
                            )
                    }
                }

        val parsed =
            runCatching {
                parser.parse(
                    normalized
                )
            }
                .getOrNull()

        if (
            parsed != null
        ) {
            return parsed
        }
    }

    return null
}