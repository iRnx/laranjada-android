package com.rnx.laranjada.domain.model

data class WatchingProgressSaveResult(
    val saved: Boolean,
    val reason: String,

    val profileUuid: String,
    val progressUuid: String,

    val contentType: String,
    val contentUuid: String,

    val positionSeconds: Long,
    val requestedPositionSeconds: Long,
    val durationSeconds: Long,

    val percent: Double,
    val isCompleted: Boolean,

    val status: String,
    val forceProgressSave: Boolean,

    val saveIntervalSeconds: Long,
    val deviceUuid: String
)