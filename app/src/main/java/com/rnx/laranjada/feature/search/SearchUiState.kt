package com.rnx.laranjada.feature.search

import com.rnx.laranjada.feature.home.MediaItemUi

data class SearchResponseUi(
    val query: String = "",
    val count: Int = 0,
    val items: List<MediaItemUi> = emptyList()
)

data class SearchUiState(
    val query: String = "",
    val submittedQuery: String = "",
    val totalCount: Int = 0,
    val results: List<MediaItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false
)