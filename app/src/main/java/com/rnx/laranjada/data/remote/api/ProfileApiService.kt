package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.core.network.ApiHttpResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class ProfileApiException(
    val statusCode: Int,
    val code: String?,
    message: String
) : IOException(
    message
)

object ProfileApiService {

    private const val CSRF_PATH =
        "/api/v1/auth/csrf/"

    private const val PROFILES_PATH =
        "/api/v1/profiles/"

    private const val SELECT_PROFILE_PATH =
        "/api/v1/profiles/select/"

    private const val AVATAR_LIBRARY_PATH =
        "/api/v1/profiles/avatars/"

    suspend fun getProfiles():
            JSONObject {
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
                path =
                    SELECT_PROFILE_PATH,
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
                "Não foi possível selecionar o perfil."
        )
    }

    suspend fun createProfile(
        name: String,
        usePin: Boolean,
        pin: String? = null
    ): JSONObject {
        ensureCsrf()

        val body =
            JSONObject()
                .put(
                    "name",
                    name.trim()
                )
                .put(
                    "use_pin",
                    usePin
                )

        if (
            usePin &&
            !pin.isNullOrBlank()
        ) {
            body.put(
                "pin",
                pin.trim()
            )
        }

        val response =
            ApiHttpClient.postJson(
                path =
                    PROFILES_PATH,
                body =
                    body,
                requiresCsrf =
                    true
            )

        if (
            response.statusCode == 201
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível criar o perfil."
        )
    }

    suspend fun updateProfile(
        profileUuid: String,
        name: String? = null,
        usePin: Boolean? = null,
        pin: String? = null
    ): JSONObject {
        ensureCsrf()

        val body =
            JSONObject()

        if (
            name != null
        ) {
            body.put(
                "name",
                name.trim()
            )
        }

        if (
            usePin != null
        ) {
            body.put(
                "use_pin",
                usePin
            )
        }

        if (
            pin != null
        ) {
            body.put(
                "pin",
                pin.trim()
            )
        }

        if (
            body.length() == 0
        ) {
            throw IllegalArgumentException(
                "Nenhuma alteração foi informada."
            )
        }

        val profilePath =
            "$PROFILES_PATH${profileUuid.trim()}/"

        val response =
            ApiHttpClient.patchJson(
                path =
                    profilePath,
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
                "Não foi possível atualizar o perfil."
        )
    }

    suspend fun deleteProfile(
        profileUuid: String
    ): JSONObject {
        ensureCsrf()

        val profilePath =
            "$PROFILES_PATH${profileUuid.trim()}/"

        val response =
            ApiHttpClient.delete(
                path =
                    profilePath,
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
                "Não foi possível excluir o perfil."
        )
    }

    suspend fun getAvatarLibrary(
        query: String? = null
    ): JSONObject {
        val normalizedQuery =
            query
                ?.trim()
                .orEmpty()

        val path =
            if (
                normalizedQuery.isBlank()
            ) {
                AVATAR_LIBRARY_PATH
            } else {
                val encodedQuery =
                    URLEncoder.encode(
                        normalizedQuery,
                        Charsets.UTF_8.name()
                    )

                "$AVATAR_LIBRARY_PATH?q=$encodedQuery"
            }

        val response =
            ApiHttpClient.get(
                path
            )

        if (
            response.statusCode == 200
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível carregar os avatares."
        )
    }

    /*
     * POST
     * /api/v1/profiles/<uuid>/avatar/
     *
     * Define ou substitui o avatar
     * atual do perfil.
     */
    suspend fun setProfileAvatar(
        profileUuid: String,
        avatarUuid: String
    ): JSONObject {
        ensureCsrf()

        val avatarPath =
            "$PROFILES_PATH${profileUuid.trim()}/avatar/"

        val body =
            JSONObject()
                .put(
                    "avatar_uuid",
                    avatarUuid.trim()
                )

        val response =
            ApiHttpClient.postJson(
                path =
                    avatarPath,
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
                "Não foi possível atualizar o avatar."
        )
    }

    /*
     * DELETE
     * /api/v1/profiles/<uuid>/avatar/
     *
     * Remove o avatar do perfil.
     */
    suspend fun removeProfileAvatar(
        profileUuid: String
    ): JSONObject {
        ensureCsrf()

        val avatarPath =
            "$PROFILES_PATH${profileUuid.trim()}/avatar/"

        val response =
            ApiHttpClient.delete(
                path =
                    avatarPath,
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
                "Não foi possível remover o avatar."
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
                fallback =
                    fallbackMessage
            )

        return ProfileApiException(
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

        val directMessage =
            json.optString(
                "message"
            ).ifBlank {
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

            when (
                value
            ) {
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
                            value.optString(
                                0
                            ).trim()

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