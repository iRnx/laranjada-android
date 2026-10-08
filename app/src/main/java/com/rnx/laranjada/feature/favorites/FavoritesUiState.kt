
package com.rnx.laranjada.feature.favorites

import com.rnx.laranjada.domain.model.Favorite
import com.rnx.laranjada.domain.model.FavoriteGenre
import com.rnx.laranjada.domain.model.FavoritesQuery

data class FavoritesUiState(
    val items: List<Favorite> = emptyList(),
    val query: FavoritesQuery = FavoritesQuery(),
    val totalCount: Int = 0,
    val totalFavorites: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 60,
    val totalPages: Int = 1,
    val nextPage: Int? = null,
    val previousPage: Int? = null,
    val availableYears: List<Int> = emptyList(),
    val availableGenres: List<FavoriteGenre> = emptyList(),
    val isLoading: Boolean = false,
    val removingContentKey: String? = null,
    val error: String? = null
) {
    val rangeLabel: String
        get() {
            if (totalCount == 0) {
                return "0 de 0"
            }

            val first =
                (page - 1) * pageSize + 1

            val last =
                minOf(
                    page * pageSize,
                    totalCount
                )

            return "$first–$last de $totalCount"
        }
}
