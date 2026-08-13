package com.rnx.laranjada.data.mapper

import com.rnx.laranjada.domain.model.AuthenticatedUser
import org.json.JSONObject
import java.io.IOException

object AuthMapper {

    fun fromResponse(
        json: JSONObject
    ): AuthenticatedUser {
        val userJson = json.optJSONObject(
            "user"
        ) ?: throw IOException(
            "A API não retornou os dados do usuário."
        )

        return fromUserJson(userJson)
    }

    fun fromUserJson(
        json: JSONObject
    ): AuthenticatedUser {
        return AuthenticatedUser(
            id = json.optInt("id"),
            uuid = json.optString("uuid").orEmpty(),
            username = json.optString("username").orEmpty(),
            email = json.optString("email").orEmpty(),
            name = json.optString("name").orEmpty(),
            isEmailVerified = json.optBoolean(
                "is_email_verified",
                false
            ),
            mustChangePassword = json.optBoolean(
                "must_change_password",
                false
            )
        )
    }
}