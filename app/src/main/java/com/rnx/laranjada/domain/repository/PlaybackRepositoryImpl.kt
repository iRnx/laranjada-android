package com.rnx.laranjada.data.repository

import com.rnx.laranjada.data.mapper.PlaybackMapper
import com.rnx.laranjada.data.remote.api.PlaybackApiService
import com.rnx.laranjada.domain.model.PlaybackPresence
import com.rnx.laranjada.domain.model.PlaybackRenewal
import com.rnx.laranjada.domain.model.PlaybackReservation
import com.rnx.laranjada.domain.model.PlaybackStop
import com.rnx.laranjada.domain.repository.PlaybackRepository

class PlaybackRepositoryImpl(
    private val apiService:
    PlaybackApiService =
        PlaybackApiService
) : PlaybackRepository {

    override suspend fun reserve(
        contentType: String,
        contentUuid: String,
        clientSessionKey: String
    ): PlaybackReservation {
        val response =
            apiService.reserve(
                contentType = contentType,
                contentUuid = contentUuid,
                clientSessionKey =
                    clientSessionKey
            )

        return PlaybackMapper
            .fromReserveResponse(
                response = response,
                contentType = contentType,
                contentUuid = contentUuid,
                requestedClientSessionKey =
                    clientSessionKey
            )
    }

    override suspend fun renew(
        sessionUuid: String
    ): PlaybackRenewal {
        val response =
            apiService.renew(
                sessionUuid =
                    sessionUuid
            )

        return PlaybackMapper
            .fromRenewResponse(
                response
            )
    }

    override suspend fun presence(
        contentType: String,
        contentUuid: String,
        clientSessionKey: String,
        status: String
    ): PlaybackPresence {
        val response =
            apiService.presence(
                contentType =
                    contentType,
                contentUuid =
                    contentUuid,
                clientSessionKey =
                    clientSessionKey,
                status =
                    status
            )

        return PlaybackMapper
            .fromPresenceResponse(
                response
            )
    }

    override suspend fun stop(
        contentType: String,
        contentUuid: String,
        clientSessionKey: String,
        status: String
    ): PlaybackStop {
        val response =
            apiService.stop(
                contentType =
                    contentType,
                contentUuid =
                    contentUuid,
                clientSessionKey =
                    clientSessionKey,
                status =
                    status
            )

        return PlaybackMapper
            .fromStopResponse(
                response
            )
    }
}