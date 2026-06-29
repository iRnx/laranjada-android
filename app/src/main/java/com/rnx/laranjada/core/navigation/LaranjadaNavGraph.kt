package com.rnx.laranjada.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rnx.laranjada.feature.details.DetailRoute
import com.rnx.laranjada.feature.home.HomeScreen

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
                onPlayClick = {
                    // Depois abre o player.
                },
                onRestartClick = {
                    // Depois reinicia do começo.
                },
                onFavoriteClick = {
                    // Depois salva favorito.
                },
                onEpisodeClick = {
                    // Depois abre o player do episódio.
                }
            )
        }
    }
}