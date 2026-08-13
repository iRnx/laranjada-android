package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.domain.model.AuthenticatedUser

interface AuthRepository {

    suspend fun login(
        usernameOrEmail: String,
        password: String
    ): AuthenticatedUser

    suspend fun getCurrentUser(): AuthenticatedUser?

    suspend fun logout()
}