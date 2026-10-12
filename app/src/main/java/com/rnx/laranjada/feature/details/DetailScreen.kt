package com.rnx.laranjada.feature.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaSurface
import com.rnx.laranjada.core.design.theme.LaranjadaSurfaceLight
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.feature.details.components.DetailFavoriteActionsRow
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
    relatedItems: List<MediaItemUi> = emptyList(),
    isRelatedLoading: Boolean = false,
    relatedErrorMessage: String? = null,
    profileName: String,
    profileAvatarUrl: String?,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onFavoriteClick: (Boolean) -> Unit = {},
    onRelatedClick: (MediaItemUi) -> Unit = {},
    onRetryRelatedClick: () -> Unit = {},
    onEpisodeClick: (EpisodeUi) -> Unit = {}
) {
    val selectedBottomIndex =
        remember {
            mutableIntStateOf(0)
        }

    var selectedSeasonIndex by
    remember {
        mutableIntStateOf(0)
    }

    var selectedTab by
    remember(
        uiState.uuid,
        uiState.isSeries
    ) {
        mutableStateOf(
            if (uiState.isSeries) {
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

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    LaranjadaBlack
                )
    ) {
        val availableWidth =
            maxWidth

        val availableHeight =
            maxHeight

        val isTablet =
            availableWidth >=
                    600.dp

        val isExpandedTablet =
            availableWidth >=
                    840.dp

        val useTabletPortraitLayout =
            isTablet &&
                    !isExpandedTablet

        val useLandscapeLayout =
            isExpandedTablet

        val contentMaxWidth =
            when {
                isTablet ->
                    760.dp

                else ->
                    availableWidth
            }

        val horizontalPadding =
            if (isTablet) {
                28.dp
            } else {
                18.dp
            }

        val actionMaxWidth =
            if (isTablet) {
                560.dp
            } else {
                availableWidth
            }

        val suggestionCardWidth =
            if (isTablet) {
                210.dp
            } else {
                154.dp
            }

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {
            when {
                /*
                 * TABLET LANDSCAPE
                 *
                 * Mantido exatamente como aprovado.
                 */
                useLandscapeLayout -> {
                    LandscapeDetail(
                        uiState =
                            uiState,

                        selectedTab =
                            selectedTab,

                        selectedSeasonIndex =
                            selectedSeasonIndex,

                        selectedSeason =
                            selectedSeason,

                        relatedItems =
                            relatedItems,

                        isRelatedLoading =
                            isRelatedLoading,

                        relatedErrorMessage =
                            relatedErrorMessage,

                        screenWidth =
                            availableWidth,

                        screenHeight =
                            availableHeight,

                        onBackClick =
                            onBackClick,

                        onPlayClick =
                            onPlayClick,

                        onRestartClick =
                            onRestartClick,

                        onTabSelected = {
                            selectedTab =
                                it
                        },

                        onSeasonSelected = {
                            selectedSeasonIndex =
                                it
                        },

                        onEpisodeClick =
                            onEpisodeClick,

                        onRelatedClick =
                            onRelatedClick,

                        onRetryRelatedClick =
                            onRetryRelatedClick
                    )
                }

                /*
                 * TABLET PORTRAIT
                 */
                useTabletPortraitLayout -> {
                    TabletPortraitDetail(
                        uiState =
                            uiState,

                        selectedTab =
                            selectedTab,

                        selectedSeasonIndex =
                            selectedSeasonIndex,

                        selectedSeason =
                            selectedSeason,

                        relatedItems =
                            relatedItems,

                        isRelatedLoading =
                            isRelatedLoading,

                        relatedErrorMessage =
                            relatedErrorMessage,

                        onBackClick =
                            onBackClick,

                        onPlayClick =
                            onPlayClick,

                        onRestartClick =
                            onRestartClick,

                        onTabSelected = {
                            selectedTab =
                                it
                        },

                        onSeasonSelected = {
                            selectedSeasonIndex =
                                it
                        },

                        onEpisodeClick =
                            onEpisodeClick,

                        onRelatedClick =
                            onRelatedClick,

                        onRetryRelatedClick =
                            onRetryRelatedClick
                    )
                }

                /*
                 * CELULAR
                 */
                else -> {
                    LazyColumn(
                        modifier =
                            Modifier.fillMaxSize(),

                        contentPadding =
                            PaddingValues(
                                bottom =
                                    124.dp
                            )
                    ) {
                        item {
                            DetailHero(
                                uiState =
                                    uiState,

                                heroHeight =
                                    460.dp,

                                isTablet =
                                    false,

                                isExpandedTablet =
                                    false,

                                onBackClick =
                                    onBackClick
                            )
                        }

                        item {
                            CenteredDetailContent(
                                maxWidth =
                                    contentMaxWidth
                            ) {
                                DetailMainInfo(
                                    uiState =
                                        uiState,

                                    isTablet =
                                        false,

                                    horizontalPadding =
                                        horizontalPadding,

                                    actionMaxWidth =
                                        actionMaxWidth,

                                    onPlayClick =
                                        onPlayClick,

                                    onRestartClick =
                                        onRestartClick
                                )
                            }
                        }

                        item {
                            CenteredDetailContent(
                                maxWidth =
                                    contentMaxWidth
                            ) {
                                DetailTabs(
                                    isSeries =
                                        uiState.isSeries,

                                    selectedTab =
                                        selectedTab,

                                    isTablet =
                                        false,

                                    horizontalPadding =
                                        horizontalPadding,

                                    onTabSelected = {
                                        selectedTab =
                                            it
                                    }
                                )
                            }
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
                                                selectedSeasonIndex =
                                                    it
                                            }
                                        )
                                    }

                                    if (
                                        selectedSeason !=
                                        null
                                    ) {
                                        items(
                                            selectedSeason
                                                .episodes
                                        ) {
                                                episode ->

                                            EpisodeCard(
                                                episode =
                                                    episode,

                                                onClick = {
                                                    if (
                                                        episode.hasVideo
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

                                        contentMaxWidth =
                                            contentMaxWidth,

                                        horizontalPadding =
                                            horizontalPadding,

                                        cardWidth =
                                            suggestionCardWidth,

                                        isTablet =
                                            false,

                                        onItemClick =
                                            onRelatedClick,

                                        onRetryClick =
                                            onRetryRelatedClick
                                    )
                                }
                            }

                            DetailTab.Details -> {
                                item {
                                    CenteredDetailContent(
                                        maxWidth =
                                            contentMaxWidth
                                    ) {
                                        DetailInfoSection(
                                            uiState =
                                                uiState,

                                            horizontalPadding =
                                                horizontalPadding,

                                            isTablet =
                                                false
                                        )
                                    }
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
                }
            }

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
                        index ==
                        3
                    ) {
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
}

/*
 * ==========================================================
 * TABLET PORTRAIT
 * ==========================================================
 */

@Composable
private fun TabletPortraitDetail(
    uiState: DetailUiState,
    selectedTab: DetailTab,
    selectedSeasonIndex: Int,
    selectedSeason: SeasonUi?,
    relatedItems: List<MediaItemUi>,
    isRelatedLoading: Boolean,
    relatedErrorMessage: String?,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
    onRestartClick: () -> Unit,
    onTabSelected: (DetailTab) -> Unit,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeClick: (EpisodeUi) -> Unit,
    onRelatedClick: (MediaItemUi) -> Unit,
    onRetryRelatedClick: () -> Unit
) {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                bottom =
                    124.dp
            )
    ) {
        item {
            TabletPortraitBackdrop(
                uiState =
                    uiState,

                selectedTab =
                    selectedTab,

                selectedSeasonIndex =
                    selectedSeasonIndex,

                onBackClick =
                    onBackClick,

                onPlayClick =
                    onPlayClick,

                onRestartClick =
                    onRestartClick,

                onTabSelected =
                    onTabSelected,

                onSeasonSelected =
                    onSeasonSelected
            )
        }

        when (
            selectedTab
        ) {
            DetailTab.Episodes -> {
                if (
                    uiState.isSeries &&
                    selectedSeason !=
                    null
                ) {
                    items(
                        items =
                            selectedSeason.episodes,

                        key = {
                                episode ->

                            episode.uuid
                        }
                    ) {
                            episode ->

                        TabletPortraitEpisodeCard(
                            episode =
                                episode,

                            onClick = {
                                if (
                                    episode.hasVideo
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

            DetailTab.Suggestions -> {
                item {
                    TabletPortraitSuggestionsSection(
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
                    CenteredDetailContent(
                        maxWidth =
                            720.dp
                    ) {
                        DetailInfoSection(
                            uiState =
                                uiState,

                            horizontalPadding =
                                28.dp,

                            isTablet =
                                true
                        )
                    }
                }
            }
        }

        item {
            Spacer(
                modifier =
                    Modifier.height(
                        30.dp
                    )
            )
        }
    }
}

@Composable
private fun TabletPortraitBackdrop(
    uiState: DetailUiState,
    selectedTab: DetailTab,
    selectedSeasonIndex: Int,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
    onRestartClick: () -> Unit,
    onTabSelected: (DetailTab) -> Unit,
    onSeasonSelected: (Int) -> Unit
) {
    /*
     * [ALTERAÇÃO]
     *
     * Antes:
     *
     * .heightIn(min = 720/760dp)
     *
     * Isso obrigava a imagem a continuar
     * mesmo depois do seletor de temporada,
     * criando o buraco enorme antes dos
     * episódios.
     *
     * Agora não existe altura fixa nem mínima.
     *
     * A própria Column abaixo determina a altura.
     * A imagem acompanha exatamente o conteúdo.
     */
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
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
                ContentScale.Crop,

            alignment =
                Alignment.Center
        )

        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops =
                                arrayOf(
                                    0.00f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.62f
                                            ),

                                    0.48f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.30f
                                            ),

                                    1.00f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.62f
                                            )
                                )
                        )
                    )
        )

        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0.00f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.12f
                                            ),

                                    0.30f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.30f
                                            ),

                                    0.54f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.54f
                                            ),

                                    0.78f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.86f
                                            ),

                                    0.96f to
                                            LaranjadaBlack,

                                    1.00f to
                                            LaranjadaBlack
                                )
                        )
                    )
        )

        Column(
            modifier =
                Modifier.fillMaxWidth()
        ) {
            DetailTopBar(
                title =
                    uiState.title,

                isTablet =
                    true,

                onBackClick =
                    onBackClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        70.dp
                    )
            )

            TabletPortraitHeroContent(
                uiState =
                    uiState,

                onPlayClick =
                    onPlayClick,

                onRestartClick =
                    onRestartClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        26.dp
                    )
            )

            DetailTabs(
                isSeries =
                    uiState.isSeries,

                selectedTab =
                    selectedTab,

                isTablet =
                    true,

                horizontalPadding =
                    28.dp,

                onTabSelected =
                    onTabSelected
            )

            if (
                uiState.isSeries &&
                selectedTab ==
                DetailTab.Episodes
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start =
                                    28.dp,

                                end =
                                    28.dp,

                                top =
                                    20.dp,

                                bottom =
                                    16.dp
                            )
                ) {
                    SeasonSelector(
                        seasons =
                            uiState.seasons,

                        selectedSeasonIndex =
                            selectedSeasonIndex,

                        onSeasonSelected =
                            onSeasonSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletPortraitHeroContent(
    uiState: DetailUiState,
    onPlayClick: () -> Unit,
    onRestartClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        28.dp
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
                34.sp,

            lineHeight =
                38.sp,

            fontWeight =
                FontWeight.ExtraBold,

            textAlign =
                TextAlign.Center,

            maxLines =
                2,

            overflow =
                TextOverflow.Ellipsis,

            modifier =
                Modifier.widthIn(
                    max =
                        620.dp
                )
        )

        Spacer(
            modifier =
                Modifier.height(
                    11.dp
                )
        )

        Box(
            modifier =
                Modifier.widthIn(
                    max =
                        620.dp
                )
        ) {
            MetadataLine(
                uiState =
                    uiState,

                alignStart =
                    false
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )

        RatingWithStar(
            rating =
                uiState.rating,

            isTablet =
                true
        )

        if (
            uiState.hasWatchProgress
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        17.dp
                    )
            )

            TabletPortraitProgressPanel(
                uiState =
                    uiState
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )

        Button(
            onClick =
                onPlayClick,

            enabled =
                uiState.canPlay,

            modifier =
                Modifier
                    .widthIn(
                        max =
                            540.dp
                    )
                    .fillMaxWidth()
                    .height(
                        58.dp
                    ),

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
                                alpha =
                                    0.28f
                            ),

                        disabledContentColor =
                            Color.Black.copy(
                                alpha =
                                    0.55f
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
                        9.dp
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
                    20.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                letterSpacing =
                    3.sp
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    13.dp
                )
        )

        DetailFavoriteActionsRow(
            contentType =
                uiState.contentTypeForApi,

            contentUuid =
                uiState.uuid,

            hasWatchProgress =
                uiState.hasWatchProgress,

            onRestartClick =
                onRestartClick
        )

        Spacer(
            modifier =
                Modifier.height(
                    22.dp
                )
        )

        if (
            uiState.synopsis
                .isNotBlank()
        ) {
            Text(
                text =
                    uiState.synopsis,

                color =
                    LaranjadaText,

                fontSize =
                    16.sp,

                lineHeight =
                    24.sp,

                fontWeight =
                    FontWeight.Normal,

                textAlign =
                    TextAlign.Start,

                modifier =
                    Modifier
                        .widthIn(
                            max =
                                620.dp
                        )
                        .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TabletPortraitProgressPanel(
    uiState: DetailUiState
) {
    val watchProgress =
        uiState.watchProgress
            ?: return

    Column(
        modifier =
            Modifier
                .widthIn(
                    max =
                        520.dp
                )
                .fillMaxWidth(),

        horizontalAlignment =
            Alignment.Start
    ) {
        Text(
            text =
                "CONTINUE ASSISTINDO",

            color =
                Color.White.copy(
                    alpha =
                        0.76f
                ),

            fontSize =
                11.sp,

            fontWeight =
                FontWeight.ExtraBold,

            letterSpacing =
                1.8.sp
        )

        Spacer(
            modifier =
                Modifier.height(
                    6.dp
                )
        )

        Text(
            text =
                if (
                    uiState.isSeries &&
                    watchProgress.hasEpisodeInfo
                ) {
                    "T${watchProgress.seasonNumber}:" +
                            "E${watchProgress.episodeNumber} " +
                            "${watchProgress.episodeTitle}"
                } else {
                    uiState.title
                },

            color =
                LaranjadaText,

            fontSize =
                20.sp,

            lineHeight =
                24.sp,

            fontWeight =
                FontWeight.ExtraBold,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )

        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        WatchProgressBar(
            watchProgress =
                watchProgress,

            modifier =
                Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TabletPortraitEpisodeCard(
    episode: EpisodeUi,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val hasVideo =
        episode.hasVideo

    Column(
        modifier =
            Modifier
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
                    horizontal =
                        28.dp,

                    vertical =
                        14.dp
                )
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .width(
                            176.dp
                        )
                        .aspectRatio(
                            16f /
                                    9f
                        )
                        .clip(
                            RoundedCornerShape(
                                9.dp
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
                    modifier =
                        Modifier
                            .size(
                                42.dp
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
                                alpha =
                                    0.35f
                            )
                        }
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.PlayArrow,

                        contentDescription =
                            "Assistir episódio",

                        tint =
                            Color.Black,

                        modifier =
                            Modifier.padding(
                                9.dp
                            )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.width(
                        16.dp
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
                        17.sp,

                    lineHeight =
                        21.sp,

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
                            8.dp
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
                                    9.dp
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

        if (
            episode.synopsis
                .isNotBlank()
        ) {
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

                maxLines =
                    3,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TabletPortraitSuggestionsSection(
    relatedItems: List<MediaItemUi>,
    isLoading: Boolean,
    errorMessage: String?,
    onItemClick: (MediaItemUi) -> Unit,
    onRetryClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        28.dp,

                    vertical =
                        24.dp
                )
    ) {
        when {
            isLoading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                150.dp
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color =
                            LaranjadaOrange,

                        modifier =
                            Modifier.size(
                                30.dp
                            ),

                        strokeWidth =
                            2.dp
                    )
                }
            }

            errorMessage !=
                    null -> {

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            14.dp
                        ),

                    color =
                        LaranjadaSurface.copy(
                            alpha =
                                0.72f
                        ),

                    border =
                        BorderStroke(
                            width =
                                1.dp,

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.05f
                                )
                        )
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                20.dp
                            )
                    ) {
                        Text(
                            text =
                                "Não foi possível carregar sugestões.",

                            color =
                                LaranjadaText,

                            fontSize =
                                15.sp,

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
                                19.sp,

                            modifier =
                                Modifier.padding(
                                    top =
                                        7.dp
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

                            modifier =
                                Modifier
                                    .padding(
                                        top =
                                            14.dp
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
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            14.dp
                        ),

                    color =
                        LaranjadaSurface.copy(
                            alpha =
                                0.72f
                        ),

                    border =
                        BorderStroke(
                            width =
                                1.dp,

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.05f
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
                                20.dp
                            )
                    )
                }
            }

            else -> {
                relatedItems
                    .chunked(
                        2
                    )
                    .forEach {
                            rowItems ->

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    16.dp
                                ),

                            verticalAlignment =
                                Alignment.Top
                        ) {
                            rowItems.forEach {
                                    item ->

                                TabletPortraitRelatedCard(
                                    item =
                                        item,

                                    modifier =
                                        Modifier.weight(
                                            1f
                                        ),

                                    onClick = {
                                        onItemClick(
                                            item
                                        )
                                    }
                                )
                            }

                            if (
                                rowItems.size ==
                                1
                            ) {
                                Spacer(
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(
                                    20.dp
                                )
                        )
                    }
            }
        }
    }
}

@Composable
private fun TabletPortraitRelatedCard(
    item: MediaItemUi,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Column(
        modifier =
            modifier
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
                        16f /
                                9f
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
                                                0.22f
                                        )
                                    )
                            )
                        )
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

        Text(
            text =
                item.title,

            color =
                LaranjadaText,

            fontSize =
                15.sp,

            lineHeight =
                19.sp,

            fontWeight =
                FontWeight.SemiBold,

            maxLines =
                2,

            overflow =
                TextOverflow.Ellipsis
        )

        if (
            item.subtitle
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
                    item.subtitle,

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

/*
 * ==========================================================
 * TABLET LANDSCAPE
 * ==========================================================
 */

@Composable
private fun LandscapeDetail(
    uiState: DetailUiState,
    selectedTab: DetailTab,
    selectedSeasonIndex: Int,
    selectedSeason: SeasonUi?,
    relatedItems: List<MediaItemUi>,
    isRelatedLoading: Boolean,
    relatedErrorMessage: String?,
    screenWidth: Dp,
    screenHeight: Dp,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
    onRestartClick: () -> Unit,
    onTabSelected: (DetailTab) -> Unit,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeClick: (EpisodeUi) -> Unit,
    onRelatedClick: (MediaItemUi) -> Unit,
    onRetryRelatedClick: () -> Unit
) {
    val backgroundMinHeight =
        maxOf(
            screenHeight *
                    1.20f,

            if (
                uiState.isSeries
            ) {
                780.dp
            } else {
                740.dp
            }
        )

    val episodeCardWidth =
        when {
            screenWidth >=
                    1200.dp ->
                205.dp

            screenWidth >=
                    1000.dp ->
                195.dp

            else ->
                185.dp
        }

    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                bottom =
                    124.dp
            )
    ) {
        item {
            LandscapeBackdrop(
                uiState =
                    uiState,

                selectedTab =
                    selectedTab,

                selectedSeasonIndex =
                    selectedSeasonIndex,

                selectedSeason =
                    selectedSeason,

                relatedItems =
                    relatedItems,

                isRelatedLoading =
                    isRelatedLoading,

                relatedErrorMessage =
                    relatedErrorMessage,

                backgroundMinHeight =
                    backgroundMinHeight,

                screenWidth =
                    screenWidth,

                episodeCardWidth =
                    episodeCardWidth,

                onBackClick =
                    onBackClick,

                onPlayClick =
                    onPlayClick,

                onRestartClick =
                    onRestartClick,

                onTabSelected =
                    onTabSelected,

                onSeasonSelected =
                    onSeasonSelected,

                onEpisodeClick =
                    onEpisodeClick,

                onRelatedClick =
                    onRelatedClick,

                onRetryRelatedClick =
                    onRetryRelatedClick
            )
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
}

@Composable
private fun LandscapeBackdrop(
    uiState: DetailUiState,
    selectedTab: DetailTab,
    selectedSeasonIndex: Int,
    selectedSeason: SeasonUi?,
    relatedItems: List<MediaItemUi>,
    isRelatedLoading: Boolean,
    relatedErrorMessage: String?,
    backgroundMinHeight: Dp,
    screenWidth: Dp,
    episodeCardWidth: Dp,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
    onRestartClick: () -> Unit,
    onTabSelected: (DetailTab) -> Unit,
    onSeasonSelected: (Int) -> Unit,
    onEpisodeClick: (EpisodeUi) -> Unit,
    onRelatedClick: (MediaItemUi) -> Unit,
    onRetryRelatedClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min =
                        backgroundMinHeight
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
                ContentScale.FillBounds
        )

        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops =
                                arrayOf(
                                    0.00f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.76f
                                            ),

                                    0.42f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.30f
                                            ),

                                    1.00f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.74f
                                            )
                                )
                        )
                    )
        )

        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0.00f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.18f
                                            ),

                                    0.28f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.28f
                                            ),

                                    0.54f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.48f
                                            ),

                                    0.76f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.82f
                                            ),

                                    0.96f to
                                            LaranjadaBlack,

                                    1.00f to
                                            LaranjadaBlack
                                )
                        )
                    )
        )

        Column(
            modifier =
                Modifier.fillMaxWidth()
        ) {
            DetailTopBar(
                title =
                    uiState.title,

                isTablet =
                    true,

                onBackClick =
                    onBackClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        26.dp
                    )
            )

            LandscapeHeroContent(
                uiState =
                    uiState,

                onPlayClick =
                    onPlayClick,

                onRestartClick =
                    onRestartClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        28.dp
                    )
            )

            DetailTabs(
                isSeries =
                    uiState.isSeries,

                selectedTab =
                    selectedTab,

                isTablet =
                    true,

                horizontalPadding =
                    40.dp,

                onTabSelected =
                    onTabSelected
            )

            when (
                selectedTab
            ) {
                DetailTab.Episodes -> {
                    if (
                        uiState.isSeries
                    ) {
                        SeriesLandscapeSeasonHeader(
                            seasons =
                                uiState.seasons,

                            selectedSeasonIndex =
                                selectedSeasonIndex,

                            onSeasonSelected =
                                onSeasonSelected
                        )

                        if (
                            selectedSeason !=
                            null
                        ) {
                            LandscapeEpisodesCarousel(
                                episodes =
                                    selectedSeason.episodes,

                                cardWidth =
                                    episodeCardWidth,

                                onEpisodeClick =
                                    onEpisodeClick
                            )
                        }
                    }
                }

                DetailTab.Suggestions -> {
                    RelatedSuggestionsSection(
                        relatedItems =
                            relatedItems,

                        isLoading =
                            isRelatedLoading,

                        errorMessage =
                            relatedErrorMessage,

                        contentMaxWidth =
                            screenWidth,

                        horizontalPadding =
                            40.dp,

                        cardWidth =
                            if (
                                uiState.isSeries
                            ) {
                                210.dp
                            } else {
                                215.dp
                            },

                        isTablet =
                            true,

                        onItemClick =
                            onRelatedClick,

                        onRetryClick =
                            onRetryRelatedClick
                    )
                }

                DetailTab.Details -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal =
                                        40.dp
                                )
                    ) {
                        DetailInfoSection(
                            uiState =
                                uiState,

                            horizontalPadding =
                                0.dp,

                            isTablet =
                                true
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        38.dp
                    )
            )
        }
    }
}

