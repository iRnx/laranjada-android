package com.rnx.laranjada.data.mapper

import com.rnx.laranjada.domain.model.ViewerAvatar
import com.rnx.laranjada.domain.model.ViewerAvatarGroup
import com.rnx.laranjada.domain.model.ViewerAvatarLibrary
import com.rnx.laranjada.domain.model.ViewerAvatarLibraryItem
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

        val maxProfiles =
            response.optInt(
                "max_profiles",
                0
            )

        val activeProfilesCount =
            response.optInt(
                "active_profiles_count",
                profiles.size
            )

        val remainingProfiles =
            response.optInt(
                "remaining_profiles",
                0
            )

        val canCreateProfile =
            response.optBoolean(
                "can_create_profile",
                false
            )

        return ViewerProfileList(
            selectedProfileUuid =
                selectedProfileUuid,
            profiles =
                profiles,
            maxProfiles =
                maxProfiles,
            activeProfilesCount =
                activeProfilesCount,
            remainingProfiles =
                remainingProfiles,
            canCreateProfile =
                canCreateProfile
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

    fun fromAvatarLibraryResponse(
        response: JSONObject
    ): ViewerAvatarLibrary {
        val groupsJson =
            response.optJSONArray(
                "groups"
            )

        val groups =
            buildList {
                if (
                    groupsJson != null
                ) {
                    for (
                    index in
                    0 until groupsJson.length()
                    ) {
                        val groupJson =
                            groupsJson
                                .optJSONObject(
                                    index
                                )
                                ?: continue

                        val avatarsJson =
                            groupJson
                                .optJSONArray(
                                    "avatars"
                                )

                        val avatars =
                            buildList {
                                if (
                                    avatarsJson != null
                                ) {
                                    for (
                                    avatarIndex in
                                    0 until avatarsJson.length()
                                    ) {
                                        val avatarJson =
                                            avatarsJson
                                                .optJSONObject(
                                                    avatarIndex
                                                )
                                                ?: continue

                                        add(
                                            fromAvatarLibraryItemJson(
                                                avatarJson
                                            )
                                        )
                                    }
                                }
                            }

                        add(
                            ViewerAvatarGroup(
                                uuid =
                                    groupJson.optString(
                                        "uuid"
                                    ),
                                name =
                                    groupJson.optString(
                                        "name"
                                    ),
                                slug =
                                    groupJson.optString(
                                        "slug"
                                    ),
                                avatars =
                                    avatars
                            )
                        )
                    }
                }
            }

        val ungroupedJson =
            response.optJSONArray(
                "ungrouped_avatars"
            )

        val ungroupedAvatars =
            buildList {
                if (
                    ungroupedJson != null
                ) {
                    for (
                    index in
                    0 until ungroupedJson.length()
                    ) {
                        val avatarJson =
                            ungroupedJson
                                .optJSONObject(
                                    index
                                )
                                ?: continue

                        add(
                            fromAvatarLibraryItemJson(
                                avatarJson
                            )
                        )
                    }
                }
            }

        return ViewerAvatarLibrary(
            searchQuery =
                response
                    .optString(
                        "search_query"
                    )
                    .trim(),
            groups =
                groups,
            ungroupedAvatars =
                ungroupedAvatars
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

    private fun fromAvatarLibraryItemJson(
        json: JSONObject
    ): ViewerAvatarLibraryItem {
        return ViewerAvatarLibraryItem(
            uuid =
                json.optString(
                    "uuid"
                ),
            name =
                json.optString(
                    "name"
                ),
            slug =
                json.optString(
                    "slug"
                ),
            imageUrl =
                json.optString(
                    "image_url"
                )
                    .trim()
                    .takeIf {
                        it.isNotBlank()
                    }
        )
    }
}