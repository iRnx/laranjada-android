package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.core.network.ApiHttpResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class WatchingProgressApiException(
    val statusCode: Int,
    val code: String?,
    message: String
) : IOException(
    message
)

object WatchingProgressApiService {

    private const val CSRF_PATH =
        "/api/v1/auth/csrf/"

    private const val PROGRESS_PATH =
        "/api/v1/watching/progress/"

    suspend fun saveProgress(
        contentType: String,
        contentUuid: String,
        positionSeconds: Long,
        durationSeconds: Long,
        status: String,
        forceProgressSave: Boolean
    ): JSONObject {

        /*
         * Normalmente o Player já possui
         * csrftoken porque o Reserve precisou
         * dele.
         *
         * Não queremos fazer um GET /csrf/
         * a cada checkpoint de progresso.
         *
         * Só buscamos um novo token caso
         * realmente não exista cookie CSRF.
         */
        ensureCsrfAvailable()

        val body =
            JSONObject()
                .put(
                    "content_type",
                    contentType
                        .trim()
                        .lowercase()
                )
                .put(
                    "content_uuid",
                    contentUuid.trim()
                )
                .put(
                    "position_seconds",
                    positionSeconds
                        .coerceAtLeast(
                            0L
                        )
                )
                .put(
                    "duration_seconds",
                    durationSeconds
                        .coerceAtLeast(
                            0L
                        )
                )
                .put(
                    "status",
                    status
                        .trim()
                        .lowercase()
                )
                .put(
                    "force_progress_save",
                    forceProgressSave
                )

        val response =
            ApiHttpClient.postJson(
                path =
                    PROGRESS_PATH,

                body =
                    body,

                requiresCsrf =
                    true
            )

        if (
            response.statusCode ==
            200
        ) {
            val json =
                response.jsonObject()

            if (
                json.optBoolean(
                    "ok",
                    false
                )
            ) {
                return json
            }
        }

        throw buildException(
            response =
                response,

            fallbackMessage =
                "Não foi possível salvar o progresso da reprodução."
        )
    }

    private suspend fun ensureCsrfAvailable() {
        if (
            hasCsrfCookie()
        ) {
            return
        }

        val response =
            ApiHttpClient.get(
                CSRF_PATH
            )

        if (
            !response.isSuccessful
        ) {
            throw buildException(
                response =
                    response,

                fallbackMessage =
                    "Não foi possível preparar a segurança para salvar o progresso."
            )
        }

        if (
            !hasCsrfCookie()
        ) {
            throw IOException(
                "O token CSRF não está disponível."
            )
        }
    }

    private fun hasCsrfCookie():
            Boolean {

        val cookieHeader =
            ApiHttpClient
                .currentCookieHeader()

        if (
            cookieHeader.isBlank()
        ) {
            return false
        }

        return cookieHeader
            .split(
                ";"
            )
            .map {
                it.trim()
            }
            .any {
                    cookie ->

                cookie.startsWith(
                    "csrftoken="
                ) &&
                        cookie
                            .substringAfter(
                                "csrftoken=",
                                ""
                            )
                            .isNotBlank()
            }
    }

    private fun buildException(
        response: ApiHttpResponse,
        fallbackMessage: String
    ): WatchingProgressApiException {

        val json =
            runCatching {
                response
                    .jsonObject()
            }
                .getOrNull()

        val code =
            json
                ?.optString(
                    "code"
                )
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val message =
            extractMessage(
                json =
                    json,

                fallback =
                    fallbackMessage
            )

        return WatchingProgressApiException(
            statusCode =
                response.statusCode,

            code =
                code,

            message =
                message
        )
    }

    private fun extractMessage(
        json: JSONObject?,
        fallback: String
    ): String {

        if (
            json ==
            null
        ) {
            return fallback
        }

        val directMessage =
            json
                .optString(
                    "message"
                )
                .trim()

        if (
            directMessage.isNotBlank()
        ) {
            return directMessage
        }

        val directError =
            json
                .optString(
                    "error"
                )
                .trim()

        if (
            directError.isNotBlank()
        ) {
            return directError
        }

        val detail =
            json
                .optString(
                    "detail"
                )
                .trim()

        if (
            detail.isNotBlank()
        ) {
            return detail
        }

        val errors =
            json.optJSONObject(
                "errors"
            )

        val nestedError =
            extractFirstError(
                errors
            )

        if (
            !nestedError
                .isNullOrBlank()
        ) {
            return nestedError
        }

        return fallback
    }

    private fun extractFirstError(
        errors: JSONObject?
    ): String? {

        if (
            errors ==
            null
        ) {
            return null
        }

        val keys =
            errors.keys()

        while (
            keys.hasNext()
        ) {
            val key =
                keys.next()

            when (
                val value =
                    errors.opt(
                        key
                    )
            ) {
                is JSONArray -> {
                    if (
                        value.length() >
                        0
                    ) {
                        val message =
                            value
                                .optString(
                                    0
                                )
                                .trim()

                        if (
                            message.isNotBlank()
                        ) {
                            return message
                        }
                    }
                }

                is String -> {
                    val message =
                        value.trim()

                    if (
                        message.isNotBlank()
                    ) {
                        return message
                    }
                }
            }
        }

        return null
    }
}
