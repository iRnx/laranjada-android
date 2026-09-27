package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.domain.model.PlaybackRenewal
import com.rnx.laranjada.domain.model.PlaybackReservation

interface PlaybackRepository {

    suspend fun reserve(
        contentType: String,
        contentUuid: String,
        clientSessionKey: String
    ): PlaybackReservation

    suspend fun renew(
        sessionUuid: String
    ): PlaybackRenewal
}