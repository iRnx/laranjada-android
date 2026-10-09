package com.rnx.laranjada.data.mapper

import com.rnx.laranjada.domain.model.AccountDevice
import com.rnx.laranjada.domain.model.AccountDeviceActionResult
import com.rnx.laranjada.domain.model.AccountDevicesSummary
import com.rnx.laranjada.domain.model.AccountOverview
import com.rnx.laranjada.domain.model.AccountPasswordChangeResult
import com.rnx.laranjada.domain.model.AccountProfilesSummary
import com.rnx.laranjada.domain.model.AccountUser
import org.json.JSONObject

object AccountMapper {

    fun overview(
        json: JSONObject
    ): AccountOverview {

        val accountJson =
            json.optJSONObject(
                "account"
            )
                ?: JSONObject()

        val profilesJson =
            json.optJSONObject(
                "profiles"
            )
                ?: JSONObject()

        val devicesJson =
            json.optJSONObject(
                "devices"
            )
                ?: JSONObject()

        val devicesArray =
            devicesJson.optJSONArray(
                "items"
            )

        val devices =
            buildList {
                if (
                    devicesArray != null
                ) {
                    for (
                    index in
                    0 until devicesArray.length()
                    ) {
                        val item =
                            devicesArray
                                .optJSONObject(
                                    index
                                )
                                ?: continue

                        add(
                            device(
                                item
                            )
                        )
                    }
                }
            }

        return AccountOverview(
            account =
                AccountUser(
                    uuid =
                        accountJson
                            .optString(
                                "uuid"
                            )
                            .trim(),

                    name =
                        accountJson
                            .optString(
                                "name"
                            )
                            .trim(),

                    username =
                        accountJson
                            .optString(
                                "username"
                            )
                            .trim(),

                    email =
                        accountJson
                            .optString(
                                "email"
                            )
                            .trim(),

                    phone =
                        accountJson
                            .optString(
                                "phone"
                            )
                            .trim(),

                    isEmailVerified =
                        accountJson
                            .optBoolean(
                                "is_email_verified",
                                false
                            ),

                    isActive =
                        accountJson
                            .optBoolean(
                                "is_active",
                                true
                            ),

                    dateJoined =
                        accountJson
                            .optNullableString(
                                "date_joined"
                            ),

                    lastLogin =
                        accountJson
                            .optNullableString(
                                "last_login"
                            )
                ),

            profiles =
                AccountProfilesSummary(
                    count =
                        profilesJson
                            .optInt(
                                "count",
                                0
                            )
                            .coerceAtLeast(
                                0
                            ),

                    maxProfiles =
                        profilesJson
                            .optInt(
                                "max_profiles",
                                0
                            )
                            .coerceAtLeast(
                                0
                            ),

                    remainingProfiles =
                        profilesJson
                            .optInt(
                                "remaining_profiles",
                                0
                            )
                            .coerceAtLeast(
                                0
                            ),

                    canCreateProfile =
                        profilesJson
                            .optBoolean(
                                "can_create_profile",
                                false
                            )
                ),

            devices =
                AccountDevicesSummary(
                    count =
                        devicesJson
                            .optInt(
                                "count",
                                devices.size
                            )
                            .coerceAtLeast(
                                0
                            ),

                    connectedCount =
                        devicesJson
                            .optInt(
                                "connected_count",
                                devices.count {
                                    it.isConnected
                                }
                            )
                            .coerceAtLeast(
                                0
                            ),

                    otherConnectedCount =
                        devicesJson
                            .optInt(
                                "other_connected_count",
                                devices.count {
                                    it.isConnected &&
                                            !it.isCurrentDevice
                                }
                            )
                            .coerceAtLeast(
                                0
                            ),

                    items =
                        devices
                )
        )
    }

    fun deviceAction(
        json: JSONObject
    ): AccountDeviceActionResult {

        return AccountDeviceActionResult(
            message =
                json.optString(
                    "message"
                )
                    .trim()
                    .ifBlank {
                        "Operação realizada com sucesso."
                    },

            currentSessionEnded =
                json.optBoolean(
                    "current_session_ended",
                    false
                ),

            disconnectedCount =
                json.optInt(
                    "disconnected_count",
                    0
                )
                    .coerceAtLeast(
                        0
                    )
        )
    }

    fun passwordChange(
        json: JSONObject
    ): AccountPasswordChangeResult {

        return AccountPasswordChangeResult(
            message =
                json.optString(
                    "message"
                )
                    .trim()
                    .ifBlank {
                        "Senha alterada com sucesso."
                    }
        )
    }

    private fun device(
        json: JSONObject
    ): AccountDevice {

        return AccountDevice(
            uuid =
                json.optString(
                    "uuid"
                )
                    .trim(),

            deviceName =
                json.optString(
                    "device_name"
                )
                    .trim(),

            deviceType =
                json.optString(
                    "device_type"
                )
                    .trim()
                    .lowercase(),

            platform =
                json.optString(
                    "platform"
                )
                    .trim(),

            browser =
                json.optString(
                    "browser"
                )
                    .trim(),

            os =
                json.optString(
                    "os"
                )
                    .trim(),

            isConnected =
                json.optBoolean(
                    "is_connected",
                    false
                ),

            isCurrentDevice =
                json.optBoolean(
                    "is_current_device",
                    false
                ),

            isActive =
                json.optBoolean(
                    "is_active",
                    true
                ),

            lastSeenAt =
                json.optNullableString(
                    "last_seen_at"
                ),

            connectedAt =
                json.optNullableString(
                    "connected_at"
                )
        )
    }

    private fun JSONObject.optNullableString(
        name: String
    ): String? {

        if (
            !has(name) ||
            isNull(name)
        ) {
            return null
        }

        return optString(
            name
        )
            .trim()
            .takeIf {
                it.isNotBlank() &&
                        it.lowercase() !=
                        "null"
            }
    }
}