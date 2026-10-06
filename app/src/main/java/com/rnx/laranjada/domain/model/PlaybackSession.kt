package com.rnx.laranjada.domain.model

data class PlaybackAuthorization(
    val url: String,
    val authorization: String,
    val tokenType: String,
    val issuedAt: String,
    val expiresAt: String,
    val expiresInSeconds: Long,
    val renewAt: String,
    val renewBeforeSeconds: Long
)

data class PlaybackReservation(
    val contentType: String,
    val contentUuid: String,
    val sessionUuid: String,
    val clientSessionKey: String,
    val limit: Int,
    val activeDeviceCount: Int,
    val presencePulseIntervalMs: Long,
    val playback: PlaybackAuthorization
)

data class PlaybackRenewal(
    val sessionUuid: String,
    val limit: Int,
    val activeDeviceCount: Int,
    val playback: PlaybackAuthorization
)

data class PlaybackCommand(
    val uuid: String,
    val type: String,
    val message: String
)

data class PlaybackPresence(
    val active: Boolean,
    val allowed: Boolean,
    val code: String,
    val message: String,
    val sessionUuid: String,
    val status: String,
    val revalidated: Boolean,
    val limit: Int,
    val activeDeviceCount: Int,
    val commands: List<PlaybackCommand>
)

data class PlaybackStop(
    val active: Boolean,
    val stopped: Boolean,
    val alreadyStopped: Boolean,
    val code: String,
    val message: String,
    val sessionUuid: String,
    val status: String
)