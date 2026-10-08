
package com.rnx.laranjada.feature.mediagrid

import com.rnx.laranjada.feature.home.MediaItemUi
import kotlin.math.ceil

data class MediaGridUiState(
    val title: String = "",
    val sectionSlug: String = "",
    val contentType: String = "",
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val pageSize: Int = 60,
    val nextUrl: String? = null,
    val previousUrl: String? = null,
    val items: List<MediaItemUi> = emptyList(),
    val appliedFilters: MediaGridAppliedFiltersUi =
        MediaGridAppliedFiltersUi(),
    val availableFilters: MediaGridAvailableFiltersUi =
        MediaGridAvailableFiltersUi()
) {
    val hasNextPage: Boolean
        get() = !nextUrl.isNullOrBlank()

    val hasPreviousPage: Boolean
        get() = !previousUrl.isNullOrBlank()

    val totalPages: Int
        get() {
            if (totalCount <= 0 || pageSize <= 0) {
                return 1
            }

            return ceil(
                totalCount.toDouble() /
                        pageSize.toDouble()
            ).toInt().coerceAtLeast(1)
        }
}

data class MediaGridAppliedFiltersUi(
    val q: String = "",
    val year: String = "",
    val order: String = "updated_desc",
    val ratingMin: String = "",
    val kind: String = "",
    val letter: String = ""
)

data class MediaGridAvailableFiltersUi(
    val years: List<Int> = emptyList(),
    val orders: List<MediaGridFilterOptionUi> =
        defaultMediaGridOrderOptions(),
    val ratings: List<MediaGridFilterOptionUi> =
        defaultMediaGridRatingOptions(),
    val kinds: List<MediaGridFilterOptionUi> =
        defaultMediaGridKindOptions()
)

data class MediaGridFilterOptionUi(
    val label: String,
    val value: String
)

private fun defaultMediaGridOrderOptions():
        List<MediaGridFilterOptionUi> {
    return listOf(
        MediaGridFilterOptionUi("Atualizados", "updated_desc"),
        MediaGridFilterOptionUi("Recentes", "created_desc"),
        MediaGridFilterOptionUi("Ano mais novo", "year_desc"),
        MediaGridFilterOptionUi("Ano mais antigo", "year_asc"),
        MediaGridFilterOptionUi("Nome A-Z", "name_asc"),
        MediaGridFilterOptionUi("Nome Z-A", "name_desc"),
        MediaGridFilterOptionUi("Nota maior", "rating_desc")
    )
}

private fun defaultMediaGridRatingOptions():
        List<MediaGridFilterOptionUi> {
    return listOf(
        MediaGridFilterOptionUi("Qualquer", ""),
        MediaGridFilterOptionUi("9+", "9"),
        MediaGridFilterOptionUi("8+", "8"),
        MediaGridFilterOptionUi("7+", "7"),
        MediaGridFilterOptionUi("6+", "6"),
        MediaGridFilterOptionUi("5+", "5"),
        MediaGridFilterOptionUi("4+", "4"),
        MediaGridFilterOptionUi("3+", "3"),
        MediaGridFilterOptionUi("2+", "2"),
        MediaGridFilterOptionUi("1+", "1")
    )
}

private fun defaultMediaGridKindOptions():
        List<MediaGridFilterOptionUi> {
    return listOf(
        MediaGridFilterOptionUi("Todos", ""),
        MediaGridFilterOptionUi("Filmes", "movies"),
        MediaGridFilterOptionUi("Séries", "series")
    )
}
