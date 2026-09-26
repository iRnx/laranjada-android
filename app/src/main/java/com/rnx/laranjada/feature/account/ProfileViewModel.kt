package com.rnx.laranjada.feature.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.remote.api.ProfileApiException
import com.rnx.laranjada.data.repository.ProfileRepositoryImpl
import com.rnx.laranjada.domain.model.ViewerProfile
import com.rnx.laranjada.domain.repository.ProfileRepository
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profiles: List<ViewerProfile> =
        emptyList(),
    val selectedProfileUuid: String? =
        null,
    val loadErrorMessage: String? =
        null,
    val isSelecting: Boolean = false,
    val selectingProfileUuid: String? =
        null,
    val selectionErrorMessage: String? =
        null
)

class ProfileViewModel(
    private val repository:
    ProfileRepository =
        ProfileRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(
        ProfileUiState()
    )
        private set

    init {
        loadProfiles()
    }

    fun loadProfiles() {
        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isLoading = true,
                    loadErrorMessage = null
                )

            try {
                val result =
                    repository.getProfiles()

                uiState =
                    uiState.copy(
                        isLoading = false,
                        profiles =
                            result.profiles,
                        selectedProfileUuid =
                            result.selectedProfileUuid,
                        loadErrorMessage = null
                    )
            } catch (
                exception: Exception
            ) {
                uiState =
                    uiState.copy(
                        isLoading = false,
                        loadErrorMessage =
                            exception.message
                                ?: "Não foi possível carregar os perfis."
                    )
            }
        }
    }

    fun clearSelectionError() {
        uiState =
            uiState.copy(
                selectionErrorMessage = null
            )
    }

    fun selectProfile(
        profileUuid: String,
        pin: String? = null,
        onSuccess: () -> Unit = {},
        onPinRequired: () -> Unit = {}
    ) {
        if (
            uiState.isSelecting
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isSelecting = true,
                    selectingProfileUuid =
                        profileUuid,
                    selectionErrorMessage =
                        null
                )

            try {
                val selectedProfile =
                    repository.selectProfile(
                        profileUuid =
                            profileUuid,
                        pin = pin
                    )

                val updatedProfiles =
                    uiState.profiles.map {
                            profile ->

                        profile.copy(
                            isSelected =
                                profile.uuid ==
                                        selectedProfile.uuid
                        )
                    }

                uiState =
                    uiState.copy(
                        isSelecting = false,
                        selectingProfileUuid =
                            null,
                        profiles =
                            updatedProfiles,
                        selectedProfileUuid =
                            selectedProfile.uuid,
                        selectionErrorMessage =
                            null
                    )

                onSuccess()
            } catch (
                exception: ProfileApiException
            ) {
                uiState =
                    uiState.copy(
                        isSelecting = false,
                        selectingProfileUuid =
                            null,
                        selectionErrorMessage =
                            exception.message
                    )

                if (
                    exception.code ==
                    "profile_pin_required"
                ) {
                    onPinRequired()
                }
            } catch (
                exception: Exception
            ) {
                uiState =
                    uiState.copy(
                        isSelecting = false,
                        selectingProfileUuid =
                            null,
                        selectionErrorMessage =
                            exception.message
                                ?: "Não foi possível selecionar o perfil."
                    )
            }
        }
    }
}