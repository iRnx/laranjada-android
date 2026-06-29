package com.rnx.laranjada.core.network

object ApiConfig {
//    const val BASE_URL = "http://10.0.2.2:8000"
    const val BASE_URL = "http://127.0.0.1:8000"
    fun buildUrl(path: String): String {
        val normalizedBaseUrl = BASE_URL.trimEnd('/')
        val normalizedPath = if (path.startsWith("/")) path else "/$path"

        return "$normalizedBaseUrl$normalizedPath"
    }
}