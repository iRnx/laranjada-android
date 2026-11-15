
package com.rnx.laranjada.feature.update

import com.rnx.laranjada.core.network.ApiConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

/**
 * Cliente publico de atualizacoes.
 *
 * Utiliza os endpoints ja existentes no Django:
 *
 * GET /api/v1/downloads/android-tv/latest/
 *
 * GET /downloads/android-tv/<versao>/
 *
 * Nao envia sessionid, csrftoken ou cookies.
 *
 * O dominio e obtido do ApiConfig e acompanha
 * a recuperacao automatica via Cloudflare.
 */
internal object UpdateRepository {

    private const val MAX_RESPONSE_BYTES = 262_144
    private const val MAX_APK_BYTES = 800L * 1024L * 1024L

    /**
     * Consulta a ultima release publicada.
     *
     * Retorna null quando o Django responder
     * available=false.
     */
    suspend fun latest(): MobileAppRelease? =
        withContext(Dispatchers.IO) {

            val origin = ApiConfig.BASE_URL

            val endpoint = URL(
                "$origin/api/v1/downloads/" +
                        "${MobileUpdateConfig.PLATFORM_SLUG}/latest/"
            )

            val connection =
                endpoint.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.instanceFollowRedirects = false

                connection.connectTimeout = 12_000
                connection.readTimeout = 15_000

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                val statusCode = connection.responseCode

                if (statusCode != HttpURLConnection.HTTP_OK) {
                    throw IOException(
                        "Consulta de atualizacao indisponivel " +
                                "(HTTP $statusCode)."
                    )
                }

                if (ApiConfig.BASE_URL != origin) {
                    throw IOException(
                        "O dominio mudou durante a consulta."
                    )
                }

                val raw = connection.inputStream.use { input ->

                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(4096)

                    while (true) {
                        coroutineContext.ensureActive()

                        val count = input.read(buffer)

                        if (count < 0) {
                            break
                        }

                        if (count == 0) {
                            continue
                        }

                        if (output.size() + count > MAX_RESPONSE_BYTES) {
                            throw IOException(
                                "A resposta da API excede o limite."
                            )
                        }

                        output.write(buffer, 0, count)
                    }

                    output.toByteArray()
                }

                if (ApiConfig.BASE_URL != origin) {
                    throw IOException(
                        "O dominio mudou durante a consulta."
                    )
                }

                val json = JSONObject(
                    String(raw, Charsets.UTF_8)
                )

                MobileUpdateConfig.parseRelease(json)

            } catch (cancelled: CancellationException) {
                throw cancelled
            } finally {
                connection.disconnect()
            }
        }

    /**
     * Verifica a URL retornada pelo Django.
     *
     * O download oficial exige HTTPS.
     * O host deve ser exatamente o dominio ativo.
     * A URL deve corresponder ao slug e versao
     * da release consultada.
     */
    private fun validateDownloadUrl(
        release: MobileAppRelease,
        baseUrl: String
    ): URL {

        val url = URL(release.downloadUrl)
        val base = URL(baseUrl)

        require(
            url.protocol == "https" &&
                    base.protocol == "https"
        ) {
            "Atualizacoes oficiais exigem HTTPS."
        }

        require(
            url.host.equals(base.host, ignoreCase = true) &&
                    url.port == base.port &&
                    url.userInfo == null &&
                    url.query == null &&
                    url.ref == null
        ) {
            "O download nao pertence ao dominio ativo."
        }

        val expectedPath =
            "/downloads/${MobileUpdateConfig.PLATFORM_SLUG}/" +
                    "${release.versionName}/"

        require(url.path == expectedPath) {
            "A URL de download nao corresponde " +
                    "a versao consultada."
        }

        require(release.fileSize in 1L..MAX_APK_BYTES) {
            "O tamanho do APK e invalido ou excede o limite."
        }

        return url
    }

    /**
     * Baixa o APK para o cache privado da aplicacao.
     *
     * Verifica:
     * - URL HTTPS no dominio ativo
     * - Tamanho anunciado
     * - SHA-256
     * - Mudanca de dominio durante o download
     *
     * A validacao de packageName e versionCode
     * continua sendo feita pelo UpdateInstaller.
     */
    suspend fun download(
        cacheDir: File,
        release: MobileAppRelease,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {

        val origin = ApiConfig.BASE_URL
        val url = validateDownloadUrl(release, origin)

        val directory = File(cacheDir, "updates").apply {
            mkdirs()
        }

        val target = File(
            directory,
            "laranjada-mobile-${release.buildNumber}.apk"
        )

        val partial = File(
            directory,
            "laranjada-mobile-${release.buildNumber}.part"
        )

        partial.delete()

        val connection =
            url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = false

            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000

            connection.setRequestProperty(
                "Accept",
                "application/octet-stream"
            )

            connection.setRequestProperty(
                "Accept-Encoding",
                "identity"
            )

            val statusCode = connection.responseCode

            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw IOException(
                    "Download indisponivel (HTTP $statusCode)."
                )
            }

            if (ApiConfig.BASE_URL != origin) {
                throw IOException(
                    "O dominio mudou durante o download."
                )
            }

            val digest = MessageDigest.getInstance("SHA-256")

            var written = 0L
            var lastProgress = -1

            connection.inputStream.use { input ->
                FileOutputStream(partial).use { output ->

                    val buffer = ByteArray(64 * 1024)

                    while (true) {
                        coroutineContext.ensureActive()

                        val count = input.read(buffer)

                        if (count < 0) {
                            break
                        }

                        if (count == 0) {
                            continue
                        }

                        written += count

                        if (written > release.fileSize) {
                            throw IOException(
                                "O APK excede o tamanho anunciado."
                            )
                        }

                        output.write(buffer, 0, count)
                        digest.update(buffer, 0, count)

                        val percent =
                            ((written * 100L) / release.fileSize)
                                .toInt()
                                .coerceIn(0, 100)

                        if (percent != lastProgress) {
                            lastProgress = percent
                            onProgress(percent)
                        }
                    }

                    output.fd.sync()
                }
            }

            if (ApiConfig.BASE_URL != origin) {
                throw IOException(
                    "O dominio mudou durante o download."
                )
            }

            if (written != release.fileSize) {
                throw IOException(
                    "O download do APK esta incompleto."
                )
            }

            val actualHash = digest.digest()
                .joinToString("") {
                    "%02x".format(it.toInt() and 0xff)
                }

            if (!actualHash.equals(
                    release.sha256,
                    ignoreCase = true
                )
            ) {
                throw SecurityException(
                    "O SHA-256 do APK nao corresponde " +
                            "ao publicado pelo Django."
                )
            }

            target.delete()

            if (!partial.renameTo(target)) {
                throw IOException(
                    "Nao foi possivel preparar o APK."
                )
            }

            target

        } catch (error: Exception) {
            partial.delete()
            throw error
        } finally {
            connection.disconnect()
        }
    }
}
