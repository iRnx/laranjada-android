
package com.rnx.laranjada.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.rnx.laranjada.feature.favorites.FavoritesScreen
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
    startDestination: String = AppRoutes.Home.route
) {
    val profileViewModel: ProfileViewModel =
        viewModel()

    val profileState =
        profileViewModel.uiState

    val menuProfiles =
        profileState.profiles.map { profile ->
            profile.toMenuProfileUi()
        }

    val selectedProfile =
        profileState.profiles.firstOrNull { profile ->
            profile.uuid ==
                    profileState.selectedProfileUuid
        } ?: profileState.profiles.firstOrNull { profile ->
            profile.isSelected
        }

    val profileSelectionRequired =
        !profileState.isLoading &&
                selectedProfile == null

    /*
     * HOME
     */
    fun returnToHome() {
        navController.navigate(
            AppRoutes.Home.route
        ) {
            popUpTo(AppRoutes.Home.route) {
                inclusive = false
            }

            launchSingleTop = true
        }
    }

    /*
     * CONTA
     */
    fun returnToAccount() {
        val returned =
            navController.popBackStack(
                AppRoutes.Account.route,
                false
            )

        if (!returned) {
            navController.navigate(
                AppRoutes.Account.route
            ) {
                launchSingleTop = true
            }
        }
    }

    /*
     * SELEÇÃO OBRIGATÓRIA DE PERFIL
     */
    fun forceProfileSelection() {
        navController.navigate(
            AppRoutes.Account.route
        ) {
            popUpTo(AppRoutes.Home.route) {
                inclusive = false
            }

            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        /*
         * HOME
         */
        composable(
            route = AppRoutes.Home.route
        ) {
            when {
                profileState.isLoading -> {
                }

                selectedProfile == null -> {
                    LaunchedEffect(Unit) {
                        forceProfileSelection()
                    }
                }

                else -> {
                    HomeScreen(
                        profileName =
                            selectedProfile.name,

                        profileAvatarUrl =
                            selectedProfile
                                .avatar
                                ?.imageUrl,

                        onContinueWatchingClick = { item ->
                            navController.navigate(
                                AppRoutes.Player.createRoute(
                                    contentType =
                                        item.contentType,

                                    uuid =
                                        item.contentUuid,

                                    seriesUuid =
                                        item.seriesUuid.orEmpty(),

                                    initialPositionSeconds =
                                        item.positionSeconds
                                )
                            )
                        },

                        onMediaClick = { contentType, uuid ->
                            navController.navigate(
                                AppRoutes.Detail.createRoute(
                                    contentType = contentType,
                                    uuid = uuid
                                )
                            )
                        },

                        onCollectionClick = { collection ->
                            navController.navigate(
                                AppRoutes.Collection.createRoute(
                                    uuid = collection.uuid
                                )
                            )
                        },

                        onSeeAllClick = { sectionSlug, title ->
                            navController.navigate(
                                AppRoutes.MediaGrid.createRoute(
                                    sectionSlug = sectionSlug,
                                    title = title
                                )
                            )
                        },

                        onCategoryGridClick = { sectionSlug, title ->
                            navController.navigate(
                                AppRoutes.MediaGrid.createRoute(
                                    sectionSlug = sectionSlug,
                                    title = title
                                )
                            )
                        },

                        /*
                         * FAVORITOS
                         *
                         * O coração da bottom bar
                         * navega para uma tela própria.
                         */
                        onFavoritesClick = {
                            navController.navigate(
                                AppRoutes.Favorites.route
                            ) {
                                launchSingleTop = true
                            }
                        },

                        onAccountClick = {
                            navController.navigate(
                                AppRoutes.Account.route
                            )
                        }
                    )
                }
            }
        }

        /*
         * FAVORITOS
         *
         * Não enviamos profileUuid
         * para a API.
         *
         * A tela utiliza este UUID
         * apenas para detectar mudanças
         * no perfil selecionado.
         */
        composable(
            route = AppRoutes.Favorites.route
        ) {
            when {
                profileState.isLoading -> {
                }

                selectedProfile == null -> {
                    LaunchedEffect(Unit) {
                        forceProfileSelection()
                    }
                }

                else -> {
                    FavoritesScreen(
                        profileUuid =
                            selectedProfile.uuid,

                        profileName =
                            selectedProfile.name,

                        profileAvatarUrl =
                            selectedProfile
                                .avatar
                                ?.imageUrl,

                        onBackClick = {
                            navController.popBackStack()
                        },

                        onHomeClick = {
                            returnToHome()
                        },

                        onAccountClick = {
                            navController.navigate(
                                AppRoutes.Account.route
                            )
                        },

                        onMediaClick = { contentType, uuid ->
                            navController.navigate(
                                AppRoutes.Detail.createRoute(
                                    contentType = contentType,
                                    uuid = uuid
                                )
                            )
                        }
                    )
                }
            }
        }

        /*
         * CONTA / PERFIS
         */
        composable(
            route = AppRoutes.Account.route
        ) {
            BackHandler(
                enabled = profileSelectionRequired
            ) {
            }

            AccountScreen(
                user = currentUser,

                isLoggingOut =
                    isLoggingOut,

                logoutErrorMessage =
                    logoutErrorMessage
                        ?: profileState.loadErrorMessage
                        ?: profileState.selectionErrorMessage,

                onBackClick = {
                    if (!profileSelectionRequired) {
                        navController.popBackStack()
                    }
                },

                onLogoutClick =
                    onLogoutClick,

                profiles =
                    menuProfiles,

                canCreateProfile =
                    profileState.canCreateProfile,

                onProfileClick = { profile ->
                    profileViewModel.clearSelectionError()

                    if (profile.hasPin) {
                        navController.navigate(
                            AppRoutes.ProfilePin.createRoute(
                                profileUuid = profile.uuid
                            )
                        )
                    } else {
                        profileViewModel.selectProfile(
                            profileUuid = profile.uuid,

                            onSuccess = {
                                returnToHome()
                            },

                            onPinRequired = {
                                navController.navigate(
                                    AppRoutes.ProfilePin.createRoute(
                                        profileUuid = profile.uuid
                                    )
                                )
                            }
                        )
                    }
                },

                onCreateProfileClick = {
                    profileViewModel.clearCreateError()

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
         * PIN
         */
        composable(
            route = AppRoutes.ProfilePin.route,

            arguments = listOf(
                navArgument(
                    AppRoutes.ProfilePin.profileUuidArg
                ) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val profileUuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.ProfilePin.profileUuidArg
                    )
                    .orEmpty()

            val profile =
                profileState.profiles.firstOrNull {
                    it.uuid == profileUuid
                }

            ProfilePinScreen(
                profileName =
                    profile?.name ?: "Perfil",

                avatarUrl =
                    profile?.avatar?.imageUrl,

                isSubmitting =
                    profileState.isSelecting &&
                            profileState.selectingProfileUuid ==
                            profileUuid,

                errorMessage =
                    profileState.selectionErrorMessage,

                onBackClick = {
                    profileViewModel.clearSelectionError()
                    navController.popBackStack()
                },

                onSubmitClick = { pin ->
                    profileViewModel.selectProfile(
                        profileUuid = profileUuid,
                        pin = pin,

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
            route = AppRoutes.CreateProfile.route
        ) {
            CreateProfileScreen(
                isSubmitting =
                    profileState.isCreatingProfile,

                errorMessage =
                    profileState.createErrorMessage,

                onBackClick = {
                    profileViewModel.clearCreateError()
                    navController.popBackStack()
                },

                onCancelClick = {
                    profileViewModel.clearCreateError()
                    navController.popBackStack()
                },

                onDoneClick = { name, usePin, pin ->
                    profileViewModel.createProfile(
                        name = name,
                        usePin = usePin,

                        pin = pin.takeIf {
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
         * EDITAR PERFIS
         */
        composable(
            route = AppRoutes.EditProfiles.route
        ) {
            EditProfilesScreen(
                profiles = menuProfiles,

                onProfileClick = { profile ->
                    profileViewModel.clearUpdateError()

                    navController.navigate(
                        AppRoutes.EditProfile.createRoute(
                            profileUuid = profile.uuid
                        )
                    )
                },

                onDoneClick = {
                    navController.popBackStack()
                }
            )
        }

        /*
         * EDITAR PERFIL
         */
        composable(
            route = AppRoutes.EditProfile.route,

            arguments = listOf(
                navArgument(
                    AppRoutes.EditProfile.profileUuidArg
                ) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val profileUuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.EditProfile.profileUuidArg
                    )
                    .orEmpty()

            val profile =
                profileState.profiles.firstOrNull {
                    it.uuid == profileUuid
                }

            EditProfileScreen(
                profileUuid = profileUuid,

                initialName =
                    profile?.name ?: "",

                avatarUrl =
                    profile?.avatar?.imageUrl,

                initialHasPin =
                    profile?.hasPin ?: false,

                isSubmitting =
                    profileState.isUpdatingProfile &&
                            profileState.updatingProfileUuid ==
                            profileUuid,

                errorMessage =
                    profileState.updateErrorMessage,

                onBackClick = {
                    profileViewModel.clearUpdateError()
                    navController.popBackStack()
                },

                onAvatarClick = {
                    profileViewModel.clearAvatarMutationError()

                    navController.navigate(
                        AppRoutes.AvatarPicker.createRoute(
                            profileUuid = profileUuid
                        )
                    )
                },

                onSaveClick = { name, usePin, pin ->
                    val currentProfile = profile

                    if (currentProfile != null) {
                        val normalizedName =
                            name.trim()

                        val normalizedPin =
                            pin.trim()

                        val namePatch =
                            normalizedName.takeIf {
                                it != currentProfile.name
                            }

                        val pinPatch =
                            normalizedPin.takeIf {
                                it.isNotBlank()
                            }

                        val usePinPatch =
                            when {
                                usePin != currentProfile.hasPin ->
                                    usePin

                                pinPatch != null ->
                                    true

                                else ->
                                    null
                            }

                        profileViewModel.updateProfile(
                            profileUuid = profileUuid,
                            name = namePatch,
                            usePin = usePinPatch,
                            pin = pinPatch,

                            onSuccess = {
                                navController.popBackStack()
                            }
                        )
                    }
                },

                onCancelClick = {
                    profileViewModel.clearUpdateError()
                    navController.popBackStack()
                },

                onDeleteClick = {
                    profileViewModel.clearDeleteError()

                    navController.navigate(
                        AppRoutes.DeleteProfile.createRoute(
                            profileUuid = profileUuid
                        )
                    )
                }
            )
        }

        /*
         * AVATAR
         */
        composable(
            route = AppRoutes.AvatarPicker.route,

            arguments = listOf(
                navArgument(
                    AppRoutes.AvatarPicker.profileUuidArg
                ) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val profileUuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.AvatarPicker.profileUuidArg
                    )
                    .orEmpty()

            val profile =
                profileState.profiles.firstOrNull {
                    it.uuid == profileUuid
                }

            LaunchedEffect(profileUuid) {
                profileViewModel.clearAvatarLibraryError()
                profileViewModel.clearAvatarMutationError()
                profileViewModel.loadAvatarLibrary()
            }

            val avatarOptions =
                buildList {
                    profileState.avatarLibrary
                        ?.groups
                        ?.forEach { group ->
                            group.avatars.forEach { avatar ->
                                add(
                                    AvatarOptionUi(
                                        uuid = avatar.uuid,
                                        name = avatar.name,
                                        collection = group.name,
                                        imageUrl = avatar.imageUrl
                                    )
                                )
                            }
                        }

                    profileState.avatarLibrary
                        ?.ungroupedAvatars
                        ?.forEach { avatar ->
                            add(
                                AvatarOptionUi(
                                    uuid = avatar.uuid,
                                    name = avatar.name,
                                    collection = "Outros",
                                    imageUrl = avatar.imageUrl
                                )
                            )
                        }
                }

            val avatarMutationInProgress =
                profileState.isUpdatingAvatar &&
                        profileState.updatingAvatarProfileUuid ==
                        profileUuid

            AvatarPickerScreen(
                profileName =
                    profile?.name ?: "Perfil",

                avatars =
                    avatarOptions,

                initialSelectedAvatarUuid =
                    profile?.avatar?.uuid,

                isLoading =
                    profileState.isLoadingAvatarLibrary,

                isSubmitting =
                    avatarMutationInProgress,

                submittingAvatarUuid =
                    if (avatarMutationInProgress) {
                        profileState.updatingAvatarUuid
                    } else {
                        null
                    },

                errorMessage =
                    profileState.avatarMutationErrorMessage
                        ?: profileState.avatarLibraryErrorMessage,

                onBackClick = {
                    profileViewModel.clearAvatarMutationError()
                    navController.popBackStack()
                },

                onAvatarClick = { avatar ->
                    profileViewModel.setProfileAvatar(
                        profileUuid = profileUuid,
                        avatarUuid = avatar.uuid,

                        onSuccess = {
                            navController.popBackStack()
                        }
                    )
                },

                onRemoveAvatarClick = {
                    profileViewModel.removeProfileAvatar(
                        profileUuid = profileUuid,

                        onSuccess = {
                            navController.popBackStack()
                        }
                    )
                }
            )
        }

        /*
         * EXCLUIR PERFIL
         */
        composable(
            route = AppRoutes.DeleteProfile.route,

            arguments = listOf(
                navArgument(
                    AppRoutes.DeleteProfile.profileUuidArg
                ) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val profileUuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.DeleteProfile.profileUuidArg
                    )
                    .orEmpty()

            val profile =
                profileState.profiles.firstOrNull {
                    it.uuid == profileUuid
                }

            DeleteProfileScreen(
                profileName =
                    profile?.name ?: "Perfil",

                avatarUrl =
                    profile?.avatar?.imageUrl,

                isSubmitting =
                    profileState.isDeletingProfile &&
                            profileState.deletingProfileUuid ==
                            profileUuid,

                errorMessage =
                    profileState.deleteErrorMessage,

                onConfirmClick = {
                    profileViewModel.deleteProfile(
                        profileUuid = profileUuid,

                        onSuccess = {
                            returnToAccount()
                        }
                    )
                },

                onCancelClick = {
                    profileViewModel.clearDeleteError()
                    navController.popBackStack()
                }
            )
        }

        /*
         * GRID
         */
        composable(
            route = AppRoutes.MediaGrid.route,

            arguments = listOf(
                navArgument(
                    AppRoutes.MediaGrid.sectionSlugArg
                ) {
                    type = NavType.StringType
                },

                navArgument(
                    AppRoutes.MediaGrid.titleArg
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            val sectionSlug =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.MediaGrid.sectionSlugArg
                    )
                    .orEmpty()

            val encodedTitle =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.MediaGrid.titleArg
                    )
                    .orEmpty()

            val title =
                encodedTitle.decodeRouteValue()

            MediaGridScreen(
                sectionSlug = sectionSlug,
                title = title,

                onBackClick = {
                    navController.popBackStack()
                },

                onMediaClick = { contentType, uuid ->
                    navController.navigate(
                        AppRoutes.Detail.createRoute(
                            contentType = contentType,
                            uuid = uuid
                        )
                    )
                }
            )
        }

        /*
         * COLEÇÃO
         */
        composable(
            route = AppRoutes.Collection.route,

            arguments = listOf(
                navArgument(
                    AppRoutes.Collection.uuidArg
                ) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val uuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Collection.uuidArg
                    )
                    .orEmpty()

            CollectionDetailRoute(
                uuid = uuid,

                onBackClick = {
                    navController.popBackStack()
                },

                onMediaClick = { contentType, mediaUuid ->
                    navController.navigate(
                        AppRoutes.Detail.createRoute(
                            contentType = contentType,
                            uuid = mediaUuid
                        )
                    )
                }
            )
        }

        /*
         * DETALHE
         *
         * A API de Favoritos no Detail
         * será conectada na próxima etapa.
         *
         * Por enquanto preservamos
         * o fluxo atual.
         */
        composable(
            route = AppRoutes.Detail.route,

            arguments = listOf(
                navArgument(
                    AppRoutes.Detail.contentTypeArg
                ) {
                    type = NavType.StringType
                },

                navArgument(
                    AppRoutes.Detail.uuidArg
                ) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val contentType =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Detail.contentTypeArg
                    )
                    .orEmpty()

            val uuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Detail.uuidArg
                    )
                    .orEmpty()

            when {
                profileState.isLoading -> {
                }

                selectedProfile == null -> {
                    LaunchedEffect(contentType, uuid) {
                        forceProfileSelection()
                    }
                }

                else -> {
                    DetailRoute(
                        contentType = contentType,
                        uuid = uuid,

                        profileName =
                            selectedProfile.name,

                        profileAvatarUrl =
                            selectedProfile
                                .avatar
                                ?.imageUrl,

                        onBackClick = {
                            navController.popBackStack()
                        },

                        onAccountClick = {
                            navController.navigate(
                                AppRoutes.Account.route
                            )
                        },

                        onPlayClick = {
                                playerContentType,
                                playerUuid,
                                seriesUuid ->

                            navController.navigate(
                                AppRoutes.Player.createRoute(
                                    contentType =
                                        playerContentType,

                                    uuid =
                                        playerUuid,

                                    seriesUuid =
                                        seriesUuid
                                )
                            )
                        },

                        onRestartClick = {
                        },

                        onFavoriteClick = {
                        },

                        onRelatedClick = { item ->
                            navController.navigate(
                                AppRoutes.Detail.createRoute(
                                    contentType =
                                        item.contentType,

                                    uuid =
                                        item.uuid
                                )
                            )
                        },

                        onEpisodeClick = { episode, seriesUuid ->
                            navController.navigate(
                                AppRoutes.Player.createRoute(
                                    contentType = "episode",
                                    uuid = episode.uuid,
                                    seriesUuid = seriesUuid
                                )
                            )
                        }
                    )
                }
            }
        }

        /*
         * PLAYER SEGURO
         *
         * Mantemos o Player exatamente
         * com os argumentos anteriores.
         *
         * Favoritos não precisa conhecer
         * URL HLS nem Bearer de playback.
         */
        composable(
            route = AppRoutes.Player.route,

            arguments = listOf(
                navArgument(
                    AppRoutes.Player.contentTypeArg
                ) {
                    type = NavType.StringType
                },

                navArgument(
                    AppRoutes.Player.uuidArg
                ) {
                    type = NavType.StringType
                },

                navArgument(
                    AppRoutes.Player.seriesUuidArg
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                },

                navArgument(
                    AppRoutes.Player.initialPositionSecondsArg
                ) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->

            val contentType =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Player.contentTypeArg
                    )
                    .orEmpty()

            val uuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Player.uuidArg
                    )
                    .orEmpty()

            val seriesUuid =
                backStackEntry.arguments
                    ?.getString(
                        AppRoutes.Player.seriesUuidArg
                    )
                    .orEmpty()
                    .decodeRouteValue()

            val initialPositionSeconds =
                backStackEntry.arguments
                    ?.getLong(
                        AppRoutes.Player.initialPositionSecondsArg
                    )
                    ?.coerceAtLeast(0L)
                    ?: 0L

            when {
                profileState.isLoading -> {
                }

                selectedProfile == null -> {
                    LaunchedEffect(contentType, uuid) {
                        forceProfileSelection()
                    }
                }

                else -> {
                    PlayerScreen(
                        contentType = contentType,
                        uuid = uuid,
                        seriesUuid = seriesUuid,

                        initialPositionSeconds =
                            initialPositionSeconds,

                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

private fun ViewerProfile.toMenuProfileUi():
        MenuProfileUi {

    return MenuProfileUi(
        uuid = uuid,
        name = name,
        avatarUrl = avatar?.imageUrl,
        hasPin = hasPin,
        isSelected = isSelected
    )
}

private fun String.decodeRouteValue(): String {
    return runCatching {
        URLDecoder.decode(this, "UTF-8")
    }.getOrDefault(this)
}
