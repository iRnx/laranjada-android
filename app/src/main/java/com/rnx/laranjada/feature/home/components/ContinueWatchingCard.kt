package com.rnx.laranjada.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.feature.home.ContinueWatchingUi

@Composable
fun ContinueWatchingCard(
    item: ContinueWatchingUi,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Column(
        modifier =
            modifier
                .width(
                    210.dp
                )
                .clickable(
                    interactionSource =
                        interactionSource,

                    indication =
                        null,

                    onClick =
                        onClick
                )
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        16f / 9f
                    )
                    .clip(
                        RoundedCornerShape(
                            12.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            item.gradientColors
                        )
                    )
        ) {
            AsyncImage(
                model =
                    item.imageUrl,

                contentDescription =
                    item.title,

                modifier =
                    Modifier.matchParentSize(),

                contentScale =
                    ContentScale.Crop
            )

            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,

                                        Color.Black.copy(
                                            alpha =
                                                0.35f
                                        )
                                    )
                            )
                        )
            )

            Box(
                modifier =
                    Modifier
                        .align(
                            Alignment.Center
                        )
                        .size(
                            42.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            Color.Black.copy(
                                alpha =
                                    0.50f
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.PlayArrow,

                    contentDescription =
                        "Continuar assistindo",

                    tint =
                        LaranjadaText,

                    modifier =
                        Modifier.size(
                            27.dp
                        )
                )
            }

            LinearProgressIndicator(
                progress = {
                    item.progress
                },

                modifier =
                    Modifier
                        .align(
                            Alignment.BottomStart
                        )
                        .fillMaxWidth()
                        .height(
                            4.dp
                        ),

                color =
                    LaranjadaOrange,

                trackColor =
                    Color.White.copy(
                        alpha =
                            0.16f
                    )
            )
        }

        if (
            item.remainingTime
                .isNotBlank()
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )

            Text(
                text =
                    item.remainingTime,

                color =
                    LaranjadaMutedText,

                fontSize =
                    11.sp,

                fontWeight =
                    FontWeight.Medium,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    5.dp
                )
        )

        Text(
            text =
                item.title,

            color =
                LaranjadaText,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.SemiBold,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )

        if (
            item.episodeInfo
                .isNotBlank()
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        3.dp
                    )
            )

            Text(
                text =
                    item.episodeInfo,

                color =
                    LaranjadaMutedText,

                fontSize =
                    12.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}