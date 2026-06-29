package com.rnx.laranjada.data.repository

import com.rnx.laranjada.data.mapper.CollectionDetailMapper
import com.rnx.laranjada.data.mapper.DetailMapper
import com.rnx.laranjada.data.mapper.HomeMapper
import com.rnx.laranjada.data.remote.api.LaranjadaApiService
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import com.rnx.laranjada.feature.collections.CollectionAppliedFiltersUi
import com.rnx.laranjada.feature.collections.CollectionDetailUiState
import com.rnx.laranjada.feature.details.DetailUiState
import com.rnx.laranjada.feature.home.HomeUiState

class LaranjadaRepositoryImpl(
    private val apiService: LaranjadaApiService = LaranjadaApiService
) : LaranjadaRepository {

    override suspend fun getHome(): HomeUiState {
        val json = apiService.getHome()

        return HomeMapper.fromJson(json)
    }

    override suspend fun getDetail(
        contentType: String,
        uuid: String
    ): DetailUiState {
        val json = when (contentType.lowercase()) {
            "series", "serie" -> apiService.getSeriesDetail(uuid)
            else -> apiService.getMovieDetail(uuid)
        }

        return DetailMapper.fromJson(
            json = json,
            requestedContentType = contentType
        )
    }

    override suspend fun getCollectionDetail(
        uuid: String,
        filters: CollectionAppliedFiltersUi
    ): CollectionDetailUiState {
        val json = apiService.getCollectionDetail(
            uuid = uuid,
            q = filters.q,
            year = filters.year,
            type = filters.type,
            order = filters.order,
            ratingMin = filters.ratingMin
        )

        return CollectionDetailMapper.fromJson(json)
    }
}