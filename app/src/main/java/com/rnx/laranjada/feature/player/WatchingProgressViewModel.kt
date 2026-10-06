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
     * Nesta primeira etapa funcional
     * salvamos SOMENTE quando o usuário
     * toca explicitamente em Pause.
     *
     * Timer periódico, background,
     * saída e ended serão adicionados
     * separadamente depois dos testes.
     */
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
                "Pause progress ignorado: " +
                        "conteúdo inválido."
            )

            return
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
         * Sem duração válida ainda,
         * não persistimos.
         *
         * Evita gravar progresso durante
         * momentos em que o Media3 ainda
         * não conhece a duração.
         */
        if (
            durationSeconds <=
            0L
        ) {
            Log.d(
                TAG,
                "Pause progress ignorado: " +
                        "duração ainda indisponível."
            )

            return
        }

        /*
         * Regra de produto igual à Web:
         *
         * Conteúdo NOVO somente começa
         * a fazer parte do Continue
         * Assistindo a partir de 90s.
         *
         * Se já havia progresso conhecido,
         * o Pause pode forçar atualização
         * antes disso.
         */
        if (
            !hasKnownProgress &&
            positionSeconds <
            FIRST_SAVE_AT_SECONDS
        ) {
            Log.d(
                TAG,
                "Pause progress ignorado: " +
                        "conteúdo novo em ${positionSeconds}s. " +
                        "Primeiro save somente a partir de " +
                        "${FIRST_SAVE_AT_SECONDS}s."
            )

            return
        }

        /*
         * Evita request e UPDATE no banco
         * quando o Pause ocorreu exatamente
         * na posição que já conhecemos como
         * persistida.
         */
        if (
            hasKnownProgress &&
            lastPersistedPositionSeconds !=
            null &&
            positionSeconds ==
            lastPersistedPositionSeconds
        ) {
            Log.d(
                TAG,
                "Pause progress ignorado: " +
                        "posição ${positionSeconds}s " +
                        "já estava persistida."
            )

            return
        }

        /*
         * Nesta etapa não precisamos
         * enfileirar múltiplos saves de Pause.
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

        uiState =
            uiState.copy(
                isSaving =
                    true,
                errorMessage =
                    null
            )

        viewModelScope.launch {
            try {
                val result =
                    repository
                        .saveProgress(
                            contentType =
                                normalizedContentType,

                            contentUuid =
                                normalizedContentUuid,

                            positionSeconds =
                                positionSeconds,

                            durationSeconds =
                                durationSeconds,

                            status =
                                STATUS_PAUSED,

                            forceProgressSave =
                                true
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
                            "content=${result.contentType}:${result.contentUuid} " +
                            "requested=${result.requestedPositionSeconds}s " +
                            "persisted=${result.positionSeconds}s " +
                            "saved=${result.saved} " +
                            "reason=${result.reason} " +
                            "completed=${result.isCompleted}"
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

    private companion object {

        const val TAG =
            "LaranjadaProgress"

        const val FIRST_SAVE_AT_SECONDS =
            90L

        const val STATUS_PAUSED =
            "paused"

        val VALID_CONTENT_TYPES =
            setOf(
                "movie",
                "episode"
            )
    }
}