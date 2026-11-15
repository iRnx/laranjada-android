package com.rnx.laranjada.core.network

import android.content.Context
import android.util.Base64
import com.rnx.laranjada.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Locale

/** Ambiente autorizado, extraído somente de manifesto verificado criptograficamente. */
data class VerifiedDomainEnvironment(
    val environment: String,
    val primaryDomain: String,
    val domains: List<String>
)

object DomainDiscoveryClient {
    private const val BOOTSTRAP_URL =
        "https://laranjada-domain-discovery.laranjadaplus.workers.dev/v1/domains"
    private const val TRUSTED_KEY_ID = "domain-discovery-v1"
    private const val SIGNATURE_ALGORITHM = "ECDSA_SHA_256"
    private const val MAX_MANIFEST_BYTES = 8192
    private val hostnamePattern = Regex(
        """^(?=.{1,253}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$"""
    )

    suspend fun fetchVerified(
        context: Context,
        environment: String
    ): VerifiedDomainEnvironment = withContext(Dispatchers.IO) {
        require(environment == "hml" || environment == "prod") {
            "Domain Discovery não é utilizado em localDebug."
        }
        val connection = URL(BOOTSTRAP_URL).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                throw IOException("Domain Discovery retornou HTTP $status.")
            }
            val keyId = connection.getHeaderField("X-Laranjada-Key-Id").orEmpty()
            val algorithm = connection.getHeaderField("X-Laranjada-Signature-Algorithm").orEmpty()
            val signature = connection.getHeaderField("X-Laranjada-Signature").orEmpty()
            if (keyId != TRUSTED_KEY_ID) throw SecurityException("Chave de assinatura não reconhecida.")
            if (algorithm != SIGNATURE_ALGORITHM) throw SecurityException("Algoritmo não autorizado.")
            if (signature.isBlank()) throw SecurityException("Manifesto sem assinatura.")

            val bytes = connection.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(2048)
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    if (n == 0) continue
                    if (output.size() + n > MAX_MANIFEST_BYTES) {
                        throw SecurityException("Manifesto excede 8192 bytes.")
                    }
                    output.write(buffer, 0, n)
                }
                output.toByteArray()
            }
            if (bytes.isEmpty()) throw SecurityException("Manifesto vazio.")
            verifySignature(context, bytes, signature)
            // Somente interpretamos JSON após verificar os bytes originais.
            val json = try {
                JSONObject(String(bytes, Charsets.UTF_8))
            } catch (ex: Exception) {
                throw SecurityException("Manifesto JSON inválido.", ex)
            }
            if (json.optInt("schema_version", -1) != 1) {
                throw SecurityException("Versão de manifesto desconhecida.")
            }
            val envs = json.optJSONObject("environments")
                ?: throw SecurityException("Ambientes ausentes.")
            val env = envs.optJSONObject(environment)
                ?: throw SecurityException("Ambiente não encontrado.")
            val primary = validateHostname(env.optString("primary_domain"))
            val array = env.optJSONArray("domains")
                ?: throw SecurityException("Lista de domínios ausente.")
            if (array.length() == 0) throw SecurityException("Lista de domínios vazia.")
            val domains = buildList {
                for (i in 0 until array.length()) {
                    val raw = array.opt(i) as? String
                        ?: throw SecurityException("Domínio não é uma string.")
                    val hostname = validateHostname(raw)
                    if (hostname !in this) add(hostname)
                }
            }
            if (primary !in domains) throw SecurityException("Domínio principal não autorizado.")
            VerifiedDomainEnvironment(environment, primary, domains)
        } finally {
            connection.disconnect()
        }
    }

    private fun verifySignature(context: Context, body: ByteArray, signatureBase64: String) {
        try {
            val keyText = context.resources.openRawResource(R.raw.domain_discovery_public_key)
                .bufferedReader(Charsets.UTF_8).use { it.readText().trim() }
            val keyBytes = Base64.decode(keyText, Base64.DEFAULT)
            val publicKey = KeyFactory.getInstance("EC")
                .generatePublic(X509EncodedKeySpec(keyBytes))
            val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
            val verifier = Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(publicKey)
            verifier.update(body)
            if (!verifier.verify(signatureBytes)) {
                throw SecurityException("Assinatura do manifesto inválida.")
            }
        } catch (ex: SecurityException) {
            throw ex
        } catch (ex: Exception) {
            throw SecurityException("Não foi possível validar o manifesto.", ex)
        }
    }

    private fun validateHostname(raw: String): String {
        val hostname = raw.trim()
        if (hostname != raw || hostname != hostname.lowercase(Locale.ROOT) ||
            !hostnamePattern.matches(hostname)
        ) throw SecurityException("Hostname inválido no manifesto.")
        return hostname
    }
}
