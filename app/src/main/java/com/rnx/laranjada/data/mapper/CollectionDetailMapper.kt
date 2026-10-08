
package com.rnx.laranjada.data.mapper

import androidx.compose.ui.graphics.Color
import com.rnx.laranjada.core.network.MediaUrlResolver
import com.rnx.laranjada.feature.collections.CollectionAppliedFiltersUi
import com.rnx.laranjada.feature.collections.CollectionAvailableFiltersUi
import com.rnx.laranjada.feature.collections.CollectionDetailUiState
import com.rnx.laranjada.feature.collections.CollectionFilterOptionUi
import com.rnx.laranjada.feature.home.MediaItemUi
import org.json.JSONArray
import org.json.JSONObject

object CollectionDetailMapper {

    fun fromJson(
        json: JSONObject
    ): CollectionDetailUiState {

        val collection =
            json.optJSONObject("collection")
                ?: JSONObject()

        return CollectionDetailUiState(
            uuid =
                collection.optNullableString("uuid")
                    ?: "",

            title =
                collection.optNullableString("title")
                    ?: "",

            imageUrl =
                MediaUrlResolver.resolve(
                    collection.optNullableString(
                        "image_url"
                    )
                ),

            movies =
                mapMediaItems(
                    json.optJSONArray("movies"),
                    fallbackContentType = "movie"
                ),

            series =
                mapMediaItems(
                    json.optJSONArray("series"),
                    fallbackContentType = "series"
                ),

            hasMovies =
                json.optBoolean(
                    "has_movies",
                    false
                ),

            hasSeries =
                json.optBoolean(
                    "has_series",
                    false
                ),

            totalMovies =
                json.optInt(
                    "total_movies",
                    0
                ),

            totalSeries =
                json.optInt(
                    "total_series",
                    0
                ),

            totalItems =
                json.optInt(
                    "total_items",
                    0
                ),

            appliedFilters =
                mapAppliedFilters(
                    json.optJSONObject(
                        "applied_filters"
                    )
                ),

            availableFilters =
                mapAvailableFilters(
                    json.optJSONObject(
                        "available_filters"
                    )
                )
        )
    }

    private fun mapMediaItems(
        array: JSONArray?,
        fallbackContentType: String
    ): List<MediaItemUi> {

        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (
            index in 0 until array.length()
            ) {
                val item =
                    array.optJSONObject(index)
                        ?: continue

                val uuid =
                    item.optNullableString("uuid")
                        ?: continue

                val contentType =
                    item.optNullableString(
                        "content_type"
                    ) ?: fallbackContentType

                add(
                    MediaItemUi(
                        title =
                            item.optNullableString(
                                "title"
                            ) ?: "",

                        subtitle =
                            if (
                                contentType == "series"
                            ) {
                                "Série"
                            } else {
                                "Filme"
                            },

                        imageUrl =
                            MediaUrlResolver.resolve(
                                item.optNullableString(
                                    "image_url"
                                )
                            ),

                        progress = null,

                        gradientColors =
                            defaultGradient(index),

                        uuid = uuid,

                        contentType =
                            contentType
                    )
                )
            }
        }
    }

    private fun mapAppliedFilters(
        json: JSONObject?
    ): CollectionAppliedFiltersUi {

        if (json == null) {
            return CollectionAppliedFiltersUi()
        }

        return CollectionAppliedFiltersUi(
            q =
                json.optNullableString("q")
                    ?: "",

            year =
                json.optNullableString("year")
                    ?: "",

            type =
                json.optNullableString("type")
                    ?: "",

            order =
                json.optNullableString("order")
                    ?: "created_desc",

            ratingMin =
                json.optNullableString(
                    "rating_min"
                ) ?: "",

            letter =
                json.optNullableString("letter")
                    ?.trim()
                    ?.uppercase()
                    ?.takeIf {
                        it == "#" ||
                                (
                                        it.length == 1 &&
                                                it[0] in 'A'..'Z'
                                        )
                    }
                    .orEmpty()
        )
    }

    private fun mapAvailableFilters(
        json: JSONObject?
    ): CollectionAvailableFiltersUi {

        if (json == null) {
            return CollectionAvailableFiltersUi()
        }

        return CollectionAvailableFiltersUi(
            years =
                mapYears(
                    json.optJSONArray("years")
                ),

            orders =
                mapOptions(
                    json.optJSONArray("orders")
                ),

            ratings =
                mapOptions(
                    json.optJSONArray("ratings")
                ),

            types =
                mapOptions(
                    json.optJSONArray("types")
                ),

            specificTypes =
                mapOptions(
                    json.optJSONArray(
                        "specific_types"
                    )
                )
        )
    }

    private fun mapYears(
        array: JSONArray?
    ): List<Int> {

        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (
            index in 0 until array.length()
            ) {
                val year =
                    array.optInt(index)

                if (year > 0) {
                    add(year)
                }
            }
        }
    }

    private fun mapOptions(
        array: JSONArray?
    ): List<CollectionFilterOptionUi> {

        if (array == null) {
            return emptyList()
        }

        return buildList {
            for (
            index in 0 until array.length()
            ) {
                val item =
                    array.optJSONObject(index)
                        ?: continue

                add(
                    CollectionFilterOptionUi(
                        label =
                            item.optNullableString(
                                "label"
                            ) ?: "",

                        value =
                            item.optNullableString(
                                "value"
                            ) ?: "",

                        contentType =
                            item.optNullableString(
                                "content_type"
                            )
                    )
                )
            }
        }
    }

    private fun JSONObject.optNullableString(
        name: String
    ): String? {

        if (
            !has(name) ||
            isNull(name)
        ) {
            return null
        }

        return optString(name)
            .trim()
            .ifBlank {
                null
            }
    }

    private fun defaultGradient(
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
            ),
            listOf(
                Color(0xFF4C1D95),
                Color(0xFF111827)
            ),
            listOf(
                Color(0xFF713F12),
                Color(0xFF111827)
            )
        )

        return gradients[
            index % gradients.size
        ]
    }
}
