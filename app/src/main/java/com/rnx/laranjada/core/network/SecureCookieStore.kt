package com.rnx.laranjada.core.network

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Persistência de cookies protegidos pelo Android Keystore (AES-GCM). */
class SecureCookieStore(context: Context) {
    companion object {
        private const val PREFERENCES_NAME = "laranjada_secure_cookies"
        private const val KEY_ALIAS = "laranjada_cookie_key"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private val MAX_AGE_ZERO_REGEX = Regex(
            """(?i)(?:^|;)\s*max-age\s*=\s*0(?:;|$)"""
        )
    }

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    private val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    @Synchronized
    fun saveFromResponseHeaders(headers: Map<String?, List<String>>) {
        val setCookieValues = headers.filterKeys {
            it?.equals("Set-Cookie", ignoreCase = true) == true
        }.values.flatten()
        if (setCookieValues.isEmpty()) return
        val editor = preferences.edit()
        setCookieValues.forEach { rawCookie ->
            val cookiePair = rawCookie.substringBefore(';')
            val separatorIndex = cookiePair.indexOf('=')
            if (separatorIndex <= 0) return@forEach
            val name = cookiePair.substring(0, separatorIndex).trim()
            val value = cookiePair.substring(separatorIndex + 1).trim()
            if (name.isBlank()) return@forEach
            if (value.isBlank() || MAX_AGE_ZERO_REGEX.containsMatchIn(rawCookie)) {
                editor.remove(name)
            } else {
                editor.putString(name, encrypt(value))
            }
        }
        editor.commit()
    }

    @Synchronized
    fun getCookie(name: String): String? {
        val encrypted = preferences.getString(name, null) ?: return null
        val decrypted = decrypt(encrypted)
        if (decrypted == null) preferences.edit().remove(name).commit()
        return decrypted
    }

    @Synchronized
    fun buildCookieHeader(): String = preferences.all.mapNotNull { (name, stored) ->
        val raw = stored as? String ?: return@mapNotNull null
        val plain = decrypt(raw)
        if (plain == null) {
            preferences.edit().remove(name).commit()
            return@mapNotNull null
        }
        "$name=$plain"
    }.sorted().joinToString("; ")

    @Synchronized
    fun hasSessionCookie(): Boolean = !getCookie("sessionid").isNullOrBlank()

    /** Logout normal mantém cookies não autenticados do mesmo domínio. */
    @Synchronized
    fun clearAuthentication() {
        preferences.edit().remove("sessionid").remove("csrftoken").commit()
    }

    /** Uma nova origem NÃO pode receber cookies nem id de dispositivo anterior. */
    @Synchronized
    fun clearForDomainChange() {
        preferences.edit().clear().commit()
    }

    private fun encrypt(text: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encrypted = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val value = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        return "$iv.$value"
    }

    private fun decrypt(value: String): String? = runCatching {
        val parts = value.split('.', limit = 2)
        require(parts.size == 2)
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val bytes = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(128, iv)
        )
        String(cipher.doFinal(bytes), Charsets.UTF_8)
    }.getOrNull()

    private fun getOrCreateSecretKey(): SecretKey {
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE
        )
        val specification = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        generator.init(specification)
        return generator.generateKey()
    }
}
