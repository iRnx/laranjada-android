package com.rnx.laranjada.core.network

import com.rnx.laranjada.BuildConfig
import java.net.URI

/** URLs centralizadas. A variante local nunca muda seu endereço. */
object ApiConfig {
    val ENVIRONMENT: String = BuildConfig.APP_ENVIRONMENT
    val isLocal: Boolean get() = ENVIRONMENT == "local"
    val isHomologation: Boolean get() = ENVIRONMENT == "hml"
    val isProduction: Boolean get() = ENVIRONMENT == "prod"

    private val initialApiUrl = BuildConfig.API_BASE_URL.trimEnd('/')
    private val initialMediaUrl = BuildConfig.MEDIA_BASE_URL.trimEnd('/')

    @Volatile private var currentApiUrl = initialApiUrl

    val BASE_URL: String get() = currentApiUrl
    val MEDIA_BASE_URL: String get() = if (isLocal) initialMediaUrl else currentApiUrl
    val activeDomain: String
        get() = runCatching { URI(BASE_URL).host.orEmpty() }.getOrDefault("")

    /** Somente DomainRecoveryCoordinator deve provocar uma mudança. */
    internal fun activateDomain(host: String): Boolean {
        if (isLocal) return false
        val target = "https://$host"
        if (currentApiUrl == target) return false
        currentApiUrl = target
        return true
    }

    fun buildUrl(path: String): String = join(BASE_URL, path)
    fun buildMediaUrl(path: String): String = join(MEDIA_BASE_URL, path)

    private fun join(base: String, path: String): String =
        base.trimEnd('/') + if (path.startsWith('/')) path else "/$path"
}
