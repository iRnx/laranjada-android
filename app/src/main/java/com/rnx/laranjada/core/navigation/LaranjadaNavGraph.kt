package com.rnx.laranjada.core.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rnx.laranjada.domain.model.AuthenticatedUser
import com.rnx.laranjada.domain.model.ViewerProfile
import com.rnx.laranjada.feature.account.AccountScreen
import com.rnx.laranjada.feature.account.AvatarOptionUi
import com.rnx.laranjada.feature.account.AvatarPickerScreen
import com.rnx.laranjada.feature.account.CreateProfileScreen
import com.rnx.laranjada.feature.account.DeleteProfileScreen
import com.rnx.laranjada.feature.account.EditProfileScreen
import com.rnx.laranjada.feature.account.EditProfilesScreen
import com.rnx.laranjada.feature.account.MenuProfileUi
import com.rnx.laranjada.feature.account.ProfilePinScreen
import com.rnx.laranjada.feature.account.ProfileViewModel
import com.rnx.laranjada.feature.collections.CollectionDetailRoute
import com.rnx.laranjada.feature.details.DetailRoute
import com.rnx.laranjada.feature.home.HomeScreen
import com.rnx.laranjada.feature.mediagrid.MediaGridScreen
import com.rnx.laranjada.feature.player.PlayerScreen
import java.net.URLDecoder

