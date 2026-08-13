package com.rnx.laranjada.domain.model

data class AuthenticatedUser(
    val id: Int,
    val uuid: String,
    val username: String,
    val email: String,
    val name: String,
    val isEmailVerified: Boolean,
    val mustChangePassword: Boolean
) {
    val displayName: String
        get() {
            return name
                .ifBlank { username }
                .ifBlank { email }
        }
}