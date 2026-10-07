package com.rnx.laranjada.feature.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.LaranjadaRepositoryImpl
import com.rnx.laranjada.data.repository.WatchingProgressRepositoryImpl
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import com.rnx.laranjada.domain.repository.WatchingProgressRepository
import com.rnx.laranjada.feature.home.data.HomeMockData
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository:
    LaranjadaRepository =
        LaranjadaRepositoryImpl(),

    private val watchingProgressRepository:
    WatchingProgressRepository =
        WatchingProgressRepositoryImpl()
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

    var restartingContentUuid by mutableStateOf<String?>(
        null
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

    fun refreshContinueWatching() {
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

    fun restartContinueWatching(
        item: ContinueWatchingUi,
        onSuccess: () -> Unit
    ) {
        val normalizedContentType =
            item.contentType
                .trim()
                .lowercase()

        val normalizedContentUuid =
            item.contentUuid
                .trim()

        if (
            normalizedContentType !in
            setOf(
                "movie",
                "episode"
            ) ||
            normalizedContentUuid
                .isBlank()
        ) {
            Log.w(
                TAG,
                "Restart ignorado: conteúdo inválido. " +
                        "content=${item.contentType}:" +
                        "${item.contentUuid}"
            )

            return
        }

        if (
            restartingContentUuid !=
            null
        ) {
            Log.d(
                TAG,
                "Restart ignorado: já existe " +
                        "uma reinicialização em andamento."
            )

            return
        }

        restartingContentUuid =
            normalizedContentUuid

        errorMessage =
            null

        viewModelScope.launch {
            try {
                val deletedCount =
                    watchingProgressRepository
                        .resetProgress(
                            contentType =
                                normalizedContentType,

                            contentUuid =
                                normalizedContentUuid
                        )

                uiState =
                    uiState.copy(
                        continueWatching =
                            uiState
                                .continueWatching
                                .filterNot {
                                        current ->

                                    current.contentType
                                        .trim()
                                        .lowercase() ==
                                            normalizedContentType &&
                                            current.contentUuid
                                                .trim() ==
                                            normalizedContentUuid
                                }
                    )

                Log.d(
                    TAG,
                    "Progresso reiniciado. " +
                            "content=$normalizedContentType:" +
                            "$normalizedContentUuid " +
                            "deleted=$deletedCount"
                )

                onSuccess()

            } catch (
                exception: Exception
            ) {
                errorMessage =
                    exception.message
                        ?: "Não foi possível reiniciar o conteúdo."

                Log.w(
                    TAG,
                    "Falha ao reiniciar progresso. " +
                            "content=$normalizedContentType:" +
                            "$normalizedContentUuid " +
                            "error=${exception.message}"
                )

            } finally {
                restartingContentUuid =
                    null
            }
        }
    }

    private companion object {

        const val TAG =
            "LaranjadaContinue"
    }
}