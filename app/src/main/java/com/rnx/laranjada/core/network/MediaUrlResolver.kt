package com.rnx.laranjada.core.network

object MediaUrlResolver {

    fun resolve(value: String?): String {
        val normalizedValue = value
            ?.trim()
            .orEmpty()

        if (normalizedValue.isBlank()) {
            return ""
        }

        if (
            normalizedValue.startsWith("http://", ignoreCase = true) ||
            normalizedValue.startsWith("https://", ignoreCase = true)
        ) {
            return normalizedValue
        }

        if (normalizedValue.startsWith("//")) {
            return "https:$normalizedValue"
        }

        return ApiConfig.buildMediaUrl(normalizedValue)
    }
}