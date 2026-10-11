package com.rnx.laranjada.data.remote.api

import com.rnx.laranjada.core.network.ApiHttpClient
import com.rnx.laranjada.core.network.ApiHttpResponse
import org.json.JSONObject
import java.io.IOException

class SubscriptionApiException(
    val statusCode: Int,
    val code: String?,
    message: String
) : IOException(
    message
)

object SubscriptionApiService {

    private const val SUBSCRIPTION_PATH =
        "/api/v1/subscription/"

    suspend fun getOverview():
            JSONObject {

        val response =
            ApiHttpClient.get(
                SUBSCRIPTION_PATH
            )

        if (
            response.statusCode == 200
        ) {
            return response.jsonObject()
        }

        throw buildException(
            response = response,
            fallbackMessage =
                "Não foi possível carregar sua assinatura."
        )
    }

    private fun buildException(
        response: ApiHttpResponse,
        fallbackMessage: String
    ): SubscriptionApiException {

        val json =
            runCatching {
                response.jsonObject()
            }
                .getOrNull()

        val code =
            json
                ?.optString(
                    "code"
                )
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val message =
            json
                ?.optString(
                    "message"
                )
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: json
                    ?.optString(
                        "detail"
                    )
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                ?: fallbackMessage

        return SubscriptionApiException(
            statusCode =
                response.statusCode,

            code =
                code,

            message =
                message
        )
    }
}