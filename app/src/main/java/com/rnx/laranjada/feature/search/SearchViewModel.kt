package com.rnx.laranjada.feature.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.LaranjadaRepositoryImpl
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS =
    400L

class SearchViewModel(
    private val repository:
    LaranjadaRepository =
        LaranjadaRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(
        SearchUiState()
    )
        private set

    private var searchJob: Job? =
        null

    /*
     * Evita que uma resposta antiga
     * substitua uma pesquisa mais nova.
     */
    private var requestGeneration =
        0

    fun onQueryChanged(
        value: String
    ) {
        searchJob?.cancel()

        val normalizedQuery =
            value.trim()

        val generation =
            ++requestGeneration

        if (
            normalizedQuery.isBlank()
        ) {
            uiState =
                SearchUiState(
                    query =
                        value
                )

            return
        }

        /*
         * Limpamos os resultados antigos
         * imediatamente para nunca mostrar
         * conteúdo de outra pesquisa.
         */
        uiState =
            uiState.copy(
                query =
                    value,

                submittedQuery =
                    "",

                totalCount =
                    0,

                results =
                    emptyList(),

                isLoading =
                    true,

                errorMessage =
                    null,

                hasSearched =
                    false
            )

        searchJob =
            viewModelScope.launch {

                delay(
                    SEARCH_DEBOUNCE_MS
                )

                executeSearch(
                    query =
                        normalizedQuery,

                    generation =
                        generation
                )
            }
    }

    fun submitSearch() {
        searchJob?.cancel()

        val normalizedQuery =
            uiState.query.trim()

        val generation =
            ++requestGeneration

        if (
            normalizedQuery.isBlank()
        ) {
            uiState =
                SearchUiState()

            return
        }

        uiState =
            uiState.copy(
                submittedQuery =
                    "",

                totalCount =
                    0,

                results =
                    emptyList(),

                isLoading =
                    true,

                errorMessage =
                    null,

                hasSearched =
                    false
            )

        searchJob =
            viewModelScope.launch {
                executeSearch(
                    query =
                        normalizedQuery,

                    generation =
                        generation
                )
            }
    }

    fun retry() {
        submitSearch()
    }

    fun clearSearch() {
        searchJob?.cancel()

        requestGeneration++

        uiState =
            SearchUiState()
    }

    private suspend fun executeSearch(
        query: String,
        generation: Int
    ) {
        try {
            val response =
                repository.search(
                    query
                )

            /*
             * Uma nova pesquisa foi iniciada
             * enquanto aguardávamos a API.
             */
            if (
                generation !=
                requestGeneration
            ) {
                return
            }

            /*
             * A caixa também pode ter sido
             * alterada sem que essa resposta
             * ainda tenha sido cancelada
             * no transporte HTTP.
             */
            if (
                uiState.query
                    .trim() != query
            ) {
                return
            }

            uiState =
                uiState.copy(
                    submittedQuery =
                        response.query,

                    totalCount =
                        response.count,

                    results =
                        response.items,

                    isLoading =
                        false,

                    errorMessage =
                        null,

                    hasSearched =
                        true
                )

        } catch (
            cancelled:
            CancellationException
        ) {
            throw cancelled

        } catch (
            exception: Exception
        ) {
            if (
                generation !=
                requestGeneration
            ) {
                return
            }

            uiState =
                uiState.copy(
                    submittedQuery =
                        query,

                    totalCount =
                        0,

                    results =
                        emptyList(),

                    isLoading =
                        false,

                    errorMessage =
                        exception.message
                            ?: "Não foi possível realizar a busca.",

                    hasSearched =
                        true
                )
        }
    }
}