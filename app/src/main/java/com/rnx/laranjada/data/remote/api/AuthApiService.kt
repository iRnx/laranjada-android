package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.core.network.ApiHttpException
import org.json.JSONObject
import java.io.IOException

class AuthApiException(
    message: String
) : IOException(message)

object AuthApiService {

    private const val CSRF_PATH = "/api/v1/auth/csrf/"
    private const val LOGIN_PATH = "/api/v1/auth/login/"
    private const val ME_PATH = "/api/v1/auth/me/"
    private const val LOGOUT_PATH = "/api/v1/auth/logout/"

    suspend fun login(
        usernameOrEmail: String,
        password: String
    ): JSONObject {
        ensureCsrf()

        val body = JSONObject()
            .put(
                "username_or_email",
                usernameOrEmail
            )
            .put(
                "password",
                password
            )

        val response = ApiHttpClient.postJson(
            path = LOGIN_PATH,
            body = body,
            requiresCsrf = true
        )

        return when (response.statusCode) {
            200 -> {
                response.jsonObject()
            }

            400 -> {
                throw AuthApiException(
                    extractMessage(
                        response.body,
                        "Confira os dados informados."
                    )
                )
            }

            401 -> {
                throw AuthApiException(
                    "E-mail, nome de usuário ou senha inválidos."
                )
            }

            403 -> {
                throw AuthApiException(
                    "Não foi possível validar a segurança da requisição. Tente novamente."
                )
            }

            429 -> {
                throw AuthApiException(
                    "Muitas tentativas de login. Aguarde cerca de 1 minuto e tente novamente."
                )
            }

            else -> {
                throw ApiHttpException(
                    statusCode = response.statusCode,
                    responseBody = response.body
                )
            }
        }
    }

    suspend fun getCurrentUser(): JSONObject? {
        val response = ApiHttpClient.get(
            ME_PATH
        )

        return when (response.statusCode) {
            200 -> {
                response.jsonObject()
            }

            401 -> {
                ApiHttpClient.clearAuthenticationCookies()
                null
            }

            else -> {
                throw ApiHttpException(
                    statusCode = response.statusCode,
                    responseBody = response.body
                )
            }
        }
    }

    suspend fun logout() {
        ensureCsrf()

        val response = ApiHttpClient.postJson(
            path = LOGOUT_PATH,
            requiresCsrf = true
        )

        when (response.statusCode) {
            200,
            401 -> {
                ApiHttpClient.clearAuthenticationCookies()
            }

            else -> {
                throw ApiHttpException(
                    statusCode = response.statusCode,
                    responseBody = response.body
                )
            }
        }
    }

    private suspend fun ensureCsrf() {
        val response = ApiHttpClient.get(
            CSRF_PATH
        )

        if (!response.isSuccessful) {
            throw ApiHttpException(
                statusCode = response.statusCode,
                responseBody = response.body
            )
        }
    }

    private fun extractMessage(
        body: String,
        fallback: String
    ): String {
        return runCatching {
            val json = JSONObject(body)

            json.optString("message")
                .ifBlank {
                    json.optString("detail")
                }
                .ifBlank {
                    fallback
                }
        }.getOrDefault(fallback)
    }
}