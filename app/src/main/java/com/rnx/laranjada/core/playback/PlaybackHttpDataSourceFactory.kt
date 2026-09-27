package com.rnx.laranjada.core.playback

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener

@OptIn(
    UnstableApi::class
)
class PlaybackHttpDataSourceFactory(
    private val authorizationStore:
    PlaybackAuthorizationStore
) : DataSource.Factory {

    private val upstreamFactory =
        DefaultHttpDataSource
            .Factory()
            .setUserAgent(
                "Laranjada-Android"
            )
            .setConnectTimeoutMs(
                15_000
            )
            .setReadTimeoutMs(
                15_000
            )
            .setAllowCrossProtocolRedirects(
                false
            )

    override fun createDataSource():
            DataSource {
        return PlaybackHttpDataSource(
            upstream =
                upstreamFactory
                    .createDataSource(),

            authorizationStore =
                authorizationStore
        )
    }
}

@OptIn(
    UnstableApi::class
)
private class PlaybackHttpDataSource(
    private val upstream:
    DefaultHttpDataSource,

    private val authorizationStore:
    PlaybackAuthorizationStore
) : DataSource {

    override fun addTransferListener(
        transferListener: TransferListener
    ) {
        upstream.addTransferListener(
            transferListener
        )
    }

    override fun open(
        dataSpec: DataSpec
    ): Long {
        /*
         * É importante limpar o header
         * antes de cada open.
         *
         * Assim um DataSource reutilizado
         * nunca carrega acidentalmente o
         * token para outra URL.
         */
        upstream.clearRequestProperty(
            AUTHORIZATION_HEADER
        )

        val authorizationHeader =
            authorizationStore
                .authorizationHeaderFor(
                    dataSpec.uri
                )

        if (
            !authorizationHeader
                .isNullOrBlank()
        ) {
            upstream.setRequestProperty(
                AUTHORIZATION_HEADER,
                authorizationHeader
            )
        }

        return upstream.open(
            dataSpec
        )
    }

    override fun read(
        buffer: ByteArray,
        offset: Int,
        length: Int
    ): Int {
        return upstream.read(
            buffer,
            offset,
            length
        )
    }

    override fun getUri():
            Uri? {
        return upstream.uri
    }

    override fun getResponseHeaders():
            Map<String, List<String>> {
        return upstream
            .responseHeaders
    }

    override fun close() {
        upstream.close()
    }

    private companion object {

        const val AUTHORIZATION_HEADER =
            "Authorization"
    }
}