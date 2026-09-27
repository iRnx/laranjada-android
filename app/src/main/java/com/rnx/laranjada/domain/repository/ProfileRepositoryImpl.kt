package com.rnx.laranjada.data.repository

import com.rnx.laranjada.data.mapper.ProfileMapper
import com.rnx.laranjada.data.remote.api.ProfileApiService
import com.rnx.laranjada.domain.model.ViewerAvatarLibrary
import com.rnx.laranjada.domain.model.ViewerProfile
import com.rnx.laranjada.domain.model.ViewerProfileList
import com.rnx.laranjada.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val apiService:
    ProfileApiService =
        ProfileApiService
) : ProfileRepository {

    override suspend fun getProfiles():
            ViewerProfileList {
        val response =
            apiService.getProfiles()

        return ProfileMapper
            .fromListResponse(
                response
            )
    }

    override suspend fun selectProfile(
        profileUuid: String,
        pin: String?
    ): ViewerProfile {
        val response =
            apiService.selectProfile(
                profileUuid =
                    profileUuid,
                pin =
                    pin
            )

        return ProfileMapper
            .fromSelectResponse(
                response
            )
    }

    override suspend fun createProfile(
        name: String,
        usePin: Boolean,
        pin: String?
    ): ViewerProfile {
        val response =
            apiService.createProfile(
                name =
                    name,
                usePin =
                    usePin,
                pin =
                    pin
            )

        return ProfileMapper
            .fromMutationResponse(
                response
            )
    }

    override suspend fun updateProfile(
        profileUuid: String,
        name: String?,
        usePin: Boolean?,
        pin: String?
    ): ViewerProfile {
        val response =
            apiService.updateProfile(
                profileUuid =
                    profileUuid,
                name =
                    name,
                usePin =
                    usePin,
                pin =
                    pin
            )

        return ProfileMapper
            .fromMutationResponse(
                response
            )
    }

    override suspend fun deleteProfile(
        profileUuid: String
    ): String {
        val response =
            apiService.deleteProfile(
                profileUuid =
                    profileUuid
            )

        return response
            .optString(
                "deleted_profile_uuid"
            )
            .trim()
            .takeIf {
                it.isNotBlank()
            }
            ?: profileUuid
    }

    override suspend fun getAvatarLibrary(
        query: String?
    ): ViewerAvatarLibrary {
        val response =
            apiService.getAvatarLibrary(
                query =
                    query
            )

        return ProfileMapper
            .fromAvatarLibraryResponse(
                response
            )
    }

    override suspend fun setProfileAvatar(
        profileUuid: String,
        avatarUuid: String
    ): ViewerProfile {
        val response =
            apiService.setProfileAvatar(
                profileUuid =
                    profileUuid,
                avatarUuid =
                    avatarUuid
            )

        return ProfileMapper
            .fromMutationResponse(
                response
            )
    }

    override suspend fun removeProfileAvatar(
        profileUuid: String
    ): ViewerProfile {
        val response =
            apiService.removeProfileAvatar(
                profileUuid =
                    profileUuid
            )

        return ProfileMapper
            .fromMutationResponse(
                response
            )
    }
}