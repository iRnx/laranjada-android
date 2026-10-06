package com.rnx.laranjada.data.mapper

import com.rnx.laranjada.domain.model.PlaybackAuthorization
import com.rnx.laranjada.domain.model.PlaybackCommand
import com.rnx.laranjada.domain.model.PlaybackPresence
import com.rnx.laranjada.domain.model.PlaybackRenewal
import com.rnx.laranjada.domain.model.PlaybackReservation
import com.rnx.laranjada.domain.model.PlaybackStop
import org.json.JSONObject

object PlaybackMapper {

    fun fromReserveResponse(
        response: JSONObject,
        contentType: String,
        contentUuid: String,
        requestedClientSessionKey: String
    ): PlaybackReservation {
        val sessionUuid =
            response.requiredString(
                "session_uuid"
            )

        val clientSessionKey =
            response
                .optString(
                    "client_session_key"
                )
                .trim()
                .ifBlank {
                    requestedClientSessionKey
                }

        val playbackJson =
            response.optJSONObject(
                "playback"
            )
                ?: error(
                    "A API não retornou os dados de playback."
                )

        return PlaybackReservation(
            contentType = contentType,
            contentUuid = contentUuid,
            sessionUuid = sessionUuid,
            clientSessionKey = clientSessionKey,
            limit = response.optInt(
                "limit",
                0
            ),
            activeDeviceCount = response.optInt(
                "active_device_count",
                0
            ),
            presencePulseIntervalMs = response.optLong(
                "presence_pulse_interval_ms",
                0L
            ),
            playback = mapPlayback(
                playbackJson
            )
        )
    }

    fun fromRenewResponse(
        response: JSONObject
    ): PlaybackRenewal {
        val sessionUuid =
            response.requiredString(
                "session_uuid"
            )

        val playbackJson =
            response.optJSONObject(
                "playback"
            )
                ?: error(
                    "A API não retornou a nova autorização de playback."
                )

        return PlaybackRenewal(
            sessionUuid = sessionUuid,
            limit = response.optInt(
                "limit",
                0
            ),
            activeDeviceCount = response.optInt(
                "active_device_count",
                0
            ),
            playback = mapPlayback(
                playbackJson
            )
        )
    }

    fun fromPresenceResponse(
        response: JSONObject
    ): PlaybackPresence {
        val commands =
            mutableListOf<PlaybackCommand>()

        val commandsJson =
            response.optJSONArray(
                "commands"
            )

        if (commandsJson != null) {
            for (
            index in
            0 until commandsJson.length()
            ) {
                val commandJson =
                    commandsJson.optJSONObject(
                        index
                    )
                        ?: continue

                commands.add(
                    PlaybackCommand(
                        uuid = commandJson
                            .optString(
                                "uuid"
                            )
                            .trim(),

                        type = commandJson
                            .optString(
                                "type"
                            )
                            .trim()
                            .lowercase(),

                        message = commandJson
                            .optString(
                                "message"
                            )
                            .trim()
                    )
                )
            }
        }

        return PlaybackPresence(
            active = response.optBoolean(
                "active",
                false
            ),
            allowed = response.optBoolean(
                "allowed",
                false
            ),
            code = response
                .optString(
                    "code"
                )
                .trim(),
            message = response
                .optString(
                    "message"
                )
                .trim(),
            sessionUuid = response
                .optString(
                    "session_uuid"
                )
                .trim(),
            status = response
                .optString(
                    "status"
                )
                .trim(),
            revalidated = response.optBoolean(
                "revalidated",
                false
            ),
            limit = response.optInt(
                "limit",
                0
            ),
            activeDeviceCount = response.optInt(
                "active_device_count",
                0
            ),
            commands = commands
        )
    }

    fun fromStopResponse(
        response: JSONObject
    ): PlaybackStop {
        return PlaybackStop(
            active = response.optBoolean(
                "active",
                false
            ),
            stopped = response.optBoolean(
                "stopped",
                false
            ),
            alreadyStopped = response.optBoolean(
                "already_stopped",
                false
            ),
            code = response
                .optString(
                    "code"
                )
                .trim(),
            message = response
                .optString(
                    "message"
                )
                .trim(),
            sessionUuid = response
                .optString(
                    "session_uuid"
                )
                .trim(),
            status = response
                .optString(
                    "status"
                )
                .trim()
        )
    }

    private fun mapPlayback(
        json: JSONObject
    ): PlaybackAuthorization {
        return PlaybackAuthorization(
            url = json.requiredString(
                "url"
            ),
            authorization = json.requiredString(
                "authorization"
            ),
            tokenType = json
                .optString(
                    "token_type"
                )
                .trim()
                .ifBlank {
                    "Bearer"
                },
            issuedAt = json
                .optString(
                    "issued_at"
                )
                .trim(),
            expiresAt = json
                .optString(
                    "expires_at"
                )
                .trim(),
            expiresInSeconds = json.optLong(
                "expires_in_seconds",
                0L
            ),
            renewAt = json
                .optString(
                    "renew_at"
                )
                .trim(),
            renewBeforeSeconds = json.optLong(
                "renew_before_seconds",
                0L
            )
        )
    }

    private fun JSONObject.requiredString(
        key: String
    ): String {
        val value =
            optString(
                key
            ).trim()

        if (value.isBlank()) {
            error(
                "Campo obrigatório ausente na resposta: $key"
            )
        }

        return value
    }
}