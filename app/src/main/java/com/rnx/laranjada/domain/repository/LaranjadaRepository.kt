package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.feature.collections.CollectionAppliedFiltersUi
import com.rnx.laranjada.feature.collections.CollectionDetailUiState
import com.rnx.laranjada.feature.details.DetailUiState
import com.rnx.laranjada.feature.home.HomeUiState
import com.rnx.laranjada.feature.home.MediaItemUi
import com.rnx.laranjada.feature.mediagrid.MediaGridAppliedFiltersUi
import com.rnx.laranjada.feature.mediagrid.MediaGridUiState

interface LaranjadaRepository {
    suspend fun getHome(): HomeUiState

    suspend fun getHomeSection(
        sectionSlug: String,
        page: Int,
        pageSize: Int,
        filters: MediaGridAppliedFiltersUi = MediaGridAppliedFiltersUi()
    ): MediaGridUiState

    suspend fun getDetail(
        contentType: String,
        uuid: String
    ): DetailUiState

    suspend fun getRelatedContent(
        contentType: String,
        uuid: String,
        limit: Int = 20
    ): List<MediaItemUi>

    suspend fun getCollectionDetail(
        uuid: String,
        filters: CollectionAppliedFiltersUi = CollectionAppliedFiltersUi()
    ): CollectionDetailUiState
}