package com.rnx.laranjada.core.network

import com.rnx.laranjada.BuildConfig

object ApiConfig {
    val ENVIRONMENT: String = BuildConfig.APP_ENVIRONMENT
    val BASE_URL: String = BuildConfig.API_BASE_URL.trimEnd('/')
    val MEDIA_BASE_URL: String = BuildConfig.MEDIA_BASE_URL.trimEnd('/')

    fun buildUrl(path: String): String {
        return buildAbsoluteUrl(
            baseUrl = BASE_URL,
            path = path
        )
    }

    fun buildMediaUrl(path: String): String {
        return buildAbsoluteUrl(
            baseUrl = MEDIA_BASE_URL,
            path = path
        )
    }

    private fun buildAbsoluteUrl(
        baseUrl: String,
        path: String
    ): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return "$baseUrl$normalizedPath"
    }
}