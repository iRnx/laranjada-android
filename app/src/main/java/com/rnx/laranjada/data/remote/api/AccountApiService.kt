package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.core.network.ApiHttpResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AccountApiException(
    val statusCode: Int,
    val code: String?,
    val fieldErrors: Map<String, String>,
    message: String
) : IOException(
    message
)

object AccountApiService {

    private const val CSRF_PATH =
        "/api/v1/auth/csrf/"

    private const val ACCOUNT_PATH =
        "/api/v1/account/"

    private const val DISCONNECT_OTHERS_PATH =
        "/api/v1/account/devices/disconnect-others/"

    private const val DISCONNECT_ALL_PATH =
        "/api/v1/account/devices/disconnect-all/"

    private const val PASSWORD_CHANGE_PATH =
        "/api/v1/account/password/change/"

    suspend fun getOverview():
            JSONObject {

        val response =
            ApiHttpClient.get(
                ACCOUNT_PATH
            )

        if (
            response.statusCode == 200
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível carregar os dados da conta."
        )
    }

    suspend fun disconnectDevice(
        deviceUuid: String
    ): JSONObject {

        val normalizedUuid =
            deviceUuid.trim()

        require(
            normalizedUuid.isNotBlank()
        ) {
            "UUID do dispositivo não informado."
        }

        ensureCsrf()

        val response =
            ApiHttpClient.postJson(
                path =
                    "/api/v1/account/devices/$normalizedUuid/disconnect/",

                requiresCsrf =
                    true
            )

        if (
            response.statusCode == 200
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível desconectar o dispositivo."
        )
    }

    suspend fun disconnectOthers():
            JSONObject {

        ensureCsrf()

        val response =
            ApiHttpClient.postJson(
                path =
                    DISCONNECT_OTHERS_PATH,

                requiresCsrf =
                    true
            )

        if (
            response.statusCode == 200
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível desconectar os outros dispositivos."
        )
    }

    suspend fun disconnectAll():
            JSONObject {

        ensureCsrf()

        val response =
            ApiHttpClient.postJson(
                path =
                    DISCONNECT_ALL_PATH,

                requiresCsrf =
                    true
            )

        if (
            response.statusCode == 200
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível desconectar os dispositivos."
        )
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        newPasswordConfirmation: String
    ): JSONObject {

        ensureCsrf()

        val body =
            JSONObject()
                .put(
                    "current_password",
                    currentPassword
                )
                .put(
                    "new_password",
                    newPassword
                )
                .put(
                    "new_password_confirmation",
                    newPasswordConfirmation
                )

        val response =
            ApiHttpClient.postJson(
                path =
                    PASSWORD_CHANGE_PATH,

                body =
                    body,

                requiresCsrf =
                    true
            )

        if (
            response.statusCode == 200
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível alterar a senha."
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
    ): AccountApiException {

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
                    ?.takeIf {
                        it.isNotBlank()
                    }
                ?: json
                    ?.optString(
                        "detail"
                    )
                    ?.takeIf {
                        it.isNotBlank()
                    }
                ?: fallbackMessage

        return AccountApiException(
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