package com.rnx.laranjada.data.mapper

import com.rnx.laranjada.domain.model.ViewerAvatar
import com.rnx.laranjada.domain.model.ViewerProfile
import com.rnx.laranjada.domain.model.ViewerProfileList
import org.json.JSONObject

object ProfileMapper {

    fun fromListResponse(
        response: JSONObject
    ): ViewerProfileList {
        val profilesJson =
            response.optJSONArray(
                "profiles"
            )

        val profiles =
            buildList {
                if (
                    profilesJson != null
                ) {
                    for (
                    index in
                    0 until profilesJson.length()
                    ) {
                        val item =
                            profilesJson
                                .optJSONObject(
                                    index
                                )
                                ?: continue

                        add(
                            fromProfileJson(
                                item
                            )
                        )
                    }
                }
            }

        val selectedProfileUuid =
            if (
                response.isNull(
                    "selected_profile_uuid"
                )
            ) {
                null
            } else {
                response
                    .optString(
                        "selected_profile_uuid"
                    )
                    .trim()
                    .takeIf {
                        it.isNotBlank()
                    }
            }

        return ViewerProfileList(
            selectedProfileUuid =
                selectedProfileUuid,
            profiles =
                profiles
        )
    }

    fun fromSelectResponse(
        response: JSONObject
    ): ViewerProfile {
        return fromProfileResponse(
            response = response,
            missingProfileMessage =
                "A API não retornou o perfil selecionado."
        )
    }

    fun fromMutationResponse(
        response: JSONObject
    ): ViewerProfile {
        return fromProfileResponse(
            response = response,
            missingProfileMessage =
                "A API não retornou o perfil atualizado."
        )
    }

    private fun fromProfileResponse(
        response: JSONObject,
        missingProfileMessage: String
    ): ViewerProfile {
        val profile =
            response.optJSONObject(
                "profile"
            ) ?: error(
                missingProfileMessage
            )

        return fromProfileJson(
            profile
        )
    }

    private fun fromProfileJson(
        json: JSONObject
    ): ViewerProfile {
        val avatarJson =
            if (
                json.isNull(
                    "avatar"
                )
            ) {
                null
            } else {
                json.optJSONObject(
                    "avatar"
                )
            }

        val avatar =
            avatarJson?.let {
                ViewerAvatar(
                    uuid =
                        it.optString(
                            "uuid"
                        ),
                    name =
                        it.optString(
                            "name"
                        ),
                    imageUrl =
                        it.optString(
                            "image_url"
                        )
                            .trim()
                            .takeIf {
                                    url ->

                                url.isNotBlank()
                            }
                )
            }

        return ViewerProfile(
            uuid =
                json.optString(
                    "uuid"
                ),
            name =
                json.optString(
                    "name"
                ),
            hasPin =
                json.optBoolean(
                    "has_pin",
                    false
                ),
            isDefault =
                json.optBoolean(
                    "is_default",
                    false
                ),
            isSelected =
                json.optBoolean(
                    "is_selected",
                    false
                ),
            avatar =
                avatar
        )
    }
}