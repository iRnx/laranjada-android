package com.rnx.laranjada.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.data.repository.AuthRepositoryImpl
import com.rnx.laranjada.domain.model.AuthenticatedUser
import com.rnx.laranjada.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

sealed interface AppSessionState {

    data object Checking :
        AppSessionState

    data object Unauthenticated :
        AppSessionState

    data object Disconnected :
        AppSessionState

    data class Authenticated(
        val user:
        AuthenticatedUser,
        val profileSessionKey:
        Long
    ) : AppSessionState

    data class Unavailable(
        val message:
        String
    ) : AppSessionState
}

class AppSessionViewModel(
    private val repository:
    AuthRepository =
        AuthRepositoryImpl()
) : ViewModel() {

    var sessionState by
    mutableStateOf<
            AppSessionState
            >(
        AppSessionState.Checking
    )
        private set

    var isLoggingOut by
    mutableStateOf(
        false
    )
        private set

    var logoutErrorMessage by
    mutableStateOf<String?>(
        null
    )
        private set

    private var sessionGeneration = 0L

    init {
        checkSession()
    }

    fun checkSession() {
        val generation = ++sessionGeneration
        viewModelScope.launch {
            sessionState =
                AppSessionState.Checking

            try {
                val user =
                    repository
                        .getCurrentUser()

                if (generation != sessionGeneration) return@launch

                sessionState =
                    if (
                        user == null
                    ) {
                        AppSessionState
                            .Unauthenticated
                    } else {
                        AppSessionState
                            .Authenticated(
                                user =
                                    user,
                                profileSessionKey =
                                    generation
                            )
                    }

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (
                exception:
                Exception
            ) {
                if (generation != sessionGeneration) return@launch
                sessionState =
                    AppSessionState
                        .Unavailable(
                            message =
                                exception.message
                                    ?: "Não foi possível verificar sua sessão."
                        )
            }
        }
    }

    fun handleRemoteDisconnection() {
        ++sessionGeneration
        ApiHttpClient.clearAuthenticationCookies()
        isLoggingOut = false
        logoutErrorMessage = null
        sessionState = AppSessionState.Disconnected
    }

    fun continueAfterRemoteDisconnection() {
        if (sessionState != AppSessionState.Disconnected) return
        ++sessionGeneration
        sessionState = AppSessionState.Unauthenticated
    }

    fun onLoginSuccess(
        user:
        AuthenticatedUser
    ) {
        val generation =
            ++sessionGeneration

        logoutErrorMessage =
            null

        sessionState =
            AppSessionState
                .Authenticated(
                    user =
                        user,
                    profileSessionKey =
                        generation
                )
    }

    fun onSessionEnded() {
        ++sessionGeneration
        ApiHttpClient
            .clearAuthenticationCookies()

        isLoggingOut =
            false

        logoutErrorMessage =
            null

        sessionState =
            AppSessionState
                .Unauthenticated
    }

    fun logout() {
        if (
            isLoggingOut
        ) {
            return
        }

        val generation = sessionGeneration
        viewModelScope.launch {
            isLoggingOut =
                true

            logoutErrorMessage =
                null

            try {
                repository.logout()
                if (generation != sessionGeneration) return@launch

                ++sessionGeneration
                sessionState =
                    AppSessionState
                        .Unauthenticated

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (
                exception:
                Exception
            ) {
                if (generation != sessionGeneration) return@launch
                logoutErrorMessage =
                    exception.message
                        ?: "Não foi possível encerrar a sessão."

            } finally {
                isLoggingOut =
                    false
            }
        }
    }
}