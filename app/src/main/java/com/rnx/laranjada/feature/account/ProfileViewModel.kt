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
        null,

    val isCreatingProfile: Boolean =
        false,
    val createErrorMessage: String? =
        null,

    val isUpdatingProfile: Boolean =
        false,
    val updatingProfileUuid: String? =
        null,
    val updateErrorMessage: String? =
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
                    isLoading =
                        true,
                    loadErrorMessage =
                        null
                )

            try {
                val result =
                    repository.getProfiles()

                uiState =
                    uiState.copy(
                        isLoading =
                            false,
                        profiles =
                            result.profiles,
                        selectedProfileUuid =
                            result.selectedProfileUuid,
                        loadErrorMessage =
                            null
                    )
            } catch (
                exception: Exception
            ) {
                uiState =
                    uiState.copy(
                        isLoading =
                            false,
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
                selectionErrorMessage =
                    null
            )
    }

    fun clearCreateError() {
        uiState =
            uiState.copy(
                createErrorMessage =
                    null
            )
    }

    fun clearUpdateError() {
        uiState =
            uiState.copy(
                updateErrorMessage =
                    null
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
                    isSelecting =
                        true,
                    selectingProfileUuid =
                        profileUuid,
                    selectionErrorMessage =
                        null
                )

            try {
                val selectedProfile =
                    repository
                        .selectProfile(
                            profileUuid =
                                profileUuid,
                            pin =
                                pin
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
                        isSelecting =
                            false,
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
                exception:
                ProfileApiException
            ) {
                uiState =
                    uiState.copy(
                        isSelecting =
                            false,
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
                        isSelecting =
                            false,
                        selectingProfileUuid =
                            null,
                        selectionErrorMessage =
                            exception.message
                                ?: "Não foi possível selecionar o perfil."
                    )
            }
        }
    }

    fun createProfile(
        name: String,
        usePin: Boolean,
        pin: String? = null,
        onSuccess: (
            ViewerProfile
        ) -> Unit = {}
    ) {
        if (
            uiState.isCreatingProfile
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isCreatingProfile =
                        true,
                    createErrorMessage =
                        null
                )

            try {
                val createdProfile =
                    repository
                        .createProfile(
                            name =
                                name.trim(),
                            usePin =
                                usePin,
                            pin =
                                pin
                        )

                val updatedProfiles =
                    (
                            uiState.profiles
                                .filterNot {
                                    it.uuid ==
                                            createdProfile.uuid
                                }
                                .map {
                                    it.copy(
                                        isSelected =
                                            false
                                    )
                                } +
                                    createdProfile.copy(
                                        isSelected =
                                            true
                                    )
                            )
                        .sortedWith(
                            compareByDescending<
                                    ViewerProfile
                                    > {
                                it.isDefault
                            }.thenBy {
                                it.name
                                    .lowercase()
                            }
                        )

                uiState =
                    uiState.copy(
                        isCreatingProfile =
                            false,
                        profiles =
                            updatedProfiles,
                        selectedProfileUuid =
                            createdProfile.uuid,
                        createErrorMessage =
                            null
                    )

                onSuccess(
                    createdProfile
                )
            } catch (
                exception:
                ProfileApiException
            ) {
                uiState =
                    uiState.copy(
                        isCreatingProfile =
                            false,
                        createErrorMessage =
                            exception.message
                    )
            } catch (
                exception: Exception
            ) {
                uiState =
                    uiState.copy(
                        isCreatingProfile =
                            false,
                        createErrorMessage =
                            exception.message
                                ?: "Não foi possível criar o perfil."
                    )
            }
        }
    }

    fun updateProfile(
        profileUuid: String,
        name: String? = null,
        usePin: Boolean? = null,
        pin: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        if (
            uiState.isUpdatingProfile
        ) {
            return
        }

        if (
            name == null &&
            usePin == null &&
            pin == null
        ) {
            onSuccess()

            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isUpdatingProfile =
                        true,
                    updatingProfileUuid =
                        profileUuid,
                    updateErrorMessage =
                        null
                )

            try {
                val updatedProfile =
                    repository
                        .updateProfile(
                            profileUuid =
                                profileUuid,
                            name =
                                name,
                            usePin =
                                usePin,
                            pin =
                                pin
                        )

                val updatedProfiles =
                    uiState.profiles
                        .map {
                                profile ->

                            if (
                                profile.uuid ==
                                updatedProfile.uuid
                            ) {
                                updatedProfile
                            } else {
                                profile
                            }
                        }
                        .sortedWith(
                            compareByDescending<
                                    ViewerProfile
                                    > {
                                it.isDefault
                            }.thenBy {
                                it.name
                                    .lowercase()
                            }
                        )

                uiState =
                    uiState.copy(
                        isUpdatingProfile =
                            false,
                        updatingProfileUuid =
                            null,
                        profiles =
                            updatedProfiles,
                        updateErrorMessage =
                            null
                    )

                onSuccess()
            } catch (
                exception:
                ProfileApiException
            ) {
                uiState =
                    uiState.copy(
                        isUpdatingProfile =
                            false,
                        updatingProfileUuid =
                            null,
                        updateErrorMessage =
                            exception.message
                    )
            } catch (
                exception: Exception
            ) {
                uiState =
                    uiState.copy(
                        isUpdatingProfile =
                            false,
                        updatingProfileUuid =
                            null,
                        updateErrorMessage =
                            exception.message
                                ?: "Não foi possível atualizar o perfil."
                    )
            }
        }
    }
}