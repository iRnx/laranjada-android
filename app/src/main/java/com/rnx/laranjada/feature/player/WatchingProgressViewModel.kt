package com.rnx.laranjada.feature.player

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.WatchingProgressRepositoryImpl
import com.rnx.laranjada.domain.model.WatchingProgressSaveResult
import com.rnx.laranjada.domain.repository.WatchingProgressRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

data class WatchingProgressUiState(
    val isSaving: Boolean = false,
    val lastResult: WatchingProgressSaveResult? = null,
    val errorMessage: String? = null
)

class WatchingProgressViewModel(
    private val repository:
    WatchingProgressRepository =
        WatchingProgressRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(
        WatchingProgressUiState()
    )
        private set

    /*
     * Um único canal de escrita de progresso.
     *
     * Periodic / Pause / Background /
     * Exit / Ended nunca fazem POST
     * concorrente.
     */
    private val progressSaveMutex =
        Mutex()

    /*
     * Última posição confirmada pelo servidor
     * durante a vida deste Player.
     */
    private val lastPersistedPositionByContent =
        mutableMapOf<String, Long>()

    /*
     * Proteção contra retry agressivo.
     *
     * Se um checkpoint periódico falhar,
     * não queremos uma nova tentativa
     * a cada segundo.
     */
    private val lastPeriodicAttemptElapsedByContent =
        mutableMapOf<String, Long>()

    fun savePeriodicProgress(
        contentType: String,
        contentUuid: String,
        positionMs: Long,
        durationMs: Long,
        hasKnownProgress: Boolean,
        lastPersistedPositionSeconds: Long? = null,
        onResult: (
            WatchingProgressSaveResult
        ) -> Unit = {}
    ) {
        val request =
            buildProgressRequest(
                contentType =
                    contentType,

                contentUuid =
                    contentUuid,

                positionMs =
                    positionMs,

                durationMs =
                    durationMs,

                hasKnownProgress =
                    hasKnownProgress,

                lastPersistedPositionSeconds =
                    lastPersistedPositionSeconds,

                source =
                    ProgressSaveSource.PERIODIC
            )
                ?: return

        /*
         * Periodic nunca precisa competir
         * com um save mais importante.
         *
         * Se Pause / Background / Exit /
         * Ended estiver salvando, simplesmente
         * esperamos a próxima verificação.
         */
        if (
            uiState.isSaving
        ) {
            return
        }

        val nowElapsed =
            SystemClock
                .elapsedRealtime()

        val lastAttemptElapsed =
            lastPeriodicAttemptElapsedByContent[
                request.contentUuid
            ]

        if (
            lastAttemptElapsed !=
            null &&
            (
                    nowElapsed -
                            lastAttemptElapsed
                    ) <
            PERIODIC_RETRY_MIN_INTERVAL_MS
        ) {
            return
        }

        viewModelScope.launch {
            progressSaveMutex
                .withLock {

                    val latestPersistedPosition =
                        resolveLastPersistedPosition(
                            contentUuid =
                                request.contentUuid,

                            fallbackPosition =
                                request
                                    .lastPersistedPositionSeconds
                        )

                    val hasPersistedProgressNow =
                        request.hasKnownProgress ||
                                lastPersistedPositionByContent
                                    .containsKey(
                                        request.contentUuid
                                    )

                    val shouldSave =
                        if (
                            hasPersistedProgressNow
                        ) {
                            if (
                                latestPersistedPosition ==
                                null
                            ) {
                                request.positionSeconds >=
                                        FIRST_SAVE_AT_SECONDS
                            } else {
                                (
                                        request.positionSeconds -
                                                latestPersistedPosition
                                        ) >=
                                        PERIODIC_SAVE_INTERVAL_SECONDS
                            }
                        } else {
                            request.positionSeconds >=
                                    FIRST_SAVE_AT_SECONDS
                        }

                    if (
                        !shouldSave
                    ) {
                        return@withLock
                    }

                    lastPeriodicAttemptElapsedByContent[
                        request.contentUuid
                    ] =
                        SystemClock
                            .elapsedRealtime()

                    uiState =
                        uiState.copy(
                            isSaving =
                                true,

                            errorMessage =
                                null
                        )

                    try {
                        val result =
                            repository
                                .saveProgress(
                                    contentType =
                                        request.contentType,

                                    contentUuid =
                                        request.contentUuid,

                                    positionSeconds =
                                        request.positionSeconds,

                                    durationSeconds =
                                        request.durationSeconds,

                                    status =
                                        STATUS_PLAYING,

                                    forceProgressSave =
                                        false
                                )

                        registerPersistedResult(
                            result
                        )

                        uiState =
                            uiState.copy(
                                isSaving =
                                    false,

                                lastResult =
                                    result,

                                errorMessage =
                                    null
                            )

                        Log.d(
                            TAG,
                            "Progress Periodic processado. " +
                                    "content=" +
                                    "${result.contentType}:" +
                                    "${result.contentUuid} " +
                                    "requested=" +
                                    "${result.requestedPositionSeconds}s " +
                                    "persisted=" +
                                    "${result.positionSeconds}s " +
                                    "saved=${result.saved} " +
                                    "reason=${result.reason} " +
                                    "status=${result.status} " +
                                    "completed=" +
                                    "${result.isCompleted}"
                        )

                        onResult(
                            result
                        )

                    } catch (
                        exception: Exception
                    ) {
                        uiState =
                            uiState.copy(
                                isSaving =
                                    false,

                                errorMessage =
                                    exception.message
                                        ?: "Não foi possível salvar o progresso."
                            )

                        Log.w(
                            TAG,
                            "Falha no checkpoint periódico. " +
                                    "content=" +
                                    "${request.contentType}:" +
                                    "${request.contentUuid} " +
                                    "position=" +
                                    "${request.positionSeconds}s " +
                                    "error=${exception.message}"
                        )
                    }
                }
        }
    }

    fun savePausedProgress(
        contentType: String,
        contentUuid: String,
        positionMs: Long,
        durationMs: Long,
        hasKnownProgress: Boolean,
        lastPersistedPositionSeconds: Long? = null,
        onResult: (
            WatchingProgressSaveResult
        ) -> Unit = {}
    ) {
        val request =
            buildProgressRequest(
                contentType =
                    contentType,

                contentUuid =
                    contentUuid,

                positionMs =
                    positionMs,

                durationMs =
                    durationMs,

                hasKnownProgress =
                    hasKnownProgress,

                lastPersistedPositionSeconds =
                    lastPersistedPositionSeconds,

                source =
                    ProgressSaveSource.PAUSE
            )
                ?: return

        if (
            uiState.isSaving
        ) {
            Log.d(
                TAG,
                "Pause progress ignorado: " +
                        "já existe um save em andamento."
            )

            return
        }

        viewModelScope.launch {
            progressSaveMutex
                .withLock {

                    val latestPersistedPosition =
                        resolveLastPersistedPosition(
                            contentUuid =
                                request.contentUuid,

                            fallbackPosition =
                                request
                                    .lastPersistedPositionSeconds
                        )

                    val hasPersistedProgressNow =
                        request.hasKnownProgress ||
                                lastPersistedPositionByContent
                                    .containsKey(
                                        request.contentUuid
                                    )

                    if (
                        hasPersistedProgressNow &&
                        latestPersistedPosition !=
                        null &&
                        request.positionSeconds ==
                        latestPersistedPosition
                    ) {
                        Log.d(
                            TAG,
                            "Pause progress ignorado: " +
                                    "posição " +
                                    "${request.positionSeconds}s " +
                                    "já estava persistida."
                        )

                        return@withLock
                    }

                    uiState =
                        uiState.copy(
                            isSaving =
                                true,

                            errorMessage =
                                null
                        )

                    try {
                        val result =
                            repository
                                .saveProgress(
                                    contentType =
                                        request.contentType,

                                    contentUuid =
                                        request.contentUuid,

                                    positionSeconds =
                                        request.positionSeconds,

                                    durationSeconds =
                                        request.durationSeconds,

                                    status =
                                        STATUS_PAUSED,

                                    forceProgressSave =
                                        true
                                )

                        registerPersistedResult(
                            result
                        )

                        uiState =
                            uiState.copy(
                                isSaving =
                                    false,

                                lastResult =
                                    result,

                                errorMessage =
                                    null
                            )

                        Log.d(
                            TAG,
                            "Progress Pause processado. " +
                                    "content=" +
                                    "${result.contentType}:" +
                                    "${result.contentUuid} " +
                                    "requested=" +
                                    "${result.requestedPositionSeconds}s " +
                                    "persisted=" +
                                    "${result.positionSeconds}s " +
                                    "saved=${result.saved} " +
                                    "reason=${result.reason} " +
                                    "completed=" +
                                    "${result.isCompleted}"
                        )

                        onResult(
                            result
                        )

                    } catch (
                        exception: Exception
                    ) {
                        uiState =
                            uiState.copy(
                                isSaving =
                                    false,

                                errorMessage =
                                    exception.message
                                        ?: "Não foi possível salvar o progresso."
                            )

                        Log.w(
                            TAG,
                            "Falha ao salvar progresso no Pause: " +
                                    "${exception.message}"
                        )
                    }
                }
        }
    }

    fun saveBackgroundProgress(
        contentType: String,
        contentUuid: String,
        positionMs: Long,
        durationMs: Long,
        hasKnownProgress: Boolean,
        lastPersistedPositionSeconds: Long? = null,
        reason: String,
        onResult: (
            WatchingProgressSaveResult
        ) -> Unit = {}
    ) {
        val request =
            buildProgressRequest(
                contentType =
                    contentType,

                contentUuid =
                    contentUuid,

                positionMs =
                    positionMs,

                durationMs =
                    durationMs,

                hasKnownProgress =
                    hasKnownProgress,

                lastPersistedPositionSeconds =
                    lastPersistedPositionSeconds,

                source =
                    ProgressSaveSource.BACKGROUND
            )
                ?: return

        viewModelScope.launch {
            progressSaveMutex
                .withLock {

                    val latestPersistedPosition =
                        resolveLastPersistedPosition(
                            contentUuid =
                                request.contentUuid,

                            fallbackPosition =
                                request
                                    .lastPersistedPositionSeconds
                        )

                    val hasPersistedProgressNow =
                        request.hasKnownProgress ||
                                lastPersistedPositionByContent
                                    .containsKey(
                                        request.contentUuid
                                    )

                    if (
                        hasPersistedProgressNow &&
                        latestPersistedPosition !=
                        null &&
                        request.positionSeconds ==
                        latestPersistedPosition
                    ) {
                        Log.d(
                            TAG,
                            "Progress Background ignorado: " +
                                    "posição " +
                                    "${request.positionSeconds}s " +
                                    "já estava persistida. " +
                                    "reason=$reason"
                        )

                        return@withLock
                    }

                    uiState =
                        uiState.copy(
                            isSaving =
                                true,

                            errorMessage =
                                null
                        )

                    try {
                        val result =
                            repository
                                .saveProgress(
                                    contentType =
                                        request.contentType,

                                    contentUuid =
                                        request.contentUuid,

                                    positionSeconds =
                                        request.positionSeconds,

                                    durationSeconds =
                                        request.durationSeconds,

                                    status =
                                        STATUS_PAUSED,

                                    forceProgressSave =
                                        true
                                )

                        registerPersistedResult(
                            result
                        )

                        uiState =
                            uiState.copy(
                                isSaving =
                                    false,

                                lastResult =
                                    result,

                                errorMessage =
                                    null
                            )

                        Log.d(
                            TAG,
                            "Progress Background processado. " +
                                    "reason=$reason " +
                                    "content=" +
                                    "${result.contentType}:" +
                                    "${result.contentUuid} " +
                                    "requested=" +
                                    "${result.requestedPositionSeconds}s " +
                                    "persisted=" +
                                    "${result.positionSeconds}s " +
                                    "saved=${result.saved} " +
                                    "reason_api=${result.reason} " +
                                    "status=${result.status} " +
                                    "completed=" +
                                    "${result.isCompleted}"
                        )

                        onResult(
                            result
                        )

                    } catch (
                        exception: Exception
                    ) {
                        uiState =
                            uiState.copy(
                                isSaving =
                                    false,

                                errorMessage =
                                    exception.message
                                        ?: "Não foi possível salvar o progresso."
                            )

                        Log.w(
                            TAG,
                            "Falha ao salvar progresso no Background. " +
                                    "reason=$reason " +
                                    "error=${exception.message}"
                        )
                    }
                }
        }
    }

    fun saveExitProgress(
        contentType: String,
        contentUuid: String,
        positionMs: Long,
        durationMs: Long,
        hasKnownProgress: Boolean,
        lastPersistedPositionSeconds: Long? = null,
        onComplete: () -> Unit
    ) {
        val request =
            buildProgressRequest(
                contentType =
                    contentType,

                contentUuid =
                    contentUuid,

                positionMs =
                    positionMs,

                durationMs =
                    durationMs,

                hasKnownProgress =
                    hasKnownProgress,

                lastPersistedPositionSeconds =
                    lastPersistedPositionSeconds,

                source =
                    ProgressSaveSource.EXIT
            )

        if (
            request ==
            null
        ) {
            onComplete()

            return
        }

        viewModelScope.launch {
            try {
                progressSaveMutex
                    .withLock {

                        val latestPersistedPosition =
                            resolveLastPersistedPosition(
                                contentUuid =
                                    request.contentUuid,

                                fallbackPosition =
                                    request
                                        .lastPersistedPositionSeconds
                            )

                        val hasPersistedProgressNow =
                            request.hasKnownProgress ||
                                    lastPersistedPositionByContent
                                        .containsKey(
                                            request.contentUuid
                                        )

                        if (
                            hasPersistedProgressNow &&
                            latestPersistedPosition !=
                            null &&
                            request.positionSeconds ==
                            latestPersistedPosition
                        ) {
                            Log.d(
                                TAG,
                                "Progress Exit ignorado: " +
                                        "posição " +
                                        "${request.positionSeconds}s " +
                                        "já estava persistida."
                            )

                            return@withLock
                        }

                        uiState =
                            uiState.copy(
                                isSaving =
                                    true,

                                errorMessage =
                                    null
                            )

                        try {
                            val result =
                                repository
                                    .saveProgress(
                                        contentType =
                                            request.contentType,

                                        contentUuid =
                                            request.contentUuid,

                                        positionSeconds =
                                            request.positionSeconds,

                                        durationSeconds =
                                            request.durationSeconds,

                                        status =
                                            STATUS_STOPPED,

                                        forceProgressSave =
                                            true
                                    )

                            registerPersistedResult(
                                result
                            )

                            uiState =
                                uiState.copy(
                                    isSaving =
                                        false,

                                    lastResult =
                                        result,

                                    errorMessage =
                                        null
                                )

                            Log.d(
                                TAG,
                                "Progress Exit processado. " +
                                        "content=" +
                                        "${result.contentType}:" +
                                        "${result.contentUuid} " +
                                        "requested=" +
                                        "${result.requestedPositionSeconds}s " +
                                        "persisted=" +
                                        "${result.positionSeconds}s " +
                                        "saved=${result.saved} " +
                                        "reason=${result.reason} " +
                                        "status=${result.status} " +
                                        "completed=" +
                                        "${result.isCompleted}"
                            )

                        } catch (
                            exception: Exception
                        ) {
                            uiState =
                                uiState.copy(
                                    isSaving =
                                        false,

                                    errorMessage =
                                        exception.message
                                            ?: "Não foi possível salvar o progresso."
                                )

                            Log.w(
                                TAG,
                                "Falha ao salvar progresso na saída: " +
                                        "${exception.message}"
                            )
                        }
                    }

            } finally {
                onComplete()
            }
        }
    }

    /*
     * FIM NATURAL DA REPRODUÇÃO
     *
     * Esta operação é diferente das demais:
     *
     * - não depende dos 90 segundos;
     * - não depende do intervalo de 30s;
     * - sempre usa force_progress_save=true;
     * - status=ended;
     * - posição final = duração.
     *
     * Portanto até um conteúdo muito curto
     * será corretamente marcado como concluído
     * se chegar naturalmente ao STATE_ENDED.
     */
    fun saveEndedProgress(
        contentType: String,
        contentUuid: String,
        positionMs: Long,
        durationMs: Long,
        onResult: (
            WatchingProgressSaveResult
        ) -> Unit = {},
        onComplete: () -> Unit = {}
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
            normalizedContentType !in
            VALID_CONTENT_TYPES ||
            normalizedContentUuid.isBlank()
        ) {
            Log.w(
                TAG,
                "Progress Ended ignorado: " +
                        "conteúdo inválido."
            )

            onComplete()

            return
        }

        val reportedPositionSeconds =
            positionMs
                .coerceAtLeast(
                    0L
                ) /
                    1_000L

        val reportedDurationSeconds =
            durationMs
                .coerceAtLeast(
                    0L
                ) /
                    1_000L

        /*
         * Em VOD o Media3 normalmente informa
         * duration no STATE_ENDED.
         *
         * Como proteção, se duration vier 0
         * mas currentPosition já for válido,
         * usamos a posição final como duração.
         *
         * STATE_ENDED nos garante que estamos
         * efetivamente no final da mídia.
         */
        val finalDurationSeconds =
            when {
                reportedDurationSeconds >
                        0L -> {
                    reportedDurationSeconds
                }

                reportedPositionSeconds >
                        0L -> {
                    reportedPositionSeconds
                }

                else -> {
                    0L
                }
            }

        if (
            finalDurationSeconds <=
            0L
        ) {
            Log.w(
                TAG,
                "Progress Ended ignorado: " +
                        "posição e duração indisponíveis."
            )

            onComplete()

            return
        }

        /*
         * O próprio backend também força
         * position=duration quando status=ended.
         *
         * Mesmo assim já enviamos o contrato
         * correto daqui.
         */
        val finalPositionSeconds =
            finalDurationSeconds

        viewModelScope.launch {
            try {
                /*
                 * Ended NUNCA é descartado porque
                 * outro save esteja em andamento.
                 *
                 * Se houver um Periodic/Pause
                 * terminando, aguardamos o Mutex
                 * e salvamos o estado terminal
                 * depois dele.
                 */
                progressSaveMutex
                    .withLock {

                        uiState =
                            uiState.copy(
                                isSaving =
                                    true,

                                errorMessage =
                                    null
                            )

                        try {
                            val result =
                                repository
                                    .saveProgress(
                                        contentType =
                                            normalizedContentType,

                                        contentUuid =
                                            normalizedContentUuid,

                                        positionSeconds =
                                            finalPositionSeconds,

                                        durationSeconds =
                                            finalDurationSeconds,

                                        status =
                                            STATUS_ENDED,

                                        forceProgressSave =
                                            true
                                    )

                            registerPersistedResult(
                                result
                            )

                            uiState =
                                uiState.copy(
                                    isSaving =
                                        false,

                                    lastResult =
                                        result,

                                    errorMessage =
                                        null
                                )

                            Log.d(
                                TAG,
                                "Progress Ended processado. " +
                                        "content=" +
                                        "${result.contentType}:" +
                                        "${result.contentUuid} " +
                                        "reported=" +
                                        "${reportedPositionSeconds}s " +
                                        "requested=" +
                                        "${result.requestedPositionSeconds}s " +
                                        "persisted=" +
                                        "${result.positionSeconds}s " +
                                        "duration=" +
                                        "${result.durationSeconds}s " +
                                        "saved=${result.saved} " +
                                        "reason=${result.reason} " +
                                        "status=${result.status} " +
                                        "completed=" +
                                        "${result.isCompleted}"
                            )

                            onResult(
                                result
                            )

                        } catch (
                            exception: Exception
                        ) {
                            uiState =
                                uiState.copy(
                                    isSaving =
                                        false,

                                    errorMessage =
                                        exception.message
                                            ?: "Não foi possível concluir o progresso."
                                )

                            Log.w(
                                TAG,
                                "Falha ao salvar Progress Ended. " +
                                        "content=" +
                                        "$normalizedContentType:" +
                                        "$normalizedContentUuid " +
                                        "error=${exception.message}"
                            )
                        }
                    }

            } finally {
                /*
                 * Mesmo se o POST de progresso
                 * falhar, precisamos encerrar
                 * a WatchingSession.
                 */
                onComplete()
            }
        }
    }

    /*
     * Centralizamos aqui a noção de
     * "já existe progresso".
     */
    private fun buildProgressRequest(
        contentType: String,
        contentUuid: String,
        positionMs: Long,
        durationMs: Long,
        hasKnownProgress: Boolean,
        lastPersistedPositionSeconds: Long?,
        source: ProgressSaveSource
    ): ProgressSaveRequest? {

        val normalizedContentType =
            contentType
                .trim()
                .lowercase(
                    Locale.US
                )

        val normalizedContentUuid =
            contentUuid.trim()

        if (
            normalizedContentType !in
            VALID_CONTENT_TYPES ||
            normalizedContentUuid.isBlank()
        ) {
            Log.w(
                TAG,
                "${source.label} progress ignorado: " +
                        "conteúdo inválido."
            )

            return null
        }

        val positionSeconds =
            positionMs
                .coerceAtLeast(
                    0L
                ) /
                    1_000L

        val durationSeconds =
            durationMs
                .coerceAtLeast(
                    0L
                ) /
                    1_000L

        if (
            durationSeconds <=
            0L
        ) {
            if (
                source !=
                ProgressSaveSource.PERIODIC
            ) {
                Log.d(
                    TAG,
                    "${source.label} progress ignorado: " +
                            "duração ainda indisponível."
                )
            }

            return null
        }

        val hasPersistedProgressNow =
            hasKnownProgress ||
                    lastPersistedPositionByContent
                        .containsKey(
                            normalizedContentUuid
                        )

        /*
         * Conteúdo novo somente entra no
         * Continue Assistindo a partir de 90s.
         *
         * END não passa por este método.
         * Ended possui regra terminal própria.
         */
        if (
            !hasPersistedProgressNow &&
            positionSeconds <
            FIRST_SAVE_AT_SECONDS
        ) {
            if (
                source !=
                ProgressSaveSource.PERIODIC
            ) {
                Log.d(
                    TAG,
                    "${source.label} progress ignorado: " +
                            "conteúdo novo em " +
                            "${positionSeconds}s. " +
                            "Primeiro save somente a partir de " +
                            "${FIRST_SAVE_AT_SECONDS}s."
                )
            }

            return null
        }

        return ProgressSaveRequest(
            contentType =
                normalizedContentType,

            contentUuid =
                normalizedContentUuid,

            positionSeconds =
                positionSeconds,

            durationSeconds =
                durationSeconds,

            hasKnownProgress =
                hasPersistedProgressNow,

            lastPersistedPositionSeconds =
                lastPersistedPositionSeconds
                    ?.coerceAtLeast(
                        0L
                    )
        )
    }

    private fun resolveLastPersistedPosition(
        contentUuid: String,
        fallbackPosition: Long?
    ): Long? {

        return lastPersistedPositionByContent[
            contentUuid
        ]
            ?: fallbackPosition
    }

    private fun registerPersistedResult(
        result: WatchingProgressSaveResult
    ) {
        if (
            result.progressUuid
                .isNotBlank()
        ) {
            lastPersistedPositionByContent[
                result.contentUuid
            ] =
                result.positionSeconds
        }
    }

    private data class ProgressSaveRequest(
        val contentType: String,
        val contentUuid: String,
        val positionSeconds: Long,
        val durationSeconds: Long,
        val hasKnownProgress: Boolean,
        val lastPersistedPositionSeconds: Long?
    )

    private enum class ProgressSaveSource(
        val label: String
    ) {
        PERIODIC(
            "Periodic"
        ),

        PAUSE(
            "Pause"
        ),

        BACKGROUND(
            "Background"
        ),

        EXIT(
            "Exit"
        )
    }

    private companion object {

        const val TAG =
            "LaranjadaProgress"

        const val FIRST_SAVE_AT_SECONDS =
            90L

        const val PERIODIC_SAVE_INTERVAL_SECONDS =
            30L

        const val PERIODIC_RETRY_MIN_INTERVAL_MS =
            5_000L

        const val STATUS_PLAYING =
            "playing"

        const val STATUS_PAUSED =
            "paused"

        const val STATUS_STOPPED =
            "stopped"

        const val STATUS_ENDED =
            "ended"

        val VALID_CONTENT_TYPES =
            setOf(
                "movie",
                "episode"
            )
    }
}