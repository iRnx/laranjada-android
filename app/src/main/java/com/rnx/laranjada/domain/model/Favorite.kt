
package com.rnx.laranjada.domain.model

data class Favorite(
    val favoriteUuid: String,
    val contentType: String,
    val contentUuid: String,
    val title: String,
    val originalTitle: String,
    val categoryType: String,
    val year: Int?,
    val rating: Double?,
    val imageUrl: String,
    val isFavorite: Boolean,
    val canWatch: Boolean,
    val isLocked: Boolean,
    val createdAt: String
) {
    val stableKey: String
        get() = "$contentType:$contentUuid"
}

data class FavoriteGenre(
    val id: String,
    val name: String
)

data class FavoritesQuery(
    val contentType: String = "all",
    val category: String = "all",
    val search: String = "",
    val year: String = "",
    val ratingMin: String = "",
    val genres: List<String> = emptyList(),
    val letter: String = "",
    val order: String = "created_desc",
    val page: Int = 1
)

data class FavoritesPage(
    val profileUuid: String,
    val totalCount: Int,
    val totalFavorites: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val nextPage: Int?,
    val previousPage: Int?,
    val availableYears: List<Int>,
    val availableGenres: List<FavoriteGenre>,
    val items: List<Favorite>
)

data class FavoriteStatus(
    val contentType: String,
    val contentUuid: String,
    val isFavorite: Boolean
)
