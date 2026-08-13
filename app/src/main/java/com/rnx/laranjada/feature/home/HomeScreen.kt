package com.rnx.laranjada.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnx.laranjada.feature.home.components.CategoryChipsRow
import com.rnx.laranjada.feature.home.components.CollectionsSection
import com.rnx.laranjada.feature.home.components.ContinueWatchingCard
import com.rnx.laranjada.feature.home.components.HeroSection
import com.rnx.laranjada.feature.home.components.HomeBottomBar
import com.rnx.laranjada.feature.home.components.HomeFooter
import com.rnx.laranjada.feature.home.components.LandscapeMediaCard
import com.rnx.laranjada.feature.home.components.SectionHeader

private val LaranjadaDarkBackground = Color(
    0xFF070B0F
)

@Composable
fun HomeScreen(
    onBannerClick: (HeroBannerUi) -> Unit = {},
    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit = { _, _ -> },
    onCollectionClick: (CollectionUi) -> Unit = {},
    onSeeAllClick: (
        sectionSlug: String,
        title: String
    ) -> Unit = { _, _ -> },
    onCategoryGridClick: (
        sectionSlug: String,
        title: String
    ) -> Unit = { _, _ -> },
    onAccountClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    val selectedBottomIndex = remember {
        mutableIntStateOf(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                LaranjadaDarkBackground
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 118.dp
            ),
            verticalArrangement = Arrangement.spacedBy(
                18.dp
            )
        ) {
            item {
                HeroSection(
                    banners = uiState.heroBanners,
                    onBannerClick = { banner ->
                        if (
                            banner.uuid.isNotBlank() &&
                            banner.contentType.isNotBlank()
                        ) {
                            onBannerClick(banner)
                        }
                    }
                )
            }

            item {
                CategoryChipsRow(
                    categories = uiState.categories,
                    selectedIndex = uiState.selectedCategoryIndex,
                    modifier = Modifier.padding(
                        horizontal = 18.dp
                    ),
                    onCategoryClick = { index ->
                        val category = uiState.categories
                            .getOrNull(index)
                            ?: return@CategoryChipsRow

                        val sectionSlug =
                            categoryToSectionSlug(
                                category.slug
                            )

                        if (
                            sectionSlug.isNotBlank()
                        ) {
                            onCategoryGridClick(
                                sectionSlug,
                                category.name
                            )
                        }
                    }
                )
            }

            if (
                uiState.continueWatching.isNotEmpty()
            ) {
                item {
                    SectionHeader(
                        title = "Continue assistindo",
                        modifier = Modifier.padding(
                            horizontal = 18.dp
                        ),
                        showSeeAll = false
                    )

                    Spacer(
                        modifier = Modifier.height(
                            6.dp
                        )
                    )

                    LazyRow(
                        contentPadding = PaddingValues(
                            horizontal = 18.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(
                            14.dp
                        )
                    ) {
                        items(
                            uiState.continueWatching
                        ) { item ->
                            ContinueWatchingCard(
                                item = item
                            )
                        }
                    }
                }
            }

            items(
                uiState.contentSections
            ) { section ->
                SectionHeader(
                    title = section.title,
                    modifier = Modifier.padding(
                        horizontal = 18.dp
                    ),
                    showSeeAll = true,
                    onSeeAllClick = {
                        val sectionSlug =
                            section.slug.ifBlank {
                                sectionTitleToSlug(
                                    section.title
                                )
                            }

                        if (
                            sectionSlug.isNotBlank()
                        ) {
                            onSeeAllClick(
                                sectionSlug,
                                section.title
                            )
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(
                        6.dp
                    )
                )

                TwoRowsMediaCarousel(
                    items = section.items,
                    onMediaClick = onMediaClick
                )
            }

            if (
                uiState.collections.isNotEmpty()
            ) {
                item {
                    CollectionsSection(
                        collections = uiState.collections,
                        onCollectionClick = onCollectionClick
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
                if (index == 3) {
                    onAccountClick()
                } else {
                    selectedBottomIndex.intValue =
                        index
                }
            },
            modifier = Modifier.align(
                Alignment.BottomCenter
            )
        )
    }
}

@Composable
private fun TwoRowsMediaCarousel(
    items: List<MediaItemUi>,
    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit
) {
    val columns = remember(items) {
        items.chunked(2)
    }

    LazyRow(
        contentPadding = PaddingValues(
            horizontal = 18.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(
            10.dp
        )
    ) {
        items(columns) { columnItems ->
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    10.dp
                )
            ) {
                columnItems.forEach { item ->
                    LandscapeMediaCard(
                        item = item,
                        onClick = {
                            if (
                                item.uuid.isNotBlank() &&
                                item.contentType.isNotBlank()
                            ) {
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
    }
}

private fun categoryToSectionSlug(
    categorySlug: String
): String {
    return when (categorySlug) {
        "movies" -> "movies"
        "series" -> "series"
        "cartoons" -> "all-cartoons"
        "animes" -> "all-animes"
        "doramas" -> "doramas"
        else -> ""
    }
}

private fun sectionTitleToSlug(
    title: String
): String {
    return when (
        title.trim().lowercase()
    ) {
        "novos filmes" ->
            "recent-movies"

        "novas séries",
        "novas series" ->
            "recent-series"

        "filmes adicionados recentemente" ->
            "recent-movies"

        "séries adicionadas recentemente",
        "series adicionadas recentemente" ->
            "recent-series"

        "filmes em desenho" ->
            "cartoon-movies"

        "séries em desenho",
        "series em desenho" ->
            "cartoon-series"

        "animes filmes" ->
            "anime-movies"

        "animes séries",
        "animes series" ->
            "anime-series"

        "doramas" ->
            "doramas"

        "filmes" ->
            "movies"

        "séries",
        "series" ->
            "series"

        "desenhos" ->
            "all-cartoons"

        "animes" ->
            "all-animes"

        else -> ""
    }
}