@Composable
private fun LandscapeHeroContent(
    uiState: DetailUiState,
    onPlayClick: () -> Unit,
    onRestartClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .widthIn(
                    max =
                        620.dp
                )
                .fillMaxWidth()
                .padding(
                    start =
                        40.dp,

                    end =
                        24.dp
                ),

        horizontalAlignment =
            Alignment.Start
    ) {
        Text(
            text =
                uiState.title,

            color =
                LaranjadaText,

            fontSize =
                40.sp,

            lineHeight =
                44.sp,

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
                    9.dp
                )
        )

        MetadataLine(
            uiState =
                uiState,

            alignStart =
                true
        )

        Spacer(
            modifier =
                Modifier.height(
                    17.dp
                )
        )

        RatingWithStar(
            rating =
                uiState.rating,

            isTablet =
                true
        )

        if (
            uiState.hasWatchProgress
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        13.dp
                    )
            )

            Text(
                text =
                    "CONTINUE ASSISTINDO",

                color =
                    LaranjadaMutedText,

                fontSize =
                    11.sp,

                letterSpacing =
                    1.7.sp,

                fontWeight =
                    FontWeight.ExtraBold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )

            Text(
                text =
                    if (
                        uiState.isSeries &&
                        uiState.watchProgress
                            ?.hasEpisodeInfo ==
                        true
                    ) {
                        "T${uiState.watchProgress.seasonNumber}:" +
                                "E${uiState.watchProgress.episodeNumber} " +
                                "${uiState.watchProgress.episodeTitle}"
                    } else {
                        uiState.title
                    },

                color =
                    LaranjadaText,

                fontSize =
                    21.sp,

                lineHeight =
                    25.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            WatchProgressBar(
                watchProgress =
                    uiState.watchProgress,

                modifier =
                    Modifier.widthIn(
                        max =
                            470.dp
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    17.dp
                )
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {
            Button(
                onClick =
                    onPlayClick,

                modifier =
                    Modifier
                        .width(
                            245.dp
                        )
                        .height(
                            54.dp
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
                                    alpha =
                                        0.28f
                                ),

                            disabledContentColor =
                                Color.Black.copy(
                                    alpha =
                                        0.55f
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
                            28.dp
                        )
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            7.dp
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
                        18.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    letterSpacing =
                        2.5.sp
                )
            }

            DetailFavoriteActionsRow(
                contentType =
                    uiState.contentTypeForApi,

                contentUuid =
                    uiState.uuid,

                hasWatchProgress =
                    uiState.hasWatchProgress,

                onRestartClick =
                    onRestartClick
            )
        }

        if (
            uiState.synopsis
                .isNotBlank()
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )

            Text(
                text =
                    uiState.synopsis,

                color =
                    LaranjadaText,

                fontSize =
                    14.sp,

                lineHeight =
                    20.sp,

                fontWeight =
                    FontWeight.Normal,

                maxLines =
                    3,

                overflow =
                    TextOverflow.Ellipsis,

                modifier =
                    Modifier.widthIn(
                        max =
                            570.dp
                    )
            )
        }
    }
}

