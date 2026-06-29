package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object LaranjadaApiService {

    suspend fun getHome(): JSONObject {
        return getJson("/api/v1/home/")
    }

    suspend fun getMovieDetail(uuid: String): JSONObject {
        return getJson("/api/v1/movies/$uuid/")
    }

    suspend fun getSeriesDetail(uuid: String): JSONObject {
        return getJson("/api/v1/series/$uuid/")
    }

    suspend fun getCollectionDetail(
        uuid: String,
        q: String = "",
        year: String = "",
        type: String = "",
        order: String = "created_desc",
        ratingMin: String = ""
    ): JSONObject {
        return getJson(
            path = "/api/v1/collections/$uuid/",
            queryParams = mapOf(
                "q" to q,
                "year" to year,
                "type" to type,
                "order" to order,
                "rating_min" to ratingMin
            )
        )
    }

    private suspend fun getJson(
        path: String,
        queryParams: Map<String, String> = emptyMap()
    ): JSONObject {
        return withContext(Dispatchers.IO) {
            val finalPath = buildPathWithQueryParams(
                path = path,
                queryParams = queryParams
            )

            val url = URL(ApiConfig.buildUrl(finalPath))
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                connection.setRequestProperty("Accept", "application/json")

                val statusCode = connection.responseCode

                val stream = if (statusCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val body = stream.bufferedReader().use { reader ->
                    reader.readText()
                }

                if (statusCode !in 200..299) {
                    throw IOException("Erro HTTP $statusCode: $body")
                }

                JSONObject(body)
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun buildPathWithQueryParams(
        path: String,
        queryParams: Map<String, String>
    ): String {
        val queryString = queryParams
            .filter { (_, value) -> value.isNotBlank() }
            .map { (key, value) ->
                "${encode(key)}=${encode(value)}"
            }
            .joinToString("&")

        if (queryString.isBlank()) {
            return path
        }

        return "$path?$queryString"
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, "UTF-8")
    }
}