package com.rnx.laranjada.feature.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.rnx.laranjada.feature.home.MediaItemUi
import com.rnx.laranjada.feature.home.components.HomeBottomBar

private enum class DetailTab {
    Episodes,
    Suggestions,
    Details
}

@Composable
fun DetailScreen(
    uiState: DetailUiState,
    relatedItems: List<MediaItemUi> =
        emptyList(),
    isRelatedLoading: Boolean =
        false,
    relatedErrorMessage: String? =
        null,

    /*
     * ViewerProfile selecionado.
     */
    profileName: String,
    profileAvatarUrl: String?,

    modifier: Modifier =
        Modifier,

    onBackClick: () -> Unit =
        {},

    onAccountClick: () -> Unit =
        {},

    onPlayClick: () -> Unit =
        {},

    onRestartClick: () -> Unit =
        {},

    onFavoriteClick: (Boolean) -> Unit =
        {},

    onRelatedClick: (MediaItemUi) -> Unit =
        {},

    onRetryRelatedClick: () -> Unit =
        {},

    onEpisodeClick: (EpisodeUi) -> Unit =
        {}
) {
    val selectedBottomIndex =
        remember {
            mutableIntStateOf(
                0
            )
        }

    var selectedSeasonIndex by remember {
        mutableIntStateOf(
            0
        )
    }

    var isFavorite by remember {
        mutableStateOf(
            false
        )
    }

    var selectedTab by remember(
        uiState.uuid,
        uiState.isSeries
    ) {
        mutableStateOf(
            if (
                uiState.isSeries
            ) {
                DetailTab.Episodes
            } else {
                DetailTab.Suggestions
            }
        )
    }

    LaunchedEffect(
        uiState.seasons.size
    ) {
        if (
            selectedSeasonIndex >=
            uiState.seasons.size
        ) {
            selectedSeasonIndex =
                0
        }
    }

    val selectedSeason =
        uiState.seasons
            .getOrNull(
                selectedSeasonIndex
            )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                LaranjadaBlack
            )
    ) {
        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    bottom = 124.dp
                )
        ) {
            item {
                DetailHero(
                    uiState =
                        uiState,

                    onBackClick =
                        onBackClick
                )
            }

            item {
                DetailMainInfo(
                    uiState =
                        uiState,

                    isFavorite =
                        isFavorite,

                    onPlayClick =
                        onPlayClick,

                    onRestartClick =
                        onRestartClick,

                    onFavoriteClick = {
                        isFavorite =
                            !isFavorite

                        onFavoriteClick(
                            isFavorite
                        )
                    }
                )
            }

            item {
                DetailTabs(
                    isSeries =
                        uiState.isSeries,

                    selectedTab =
                        selectedTab,

                    onTabSelected = {
                            tab ->

                        selectedTab =
                            tab
                    }
                )
            }

            when (
                selectedTab
            ) {
                DetailTab.Episodes -> {
                    if (
                        uiState.isSeries
                    ) {
                        item {
                            SeasonHeader(
                                seasons =
                                    uiState.seasons,

                                selectedSeasonIndex =
                                    selectedSeasonIndex,

                                onSeasonSelected = {
                                        index ->

                                    selectedSeasonIndex =
                                        index
                                }
                            )
                        }

                        if (
                            selectedSeason != null
                        ) {
                            items(
                                selectedSeason.episodes
                            ) {
                                    episode ->

                                EpisodeCard(
                                    episode =
                                        episode,

                                    onClick = {
                                        if (
                                            episode
                                                .hlsUrl
                                                .isNotBlank()
                                        ) {
                                            onEpisodeClick(
                                                episode
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                DetailTab.Suggestions -> {
                    item {
                        RelatedSuggestionsSection(
                            relatedItems =
                                relatedItems,

                            isLoading =
                                isRelatedLoading,

                            errorMessage =
                                relatedErrorMessage,

                            onItemClick =
                                onRelatedClick,

                            onRetryClick =
                                onRetryRelatedClick
                        )
                    }
                }

                DetailTab.Details -> {
                    item {
                        DetailInfoSection(
                            uiState =
                                uiState
                        )
                    }
                }
            }

            item {
                Spacer(
                    modifier =
                        Modifier.height(
                            24.dp
                        )
                )
            }
        }

        /*
         * Agora o detalhe recebe o mesmo
         * ViewerProfile utilizado pela Home.
         *
         * Portanto não aparece mais "?"
         * quando existe um avatar real.
         */
        HomeBottomBar(
            selectedIndex =
                selectedBottomIndex
                    .intValue,

            profileName =
                profileName,

            profileAvatarUrl =
                profileAvatarUrl,

            onItemClick = {
                    index ->

                if (
                    index == 3
                ) {
                    /*
                     * Perfil / Conta.
                     */
                    onAccountClick()
                } else {
                    selectedBottomIndex
                        .intValue =
                        index
                }
            },

            modifier =
                Modifier.align(
                    Alignment.BottomCenter
                )
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
            .height(
                460.dp
            )
            .background(
                LaranjadaBlack
            )
    ) {
        AsyncImage(
            model =
                uiState.imageDetailUrl,

            contentDescription =
                uiState.title,

            modifier =
                Modifier.matchParentSize(),

            contentScale =
                ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops =
                            arrayOf(
                                0.00f to
                                        Color.Black.copy(
                                            alpha = 0.42f
                                        ),

                                0.18f to
                                        Color.Black.copy(
                                            alpha = 0.12f
                                        ),

                                0.62f to
                                        Color.Black.copy(
                                            alpha = 0.34f
                                        ),

                                0.84f to
                                        Color.Black.copy(
                                            alpha = 0.82f
                                        ),

                                1.00f to
                                        LaranjadaBlack
                            )
                    )
                )
        )

        DetailTopBar(
            title =
                uiState.title,

            onBackClick =
                onBackClick
        )

        Column(
            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .padding(
                    horizontal = 24.dp
                )
                .padding(
                    bottom = 16.dp
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    uiState.title,

                color =
                    LaranjadaText,

                fontSize =
                    29.sp,

                lineHeight =
                    33.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                textAlign =
                    TextAlign.Center,

                maxLines =
                    3,

                overflow =
                    TextOverflow.Ellipsis
            )

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            MetadataLine(
                uiState =
                    uiState
            )
        }
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
            .padding(
                start = 6.dp,
                end = 14.dp,
                top = 8.dp,
                bottom = 8.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        IconButton(
            onClick =
                onBackClick
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.ArrowBack,

                contentDescription =
                    "Voltar",

                tint =
                    LaranjadaText,

                modifier =
                    Modifier.size(
                        27.dp
                    )
            )
        }

        Text(
            text =
                title,

            color =
                LaranjadaText,

            fontSize =
                18.sp,

            lineHeight =
                20.sp,

            fontWeight =
                FontWeight.Bold,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis,

            modifier = Modifier
                .weight(
                    1f
                )
                .padding(
                    end = 8.dp
                )
        )
    }
}

@Composable
private fun MetadataLine(
    uiState: DetailUiState
) {
    val years =
        if (
            uiState.endYear != null
        ) {
            "${uiState.year} – ${uiState.endYear}"
        } else {
            uiState.year
        }

    val genresText =
        uiState.genres
            .take(
                2
            )
            .joinToString(
                ", "
            )

    Row(
        horizontalArrangement =
            Arrangement.Center,

        verticalAlignment =
            Alignment.CenterVertically,

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 10.dp
            )
    ) {
        if (
            years.isNotBlank()
        ) {
            Text(
                text =
                    years,

                color =
                    LaranjadaMutedText,

                fontSize =
                    13.sp,

                fontWeight =
                    FontWeight.Medium
            )
        }

        if (
            years.isNotBlank() &&
            uiState.durationInfo
                .isNotBlank()
        ) {
            MetaDot()
        }

        if (
            uiState.durationInfo
                .isNotBlank()
        ) {
            Text(
                text =
                    uiState.durationInfo,

                color =
                    LaranjadaMutedText,

                fontSize =
                    13.sp,

                fontWeight =
                    FontWeight.Medium
            )
        }

        if (
            genresText.isNotBlank()
        ) {
            MetaDot()

            Text(
                text =
                    genresText,

                color =
                    LaranjadaMutedText,

                fontSize =
                    13.sp,

                fontWeight =
                    FontWeight.Medium,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MetaDot() {
    Text(
        text =
            " • ",

        color =
            LaranjadaMutedText,

        fontSize =
            13.sp,

        fontWeight =
            FontWeight.Bold
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
            .padding(
                horizontal = 18.dp
            )
            .padding(
                top = 4.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        RatingWithStar(
            rating =
                uiState.rating
        )

        if (
            uiState.hasWatchProgress
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )

            WatchProgressBar(
                watchProgress =
                    uiState.watchProgress
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    14.dp
                )
        )

        Button(
            onClick =
                onPlayClick,

            modifier = Modifier
                .fillMaxWidth()
                .height(
                    56.dp
                ),

            enabled =
                uiState.canPlay,

            shape =
                RoundedCornerShape(
                    7.dp
                ),

            colors =
                ButtonDefaults
                    .buttonColors(
                        containerColor =
                            Color.White,

                        contentColor =
                            Color.Black,

                        disabledContainerColor =
                            Color.White.copy(
                                alpha = 0.28f
                            ),

                        disabledContentColor =
                            Color.Black.copy(
                                alpha = 0.55f
                            )
                    )
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.PlayArrow,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        30.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            Text(
                text =
                    if (
                        uiState.hasWatchProgress
                    ) {
                        "CONTINUAR"
                    } else {
                        "ASSISTIR"
                    },

                fontSize =
                    21.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                letterSpacing =
                    3.sp
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        DetailActionsRow(
            hasWatchProgress =
                uiState.hasWatchProgress,

            isFavorite =
                isFavorite,

            onRestartClick =
                onRestartClick,

            onFavoriteClick =
                onFavoriteClick
        )

        if (
            uiState.isSeries &&
            uiState.watchProgress
                ?.hasEpisodeInfo == true
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )

            ContinueEpisodeInfo(
                watchProgress =
                    uiState.watchProgress
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )

        Text(
            text =
                uiState.synopsis,

            color =
                LaranjadaText,

            fontSize =
                16.sp,

            lineHeight =
                23.sp,

            fontWeight =
                FontWeight.Normal,

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(
                    26.dp
                )
        )
    }
}

@Composable
private fun RatingWithStar(
    rating: String
) {
    if (
        rating.isBlank()
    ) {
        return
    }

    Row(
        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.Center
    ) {
        Text(
            text =
                rating,

            color =
                LaranjadaText,

            fontSize =
                16.sp,

            fontWeight =
                FontWeight.ExtraBold
        )

        Spacer(
            modifier =
                Modifier.width(
                    5.dp
                )
        )

        Icon(
            imageVector =
                Icons.Rounded.Star,

            contentDescription =
                "Avaliação",

            tint =
                Color(
                    0xFFFFC107
                ),

            modifier =
                Modifier.size(
                    19.dp
                )
        )
    }
}

@Composable
private fun WatchProgressBar(
    watchProgress: WatchProgressUi?
) {
    if (
        watchProgress == null
    ) {
        return
    }

    val progress =
        watchProgress.progress
            .coerceIn(
                0f,
                1f
            )

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(
                    1f
                )
                .height(
                    4.dp
                )
                .clip(
                    RoundedCornerShape(
                        10.dp
                    )
                )
                .background(
                    LaranjadaSurfaceLight
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(
                        progress
                    )
                    .height(
                        4.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            10.dp
                        )
                    )
                    .background(
                        LaranjadaOrange
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    12.dp
                )
        )

        Text(
            text =
                watchProgress
                    .remainingText,

            color =
                LaranjadaMutedText,

            fontSize =
                13.sp,

            fontWeight =
                FontWeight.Medium
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
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.Center,

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        if (
            hasWatchProgress
        ) {
            RestartButton(
                onClick =
                    onRestartClick
            )

            Spacer(
                modifier =
                    Modifier.width(
                        56.dp
                    )
            )
        }

        FavoriteButton(
            isFavorite =
                isFavorite,

            onClick =
                onFavoriteClick
        )
    }
}

@Composable
private fun RestartButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick =
            onClick,

        modifier =
            Modifier.size(
                52.dp
            )
    ) {
        Icon(
            imageVector =
                Icons.Rounded.Replay,

            contentDescription =
                "Reiniciar",

            tint =
                LaranjadaText,

            modifier =
                Modifier.size(
                    31.dp
                )
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick =
            onClick,

        modifier =
            Modifier.size(
                52.dp
            )
    ) {
        Icon(
            imageVector =
                if (
                    isFavorite
                ) {
                    Icons.Rounded.Favorite
                } else {
                    Icons.Rounded
                        .FavoriteBorder
                },

            contentDescription =
                if (
                    isFavorite
                ) {
                    "Remover dos favoritos"
                } else {
                    "Adicionar aos favoritos"
                },

            tint =
                if (
                    isFavorite
                ) {
                    Color(
                        0xFFE53935
                    )
                } else {
                    LaranjadaText
                },

            modifier =
                Modifier.size(
                    31.dp
                )
        )
    }
}

@Composable
private fun ContinueEpisodeInfo(
    watchProgress: WatchProgressUi
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Text(
            text =
                "T${watchProgress.seasonNumber}:E${watchProgress.episodeNumber} ${watchProgress.episodeTitle}",

            color =
                LaranjadaText,

            fontSize =
                21.sp,

            lineHeight =
                27.sp,

            fontWeight =
                FontWeight.ExtraBold,

            maxLines =
                2,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DetailTabs(
    isSeries: Boolean,
    selectedTab: DetailTab,
    onTabSelected: (DetailTab) -> Unit
) {
    val tabs =
        if (
            isSeries
        ) {
            listOf(
                DetailTab.Episodes to
                        "EPISÓDIOS",

                DetailTab.Suggestions to
                        "SUGESTÕES",

                DetailTab.Details to
                        "DETALHES"
            )
        } else {
            listOf(
                DetailTab.Suggestions to
                        "SUGESTÕES",

                DetailTab.Details to
                        "DETALHES"
            )
        }

    val scrollState =
        rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 18.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    scrollState
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    34.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            tabs.forEach {
                    (
                        tab,
                        label
                    ) ->

                DetailTabItem(
                    label =
                        label,

                    selected =
                        selectedTab ==
                                tab,

                    onClick = {
                        onTabSelected(
                            tab
                        )
                    }
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        HorizontalDivider(
            color =
                LaranjadaSurfaceLight,

            thickness =
                1.dp
        )
    }
}

@Composable
private fun DetailTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource =
                    interactionSource,

                indication =
                    null,

                onClick =
                    onClick
            )
            .padding(
                vertical = 4.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                label,

            color =
                if (
                    selected
                ) {
                    LaranjadaText
                } else {
                    LaranjadaMutedText
                },

            fontSize =
                16.sp,

            fontWeight =
                FontWeight.ExtraBold,

            letterSpacing =
                2.6.sp,

            maxLines =
                1,

            softWrap =
                false,

            overflow =
                TextOverflow.Visible
        )

        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

        Box(
            modifier = Modifier
                .height(
                    4.dp
                )
                .width(
                    when (
                        label
                    ) {
                        "EPISÓDIOS" ->
                            104.dp

                        "SUGESTÕES" ->
                            108.dp

                        "DETALHES" ->
                            96.dp

                        else ->
                            90.dp
                    }
                )
                .clip(
                    RoundedCornerShape(
                        12.dp
                    )
                )
                .background(
                    if (
                        selected
                    ) {
                        Color.White
                    } else {
                        Color.Transparent
                    }
                )
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
            .padding(
                horizontal = 18.dp
            )
            .padding(
                top = 22.dp,
                bottom = 18.dp
            )
    ) {
        SeasonSelector(
            seasons =
                seasons,

            selectedSeasonIndex =
                selectedSeasonIndex,

            onSeasonSelected =
                onSeasonSelected
        )
    }
}

@Composable
private fun SeasonSelector(
    seasons: List<SeasonUi>,
    selectedSeasonIndex: Int,
    onSeasonSelected: (Int) -> Unit
) {
    var expanded by remember {
        mutableStateOf(
            false
        )
    }

    Box {
        Surface(
            shape =
                RoundedCornerShape(
                    26.dp
                ),

            color =
                LaranjadaSurfaceLight,

            modifier =
                Modifier.clickable {
                    expanded =
                        true
                }
        ) {
            Row(
                modifier =
                    Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 11.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        "Temporada ${
                            seasons
                                .getOrNull(
                                    selectedSeasonIndex
                                )
                                ?.number
                                ?: 1
                        }",

                    color =
                        LaranjadaText,

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Medium
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Icon(
                    imageVector =
                        Icons.Rounded
                            .KeyboardArrowDown,

                    contentDescription =
                        null,

                    tint =
                        LaranjadaText,

                    modifier =
                        Modifier.size(
                            25.dp
                        )
                )
            }
        }

        DropdownMenu(
            expanded =
                expanded,

            onDismissRequest = {
                expanded =
                    false
            },

            modifier =
                Modifier.background(
                    LaranjadaSurface
                )
        ) {
            seasons.forEachIndexed {
                    index,
                    season ->

                DropdownMenuItem(
                    text = {
                        Text(
                            text =
                                "Temporada ${season.number}",

                            color =
                                LaranjadaText
                        )
                    },

                    onClick = {
                        onSeasonSelected(
                            index
                        )

                        expanded =
                            false
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
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val hasVideo =
        episode.hlsUrl
            .isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource =
                    interactionSource,

                indication =
                    null,

                enabled =
                    hasVideo,

                onClick =
                    onClick
            )
            .padding(
                horizontal = 18.dp
            )
            .padding(
                bottom = 26.dp
            )
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(
                        136.dp
                    )
                    .aspectRatio(
                        16f / 9f
                    )
                    .clip(
                        RoundedCornerShape(
                            8.dp
                        )
                    )
                    .background(
                        LaranjadaSurfaceLight
                    )
            ) {
                AsyncImage(
                    model =
                        episode.imageUrl,

                    contentDescription =
                        episode.title,

                    modifier =
                        Modifier.matchParentSize(),

                    contentScale =
                        ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .size(
                            38.dp
                        )
                        .align(
                            Alignment.Center
                        ),

                    shape =
                        CircleShape,

                    color =
                        if (
                            hasVideo
                        ) {
                            Color.White
                        } else {
                            Color.White.copy(
                                alpha = 0.35f
                            )
                        }
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded
                                .PlayArrow,

                        contentDescription =
                            "Assistir episódio",

                        tint =
                            Color.Black,

                        modifier =
                            Modifier.padding(
                                8.dp
                            )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.width(
                        14.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Text(
                    text =
                        "${episode.number}. ${episode.title}",

                    color =
                        if (
                            hasVideo
                        ) {
                            LaranjadaText
                        } else {
                            LaranjadaMutedText
                        },

                    fontSize =
                        16.sp,

                    lineHeight =
                        20.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    maxLines =
                        2,

                    overflow =
                        TextOverflow.Ellipsis
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            7.dp
                        )
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    if (
                        episode.runtime
                            .isNotBlank()
                    ) {
                        Text(
                            text =
                                episode.runtime,

                            color =
                                LaranjadaMutedText,

                            fontSize =
                                13.sp,

                            fontWeight =
                                FontWeight.Medium
                        )

                        Spacer(
                            modifier =
                                Modifier.width(
                                    8.dp
                                )
                        )
                    }

                    RatingBadge(
                        text =
                            episode.rating
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        Text(
            text =
                episode.synopsis,

            color =
                if (
                    hasVideo
                ) {
                    LaranjadaText
                } else {
                    LaranjadaMutedText
                },

            fontSize =
                14.sp,

            lineHeight =
                21.sp,

            fontWeight =
                FontWeight.Normal
        )
    }
}

@Composable
private fun RatingBadge(
    text: String
) {
    if (
        text.isBlank()
    ) {
        return
    }

    Surface(
        shape =
            RoundedCornerShape(
                5.dp
            ),

        color =
            LaranjadaSurfaceLight
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically,

            modifier =
                Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 5.dp
                )
        ) {
            Text(
                text =
                    text,

                color =
                    Color.White,

                fontSize =
                    13.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.width(
                        4.dp
                    )
            )

            Icon(
                imageVector =
                    Icons.Rounded.Star,

                contentDescription =
                    "Avaliação",

                tint =
                    Color(
                        0xFFFFC107
                    ),

                modifier =
                    Modifier.size(
                        14.dp
                    )
            )
        }
    }
}

@Composable
private fun RelatedSuggestionsSection(
    relatedItems: List<MediaItemUi>,
    isLoading: Boolean,
    errorMessage: String?,
    onItemClick: (MediaItemUi) -> Unit,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 22.dp,
                bottom = 10.dp
            )
    ) {
        Text(
            text =
                "Sugestões",

            color =
                LaranjadaText,

            fontSize =
                19.sp,

            fontWeight =
                FontWeight.ExtraBold,

            modifier =
                Modifier.padding(
                    horizontal = 18.dp
                )
        )

        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            120.dp
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color =
                            LaranjadaOrange,

                        modifier =
                            Modifier.size(
                                28.dp
                            ),

                        strokeWidth =
                            2.dp
                    )
                }
            }

            errorMessage != null -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 18.dp
                        ),

                    shape =
                        RoundedCornerShape(
                            14.dp
                        ),

                    color =
                        LaranjadaSurface.copy(
                            alpha = 0.72f
                        ),

                    border =
                        BorderStroke(
                            width =
                                1.dp,

                            color =
                                Color.White.copy(
                                    alpha = 0.05f
                                )
                        )
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                18.dp
                            )
                    ) {
                        Text(
                            text =
                                "Não foi possível carregar sugestões.",

                            color =
                                LaranjadaText,

                            fontSize =
                                14.sp,

                            fontWeight =
                                FontWeight.SemiBold
                        )

                        Text(
                            text =
                                errorMessage,

                            color =
                                LaranjadaMutedText,

                            fontSize =
                                13.sp,

                            lineHeight =
                                18.sp,

                            modifier =
                                Modifier.padding(
                                    top = 6.dp
                                )
                        )

                        Text(
                            text =
                                "Tentar novamente",

                            color =
                                LaranjadaOrange,

                            fontSize =
                                14.sp,

                            fontWeight =
                                FontWeight.Bold,

                            modifier = Modifier
                                .padding(
                                    top = 12.dp
                                )
                                .clickable {
                                    onRetryClick()
                                }
                        )
                    }
                }
            }

            relatedItems.isEmpty() -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 18.dp
                        ),

                    shape =
                        RoundedCornerShape(
                            14.dp
                        ),

                    color =
                        LaranjadaSurface.copy(
                            alpha = 0.72f
                        ),

                    border =
                        BorderStroke(
                            width =
                                1.dp,

                            color =
                                Color.White.copy(
                                    alpha = 0.05f
                                )
                        )
                ) {
                    Text(
                        text =
                            "Nenhuma sugestão encontrada por enquanto.",

                        color =
                            LaranjadaMutedText,

                        fontSize =
                            14.sp,

                        lineHeight =
                            20.sp,

                        modifier =
                            Modifier.padding(
                                18.dp
                            )
                    )
                }
            }

            else -> {
                LazyRow(
                    contentPadding =
                        PaddingValues(
                            horizontal = 18.dp
                        ),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {
                    items(
                        items =
                            relatedItems,

                        key = {
                                item ->

                            "${item.contentType}-${item.uuid}"
                        }
                    ) {
                            item ->

                        RelatedContentCard(
                            item =
                                item,

                            onClick = {
                                onItemClick(
                                    item
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedContentCard(
    item: MediaItemUi,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Column(
        modifier = Modifier
            .width(
                154.dp
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
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(
                    16f / 9f
                )
                .clip(
                    RoundedCornerShape(
                        10.dp
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
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,

                                Color.Black.copy(
                                    alpha = 0.20f
                                )
                            )
                        )
                    )
            )
        }

        Text(
            text =
                item.title,

            color =
                LaranjadaText,

            fontSize =
                12.sp,

            lineHeight =
                15.sp,

            fontWeight =
                FontWeight.SemiBold,

            maxLines =
                2,

            overflow =
                TextOverflow.Ellipsis,

            modifier =
                Modifier.padding(
                    top = 7.dp
                )
        )

        if (
            item.subtitle
                .isNotBlank()
        ) {
            Text(
                text =
                    item.subtitle,

                color =
                    LaranjadaMutedText,

                fontSize =
                    11.sp,

                lineHeight =
                    13.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,

                modifier =
                    Modifier.padding(
                        top = 2.dp
                    )
            )
        }
    }
}

@Composable
private fun DetailInfoSection(
    uiState: DetailUiState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 18.dp
            )
            .padding(
                top = 24.dp,
                bottom = 10.dp
            )
    ) {
        Text(
            text =
                "Detalhes",

            color =
                LaranjadaText,

            fontSize =
                19.sp,

            fontWeight =
                FontWeight.ExtraBold
        )

        Spacer(
            modifier =
                Modifier.height(
                    14.dp
                )
        )

        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    14.dp
                ),

            color =
                LaranjadaSurface.copy(
                    alpha = 0.72f
                ),

            border =
                BorderStroke(
                    width =
                        1.dp,

                    color =
                        Color.White.copy(
                            alpha = 0.05f
                        )
                )
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 14.dp
                    )
            ) {
                DetailInfoRow(
                    label =
                        "Título original",

                    value =
                        uiState.originalTitle
                            .ifBlank {
                                uiState.title
                            }
                )

                DetailInfoRow(
                    label =
                        "Ano",

                    value =
                        if (
                            uiState.endYear != null
                        ) {
                            "${uiState.year} – ${uiState.endYear}"
                        } else {
                            uiState.year
                        }
                )

                DetailInfoRow(
                    label =
                        "Duração",

                    value =
                        uiState.durationInfo
                )

                DetailInfoRow(
                    label =
                        "Nota",

                    value =
                        uiState.rating
                )

                DetailInfoRow(
                    label =
                        "Tipo",

                    value =
                        if (
                            uiState.isSeries
                        ) {
                            "Série"
                        } else {
                            "Filme"
                        }
                )

                DetailInfoRow(
                    label =
                        "Gêneros",

                    value =
                        uiState.genres
                            .joinToString(
                                ", "
                            )
                )
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String
) {
    if (
        value.isBlank()
    ) {
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 7.dp
            ),

        verticalAlignment =
            Alignment.Top
    ) {
        Text(
            text =
                label,

            color =
                LaranjadaMutedText,

            fontSize =
                13.sp,

            lineHeight =
                18.sp,

            fontWeight =
                FontWeight.Medium,

            modifier =
                Modifier.width(
                    116.dp
                )
        )

        Text(
            text =
                value,

            color =
                LaranjadaText,

            fontSize =
                13.sp,

            lineHeight =
                18.sp,

            fontWeight =
                FontWeight.SemiBold,

            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}