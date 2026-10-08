
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
    val appliedFilters: CollectionAppliedFiltersUi =
        CollectionAppliedFiltersUi(),
    val availableFilters: CollectionAvailableFiltersUi =
        CollectionAvailableFiltersUi()
) {
    val isEmptyResult: Boolean
        get() = movies.isEmpty() && series.isEmpty()
}

data class CollectionAppliedFiltersUi(
    val q: String = "",
    val year: String = "",
    val type: String = "",
    val order: String = "created_desc",
    val ratingMin: String = "",
    val letter: String = ""
)

data class CollectionAvailableFiltersUi(
    val years: List<Int> = emptyList(),
    val orders: List<CollectionFilterOptionUi> =
        defaultOrderOptions(),
    val ratings: List<CollectionFilterOptionUi> =
        defaultRatingOptions(),
    val types: List<CollectionFilterOptionUi> =
        defaultTypeOptions(),
    val specificTypes: List<CollectionFilterOptionUi> =
        defaultSpecificTypeOptions()
)

data class CollectionFilterOptionUi(
    val label: String,
    val value: String,
    val contentType: String? = null
)

private fun defaultOrderOptions():
        List<CollectionFilterOptionUi> {
    return listOf(
        CollectionFilterOptionUi("Recentes", "created_desc"),
        CollectionFilterOptionUi("Ano mais novo", "year_desc"),
        CollectionFilterOptionUi("Ano mais antigo", "year_asc"),
        CollectionFilterOptionUi("Nome A-Z", "name_asc"),
        CollectionFilterOptionUi("Nome Z-A", "name_desc"),
        CollectionFilterOptionUi("Nota maior", "rating_desc")
    )
}

private fun defaultRatingOptions():
        List<CollectionFilterOptionUi> {
    return listOf(
        CollectionFilterOptionUi("Qualquer", ""),
        CollectionFilterOptionUi("9+", "9"),
        CollectionFilterOptionUi("8+", "8"),
        CollectionFilterOptionUi("7+", "7"),
        CollectionFilterOptionUi("6+", "6"),
        CollectionFilterOptionUi("5+", "5"),
        CollectionFilterOptionUi("4+", "4"),
        CollectionFilterOptionUi("3+", "3"),
        CollectionFilterOptionUi("2+", "2"),
        CollectionFilterOptionUi("1+", "1")
    )
}

private fun defaultTypeOptions():
        List<CollectionFilterOptionUi> {
    return listOf(
        CollectionFilterOptionUi(
            "Todos",
            "",
            "all"
        ),
        CollectionFilterOptionUi(
            "Filmes",
            "movies_only",
            "movie"
        ),
        CollectionFilterOptionUi(
            "Séries",
            "series_only",
            "series"
        )
    )
}

private fun defaultSpecificTypeOptions():
        List<CollectionFilterOptionUi> {
    return listOf(
        CollectionFilterOptionUi(
            "Filmes Humanos",
            "movies",
            "movie"
        ),
        CollectionFilterOptionUi(
            "Filmes em Desenho",
            "cartoons_movies",
            "movie"
        ),
        CollectionFilterOptionUi(
            "Animes Filmes",
            "animes_movies",
            "movie"
        ),
        CollectionFilterOptionUi(
            "Séries Humanas",
            "series",
            "series"
        ),
        CollectionFilterOptionUi(
            "Séries em Desenho",
            "cartoons_series",
            "series"
        ),
        CollectionFilterOptionUi(
            "Séries Doramas",
            "doramas_series",
            "series"
        ),
        CollectionFilterOptionUi(
            "Animes Séries",
            "animes_series",
            "series"
        )
    )
}
