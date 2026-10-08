
package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.core.network.ApiHttpResponse
import com.rnx.laranjada.domain.model.FavoritesQuery
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.UUID

object FavoritesApiService {

    private const val BASE_PATH =
        "/api/v1/favorites/"

    private const val CSRF_PATH =
        "/api/v1/auth/csrf/"

    suspend fun list(
        query: FavoritesQuery
    ): JSONObject {

        val parameters = linkedMapOf(
            "content_type" to query.contentType,
            "category" to query.category,
            "q" to query.search,
            "year" to query.year,
            "rating_min" to query.ratingMin,
            "letter" to query.letter,
            "order" to query.order,
            "page" to query.page
                .coerceAtLeast(1)
                .toString()
        )

        val pairs = parameters
            .filterValues {
                it.isNotBlank()
            }
            .map { (key, value) ->
                "${encode(key)}=${encode(value)}"
            }
            .toMutableList()

        query.genres.forEach { genreId ->
            if (genreId.isNotBlank()) {
                pairs.add(
                    "genres=${encode(genreId)}"
                )
            }
        }

        val path =
            "$BASE_PATH?${pairs.joinToString("&")}"

        return requireOk(
            ApiHttpClient.get(path)
        )
    }

    suspend fun status(
        contentType: String,
        contentUuid: String
    ): JSONObject {

        val path =
            "${BASE_PATH}status/" +
                    "${validateType(contentType)}/" +
                    "${validateUuid(contentUuid)}/"

        return requireOk(
            ApiHttpClient.get(path)
        )
    }

    suspend fun toggle(
        contentType: String,
        contentUuid: String
    ): JSONObject {

        val path =
            "${BASE_PATH}toggle/" +
                    "${validateType(contentType)}/" +
                    "${validateUuid(contentUuid)}/"

        ensureCsrfAvailable()

        return requireOk(
            ApiHttpClient.postJson(
                path = path,
                body = JSONObject(),
                requiresCsrf = true
            )
        )
    }

    private fun validateType(
        value: String
    ): String {

        return value.trim().lowercase().also {
            require(
                it == "movie" || it == "series"
            ) {
                "Tipo inválido para Favoritos."
            }
        }
    }

    private fun validateUuid(
        value: String
    ): String {

        return value.trim().also { uuid ->
            require(
                runCatching {
                    UUID.fromString(uuid)
                }.isSuccess
            ) {
                "UUID do conteúdo inválido."
            }
        }
    }

    private fun encode(
        value: String
    ): String {

        return URLEncoder.encode(
            value,
            "UTF-8"
        )
    }

    private fun requireOk(
        response: ApiHttpResponse
    ): JSONObject {

        val json = runCatching {
            response.jsonObject()
        }.getOrNull()

        if (
            !response.isSuccessful ||
            json?.optBoolean("ok", false) != true
        ) {
            val reason =
                json?.optString("message")
                    ?.takeIf { it.isNotBlank() }
                    ?: json?.optString("detail")
                        ?.takeIf { it.isNotBlank() }
                    ?: "Não foi possível acessar Favoritos (HTTP ${response.statusCode})."

            throw IOException(reason)
        }

        return json
    }

    private suspend fun ensureCsrfAvailable() {
        if (hasCsrfCookie()) {
            return
        }

        val response =
            ApiHttpClient.get(CSRF_PATH)

        if (
            !response.isSuccessful ||
            !hasCsrfCookie()
        ) {
            throw IOException(
                "Token CSRF indisponível para Favoritos."
            )
        }
    }

    private fun hasCsrfCookie(): Boolean {
        return ApiHttpClient
            .currentCookieHeader()
            .split(";")
            .map { it.trim() }
            .any {
                it.startsWith("csrftoken=") &&
                        it.substringAfter(
                            "csrftoken="
                        ).isNotBlank()
            }
    }
}
