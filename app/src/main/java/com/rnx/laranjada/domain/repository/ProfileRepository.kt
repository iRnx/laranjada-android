package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.domain.model.ViewerProfile
import com.rnx.laranjada.domain.model.ViewerProfileList

interface ProfileRepository {

    suspend fun getProfiles():
            ViewerProfileList

    suspend fun selectProfile(
        profileUuid: String,
        pin: String? = null
    ): ViewerProfile
}