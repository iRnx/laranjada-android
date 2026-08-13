package com.rnx.laranjada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.core.design.theme.LaranjadaTheme
import com.rnx.laranjada.core.navigation.LaranjadaNavGraph
import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.feature.auth.AppSessionState
import com.rnx.laranjada.feature.auth.AppSessionViewModel
import com.rnx.laranjada.feature.auth.LoginScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        ApiHttpClient.initialize(
            applicationContext
        )

        setContent {
            LaranjadaTheme {
                val sessionViewModel:
                        AppSessionViewModel = viewModel()

                when (
                    val sessionState =
                        sessionViewModel.sessionState
                ) {
                    AppSessionState.Checking -> {
                        SessionLoadingScreen()
                    }

                    AppSessionState.Unauthenticated -> {
                        LoginScreen(
                            onLoginSuccess =
                                sessionViewModel::onLoginSuccess
                        )
                    }

                    is AppSessionState.Authenticated -> {
                        val navController =
                            rememberNavController()

                        LaranjadaNavGraph(
                            navController = navController,
                            currentUser = sessionState.user,
                            isLoggingOut =
                                sessionViewModel.isLoggingOut,
                            logoutErrorMessage =
                                sessionViewModel.logoutErrorMessage,
                            onLogoutClick =
                                sessionViewModel::logout
                        )
                    }

                    is AppSessionState.Unavailable -> {
                        SessionUnavailableScreen(
                            message =
                                sessionState.message,
                            onRetryClick =
                                sessionViewModel::checkSession
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                LaranjadaBlack
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = LaranjadaOrange
        )
    }
}

@Composable
private fun SessionUnavailableScreen(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                LaranjadaBlack
            )
            .padding(
                28.dp
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Não foi possível verificar sua sessão.",
            color = LaranjadaText,
            textAlign = TextAlign.Center
        )

        Text(
            text = message,
            color = Color.White.copy(
                alpha = 0.65f
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 20.dp
            )
        )

        Button(
            onClick = onRetryClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = LaranjadaOrange,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "TENTAR NOVAMENTE"
            )
        }
    }
}