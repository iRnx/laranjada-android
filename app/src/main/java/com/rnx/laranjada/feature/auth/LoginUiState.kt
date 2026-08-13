package com.rnx.laranjada.feature.auth

data class LoginUiState(
    val usernameOrEmail: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null
)