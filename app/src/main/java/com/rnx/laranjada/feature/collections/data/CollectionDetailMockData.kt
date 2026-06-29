package com.rnx.laranjada.feature.collections.data

import androidx.compose.ui.graphics.Color
import com.rnx.laranjada.feature.collections.CollectionDetailUiState
import com.rnx.laranjada.feature.home.MediaItemUi

object CollectionDetailMockData {

    val dcCollection = CollectionDetailUiState(
        uuid = "mock-dc-collection",
        title = "DC",
        imageUrl = "https://picsum.photos/seed/laranjada-dc-collection-hero/1200/675",
        movies = listOf(
            media(
                title = "Cidade Sombria",
                subtitle = "Filme",
                imageUrl = "https://picsum.photos/seed/dc-movie-1/640/360",
                uuid = "mock-dc-movie-1",
                contentType = "movie"
            ),
            media(
                title = "Liga da Noite",
                subtitle = "Filme",
                imageUrl = "https://picsum.photos/seed/dc-movie-2/640/360",
                uuid = "mock-dc-movie-2",
                contentType = "movie"
            ),
            media(
                title = "Guardião Cósmico",
                subtitle = "Filme",
                imageUrl = "https://picsum.photos/seed/dc-movie-3/640/360",
                uuid = "mock-dc-movie-3",
                contentType = "movie"
            ),
            media(
                title = "Cidade Neon",
                subtitle = "Filme",
                imageUrl = "https://picsum.photos/seed/dc-movie-4/640/360",
                uuid = "mock-dc-movie-4",
                contentType = "movie"
            )
        ),
        series = listOf(
            media(
                title = "Jovens Heróis",
                subtitle = "Série",
                imageUrl = "https://picsum.photos/seed/dc-series-1/640/360",
                uuid = "mock-dc-series-1",
                contentType = "series"
            ),
            media(
                title = "O Arqueiro",
                subtitle = "Série",
                imageUrl = "https://picsum.photos/seed/dc-series-2/640/360",
                uuid = "mock-dc-series-2",
                contentType = "series"
            ),
            media(
                title = "Conselho Secreto",
                subtitle = "Série",
                imageUrl = "https://picsum.photos/seed/dc-series-3/640/360",
                uuid = "mock-dc-series-3",
                contentType = "series"
            ),
            media(
                title = "Guerra nas Sombras",
                subtitle = "Série",
                imageUrl = "https://picsum.photos/seed/dc-series-4/640/360",
                uuid = "mock-dc-series-4",
                contentType = "series"
            )
        )
    )

    private fun media(
        title: String,
        subtitle: String,
        imageUrl: String,
        uuid: String,
        contentType: String
    ): MediaItemUi {
        return MediaItemUi(
            title = title,
            subtitle = subtitle,
            imageUrl = imageUrl,
            progress = null,
            gradientColors = listOf(
                Color(0xFF08121A),
                Color(0xFF0A0A0B)
            ),
            uuid = uuid,
            contentType = contentType
        )
    }
}