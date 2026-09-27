package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.domain.model.ViewerAvatarLibrary
import com.rnx.laranjada.domain.model.ViewerProfile
import com.rnx.laranjada.domain.model.ViewerProfileList

interface ProfileRepository {

    suspend fun getProfiles():
            ViewerProfileList

    suspend fun selectProfile(
        profileUuid: String,
        pin: String? = null
    ): ViewerProfile

    suspend fun createProfile(
        name: String,
        usePin: Boolean,
        pin: String? = null
    ): ViewerProfile

    suspend fun updateProfile(
        profileUuid: String,
        name: String? = null,
        usePin: Boolean? = null,
        pin: String? = null
    ): ViewerProfile

    suspend fun deleteProfile(
        profileUuid: String
    ): String

    suspend fun getAvatarLibrary(
        query: String? = null
    ): ViewerAvatarLibrary

    suspend fun setProfileAvatar(
        profileUuid: String,
        avatarUuid: String
    ): ViewerProfile

    suspend fun removeProfileAvatar(
        profileUuid: String
    ): ViewerProfile
}