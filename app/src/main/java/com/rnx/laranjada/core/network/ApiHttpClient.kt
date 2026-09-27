package com.rnx.laranjada.core.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

data class ApiHttpResponse(
    val statusCode: Int,
    val body: String
) {
    val isSuccessful: Boolean
        get() = statusCode in 200..299

    fun jsonObject(): JSONObject {
        if (body.isBlank()) {
            return JSONObject()
        }

        return JSONObject(body)
    }

    fun requireSuccessJson(): JSONObject {
        if (!isSuccessful) {
            throw ApiHttpException(
                statusCode = statusCode,
                responseBody = body
            )
        }

        return jsonObject()
    }
}

class ApiHttpException(
    val statusCode: Int,
    val responseBody: String
) : IOException(
    buildMessage(
        statusCode = statusCode,
        responseBody = responseBody
    )
) {
    companion object {

        private fun buildMessage(
            statusCode: Int,
            responseBody: String
        ): String {
            val responseMessage =
                runCatching {
                    val json =
                        JSONObject(
                            responseBody
                        )

                    json.optString(
                        "message"
                    ).ifBlank {
                        json.optString(
                            "detail"
                        )
                    }
                }.getOrNull()

            return responseMessage
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Erro HTTP $statusCode."
        }
    }
}

object ApiHttpClient {

    private val jsonMediaType =
        "application/json; charset=utf-8"
            .toMediaType()

    private val patchHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(
                15,
                TimeUnit.SECONDS
            )
            .readTimeout(
                15,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                15,
                TimeUnit.SECONDS
            )
            .followRedirects(
                false
            )
            .followSslRedirects(
                false
            )
            .build()

    private lateinit var cookieStore:
            SecureCookieStore

    fun initialize(
        context: Context
    ) {
        if (
            ::cookieStore.isInitialized
        ) {
            return
        }

        cookieStore =
            SecureCookieStore(
                context.applicationContext
            )
    }

    suspend fun get(
        path: String
    ): ApiHttpResponse {
        return execute(
            method = "GET",
            path = path
        )
    }

    suspend fun postJson(
        path: String,
        body: JSONObject? = null,
        requiresCsrf: Boolean = false
    ): ApiHttpResponse {
        return execute(
            method = "POST",
            path = path,
            body = body?.toString(),
            requiresCsrf = requiresCsrf
        )
    }

    suspend fun patchJson(
        path: String,
        body: JSONObject,
        requiresCsrf: Boolean = false
    ): ApiHttpResponse {
        return executePatch(
            path = path,
            body = body,
            requiresCsrf = requiresCsrf
        )
    }

    suspend fun delete(
        path: String,
        requiresCsrf: Boolean = false
    ): ApiHttpResponse {
        return execute(
            method = "DELETE",
            path = path,
            requiresCsrf = requiresCsrf
        )
    }

    fun clearAuthenticationCookies() {
        ensureInitialized()

        cookieStore.clearAuthentication()
    }

    fun hasSessionCookie(): Boolean {
        ensureInitialized()

        return cookieStore.hasSessionCookie()
    }

    fun currentCookieHeader(): String {
        ensureInitialized()

        return cookieStore.buildCookieHeader()
    }

