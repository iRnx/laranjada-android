package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.domain.model.WatchingProgressSaveResult

interface WatchingProgressRepository {

    suspend fun saveProgress(
        contentType: String,
        contentUuid: String,
        positionSeconds: Long,
        durationSeconds: Long,
        status: String,
        forceProgressSave: Boolean
    ): WatchingProgressSaveResult
}