package com.rnx.laranjada.feature.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaSurface
import com.rnx.laranjada.core.design.theme.LaranjadaSurfaceLight
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.feature.details.data.DetailMockData
import com.rnx.laranjada.feature.home.components.HomeBottomBar

@Composable
fun DetailScreen(
    uiState: DetailUiState = DetailMockData.serieDetail,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onFavoriteClick: (Boolean) -> Unit = {},
    onEpisodeClick: (EpisodeUi) -> Unit = {}
) {
    val selectedBottomIndex = remember { mutableIntStateOf(0) }
    var selectedSeasonIndex by remember { mutableIntStateOf(0) }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.seasons.size) {
        if (selectedSeasonIndex >= uiState.seasons.size) {
            selectedSeasonIndex = 0
        }
    }

    val selectedSeason = uiState.seasons.getOrNull(selectedSeasonIndex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LaranjadaBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 132.dp)
        ) {
            item {
                DetailHero(
                    uiState = uiState,
                    onBackClick = onBackClick
                )
            }

            item {
                DetailMainInfo(
                    uiState = uiState,
                    isFavorite = isFavorite,
                    onPlayClick = onPlayClick,
                    onRestartClick = onRestartClick,
                    onFavoriteClick = {
                        isFavorite = !isFavorite
                        onFavoriteClick(isFavorite)
                    }
                )
            }

            if (uiState.isSeries) {
                item {
                    EpisodesTitle()
                }

                item {
                    SeasonHeader(
                        seasons = uiState.seasons,
                        selectedSeasonIndex = selectedSeasonIndex,
                        onSeasonSelected = { index ->
                            selectedSeasonIndex = index
                        }
                    )
                }

                if (selectedSeason != null) {
                    items(selectedSeason.episodes) { episode ->
                        EpisodeCard(
                            episode = episode,
                            onClick = {
                                onEpisodeClick(episode)
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        HomeBottomBar(
            selectedIndex = selectedBottomIndex.intValue,
            onItemClick = { index ->
                selectedBottomIndex.intValue = index
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DetailHero(
    uiState: DetailUiState,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
            .background(LaranjadaBlack)
    ) {
        AsyncImage(
            model = uiState.imageDetailUrl,
            contentDescription = uiState.title,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black.copy(alpha = 0.15f),
                            0.35f to Color.Black.copy(alpha = 0.05f),
                            0.75f to Color.Black.copy(alpha = 0.74f),
                            1.00f to LaranjadaBlack
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = "Fechar",
                onClick = onBackClick
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.title,
                color = LaranjadaText,
                fontSize = 31.sp,
                lineHeight = 35.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            MetadataLine(uiState = uiState)
        }
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.45f)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

@Composable
private fun MetadataLine(
    uiState: DetailUiState
) {
    val years = if (uiState.endYear != null) {
        "${uiState.year} – ${uiState.endYear}"
    } else {
        uiState.year
    }

    val genresText = uiState.genres.take(2).joinToString(", ")

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = years,
            color = LaranjadaMutedText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        MetaDot()

        Text(
            text = uiState.durationInfo,
            color = LaranjadaMutedText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        if (genresText.isNotBlank()) {
            MetaDot()

            Text(
                text = genresText,
                color = LaranjadaMutedText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MetaDot() {
    Text(
        text = " • ",
        color = LaranjadaMutedText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun DetailMainInfo(
    uiState: DetailUiState,
    isFavorite: Boolean,
    onPlayClick: () -> Unit,
    onRestartClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RatingBadge(text = uiState.rating)

        if (uiState.hasWatchProgress) {
            Spacer(modifier = Modifier.height(20.dp))

            WatchProgressBar(
                watchProgress = uiState.watchProgress
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onPlayClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(7.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(31.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (uiState.hasWatchProgress) "CONTINUAR" else "ASSISTIR",
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        DetailActionsRow(
            hasWatchProgress = uiState.hasWatchProgress,
            isFavorite = isFavorite,
            onRestartClick = onRestartClick,
            onFavoriteClick = onFavoriteClick
        )

        if (uiState.isSeries && uiState.watchProgress?.hasEpisodeInfo == true) {
            Spacer(modifier = Modifier.height(22.dp))

            ContinueEpisodeInfo(
                watchProgress = uiState.watchProgress
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = uiState.synopsis,
            color = LaranjadaText,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun WatchProgressBar(
    watchProgress: WatchProgressUi?
) {
    if (watchProgress == null) return

    val progress = watchProgress.progress.coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(LaranjadaSurfaceLight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LaranjadaOrange)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = watchProgress.remainingText,
            color = LaranjadaMutedText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DetailActionsRow(
    hasWatchProgress: Boolean,
    isFavorite: Boolean,
    onRestartClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasWatchProgress) {
            RestartButton(
                onClick = onRestartClick
            )

            Spacer(modifier = Modifier.width(56.dp))
        }

        FavoriteButton(
            isFavorite = isFavorite,
            onClick = onFavoriteClick
        )
    }
}

@Composable
private fun RestartButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(54.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Replay,
            contentDescription = "Reiniciar",
            tint = LaranjadaText,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(54.dp)
    ) {
        Icon(
            imageVector = if (isFavorite) {
                Icons.Rounded.Favorite
            } else {
                Icons.Rounded.FavoriteBorder
            },
            contentDescription = if (isFavorite) {
                "Remover dos favoritos"
            } else {
                "Adicionar aos favoritos"
            },
            tint = if (isFavorite) {
                Color(0xFFE53935)
            } else {
                LaranjadaText
            },
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
private fun ContinueEpisodeInfo(
    watchProgress: WatchProgressUi
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "T${watchProgress.seasonNumber}:E${watchProgress.episodeNumber} ${watchProgress.episodeTitle}",
            color = LaranjadaText,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RatingBadge(
    text: String
) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = if (text.equals("AL", ignoreCase = true)) {
            Color(0xFF11A65B)
        } else {
            LaranjadaSurfaceLight
        }
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EpisodesTitle() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    ) {
        Text(
            text = "EPISÓDIOS",
            color = LaranjadaText,
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .height(5.dp)
                .width(122.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        )

        HorizontalDivider(
            color = LaranjadaSurfaceLight,
            thickness = 1.dp
        )
    }
}

@Composable
private fun SeasonHeader(
    seasons: List<SeasonUi>,
    selectedSeasonIndex: Int,
    onSeasonSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(top = 24.dp, bottom = 18.dp)
    ) {
        SeasonSelector(
            seasons = seasons,
            selectedSeasonIndex = selectedSeasonIndex,
            onSeasonSelected = onSeasonSelected
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Classificação da Temporada ${seasons.getOrNull(selectedSeasonIndex)?.number ?: 1}: ",
                color = LaranjadaText,
                fontSize = 14.sp
            )

            RatingBadge(text = "AL")
        }
    }
}

@Composable
private fun SeasonSelector(
    seasons: List<SeasonUi>,
    selectedSeasonIndex: Int,
    onSeasonSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = LaranjadaSurfaceLight,
            modifier = Modifier.clickable {
                expanded = true
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Temporada ${seasons.getOrNull(selectedSeasonIndex)?.number ?: 1}",
                    color = LaranjadaText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = LaranjadaText,
                    modifier = Modifier.size(27.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.background(LaranjadaSurface)
        ) {
            seasons.forEachIndexed { index, season ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Temporada ${season.number}",
                            color = LaranjadaText
                        )
                    },
                    onClick = {
                        onSeasonSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: EpisodeUi,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp)
            .padding(bottom = 30.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LaranjadaSurfaceLight)
            ) {
                AsyncImage(
                    model = episode.imageUrl,
                    contentDescription = episode.title,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .align(Alignment.Center),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Assistir episódio",
                        tint = Color.Black,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${episode.number}. ${episode.title}",
                    color = LaranjadaText,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = episode.runtime,
                        color = LaranjadaMutedText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    RatingBadge(text = episode.rating)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = episode.synopsis,
            color = LaranjadaText,
            fontSize = 18.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.Normal
        )
    }
}