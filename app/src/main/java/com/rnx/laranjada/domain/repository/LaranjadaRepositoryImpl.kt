package com.rnx.laranjada.data.repository

import com.rnx.laranjada.data.mapper.CollectionDetailMapper
import com.rnx.laranjada.data.mapper.DetailMapper
import com.rnx.laranjada.data.mapper.HomeMapper
import com.rnx.laranjada.data.mapper.MediaGridMapper
import com.rnx.laranjada.data.mapper.RelatedContentMapper
import com.rnx.laranjada.data.remote.api.LaranjadaApiService
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import com.rnx.laranjada.feature.collections.CollectionAppliedFiltersUi
import com.rnx.laranjada.feature.collections.CollectionDetailUiState
import com.rnx.laranjada.feature.details.DetailUiState
import com.rnx.laranjada.feature.home.HomeUiState
import com.rnx.laranjada.feature.home.MediaItemUi
import com.rnx.laranjada.feature.mediagrid.MediaGridAppliedFiltersUi
import com.rnx.laranjada.feature.mediagrid.MediaGridUiState

class LaranjadaRepositoryImpl(
    private val apiService: LaranjadaApiService = LaranjadaApiService
) : LaranjadaRepository {

    override suspend fun getHome(): HomeUiState {
        val json = apiService.getHome()

        return HomeMapper.fromJson(json)
    }

    override suspend fun getHomeSection(
        sectionSlug: String,
        page: Int,
        pageSize: Int,
        filters: MediaGridAppliedFiltersUi
    ): MediaGridUiState {
        val json = apiService.getHomeSection(
            sectionSlug = sectionSlug,
            page = page,
            pageSize = pageSize,
            q = filters.q,
            year = filters.year,
            order = filters.order,
            ratingMin = filters.ratingMin,
            kind = filters.kind
        )

        return MediaGridMapper.fromJson(
            json = json,
            fallbackSectionSlug = sectionSlug
        )
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

    override suspend fun getRelatedContent(
        contentType: String,
        uuid: String,
        limit: Int
    ): List<MediaItemUi> {
        val json = apiService.getRelatedContent(
            contentType = contentType,
            uuid = uuid,
            limit = limit
        )

        return RelatedContentMapper.fromJson(json)
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