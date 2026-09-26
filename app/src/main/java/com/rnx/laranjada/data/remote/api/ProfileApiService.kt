package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.core.network.ApiHttpResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ProfileApiException(
    val statusCode: Int,
    val code: String?,
    message: String
) : IOException(message)

object ProfileApiService {

    private const val CSRF_PATH =
        "/api/v1/auth/csrf/"

    private const val PROFILES_PATH =
        "/api/v1/profiles/"

    private const val SELECT_PROFILE_PATH =
        "/api/v1/profiles/select/"

    suspend fun getProfiles(): JSONObject {
        val response =
            ApiHttpClient.get(
                PROFILES_PATH
            )

        if (
            response.statusCode == 200
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível carregar os perfis."
        )
    }

    suspend fun selectProfile(
        profileUuid: String,
        pin: String? = null
    ): JSONObject {
        ensureCsrf()

        val body =
            JSONObject()
                .put(
                    "profile_uuid",
                    profileUuid
                )

        if (
            !pin.isNullOrBlank()
        ) {
            body.put(
                "pin",
                pin.trim()
            )
        }

        val response =
            ApiHttpClient.postJson(
                path = SELECT_PROFILE_PATH,
                body = body,
                requiresCsrf = true
            )

        if (
            response.statusCode == 200
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível selecionar o perfil."
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
                    "Não foi possível preparar a segurança da requisição."
            )
        }
    }

    private fun buildException(
        response: ApiHttpResponse,
        fallbackMessage: String
    ): ProfileApiException {
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

        return ProfileApiException(
            statusCode =
                response.statusCode,
            code = code,
            message = message
        )
    }

    private fun extractMessage(
        json: JSONObject?,
        fallback: String
    ): String {
        if (json == null) {
            return fallback
        }

        val directMessage =
            json.optString(
                "message"
            )
                .ifBlank {
                    json.optString(
                        "detail"
                    )
                }

        if (
            directMessage.isNotBlank()
        ) {
            return directMessage
        }

        val keys =
            json.keys()

        while (
            keys.hasNext()
        ) {
            val key =
                keys.next()

            val value =
                json.opt(
                    key
                )

            when (value) {
                is JSONArray -> {
                    if (
                        value.length() > 0
                    ) {
                        val firstMessage =
                            value.optString(
                                0
                            )

                        if (
                            firstMessage.isNotBlank()
                        ) {
                            return firstMessage
                        }
                    }
                }

                is String -> {
                    if (
                        value.isNotBlank()
                    ) {
                        return value
                    }
                }
            }
        }

        return fallback
    }
}