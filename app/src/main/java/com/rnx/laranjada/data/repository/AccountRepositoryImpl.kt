package com.rnx.laranjada.data.repository

import com.rnx.laranjada.data.mapper.AccountMapper
import com.rnx.laranjada.data.remote.api.AccountApiService
import com.rnx.laranjada.domain.model.AccountDeviceActionResult
import com.rnx.laranjada.domain.model.AccountOverview
import com.rnx.laranjada.domain.model.AccountPasswordChangeResult
import com.rnx.laranjada.domain.repository.AccountRepository

class AccountRepositoryImpl(
    private val api:
    AccountApiService =
        AccountApiService
) : AccountRepository {

    override suspend fun getOverview():
            AccountOverview {

        return AccountMapper.overview(
            api.getOverview()
        )
    }

    override suspend fun disconnectDevice(
        deviceUuid: String
    ): AccountDeviceActionResult {

        return AccountMapper.deviceAction(
            api.disconnectDevice(
                deviceUuid
            )
        )
    }

    override suspend fun disconnectOthers():
            AccountDeviceActionResult {

        return AccountMapper.deviceAction(
            api.disconnectOthers()
        )
    }

    override suspend fun disconnectAll():
            AccountDeviceActionResult {

        return AccountMapper.deviceAction(
            api.disconnectAll()
        )
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        newPasswordConfirmation: String
    ): AccountPasswordChangeResult {

        return AccountMapper.passwordChange(
            api.changePassword(
                currentPassword =
                    currentPassword,

                newPassword =
                    newPassword,

                newPasswordConfirmation =
                    newPasswordConfirmation
            )
        )
    }
}