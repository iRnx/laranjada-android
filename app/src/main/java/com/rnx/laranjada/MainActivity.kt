package com.rnx.laranjada

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.core.design.theme.LaranjadaTheme
import com.rnx.laranjada.core.navigation.AppRoutes
import com.rnx.laranjada.core.navigation.LaranjadaNavGraph
import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.feature.account.ProfileViewModel
import com.rnx.laranjada.feature.auth.AppSessionState
import com.rnx.laranjada.feature.auth.AppSessionViewModel
import com.rnx.laranjada.feature.auth.LoginScreen
import com.rnx.laranjada.feature.home.components.BottomBarNavigationContext
import com.rnx.laranjada.feature.home.components.HomeBottomBar
import com.rnx.laranjada.feature.home.components.LocalBottomBarNavigation

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        ApiHttpClient.initialize(
            applicationContext
        )

        setContent {
            LaranjadaTheme {
                val sessionViewModel:
                        AppSessionViewModel =
                    viewModel()

                when (
                    val sessionState =
                        sessionViewModel
                            .sessionState
                ) {
                    AppSessionState.Checking -> {
                        SessionLoadingScreen()
                    }

                    AppSessionState.Unauthenticated -> {
                        LoginScreen(
                            onLoginSuccess =
                                sessionViewModel::
                                onLoginSuccess
                        )
                    }

                    is AppSessionState.Authenticated -> {
                        AuthenticatedApp(
                            sessionState =
                                sessionState,

                            isLoggingOut =
                                sessionViewModel
                                    .isLoggingOut,

                            logoutErrorMessage =
                                sessionViewModel
                                    .logoutErrorMessage,

                            onLogoutClick =
                                sessionViewModel::
                                logout
                        )
                    }

                    is AppSessionState.Unavailable -> {
                        SessionUnavailableScreen(
                            message =
                                sessionState.message,

                            onRetryClick =
                                sessionViewModel::
                                checkSession
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedApp(
    sessionState:
    AppSessionState.Authenticated,
    isLoggingOut: Boolean,
    logoutErrorMessage: String?,
    onLogoutClick: () -> Unit
) {
    val context =
        LocalContext.current

    val navController =
        rememberNavController()

    val profileViewModel:
            ProfileViewModel =
        viewModel()

    val profileState =
        profileViewModel.uiState

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

    val canNavigate =
        profileState.isLoading ==
                false &&
                selectedProfile != null

    val backStackEntry by
    navController
        .currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry
            ?.destination
            ?.route

    val showBottomBar =
        canNavigate &&
                currentRoute in
                setOf(
                    AppRoutes.Home.route,
                    AppRoutes.Search.route,
                    AppRoutes.Favorites.route,
                    AppRoutes.Account.route,
                    AppRoutes.Detail.route,
                    AppRoutes.Collection.route,
                    AppRoutes.MediaGrid.route
                )

    val selectedBottomIndex =
        when (
            currentRoute
        ) {
            AppRoutes.Search.route ->
                1

            AppRoutes.Favorites.route ->
                2

            AppRoutes.Account.route ->
                3

            else ->
                0
        }

    /*
     * Toast de boas-vindas continua
     * aparecendo apenas uma vez.
     */
    var welcomeShown by
    remember {
        mutableStateOf(
            false
        )
    }

    LaunchedEffect(
        canNavigate,
        selectedProfile?.uuid
    ) {
        if (
            canNavigate &&
            welcomeShown == false
        ) {
            welcomeShown =
                true

            val profileName =
                selectedProfile
                    ?.name
                    .orEmpty()

            val message =
                if (
                    profileName.isBlank()
                ) {
                    "🍊 Bem-vindo ao Laranjada!"
                } else {
                    "🍊 Bem-vindo ao Laranjada, $profileName!"
                }

            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    val navigationContext =
        BottomBarNavigationContext(
            selectedIndex =
                selectedBottomIndex,

            profileName =
                selectedProfile
                    ?.name
                    .orEmpty(),

            profileAvatarUrl =
                selectedProfile
                    ?.avatar
                    ?.imageUrl,

            /*
             * Search agora existe
             * de verdade.
             */
            searchEnabled =
                true,

            onNavigate = {
                    index ->

                if (canNavigate) {
                    val destination =
                        when (
                            index
                        ) {
                            0 ->
                                AppRoutes
                                    .Home
                                    .route

                            1 ->
                                AppRoutes
                                    .Search
                                    .route

                            2 ->
                                AppRoutes
                                    .Favorites
                                    .route

                            3 ->
                                AppRoutes
                                    .Account
                                    .route

                            else ->
                                null
                        }

                    if (
                        destination != null &&
                        currentRoute !=
                        destination
                    ) {
                        if (
                            destination ==
                            AppRoutes.Home.route
                        ) {
                            val returned =
                                navController
                                    .popBackStack(
                                        AppRoutes
                                            .Home
                                            .route,

                                        false
                                    )

                            if (
                                returned ==
                                false
                            ) {
                                navController.navigate(
                                    AppRoutes
                                        .Home
                                        .route
                                ) {
                                    launchSingleTop =
                                        true
                                }
                            }
                        } else {
                            navController.navigate(
                                destination
                            ) {
                                /*
                                 * Home continua sendo
                                 * a raiz das seções
                                 * principais.
                                 */
                                popUpTo(
                                    AppRoutes
                                        .Home
                                        .route
                                ) {
                                    inclusive =
                                        false
                                }

                                launchSingleTop =
                                    true
                            }
                        }
                    }
                }
            }
        )

    CompositionLocalProvider(
        LocalBottomBarNavigation
                provides
                navigationContext
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        LaranjadaBlack
                    )
        ) {
            Box(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                LaranjadaNavGraph(
                    navController =
                        navController,

                    currentUser =
                        sessionState.user,

                    isLoggingOut =
                        isLoggingOut,

                    logoutErrorMessage =
                        logoutErrorMessage,

                    onLogoutClick =
                        onLogoutClick
                )
            }

            if (
                showBottomBar
            ) {
                HomeBottomBar(
                    selectedIndex =
                        selectedBottomIndex,

                    profileName =
                        selectedProfile
                            ?.name
                            .orEmpty(),

                    profileAvatarUrl =
                        selectedProfile
                            ?.avatar
                            ?.imageUrl,

                    modifier =
                        Modifier
                            .fillMaxWidth(),

                    isHostBar =
                        true
                )
            }
        }
    }
}

@Composable
private fun SessionLoadingScreen() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    LaranjadaBlack
                ),

        contentAlignment =
            Alignment.Center
    ) {
        CircularProgressIndicator(
            color =
                LaranjadaOrange
        )
    }
}

@Composable
private fun SessionUnavailableScreen(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    LaranjadaBlack
                )
                .padding(
                    28.dp
                ),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                "Não foi possível verificar sua sessão.",

            color =
                LaranjadaText,

            textAlign =
                TextAlign.Center
        )

        Text(
            text =
                message,

            color =
                Color.White.copy(
                    alpha = 0.65f
                ),

            textAlign =
                TextAlign.Center,

            modifier =
                Modifier.padding(
                    top = 8.dp,
                    bottom = 20.dp
                )
        )

        Button(
            onClick =
                onRetryClick,

            colors =
                ButtonDefaults
                    .buttonColors(
                        containerColor =
                            LaranjadaOrange,

                        contentColor =
                            Color.White
                    )
        ) {
            Text(
                text =
                    "TENTAR NOVAMENTE"
            )
        }
    }
}