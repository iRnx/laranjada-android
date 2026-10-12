package com.rnx.laranjada.feature.home.components

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.feature.favorites.components.FavoriteActionIcon
import com.rnx.laranjada.feature.home.ContinueWatchingUi
import com.rnx.laranjada.feature.home.HeroBannerUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val HeroBodyBackground =
    Color(
        0xFF070B0F
    )

private const val HERO_AUTOPLAY_DELAY_MS =
    10_000L

private const val HERO_SCROLL_DURATION_MS =
    420

@Composable
fun HeroSection(
    banners: List<HeroBannerUi>,
    continueWatching: List<ContinueWatchingUi>,
    modifier: Modifier = Modifier,
    onBannerClick: (HeroBannerUi) -> Unit = {},
    onWatchClick: (
        banner: HeroBannerUi,
        continueItem: ContinueWatchingUi?
    ) -> Unit = { _, _ -> },
    onRestartClick: (ContinueWatchingUi) -> Unit = {},
    onFavoriteClick: (HeroBannerUi) -> Unit = {}
) {
    if (
        banners.isEmpty()
    ) {
        return
    }

    val pagerState =
        rememberPagerState(
            initialPage =
                0,

            pageCount = {
                banners.size
            }
        )

    val coroutineScope =
        rememberCoroutineScope()

    val pagerInteractionVersion =
        remember {
            mutableIntStateOf(
                0
            )
        }

    LaunchedEffect(
        pagerState.isScrollInProgress
    ) {
        if (
            !pagerState.isScrollInProgress
        ) {
            pagerInteractionVersion
                .intValue++
        }
    }

    LaunchedEffect(
        banners.size,
        pagerState.settledPage,
        pagerInteractionVersion.intValue
    ) {
        if (
            banners.size <=
            1
        ) {
            return@LaunchedEffect
        }

        delay(
            HERO_AUTOPLAY_DELAY_MS
        )

        if (
            pagerState.isScrollInProgress
        ) {
            return@LaunchedEffect
        }

        val currentPage =
            pagerState.settledPage

        val nextPage =
            if (
                currentPage >=
                banners.lastIndex
            ) {
                0
            } else {
                currentPage + 1
            }

        pagerState.animateScrollToPage(
            page =
                nextPage,

            animationSpec =
                tween(
                    durationMillis =
                        HERO_SCROLL_DURATION_MS,

                    easing =
                        FastOutSlowInEasing
                )
        )
    }

    HorizontalPager(
        state =
            pagerState,

        modifier =
            modifier.fillMaxWidth()
    ) {
            page ->

        val banner =
            banners[
                page
            ]

        val continueItem =
            remember(
                banner.uuid,
                banner.contentType,
                continueWatching
            ) {
                findContinueItemForBanner(
                    banner =
                        banner,

                    continueWatching =
                        continueWatching
                )
            }

        HeroBannerSlide(
            banner =
                banner,

            continueItem =
                continueItem,

            selectedIndex =
                pagerState.currentPage,

            totalItems =
                banners.size,

            onBannerClick = {
                onBannerClick(
                    banner
                )
            },

            onWatchClick = {
                onWatchClick(
                    banner,
                    continueItem
                )
            },

            onRestartClick = {
                if (
                    continueItem !=
                    null
                ) {
                    onRestartClick(
                        continueItem
                    )
                }
            },

            onFavoriteClick = {
                onFavoriteClick(
                    banner
                )
            },

            onIndicatorClick = {
                    index ->

                if (
                    index ==
                    pagerState.currentPage
                ) {
                    pagerInteractionVersion
                        .intValue++

                    return@HeroBannerSlide
                }

                coroutineScope.launch {
                    pagerState
                        .animateScrollToPage(
                            page =
                                index,

                            animationSpec =
                                tween(
                                    durationMillis =
                                        HERO_SCROLL_DURATION_MS,

                                    easing =
                                        FastOutSlowInEasing
                                )
                        )
                }
            }
        )
    }
}