@Composable
private fun SeriesLandscapeSeasonHeader(
    seasons: List<SeasonUi>,
    selectedSeasonIndex: Int,
    onSeasonSelected: (Int) -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start =
                        40.dp,

                    end =
                        40.dp,

                    top =
                        20.dp,

                    bottom =
                        14.dp
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
private fun LandscapeEpisodesCarousel(
    episodes: List<EpisodeUi>,
    cardWidth: Dp,
    onEpisodeClick: (EpisodeUi) -> Unit
) {
    LazyRow(
        contentPadding =
            PaddingValues(
                start =
                    40.dp,

                end =
                    40.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                16.dp
            )
    ) {
        items(
            items =
                episodes,

            key = {
                    episode ->

                episode.uuid
            }
        ) {
                episode ->

            LandscapeEpisodeCard(
                episode =
                    episode,

                cardWidth =
                    cardWidth,

                onClick = {
                    if (
                        episode.hasVideo
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

@Composable
private fun LandscapeEpisodeCard(
    episode: EpisodeUi,
    cardWidth: Dp,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val hasVideo =
        episode.hasVideo

    Column(
        modifier =
            Modifier
                .width(
                    cardWidth
                )
                .clickable(
                    enabled =
                        hasVideo,

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
                        16f /
                                9f
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
                                                0.26f
                                        )
                                    )
                            )
                        )
            )

            Surface(
                modifier =
                    Modifier
                        .size(
                            42.dp
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
                            alpha =
                                0.35f
                        )
                    }
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.PlayArrow,

                    contentDescription =
                        "Assistir episódio",

                    tint =
                        Color.Black,

                    modifier =
                        Modifier.padding(
                            9.dp
                        )
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

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
                14.sp,

            lineHeight =
                18.sp,

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
                    6.dp
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
                        12.sp,

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

        if (
            episode.synopsis
                .isNotBlank()
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
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
                    12.sp,

                lineHeight =
                    17.sp,

                maxLines =
                    3,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CenteredDetailContent(
    maxWidth: Dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier =
            Modifier.fillMaxWidth(),

        contentAlignment =
            Alignment.TopCenter
    ) {
        Box(
            modifier =
                Modifier
                    .widthIn(
                        max =
                            maxWidth
                    )
                    .fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
private fun DetailHero(
    uiState: DetailUiState,
    heroHeight: Dp,
    isTablet: Boolean,
    isExpandedTablet: Boolean,
    onBackClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    heroHeight
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

        if (
            isTablet
        ) {
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colorStops =
                                    arrayOf(
                                        0.00f to
                                                Color.Black.copy(
                                                    alpha =
                                                        0.52f
                                                ),

                                        0.32f to
                                                Color.Black.copy(
                                                    alpha =
                                                        0.20f
                                                ),

                                        0.68f to
                                                Color.Transparent,

                                        1.00f to
                                                Color.Transparent
                                    )
                            )
                        )
            )
        }

        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0.00f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.42f
                                            ),

                                    0.18f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.12f
                                            ),

                                    0.62f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.34f
                                            ),

                                    0.84f to
                                            Color.Black.copy(
                                                alpha =
                                                    0.82f
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

            isTablet =
                isTablet,

            onBackClick =
                onBackClick
        )

        Column(
            modifier =
                Modifier
                    .align(
                        if (
                            isTablet
                        ) {
                            Alignment.BottomStart
                        } else {
                            Alignment.BottomCenter
                        }
                    )
                    .widthIn(
                        max =
                            when {
                                isExpandedTablet ->
                                    760.dp

                                isTablet ->
                                    620.dp

                                else ->
                                    520.dp
                            }
                    )
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            when {
                                isExpandedTablet ->
                                    40.dp

                                isTablet ->
                                    28.dp

                                else ->
                                    24.dp
                            }
                    )
                    .padding(
                        bottom =
                            if (
                                isTablet
                            ) {
                                24.dp
                            } else {
                                16.dp
                            }
                    ),

            horizontalAlignment =
                if (
                    isTablet
                ) {
                    Alignment.Start
                } else {
                    Alignment.CenterHorizontally
                }
        ) {
            Text(
                text =
                    uiState.title,

                color =
                    LaranjadaText,

                fontSize =
                    when {
                        isExpandedTablet ->
                            38.sp

                        isTablet ->
                            34.sp

                        else ->
                            29.sp
                    },

                lineHeight =
                    when {
                        isExpandedTablet ->
                            42.sp

                        isTablet ->
                            38.sp

                        else ->
                            33.sp
                    },

                fontWeight =
                    FontWeight.ExtraBold,

                textAlign =
                    if (
                        isTablet
                    ) {
                        TextAlign.Start
                    } else {
                        TextAlign.Center
                    },

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
                    uiState,

                alignStart =
                    isTablet
            )
        }
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    isTablet: Boolean,
    onBackClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .padding(
                    start =
                        if (
                            isTablet
                        ) {
                            20.dp
                        } else {
                            6.dp
                        },

                    end =
                        if (
                            isTablet
                        ) {
                            20.dp
                        } else {
                            14.dp
                        },

                    top =
                        8.dp,

                    bottom =
                        8.dp
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
                    Icons.AutoMirrored
                        .Rounded
                        .ArrowBack,

                contentDescription =
                    "Voltar",

                tint =
                    LaranjadaText,

                modifier =
                    Modifier.size(
                        if (
                            isTablet
                        ) {
                            29.dp
                        } else {
                            27.dp
                        }
                    )
            )
        }

        Text(
            text =
                title,

            color =
                LaranjadaText,

            fontSize =
                if (
                    isTablet
                ) {
                    19.sp
                } else {
                    18.sp
                },

            lineHeight =
                if (
                    isTablet
                ) {
                    22.sp
                } else {
                    20.sp
                },

            fontWeight =
                FontWeight.Bold,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis,

            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .padding(
                        end =
                            8.dp
                    )
        )
    }
}

