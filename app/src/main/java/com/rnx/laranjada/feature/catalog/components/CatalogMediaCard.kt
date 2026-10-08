
package com.rnx.laranjada.feature.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.feature.home.MediaItemUi

@Composable
fun CatalogMediaCard(
    item: MediaItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    /*
     * O MediaGrid devolve geralmente:
     *
     * "2026 • 8.5"
     *
     * Coleções pode devolver:
     *
     * "Filme" ou "Série"
     *
     * Preservamos ambas as estruturas.
     */
    val subtitleParts =
        item.subtitle.split(
            " • ",
            limit = 2
        )

    val firstPart =
        subtitleParts.firstOrNull()
            .orEmpty()
            .trim()

    val rating =
        subtitleParts
            .getOrNull(1)
            ?.trim()
            ?.toDoubleOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth(),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(
                    RoundedCornerShape(5.dp)
                )
                .background(
                    Brush.linearGradient(
                        item.gradientColors
                    )
                )
                .clickable(
                    onClick = onClick
                )
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = item.title,
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,

            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp)
                .clickable(
                    onClick = onClick
                )
        )

        if (item.subtitle.isNotBlank()) {
            Row(
                modifier = Modifier.padding(
                    top = 4.dp
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(5.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                if (firstPart.isNotBlank()) {
                    Text(
                        text = firstPart,
                        color = Color(0xFFBFC4C8),
                        fontSize = 11.sp
                    )
                }

                if (
                    rating != null &&
                    rating > 0.0
                ) {
                    Text(
                        text = subtitleParts[1].trim(),
                        color = Color.White,
                        fontSize = 11.sp
                    )

                    Icon(
                        imageVector =
                            Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFCC00),
                        modifier = Modifier.size(12.dp)
                    )
                } else if (
                    subtitleParts.size > 1
                ) {
                    Text(
                        text = subtitleParts[1].trim(),
                        color = Color(0xFFBFC4C8),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
