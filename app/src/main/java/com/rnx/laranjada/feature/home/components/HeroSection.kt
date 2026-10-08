
package com.rnx.laranjada.feature.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

private val HeroBodyBackground = Color(0xFF070B0F)

private const val HERO_AUTOPLAY_DELAY_MS = 10_000L
private const val HERO_SCROLL_DURATION_MS = 420

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
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { banners.size }
    )

    val coroutineScope = rememberCoroutineScope()
    val pagerInteractionVersion = remember { mutableIntStateOf(0) }

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            pagerInteractionVersion.intValue++
        }
    }

    LaunchedEffect(
        banners.size,
        pagerState.settledPage,
        pagerInteractionVersion.intValue
    ) {
        if (banners.size <= 1) return@LaunchedEffect

        delay(HERO_AUTOPLAY_DELAY_MS)

        if (pagerState.isScrollInProgress) {
            return@LaunchedEffect
        }

        val currentPage = pagerState.settledPage
        val nextPage = if (currentPage >= banners.lastIndex) {
            0
        } else {
            currentPage + 1
        }

        pagerState.animateScrollToPage(
            page = nextPage,
            animationSpec = tween(
                durationMillis = HERO_SCROLL_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth()
    ) { page ->
        val banner = banners[page]

        val continueItem = remember(
            banner.uuid,
            banner.contentType,
            continueWatching
        ) {
            findContinueItemForBanner(
                banner = banner,
                continueWatching = continueWatching
            )
        }

        HeroBannerSlide(
            banner = banner,
            continueItem = continueItem,
            selectedIndex = pagerState.currentPage,
            totalItems = banners.size,
            onBannerClick = {
                onBannerClick(banner)
            },
            onWatchClick = {
                onWatchClick(banner, continueItem)
            },
            onRestartClick = {
                if (continueItem != null) {
                    onRestartClick(continueItem)
                }
            },
            onFavoriteClick = {
                onFavoriteClick(banner)
            },
            onIndicatorClick = { index ->
                if (index == pagerState.currentPage) {
                    pagerInteractionVersion.intValue++
                    return@HeroBannerSlide
                }

                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        page = index,
                        animationSpec = tween(
                            durationMillis = HERO_SCROLL_DURATION_MS,
                            easing = FastOutSlowInEasing
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
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(5f / 9f)
            .background(LaranjadaBlack)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onBannerClick
            )
    ) {
        AsyncImage(
            model = banner.imageUrl,
            contentDescription = banner.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Transparent,
                            0.42f to Color.Transparent,
                            0.58f to HeroBodyBackground.copy(alpha = 0.05f),
                            0.70f to HeroBodyBackground.copy(alpha = 0.28f),
                            0.82f to HeroBodyBackground.copy(alpha = 0.68f),
                            0.92f to HeroBodyBackground.copy(alpha = 0.94f),
                            1.00f to HeroBodyBackground
                        )
                    )
                )
        )

        HeroContent(
            banner = banner,
            continueItem = continueItem,
            onWatchClick = onWatchClick,
            onRestartClick = onRestartClick,
            onFavoriteClick = onFavoriteClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 118.dp
                )
        )

        HeroIndicators(
            totalItems = totalItems,
            selectedIndex = selectedIndex,
            onIndicatorClick = onIndicatorClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 68.dp)
        )
    }
}

@Composable
private fun HeroContent(
    banner: HeroBannerUi,
    continueItem: ContinueWatchingUi?,
    onWatchClick: () -> Unit,
    onRestartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        HeroTitle(banner = banner)

        val metadata = buildList {
            if (banner.year.isNotBlank()) {
                add(banner.year)
            }

            if (banner.duration.isNotBlank()) {
                add(banner.duration)
            }

            banner.genres
                .filter { it.isNotBlank() }
                .take(3)
                .forEach { add(it) }
        }

        if (metadata.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = metadata.joinToString(separator = "  ·  "),
                color = LaranjadaText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (
            banner.rating.isNotBlank() &&
            banner.rating != "0" &&
            banner.rating != "0.0"
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = banner.rating,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onWatchClick,
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 14.dp,
                    vertical = 0.dp
                ),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(modifier = Modifier.size(4.dp))

                Text(
                    text = if (continueItem != null) {
                        "CONTINUAR"
                    } else {
                        "ASSISTIR"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }

            if (continueItem != null) {
                IconButton(
                    onClick = onRestartClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Replay,
                        contentDescription = "Reiniciar",
                        tint = Color.White,
                        modifier = Modifier.size(27.dp)
                    )
                }
            }

            FavoriteActionIcon(
                contentType = banner.contentType,
                contentUuid = banner.uuid,
                inactiveTint = Color.White,
                iconSize = 28.dp,
                modifier = Modifier.size(40.dp)
            )
        }

        if (continueItem != null) {
            HeroContinueProgress(item = continueItem)
        }

        if (banner.synopsis.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = banner.synopsis,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeroContinueProgress(item: ContinueWatchingUi) {
    val percentage = (
            item.progress.coerceIn(0f, 1f) * 100f
            ).toInt().coerceIn(0, 100)

    Spacer(modifier = Modifier.height(9.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = { item.progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .width(132.dp)
                .height(4.dp),
            color = LaranjadaOrange,
            trackColor = Color.White.copy(alpha = 0.32f)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = "$percentage% assistido",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }

    val secondaryMetadata = buildList {
        if (item.episodeInfo.isNotBlank()) {
            add(item.episodeInfo)
        }

        if (item.remainingTime.isNotBlank()) {
            add(item.remainingTime)
        }
    }

    if (secondaryMetadata.isNotEmpty()) {
        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = secondaryMetadata.joinToString(separator = "  ·  "),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HeroTitle(banner: HeroBannerUi) {
    if (banner.titleImageUrl.isNotBlank()) {
        AsyncImage(
            model = banner.titleImageUrl,
            contentDescription = banner.title,
            modifier = Modifier
                .height(72.dp)
                .widthIn(max = 210.dp),
            contentScale = ContentScale.Fit,
            alignment = Alignment.CenterStart
        )

        return
    }

    Text(
        text = banner.title,
        color = Color.White,
        fontSize = 30.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun HeroIndicators(
    totalItems: Int,
    selectedIndex: Int,
    onIndicatorClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalItems <= 1) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalItems) { index ->
            val selected = index == selectedIndex
            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onIndicatorClick(index) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(if (selected) 11.dp else 9.dp)
                        .background(
                            color = if (selected) {
                                Color.White
                            } else {
                                LaranjadaMutedText.copy(alpha = 0.70f)
                            },
                            shape = CircleShape
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
    val bannerContentType = banner.contentType.trim().lowercase()
    val bannerUuid = banner.uuid.trim()

    if (bannerUuid.isBlank()) return null

    val isSeries = bannerContentType in setOf("series", "serie")

    return if (isSeries) {
        continueWatching.firstOrNull { item ->
            item.contentType.trim().lowercase() == "episode" &&
                    item.seriesUuid?.trim() == bannerUuid
        }
    } else {
        continueWatching.firstOrNull { item ->
            item.contentType.trim().lowercase() == "movie" &&
                    item.contentUuid.trim() == bannerUuid
        }
    }
}
