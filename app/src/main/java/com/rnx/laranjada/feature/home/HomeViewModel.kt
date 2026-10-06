package com.rnx.laranjada.feature.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.LaranjadaRepositoryImpl
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import com.rnx.laranjada.feature.home.data.HomeMockData
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository:
    LaranjadaRepository =
        LaranjadaRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(
        HomeMockData.uiState
    )
        private set

    var isLoading by mutableStateOf(
        false
    )
        private set

    var isRefreshingContinueWatching by mutableStateOf(
        false
    )
        private set

    var errorMessage by mutableStateOf<String?>(
        null
    )
        private set

    init {
        loadHome()
    }

    fun loadHome() {
        /*
         * Marcamos o carregamento ANTES
         * de abrir a coroutine.
         *
         * Isso evita que a Home recém-criada
         * faça um segundo GET de Continue
         * ao mesmo tempo que o primeiro load.
         */
        if (
            isLoading
        ) {
            return
        }

        isLoading =
            true

        viewModelScope.launch {

            errorMessage =
                null

            try {
                /*
                 * Primeiro carregamos a Home.
                 *
                 * Se o Continue Assistindo
                 * falhar, não queremos perder
                 * banner, categorias e catálogo.
                 */
                val homeState =
                    repository.getHome()

                uiState =
                    homeState

                try {
                    val continueWatching =
                        repository
                            .getContinueWatching()

                    uiState =
                        uiState.copy(
                            continueWatching =
                                continueWatching
                        )

                    Log.d(
                        TAG,
                        "Continue Assistindo carregado. " +
                                "count=${continueWatching.size}"
                    )

                } catch (
                    exception: Exception
                ) {
                    errorMessage =
                        exception.message

                    Log.w(
                        TAG,
                        "Falha ao carregar Continue Assistindo: " +
                                "${exception.message}"
                    )
                }

            } catch (
                exception: Exception
            ) {
                errorMessage =
                    exception.message

                Log.w(
                    TAG,
                    "Falha ao carregar Home: " +
                            "${exception.message}"
                )

            } finally {
                isLoading =
                    false
            }
        }
    }

    /*
     * Atualiza SOMENTE o Continue Assistindo.
     *
     * Não recarrega:
     * - banner;
     * - categorias;
     * - catálogo;
     * - coleções.
     *
     * Isso deixa o retorno do Player leve
     * e evita requisições desnecessárias.
     */
    fun refreshContinueWatching() {
        /*
         * Se a Home ainda estiver fazendo
         * o carregamento inicial, ela já vai
         * buscar o Continue Assistindo.
         */
        if (
            isLoading
        ) {
            Log.d(
                TAG,
                "Refresh ignorado: " +
                        "Home ainda está carregando."
            )

            return
        }

        /*
         * Evita dois refreshes simultâneos.
         */
        if (
            isRefreshingContinueWatching
        ) {
            Log.d(
                TAG,
                "Refresh ignorado: " +
                        "já existe atualização em andamento."
            )

            return
        }

        isRefreshingContinueWatching =
            true

        viewModelScope.launch {
            try {
                val continueWatching =
                    repository
                        .getContinueWatching()

                uiState =
                    uiState.copy(
                        continueWatching =
                            continueWatching
                    )

                val firstItem =
                    continueWatching
                        .firstOrNull()

                Log.d(
                    TAG,
                    "Continue Assistindo atualizado. " +
                            "count=${continueWatching.size} " +
                            "first=" +
                            if (
                                firstItem !=
                                null
                            ) {
                                "${firstItem.contentType}:" +
                                        "${firstItem.contentUuid} " +
                                        "position=" +
                                        "${firstItem.positionSeconds}s"
                            } else {
                                "none"
                            }
                )

            } catch (
                exception: Exception
            ) {
                /*
                 * Não destruímos a lista antiga
                 * caso o refresh falhe.
                 *
                 * O usuário continua vendo
                 * o Continue que já estava
                 * carregado.
                 */
                errorMessage =
                    exception.message

                Log.w(
                    TAG,
                    "Falha ao atualizar Continue Assistindo: " +
                            "${exception.message}"
                )

            } finally {
                isRefreshingContinueWatching =
                    false
            }
        }
    }

    private companion object {

        const val TAG =
            "LaranjadaContinue"
    }
}