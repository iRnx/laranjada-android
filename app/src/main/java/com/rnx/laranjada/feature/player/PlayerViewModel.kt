package com.rnx.laranjada.feature.player

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.core.playback.PlaybackAuthorizationStore
import com.rnx.laranjada.data.remote.api.PlaybackApiException
import com.rnx.laranjada.data.repository.LaranjadaRepositoryImpl
import com.rnx.laranjada.data.repository.PlaybackRepositoryImpl
import com.rnx.laranjada.domain.model.PlaybackAuthorization
import com.rnx.laranjada.domain.model.PlaybackReservation
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import com.rnx.laranjada.domain.repository.PlaybackRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class PlaybackUiState(
    val isReserving: Boolean =
        false,

    val reservation:
    PlaybackReservation? =
        null,

    val reserveErrorCode: String? =
        null,

    val reserveErrorMessage: String? =
        null,

    val isRenewing: Boolean =
        false,

    val renewErrorCode: String? =
        null,

    val renewErrorMessage: String? =
        null,

    val fatalAuthorizationError: Boolean =
        false
)

class PlayerViewModel(
    private val repository:
    LaranjadaRepository =
        LaranjadaRepositoryImpl(),

    private val playbackRepository:
    PlaybackRepository =
        PlaybackRepositoryImpl(),

    val authorizationStore:
    PlaybackAuthorizationStore =
        PlaybackAuthorizationStore()
) : ViewModel() {

    var episodesUiState by mutableStateOf<
            PlayerEpisodesUiState?
            >(
        null
    )
        private set

    var isLoadingEpisodes by mutableStateOf(
        false
    )
        private set

    var episodesErrorMessage by mutableStateOf<
            String?
            >(
        null
    )
        private set

    var playbackUiState by mutableStateOf(
        PlaybackUiState()
    )
        private set

    private var loadedSeriesUuid:
            String? =
        null

    private var initialPlaybackStarted =
        false

    private var lastRequestedContentType =
        ""

    private var lastRequestedContentUuid =
        ""

    private var renewJob:
            Job? =
        null

    private var renewRetryAttempt =
        0

    private var renewDeadlineElapsedMs:
            Long? =
        null

    private var expiresDeadlineElapsedMs:
            Long? =
        null

    fun ensureInitialPlayback(
        contentType: String,
        contentUuid: String
    ) {
        if (
            initialPlaybackStarted
        ) {
            return
        }

        initialPlaybackStarted =
            true

        startPlayback(
            contentType =
                contentType,

            contentUuid =
                contentUuid,

            force =
                true
        )
    }

    fun startPlayback(
        contentType: String,
        contentUuid: String,
        force: Boolean = false
    ) {
        val normalizedContentType =
            contentType
                .trim()
                .lowercase(
                    Locale.US
                )

        val normalizedContentUuid =
            contentUuid.trim()

        if (
            normalizedContentType
                .isBlank() ||
            normalizedContentUuid
                .isBlank()
        ) {
            playbackUiState =
                playbackUiState.copy(
                    reserveErrorCode =
                        "invalid_request",

                    reserveErrorMessage =
                        "Conteúdo de reprodução inválido."
                )

            return
        }

        if (
            playbackUiState.isReserving
        ) {
            return
        }

        val currentReservation =
            playbackUiState.reservation

        if (
            !force &&
            currentReservation != null &&
            currentReservation.contentType ==
            normalizedContentType &&
            currentReservation.contentUuid ==
            normalizedContentUuid
        ) {
            return
        }

        lastRequestedContentType =
            normalizedContentType

        lastRequestedContentUuid =
            normalizedContentUuid

        viewModelScope.launch {
            playbackUiState =
                playbackUiState.copy(
                    isReserving =
                        true,

                    reserveErrorCode =
                        null,

                    reserveErrorMessage =
                        null,

                    fatalAuthorizationError =
                        false
                )

            try {
                val clientSessionKey =
                    createClientSessionKey()

                val reservation =
                    playbackRepository.reserve(
                        contentType =
                            normalizedContentType,

                        contentUuid =
                            normalizedContentUuid,

                        clientSessionKey =
                            clientSessionKey
                    )

                /*
                 * Primeiro atualizamos o estado
                 * thread-safe que o Media3 consulta.
                 */
                authorizationStore.update(
                    reservation.playback
                )

                /*
                 * Qualquer timer da reprodução
                 * anterior deixa de ser relevante.
                 */
                renewJob?.cancel()

                renewRetryAttempt =
                    0

                configureDeadlines(
                    reservation.playback
                )

                playbackUiState =
                    playbackUiState.copy(
                        isReserving =
                            false,

                        reservation =
                            reservation,

                        reserveErrorCode =
                            null,

                        reserveErrorMessage =
                            null,

                        isRenewing =
                            false,

                        renewErrorCode =
                            null,

                        renewErrorMessage =
                            null,

                        fatalAuthorizationError =
                            false
                    )

                Log.d(
                    TAG,
                    "Playback reservado. session=${reservation.sessionUuid} " +
                            "content=${reservation.contentType}:${reservation.contentUuid}"
                )

                scheduleRenew(
                    reservation.sessionUuid
                )
            } catch (
                exception:
                PlaybackApiException
            ) {
                playbackUiState =
                    playbackUiState.copy(
                        isReserving =
                            false,

                        reserveErrorCode =
                            exception.code,

                        reserveErrorMessage =
                            exception.message
                                ?: "Não foi possível autorizar a reprodução."
                    )
            } catch (
                exception: Exception
            ) {
                playbackUiState =
                    playbackUiState.copy(
                        isReserving =
                            false,

                        reserveErrorCode =
                            null,

                        reserveErrorMessage =
                            exception.message
                                ?: "Não foi possível autorizar a reprodução."
                    )
            }
        }
    }

    fun retryLastPlayback() {
        if (
            lastRequestedContentType
                .isBlank() ||
            lastRequestedContentUuid
                .isBlank()
        ) {
            return
        }

        startPlayback(
            contentType =
                lastRequestedContentType,

            contentUuid =
                lastRequestedContentUuid,

            force =
                true
        )
    }

    fun loadSeriesEpisodes(
        seriesUuid: String,
        forceRefresh: Boolean = false
    ) {
        if (
            seriesUuid.isBlank()
        ) {
            episodesUiState =
                null

            episodesErrorMessage =
                null

            loadedSeriesUuid =
                null

            return
        }

        if (
            !forceRefresh &&
            loadedSeriesUuid ==
            seriesUuid &&
            episodesUiState !=
            null
        ) {
            return
        }

        loadedSeriesUuid =
            seriesUuid

        viewModelScope.launch {
            isLoadingEpisodes =
                true

            episodesErrorMessage =
                null

            try {
                val detail =
                    repository.getDetail(
                        contentType =
                            "series",

                        uuid =
                            seriesUuid
                    )

                episodesUiState =
                    detail
                        .toPlayerEpisodesUiState()
            } catch (
                exception: Exception
            ) {
                episodesErrorMessage =
                    exception.message
            } finally {
                isLoadingEpisodes =
                    false
            }
        }
    }

    fun clearLocalPlayback() {
        renewJob?.cancel()

        renewJob =
            null

        renewRetryAttempt =
            0

        renewDeadlineElapsedMs =
            null

        expiresDeadlineElapsedMs =
            null

        authorizationStore.clear()

        playbackUiState =
            PlaybackUiState()

        initialPlaybackStarted =
            false

        lastRequestedContentType =
            ""

        lastRequestedContentUuid =
            ""
    }

    private fun scheduleRenew(
        sessionUuid: String
    ) {
        renewJob?.cancel()

        val deadline =
            renewDeadlineElapsedMs
                ?: return

        val delayMs =
            (
                    deadline -
                            SystemClock
                                .elapsedRealtime()
                    )
                .coerceAtLeast(
                    0L
                )

        renewJob =
            viewModelScope.launch {
                if (
                    delayMs > 0L
                ) {
                    delay(
                        delayMs
                    )
                }

                renewJob =
                    null

                performRenew(
                    expectedSessionUuid =
                        sessionUuid
                )
            }
    }

    private suspend fun performRenew(
        expectedSessionUuid: String
    ) {
        val currentReservation =
            playbackUiState
                .reservation

        if (
            currentReservation ==
            null ||
            currentReservation.sessionUuid !=
            expectedSessionUuid
        ) {
            return
        }

        if (
            playbackUiState.isRenewing
        ) {
            return
        }

        playbackUiState =
            playbackUiState.copy(
                isRenewing =
                    true,

                renewErrorCode =
                    null,

                renewErrorMessage =
                    null
            )

        try {
            val renewal =
                playbackRepository.renew(
                    sessionUuid =
                        expectedSessionUuid
                )

            /*
             * O contrato exige a mesma
             * WatchingSession.
             */
            if (
                renewal.sessionUuid !=
                expectedSessionUuid
            ) {
                throw IllegalStateException(
                    "A API retornou uma sessão diferente durante o Renew."
                )
            }

            /*
             * Pode ter ocorrido uma troca de
             * episódio enquanto a requisição
             * estava em andamento.
             *
             * Nesse caso descartamos o Renew
             * antigo.
             */
            val latestReservation =
                playbackUiState
                    .reservation

            if (
                latestReservation ==
                null ||
                latestReservation.sessionUuid !=
                expectedSessionUuid
            ) {
                return
            }

            /*
             * Troca atômica do Bearer.
             *
             * O ExoPlayer não é recriado.
             */
            authorizationStore.update(
                renewal.playback
            )

            configureDeadlines(
                renewal.playback
            )

            renewRetryAttempt =
                0

            val updatedReservation =
                latestReservation.copy(
                    limit =
                        renewal.limit,

                    activeDeviceCount =
                        renewal.activeDeviceCount,

                    playback =
                        renewal.playback
                )

            playbackUiState =
                playbackUiState.copy(
                    isRenewing =
                        false,

                    reservation =
                        updatedReservation,

                    renewErrorCode =
                        null,

                    renewErrorMessage =
                        null,

                    fatalAuthorizationError =
                        false
                )

            Log.d(
                TAG,
                "Playback Authorization renovada. session=$expectedSessionUuid " +
                        "renew_at=${renewal.playback.renewAt}"
            )

            scheduleRenew(
                expectedSessionUuid
            )
        } catch (
            exception:
            PlaybackApiException
        ) {
            if (
                shouldRetryRenew(
                    exception.statusCode
                )
            ) {
                handleTransientRenewFailure(
                    expectedSessionUuid =
                        expectedSessionUuid,

                    code =
                        exception.code,

                    message =
                        exception.message
                            ?: "Falha temporária ao renovar a reprodução."
                )
            } else {
                handleFatalRenewFailure(
                    expectedSessionUuid =
                        expectedSessionUuid,

                    code =
                        exception.code,

                    message =
                        exception.message
                            ?: "A reprodução não está mais autorizada."
                )
            }
        } catch (
            exception: IOException
        ) {
            handleTransientRenewFailure(
                expectedSessionUuid =
                    expectedSessionUuid,

                code =
                    null,

                message =
                    exception.message
                        ?: "Falha de rede ao renovar a reprodução."
            )
        } catch (
            exception: Exception
        ) {
            handleFatalRenewFailure(
                expectedSessionUuid =
                    expectedSessionUuid,

                code =
                    null,

                message =
                    exception.message
                        ?: "Não foi possível renovar a reprodução."
            )
        }
    }

    private fun handleTransientRenewFailure(
        expectedSessionUuid: String,
        code: String?,
        message: String
    ) {
        val currentSessionUuid =
            playbackUiState
                .reservation
                ?.sessionUuid

        if (
            currentSessionUuid !=
            expectedSessionUuid
        ) {
            return
        }

        playbackUiState =
            playbackUiState.copy(
                isRenewing =
                    false,

                renewErrorCode =
                    code,

                renewErrorMessage =
                    message,

                fatalAuthorizationError =
                    false
            )

        val expirationDeadline =
            expiresDeadlineElapsedMs

        if (
            expirationDeadline ==
            null
        ) {
            handleFatalRenewFailure(
                expectedSessionUuid =
                    expectedSessionUuid,

                code =
                    code,

                message =
                    message
            )

            return
        }

        val remainingMs =
            expirationDeadline -
                    SystemClock
                        .elapsedRealtime()

        if (
            remainingMs <=
            2_000L
        ) {
            handleFatalRenewFailure(
                expectedSessionUuid =
                    expectedSessionUuid,

                code =
                    code,

                message =
                    "A autorização da reprodução expirou."
            )

            return
        }

        val retryDelays =
            longArrayOf(
                5_000L,
                10_000L,
                20_000L,
                30_000L
            )

        val retryDelay =
            retryDelays[
                renewRetryAttempt
                    .coerceAtMost(
                        retryDelays.lastIndex
                    )
            ]

        renewRetryAttempt +=
            1

        val safeRetryDelay =
            minOf(
                retryDelay,
                (
                        remainingMs -
                                1_000L
                        )
                    .coerceAtLeast(
                        1_000L
                    )
            )

        Log.w(
            TAG,
            "Renew temporariamente falhou. Nova tentativa em ${safeRetryDelay}ms."
        )

        renewJob?.cancel()

        renewJob =
            viewModelScope.launch {
                delay(
                    safeRetryDelay
                )

                renewJob =
                    null

                performRenew(
                    expectedSessionUuid =
                        expectedSessionUuid
                )
            }
    }

    private fun handleFatalRenewFailure(
        expectedSessionUuid: String,
        code: String?,
        message: String
    ) {
        val currentSessionUuid =
            playbackUiState
                .reservation
                ?.sessionUuid

        if (
            currentSessionUuid !=
            expectedSessionUuid
        ) {
            return
        }

        renewJob?.cancel()

        renewJob =
            null

        /*
         * Negação definitiva:
         * o Media3 deixa de receber o Bearer.
         */
        authorizationStore.clear()

        playbackUiState =
            playbackUiState.copy(
                isRenewing =
                    false,

                renewErrorCode =
                    code,

                renewErrorMessage =
                    message,

                fatalAuthorizationError =
                    true
            )

        Log.e(
            TAG,
            "Renew negado definitivamente. session=$expectedSessionUuid code=$code"
        )
    }

    private fun configureDeadlines(
        playback: PlaybackAuthorization
    ) {
        val nowElapsed =
            SystemClock
                .elapsedRealtime()

        val renewDelayMs =
            serverIntervalMillis(
                start =
                    playback.issuedAt,

                end =
                    playback.renewAt
            )
                ?: (
                        (
                                playback
                                    .expiresInSeconds -
                                        playback
                                            .renewBeforeSeconds
                                )
                            .coerceAtLeast(
                                1L
                            ) *
                                1_000L
                        )

        val expiresDelayMs =
            serverIntervalMillis(
                start =
                    playback.issuedAt,

                end =
                    playback.expiresAt
            )
                ?: (
                        playback
                            .expiresInSeconds
                            .coerceAtLeast(
                                1L
                            ) *
                                1_000L
                        )

        renewDeadlineElapsedMs =
            nowElapsed +
                    renewDelayMs
                        .coerceAtLeast(
                            1_000L
                        )

        expiresDeadlineElapsedMs =
            nowElapsed +
                    expiresDelayMs
                        .coerceAtLeast(
                            1_000L
                        )
    }

    private fun serverIntervalMillis(
        start: String,
        end: String
    ): Long? {
        val startMillis =
            parseIsoTimestamp(
                start
            )
                ?: return null

        val endMillis =
            parseIsoTimestamp(
                end
            )
                ?: return null

        val interval =
            endMillis -
                    startMillis

        return interval
            .takeIf {
                it > 0L
            }
    }

    private fun parseIsoTimestamp(
        value: String
    ): Long? {
        if (
            value.isBlank()
        ) {
            return null
        }

        /*
         * Django pode devolver microssegundos.
         * SimpleDateFormat não precisa deles
         * para calcular nosso intervalo.
         */
        val normalized =
            value
                .trim()
                .replace(
                    FRACTIONAL_SECONDS_REGEX,
                    ""
                )

        val format =
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                Locale.US
            ).apply {
                isLenient =
                    false

                timeZone =
                    TimeZone.getTimeZone(
                        "UTC"
                    )
            }

        return runCatching {
            format
                .parse(
                    normalized
                )
                ?.time
        }.getOrNull()
    }

    private fun shouldRetryRenew(
        statusCode: Int
    ): Boolean {
        return statusCode ==
                408 ||
                statusCode ==
                429 ||
                statusCode in
                500..599
    }

    private fun createClientSessionKey():
            String {
        /*
         * Backend aceita:
         * [A-Za-z0-9._:-]
         * entre 16 e 128 caracteres.
         */
        return "android-player:${UUID.randomUUID()}"
    }

    override fun onCleared() {
        renewJob?.cancel()

        authorizationStore.clear()

        super.onCleared()
    }

    private companion object {

        const val TAG =
            "LaranjadaPlayback"

        val FRACTIONAL_SECONDS_REGEX =
            Regex(
                """\.\d+(?=Z$|[+-]\d{2}:\d{2}$)"""
            )
    }
}