@Composable
private fun HeroBannerSlide(
    banner: HeroBannerUi,
    continueItem: ContinueWatchingUi?,
    selectedIndex: Int,
    totalItems: Int,
    onBannerClick: () -> Unit,
    onWatchClick: () -> Unit,
    onRestartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onIndicatorClick: (Int) -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val configuration =
        LocalConfiguration.current

    BoxWithConstraints(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        val isTablet =
            maxWidth >=
                    600.dp

        val isExpandedTablet =
            maxWidth >=
                    840.dp

        /*
         * [ALTERAÇÃO]
         *
         * Não basta saber se é tablet.
         *
         * Tablet em pé e tablet deitado
         * precisam de proporções diferentes.
         */
        val isTabletPortrait =
            isTablet &&
                    configuration.orientation ==
                    Configuration.ORIENTATION_PORTRAIT

        val heroImageUrl =
            if (
                isTablet
            ) {
                banner.desktopImageUrl
                    .ifBlank {
                        banner.imageUrl
                    }
            } else {
                banner.mobileImageUrl
                    .ifBlank {
                        banner.imageUrl
                    }
            }

        /*
         * CELULAR:
         * arte vertical mobile.
         *
         * TABLET EM PÉ:
         * arte desktop dentro de uma
         * janela 16:9 mais alta.
         *
         * TABLET DEITADO:
         * mantém 2.4:1 aprovado.
         */
        val heroAspectRatio =
            when {
                !isTablet ->
                    5f / 9f

                isTabletPortrait ->
                    16f / 9f

                else ->
                    2.4f
            }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        heroAspectRatio
                    )
                    .background(
                        LaranjadaBlack
                    )
                    .clickable(
                        interactionSource =
                            interactionSource,

                        indication =
                            null,

                        onClick =
                            onBannerClick
                    )
        ) {
            AsyncImage(
                model =
                    heroImageUrl,

                contentDescription =
                    banner.title,

                modifier =
                    Modifier.fillMaxSize(),

                contentScale =
                    ContentScale.Crop
            )

            if (
                isTablet
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colorStops =
                                        arrayOf(
                                            0.00f to
                                                    HeroBodyBackground.copy(
                                                        alpha =
                                                            0.90f
                                                    ),

                                            0.28f to
                                                    HeroBodyBackground.copy(
                                                        alpha =
                                                            0.72f
                                                    ),

                                            0.52f to
                                                    HeroBodyBackground.copy(
                                                        alpha =
                                                            0.22f
                                                    ),

                                            0.74f to
                                                    Color.Transparent,

                                            1.00f to
                                                    Color.Transparent
                                        )
                                )
                            )
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops =
                                        arrayOf(
                                            0.00f to
                                                    Color.Transparent,

                                            0.60f to
                                                    Color.Transparent,

                                            0.82f to
                                                    HeroBodyBackground.copy(
                                                        alpha =
                                                            0.28f
                                                    ),

                                            1.00f to
                                                    HeroBodyBackground.copy(
                                                        alpha =
                                                            0.88f
                                                    )
                                        )
                                )
                            )
                )
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops =
                                        arrayOf(
                                            0.00f to
                                                    Color.Transparent,

                                            0.42f to
                                                    Color.Transparent,

                                            0.58f to
                                                    HeroBodyBackground.copy(
                                                        alpha =
                                                            0.05f
                                                    ),

                                            0.70f to
                                                    HeroBodyBackground.copy(
                                                        alpha =
                                                            0.28f
                                                    ),

                                            0.82f to
                                                    HeroBodyBackground.copy(
                                                        alpha =
                                                            0.68f
                                                    ),

                                            0.92f to
                                                    HeroBodyBackground.copy(
                                                        alpha =
                                                            0.94f
                                                    ),

                                            1.00f to
                                                    HeroBodyBackground
                                        )
                                )
                            )
                )
            }

            HeroContent(
                banner =
                    banner,

                continueItem =
                    continueItem,

                isTablet =
                    isTablet,

                isExpandedTablet =
                    isExpandedTablet,

                onWatchClick =
                    onWatchClick,

                onRestartClick =
                    onRestartClick,

                onFavoriteClick =
                    onFavoriteClick,

                modifier =
                    if (
                        isTablet
                    ) {
                        Modifier
                            .align(
                                Alignment.BottomStart
                            )
                            .padding(
                                start =
                                    if (
                                        isExpandedTablet
                                    ) {
                                        42.dp
                                    } else {
                                        28.dp
                                    },

                                end =
                                    24.dp,

                                bottom =
                                    if (
                                        isTabletPortrait
                                    ) {
                                        96.dp
                                    } else {
                                        92.dp
                                    }
                            )
                            .widthIn(
                                max =
                                    if (
                                        isExpandedTablet
                                    ) {
                                        520.dp
                                    } else {
                                        440.dp
                                    }
                            )
                            .fillMaxWidth()
                    } else {
                        Modifier
                            .align(
                                Alignment.BottomStart
                            )
                            .padding(
                                start =
                                    16.dp,

                                end =
                                    16.dp,

                                bottom =
                                    118.dp
                            )
                            .fillMaxWidth()
                    }
            )

            HeroIndicators(
                totalItems =
                    totalItems,

                selectedIndex =
                    selectedIndex,

                isTablet =
                    isTablet,

                onIndicatorClick =
                    onIndicatorClick,

                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .padding(
                            bottom =
                                if (
                                    isTabletPortrait
                                ) {
                                    58.dp
                                } else if (
                                    isTablet
                                ) {
                                    56.dp
                                } else {
                                    68.dp
                                }
                        )
            )
        }
    }
}

