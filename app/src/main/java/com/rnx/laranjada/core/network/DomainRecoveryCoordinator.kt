package com.rnx.laranjada.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private const val DOMAIN_LOG = "LaranjadaDomain"

enum class DomainRecoveryPhase {
    READY, CHECKING_CURRENT, RECOVERING, NO_NETWORK, WAITING_FOR_DOMAIN, SECURITY_ERROR
}

data class DomainRecoveryStatus(
    val phase: DomainRecoveryPhase = DomainRecoveryPhase.READY,
    val description: String = ""
)

/** Recovery de API apenas; não reescreve URLs absolutas de playback HLS. */
object DomainRecoveryCoordinator {
    private const val PREFS = "laranjada_mobile_domain_recovery_v1"
    private const val LAST_GOOD = "last_known_good_domain"
    private const val HEALTH_PATH = "/api/v1/auth/csrf/"
    private val allowedHost = Regex(
        """^(?=.{1,253}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$"""
    )
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()
    private lateinit var context: Context
    private var task: Job? = null

    private val _status = MutableStateFlow(DomainRecoveryStatus())
    val status: StateFlow<DomainRecoveryStatus> = _status.asStateFlow()
    private val _domainChange = MutableStateFlow(0L)
    val domainChange: StateFlow<Long> = _domainChange.asStateFlow()
    private val _connectionRestored = MutableStateFlow(0L)
    val connectionRestored: StateFlow<Long> = _connectionRestored.asStateFlow()

    fun initialize(appContext: Context) {
        synchronized(lock) {
            if (::context.isInitialized) return
            context = appContext.applicationContext
            // O domínio antigo salvo não é ativado automaticamente: primeiro
            // precisa constar em um manifesto assinado e responder ao health.
        }
    }

    fun onApiTransportFailure(url: URL, exception: IOException) {
        if (ApiConfig.isLocal || !::context.isInitialized) return
        if (url.host != ApiConfig.activeDomain) return
        if (exception is java.io.InterruptedIOException && Thread.currentThread().isInterrupted) return
        if (exception.message?.contains("CSRF", ignoreCase = true) == true) return
        if (exception.message?.startsWith("O domínio da API mudou") == true) return
        Log.w(DOMAIN_LOG, "API_TRANSPORT_ERROR host=${url.host}", exception)
        startRecovery(false)
    }

    fun onApiResponse(url: URL, status: Int) {
        if (ApiConfig.isLocal || !::context.isInitialized) return
        if (url.host != ApiConfig.activeDomain) return
        if (status in setOf(500, 502, 503, 504, 520, 521, 522, 523, 524, 525, 526)) {
            startRecovery(false)
        } else if (status in 200..499) {
            saveLastGood(url.host)
        }
    }

    fun retryNow() {
        if (!ApiConfig.isLocal && ::context.isInitialized) startRecovery(true)
    }

    fun resumeIfNecessary() {
        if (!ApiConfig.isLocal && ::context.isInitialized &&
            _status.value.phase != DomainRecoveryPhase.READY
        ) startRecovery(false)
    }

    fun pause() = synchronized(lock) {
        task?.cancel()
        task = null
        Unit
    }

    private fun startRecovery(force: Boolean) = synchronized(lock) {
        if (!force && task?.isActive == true) return@synchronized
        if (force) task?.cancel()
        task = scope.launch { recover() }
    }

