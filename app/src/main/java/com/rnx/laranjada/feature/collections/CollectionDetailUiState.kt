package com.rnx.laranjada.feature.collections

import com.rnx.laranjada.feature.home.MediaItemUi

data class CollectionDetailUiState(
    val uuid: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val movies: List<MediaItemUi> = emptyList(),
    val series: List<MediaItemUi> = emptyList(),
    val hasMovies: Boolean = false,
    val hasSeries: Boolean = false,
    val totalMovies: Int = 0,
    val totalSeries: Int = 0,
    val totalItems: Int = 0,
    val appliedFilters: CollectionAppliedFiltersUi = CollectionAppliedFiltersUi(),
    val availableFilters: CollectionAvailableFiltersUi = CollectionAvailableFiltersUi()
) {
    val isEmptyResult: Boolean
        get() = movies.isEmpty() && series.isEmpty()
}

data class CollectionAppliedFiltersUi(
    val q: String = "",
    val year: String = "",
    val type: String = "",
    val order: String = "created_desc",
    val ratingMin: String = ""
)

data class CollectionAvailableFiltersUi(
    val years: List<Int> = emptyList(),
    val orders: List<CollectionFilterOptionUi> = defaultOrderOptions(),
    val ratings: List<CollectionFilterOptionUi> = defaultRatingOptions(),
    val types: List<CollectionFilterOptionUi> = defaultTypeOptions(),
    val specificTypes: List<CollectionFilterOptionUi> = defaultSpecificTypeOptions()
)

data class CollectionFilterOptionUi(
    val label: String,
    val value: String,
    val contentType: String? = null
)

private fun defaultOrderOptions(): List<CollectionFilterOptionUi> {
    return listOf(
        CollectionFilterOptionUi(label = "Recentes", value = "created_desc"),
        CollectionFilterOptionUi(label = "Ano mais novo", value = "year_desc"),
        CollectionFilterOptionUi(label = "Ano mais antigo", value = "year_asc"),
        CollectionFilterOptionUi(label = "Nome A-Z", value = "name_asc"),
        CollectionFilterOptionUi(label = "Nome Z-A", value = "name_desc"),
        CollectionFilterOptionUi(label = "Nota maior", value = "rating_desc")
    )
}

private fun defaultRatingOptions(): List<CollectionFilterOptionUi> {
    return listOf(
        CollectionFilterOptionUi(label = "Qualquer", value = ""),
        CollectionFilterOptionUi(label = "9+", value = "9"),
        CollectionFilterOptionUi(label = "8+", value = "8"),
        CollectionFilterOptionUi(label = "7+", value = "7"),
        CollectionFilterOptionUi(label = "6+", value = "6"),
        CollectionFilterOptionUi(label = "5+", value = "5"),
        CollectionFilterOptionUi(label = "4+", value = "4"),
        CollectionFilterOptionUi(label = "3+", value = "3"),
        CollectionFilterOptionUi(label = "2+", value = "2"),
        CollectionFilterOptionUi(label = "1+", value = "1")
    )
}

private fun defaultTypeOptions(): List<CollectionFilterOptionUi> {
    return listOf(
        CollectionFilterOptionUi(label = "Todos", value = "", contentType = "all"),
        CollectionFilterOptionUi(label = "Filmes", value = "movies_only", contentType = "movie"),
        CollectionFilterOptionUi(label = "Séries", value = "series_only", contentType = "series")
    )
}

private fun defaultSpecificTypeOptions(): List<CollectionFilterOptionUi> {
    return listOf(
        CollectionFilterOptionUi(label = "Filmes Humanos", value = "movies", contentType = "movie"),
        CollectionFilterOptionUi(label = "Filmes em Desenho", value = "cartoons_movies", contentType = "movie"),
        CollectionFilterOptionUi(label = "Animes Filmes", value = "animes_movies", contentType = "movie"),
        CollectionFilterOptionUi(label = "Séries Humanas", value = "series", contentType = "series"),
        CollectionFilterOptionUi(label = "Séries em Desenho", value = "cartoons_series", contentType = "series"),
        CollectionFilterOptionUi(label = "Séries Doramas", value = "doramas_series", contentType = "series"),
        CollectionFilterOptionUi(label = "Animes Séries", value = "animes_series", contentType = "series")
    )
}