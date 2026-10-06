package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.domain.model.PlaybackPresence
import com.rnx.laranjada.domain.model.PlaybackRenewal
import com.rnx.laranjada.domain.model.PlaybackReservation
import com.rnx.laranjada.domain.model.PlaybackStop

interface PlaybackRepository {

    suspend fun reserve(
        contentType: String,
        contentUuid: String,
        clientSessionKey: String
    ): PlaybackReservation

    suspend fun renew(
        sessionUuid: String
    ): PlaybackRenewal

    suspend fun presence(
        contentType: String,
        contentUuid: String,
        clientSessionKey: String,
        status: String
    ): PlaybackPresence

    suspend fun stop(
        contentType: String,
        contentUuid: String,
        clientSessionKey: String,
        status: String
    ): PlaybackStop
}