@Composable
fun LaranjadaNavGraph(
    navController: NavHostController,
    currentUser: AuthenticatedUser,
    isLoggingOut: Boolean,
    logoutErrorMessage: String?,
    onLogoutClick: () -> Unit,
    startDestination: String =
        AppRoutes.Home.route
) {
    val profileViewModel:
            ProfileViewModel =
        viewModel()

    val profileState =
        profileViewModel.uiState

    val menuProfiles =
        profileState.profiles.map {
                profile ->

            profile.toMenuProfileUi()
        }

    val selectedProfile =
        profileState.profiles
            .firstOrNull {
                    profile ->

                profile.uuid ==
                        profileState
                            .selectedProfileUuid
            }
            ?: profileState.profiles
                .firstOrNull {
                        profile ->

                    profile.isSelected
                }

    fun returnToHome() {
        navController.navigate(
            AppRoutes.Home.route
        ) {
            popUpTo(
                AppRoutes.Home.route
            ) {
                inclusive = false
            }

            launchSingleTop = true
        }
    }

    NavHost(
        navController =
            navController,
        startDestination =
            startDestination
    ) {

        /*
         * HOME
         */
        composable(
            route =
                AppRoutes.Home.route
        ) {
            HomeScreen(
                profileName =
                    selectedProfile?.name
                        ?: currentUser.displayName,
                profileAvatarUrl =
                    selectedProfile
                        ?.avatar
                        ?.imageUrl,
                onMediaClick = {
                        contentType,
                        uuid ->

                    navController.navigate(
                        AppRoutes.Detail
                            .createRoute(
                                contentType =
                                    contentType,
                                uuid =
                                    uuid
                            )
                    )
                },
                onCollectionClick = {
                        collection ->

                    navController.navigate(
                        AppRoutes.Collection
                            .createRoute(
                                uuid =
                                    collection.uuid
                            )
                    )
                },
                onSeeAllClick = {
                        sectionSlug,
                        title ->

                    navController.navigate(
                        AppRoutes.MediaGrid
                            .createRoute(
                                sectionSlug =
                                    sectionSlug,
                                title =
                                    title
                            )
                    )
                },
                onCategoryGridClick = {
                        sectionSlug,
                        title ->

                    navController.navigate(
                        AppRoutes.MediaGrid
                            .createRoute(
                                sectionSlug =
                                    sectionSlug,
                                title =
                                    title
                            )
                    )
                },
                onAccountClick = {
                    navController.navigate(
                        AppRoutes.Account.route
                    )
                }
            )
        }

        /*
         * MENU
         */
        composable(
            route =
                AppRoutes.Account.route
        ) {
            AccountScreen(
                user =
                    currentUser,
                isLoggingOut =
                    isLoggingOut,
                logoutErrorMessage =
                    logoutErrorMessage
                        ?: profileState
                            .loadErrorMessage
                        ?: profileState
                            .selectionErrorMessage,
                onBackClick = {
                    navController
                        .popBackStack()
                },
                onLogoutClick =
                    onLogoutClick,
                profiles =
                    menuProfiles,
                onProfileClick = {
                        profile ->

                    profileViewModel
                        .clearSelectionError()

                    if (
                        profile.hasPin
                    ) {
                        navController.navigate(
                            AppRoutes.ProfilePin
                                .createRoute(
                                    profileUuid =
                                        profile.uuid
                                )
                        )
                    } else {
                        profileViewModel
                            .selectProfile(
                                profileUuid =
                                    profile.uuid,
                                onSuccess = {
                                    returnToHome()
                                },
                                onPinRequired = {
                                    navController.navigate(
                                        AppRoutes.ProfilePin
                                            .createRoute(
                                                profileUuid =
                                                    profile.uuid
                                            )
                                    )
                                }
                            )
                    }
                },
                onCreateProfileClick = {
                    profileViewModel
                        .clearCreateError()

                    navController.navigate(
                        AppRoutes.CreateProfile.route
                    )
                },
                onEditProfilesClick = {
                    navController.navigate(
                        AppRoutes.EditProfiles.route
                    )
                }
            )
        }

        /*
         * PIN DO PERFIL
         */
        composable(
            route =
                AppRoutes.ProfilePin.route,
            arguments = listOf(
                navArgument(
                    AppRoutes.ProfilePin
                        .profileUuidArg
                ) {
                    type =
                        NavType.StringType
                }
            )
        ) { backStackEntry ->
            val profileUuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.ProfilePin
                            .profileUuidArg
                    )
                    .orEmpty()

            val profile =
                profileState.profiles
                    .firstOrNull {
                        it.uuid ==
                                profileUuid
                    }

            ProfilePinScreen(
                profileName =
                    profile?.name
                        ?: "Perfil",
                avatarUrl =
                    profile
                        ?.avatar
                        ?.imageUrl,
                isSubmitting =
                    profileState.isSelecting &&
                            profileState
                                .selectingProfileUuid ==
                            profileUuid,
                errorMessage =
                    profileState
                        .selectionErrorMessage,
                onBackClick = {
                    profileViewModel
                        .clearSelectionError()

                    navController
                        .popBackStack()
                },
                onSubmitClick = {
                        pin ->

                    profileViewModel
                        .selectProfile(
                            profileUuid =
                                profileUuid,
                            pin =
                                pin,
                            onSuccess = {
                                returnToHome()
                            }
                        )
                }
            )
        }

        /*
         * CRIAR PERFIL
         */
        composable(
            route =
                AppRoutes.CreateProfile.route
        ) {
            CreateProfileScreen(
                isSubmitting =
                    profileState
                        .isCreatingProfile,
                errorMessage =
                    profileState
                        .createErrorMessage,
                onBackClick = {
                    profileViewModel
                        .clearCreateError()

                    navController
                        .popBackStack()
                },
                onCancelClick = {
                    profileViewModel
                        .clearCreateError()

                    navController
                        .popBackStack()
                },
                onDoneClick = {
                        name,
                        usePin,
                        pin ->

                    profileViewModel
                        .createProfile(
                            name =
                                name,
                            usePin =
                                usePin,
                            pin =
                                pin.takeIf {
                                    usePin
                                },
                            onSuccess = {
                                returnToHome()
                            }
                        )
                }
            )
        }

        /*
         * ESCOLHER PERFIL PARA EDITAR
         */
        composable(
            route =
                AppRoutes.EditProfiles.route
        ) {
            EditProfilesScreen(
                profiles =
                    menuProfiles,
                onProfileClick = {
                        profile ->

                    profileViewModel
                        .clearUpdateError()

                    navController.navigate(
                        AppRoutes.EditProfile
                            .createRoute(
                                profileUuid =
                                    profile.uuid
                            )
                    )
                },
                onDoneClick = {
                    navController
                        .popBackStack()
                }
            )
        }

        /*
         * EDITAR PERFIL
         */
        composable(
            route =
                AppRoutes.EditProfile.route,
            arguments = listOf(
                navArgument(
                    AppRoutes.EditProfile
                        .profileUuidArg
                ) {
                    type =
                        NavType.StringType
                }
            )
        ) { backStackEntry ->
            val profileUuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.EditProfile
                            .profileUuidArg
                    )
                    .orEmpty()

            val profile =
                profileState.profiles
                    .firstOrNull {
                        it.uuid ==
                                profileUuid
                    }

            EditProfileScreen(
                profileUuid =
                    profileUuid,
                initialName =
                    profile?.name
                        ?: "",
                avatarUrl =
                    profile
                        ?.avatar
                        ?.imageUrl,
                initialHasPin =
                    profile?.hasPin
                        ?: false,
                isSubmitting =
                    profileState
                        .isUpdatingProfile &&
                            profileState
                                .updatingProfileUuid ==
                            profileUuid,
                errorMessage =
                    profileState
                        .updateErrorMessage,
                onBackClick = {
                    profileViewModel
                        .clearUpdateError()

                    navController
                        .popBackStack()
                },
                onAvatarClick = {
                    navController.navigate(
                        AppRoutes.AvatarPicker
                            .createRoute(
                                profileUuid =
                                    profileUuid
                            )
                    )
                },
                onSaveClick = {
                        name,
                        usePin,
                        pin ->

                    val currentProfile =
                        profile

                    if (
                        currentProfile != null
                    ) {
                        val normalizedName =
                            name.trim()

                        val normalizedPin =
                            pin.trim()

                        /*
                         * Só envia o nome quando
                         * ele realmente mudou.
                         */
                        val namePatch =
                            normalizedName
                                .takeIf {
                                    it !=
                                            currentProfile
                                                .name
                                }

                        /*
                         * PIN vazio significa:
                         *
                         * não estamos criando nem
                         * alterando o PIN.
                         */
                        val pinPatch =
                            normalizedPin
                                .takeIf {
                                    it.isNotBlank()
                                }

                        /*
                         * PATCH parcial:
                         *
                         * PERFIL COM PIN
                         *
                         * manter PIN:
                         * usePin == hasPin
                         * pin vazio
                         * → não envia nada de PIN
                         *
                         * alterar PIN:
                         * pin preenchido
                         * → use_pin=true + pin
                         *
                         * remover PIN:
                         * usePin=false
                         * → use_pin=false
                         *
                         *
                         * PERFIL SEM PIN
                         *
                         * continuar sem PIN:
                         * → não envia nada
                         *
                         * criar PIN:
                         * usePin=true + pin
                         */
                        val usePinPatch =
                            when {
                                usePin !=
                                        currentProfile
                                            .hasPin -> {
                                    usePin
                                }

                                pinPatch != null -> {
                                    true
                                }

                                else -> {
                                    null
                                }
                            }

                        profileViewModel
                            .updateProfile(
                                profileUuid =
                                    profileUuid,
                                name =
                                    namePatch,
                                usePin =
                                    usePinPatch,
                                pin =
                                    pinPatch,
                                onSuccess = {
                                    navController
                                        .popBackStack()
                                }
                            )
                    }
                },
                onCancelClick = {
                    profileViewModel
                        .clearUpdateError()

                    navController
                        .popBackStack()
                },
                onDeleteClick = {
                    navController.navigate(
                        AppRoutes.DeleteProfile
                            .createRoute(
                                profileUuid =
                                    profileUuid
                            )
                    )
                }
            )
        }

        /*
         * ESCOLHER AVATAR
         *
         * Ainda temporário.
         */
        composable(
            route =
                AppRoutes.AvatarPicker.route,
            arguments = listOf(
                navArgument(
                    AppRoutes.AvatarPicker
                        .profileUuidArg
                ) {
                    type =
                        NavType.StringType
                }
            )
        ) { backStackEntry ->
            val profileUuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.AvatarPicker
                            .profileUuidArg
                    )
                    .orEmpty()

            val profile =
                profileState.profiles
                    .firstOrNull {
                        it.uuid ==
                                profileUuid
                    }

            val temporaryAvatars =
                listOf(
                    AvatarOptionUi(
                        uuid =
                            "avatar-hermione",
                        name =
                            "Hermione",
                        collection =
                            "Harry Potter",
                        imageUrl =
                            null
                    ),
                    AvatarOptionUi(
                        uuid =
                            "avatar-slytherin",
                        name =
                            "Slytherin",
                        collection =
                            "Harry Potter",
                        imageUrl =
                            null
                    ),
                    AvatarOptionUi(
                        uuid =
                            "avatar-voldemort",
                        name =
                            "Voldemort",
                        collection =
                            "Harry Potter",
                        imageUrl =
                            null
                    ),
                    AvatarOptionUi(
                        uuid =
                            "avatar-robin",
                        name =
                            "Robin",
                        collection =
                            "Os Jovens Titãs em Ação",
                        imageUrl =
                            null
                    )
                )

            AvatarPickerScreen(
                profileName =
                    profile?.name
                        ?: "Perfil",
                avatars =
                    temporaryAvatars,
                initialSelectedAvatarUuid =
                    profile
                        ?.avatar
                        ?.uuid,
                onBackClick = {
                    navController
                        .popBackStack()
                },
                onAvatarClick = {
                }
            )
        }

        /*
         * EXCLUIR PERFIL
         *
         * A API DELETE ainda será
         * conectada no próximo bloco.
         */
        composable(
            route =
                AppRoutes.DeleteProfile.route,
            arguments = listOf(
                navArgument(
                    AppRoutes.DeleteProfile
                        .profileUuidArg
                ) {
                    type =
                        NavType.StringType
                }
            )
        ) { backStackEntry ->
            val profileUuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.DeleteProfile
                            .profileUuidArg
                    )
                    .orEmpty()

            val profile =
                profileState.profiles
                    .firstOrNull {
                        it.uuid ==
                                profileUuid
                    }

            DeleteProfileScreen(
                profileName =
                    profile?.name
                        ?: "Perfil",
                avatarUrl =
                    profile
                        ?.avatar
                        ?.imageUrl,
                onConfirmClick = {
                },
                onCancelClick = {
                    navController
                        .popBackStack()
                }
            )
        }

        /*
         * GRID
         */
        composable(
            route =
                AppRoutes.MediaGrid.route,
            arguments = listOf(
                navArgument(
                    AppRoutes.MediaGrid
                        .sectionSlugArg
                ) {
                    type =
                        NavType.StringType
                },
                navArgument(
                    AppRoutes.MediaGrid
                        .titleArg
                ) {
                    type =
                        NavType.StringType

                    defaultValue =
                        ""
                }
            )
        ) { backStackEntry ->
            val sectionSlug =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.MediaGrid
                            .sectionSlugArg
                    )
                    .orEmpty()

            val encodedTitle =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.MediaGrid
                            .titleArg
                    )
                    .orEmpty()

            val title =
                encodedTitle
                    .decodeRouteValue()

            MediaGridScreen(
                sectionSlug =
                    sectionSlug,
                title =
                    title,
                onBackClick = {
                    navController
                        .popBackStack()
                },
                onMediaClick = {
                        contentType,
                        uuid ->

                    navController.navigate(
                        AppRoutes.Detail
                            .createRoute(
                                contentType =
                                    contentType,
                                uuid =
                                    uuid
                            )
                    )
                }
            )
        }

        /*
         * COLEÇÃO
         */
        composable(
            route =
                AppRoutes.Collection.route,
            arguments = listOf(
                navArgument(
                    AppRoutes.Collection
                        .uuidArg
                ) {
                    type =
                        NavType.StringType
                }
            )
        ) { backStackEntry ->
            val uuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Collection
                            .uuidArg
                    )
                    .orEmpty()

            CollectionDetailRoute(
                uuid =
                    uuid,
                onBackClick = {
                    navController
                        .popBackStack()
                },
                onMediaClick = {
                        contentType,
                        mediaUuid ->

                    navController.navigate(
                        AppRoutes.Detail
                            .createRoute(
                                contentType =
                                    contentType,
                                uuid =
                                    mediaUuid
                            )
                    )
                }
            )
        }

        /*
         * DETALHE
         */
        composable(
            route =
                AppRoutes.Detail.route,
            arguments = listOf(
                navArgument(
                    AppRoutes.Detail
                        .contentTypeArg
                ) {
                    type =
                        NavType.StringType
                },
                navArgument(
                    AppRoutes.Detail
                        .uuidArg
                ) {
                    type =
                        NavType.StringType
                }
            )
        ) { backStackEntry ->
            val contentType =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Detail
                            .contentTypeArg
                    )
                    .orEmpty()

            val uuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Detail
                            .uuidArg
                    )
                    .orEmpty()

            DetailRoute(
                contentType =
                    contentType,
                uuid =
                    uuid,
                onBackClick = {
                    navController
                        .popBackStack()
                },
                onPlayClick = {
                        playerContentType,
                        playerUuid,
                        hlsUrl,
                        seriesUuid ->

                    navController.navigate(
                        AppRoutes.Player
                            .createRoute(
                                contentType =
                                    playerContentType,
                                uuid =
                                    playerUuid,
                                hlsUrl =
                                    hlsUrl,
                                seriesUuid =
                                    seriesUuid
                            )
                    )
                },
                onRestartClick = {
                },
                onFavoriteClick = {
                },
                onRelatedClick = {
                        item ->

                    navController.navigate(
                        AppRoutes.Detail
                            .createRoute(
                                contentType =
                                    item.contentType,
                                uuid =
                                    item.uuid
                            )
                    )
                },
                onEpisodeClick = {
                        episode,
                        seriesUuid ->

                    navController.navigate(
                        AppRoutes.Player
                            .createRoute(
                                contentType =
                                    "episode",
                                uuid =
                                    episode.uuid,
                                hlsUrl =
                                    episode.hlsUrl,
                                seriesUuid =
                                    seriesUuid
                            )
                    )
                }
            )
        }

        /*
         * PLAYER ANTIGO
         *
         * Será substituído pela task
         * Playback Authorization.
         */
        composable(
            route =
                AppRoutes.Player.route,
            arguments = listOf(
                navArgument(
                    AppRoutes.Player
                        .contentTypeArg
                ) {
                    type =
                        NavType.StringType
                },
                navArgument(
                    AppRoutes.Player
                        .uuidArg
                ) {
                    type =
                        NavType.StringType
                },
                navArgument(
                    AppRoutes.Player
                        .hlsUrlArg
                ) {
                    type =
                        NavType.StringType

                    defaultValue =
                        ""
                },
                navArgument(
                    AppRoutes.Player
                        .seriesUuidArg
                ) {
                    type =
                        NavType.StringType

                    defaultValue =
                        ""
                }
            )
        ) { backStackEntry ->
            val contentType =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Player
                            .contentTypeArg
                    )
                    .orEmpty()

            val uuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Player
                            .uuidArg
                    )
                    .orEmpty()

            val hlsUrl =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Player
                            .hlsUrlArg
                    )
                    .orEmpty()
                    .decodeRouteValue()

            val seriesUuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Player
                            .seriesUuidArg
                    )
                    .orEmpty()
                    .decodeRouteValue()

            PlayerScreen(
                contentType =
                    contentType,
                uuid =
                    uuid,
                hlsUrl =
                    hlsUrl,
                seriesUuid =
                    seriesUuid,
                onBackClick = {
                    navController
                        .popBackStack()
                }
            )
        }
    }
}

private fun ViewerProfile.toMenuProfileUi():
        MenuProfileUi {
    return MenuProfileUi(
        uuid =
            uuid,
        name =
            name,
        avatarUrl =
            avatar?.imageUrl,
        hasPin =
            hasPin,
        isSelected =
            isSelected
    )
}

private fun String.decodeRouteValue():
        String {
    return runCatching {
        URLDecoder.decode(
            this,
            "UTF-8"
        )
    }.getOrDefault(
        this
    )
}