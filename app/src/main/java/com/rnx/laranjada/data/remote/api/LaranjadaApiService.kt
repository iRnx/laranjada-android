package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiHttpClient
import org.json.JSONObject
import java.net.URLEncoder

object LaranjadaApiService {

    suspend fun getHome(): JSONObject {
        return getJson(
            "/api/v1/home/"
        )
    }

    suspend fun getContinueWatching(): JSONObject {
        return getJson(
            "/api/v1/watching/continue/"
        )
    }

    suspend fun search(
        query: String
    ): JSONObject {
        return getJson(
            path =
                "/api/v1/search/",

            queryParams =
                mapOf(
                    "q" to
                            query.trim()
                )
        )
    }

    suspend fun getHomeSection(
        sectionSlug: String,
        page: Int,
        pageSize: Int,
        q: String = "",
        year: String = "",
        order: String = "updated_desc",
        ratingMin: String = "",
        kind: String = "",
        letter: String = ""
    ): JSONObject {
        return getJson(
            path = "/api/v1/home/sections/$sectionSlug/",
            queryParams = mapOf(
                "page" to page.toString(),
                "page_size" to pageSize.toString(),
                "q" to q,
                "year" to year,
                "order" to order,
                "rating_min" to ratingMin,
                "kind" to kind,
                "letter" to normalizeLetter(letter)
            )
        )
    }

    suspend fun getMovieDetail(
        uuid: String
    ): JSONObject {
        return getJson(
            "/api/v1/movies/$uuid/"
        )
    }

    suspend fun getSeriesDetail(
        uuid: String
    ): JSONObject {
        return getJson(
            "/api/v1/series/$uuid/"
        )
    }

    suspend fun getRelatedContent(
        contentType: String,
        uuid: String,
        limit: Int = 20
    ): JSONObject {
        val normalizedContentType =
            contentType.lowercase()

        val path =
            when (normalizedContentType) {
                "series",
                "serie" ->
                    "/api/v1/series/$uuid/related/"

                else ->
                    "/api/v1/movies/$uuid/related/"
            }

        return getJson(
            path = path,

            queryParams = mapOf(
                "limit" to
                        limit.toString()
            )
        )
    }

    suspend fun getCollectionDetail(
        uuid: String,
        q: String = "",
        year: String = "",
        type: String = "",
        order: String = "created_desc",
        ratingMin: String = "",
        letter: String = ""
    ): JSONObject {
        return getJson(
            path =
                "/api/v1/collections/$uuid/",

            queryParams =
                mapOf(
                    "q" to q,
                    "year" to year,
                    "type" to type,
                    "order" to order,
                    "rating_min" to ratingMin,
                    "letter" to normalizeLetter(letter)
                )
        )
    }

    private fun normalizeLetter(
        value: String
    ): String {
        val normalized =
            value
                .trim()
                .uppercase()

        return when {
            normalized == "#" ->
                "#"

            normalized.length == 1 &&
                    normalized[0] in 'A'..'Z' ->
                normalized

            else ->
                ""
        }
    }

    private suspend fun getJson(
        path: String,
        queryParams: Map<String, String> =
            emptyMap()
    ): JSONObject {
        val finalPath =
            buildPathWithQueryParams(
                path =
                    path,

                queryParams =
                    queryParams
            )

        val response =
            ApiHttpClient.get(
                finalPath
            )

        return response
            .requireSuccessJson()
    }

    private fun buildPathWithQueryParams(
        path: String,
        queryParams: Map<String, String>
    ): String {
        val queryString =
            queryParams
                .filter {
                        (_, value) ->

                    value.isNotBlank()
                }
                .map {
                        (key, value) ->

                    "${encode(key)}=${encode(value)}"
                }
                .joinToString(
                    "&"
                )

        if (
            queryString.isBlank()
        ) {
            return path
        }

        return "$path?$queryString"
    }

    private fun encode(
        value: String
    ): String {
        return URLEncoder.encode(
            value,
            "UTF-8"
        )
    }
}