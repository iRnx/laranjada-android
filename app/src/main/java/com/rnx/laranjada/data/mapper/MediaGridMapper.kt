
package com.rnx.laranjada.data.mapper

import androidx.compose.ui.graphics.Color
import com.rnx.laranjada.core.network.MediaUrlResolver
import com.rnx.laranjada.feature.home.MediaItemUi
import com.rnx.laranjada.feature.mediagrid.MediaGridAppliedFiltersUi
import com.rnx.laranjada.feature.mediagrid.MediaGridAvailableFiltersUi
import com.rnx.laranjada.feature.mediagrid.MediaGridFilterOptionUi
import com.rnx.laranjada.feature.mediagrid.MediaGridUiState
import org.json.JSONArray
import org.json.JSONObject

object MediaGridMapper {

    fun fromJson(
        json: JSONObject,
        fallbackSectionSlug: String
    ): MediaGridUiState {
        val sectionJson =
            json.optJSONObject("section")

        val sectionTitle =
            sectionJson
                ?.optString("title")
                .orEmpty()

        val sectionSlug =
            sectionJson
                ?.optString("slug")
                .orEmpty()

        val contentType =
            sectionJson
                ?.optString("content_type")
                .orEmpty()

        val count =
            json.optInt("count", 0)

        val next =
            json.optStringOrNull("next")

        val previous =
            json.optStringOrNull("previous")

        val appliedFilters =
            mapAppliedFilters(
                json.optJSONObject(
                    "applied_filters"
                )
            )

        val availableFilters =
            mapAvailableFilters(
                json.optJSONObject(
                    "available_filters"
                )
            )

        val results =
            json.optJSONArray("results")

        val items = buildList {
            if (results != null) {
                for (
                index in 0 until results.length()
                ) {
                    val itemJson =
                        results.optJSONObject(index)
                            ?: continue

                    add(
                        itemJson.toMediaItemUi()
                    )
                }
            }
        }

        return MediaGridUiState(
            title =
                sectionTitle.ifBlank {
                    "Ver tudo"
                },

            sectionSlug =
                sectionSlug.ifBlank {
                    fallbackSectionSlug
                },

            contentType =
                contentType,

            totalCount =
                count,

            currentPage =
                1,

            nextUrl =
                next,

            previousUrl =
                previous,

            items =
                items,

            appliedFilters =
                appliedFilters,

            availableFilters =
                availableFilters
        )
    }

    private fun mapAppliedFilters(
        json: JSONObject?
    ): MediaGridAppliedFiltersUi {

        if (json == null) {
            return MediaGridAppliedFiltersUi()
        }

        return MediaGridAppliedFiltersUi(
            q =
                json.optString("q").orEmpty(),

            year =
                json.optString("year").orEmpty(),

            order =
                json.optString("order")
                    .ifBlank {
                        "updated_desc"
                    },

            ratingMin =
                json.optString(
                    "rating_min"
                ).orEmpty(),

            kind =
                json.optString("kind").orEmpty(),

            letter =
                json.optString("letter")
                    .trim()
                    .uppercase()
                    .takeIf {
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
    ): MediaGridAvailableFiltersUi {

        if (json == null) {
            return MediaGridAvailableFiltersUi()
        }

        return MediaGridAvailableFiltersUi(
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

            kinds =
                mapOptions(
                    json.optJSONArray("kinds")
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
    ): List<MediaGridFilterOptionUi> {

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
                    MediaGridFilterOptionUi(
                        label =
                            item.optString(
                                "label"
                            ).orEmpty(),

                        value =
                            item.optString(
                                "value"
                            ).orEmpty()
                    )
                )
            }
        }
    }

    private fun JSONObject.toMediaItemUi():
            MediaItemUi {

        val title =
            optString("title").orEmpty()

        val year =
            optString("year").orEmpty()

        val rating =
            optString("rating").orEmpty()

        val subtitle = buildString {
            if (year.isNotBlank()) {
                append(year)
            }

            if (
                rating.isNotBlank() &&
                rating != "0" &&
                rating != "0.0"
            ) {
                if (isNotBlank()) {
                    append(" • ")
                }

                append(rating)
            }
        }

        return MediaItemUi(
            title = title,

            subtitle = subtitle,

            imageUrl =
                MediaUrlResolver.resolve(
                    optString(
                        "image_url"
                    ).orEmpty()
                ),

            progress = null,

            gradientColors = listOf(
                Color(0xFF151515),
                Color(0xFF050505)
            ),

            uuid =
                optString("uuid").orEmpty(),

            contentType =
                optString(
                    "content_type"
                ).orEmpty()
        )
    }

    private fun JSONObject.optStringOrNull(
        name: String
    ): String? {

        if (isNull(name)) {
            return null
        }

        return optString(name)
            .takeIf {
                it.isNotBlank() &&
                        it.lowercase() != "null"
            }
    }
}
