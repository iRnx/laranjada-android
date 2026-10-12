package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.core.network.ApiHttpResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class SupportApiException(
    val statusCode: Int,
    val code: String?,
    val fieldErrors: Map<String, String>,
    message: String
) : IOException(
    message
)

object SupportApiService {

    private const val CSRF_PATH =
        "/api/v1/auth/csrf/"

    private const val SUPPORT_PATH =
        "/api/v1/support/"

    suspend fun getCenter(
        page: Int = 1
    ): JSONObject {

        val safePage =
            page.coerceAtLeast(
                1
            )

        val response =
            ApiHttpClient.get(
                "$SUPPORT_PATH?page=$safePage"
            )

        if (
            response.statusCode == 200
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível carregar a Central de Atendimento."
        )
    }

    suspend fun getConversation(
        conversationUuid: String
    ): JSONObject {

        val uuid =
            conversationUuid.trim()

        require(
            uuid.isNotBlank()
        ) {
            "UUID do atendimento não informado."
        }

        val response =
            ApiHttpClient.get(
                "/api/v1/support/conversations/$uuid/"
            )

        if (
            response.statusCode == 200
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível carregar o atendimento."
        )
    }

    suspend fun createConversation(
        body: JSONObject
    ): JSONObject {

        ensureCsrf()

        val response =
            ApiHttpClient.postJson(
                path =
                    "/api/v1/support/conversations/",

                body =
                    body,

                requiresCsrf =
                    true
            )

        if (
            response.statusCode in
            200..299
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível enviar o atendimento."
        )
    }

    suspend fun sendMessage(
        conversationUuid: String,
        body: JSONObject
    ): JSONObject {

        val uuid =
            conversationUuid.trim()

        require(
            uuid.isNotBlank()
        ) {
            "UUID do atendimento não informado."
        }

        ensureCsrf()

        val response =
            ApiHttpClient.postJson(
                path =
                    "/api/v1/support/conversations/$uuid/messages/",

                body =
                    body,

                requiresCsrf =
                    true
            )

        if (
            response.statusCode in
            200..299
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível enviar a mensagem."
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
    ): SupportApiException {

        val json =
            runCatching {
                response.jsonObject()
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

        val fieldErrors =
            extractFieldErrors(
                json?.optJSONObject(
                    "errors"
                )
            )

        val fieldMessage =
            fieldErrors.values
                .firstOrNull {
                    it.isNotBlank()
                }

        val message =
            fieldMessage
                ?: json
                    ?.optString(
                        "message"
                    )
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                ?: json
                    ?.optString(
                        "detail"
                    )
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                ?: fallbackMessage

        return SupportApiException(
            statusCode =
                response.statusCode,

            code =
                code,

            fieldErrors =
                fieldErrors,

            message =
                message
        )
    }

    private fun extractFieldErrors(
        errors: JSONObject?
    ): Map<String, String> {

        if (
            errors == null
        ) {
            return emptyMap()
        }

        val result =
            linkedMapOf<
                    String,
                    String
                    >()

        val keys =
            errors.keys()

        while (
            keys.hasNext()
        ) {
            val key =
                keys.next()

            val message =
                extractFirstError(
                    errors.opt(
                        key
                    )
                )

            if (
                !message.isNullOrBlank()
            ) {
                result[key] =
                    message
            }
        }

        return result
    }

    private fun extractFirstError(
        value: Any?
    ): String? {

        return when (
            value
        ) {
            null,
            JSONObject.NULL ->
                null

            is String ->
                value
                    .trim()
                    .takeIf {
                        it.isNotBlank()
                    }

            is JSONArray -> {
                var result:
                        String? =
                    null

                for (
                index in
                0 until value.length()
                ) {
                    result =
                        extractFirstError(
                            value.opt(
                                index
                            )
                        )

                    if (
                        !result.isNullOrBlank()
                    ) {
                        break
                    }
                }

                result
            }

            is JSONObject -> {
                val keys =
                    value.keys()

                var result:
                        String? =
                    null

                while (
                    keys.hasNext()
                ) {
                    result =
                        extractFirstError(
                            value.opt(
                                keys.next()
                            )
                        )

                    if (
                        !result.isNullOrBlank()
                    ) {
                        break
                    }
                }

                result
            }

            else ->
                value
                    .toString()
                    .trim()
                    .takeIf {
                        it.isNotBlank()
                    }
        }
    }
}