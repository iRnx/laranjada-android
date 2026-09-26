package com.rnx.laranjada.core.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

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
            val responseMessage = runCatching {
                val json = JSONObject(responseBody)

                json.optString("message")
                    .ifBlank {
                        json.optString("detail")
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

    private lateinit var cookieStore: SecureCookieStore

    fun initialize(
        context: Context
    ) {
        if (::cookieStore.isInitialized) {
            return
        }

        cookieStore = SecureCookieStore(
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
            val url = URL(
                ApiConfig.buildUrl(path)
            )

            val connection =
                url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = method
                connection.instanceFollowRedirects = false
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                val cookieHeader =
                    cookieStore.buildCookieHeader()

                if (
                    cookieHeader.isNotBlank()
                ) {
                    connection.setRequestProperty(
                        "Cookie",
                        cookieHeader
                    )
                }

                if (requiresCsrf) {
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

                    /*
                     * Importante para Django CSRF,
                     * principalmente em HML/PROD.
                     *
                     * Exemplos:
                     *
                     * local:
                     * http://127.0.0.1:8000
                     *
                     * hml:
                     * https://laranjada.eu
                     *
                     * Nenhum domínio fica hardcoded.
                     */
                    val origin =
                        "${url.protocol}://${url.authority}"

                    connection.setRequestProperty(
                        "Origin",
                        origin
                    )
                }

                if (body != null) {
                    connection.doOutput = true

                    connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                    )

                    connection.outputStream.use { output ->
                        output.write(
                            body.toByteArray(
                                Charsets.UTF_8
                            )
                        )
                    }
                }

                val statusCode =
                    connection.responseCode

                cookieStore.saveFromResponseHeaders(
                    connection.headerFields
                )

                val responseStream = if (
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
                    statusCode = statusCode,
                    body = responseBody
                )
            } finally {
                connection.disconnect()
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