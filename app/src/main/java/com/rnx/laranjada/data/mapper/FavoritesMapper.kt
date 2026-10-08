
package com.rnx.laranjada.data.mapper

import com.rnx.laranjada.core.network.MediaUrlResolver
import com.rnx.laranjada.domain.model.Favorite
import com.rnx.laranjada.domain.model.FavoriteGenre
import com.rnx.laranjada.domain.model.FavoriteStatus
import com.rnx.laranjada.domain.model.FavoritesPage
import org.json.JSONArray
import org.json.JSONObject

object FavoritesMapper {

    fun page(
        json: JSONObject
    ): FavoritesPage {

        val results =
            json.optJSONArray("results")
                ?: JSONArray()

        val items = buildList {
            for (i in 0 until results.length()) {
                val obj =
                    results.optJSONObject(i)
                        ?: continue

                val type =
                    obj.optString("content_type")
                        .trim()
                        .lowercase()

                val uuid =
                    obj.optString("content_uuid")
                        .trim()

                if (
                    uuid.isBlank() ||
                    type !in setOf(
                        "movie",
                        "series"
                    )
                ) {
                    continue
                }

                add(
                    Favorite(
                        favoriteUuid =
                            obj.optString("favorite_uuid"),

                        contentType =
                            type,

                        contentUuid =
                            uuid,

                        title =
                            obj.optString("title"),

                        originalTitle =
                            obj.optString("original_title"),

                        categoryType =
                            obj.optString("type"),

                        year =
                            obj.optNullableInt("year"),

                        rating =
                            obj.optNullableDouble("rating"),

                        imageUrl =
                            MediaUrlResolver.resolve(
                                obj.optString("image_url")
                            ),

                        isFavorite =
                            obj.optBoolean(
                                "is_favorite",
                                true
                            ),

                        canWatch =
                            obj.optBoolean(
                                "can_watch",
                                false
                            ),

                        isLocked =
                            obj.optBoolean(
                                "is_locked",
                                false
                            ),

                        createdAt =
                            obj.optString("created_at")
                    )
                )
            }
        }

        val yearsJson =
            json.optJSONArray("available_years")
                ?: JSONArray()

        val years = buildList {
            for (i in 0 until yearsJson.length()) {
                val value =
                    yearsJson.optInt(i, 0)

                if (value > 0) {
                    add(value)
                }
            }
        }

        val genresJson =
            json.optJSONArray("available_genres")
                ?: JSONArray()

        val genres = buildList {
            for (i in 0 until genresJson.length()) {
                val genre =
                    genresJson.optJSONObject(i)
                        ?: continue

                val id =
                    genre.optString("id")
                        .trim()

                val name =
                    genre.optString("name")
                        .trim()

                if (
                    id.isNotBlank() &&
                    name.isNotBlank()
                ) {
                    add(
                        FavoriteGenre(
                            id = id,
                            name = name
                        )
                    )
                }
            }
        }

        return FavoritesPage(
            profileUuid =
                json.optString("profile_uuid"),

            totalCount =
                json.optInt(
                    "count",
                    items.size
                ).coerceAtLeast(0),

            totalFavorites =
                json.optInt(
                    "total_favorites",
                    items.size
                ).coerceAtLeast(0),

            page =
                json.optInt(
                    "page",
                    1
                ).coerceAtLeast(1),

            pageSize =
                json.optInt(
                    "page_size",
                    60
                ).coerceAtLeast(1),

            totalPages =
                json.optInt(
                    "total_pages",
                    1
                ).coerceAtLeast(1),

            nextPage =
                json.optPage("next_page"),

            previousPage =
                json.optPage("previous_page"),

            availableYears =
                years,

            availableGenres =
                genres,

            items =
                items
        )
    }

    fun status(
        json: JSONObject
    ): FavoriteStatus {

        return FavoriteStatus(
            contentType =
                json.optString("content_type"),

            contentUuid =
                json.optString("content_uuid"),

            isFavorite =
                json.optBoolean(
                    "is_favorite",
                    false
                )
        )
    }

    private fun JSONObject.optPage(
        key: String
    ): Int? {

        if (!has(key) || isNull(key)) {
            return null
        }

        return optInt(key, 0)
            .takeIf { it > 0 }
    }

    private fun JSONObject.optNullableInt(
        key: String
    ): Int? {

        if (!has(key) || isNull(key)) {
            return null
        }

        return optString(key).toIntOrNull()
    }

    private fun JSONObject.optNullableDouble(
        key: String
    ): Double? {

        if (!has(key) || isNull(key)) {
            return null
        }

        return optString(key).toDoubleOrNull()
    }
}
