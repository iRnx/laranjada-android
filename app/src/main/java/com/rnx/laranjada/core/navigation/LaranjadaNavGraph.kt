package com.rnx.laranjada.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rnx.laranjada.feature.collections.CollectionDetailRoute
import com.rnx.laranjada.feature.details.DetailRoute
import com.rnx.laranjada.feature.home.HomeScreen
import com.rnx.laranjada.feature.player.PlayerScreen

@Composable
fun LaranjadaNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Home.route
    ) {
        composable(
            route = AppRoutes.Home.route
        ) {
            HomeScreen(
                onBannerClick = { banner ->
                    navController.navigate(
                        AppRoutes.Detail.createRoute(
                            contentType = banner.contentType,
                            uuid = banner.uuid
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
                }
            )
        }

        composable(
            route = AppRoutes.Detail.route,
            arguments = listOf(
                navArgument(AppRoutes.Detail.contentTypeArg) {
                    type = NavType.StringType
                },
                navArgument(AppRoutes.Detail.uuidArg) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val contentType = backStackEntry.arguments
                ?.getString(AppRoutes.Detail.contentTypeArg)
                .orEmpty()

            val uuid = backStackEntry.arguments
                ?.getString(AppRoutes.Detail.uuidArg)
                .orEmpty()

            DetailRoute(
                contentType = contentType,
                uuid = uuid,
                onBackClick = {
                    navController.popBackStack()
                },
                onPlayClick = { playerContentType, playerUuid, hlsUrl, seriesUuid ->
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
                    // Depois vamos usar progresso real para reiniciar do começo.
                    // Por enquanto, o botão só existe quando tiver progresso.
                },
                onFavoriteClick = {
                    // Depois salva favorito.
                },
                onEpisodeClick = { episode, seriesUuid ->
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
            route = AppRoutes.Collection.route,
            arguments = listOf(
                navArgument(AppRoutes.Collection.uuidArg) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val uuid = backStackEntry.arguments
                ?.getString(AppRoutes.Collection.uuidArg)
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
            route = AppRoutes.Player.route,
            arguments = listOf(
                navArgument(AppRoutes.Player.contentTypeArg) {
                    type = NavType.StringType
                },
                navArgument(AppRoutes.Player.uuidArg) {
                    type = NavType.StringType
                },
                navArgument(AppRoutes.Player.hlsUrlArg) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(AppRoutes.Player.seriesUuidArg) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val contentType = backStackEntry.arguments
                ?.getString(AppRoutes.Player.contentTypeArg)
                .orEmpty()

            val uuid = backStackEntry.arguments
                ?.getString(AppRoutes.Player.uuidArg)
                .orEmpty()

            val hlsUrl = backStackEntry.arguments
                ?.getString(AppRoutes.Player.hlsUrlArg)
                .orEmpty()

            val seriesUuid = backStackEntry.arguments
                ?.getString(AppRoutes.Player.seriesUuidArg)
                .orEmpty()

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