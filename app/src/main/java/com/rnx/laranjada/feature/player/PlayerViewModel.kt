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
    val isReserving: Boolean = false,
    val reservation: PlaybackReservation? = null,

    val reserveErrorCode: String? = null,
    val reserveErrorMessage: String? = null,

    val isRenewing: Boolean = false,
    val renewErrorCode: String? = null,
    val renewErrorMessage: String? = null,

    val presenceErrorMessage: String? = null,

    val isStopping: Boolean = false,
    val stopErrorMessage: String? = null,

    val fatalAuthorizationError: Boolean = false,
    val fatalPlaybackMessage: String? = null
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

    var episodesUiState by
    mutableStateOf<PlayerEpisodesUiState?>(
        null
    )
        private set

    var isLoadingEpisodes by
    mutableStateOf(
        false
    )
        private set

    var episodesErrorMessage by
    mutableStateOf<String?>(
        null
    )
        private set

    var playbackUiState by
    mutableStateOf(
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

    private var presenceJob:
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

    private var currentPresenceStatus =
        PRESENCE_BUFFERING

    /*
     * Quando uma sessão já recebeu Stop,
     * Ended, Error ou foi encerrada pelo
     * servidor, guardamos o UUID para não
     * tentar reativá-la.
     */
    private var terminalSessionUuid:
            String? =
        null

    /*
     * Background normal não deve manter
     * WatchingSession, Presence ou Renew vivos.
     *
     * Mantemos a reservation local apenas para
     * conseguir reautorizar o mesmo conteúdo
     * quando a Activity voltar ao foreground.
     */
    private var backgroundSuspendedSessionUuid:
            String? =
        null

    /*
     * Se o usuário voltar ao app antes do Stop
     * de background terminar, a nova Reserve
     * fica enfileirada até o Stop concluir.
     */
    private var resumeAfterBackgroundRequested =
        false

    /*
     * Representa a intenção atual do lifecycle.
     *
     * true:
     * o app está em background normal e qualquer
     * sessão que terminar de ser reservada nesse
     * intervalo deve ser liberada novamente.
     *
     * PiP reproduzindo não ativa esta flag.
     */
    private var backgroundSuspensionActive =
        false

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
            normalizedContentUuid &&
            terminalSessionUuid !=
            currentReservation.sessionUuid
        ) {
            return
        }

        lastRequestedContentType =
            normalizedContentType

        lastRequestedContentUuid =
            normalizedContentUuid

        val previousReservation =
            currentReservation

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
                        false,
                    fatalPlaybackMessage =
                        null
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

                val previousReservationWasTerminal =
                    previousReservation !=
                            null &&
                            terminalSessionUuid ==
                            previousReservation
                                .sessionUuid

                val previousReservationWasBackgroundSuspended =
                    previousReservation !=
                            null &&
                            backgroundSuspendedSessionUuid ==
                            previousReservation
                                .sessionUuid

                /*
                 * A Reserve pode ter começado no
                 * foreground e terminar depois do
                 * ON_STOP.
                 *
                 * Nesse caso NÃO ativamos
                 * Presence/Renew nem entregamos
                 * Bearer ao Media3. Registramos a
                 * nova sessão apenas para poder
                 * encerrá-la e reautorizar depois.
                 */
                if (
                    backgroundSuspensionActive
                ) {
                    renewJob?.cancel()
                    presenceJob?.cancel()

                    renewJob =
                        null

                    presenceJob =
                        null

                    authorizationStore.clear()

                    renewRetryAttempt =
                        0

                    renewDeadlineElapsedMs =
                        null

                    expiresDeadlineElapsedMs =
                        null

                    terminalSessionUuid =
                        reservation.sessionUuid

                    backgroundSuspendedSessionUuid =
                        reservation.sessionUuid

                    currentPresenceStatus =
                        PRESENCE_PAUSED

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
                            presenceErrorMessage =
                                null,
                            isStopping =
                                true,
                            stopErrorMessage =
                                null,
                            fatalAuthorizationError =
                                false,
                            fatalPlaybackMessage =
                                null
                        )

                    Log.d(
                        TAG,
                        "Reserve concluída já em background. " +
                                "Sessão será liberada. " +
                                "session=${reservation.sessionUuid}"
                    )

                    viewModelScope.launch {
                        stopReservationQuietly(
                            reservation =
                                reservation,
                            status =
                                STOP_STOPPED
                        )

                        val currentSessionUuid =
                            playbackUiState
                                .reservation
                                ?.sessionUuid

                        if (
                            currentSessionUuid ==
                            reservation.sessionUuid
                        ) {
                            playbackUiState =
                                playbackUiState.copy(
                                    isStopping =
                                        false
                                )
                        }

                        if (
                            !backgroundSuspensionActive &&
                            resumeAfterBackgroundRequested
                        ) {
                            resumePlaybackAfterBackground()
                        }
                    }

                    return@launch
                }

                /*
                 * Daqui em diante a nova
                 * reprodução passa a ser a
                 * reprodução oficial do player.
                 */
                renewJob?.cancel()
                presenceJob?.cancel()

                renewJob =
                    null

                presenceJob =
                    null

                authorizationStore.update(
                    reservation.playback
                )

                renewRetryAttempt =
                    0

                terminalSessionUuid =
                    null

                backgroundSuspendedSessionUuid =
                    null

                resumeAfterBackgroundRequested =
                    false

                currentPresenceStatus =
                    if (
                        previousReservationWasBackgroundSuspended
                    ) {
                        PRESENCE_PAUSED
                    } else {
                        PRESENCE_BUFFERING
                    }

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
                        presenceErrorMessage =
                            null,
                        isStopping =
                            false,
                        stopErrorMessage =
                            null,
                        fatalAuthorizationError =
                            false,
                        fatalPlaybackMessage =
                            null
                    )

                Log.d(
                    TAG,
                    "Playback reservado. " +
                            "session=${reservation.sessionUuid} " +
                            "content=${reservation.contentType}:${reservation.contentUuid}"
                )

                if (
                    previousReservationWasBackgroundSuspended
                ) {
                    Log.d(
                        TAG,
                        "Playback reautorizado após background. " +
                                "session=${reservation.sessionUuid} " +
                                "status=paused"
                    )
                }

                scheduleRenew(
                    reservation.sessionUuid
                )

                schedulePresence(
                    reservation
                )

                /*
                 * Se isso foi uma troca de
                 * episódio/conteúdo, encerramos
                 * a sessão anterior.
                 *
                 * A chamada é independente da
                 * nova autorização.
                 */
                if (
                    previousReservation !=
                    null &&
                    previousReservation
                        .sessionUuid !=
                    reservation.sessionUuid &&
                    !previousReservationWasTerminal
                ) {
                    viewModelScope.launch {
                        stopReservationQuietly(
                            reservation =
                                previousReservation,
                            status =
                                STOP_STOPPED
                        )
                    }
                }
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

    fun updatePresenceStatus(
        status: String
    ) {
        val normalized =
            status
                .trim()
                .lowercase(
                    Locale.US
                )

        if (
            normalized !in
            ACTIVE_PRESENCE_STATUSES
        ) {
            return
        }

        val reservation =
            playbackUiState
                .reservation
                ?: return

        if (
            terminalSessionUuid ==
            reservation.sessionUuid
        ) {
            return
        }

        currentPresenceStatus =
            normalized
    }

    fun suspendPlaybackForBackground() {
        backgroundSuspensionActive =
            true

        /*
         * Se existia uma retomada enfileirada
         * por um retorno muito rápido ao app,
         * o novo ON_STOP cancela essa intenção.
         */
        resumeAfterBackgroundRequested =
            false

        val reservation =
            playbackUiState
                .reservation

        if (
            reservation ==
            null
        ) {
            Log.d(
                TAG,
                "Background sem reservation ativa. " +
                        "Qualquer Reserve pendente será liberada ao concluir."
            )

            return
        }

        val sessionUuid =
            reservation.sessionUuid

        backgroundSuspendedSessionUuid =
            sessionUuid

        if (
            terminalSessionUuid ==
            sessionUuid
        ) {
            Log.d(
                TAG,
                "Background já possui sessão suspensa. " +
                        "session=$sessionUuid"
            )

            return
        }

        Log.d(
            TAG,
            "Suspendendo playback por background. " +
                    "session=$sessionUuid"
        )

        stopPlayback(
            status =
                STOP_STOPPED,
            clearLocalAfter =
                false
        )
    }

    fun resumePlaybackAfterBackground() {
        backgroundSuspensionActive =
            false

        val reservation =
            playbackUiState
                .reservation

        /*
         * Pode acontecer de o app ter ido ao
         * background enquanto a primeira Reserve
         * ainda estava em andamento.
         */
        if (
            reservation ==
            null
        ) {
            if (
                playbackUiState.isReserving
            ) {
                return
            }

            if (
                lastRequestedContentType
                    .isBlank() ||
                lastRequestedContentUuid
                    .isBlank()
            ) {
                return
            }

            Log.d(
                TAG,
                "Retomando playback após background " +
                        "sem reservation local."
            )

            startPlayback(
                contentType =
                    lastRequestedContentType,
                contentUuid =
                    lastRequestedContentUuid,
                force =
                    true
            )

            return
        }

        val suspendedSessionUuid =
            backgroundSuspendedSessionUuid
                ?: return

        if (
            reservation.sessionUuid !=
            suspendedSessionUuid
        ) {
            return
        }

        resumeAfterBackgroundRequested =
            true

        if (
            playbackUiState.isStopping
        ) {
            Log.d(
                TAG,
                "Retomada de background aguardando Stop. " +
                        "session=$suspendedSessionUuid"
            )

            return
        }

        if (
            playbackUiState.isReserving
        ) {
            Log.d(
                TAG,
                "Retomada de background aguardando Reserve em andamento."
            )

            return
        }

        resumeAfterBackgroundRequested =
            false

        Log.d(
            TAG,
            "Reautorizando playback após background. " +
                    "content=${reservation.contentType}:" +
                    "${reservation.contentUuid}"
        )

        startPlayback(
            contentType =
                reservation.contentType,
            contentUuid =
                reservation.contentUuid,
            force =
                true
        )
    }

    fun stopPlayback(
        status: String = STOP_STOPPED,
        clearLocalAfter: Boolean = true,
        onComplete: () -> Unit = {}
    ) {
        val normalizedStatus =
            status
                .trim()
                .lowercase(
                    Locale.US
                )
                .takeIf {
                    it in
                            TERMINAL_STOP_STATUSES
                }
                ?: STOP_STOPPED

        val reservation =
            playbackUiState
                .reservation

        if (
            reservation == null
        ) {
            if (
                clearLocalAfter
            ) {
                clearLocalPlaybackInternal()
            }

            onComplete()

            return
        }

        val sessionUuid =
            reservation.sessionUuid

        /*
         * Para imediatamente Presence,
         * Renew e novos requests HLS.
         */
        presenceJob?.cancel()
        renewJob?.cancel()

        presenceJob =
            null

        renewJob =
            null

        authorizationStore.clear()

        val wasAlreadyTerminal =
            terminalSessionUuid ==
                    sessionUuid

        terminalSessionUuid =
            sessionUuid

        if (
            wasAlreadyTerminal
        ) {
            if (
                clearLocalAfter
            ) {
                clearLocalPlaybackInternal()
            }

            onComplete()

            return
        }

        playbackUiState =
            playbackUiState.copy(
                isStopping =
                    true,
                stopErrorMessage =
                    null
            )

        viewModelScope.launch {
            try {
                val result =
                    playbackRepository.stop(
                        contentType =
                            reservation.contentType,
                        contentUuid =
                            reservation.contentUuid,
                        clientSessionKey =
                            reservation.clientSessionKey,
                        status =
                            normalizedStatus
                    )

                Log.d(
                    TAG,
                    "Playback encerrado. " +
                            "session=${reservation.sessionUuid} " +
                            "status=${result.status} " +
                            "already_stopped=${result.alreadyStopped}"
                )
            } catch (
                exception: Exception
            ) {
                Log.w(
                    TAG,
                    "Falha ao enviar Stop para " +
                            "session=${reservation.sessionUuid}: " +
                            "${exception.message}"
                )

                /*
                 * Mesmo que o Stop REST falhe,
                 * o cliente interrompe localmente.
                 *
                 * O backend possui expiração
                 * natural por ausência de Presence.
                 */
                if (
                    playbackUiState
                        .reservation
                        ?.sessionUuid ==
                    sessionUuid
                ) {
                    playbackUiState =
                        playbackUiState.copy(
                            stopErrorMessage =
                                exception.message
                                    ?: "Não foi possível confirmar o encerramento da reprodução."
                        )
                }
            } finally {
                val currentSessionUuid =
                    playbackUiState
                        .reservation
                        ?.sessionUuid

                val shouldResumeAfterBackground =
                    !clearLocalAfter &&
                            normalizedStatus ==
                            STOP_STOPPED &&
                            currentSessionUuid ==
                            sessionUuid &&
                            backgroundSuspendedSessionUuid ==
                            sessionUuid &&
                            resumeAfterBackgroundRequested &&
                            !backgroundSuspensionActive

                if (
                    currentSessionUuid ==
                    sessionUuid
                ) {
                    if (
                        clearLocalAfter
                    ) {
                        clearLocalPlaybackInternal()
                    } else {
                        playbackUiState =
                            playbackUiState.copy(
                                isStopping =
                                    false
                            )
                    }
                }

                onComplete()

                if (
                    shouldResumeAfterBackground
                ) {
                    Log.d(
                        TAG,
                        "Stop de background concluído. " +
                                "Executando Reserve enfileirada."
                    )

                    resumePlaybackAfterBackground()
                }
            }
        }
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
        clearLocalPlaybackInternal()
    }

    private fun schedulePresence(
        reservation:
        PlaybackReservation
    ) {
        presenceJob?.cancel()

        val intervalMs =
            reservation
                .presencePulseIntervalMs

        if (
            intervalMs <=
            0L
        ) {
            handleFatalPresenceFailure(
                expectedSessionUuid =
                    reservation.sessionUuid,
                message =
                    "O servidor não informou um intervalo válido para manter a reprodução ativa."
            )

            return
        }

        Log.d(
            TAG,
            "Presence iniciado. " +
                    "session=${reservation.sessionUuid} " +
                    "interval=${intervalMs}ms"
        )

        presenceJob =
            viewModelScope.launch {
                while (
                    true
                ) {
                    delay(
                        intervalMs
                    )

                    val currentReservation =
                        playbackUiState
                            .reservation

                    if (
                        currentReservation ==
                        null ||
                        currentReservation
                            .sessionUuid !=
                        reservation.sessionUuid ||
                        terminalSessionUuid ==
                        reservation.sessionUuid
                    ) {
                        return@launch
                    }

                    sendPresencePulse(
                        expectedSessionUuid =
                            reservation.sessionUuid
                    )
                }
            }
    }

    private suspend fun sendPresencePulse(
        expectedSessionUuid: String
    ) {
        val reservation =
            playbackUiState
                .reservation
                ?: return

        if (
            reservation.sessionUuid !=
            expectedSessionUuid ||
            terminalSessionUuid ==
            expectedSessionUuid
        ) {
            return
        }

        try {
            val presence =
                playbackRepository.presence(
                    contentType =
                        reservation.contentType,
                    contentUuid =
                        reservation.contentUuid,
                    clientSessionKey =
                        reservation.clientSessionKey,
                    status =
                        currentPresenceStatus
                )

            val latestReservation =
                playbackUiState
                    .reservation

            if (
                latestReservation ==
                null ||
                latestReservation
                    .sessionUuid !=
                expectedSessionUuid ||
                terminalSessionUuid ==
                expectedSessionUuid
            ) {
                return
            }

            if (
                !presence.active ||
                !presence.allowed
            ) {
                handleFatalPresenceFailure(
                    expectedSessionUuid =
                        expectedSessionUuid,
                    message =
                        presence.message
                            .ifBlank {
                                "A sessão de reprodução não está mais ativa."
                            }
                )

                return
            }

            val stopCommand =
                presence.commands
                    .firstOrNull {
                        it.type ==
                                "stop"
                    }

            if (
                stopCommand != null
            ) {
                handleFatalPresenceFailure(
                    expectedSessionUuid =
                        expectedSessionUuid,
                    message =
                        stopCommand.message
                            .ifBlank {
                                "Esta reprodução foi interrompida."
                            }
                )

                return
            }

            playbackUiState =
                playbackUiState.copy(
                    presenceErrorMessage =
                        null
                )

            Log.d(
                TAG,
                "Presence OK. " +
                        "session=$expectedSessionUuid " +
                        "status=${presence.status} " +
                        "code=${presence.code}"
            )
        } catch (
            exception:
            PlaybackApiException
        ) {
            if (
                shouldRetryPresence(
                    exception.statusCode
                )
            ) {
                playbackUiState =
                    playbackUiState.copy(
                        presenceErrorMessage =
                            exception.message
                    )

                Log.w(
                    TAG,
                    "Presence temporariamente falhou. " +
                            "session=$expectedSessionUuid " +
                            "code=${exception.code}"
                )
            } else {
                handleFatalPresenceFailure(
                    expectedSessionUuid =
                        expectedSessionUuid,
                    message =
                        exception.message
                            ?: "A sessão de reprodução não está mais válida."
                )
            }
        } catch (
            exception: IOException
        ) {
            playbackUiState =
                playbackUiState.copy(
                    presenceErrorMessage =
                        exception.message
                )

            Log.w(
                TAG,
                "Presence falhou por rede. " +
                        "session=$expectedSessionUuid"
            )
        } catch (
            exception: Exception
        ) {
            handleFatalPresenceFailure(
                expectedSessionUuid =
                    expectedSessionUuid,
                message =
                    exception.message
                        ?: "Não foi possível manter a sessão de reprodução ativa."
            )
        }
    }

    private fun handleFatalPresenceFailure(
        expectedSessionUuid: String,
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

        terminalSessionUuid =
            expectedSessionUuid

        presenceJob?.cancel()
        renewJob?.cancel()

        presenceJob =
            null

        renewJob =
            null

        authorizationStore.clear()

        playbackUiState =
            playbackUiState.copy(
                isRenewing =
                    false,
                presenceErrorMessage =
                    message,
                fatalAuthorizationError =
                    true,
                fatalPlaybackMessage =
                    message
            )

        Log.e(
            TAG,
            "Presence encerrada definitivamente. " +
                    "session=$expectedSessionUuid"
        )
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
            expectedSessionUuid ||
            terminalSessionUuid ==
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

            if (
                renewal.sessionUuid !=
                expectedSessionUuid
            ) {
                throw IllegalStateException(
                    "A API retornou uma sessão diferente durante o Renew."
                )
            }

            val latestReservation =
                playbackUiState
                    .reservation

            if (
                latestReservation ==
                null ||
                latestReservation.sessionUuid !=
                expectedSessionUuid ||
                terminalSessionUuid ==
                expectedSessionUuid
            ) {
                return
            }

            /*
             * Troca atômica do Bearer.
             *
             * A instância do ExoPlayer
             * permanece a mesma.
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
                        false,
                    fatalPlaybackMessage =
                        null
                )

            Log.d(
                TAG,
                "Playback Authorization renovada. " +
                        "session=$expectedSessionUuid " +
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
            expectedSessionUuid ||
            terminalSessionUuid ==
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
            "Renew temporariamente falhou. " +
                    "Nova tentativa em ${safeRetryDelay}ms."
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

        terminalSessionUuid =
            expectedSessionUuid

        renewJob?.cancel()
        presenceJob?.cancel()

        renewJob =
            null

        presenceJob =
            null

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
                    true,
                fatalPlaybackMessage =
                    message
            )

        Log.e(
            TAG,
            "Renew negado definitivamente. " +
                    "session=$expectedSessionUuid " +
                    "code=$code"
        )
    }

    private suspend fun stopReservationQuietly(
        reservation: PlaybackReservation,
        status: String
    ) {
        try {
            val result =
                playbackRepository.stop(
                    contentType =
                        reservation.contentType,
                    contentUuid =
                        reservation.contentUuid,
                    clientSessionKey =
                        reservation.clientSessionKey,
                    status =
                        status
                )

            Log.d(
                TAG,
                "Sessão anterior encerrada. " +
                        "session=${reservation.sessionUuid} " +
                        "status=${result.status}"
            )
        } catch (
            exception: Exception
        ) {
            Log.w(
                TAG,
                "Não foi possível encerrar a sessão anterior " +
                        "${reservation.sessionUuid}: ${exception.message}"
            )
        }
    }

    private fun configureDeadlines(
        playback:
        PlaybackAuthorization
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

    private fun shouldRetryPresence(
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
        return "android-player:${UUID.randomUUID()}"
    }

    private fun clearLocalPlaybackInternal() {
        renewJob?.cancel()
        presenceJob?.cancel()

        renewJob =
            null

        presenceJob =
            null

        renewRetryAttempt =
            0

        renewDeadlineElapsedMs =
            null

        expiresDeadlineElapsedMs =
            null

        terminalSessionUuid =
            null

        backgroundSuspendedSessionUuid =
            null

        resumeAfterBackgroundRequested =
            false

        backgroundSuspensionActive =
            false

        currentPresenceStatus =
            PRESENCE_BUFFERING

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

    override fun onCleared() {
        renewJob?.cancel()
        presenceJob?.cancel()

        authorizationStore.clear()

        super.onCleared()
    }

    private companion object {

        const val TAG =
            "LaranjadaPlayback"

        const val PRESENCE_PLAYING =
            "playing"

        const val PRESENCE_PAUSED =
            "paused"

        const val PRESENCE_BUFFERING =
            "buffering"

        const val PRESENCE_SEEKING =
            "seeking"

        const val STOP_STOPPED =
            "stopped"

        const val STOP_ENDED =
            "ended"

        const val STOP_ERROR =
            "error"

        val ACTIVE_PRESENCE_STATUSES =
            setOf(
                PRESENCE_PLAYING,
                PRESENCE_PAUSED,
                PRESENCE_BUFFERING,
                PRESENCE_SEEKING
            )

        val TERMINAL_STOP_STATUSES =
            setOf(
                STOP_STOPPED,
                STOP_ENDED,
                STOP_ERROR
            )

        val FRACTIONAL_SECONDS_REGEX =
            Regex(
                """\.\d+(?=Z$|[+-]\d{2}:\d{2}$)"""
            )
    }
}