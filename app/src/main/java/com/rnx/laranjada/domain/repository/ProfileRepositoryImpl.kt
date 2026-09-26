package com.rnx.laranjada.data.repository

import com.rnx.laranjada.data.mapper.ProfileMapper
import com.rnx.laranjada.data.remote.api.ProfileApiService
import com.rnx.laranjada.domain.model.ViewerProfile
import com.rnx.laranjada.domain.model.ViewerProfileList
import com.rnx.laranjada.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val apiService:
    ProfileApiService = ProfileApiService
) : ProfileRepository {

    override suspend fun getProfiles():
            ViewerProfileList {
        val response =
            apiService.getProfiles()

        return ProfileMapper.fromListResponse(
            response
        )
    }

    override suspend fun selectProfile(
        profileUuid: String,
        pin: String?
    ): ViewerProfile {
        val response =
            apiService.selectProfile(
                profileUuid = profileUuid,
                pin = pin
            )

        return ProfileMapper.fromSelectResponse(
            response
        )
    }
}