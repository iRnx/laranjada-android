package com.rnx.laranjada.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.AuthRepositoryImpl
import com.rnx.laranjada.domain.model.AuthenticatedUser
import com.rnx.laranjada.domain.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(
        LoginUiState()
    )
        private set

    var authenticatedUser by mutableStateOf<AuthenticatedUser?>(
        null
    )
        private set

    fun onUsernameOrEmailChanged(
        value: String
    ) {
        uiState = uiState.copy(
            usernameOrEmail = value,
            usernameError = null,
            errorMessage = null
        )
    }

    fun onPasswordChanged(
        value: String
    ) {
        uiState = uiState.copy(
            password = value,
            passwordError = null,
            errorMessage = null
        )
    }

    fun togglePasswordVisibility() {
        uiState = uiState.copy(
            passwordVisible = !uiState.passwordVisible
        )
    }

    fun login() {
        if (uiState.isLoading) {
            return
        }

        val usernameOrEmail = uiState.usernameOrEmail.trim()
        val password = uiState.password

        val usernameError = if (
            usernameOrEmail.isBlank()
        ) {
            "Informe seu e-mail ou nome de usuário."
        } else {
            null
        }

        val passwordError = if (
            password.isBlank()
        ) {
            "Informe sua senha."
        } else {
            null
        }

        if (
            usernameError != null ||
            passwordError != null
        ) {
            uiState = uiState.copy(
                usernameError = usernameError,
                passwordError = passwordError
            )

            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                authenticatedUser = repository.login(
                    usernameOrEmail = usernameOrEmail,
                    password = password
                )
            } catch (exception: Exception) {
                uiState = uiState.copy(
                    errorMessage = exception.message
                        ?: "Não foi possível entrar na sua conta."
                )
            } finally {
                uiState = uiState.copy(
                    isLoading = false
                )
            }
        }
    }

    fun consumeAuthenticatedUser() {
        authenticatedUser = null
    }
}