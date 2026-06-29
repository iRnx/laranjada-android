package com.rnx.laranjada.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.feature.home.components.CategoryChipsRow
import com.rnx.laranjada.feature.home.components.CollectionsSection
import com.rnx.laranjada.feature.home.components.ContinueWatchingCard
import com.rnx.laranjada.feature.home.components.HeroSection
import com.rnx.laranjada.feature.home.components.HomeBottomBar
import com.rnx.laranjada.feature.home.components.HomeFooter
import com.rnx.laranjada.feature.home.components.LandscapeMediaCard
import com.rnx.laranjada.feature.home.components.SectionHeader

@Composable
fun HomeScreen(
    onBannerClick: (HeroBannerUi) -> Unit = {},
    onMediaClick: (contentType: String, uuid: String) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val selectedBottomIndex = remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LaranjadaBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HeroSection(
                    banners = uiState.heroBanners,
                    onBannerClick = { banner ->
                        if (banner.uuid.isNotBlank() && banner.contentType.isNotBlank()) {
                            onBannerClick(banner)
                        }
                    }
                )
            }

            item {
                CategoryChipsRow(
                    categories = uiState.categories,
                    selectedIndex = uiState.selectedCategoryIndex,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }

            if (uiState.continueWatching.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Continue assistindo",
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.continueWatching) { item ->
                            ContinueWatchingCard(item = item)
                        }
                    }
                }
            }

            items(uiState.contentSections) { section ->
                SectionHeader(
                    title = section.title,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(section.items) { item ->
                        LandscapeMediaCard(
                            item = item,
                            onClick = {
                                if (item.uuid.isNotBlank() && item.contentType.isNotBlank()) {
                                    onMediaClick(
                                        item.contentType,
                                        item.uuid
                                    )
                                }
                            }
                        )
                    }
                }
            }

            if (uiState.collections.isNotEmpty()) {
                item {
                    CollectionsSection(
                        collections = uiState.collections
                    )
                }
            }

            item {
                HomeFooter(
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 28.dp,
                        bottom = 20.dp
                    )
                )
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