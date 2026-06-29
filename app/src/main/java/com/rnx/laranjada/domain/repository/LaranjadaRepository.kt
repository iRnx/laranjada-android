package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.feature.collections.CollectionAppliedFiltersUi
import com.rnx.laranjada.feature.collections.CollectionDetailUiState
import com.rnx.laranjada.feature.details.DetailUiState
import com.rnx.laranjada.feature.home.HomeUiState

interface LaranjadaRepository {
    suspend fun getHome(): HomeUiState

    suspend fun getDetail(
        contentType: String,
        uuid: String
    ): DetailUiState

    suspend fun getCollectionDetail(
        uuid: String,
        filters: CollectionAppliedFiltersUi = CollectionAppliedFiltersUi()
    ): CollectionDetailUiState
}