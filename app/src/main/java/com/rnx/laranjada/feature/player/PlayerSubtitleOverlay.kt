package com.rnx.laranjada.feature.player

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.text.Cue

@Composable
internal fun CustomSubtitleOverlay(
    cues: List<Cue>,
    settings: SubtitleVisualSettings,
    modifier: Modifier = Modifier
) {
    val subtitleText = cues
        .mapNotNull { cue ->
            cue.text
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }
        .joinToString(separator = "\n")

    if (subtitleText.isBlank()) {
        return
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        val bottomPadding = (maxHeight.value * settings.bottomPaddingFraction).dp
        val backgroundAlpha = AndroidColor.alpha(settings.backgroundColorArgb)

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = bottomPadding
                )
                .then(
                    if (backgroundAlpha > 0) {
                        Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(settings.backgroundColorArgb))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    } else {
                        Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    }
                )
        ) {
            Text(
                text = subtitleText,
                color = Color(settings.textColorArgb),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = subtitleFontSize(settings.textSizeFraction).sp,
                lineHeight = (subtitleFontSize(settings.textSizeFraction) * 1.18f).sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(1.5f, 1.5f),
                        blurRadius = 3f
                    )
                )
            )
        }
    }
}