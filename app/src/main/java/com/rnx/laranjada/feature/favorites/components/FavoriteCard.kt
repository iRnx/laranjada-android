
package com.rnx.laranjada.feature.favorites.components

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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.domain.model.Favorite
import java.util.Locale

@Composable
fun FavoriteCard(
    item: Favorite,
    removing: Boolean,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),

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
                    Color(0xFF15191D)
                )
                .clickable(
                    onClick = onOpen
                )
        ) {
            AsyncImage(
                model = item.imageUrl,

                contentDescription =
                    item.title,

                contentScale =
                    ContentScale.Crop,

                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )

            if (item.isLocked) {
                Icon(
                    imageVector =
                        Icons.Rounded.Lock,

                    contentDescription =
                        "Bloqueado no seu plano",

                    tint =
                        Color.White,

                    modifier = Modifier
                        .align(
                            Alignment.TopStart
                        )
                        .padding(7.dp)
                        .size(17.dp)
                )
            }

            /*
             * CORAÇÃO NO ESTILO DA WEB
             *
             * Sem fundo circular.
             * Ícone compacto de 19dp.
             * Área de toque de 40dp.
             *
             * Posicionado no canto
             * inferior direito.
             */
            Box(
                modifier = Modifier
                    .align(
                        Alignment.BottomEnd
                    )
                    .padding(
                        end = 1.dp,
                        bottom = 0.dp
                    ),

                contentAlignment =
                    Alignment.Center
            ) {
                if (removing) {
                    CircularProgressIndicator(
                        color =
                            LaranjadaOrange,

                        strokeWidth =
                            2.dp,

                        modifier = Modifier
                            .padding(11.dp)
                            .size(17.dp)
                    )
                } else {
                    IconButton(
                        onClick =
                            onRemove,

                        modifier =
                            Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Favorite,

                            contentDescription =
                                "Remover ${item.title} dos favoritos",

                            tint =
                                LaranjadaOrange,

                            modifier =
                                Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        Text(
            text =
                item.title,

            color =
                Color.White,

            fontWeight =
                FontWeight.SemiBold,

            fontSize =
                12.sp,

            textAlign =
                TextAlign.Center,

            maxLines =
                2,

            lineHeight =
                15.sp,

            overflow =
                TextOverflow.Ellipsis,

            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp)
                .clickable(onClick = onOpen)
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(5.dp),

            verticalAlignment =
                Alignment.CenterVertically,

            modifier =
                Modifier.padding(top = 3.dp)
        ) {
            item.year?.let { year ->
                Text(
                    text =
                        year.toString(),

                    color =
                        Color(0xFFBFC4C8),

                    fontSize =
                        11.sp
                )
            }

            item.rating
                ?.takeIf { it > 0.0 }
                ?.let { rating ->

                    Text(
                        text =
                            String.format(
                                Locale.US,
                                "%.1f",
                                rating
                            ),

                        color =
                            Color.White,

                        fontSize =
                            11.sp
                    )

                    Icon(
                        imageVector =
                            Icons.Rounded.Star,

                        contentDescription =
                            null,

                        tint =
                            Color(0xFFFFCC00),

                        modifier =
                            Modifier.size(12.dp)
                    )
                }
        }
    }
}
