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

data class ApiHttpResponse(val statusCode: Int, val body: String) {
    val isSuccessful: Boolean get() = statusCode in 200..299
    fun jsonObject(): JSONObject = if (body.isBlank()) JSONObject() else JSONObject(body)
    fun requireSuccessJson(): JSONObject {
        if (!isSuccessful) throw ApiHttpException(statusCode, body)
        return jsonObject()
    }
}

class ApiHttpException(
    val statusCode: Int,
    val responseBody: String
) : IOException(buildMessage(statusCode, responseBody)) {
    companion object {
        private fun buildMessage(statusCode: Int, responseBody: String): String {
            val extracted = runCatching {
                val json = JSONObject(responseBody)
                json.optString("message").ifBlank { json.optString("detail") }
            }.getOrNull()
            return extracted?.takeIf { it.isNotBlank() } ?: "Erro HTTP $statusCode."
        }
    }
}

/** Cliente API único para GET/POST/DELETE/PATCH, com troca de domínio segura. */
object ApiHttpClient {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val patchClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    private val originLock = Any()
    private lateinit var cookieStore: SecureCookieStore

    fun initialize(context: Context) = synchronized(originLock) {
        if (!::cookieStore.isInitialized) {
            cookieStore = SecureCookieStore(context.applicationContext)
        }
    }

    fun clearAuthenticationCookies() = synchronized(originLock) {
        cookies().clearAuthentication()
    }

    fun hasSessionCookie(): Boolean = synchronized(originLock) {
        cookies().hasSessionCookie()
    }

    fun currentCookieHeader(): String = synchronized(originLock) {
        cookies().buildCookieHeader()
    }

    /** A limpeza e a troca de origem ocorrem na mesma região crítica. */
    internal fun activateNewDomain(host: String): Boolean = synchronized(originLock) {
        if (ApiConfig.isLocal || ApiConfig.activeDomain == host) {
            return@synchronized false
        }
        cookies().clearForDomainChange()
        ApiConfig.activateDomain(host)
    }

    suspend fun get(path: String): ApiHttpResponse = execute("GET", path)

    suspend fun postJson(
        path: String,
        body: JSONObject? = null,
        requiresCsrf: Boolean = false
    ): ApiHttpResponse = execute("POST", path, body?.toString(), requiresCsrf)

    suspend fun patchJson(
        path: String,
        body: JSONObject,
        requiresCsrf: Boolean = false
    ): ApiHttpResponse = executePatch(path, body, requiresCsrf)

    suspend fun delete(
        path: String,
        requiresCsrf: Boolean = false
    ): ApiHttpResponse = execute("DELETE", path, requiresCsrf = requiresCsrf)

    private fun cookies(): SecureCookieStore {
        check(::cookieStore.isInitialized) {
            "ApiHttpClient.initialize(context) deve ser chamado antes da primeira requisição."
        }
        return cookieStore
    }

    private data class RequestContext(
        val apiBase: String,
        val url: URL,
        val cookieHeader: String,
        val csrfToken: String?
    )

    private fun requestContext(path: String, requiresCsrf: Boolean): RequestContext =
        synchronized(originLock) {
            val base = ApiConfig.BASE_URL
            val normalizedPath = if (path.startsWith('/')) path else "/$path"
            val token = if (requiresCsrf) {
                cookies().getCookie("csrftoken")
                    ?: throw IOException("O token CSRF não está disponível.")
            } else null
            RequestContext(
                apiBase = base,
                url = URL(base + normalizedPath),
                cookieHeader = cookies().buildCookieHeader(),
                csrfToken = token
            )
        }

    private fun assertSameOrigin(request: RequestContext) {
        if (ApiConfig.BASE_URL != request.apiBase) {
            throw IOException("O domínio da API mudou durante a requisição.")
        }
    }

    /** Respostas do host anterior não podem reintroduzir cookies no host novo. */
    private fun captureResponse(
        request: RequestContext,
        headers: Map<String?, List<String>>,
        statusCode: Int
    ) = synchronized(originLock) {
        assertSameOrigin(request)
        cookies().saveFromResponseHeaders(headers)
        DomainRecoveryCoordinator.onApiResponse(request.url, statusCode)
    }

    private fun reportNetworkError(request: RequestContext, exception: IOException) {
        if (ApiConfig.BASE_URL == request.apiBase) {
            DomainRecoveryCoordinator.onApiTransportFailure(request.url, exception)
        }
    }

    private suspend fun execute(
        method: String,
        path: String,
        body: String? = null,
        requiresCsrf: Boolean = false
    ): ApiHttpResponse = withContext(Dispatchers.IO) {
        val request = requestContext(path, requiresCsrf)
        val connection = request.url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            if (request.cookieHeader.isNotBlank()) {
                connection.setRequestProperty("Cookie", request.cookieHeader)
            }
            request.csrfToken?.let { token ->
                connection.setRequestProperty("X-CSRFToken", token)
                connection.setRequestProperty(
                    "Origin", "${request.url.protocol}://${request.url.authority}"
                )
            }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
            assertSameOrigin(request)
            if (body != null) {
                connection.outputStream.use { stream ->
                    stream.write(body.toByteArray(Charsets.UTF_8))
                }
            }
            val statusCode = connection.responseCode
            val input = if (statusCode in 200..299) {
                runCatching { connection.inputStream }.getOrNull()
            } else connection.errorStream
            val response = input?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }.orEmpty()
            captureResponse(request, connection.headerFields, statusCode)
            ApiHttpResponse(statusCode, response)
        } catch (ex: IOException) {
            reportNetworkError(request, ex)
            throw ex
        } finally {
            connection.disconnect()
        }
    }

    /** HttpURLConnection do Android não implementa PATCH: usamos OkHttp. */
    private suspend fun executePatch(
        path: String,
        body: JSONObject,
        requiresCsrf: Boolean
    ): ApiHttpResponse = withContext(Dispatchers.IO) {
        val request = requestContext(path, requiresCsrf)
        val builder = Request.Builder()
            .url(request.url.toString())
            .header("Accept", "application/json")
        if (request.cookieHeader.isNotBlank()) {
            builder.header("Cookie", request.cookieHeader)
        }
        request.csrfToken?.let { token ->
            builder.header("X-CSRFToken", token)
            builder.header("Origin", "${request.url.protocol}://${request.url.authority}")
        }
        val httpRequest = builder.patch(
            body.toString().toRequestBody(jsonMediaType)
        ).build()
        try {
            assertSameOrigin(request)
            patchClient.newCall(httpRequest).execute().use { response ->
                val headers: Map<String?, List<String>> = response.headers
                    .toMultimap().entries.associate { (key, values) ->
                        (key as String?) to values
                    }
                val bodyText = response.body?.string().orEmpty()
                captureResponse(request, headers, response.code)
                ApiHttpResponse(response.code, bodyText)
            }
        } catch (ex: IOException) {
            reportNetworkError(request, ex)
            throw ex
        }
    }
}
