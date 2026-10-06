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
     * por este Mutex.
     *
     * Isso é importante principalmente no
     * cenário:
     *
     * Pause
     * -> POST ainda executando
     * -> usuário aperta X/Voltar
     *
     * O save de saída espera o Pause
     * terminar em vez de ser descartado.
     */
    private val progressSaveMutex =
        Mutex()

    /*
     * Guarda, durante a vida deste Player,
     * a posição mais recente que sabemos
     * ter sido persistida no servidor.
     *
     * Serve principalmente para evitar:
     *
     * Pause em 100s
     * -> saved=true
     * -> X imediatamente em 100s
     * -> POST duplicado desnecessário
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
         * Para Pause continuamos evitando
         * empilhar diversos saves enquanto
         * outro já está processando.
         *
         * O save de EXIT é diferente:
         * ele nunca será simplesmente
         * descartado por isso.
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

                    if (
                        request.hasKnownProgress &&
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
     * SAVE DE SAÍDA
     *
     * Usado exclusivamente nesta etapa
     * quando o usuário:
     *
     * - toca no X do Player;
     * - usa Voltar do Android.
     *
     * Esse método SEMPRE chama onComplete,
     * mesmo quando:
     *
     * - o conteúdo ainda não atingiu 90s;
     * - a mesma posição já foi persistida;
     * - houve falha de rede/API.
     *
     * Assim uma falha de progresso nunca
     * prende o usuário dentro do Player.
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
         * Se não há nada a persistir,
         * continuamos normalmente com
         * Stop + fechamento do Player.
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

                        /*
                         * Essa consulta acontece
                         * DENTRO do Mutex.
                         *
                         * Portanto, se um Pause
                         * estava salvando antes
                         * do X/Voltar, neste ponto
                         * já conhecemos o resultado
                         * daquele Pause.
                         */
                        val latestPersistedPosition =
                            resolveLastPersistedPosition(
                                contentUuid =
                                    request.contentUuid,

                                fallbackPosition =
                                    request
                                        .lastPersistedPositionSeconds
                            )

                        if (
                            request.hasKnownProgress &&
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
                 * Mesmo que o save falhe,
                 * o Player precisa conseguir
                 * encerrar normalmente.
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

        /*
         * Se Media3 ainda não informou uma
         * duração válida, não gravamos uma
         * duração zero por acidente.
         */
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
         * Regra de produto igual à Web:
         *
         * conteúdo NOVO só começa a entrar
         * no Continue Assistindo após 90s.
         *
         * Conteúdo que JÁ POSSUI progresso
         * pode ser atualizado antes disso.
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
         * progressUuid preenchido significa
         * que o servidor possui um objeto
         * de progresso persistido.
         *
         * Mesmo saved=false pode devolver
         * um progresso já existente.
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