@Composable
private fun MetadataLine(
    uiState: DetailUiState,
    alignStart: Boolean
) {
    val years =
        if (
            uiState.endYear !=
            null
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
            if (
                alignStart
            ) {
                Arrangement.Start
            } else {
                Arrangement.Center
            },

        verticalAlignment =
            Alignment.CenterVertically,

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        if (
                            alignStart
                        ) {
                            0.dp
                        } else {
                            10.dp
                        }
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
    isTablet: Boolean,
    horizontalPadding: Dp,
    actionMaxWidth: Dp,
    onPlayClick: () -> Unit,
    onRestartClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        horizontalPadding
                )
                .padding(
                    top =
                        if (
                            isTablet
                        ) {
                            10.dp
                        } else {
                            4.dp
                        }
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        RatingWithStar(
            rating =
                uiState.rating,

            isTablet =
                isTablet
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
                    uiState.watchProgress,

                modifier =
                    Modifier
                        .widthIn(
                            max =
                                actionMaxWidth
                        )
                        .fillMaxWidth()
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    if (
                        isTablet
                    ) {
                        18.dp
                    } else {
                        14.dp
                    }
                )
        )

        Button(
            onClick =
                onPlayClick,

            modifier =
                Modifier
                    .widthIn(
                        max =
                            actionMaxWidth
                    )
                    .fillMaxWidth()
                    .height(
                        if (
                            isTablet
                        ) {
                            60.dp
                        } else {
                            56.dp
                        }
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
                                alpha =
                                    0.28f
                            ),

                        disabledContentColor =
                            Color.Black.copy(
                                alpha =
                                    0.55f
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
                        if (
                            isTablet
                        ) {
                            32.dp
                        } else {
                            30.dp
                        }
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
                    if (
                        isTablet
                    ) {
                        22.sp
                    } else {
                        21.sp
                    },

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

        DetailFavoriteActionsRow(
            contentType =
                uiState.contentTypeForApi,

            contentUuid =
                uiState.uuid,

            hasWatchProgress =
                uiState.hasWatchProgress,

            onRestartClick =
                onRestartClick
        )

        if (
            uiState.isSeries &&
            uiState.watchProgress
                ?.hasEpisodeInfo ==
            true
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
                    if (
                        isTablet
                    ) {
                        24.dp
                    } else {
                        20.dp
                    }
                )
        )

        Text(
            text =
                uiState.synopsis,

            color =
                LaranjadaText,

            fontSize =
                if (
                    isTablet
                ) {
                    17.sp
                } else {
                    16.sp
                },

            lineHeight =
                if (
                    isTablet
                ) {
                    25.sp
                } else {
                    23.sp
                },

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(
                    if (
                        isTablet
                    ) {
                        30.dp
                    } else {
                        26.dp
                    }
                )
        )
    }
}

