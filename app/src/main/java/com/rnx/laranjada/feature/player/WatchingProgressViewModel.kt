package com.rnx.laranjada.feature.player

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
     * Todos os saves de progresso passam
     * pelo mesmo Mutex.
     *
     * Isso evita concorrência entre:
     *
     * Pause
     * Background
     * Exit
     *
     * Exemplo:
     *
     * Pause iniciou POST
     * -> usuário aperta Home
     * -> Background espera
     * -> Pause termina
     * -> Background verifica posição
     * -> evita duplicidade se necessário
     */
    private val progressSaveMutex =
        Mutex()

    /*
     * Última posição que sabemos ter sido
     * persistida no servidor durante a vida
     * deste Player.
     */
    private val lastPersistedPositionByContent =
        mutableMapOf<String, Long>()

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

        /*
         * Para o clique normal de Pause
         * continuamos evitando empilhar
         * vários requests.
         *
         * Background e Exit são tratados
         * de forma diferente e podem esperar
         * o Mutex.
         */
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

    /*
     * SAVE DE BACKGROUND
     *
     * Chamado quando o Player deixa de estar
     * visível por:
     *
     * - botão Home;
     * - troca para outro aplicativo;
     * - tela de Recentes;
     * - bloqueio da tela.
     *
     * Picture-in-Picture é filtrado antes
     * de chegar aqui.
     *
     * Esse save usa status=paused porque
     * o usuário não saiu do Player.
     */
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

        /*
         * Diferente do Pause normal,
         * Background NÃO é descartado apenas
         * porque outro save está rodando.
         *
         * Ele entra na fila do Mutex.
         */
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

                    /*
                     * Pode acontecer:
                     *
                     * Pause em 100s
                     * -> imediatamente Home
                     * -> Background também captura 100s
                     *
                     * Depois que o Mutex libera,
                     * verificamos novamente e
                     * evitamos POST duplicado.
                     */
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

    /*
     * SAVE DE SAÍDA
     *
     * Usado quando:
     *
     * - toca no X;
     * - usa Voltar do Android.
     *
     * A ordem continua:
     *
     * Progress
     * -> Stop
     * -> sair do Player
     */
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

        /*
         * Nada para persistir.
         *
         * Continua normalmente com Stop.
         */
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
                /*
                 * Mesmo se o save falhar,
                 * o usuário precisa conseguir
                 * fechar o Player.
                 */
                onComplete()
            }
        }
    }

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
            Log.d(
                TAG,
                "${source.label} progress ignorado: " +
                        "duração ainda indisponível."
            )

            return null
        }

        /*
         * Regra de produto:
         *
         * Conteúdo novo:
         * primeiro save somente >= 90s.
         *
         * Conteúdo já conhecido:
         * force save pode atualizar mesmo
         * abaixo de 90s.
         */
        if (
            !hasKnownProgress &&
            positionSeconds <
            FIRST_SAVE_AT_SECONDS
        ) {
            Log.d(
                TAG,
                "${source.label} progress ignorado: " +
                        "conteúdo novo em " +
                        "${positionSeconds}s. " +
                        "Primeiro save somente a partir de " +
                        "${FIRST_SAVE_AT_SECONDS}s."
            )

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
                hasKnownProgress,

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
        /*
         * Mesmo saved=false pode representar
         * um progresso já existente.
         *
         * progressUuid é nossa evidência
         * de que o backend possui registro.
         */
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

        const val STATUS_PAUSED =
            "paused"

        const val STATUS_STOPPED =
            "stopped"

        val VALID_CONTENT_TYPES =
            setOf(
                "movie",
                "episode"
            )
    }
}