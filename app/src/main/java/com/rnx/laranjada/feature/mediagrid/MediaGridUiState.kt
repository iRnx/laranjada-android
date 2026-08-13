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
    val appliedFilters: MediaGridAppliedFiltersUi = MediaGridAppliedFiltersUi(),
    val availableFilters: MediaGridAvailableFiltersUi = MediaGridAvailableFiltersUi()
) {
    val hasNextPage: Boolean
        get() = !nextUrl.isNullOrBlank()

    val hasPreviousPage: Boolean
        get() = !previousUrl.isNullOrBlank()

    val totalPages: Int
        get() {
            if (totalCount <= 0 || pageSize <= 0) return 1
            return ceil(totalCount.toDouble() / pageSize.toDouble()).toInt().coerceAtLeast(1)
        }
}

data class MediaGridAppliedFiltersUi(
    val q: String = "",
    val year: String = "",
    val order: String = "updated_desc",
    val ratingMin: String = "",
    val kind: String = ""
)

data class MediaGridAvailableFiltersUi(
    val years: List<Int> = emptyList(),
    val orders: List<MediaGridFilterOptionUi> = defaultMediaGridOrderOptions(),
    val ratings: List<MediaGridFilterOptionUi> = defaultMediaGridRatingOptions(),
    val kinds: List<MediaGridFilterOptionUi> = defaultMediaGridKindOptions()
)

data class MediaGridFilterOptionUi(
    val label: String,
    val value: String
)

private fun defaultMediaGridOrderOptions(): List<MediaGridFilterOptionUi> {
    return listOf(
        MediaGridFilterOptionUi(label = "Atualizados", value = "updated_desc"),
        MediaGridFilterOptionUi(label = "Recentes", value = "created_desc"),
        MediaGridFilterOptionUi(label = "Ano mais novo", value = "year_desc"),
        MediaGridFilterOptionUi(label = "Ano mais antigo", value = "year_asc"),
        MediaGridFilterOptionUi(label = "Nome A-Z", value = "name_asc"),
        MediaGridFilterOptionUi(label = "Nome Z-A", value = "name_desc"),
        MediaGridFilterOptionUi(label = "Nota maior", value = "rating_desc")
    )
}

private fun defaultMediaGridRatingOptions(): List<MediaGridFilterOptionUi> {
    return listOf(
        MediaGridFilterOptionUi(label = "Qualquer", value = ""),
        MediaGridFilterOptionUi(label = "9+", value = "9"),
        MediaGridFilterOptionUi(label = "8+", value = "8"),
        MediaGridFilterOptionUi(label = "7+", value = "7"),
        MediaGridFilterOptionUi(label = "6+", value = "6"),
        MediaGridFilterOptionUi(label = "5+", value = "5"),
        MediaGridFilterOptionUi(label = "4+", value = "4"),
        MediaGridFilterOptionUi(label = "3+", value = "3"),
        MediaGridFilterOptionUi(label = "2+", value = "2"),
        MediaGridFilterOptionUi(label = "1+", value = "1")
    )
}

private fun defaultMediaGridKindOptions(): List<MediaGridFilterOptionUi> {
    return listOf(
        MediaGridFilterOptionUi(label = "Todos", value = ""),
        MediaGridFilterOptionUi(label = "Filmes", value = "movies"),
        MediaGridFilterOptionUi(label = "Séries", value = "series")
    )
}