package com.rnx.laranjada.feature.home

import androidx.compose.ui.graphics.Color

data class HomeUiState(
    val heroBanners: List<HeroBannerUi>,
    val selectedHeroIndex: Int,
    val categories: List<CategoryUi>,
    val selectedCategoryIndex: Int,
    val continueWatching: List<ContinueWatchingUi>,
    val contentSections: List<HomeContentSectionUi>,
    val collections: List<CollectionUi>
)

data class HeroBannerUi(
    val title: String,
    val year: String,
    val rating: String,
    val genres: List<String>,
    val duration: String,
    val quality: String,
    val synopsis: String,
    val imageUrl: String,
    val gradientColors: List<Color>,
    val uuid: String,
    val contentType: String
)

data class CategoryUi(
    val name: String
)

data class ContinueWatchingUi(
    val title: String,
    val episodeInfo: String,
    val remainingTime: String,
    val progress: Float,
    val imageUrl: String,
    val gradientColors: List<Color>
)

data class HomeContentSectionUi(
    val title: String,
    val contentType: String,
    val items: List<MediaItemUi>
)

data class MediaItemUi(
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val progress: Float? = null,
    val gradientColors: List<Color>,
    val uuid: String,
    val contentType: String
)

data class CollectionUi(
    val title: String,
    val imageUrl: String,
    val gradientColors: List<Color>
)