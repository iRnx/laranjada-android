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

class SecureCookieStore(
    context: Context
) {

    companion object {
        private const val PREFERENCES_NAME = "laranjada_secure_cookies"
        private const val KEY_ALIAS = "laranjada_cookie_key"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        private val MAX_AGE_ZERO_REGEX = Regex(
            pattern = """(?i)(?:^|;)\s*max-age\s*=\s*0(?:;|$)"""
        )
    }

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val keyStore = KeyStore.getInstance(
        ANDROID_KEY_STORE
    ).apply {
        load(null)
    }

    @Synchronized
    fun saveFromResponseHeaders(
        headers: Map<String?, List<String>>
    ) {
        val setCookieValues = headers
            .filterKeys { key ->
                key?.equals(
                    "Set-Cookie",
                    ignoreCase = true
                ) == true
            }
            .values
            .flatten()

        if (setCookieValues.isEmpty()) {
            return
        }

        val editor = preferences.edit()

        setCookieValues.forEach { rawCookie ->
            val parsedCookie = parseCookie(rawCookie)
                ?: return@forEach

            if (parsedCookie.shouldDelete) {
                editor.remove(parsedCookie.name)
            } else {
                editor.putString(
                    parsedCookie.name,
                    encrypt(parsedCookie.value)
                )
            }
        }

        editor.apply()
    }

    @Synchronized
    fun getCookie(name: String): String? {
        val encryptedValue = preferences.getString(
            name,
            null
        ) ?: return null

        val decryptedValue = decrypt(encryptedValue)

        if (decryptedValue == null) {
            preferences.edit()
                .remove(name)
                .apply()
        }

        return decryptedValue
    }

    @Synchronized
    fun buildCookieHeader(): String {
        return preferences.all
            .mapNotNull { (name, encryptedValue) ->
                val value = encryptedValue as? String
                    ?: return@mapNotNull null

                val decryptedValue = decrypt(value)

                if (decryptedValue == null) {
                    preferences.edit()
                        .remove(name)
                        .apply()

                    return@mapNotNull null
                }

                "$name=$decryptedValue"
            }
            .sorted()
            .joinToString("; ")
    }

    @Synchronized
    fun hasSessionCookie(): Boolean {
        return !getCookie("sessionid").isNullOrBlank()
    }

    @Synchronized
    fun clearAuthentication() {
        preferences.edit()
            .remove("sessionid")
            .remove("csrftoken")
            .apply()
    }

    private fun parseCookie(
        rawCookie: String
    ): ParsedCookie? {
        val cookiePair = rawCookie.substringBefore(";")
        val separatorIndex = cookiePair.indexOf("=")

        if (separatorIndex <= 0) {
            return null
        }

        val name = cookiePair
            .substring(0, separatorIndex)
            .trim()

        val value = cookiePair
            .substring(separatorIndex + 1)
            .trim()

        if (name.isBlank()) {
            return null
        }

        val shouldDelete = value.isBlank() ||
                MAX_AGE_ZERO_REGEX.containsMatchIn(rawCookie)

        return ParsedCookie(
            name = name,
            value = value,
            shouldDelete = shouldDelete
        )
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(
            TRANSFORMATION
        )

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateSecretKey()
        )

        val encryptedBytes = cipher.doFinal(
            value.toByteArray(Charsets.UTF_8)
        )

        val iv = Base64.encodeToString(
            cipher.iv,
            Base64.NO_WRAP
        )

        val encryptedValue = Base64.encodeToString(
            encryptedBytes,
            Base64.NO_WRAP
        )

        return "$iv.$encryptedValue"
    }

    private fun decrypt(
        encryptedValue: String
    ): String? {
        return runCatching {
            val parts = encryptedValue.split(
                ".",
                limit = 2
            )

            if (parts.size != 2) {
                return null
            }

            val iv = Base64.decode(
                parts[0],
                Base64.NO_WRAP
            )

            val encryptedBytes = Base64.decode(
                parts[1],
                Base64.NO_WRAP
            )

            val cipher = Cipher.getInstance(
                TRANSFORMATION
            )

            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(
                    128,
                    iv
                )
            )

            String(
                cipher.doFinal(encryptedBytes),
                Charsets.UTF_8
            )
        }.getOrNull()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val existingKey = keyStore.getKey(
            KEY_ALIAS,
            null
        ) as? SecretKey

        if (existingKey != null) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(
                KeyProperties.BLOCK_MODE_GCM
            )
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keySpec)

        return keyGenerator.generateKey()
    }

    private data class ParsedCookie(
        val name: String,
        val value: String,
        val shouldDelete: Boolean
    )
}