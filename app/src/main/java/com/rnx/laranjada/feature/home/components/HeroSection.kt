package com.rnx.laranjada.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.feature.home.HeroBannerUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HeroSection(
    banners: List<HeroBannerUi>,
    modifier: Modifier = Modifier,
    onBannerClick: (HeroBannerUi) -> Unit = {}
) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { banners.size }
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(banners.size) {
        while (banners.size > 1) {
            delay(10_000)

            val nextPage = if (pagerState.currentPage == banners.lastIndex) {
                0
            } else {
                pagerState.currentPage + 1
            }

            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            HeroBannerImage(
                banner = banners[page],
                onClick = {
                    onBannerClick(banners[page])
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        HeroIndicators(
            totalItems = banners.size,
            selectedIndex = pagerState.currentPage,
            onIndicatorClick = { index ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
        )
    }
}

@Composable
private fun HeroBannerImage(
    banner: HeroBannerUi,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .background(LaranjadaBlack)
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        AsyncImage(
            model = banner.imageUrl,
            contentDescription = banner.title,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun HeroIndicators(
    totalItems: Int,
    selectedIndex: Int,
    onIndicatorClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalItems) { index ->
            val selected = index == selectedIndex
            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (selected) 9.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            LaranjadaOrange
                        } else {
                            LaranjadaMutedText.copy(alpha = 0.45f)
                        }
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onIndicatorClick(index) }
                    )
            )
        }
    }
}