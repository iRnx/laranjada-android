package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.core.network.ApiHttpResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class PlaybackApiException(
    val statusCode: Int,
    val code: String?,
    message: String
) : IOException(
    message
)

object PlaybackApiService {

    private const val CSRF_PATH =
        "/api/v1/auth/csrf/"

    private const val RESERVE_PATH =
        "/api/v1/playback/reserve/"

    private const val RENEW_PATH =
        "/api/v1/playback/renew/"

    private const val PRESENCE_PATH =
        "/api/v1/playback/presence/"

    private const val STOP_PATH =
        "/api/v1/playback/stop/"

    suspend fun reserve(
        contentType: String,
        contentUuid: String,
        clientSessionKey: String
    ): JSONObject {
        ensureCsrf()

        val body =
            JSONObject()
                .put(
                    "content_type",
                    contentType
                )
                .put(
                    "content_uuid",
                    contentUuid
                )
                .put(
                    "client_session_key",
                    clientSessionKey
                )

        val response =
            ApiHttpClient.postJson(
                path = RESERVE_PATH,
                body = body,
                requiresCsrf = true
            )

        if (
            response.statusCode == 200
        ) {
            val json =
                response.jsonObject()

            if (
                json.optBoolean(
                    "ok",
                    false
                ) &&
                json.optBoolean(
                    "allowed",
                    false
                )
            ) {
                return json
            }
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível autorizar a reprodução."
        )
    }

    suspend fun renew(
        sessionUuid: String
    ): JSONObject {
        ensureCsrf()

        val body =
            JSONObject()
                .put(
                    "session_uuid",
                    sessionUuid
                )

        val response =
            ApiHttpClient.postJson(
                path = RENEW_PATH,
                body = body,
                requiresCsrf = true
            )

        if (
            response.statusCode == 200
        ) {
            val json =
                response.jsonObject()

            if (
                json.optBoolean(
                    "ok",
                    false
                ) &&
                json.optBoolean(
                    "allowed",
                    false
                )
            ) {
                return json
            }
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível renovar a autorização da reprodução."
        )
    }

    suspend fun presence(
        contentType: String,
        contentUuid: String,
        clientSessionKey: String,
        status: String
    ): JSONObject {
        /*
         * O Reserve já preparou CSRF/cookies.
         *
         * Não fazemos GET /csrf/ a cada pulse,
         * pois o Presence pode ocorrer a cada
         * poucos segundos, conforme configuração
         * devolvida pelo backend.
         */
        val body =
            JSONObject()
                .put(
                    "content_type",
                    contentType
                )
                .put(
                    "content_uuid",
                    contentUuid
                )
                .put(
                    "client_session_key",
                    clientSessionKey
                )
                .put(
                    "status",
                    status
                )

        val response =
            ApiHttpClient.postJson(
                path = PRESENCE_PATH,
                body = body,
                requiresCsrf = true
            )

        if (
            response.statusCode == 200
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
            response = response,
            fallbackMessage =
                "Não foi possível atualizar a presença da reprodução."
        )
    }

    suspend fun stop(
        contentType: String,
        contentUuid: String,
        clientSessionKey: String,
        status: String
    ): JSONObject {
        /*
         * Stop não é frequente.
         *
         * Aqui garantimos novamente o CSRF,
         * inclusive depois de reproduções longas.
         */
        ensureCsrf()

        val body =
            JSONObject()
                .put(
                    "content_type",
                    contentType
                )
                .put(
                    "content_uuid",
                    contentUuid
                )
                .put(
                    "client_session_key",
                    clientSessionKey
                )
                .put(
                    "status",
                    status
                )

        val response =
            ApiHttpClient.postJson(
                path = STOP_PATH,
                body = body,
                requiresCsrf = true
            )

        if (
            response.statusCode == 200
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
            response = response,
            fallbackMessage =
                "Não foi possível encerrar a reprodução."
        )
    }

    private suspend fun ensureCsrf() {
        val response =
            ApiHttpClient.get(
                CSRF_PATH
            )

        if (
            !response.isSuccessful
        ) {
            throw buildException(
                response = response,
                fallbackMessage =
                    "Não foi possível preparar a segurança da reprodução."
            )
        }
    }

    private fun buildException(
        response: ApiHttpResponse,
        fallbackMessage: String
    ): PlaybackApiException {
        val json =
            runCatching {
                response.jsonObject()
            }.getOrNull()

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
                json = json,
                fallback = fallbackMessage
            )

        return PlaybackApiException(
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
            json == null
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
            !nestedError.isNullOrBlank()
        ) {
            return nestedError
        }

        return fallback
    }

    private fun extractFirstError(
        errors: JSONObject?
    ): String? {
        if (
            errors == null
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
                        value.length() > 0
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