    private suspend fun recover() {
        val failedHost = ApiConfig.activeDomain
        val waits = longArrayOf(0L, 5_000L, 15_000L, 30_000L, 60_000L)
        try {
            _status.value = DomainRecoveryStatus(DomainRecoveryPhase.CHECKING_CURRENT)
            if (!hasInternet()) {
                _status.value = DomainRecoveryStatus(DomainRecoveryPhase.NO_NETWORK)
            } else {
                if (probe(failedHost)) { restored(); return }
                delay(1_500L)
                if (probe(failedHost)) { restored(); return }
            }

            var attempt = 0
            while (true) {
                delay(waits[attempt.coerceAtMost(waits.lastIndex)])
                attempt++
                if (!hasInternet()) {
                    _status.value = DomainRecoveryStatus(DomainRecoveryPhase.NO_NETWORK)
                    continue
                }
                _status.value = DomainRecoveryStatus(DomainRecoveryPhase.RECOVERING)
                if (attempt > 1 && probe(ApiConfig.activeDomain)) {
                    restored()
                    return
                }

                val manifest = try {
                    DomainDiscoveryClient.fetchVerified(context, ApiConfig.ENVIRONMENT)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (security: SecurityException) {
                    Log.e(DOMAIN_LOG, "DOMAIN_SIGNATURE_REJECTED", security)
                    _status.value = DomainRecoveryStatus(DomainRecoveryPhase.SECURITY_ERROR)
                    return
                } catch (ex: Exception) {
                    Log.w(DOMAIN_LOG, "DOMAIN_DISCOVERY_FAILED", ex)
                    _status.value = DomainRecoveryStatus(DomainRecoveryPhase.WAITING_FOR_DOMAIN)
                    continue
                }

                // Último domínio é APENAS uma dica. É obrigatório estar no
                // manifesto assinado para o ambiente ativo desta build.
                val lastGood = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getString(LAST_GOOD, null)
                val authorized = (listOf(manifest.primaryDomain) + manifest.domains).distinct()
                val candidates = (listOf(manifest.primaryDomain) + listOfNotNull(lastGood) + authorized)
                    .distinct()
                    .filter { it in authorized && isValidHost(it) }
                    .filter { it != failedHost } +
                    authorized.filter { it == failedHost && isValidHost(it) }

                var chosen: String? = null
                for (candidate in candidates) {
                    if (probe(candidate)) {
                        chosen = candidate
                        break
                    }
                }
                if (chosen == null) {
                    _status.value = DomainRecoveryStatus(DomainRecoveryPhase.WAITING_FOR_DOMAIN)
                    continue
                }
                if (chosen == ApiConfig.activeDomain) {
                    restored()
                    return
                }

                if (ApiHttpClient.activateNewDomain(chosen)) {
                    saveLastGood(chosen)
                    _domainChange.update { it + 1L }
                    Log.i(DOMAIN_LOG, "DOMAIN_RECOVERY_COMPLETED host=$chosen")
                }
                _status.value = DomainRecoveryStatus(DomainRecoveryPhase.READY)
                return
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (ex: Exception) {
            Log.e(DOMAIN_LOG, "DOMAIN_RECOVERY_ERROR", ex)
            _status.value = DomainRecoveryStatus(DomainRecoveryPhase.WAITING_FOR_DOMAIN)
        }
    }

    private fun restored() {
        _status.value = DomainRecoveryStatus(DomainRecoveryPhase.READY)
        _connectionRestored.update { it + 1L }
        Log.i(DOMAIN_LOG, "DOMAIN_CURRENT_RESTORED host=${ApiConfig.activeDomain}")
    }

    private fun saveLastGood(host: String) {
        if (!isValidHost(host)) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(LAST_GOOD, host).apply()
    }

    private fun hasInternet(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun isValidHost(host: String): Boolean =
        host == host.lowercase(Locale.ROOT) && allowedHost.matches(host)

    private suspend fun probe(host: String): Boolean = withContext(Dispatchers.IO) {
        if (!isValidHost(host)) return@withContext false
        val connection = URL("https://$host$HEALTH_PATH").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 6_000
            connection.readTimeout = 6_000
            connection.setRequestProperty("Accept", "application/json")
            if (connection.responseCode != 200) return@withContext false
            val bytes = connection.inputStream.use { input ->
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    if (n == 0) continue
                    if (out.size() + n > 8192) return@withContext false
                    out.write(buffer, 0, n)
                }
                out.toByteArray()
            }
            if (bytes.isEmpty()) return@withContext false
            val json = JSONObject(String(bytes, Charsets.UTF_8))
            json.optBoolean("ok", false) && json.optString("csrf_token").isNotBlank()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            false
        } finally {
            connection.disconnect()
        }
    }
}