    private suspend fun execute(
        method: String,
        path: String,
        body: String? = null,
        requiresCsrf: Boolean = false
    ): ApiHttpResponse {
        ensureInitialized()

        return withContext(
            Dispatchers.IO
        ) {
            val url =
                URL(
                    ApiConfig.buildUrl(
                        path
                    )
                )

            val connection =
                url.openConnection()
                        as HttpURLConnection

            try {
                connection.requestMethod =
                    method

                connection.instanceFollowRedirects =
                    false

                connection.connectTimeout =
                    15_000

                connection.readTimeout =
                    15_000

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                val cookieHeader =
                    cookieStore
                        .buildCookieHeader()

                if (
                    cookieHeader.isNotBlank()
                ) {
                    connection.setRequestProperty(
                        "Cookie",
                        cookieHeader
                    )
                }

                if (
                    requiresCsrf
                ) {
                    val csrfToken =
                        cookieStore.getCookie(
                            "csrftoken"
                        )

                    if (
                        csrfToken.isNullOrBlank()
                    ) {
                        throw IOException(
                            "O token CSRF não está disponível."
                        )
                    }

                    connection.setRequestProperty(
                        "X-CSRFToken",
                        csrfToken
                    )

                    val origin =
                        "${url.protocol}://${url.authority}"

                    connection.setRequestProperty(
                        "Origin",
                        origin
                    )
                }

                if (
                    body != null
                ) {
                    connection.doOutput =
                        true

                    connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                    )

                    connection
                        .outputStream
                        .use { output ->
                            output.write(
                                body.toByteArray(
                                    Charsets.UTF_8
                                )
                            )
                        }
                }

                val statusCode =
                    connection.responseCode

                cookieStore
                    .saveFromResponseHeaders(
                        connection.headerFields
                    )

                val responseStream =
                    if (
                        statusCode in 200..299
                    ) {
                        runCatching {
                            connection.inputStream
                        }.getOrNull()
                    } else {
                        connection.errorStream
                    }

                val responseBody =
                    responseStream
                        ?.bufferedReader(
                            Charsets.UTF_8
                        )
                        ?.use { reader ->
                            reader.readText()
                        }
                        .orEmpty()

                ApiHttpResponse(
                    statusCode =
                        statusCode,
                    body =
                        responseBody
                )
            } finally {
                connection.disconnect()
            }
        }
    }

    private suspend fun executePatch(
        path: String,
        body: JSONObject,
        requiresCsrf: Boolean
    ): ApiHttpResponse {
        ensureInitialized()

        return withContext(
            Dispatchers.IO
        ) {
            val url =
                URL(
                    ApiConfig.buildUrl(
                        path
                    )
                )

            val requestBuilder =
                Request.Builder()
                    .url(
                        url.toString()
                    )
                    .header(
                        "Accept",
                        "application/json"
                    )

            val cookieHeader =
                cookieStore
                    .buildCookieHeader()

            if (
                cookieHeader.isNotBlank()
            ) {
                requestBuilder.header(
                    "Cookie",
                    cookieHeader
                )
            }

            if (
                requiresCsrf
            ) {
                val csrfToken =
                    cookieStore.getCookie(
                        "csrftoken"
                    )

                if (
                    csrfToken.isNullOrBlank()
                ) {
                    throw IOException(
                        "O token CSRF não está disponível."
                    )
                }

                requestBuilder.header(
                    "X-CSRFToken",
                    csrfToken
                )

                val origin =
                    "${url.protocol}://${url.authority}"

                requestBuilder.header(
                    "Origin",
                    origin
                )
            }

            val requestBody =
                body
                    .toString()
                    .toRequestBody(
                        jsonMediaType
                    )

            val request =
                requestBuilder
                    .patch(
                        requestBody
                    )
                    .build()

            patchHttpClient
                .newCall(
                    request
                )
                .execute()
                .use { response ->

                    val responseHeaders:
                            Map<String?, List<String>> =
                        response
                            .headers
                            .toMultimap()
                            .entries
                            .associate {
                                    entry ->

                                (
                                        entry.key as String?
                                        ) to entry.value
                            }

                    cookieStore
                        .saveFromResponseHeaders(
                            responseHeaders
                        )

                    ApiHttpResponse(
                        statusCode =
                            response.code,
                        body =
                            response.body
                                ?.string()
                                .orEmpty()
                    )
                }
        }
    }

    private fun ensureInitialized() {
        check(
            ::cookieStore.isInitialized
        ) {
            "ApiHttpClient.initialize(context) precisa ser chamado antes das requisições."
        }
    }
}