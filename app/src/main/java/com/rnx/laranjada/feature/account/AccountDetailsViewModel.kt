package com.rnx.laranjada.feature.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.remote.api.AccountApiException
import com.rnx.laranjada.data.repository.AccountRepositoryImpl
import com.rnx.laranjada.domain.model.AccountDevicesSummary
import com.rnx.laranjada.domain.model.AccountOverview
import com.rnx.laranjada.domain.repository.AccountRepository
import kotlinx.coroutines.launch

data class AccountDetailsUiState(
    val overview: AccountOverview? = null,
    val isLoading: Boolean = false,
    val loadErrorMessage: String? = null,
    val deviceActionUuid: String? = null,
    val isDisconnectingOthers: Boolean = false,
    val isDisconnectingAll: Boolean = false,
    val isChangingPassword: Boolean = false,
    val passwordErrorMessage: String? = null
)

class AccountDetailsViewModel(
    private val repository:
    AccountRepository =
        AccountRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(
        AccountDetailsUiState()
    )
        private set

    var feedbackMessage by mutableStateOf<String?>(
        null
    )
        private set

    private var started =
        false

    fun start(
        onSessionEnded: () -> Unit
    ) {
        if (
            started
        ) {
            return
        }

        started =
            true

        loadOverview(
            showLoading =
                true,

            onSessionEnded =
                onSessionEnded
        )
    }

    fun retry(
        onSessionEnded: () -> Unit
    ) {
        loadOverview(
            showLoading =
                true,

            onSessionEnded =
                onSessionEnded
        )
    }

    fun refreshAfterReturn(
        onSessionEnded: () -> Unit
    ) {
        if (
            !started ||
            uiState.isLoading ||
            uiState.overview == null ||
            uiState.deviceActionUuid != null ||
            uiState.isDisconnectingOthers ||
            uiState.isDisconnectingAll
        ) {
            return
        }

        loadOverview(
            showLoading =
                false,

            onSessionEnded =
                onSessionEnded
        )
    }

    fun disconnectDevice(
        deviceUuid: String,
        onSessionEnded: () -> Unit
    ) {
        if (
            uiState.deviceActionUuid != null ||
            uiState.isDisconnectingOthers ||
            uiState.isDisconnectingAll
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    deviceActionUuid =
                        deviceUuid
                )

            try {
                val result =
                    repository.disconnectDevice(
                        deviceUuid
                    )

                uiState =
                    uiState.copy(
                        deviceActionUuid =
                            null
                    )

                if (
                    result.currentSessionEnded
                ) {
                    onSessionEnded()

                    return@launch
                }

                markDeviceDisconnected(
                    deviceUuid
                )

                feedbackMessage =
                    result.message

            } catch (
                exception:
                Exception
            ) {
                uiState =
                    uiState.copy(
                        deviceActionUuid =
                            null
                    )

                if (
                    handleSessionEnded(
                        exception =
                            exception,

                        onSessionEnded =
                            onSessionEnded
                    )
                ) {
                    return@launch
                }

                feedbackMessage =
                    exception.message
                        ?: "Não foi possível desconectar o dispositivo."
            }
        }
    }

    fun disconnectOthers(
        onSessionEnded: () -> Unit
    ) {
        if (
            uiState.deviceActionUuid != null ||
            uiState.isDisconnectingOthers ||
            uiState.isDisconnectingAll
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isDisconnectingOthers =
                        true
                )

            try {
                val result =
                    repository
                        .disconnectOthers()

                uiState =
                    uiState.copy(
                        isDisconnectingOthers =
                            false
                    )

                if (
                    result.currentSessionEnded
                ) {
                    onSessionEnded()

                    return@launch
                }

                markOtherDevicesDisconnected()

                feedbackMessage =
                    result.message

            } catch (
                exception:
                Exception
            ) {
                uiState =
                    uiState.copy(
                        isDisconnectingOthers =
                            false
                    )

                if (
                    handleSessionEnded(
                        exception =
                            exception,

                        onSessionEnded =
                            onSessionEnded
                    )
                ) {
                    return@launch
                }

                feedbackMessage =
                    exception.message
                        ?: "Não foi possível desconectar os outros dispositivos."
            }
        }
    }

    fun disconnectAll(
        onSessionEnded: () -> Unit
    ) {
        if (
            uiState.deviceActionUuid != null ||
            uiState.isDisconnectingOthers ||
            uiState.isDisconnectingAll
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isDisconnectingAll =
                        true
                )

            try {
                val result =
                    repository
                        .disconnectAll()

                uiState =
                    uiState.copy(
                        isDisconnectingAll =
                            false
                    )

                if (
                    result.currentSessionEnded
                ) {
                    onSessionEnded()

                    return@launch
                }

                markAllDevicesDisconnected()

                feedbackMessage =
                    result.message

            } catch (
                exception:
                Exception
            ) {
                uiState =
                    uiState.copy(
                        isDisconnectingAll =
                            false
                    )

                if (
                    handleSessionEnded(
                        exception =
                            exception,

                        onSessionEnded =
                            onSessionEnded
                    )
                ) {
                    return@launch
                }

                feedbackMessage =
                    exception.message
                        ?: "Não foi possível desconectar os dispositivos."
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        newPasswordConfirmation: String,
        onSuccess: () -> Unit,
        onSessionEnded: () -> Unit
    ) {
        if (
            uiState.isChangingPassword
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isChangingPassword =
                        true,

                    passwordErrorMessage =
                        null
                )

            try {
                val result =
                    repository
                        .changePassword(
                            currentPassword =
                                currentPassword,

                            newPassword =
                                newPassword,

                            newPasswordConfirmation =
                                newPasswordConfirmation
                        )

                uiState =
                    uiState.copy(
                        isChangingPassword =
                            false,

                        passwordErrorMessage =
                            null
                    )

                feedbackMessage =
                    result.message

                onSuccess()

            } catch (
                exception:
                Exception
            ) {
                uiState =
                    uiState.copy(
                        isChangingPassword =
                            false
                    )

                if (
                    handleSessionEnded(
                        exception =
                            exception,

                        onSessionEnded =
                            onSessionEnded
                    )
                ) {
                    return@launch
                }

                uiState =
                    uiState.copy(
                        passwordErrorMessage =
                            exception.message
                                ?: "Não foi possível alterar a senha."
                    )
            }
        }
    }

    fun clearPasswordError() {
        if (
            uiState.passwordErrorMessage !=
            null
        ) {
            uiState =
                uiState.copy(
                    passwordErrorMessage =
                        null
                )
        }
    }

    fun consumeFeedback() {
        feedbackMessage =
            null
    }

    private fun loadOverview(
        showLoading: Boolean,
        onSessionEnded: () -> Unit
    ) {
        if (
            showLoading &&
            uiState.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (
                showLoading
            ) {
                uiState =
                    uiState.copy(
                        isLoading =
                            true,

                        loadErrorMessage =
                            null
                    )
            }

            try {
                val overview =
                    repository.getOverview()

                uiState =
                    uiState.copy(
                        overview =
                            overview,

                        isLoading =
                            false,

                        loadErrorMessage =
                            null
                    )

            } catch (
                exception:
                Exception
            ) {
                if (
                    handleSessionEnded(
                        exception =
                            exception,

                        onSessionEnded =
                            onSessionEnded
                    )
                ) {
                    return@launch
                }

                if (
                    showLoading
                ) {
                    uiState =
                        uiState.copy(
                            isLoading =
                                false,

                            loadErrorMessage =
                                exception.message
                                    ?: "Não foi possível carregar os dados da conta."
                        )
                }
            }
        }
    }

    private fun handleSessionEnded(
        exception: Exception,
        onSessionEnded: () -> Unit
    ): Boolean {

        val sessionExpired =
            exception is
                    AccountApiException &&
                    exception.statusCode ==
                    401

        if (
            sessionExpired
        ) {
            onSessionEnded()

            return true
        }

        return false
    }

    private fun markDeviceDisconnected(
        deviceUuid: String
    ) {
        val overview =
            uiState.overview
                ?: return

        val devices =
            overview.devices

        val updatedItems =
            devices.items.map {
                    device ->

                if (
                    device.uuid ==
                    deviceUuid
                ) {
                    device.copy(
                        isConnected =
                            false
                    )
                } else {
                    device
                }
            }

        updateDevices(
            devices =
                devices,

            items =
                updatedItems
        )
    }

    private fun markOtherDevicesDisconnected() {
        val overview =
            uiState.overview
                ?: return

        val devices =
            overview.devices

        val updatedItems =
            devices.items.map {
                    device ->

                if (
                    !device.isCurrentDevice
                ) {
                    device.copy(
                        isConnected =
                            false
                    )
                } else {
                    device
                }
            }

        updateDevices(
            devices =
                devices,

            items =
                updatedItems
        )
    }

    private fun markAllDevicesDisconnected() {
        val overview =
            uiState.overview
                ?: return

        val devices =
            overview.devices

        val updatedItems =
            devices.items.map {
                    device ->

                device.copy(
                    isConnected =
                        false
                )
            }

        updateDevices(
            devices =
                devices,

            items =
                updatedItems
        )
    }

    private fun updateDevices(
        devices: AccountDevicesSummary,
        items: List<
                com.rnx.laranjada.domain.model.AccountDevice
                >
    ) {
        val overview =
            uiState.overview
                ?: return

        val updatedDevices =
            devices.copy(
                connectedCount =
                    items.count {
                        it.isConnected
                    },

                otherConnectedCount =
                    items.count {
                        it.isConnected &&
                                !it.isCurrentDevice
                    },

                items =
                    items
            )

        uiState =
            uiState.copy(
                overview =
                    overview.copy(
                        devices =
                            updatedDevices
                    )
            )
    }
}