package com.rnx.laranjada.data.mapper

import androidx.compose.ui.graphics.Color
import com.rnx.laranjada.core.network.MediaUrlResolver
import com.rnx.laranjada.feature.home.MediaItemUi
import com.rnx.laranjada.feature.search.SearchResponseUi
import org.json.JSONObject

object SearchMapper {

    fun fromJson(
        json: JSONObject
    ): SearchResponseUi {

        val results =
            json.optJSONArray(
                "results"
            )

        val items =
            buildList {
                if (results != null) {
                    for (
                    index in 0 until results.length()
                    ) {
                        val item =
                            results.optJSONObject(
                                index
                            )
                                ?: continue

                        add(
                            item.toMediaItemUi()
                        )
                    }
                }
            }

        val count =
            if (json.has("count")) {
                json.optInt(
                    "count",
                    items.size
                )
            } else {
                items.size
            }

        return SearchResponseUi(
            query =
                json.optString(
                    "query"
                )
                    .trim(),

            count =
                count.coerceAtLeast(0),

            /*
             * Não ordenamos nada aqui.
             *
             * A ordem do JSONArray é
             * exatamente a relevância
             * calculada pelo Django.
             */
            items =
                items
        )
    }

    private fun JSONObject.toMediaItemUi():
            MediaItemUi {

        val title =
            optString(
                "title"
            )
                .trim()

        val year =
            if (
                has("year") &&
                !isNull("year")
            ) {
                optString(
                    "year"
                )
                    .trim()
            } else {
                ""
            }

        val rating =
            if (
                has("rating") &&
                !isNull("rating")
            ) {
                optString(
                    "rating"
                )
                    .trim()
            } else {
                ""
            }

        val subtitle =
            buildString {
                if (year.isNotBlank()) {
                    append(
                        year
                    )
                }

                if (
                    rating.isNotBlank() &&
                    rating != "0" &&
                    rating != "0.0"
                ) {
                    if (isNotBlank()) {
                        append(
                            " • "
                        )
                    }

                    append(
                        rating
                    )
                }
            }

        return MediaItemUi(
            title =
                title,

            subtitle =
                subtitle,

            imageUrl =
                MediaUrlResolver.resolve(
                    optString(
                        "image_url"
                    )
                ),

            progress =
                null,

            gradientColors =
                listOf(
                    Color(
                        0xFF151515
                    ),

                    Color(
                        0xFF050505
                    )
                ),

            uuid =
                optString(
                    "uuid"
                )
                    .trim(),

            contentType =
                optString(
                    "content_type"
                )
                    .trim()
        )
    }
}