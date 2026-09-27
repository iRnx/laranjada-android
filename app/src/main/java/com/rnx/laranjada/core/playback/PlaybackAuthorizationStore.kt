package com.rnx.laranjada.core.playback

import android.net.Uri
import com.rnx.laranjada.domain.model.PlaybackAuthorization
import java.net.URI
import java.util.concurrent.atomic.AtomicReference

class PlaybackAuthorizationStore {

    private data class State(
        val scheme: String,
        val host: String,
        val port: Int,
        val allowedPathPrefix: String,
        val authorizationHeader: String
    )

    private val state =
        AtomicReference<State?>(
            null
        )

    fun update(
        playback: PlaybackAuthorization
    ) {
        val parsedUri =
            parseUri(
                playback.url
            )
                ?: throw IllegalArgumentException(
                    "A API retornou uma URL de playback inválida."
                )

        val scheme =
            parsedUri.scheme
                ?.lowercase()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: throw IllegalArgumentException(
                    "A URL de playback não possui protocolo."
                )

        val host =
            parsedUri.host
                ?.lowercase()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: throw IllegalArgumentException(
                    "A URL de playback não possui host."
                )

        val path =
            parsedUri.path
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: throw IllegalArgumentException(
                    "A URL de playback não possui caminho."
                )

        val lastSlash =
            path.lastIndexOf(
                '/'
            )

        val allowedPathPrefix =
            if (
                lastSlash >= 0
            ) {
                path.substring(
                    0,
                    lastSlash + 1
                )
            } else {
                "/"
            }

        val tokenType =
            playback.tokenType
                .trim()
                .ifBlank {
                    "Bearer"
                }

        val authorization =
            playback.authorization
                .trim()

        if (
            authorization.isBlank()
        ) {
            throw IllegalArgumentException(
                "A API não retornou uma autorização de playback."
            )
        }

        state.set(
            State(
                scheme =
                    scheme,

                host =
                    host,

                port =
                    effectivePort(
                        parsedUri
                    ),

                allowedPathPrefix =
                    allowedPathPrefix,

                authorizationHeader =
                    "$tokenType $authorization"
            )
        )
    }

    fun authorizationHeaderFor(
        uri: Uri
    ): String? {
        val currentState =
            state.get()
                ?: return null

        val requestUri =
            parseUri(
                uri.toString()
            )
                ?: return null

        val requestScheme =
            requestUri.scheme
                ?.lowercase()
                ?: return null

        val requestHost =
            requestUri.host
                ?.lowercase()
                ?: return null

        if (
            requestScheme !=
            currentState.scheme
        ) {
            return null
        }

        if (
            requestHost !=
            currentState.host
        ) {
            return null
        }

        if (
            effectivePort(
                requestUri
            ) !=
            currentState.port
        ) {
            return null
        }

        val requestPath =
            requestUri.path
                ?: return null

        if (
            !requestPath.startsWith(
                currentState
                    .allowedPathPrefix
            )
        ) {
            return null
        }

        return currentState
            .authorizationHeader
    }

    fun clear() {
        state.set(
            null
        )
    }

    private fun parseUri(
        value: String
    ): URI? {
        return runCatching {
            URI(
                value
            ).normalize()
        }.getOrNull()
    }

    private fun effectivePort(
        uri: URI
    ): Int {
        if (
            uri.port >= 0
        ) {
            return uri.port
        }

        return when (
            uri.scheme
                ?.lowercase()
        ) {
            "https" ->
                443

            "http" ->
                80

            else ->
                -1
        }
    }
}