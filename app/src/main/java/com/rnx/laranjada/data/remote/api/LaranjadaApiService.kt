package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

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

    private suspend fun getJson(path: String): JSONObject {
        return withContext(Dispatchers.IO) {
            val url = URL(ApiConfig.buildUrl(path))
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
}