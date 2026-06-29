package com.rnx.laranjada.data.mapper

import com.rnx.laranjada.feature.details.DetailContentType
import com.rnx.laranjada.feature.details.DetailUiState
import com.rnx.laranjada.feature.details.EpisodeUi
import com.rnx.laranjada.feature.details.SeasonUi
import org.json.JSONArray
import org.json.JSONObject

object DetailMapper {

    fun fromJson(
        json: JSONObject,
        requestedContentType: String
    ): DetailUiState {
        val contentType = json.optNullableString("content_type")
            ?: requestedContentType

        val normalizedContentType = when (contentType.lowercase()) {
            "series", "serie" -> DetailContentType.Series
            else -> DetailContentType.Movie
        }

        val seasons = if (normalizedContentType == DetailContentType.Series) {
            mapSeasons(json.optJSONArray("seasons"))
        } else {
            emptyList()
        }

        return DetailUiState(
            uuid = json.optNullableString("uuid") ?: "",
            contentType = normalizedContentType,
            title = json.optString("title"),
            originalTitle = json.optNullableString("original_title") ?: json.optString("title"),
            year = json.optInt("year").takeIf { it > 0 }?.toString() ?: "",
            endYear = null,
            rating = formatRating(json.opt("rating")),
            genres = mapGenres(json.optJSONArray("genres")),
            durationInfo = if (normalizedContentType == DetailContentType.Series) {
                formatSeasonsCount(seasons.size)
            } else {
                json.optNullableString("runtime") ?: ""
            },
            synopsis = json.optNullableString("sinopse") ?: "",
            imageDetailUrl = json.optNullableString("image_detail_url") ?: "",
            imageThumbUrl = json.optNullableString("image_thumb_url") ?: "",
            hlsUrl = json.optNullableString("hls_url") ?: "",
            hasVideo = json.optBoolean("has_video", false),
            watchProgress = null,
            seasons = seasons
        )
    }

    private fun mapGenres(array: JSONArray?): List<String> {
        if (array == null) return emptyList()

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val name = item.optNullableString("name") ?: continue
                add(name)
            }
        }
    }

    private fun mapSeasons(array: JSONArray?): List<SeasonUi> {
        if (array == null) return emptyList()

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val seasonNumber = item.optInt("number")

                add(
                    SeasonUi(
                        id = item.optInt("id").toString(),
                        number = seasonNumber,
                        episodes = mapEpisodes(item.optJSONArray("episodes"))
                    )
                )
            }
        }
    }

    private fun mapEpisodes(array: JSONArray?): List<EpisodeUi> {
        if (array == null) return emptyList()

        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue

                add(
                    EpisodeUi(
                        id = item.optInt("id").toString(),
                        uuid = item.optString("uuid"),
                        number = item.optInt("episode_number"),
                        title = item.optString("title"),
                        runtime = item.optNullableString("runtime") ?: "",
                        rating = formatRating(item.opt("rating")),
                        synopsis = item.optNullableString("sinopse") ?: "",
                        imageUrl = item.optNullableString("image_url") ?: "",
                        hasVideo = item.optBoolean("has_video", false),
                        hlsUrl = item.optNullableString("hls_url") ?: ""
                    )
                )
            }
        }
    }

    private fun formatSeasonsCount(count: Int): String {
        return when (count) {
            0 -> "Sem temporadas"
            1 -> "1 temporada"
            else -> "$count temporadas"
        }
    }

    private fun formatRating(value: Any?): String {
        if (value == null) return ""

        val text = value.toString()

        return if (text.endsWith(".0")) {
            text.removeSuffix(".0")
        } else {
            text
        }
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (isNull(name)) return null

        val value = optString(name).trim()

        return value.ifBlank { null }
    }
}