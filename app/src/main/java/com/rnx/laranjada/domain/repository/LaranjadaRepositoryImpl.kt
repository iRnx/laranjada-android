
package com.rnx.laranjada.data.repository

import androidx.compose.ui.graphics.Color
import com.rnx.laranjada.core.network.MediaUrlResolver
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
import com.rnx.laranjada.feature.home.ContinueWatchingUi
import com.rnx.laranjada.feature.home.HomeUiState
import com.rnx.laranjada.feature.home.MediaItemUi
import com.rnx.laranjada.feature.mediagrid.MediaGridAppliedFiltersUi
import com.rnx.laranjada.feature.mediagrid.MediaGridUiState
import org.json.JSONArray
import org.json.JSONObject

class LaranjadaRepositoryImpl(
    private val apiService: LaranjadaApiService =
        LaranjadaApiService
) : LaranjadaRepository {

    override suspend fun getHome(): HomeUiState {
        return HomeMapper.fromJson(
            apiService.getHome()
        )
    }

    override suspend fun getContinueWatching():
            List<ContinueWatchingUi> {

        val results =
            apiService
                .getContinueWatching()
                .optJSONArray("results")
                ?: return emptyList()

        return mapContinueWatching(results)
    }

    override suspend fun getHomeSection(
        sectionSlug: String,
        page: Int,
        pageSize: Int,
        filters: MediaGridAppliedFiltersUi
    ): MediaGridUiState {

        val json =
            apiService.getHomeSection(
                sectionSlug = sectionSlug,
                page = page,
                pageSize = pageSize,
                q = filters.q,
                year = filters.year,
                order = filters.order,
                ratingMin = filters.ratingMin,
                kind = filters.kind,
                letter = filters.letter
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

        val json =
            when (
                contentType.lowercase()
            ) {
                "series", "serie" ->
                    apiService.getSeriesDetail(uuid)

                else ->
                    apiService.getMovieDetail(uuid)
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

        val json =
            apiService.getRelatedContent(
                contentType = contentType,
                uuid = uuid,
                limit = limit
            )

        return RelatedContentMapper.fromJson(
            json
        )
    }

    override suspend fun getCollectionDetail(
        uuid: String,
        filters: CollectionAppliedFiltersUi
    ): CollectionDetailUiState {

        val json =
            apiService.getCollectionDetail(
                uuid = uuid,
                q = filters.q,
                year = filters.year,
                type = filters.type,
                order = filters.order,
                ratingMin = filters.ratingMin,
                letter = filters.letter
            )

        return CollectionDetailMapper.fromJson(
            json
        )
    }

    private fun mapContinueWatching(
        array: JSONArray
    ): List<ContinueWatchingUi> {

        return buildList {
            for (index in 0 until array.length()) {
                val item =
                    array.optJSONObject(index)
                        ?: continue

                val contentType =
                    item.optString(
                        "content_type"
                    ).trim()

                val contentUuid =
                    item.optString(
                        "content_uuid"
                    ).trim()

                if (
                    contentType.isBlank() ||
                    contentUuid.isBlank()
                ) {
                    continue
                }

                val percent =
                    item.optDouble(
                        "percent",
                        0.0
                    )
                        .toFloat()
                        .coerceIn(0f, 100f)

                add(
                    ContinueWatchingUi(
                        title =
                            item.optString("title"),

                        episodeInfo =
                            item.optString("subtitle"),

                        remainingTime =
                            item.optString(
                                "remaining_label"
                            ),

                        progress =
                            (percent / 100f)
                                .coerceIn(0f, 1f),

                        imageUrl =
                            MediaUrlResolver.resolve(
                                item.optNullableString(
                                    "image_url"
                                )
                            ),

                        gradientColors =
                            continueWatchingGradient(
                                index
                            ),

                        contentType =
                            contentType,

                        contentUuid =
                            contentUuid,

                        seriesUuid =
                            item.optNullableString(
                                "series_uuid"
                            ),

                        positionSeconds =
                            item.optLong(
                                "position_seconds",
                                0L
                            ).coerceAtLeast(0L),

                        durationSeconds =
                            item.optLong(
                                "duration_seconds",
                                0L
                            ).coerceAtLeast(0L),

                        seasonNumber =
                            item.optNullableInt(
                                "season_number"
                            ),

                        episodeNumber =
                            item.optNullableInt(
                                "episode_number"
                            ),

                        lastWatchedAt =
                            item.optString(
                                "last_watched_at"
                            )
                    )
                )
            }
        }
    }

    private fun JSONObject.optNullableString(
        name: String
    ): String? {

        if (!has(name) || isNull(name)) {
            return null
        }

        return optString(name)
            .trim()
            .ifBlank {
                null
            }
    }

    private fun JSONObject.optNullableInt(
        name: String
    ): Int? {

        if (!has(name) || isNull(name)) {
            return null
        }

        return runCatching {
            getInt(name)
        }.getOrNull()
    }

    private fun continueWatchingGradient(
        index: Int
    ): List<Color> {

        val gradients = listOf(
            listOf(
                Color(0xFF08121A),
                Color(0xFF0A0A0B)
            ),
            listOf(
                Color(0xFF1E3A8A),
                Color(0xFF020617)
            ),
            listOf(
                Color(0xFF7F1D1D),
                Color(0xFF111827)
            ),
            listOf(
                Color(0xFF14532D),
                Color(0xFF020617)
            )
        )

        return gradients[
            index % gradients.size
        ]
    }
}
