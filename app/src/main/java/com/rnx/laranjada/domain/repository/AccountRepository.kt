package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.domain.model.AccountDeviceActionResult
import com.rnx.laranjada.domain.model.AccountOverview
import com.rnx.laranjada.domain.model.AccountPasswordChangeResult

interface AccountRepository {

    suspend fun getOverview():
            AccountOverview

    suspend fun disconnectDevice(
        deviceUuid: String
    ): AccountDeviceActionResult

    suspend fun disconnectOthers():
            AccountDeviceActionResult

    suspend fun disconnectAll():
            AccountDeviceActionResult

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        newPasswordConfirmation: String
    ): AccountPasswordChangeResult
}