@Composable
private fun HeroContent(
    banner: HeroBannerUi,
    continueItem: ContinueWatchingUi?,
    isTablet: Boolean,
    isExpandedTablet: Boolean,
    onWatchClick: () -> Unit,
    onRestartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
    ) {
        HeroTitle(
            banner =
                banner,

            isTablet =
                isTablet,

            isExpandedTablet =
                isExpandedTablet
        )

        val metadata =
            buildList {
                if (
                    banner.year
                        .isNotBlank()
                ) {
                    add(
                        banner.year
                    )
                }

                if (
                    banner.duration
                        .isNotBlank()
                ) {
                    add(
                        banner.duration
                    )
                }

                banner.genres
                    .filter {
                        it.isNotBlank()
                    }
                    .take(
                        if (
                            isTablet
                        ) {
                            2
                        } else {
                            3
                        }
                    )
                    .forEach {
                        add(
                            it
                        )
                    }
            }

        if (
            metadata.isNotEmpty()
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        if (
                            isTablet
                        ) {
                            6.dp
                        } else {
                            8.dp
                        }
                    )
            )

            Text(
                text =
                    metadata.joinToString(
                        separator =
                            "  ·  "
                    ),

                color =
                    LaranjadaText,

                fontSize =
                    if (
                        isTablet
                    ) {
                        13.sp
                    } else {
                        12.sp
                    },

                fontWeight =
                    FontWeight.Medium,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }

        if (
            banner.rating.isNotBlank() &&
            banner.rating != "0" &&
            banner.rating != "0.0"
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        if (
                            isTablet
                        ) {
                            5.dp
                        } else {
                            8.dp
                        }
                    )
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.spacedBy(
                        4.dp
                    )
            ) {
                Text(
                    text =
                        banner.rating,

                    color =
                        Color.White,

                    fontSize =
                        if (
                            isTablet
                        ) {
                            14.sp
                        } else {
                            13.sp
                        },

                    fontWeight =
                        FontWeight.Bold
                )

                Icon(
                    imageVector =
                        Icons.Rounded.Star,

                    contentDescription =
                        null,

                    tint =
                        Color(
                            0xFFFFC107
                        ),

                    modifier =
                        Modifier.size(
                            if (
                                isTablet
                            ) {
                                17.dp
                            } else {
                                16.dp
                            }
                        )
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    if (
                        isTablet
                    ) {
                        8.dp
                    } else {
                        10.dp
                    }
                )
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(
                    if (
                        isTablet
                    ) {
                        10.dp
                    } else {
                        8.dp
                    }
                )
        ) {
            Button(
                onClick =
                    onWatchClick,

                shape =
                    RoundedCornerShape(
                        7.dp
                    ),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color.White,

                        contentColor =
                            Color.Black
                    ),

                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        horizontal =
                            if (
                                isTablet
                            ) {
                                18.dp
                            } else {
                                14.dp
                            },

                        vertical =
                            0.dp
                    ),

                modifier =
                    Modifier.height(
                        if (
                            isTablet
                        ) {
                            42.dp
                        } else {
                            38.dp
                        }
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
                                21.dp
                            } else {
                                19.dp
                            }
                        )
                )

                Spacer(
                    modifier =
                        Modifier.size(
                            4.dp
                        )
                )

                Text(
                    text =
                        if (
                            continueItem !=
                            null
                        ) {
                            "CONTINUAR"
                        } else {
                            "ASSISTIR"
                        },

                    fontSize =
                        if (
                            isTablet
                        ) {
                            12.sp
                        } else {
                            11.sp
                        },

                    fontWeight =
                        FontWeight.ExtraBold,

                    letterSpacing =
                        0.8.sp
                )
            }

            if (
                continueItem !=
                null
            ) {
                IconButton(
                    onClick =
                        onRestartClick,

                    modifier =
                        Modifier.size(
                            if (
                                isTablet
                            ) {
                                44.dp
                            } else {
                                40.dp
                            }
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Replay,

                        contentDescription =
                            "Reiniciar",

                        tint =
                            Color.White,

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
            }

            FavoriteActionIcon(
                contentType =
                    banner.contentType,

                contentUuid =
                    banner.uuid,

                inactiveTint =
                    Color.White,

                iconSize =
                    if (
                        isTablet
                    ) {
                        30.dp
                    } else {
                        28.dp
                    },

                modifier =
                    Modifier.size(
                        if (
                            isTablet
                        ) {
                            44.dp
                        } else {
                            40.dp
                        }
                    )
            )
        }

        if (
            continueItem !=
            null
        ) {
            HeroContinueProgress(
                item =
                    continueItem,

                isTablet =
                    isTablet
            )
        }

        if (
            banner.synopsis
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
                    banner.synopsis,

                color =
                    Color.White,

                fontSize =
                    if (
                        isTablet
                    ) {
                        13.sp
                    } else {
                        12.sp
                    },

                lineHeight =
                    if (
                        isTablet
                    ) {
                        18.sp
                    } else {
                        16.sp
                    },

                fontWeight =
                    FontWeight.Medium,

                maxLines =
                    if (
                        isExpandedTablet
                    ) {
                        3
                    } else {
                        2
                    },

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeroContinueProgress(
    item: ContinueWatchingUi,
    isTablet: Boolean
) {
    val percentage =
        (
                item.progress
                    .coerceIn(
                        0f,
                        1f
                    ) *
                        100f
                )
            .toInt()
            .coerceIn(
                0,
                100
            )

    Spacer(
        modifier =
            Modifier.height(
                9.dp
            )
    )

    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        LinearProgressIndicator(
            progress = {
                item.progress
                    .coerceIn(
                        0f,
                        1f
                    )
            },

            modifier =
                Modifier
                    .width(
                        if (
                            isTablet
                        ) {
                            160.dp
                        } else {
                            132.dp
                        }
                    )
                    .height(
                        4.dp
                    ),

            color =
                LaranjadaOrange,

            trackColor =
                Color.White.copy(
                    alpha = 0.32f
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
                "$percentage% assistido",

            color =
                Color.White,

            fontSize =
                if (
                    isTablet
                ) {
                    12.sp
                } else {
                    11.sp
                },

            fontWeight =
                FontWeight.SemiBold,

            maxLines =
                1
        )
    }

    val secondaryMetadata =
        buildList {
            if (
                item.episodeInfo
                    .isNotBlank()
            ) {
                add(
                    item.episodeInfo
                )
            }

            if (
                item.remainingTime
                    .isNotBlank()
            ) {
                add(
                    item.remainingTime
                )
            }
        }

    if (
        secondaryMetadata
            .isNotEmpty()
    ) {
        Spacer(
            modifier =
                Modifier.height(
                    5.dp
                )
        )

        Text(
            text =
                secondaryMetadata
                    .joinToString(
                        separator =
                            "  ·  "
                    ),

            color =
                Color.White,

            fontSize =
                if (
                    isTablet
                ) {
                    12.sp
                } else {
                    11.sp
                },

            fontWeight =
                FontWeight.Medium,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HeroTitle(
    banner: HeroBannerUi,
    isTablet: Boolean,
    isExpandedTablet: Boolean
) {
    if (
        banner.titleImageUrl
            .isNotBlank()
    ) {
        AsyncImage(
            model =
                banner.titleImageUrl,

            contentDescription =
                banner.title,

            modifier =
                Modifier
                    .height(
                        when {
                            isExpandedTablet ->
                                86.dp

                            isTablet ->
                                72.dp

                            else ->
                                72.dp
                        }
                    )
                    .widthIn(
                        max =
                            when {
                                isExpandedTablet ->
                                    320.dp

                                isTablet ->
                                    270.dp

                                else ->
                                    210.dp
                            }
                    ),

            contentScale =
                ContentScale.Fit,

            alignment =
                Alignment.CenterStart
        )

        return
    }

    Text(
        text =
            banner.title,

        color =
            Color.White,

        fontSize =
            when {
                isExpandedTablet ->
                    38.sp

                isTablet ->
                    34.sp

                else ->
                    30.sp
            },

        lineHeight =
            when {
                isExpandedTablet ->
                    40.sp

                isTablet ->
                    36.sp

                else ->
                    32.sp
            },

        fontWeight =
            FontWeight.ExtraBold,

        maxLines =
            2,

        overflow =
            TextOverflow.Ellipsis
    )
}

@Composable
private fun HeroIndicators(
    totalItems: Int,
    selectedIndex: Int,
    isTablet: Boolean,
    onIndicatorClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (
        totalItems <=
        1
    ) {
        return
    }

    Row(
        modifier =
            modifier,

        horizontalArrangement =
            Arrangement.Center,

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        repeat(
            totalItems
        ) {
                index ->

            val selected =
                index ==
                        selectedIndex

            val interactionSource =
                remember {
                    MutableInteractionSource()
                }

            Box(
                modifier =
                    Modifier
                        .size(
                            if (
                                isTablet
                            ) {
                                24.dp
                            } else {
                                22.dp
                            }
                        )
                        .clickable(
                            interactionSource =
                                interactionSource,

                            indication =
                                null,

                            onClick = {
                                onIndicatorClick(
                                    index
                                )
                            }
                        ),

                contentAlignment =
                    Alignment.Center
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(
                                when {
                                    isTablet &&
                                            selected ->
                                        12.dp

                                    isTablet ->
                                        10.dp

                                    selected ->
                                        11.dp

                                    else ->
                                        9.dp
                                }
                            )
                            .background(
                                color =
                                    if (
                                        selected
                                    ) {
                                        Color.White
                                    } else {
                                        LaranjadaMutedText.copy(
                                            alpha = 0.70f
                                        )
                                    },

                                shape =
                                    CircleShape
                            )
                )
            }
        }
    }
}

private fun findContinueItemForBanner(
    banner: HeroBannerUi,
    continueWatching: List<ContinueWatchingUi>
): ContinueWatchingUi? {
    val bannerContentType =
        banner.contentType
            .trim()
            .lowercase()

    val bannerUuid =
        banner.uuid
            .trim()

    if (
        bannerUuid.isBlank()
    ) {
        return null
    }

    val isSeries =
        bannerContentType in
                setOf(
                    "series",
                    "serie"
                )

    return if (
        isSeries
    ) {
        continueWatching
            .firstOrNull {
                    item ->

                item.contentType
                    .trim()
                    .lowercase() ==
                        "episode" &&
                        item.seriesUuid
                            ?.trim() ==
                        bannerUuid
            }
    } else {
        continueWatching
            .firstOrNull {
                    item ->

                item.contentType
                    .trim()
                    .lowercase() ==
                        "movie" &&
                        item.contentUuid
                            .trim() ==
                        bannerUuid
            }
    }
}