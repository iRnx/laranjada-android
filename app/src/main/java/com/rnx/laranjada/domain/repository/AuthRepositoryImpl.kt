package com.rnx.laranjada.data.repository

import com.rnx.laranjada.data.mapper.AuthMapper
import com.rnx.laranjada.data.remote.api.AuthApiService
import com.rnx.laranjada.domain.model.AuthenticatedUser
import com.rnx.laranjada.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val apiService: AuthApiService = AuthApiService
) : AuthRepository {

    override suspend fun login(
        usernameOrEmail: String,
        password: String
    ): AuthenticatedUser {
        val response = apiService.login(
            usernameOrEmail = usernameOrEmail,
            password = password
        )

        return AuthMapper.fromResponse(
            response
        )
    }

    override suspend fun getCurrentUser(): AuthenticatedUser? {
        val response = apiService.getCurrentUser()
            ?: return null

        return AuthMapper.fromResponse(
            response
        )
    }

    override suspend fun logout() {
        apiService.logout()
    }
}