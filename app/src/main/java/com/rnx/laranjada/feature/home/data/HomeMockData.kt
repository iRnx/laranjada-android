package com.rnx.laranjada.feature.home.data

import androidx.compose.ui.graphics.Color
import com.rnx.laranjada.feature.home.CategoryUi
import com.rnx.laranjada.feature.home.CollectionUi
import com.rnx.laranjada.feature.home.ContinueWatchingUi
import com.rnx.laranjada.feature.home.HeroBannerUi
import com.rnx.laranjada.feature.home.HomeContentSectionUi
import com.rnx.laranjada.feature.home.HomeUiState
import com.rnx.laranjada.feature.home.MediaItemUi

object HomeMockData {

    val uiState = HomeUiState(
        heroBanners = listOf(
            HeroBannerUi(
                title = "Carregando...",
                year = "",
                rating = "",
                genres = emptyList(),
                duration = "",
                quality = "",
                synopsis = "",
                imageUrl = "https://picsum.photos/seed/laranjada-loading-banner/1200/1500",
                gradientColors = listOf(Color(0xFF08121A), Color(0xFF0A0A0B)),
                uuid = "mock-series-uuid",
                contentType = "series"
            )
        ),
        selectedHeroIndex = 0,
        categories = listOf(
            CategoryUi("Tudo"),
            CategoryUi("Filmes"),
            CategoryUi("Séries")
        ),
        selectedCategoryIndex = 0,
        continueWatching = emptyList(),
        contentSections = listOf(
            HomeContentSectionUi(
                title = "Carregando conteúdos",
                contentType = "movie",
                items = listOf(
                    MediaItemUi(
                        title = "Carregando...",
                        subtitle = "",
                        imageUrl = "https://picsum.photos/seed/laranjada-loading-card/640/360",
                        progress = null,
                        gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF020617)),
                        uuid = "mock-movie-uuid",
                        contentType = "movie"
                    )
                )
            )
        ),
        collections = emptyList()
    )
}