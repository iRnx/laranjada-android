package com.rnx.laranjada.feature.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.remote.api.ProfileApiException
import com.rnx.laranjada.data.repository.ProfileRepositoryImpl
import com.rnx.laranjada.domain.model.ViewerAvatarLibrary
import com.rnx.laranjada.domain.model.ViewerProfile
import com.rnx.laranjada.domain.model.ViewerProfileList
import com.rnx.laranjada.domain.repository.ProfileRepository
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean =
        true,

    val profiles: List<ViewerProfile> =
        emptyList(),

    val selectedProfileUuid: String? =
        null,

    val maxProfiles: Int =
        0,

    val activeProfilesCount: Int =
        0,

    val remainingProfiles: Int =
        0,

    val canCreateProfile: Boolean =
        false,

    val loadErrorMessage: String? =
        null,

    val isSelecting: Boolean =
        false,

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
        null,

    val isDeletingProfile: Boolean =
        false,

    val deletingProfileUuid: String? =
        null,

    val deleteErrorMessage: String? =
        null,

    val isLoadingAvatarLibrary: Boolean =
        false,

    val avatarLibrary: ViewerAvatarLibrary? =
        null,

    val avatarLibraryErrorMessage: String? =
        null,

    /*
     * POST/DELETE do avatar.
     */
    val isUpdatingAvatar: Boolean =
        false,

    val updatingAvatarProfileUuid: String? =
        null,

    /*
     * Preenchido quando estamos escolhendo
     * um novo avatar.
     *
     * null durante remoção/reset.
     */
    val updatingAvatarUuid: String? =
        null,

    val avatarMutationErrorMessage: String? =
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

                applyProfileList(
                    result
                )

                uiState =
                    uiState.copy(
                        isLoading =
                            false,
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

    fun clearDeleteError() {
        uiState =
            uiState.copy(
                deleteErrorMessage =
                    null
            )
    }

    fun clearAvatarLibraryError() {
        uiState =
            uiState.copy(
                avatarLibraryErrorMessage =
                    null
            )
    }

    fun clearAvatarMutationError() {
        uiState =
            uiState.copy(
                avatarMutationErrorMessage =
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

                val fallbackActiveProfilesCount =
                    uiState.activeProfilesCount +
                            1

                val fallbackRemainingProfiles =
                    maxOf(
                        0,
                        uiState.maxProfiles -
                                fallbackActiveProfilesCount
                    )

                val fallbackCanCreateProfile =
                    if (
                        uiState.maxProfiles > 0
                    ) {
                        fallbackActiveProfilesCount <
                                uiState.maxProfiles
                    } else {
                        uiState.canCreateProfile
                    }

                uiState =
                    uiState.copy(
                        profiles =
                            updatedProfiles,
                        selectedProfileUuid =
                            createdProfile.uuid,
                        activeProfilesCount =
                            fallbackActiveProfilesCount,
                        remainingProfiles =
                            fallbackRemainingProfiles,
                        canCreateProfile =
                            fallbackCanCreateProfile,
                        createErrorMessage =
                            null
                    )

                val refreshedProfiles =
                    runCatching {
                        repository.getProfiles()
                    }.getOrNull()

                if (
                    refreshedProfiles != null
                ) {
                    applyProfileList(
                        refreshedProfiles
                    )
                }

                uiState =
                    uiState.copy(
                        isCreatingProfile =
                            false,
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

                replaceProfile(
                    updatedProfile
                )

                uiState =
                    uiState.copy(
                        isUpdatingProfile =
                            false,
                        updatingProfileUuid =
                            null,
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

    fun deleteProfile(
        profileUuid: String,
        onSuccess: () -> Unit = {}
    ) {
        if (
            uiState.isDeletingProfile
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isDeletingProfile =
                        true,
                    deletingProfileUuid =
                        profileUuid,
                    deleteErrorMessage =
                        null
                )

            try {
                val deletedProfileUuid =
                    repository.deleteProfile(
                        profileUuid =
                            profileUuid
                    )

                val remainingProfiles =
                    uiState.profiles
                        .filterNot {
                            it.uuid ==
                                    deletedProfileUuid
                        }

                val fallbackActiveProfilesCount =
                    maxOf(
                        0,
                        uiState.activeProfilesCount -
                                1
                    )

                val fallbackRemainingProfiles =
                    maxOf(
                        0,
                        uiState.maxProfiles -
                                fallbackActiveProfilesCount
                    )

                val fallbackCanCreateProfile =
                    if (
                        uiState.maxProfiles > 0
                    ) {
                        fallbackActiveProfilesCount <
                                uiState.maxProfiles
                    } else {
                        uiState.canCreateProfile
                    }

                uiState =
                    uiState.copy(
                        profiles =
                            remainingProfiles,
                        selectedProfileUuid =
                            if (
                                uiState.selectedProfileUuid ==
                                deletedProfileUuid
                            ) {
                                null
                            } else {
                                uiState.selectedProfileUuid
                            },
                        activeProfilesCount =
                            fallbackActiveProfilesCount,
                        remainingProfiles =
                            fallbackRemainingProfiles,
                        canCreateProfile =
                            fallbackCanCreateProfile
                    )

                val refreshedProfiles =
                    runCatching {
                        repository.getProfiles()
                    }.getOrNull()

                if (
                    refreshedProfiles != null
                ) {
                    applyProfileList(
                        refreshedProfiles
                    )
                }

                uiState =
                    uiState.copy(
                        isDeletingProfile =
                            false,
                        deletingProfileUuid =
                            null,
                        deleteErrorMessage =
                            null
                    )

                onSuccess()
            } catch (
                exception:
                ProfileApiException
            ) {
                uiState =
                    uiState.copy(
                        isDeletingProfile =
                            false,
                        deletingProfileUuid =
                            null,
                        deleteErrorMessage =
                            exception.message
                    )
            } catch (
                exception: Exception
            ) {
                uiState =
                    uiState.copy(
                        isDeletingProfile =
                            false,
                        deletingProfileUuid =
                            null,
                        deleteErrorMessage =
                            exception.message
                                ?: "Não foi possível excluir o perfil."
                    )
            }
        }
    }

    fun loadAvatarLibrary(
        query: String? = null
    ) {
        if (
            uiState.isLoadingAvatarLibrary
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isLoadingAvatarLibrary =
                        true,
                    avatarLibraryErrorMessage =
                        null
                )

            try {
                val result =
                    repository.getAvatarLibrary(
                        query =
                            query
                    )

                uiState =
                    uiState.copy(
                        isLoadingAvatarLibrary =
                            false,
                        avatarLibrary =
                            result,
                        avatarLibraryErrorMessage =
                            null
                    )
            } catch (
                exception:
                ProfileApiException
            ) {
                uiState =
                    uiState.copy(
                        isLoadingAvatarLibrary =
                            false,
                        avatarLibraryErrorMessage =
                            exception.message
                    )
            } catch (
                exception: Exception
            ) {
                uiState =
                    uiState.copy(
                        isLoadingAvatarLibrary =
                            false,
                        avatarLibraryErrorMessage =
                            exception.message
                                ?: "Não foi possível carregar os avatares."
                    )
            }
        }
    }

    /*
     * Escolher/trocar avatar.
     */
    fun setProfileAvatar(
        profileUuid: String,
        avatarUuid: String,
        onSuccess: () -> Unit = {}
    ) {
        if (
            uiState.isUpdatingAvatar
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isUpdatingAvatar =
                        true,
                    updatingAvatarProfileUuid =
                        profileUuid,
                    updatingAvatarUuid =
                        avatarUuid,
                    avatarMutationErrorMessage =
                        null
                )

            try {
                val updatedProfile =
                    repository
                        .setProfileAvatar(
                            profileUuid =
                                profileUuid,
                            avatarUuid =
                                avatarUuid
                        )

                /*
                 * A própria API devolve
                 * o perfil completo atualizado.
                 */
                replaceProfile(
                    updatedProfile
                )

                uiState =
                    uiState.copy(
                        isUpdatingAvatar =
                            false,
                        updatingAvatarProfileUuid =
                            null,
                        updatingAvatarUuid =
                            null,
                        avatarMutationErrorMessage =
                            null
                    )

                onSuccess()
            } catch (
                exception:
                ProfileApiException
            ) {
                uiState =
                    uiState.copy(
                        isUpdatingAvatar =
                            false,
                        updatingAvatarProfileUuid =
                            null,
                        updatingAvatarUuid =
                            null,
                        avatarMutationErrorMessage =
                            exception.message
                    )
            } catch (
                exception: Exception
            ) {
                uiState =
                    uiState.copy(
                        isUpdatingAvatar =
                            false,
                        updatingAvatarProfileUuid =
                            null,
                        updatingAvatarUuid =
                            null,
                        avatarMutationErrorMessage =
                            exception.message
                                ?: "Não foi possível atualizar o avatar."
                    )
            }
        }
    }

    /*
     * Remover/resetar avatar.
     */
    fun removeProfileAvatar(
        profileUuid: String,
        onSuccess: () -> Unit = {}
    ) {
        if (
            uiState.isUpdatingAvatar
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isUpdatingAvatar =
                        true,
                    updatingAvatarProfileUuid =
                        profileUuid,

                    /*
                     * null identifica a operação
                     * de remoção para a UI.
                     */
                    updatingAvatarUuid =
                        null,

                    avatarMutationErrorMessage =
                        null
                )

            try {
                val updatedProfile =
                    repository
                        .removeProfileAvatar(
                            profileUuid =
                                profileUuid
                        )

                replaceProfile(
                    updatedProfile
                )

                uiState =
                    uiState.copy(
                        isUpdatingAvatar =
                            false,
                        updatingAvatarProfileUuid =
                            null,
                        updatingAvatarUuid =
                            null,
                        avatarMutationErrorMessage =
                            null
                    )

                onSuccess()
            } catch (
                exception:
                ProfileApiException
            ) {
                uiState =
                    uiState.copy(
                        isUpdatingAvatar =
                            false,
                        updatingAvatarProfileUuid =
                            null,
                        updatingAvatarUuid =
                            null,
                        avatarMutationErrorMessage =
                            exception.message
                    )
            } catch (
                exception: Exception
            ) {
                uiState =
                    uiState.copy(
                        isUpdatingAvatar =
                            false,
                        updatingAvatarProfileUuid =
                            null,
                        updatingAvatarUuid =
                            null,
                        avatarMutationErrorMessage =
                            exception.message
                                ?: "Não foi possível remover o avatar."
                    )
            }
        }
    }

    private fun replaceProfile(
        updatedProfile: ViewerProfile
    ) {
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
                profiles =
                    updatedProfiles,

                selectedProfileUuid =
                    if (
                        updatedProfile.isSelected
                    ) {
                        updatedProfile.uuid
                    } else {
                        uiState.selectedProfileUuid
                    }
            )
    }

    private fun applyProfileList(
        result: ViewerProfileList
    ) {
        uiState =
            uiState.copy(
                profiles =
                    result.profiles,
                selectedProfileUuid =
                    result.selectedProfileUuid,
                maxProfiles =
                    result.maxProfiles,
                activeProfilesCount =
                    result.activeProfilesCount,
                remainingProfiles =
                    result.remainingProfiles,
                canCreateProfile =
                    result.canCreateProfile
            )
    }
}