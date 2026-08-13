package com.rnx.laranjada.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rnx.laranjada.domain.model.AuthenticatedUser
import com.rnx.laranjada.feature.account.AccountScreen
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
    startDestination: String = AppRoutes.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = AppRoutes.Home.route
        ) {
            HomeScreen(
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
                onAccountClick = {
                    navController.navigate(
                        AppRoutes.Account.route
                    )
                }
            )
        }

        composable(
            route = AppRoutes.Account.route
        ) {
            AccountScreen(
                user = currentUser,
                isLoggingOut = isLoggingOut,
                logoutErrorMessage = logoutErrorMessage,
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = onLogoutClick
            )
        }

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
            val sectionSlug = backStackEntry.arguments
                ?.getString(
                    AppRoutes.MediaGrid.sectionSlugArg
                )
                .orEmpty()

            val encodedTitle = backStackEntry.arguments
                ?.getString(
                    AppRoutes.MediaGrid.titleArg
                )
                .orEmpty()

            val title = encodedTitle.decodeRouteValue()

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
            val uuid = backStackEntry.arguments
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
            val contentType = backStackEntry.arguments
                ?.getString(
                    AppRoutes.Detail.contentTypeArg
                )
                .orEmpty()

            val uuid = backStackEntry.arguments
                ?.getString(
                    AppRoutes.Detail.uuidArg
                )
                .orEmpty()

            DetailRoute(
                contentType = contentType,
                uuid = uuid,
                onBackClick = {
                    navController.popBackStack()
                },
                onPlayClick = {
                        playerContentType,
                        playerUuid,
                        hlsUrl,
                        seriesUuid ->

                    navController.navigate(
                        AppRoutes.Player.createRoute(
                            contentType = playerContentType,
                            uuid = playerUuid,
                            hlsUrl = hlsUrl,
                            seriesUuid = seriesUuid
                        )
                    )
                },
                onRestartClick = {
                    // Depois vamos ligar isso no progresso real.
                },
                onFavoriteClick = {
                    // Depois vamos ligar isso na API de favoritos.
                },
                onRelatedClick = { item ->
                    navController.navigate(
                        AppRoutes.Detail.createRoute(
                            contentType = item.contentType,
                            uuid = item.uuid
                        )
                    )
                },
                onEpisodeClick = {
                        episode,
                        seriesUuid ->

                    navController.navigate(
                        AppRoutes.Player.createRoute(
                            contentType = "episode",
                            uuid = episode.uuid,
                            hlsUrl = episode.hlsUrl,
                            seriesUuid = seriesUuid
                        )
                    )
                }
            )
        }

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
                    AppRoutes.Player.hlsUrlArg
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(
                    AppRoutes.Player.seriesUuidArg
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val contentType = backStackEntry.arguments
                ?.getString(
                    AppRoutes.Player.contentTypeArg
                )
                .orEmpty()

            val uuid = backStackEntry.arguments
                ?.getString(
                    AppRoutes.Player.uuidArg
                )
                .orEmpty()

            val hlsUrl = backStackEntry.arguments
                ?.getString(
                    AppRoutes.Player.hlsUrlArg
                )
                .orEmpty()
                .decodeRouteValue()

            val seriesUuid = backStackEntry.arguments
                ?.getString(
                    AppRoutes.Player.seriesUuidArg
                )
                .orEmpty()
                .decodeRouteValue()

            PlayerScreen(
                contentType = contentType,
                uuid = uuid,
                hlsUrl = hlsUrl,
                seriesUuid = seriesUuid,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun String.decodeRouteValue(): String {
    return runCatching {
        URLDecoder.decode(
            this,
            "UTF-8"
        )
    }.getOrDefault(this)
}