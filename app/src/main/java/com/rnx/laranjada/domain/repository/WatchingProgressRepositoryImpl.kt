package com.rnx.laranjada.data.repository

import com.rnx.laranjada.data.remote.api.WatchingProgressApiService
import com.rnx.laranjada.domain.model.WatchingProgressSaveResult
import com.rnx.laranjada.domain.repository.WatchingProgressRepository
import org.json.JSONObject

class WatchingProgressRepositoryImpl(
    private val apiService:
    WatchingProgressApiService =
        WatchingProgressApiService
) : WatchingProgressRepository {

    override suspend fun saveProgress(
        contentType: String,
        contentUuid: String,
        positionSeconds: Long,
        durationSeconds: Long,
        status: String,
        forceProgressSave: Boolean
    ): WatchingProgressSaveResult {

        val response =
            apiService
                .saveProgress(
                    contentType =
                        contentType,

                    contentUuid =
                        contentUuid,

                    positionSeconds =
                        positionSeconds,

                    durationSeconds =
                        durationSeconds,

                    status =
                        status,

                    forceProgressSave =
                        forceProgressSave
                )

        return mapResponse(
            response
        )
    }

    private fun mapResponse(
        json: JSONObject
    ): WatchingProgressSaveResult {

        return WatchingProgressSaveResult(
            saved =
                json.optBoolean(
                    "saved",
                    false
                ),

            reason =
                json
                    .optString(
                        "reason"
                    )
                    .trim(),

            profileUuid =
                json
                    .optString(
                        "profile_uuid"
                    )
                    .trim(),

            progressUuid =
                json
                    .optString(
                        "progress_uuid"
                    )
                    .trim(),

            contentType =
                json
                    .optString(
                        "content_type"
                    )
                    .trim(),

            contentUuid =
                json
                    .optString(
                        "content_uuid"
                    )
                    .trim(),

            positionSeconds =
                json
                    .optLong(
                        "position_seconds",
                        0L
                    )
                    .coerceAtLeast(
                        0L
                    ),

            requestedPositionSeconds =
                json
                    .optLong(
                        "requested_position_seconds",
                        0L
                    )
                    .coerceAtLeast(
                        0L
                    ),

            durationSeconds =
                json
                    .optLong(
                        "duration_seconds",
                        0L
                    )
                    .coerceAtLeast(
                        0L
                    ),

            percent =
                json
                    .optDouble(
                        "percent",
                        0.0
                    )
                    .coerceIn(
                        0.0,
                        100.0
                    ),

            isCompleted =
                json.optBoolean(
                    "is_completed",
                    false
                ),

            status =
                json
                    .optString(
                        "status"
                    )
                    .trim(),

            forceProgressSave =
                json.optBoolean(
                    "force_progress_save",
                    false
                ),

            saveIntervalSeconds =
                json
                    .optLong(
                        "save_interval_seconds",
                        30L
                    )
                    .coerceAtLeast(
                        0L
                    ),

            deviceUuid =
                json
                    .optString(
                        "device_uuid"
                    )
                    .trim()
        )
    }
}