@Composable
private fun RatingWithStar(
    rating: String,
    isTablet: Boolean
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
                if (
                    isTablet
                ) {
                    17.sp
                } else {
                    16.sp
                },

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
                    if (
                        isTablet
                    ) {
                        20.dp
                    } else {
                        19.dp
                    }
                )
        )
    }
}

@Composable
private fun WatchProgressBar(
    watchProgress: WatchProgressUi?,
    modifier: Modifier = Modifier
) {
    if (
        watchProgress ==
        null
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
            modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
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
                modifier =
                    Modifier
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
                watchProgress.remainingText,

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
private fun ContinueEpisodeInfo(
    watchProgress: WatchProgressUi
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Text(
            text =
                "T${watchProgress.seasonNumber}:" +
                        "E${watchProgress.episodeNumber} " +
                        "${watchProgress.episodeTitle}",

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
    isTablet: Boolean,
    horizontalPadding: Dp,
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

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        horizontalPadding
                )
    ) {
        HorizontalDivider(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(
                        Alignment.BottomStart
                    ),

            color =
                Color.White.copy(
                    alpha =
                        0.16f
                ),

            thickness =
                1.dp
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        scrollState
                    ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    if (
                        isTablet
                    ) {
                        48.dp
                    } else {
                        34.dp
                    }
                ),

            verticalAlignment =
                Alignment.Bottom
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

                    isTablet =
                        isTablet,

                    onClick = {
                        onTabSelected(
                            tab
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailTabItem(
    label: String,
    selected: Boolean,
    isTablet: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val tabWidth =
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
                96.dp
        }

    Column(
        modifier =
            Modifier
                .width(
                    tabWidth
                )
                .clickable(
                    interactionSource =
                        interactionSource,

                    indication =
                        null,

                    onClick =
                        onClick
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
                if (
                    isTablet
                ) {
                    17.sp
                } else {
                    16.sp
                },

            fontWeight =
                FontWeight.ExtraBold,

            letterSpacing =
                2.6.sp,

            maxLines =
                1,

            softWrap =
                false,

            overflow =
                TextOverflow.Visible,

            modifier =
                Modifier.padding(
                    bottom =
                        12.dp
                )
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        3.dp
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
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        18.dp
                )
                .padding(
                    top =
                        22.dp,

                    bottom =
                        18.dp
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
    var expanded by
    remember {
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
                        horizontal =
                            18.dp,

                        vertical =
                            11.dp
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
                        Icons.Rounded.KeyboardArrowDown,

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
        episode.hasVideo

    Column(
        modifier =
            Modifier
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
                    horizontal =
                        18.dp
                )
                .padding(
                    bottom =
                        26.dp
                )
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .width(
                            136.dp
                        )
                        .aspectRatio(
                            16f /
                                    9f
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
                    modifier =
                        Modifier
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
                                alpha =
                                    0.35f
                            )
                        }
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.PlayArrow,

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
                21.sp
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
                    horizontal =
                        8.dp,

                    vertical =
                        5.dp
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
    contentMaxWidth: Dp,
    horizontalPadding: Dp,
    cardWidth: Dp,
    isTablet: Boolean,
    onItemClick: (MediaItemUi) -> Unit,
    onRetryClick: () -> Unit
) {
    CenteredDetailContent(
        maxWidth =
            contentMaxWidth
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            if (
                                isTablet
                            ) {
                                28.dp
                            } else {
                                22.dp
                            },

                        bottom =
                            10.dp
                    )
        ) {
            Text(
                text =
                    "Sugestões",

                color =
                    LaranjadaText,

                fontSize =
                    if (
                        isTablet
                    ) {
                        22.sp
                    } else {
                        19.sp
                    },

                fontWeight =
                    FontWeight.ExtraBold,

                modifier =
                    Modifier.padding(
                        horizontal =
                            horizontalPadding
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        if (
                            isTablet
                        ) {
                            16.dp
                        } else {
                            12.dp
                        }
                    )
            )

            when {
                isLoading -> {
                    Box(
                        modifier =
                            Modifier
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

                errorMessage !=
                        null -> {

                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal =
                                        horizontalPadding
                                ),

                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),

                        color =
                            LaranjadaSurface.copy(
                                alpha =
                                    0.72f
                            ),

                        border =
                            BorderStroke(
                                width =
                                    1.dp,

                                color =
                                    Color.White.copy(
                                        alpha =
                                            0.05f
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
                                        top =
                                            6.dp
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

                                modifier =
                                    Modifier
                                        .padding(
                                            top =
                                                12.dp
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
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal =
                                        horizontalPadding
                                ),

                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),

                        color =
                            LaranjadaSurface.copy(
                                alpha =
                                    0.72f
                            ),

                        border =
                            BorderStroke(
                                width =
                                    1.dp,

                                color =
                                    Color.White.copy(
                                        alpha =
                                            0.05f
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
                                horizontal =
                                    horizontalPadding
                            ),

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                if (
                                    isTablet
                                ) {
                                    16.dp
                                } else {
                                    12.dp
                                }
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

                                cardWidth =
                                    cardWidth,

                                isTablet =
                                    isTablet,

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
}

@Composable
private fun RelatedContentCard(
    item: MediaItemUi,
    cardWidth: Dp,
    isTablet: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Column(
        modifier =
            Modifier
                .width(
                    cardWidth
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
                        16f /
                                9f
                    )
                    .clip(
                        RoundedCornerShape(
                            if (
                                isTablet
                            ) {
                                12.dp
                            } else {
                                10.dp
                            }
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
                                                0.20f
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
                if (
                    isTablet
                ) {
                    14.sp
                } else {
                    12.sp
                },

            lineHeight =
                if (
                    isTablet
                ) {
                    18.sp
                } else {
                    15.sp
                },

            fontWeight =
                FontWeight.SemiBold,

            maxLines =
                2,

            overflow =
                TextOverflow.Ellipsis,

            modifier =
                Modifier.padding(
                    top =
                        7.dp
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
                    if (
                        isTablet
                    ) {
                        12.sp
                    } else {
                        11.sp
                    },

                lineHeight =
                    if (
                        isTablet
                    ) {
                        15.sp
                    } else {
                        13.sp
                    },

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,

                modifier =
                    Modifier.padding(
                        top =
                            2.dp
                    )
            )
        }
    }
}

@Composable
private fun DetailInfoSection(
    uiState: DetailUiState,
    horizontalPadding: Dp,
    isTablet: Boolean
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        horizontalPadding
                )
                .padding(
                    top =
                        if (
                            isTablet
                        ) {
                            28.dp
                        } else {
                            24.dp
                        },

                    bottom =
                        10.dp
                )
    ) {
        Text(
            text =
                "Detalhes",

            color =
                LaranjadaText,

            fontSize =
                if (
                    isTablet
                ) {
                    22.sp
                } else {
                    19.sp
                },

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
                    alpha =
                        0.72f
                ),

            border =
                BorderStroke(
                    width =
                        1.dp,

                    color =
                        Color.White.copy(
                            alpha =
                                0.05f
                        )
                )
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        horizontal =
                            if (
                                isTablet
                            ) {
                                20.dp
                            } else {
                                16.dp
                            },

                        vertical =
                            if (
                                isTablet
                            ) {
                                18.dp
                            } else {
                                14.dp
                            }
                    )
            ) {
                DetailInfoRow(
                    label =
                        "Título original",

                    value =
                        uiState.originalTitle
                            .ifBlank {
                                uiState.title
                            },

                    isTablet =
                        isTablet
                )

                DetailInfoRow(
                    label =
                        "Ano",

                    value =
                        if (
                            uiState.endYear !=
                            null
                        ) {
                            "${uiState.year} – ${uiState.endYear}"
                        } else {
                            uiState.year
                        },

                    isTablet =
                        isTablet
                )

                DetailInfoRow(
                    label =
                        "Duração",

                    value =
                        uiState.durationInfo,

                    isTablet =
                        isTablet
                )

                DetailInfoRow(
                    label =
                        "Nota",

                    value =
                        uiState.rating,

                    isTablet =
                        isTablet
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
                        },

                    isTablet =
                        isTablet
                )

                DetailInfoRow(
                    label =
                        "Gêneros",

                    value =
                        uiState.genres
                            .joinToString(
                                ", "
                            ),

                    isTablet =
                        isTablet
                )
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    isTablet: Boolean
) {
    if (
        value.isBlank()
    ) {
        return
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical =
                        if (
                            isTablet
                        ) {
                            9.dp
                        } else {
                            7.dp
                        }
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
                if (
                    isTablet
                ) {
                    14.sp
                } else {
                    13.sp
                },

            lineHeight =
                if (
                    isTablet
                ) {
                    20.sp
                } else {
                    18.sp
                },

            fontWeight =
                FontWeight.Medium,

            modifier =
                Modifier.width(
                    if (
                        isTablet
                    ) {
                        150.dp
                    } else {
                        116.dp
                    }
                )
        )

        Text(
            text =
                value,

            color =
                LaranjadaText,

            fontSize =
                if (
                    isTablet
                ) {
                    14.sp
                } else {
                    13.sp
                },

            lineHeight =
                if (
                    isTablet
                ) {
                    20.sp
                } else {
                    18.sp
                },

            fontWeight =
                FontWeight.SemiBold,

            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}