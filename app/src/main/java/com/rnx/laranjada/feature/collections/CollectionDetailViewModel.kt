
package com.rnx.laranjada.feature.collections

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

class CollectionDetailViewModel(
    private val collectionUuid: String,
    private val repository: LaranjadaRepository =
        LaranjadaRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(
        CollectionDetailUiState(
            uuid = collectionUuid,
            title = "Coleção"
        )
    )
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var loadJob: Job? = null
    private var searchJob: Job? = null
    private var requestGeneration = 0

    init {
        loadCollection()
    }

    /*
     * Carrega a coleção com os filtros.
     *
     * Não existe paginação na API
     * atual de Coleções.
     */
    fun loadCollection(
        filters: CollectionAppliedFiltersUi =
            uiState.appliedFilters
    ) {
        searchJob?.cancel()
        loadJob?.cancel()

        val generation =
            ++requestGeneration

        /*
         * Atualiza a seleção imediatamente,
         * mas o resultado vem do Django.
         */
        uiState =
            uiState.copy(
                appliedFilters = filters
            )

        isLoading = true
        errorMessage = null

        loadJob =
            viewModelScope.launch {
                try {
                    val result =
                        repository.getCollectionDetail(
                            uuid = collectionUuid,
                            filters = filters
                        )

                    if (
                        generation != requestGeneration
                    ) {
                        return@launch
                    }

                    /*
                     * A API devolve a letra
                     * normalizada em applied_filters.
                     */
                    uiState = result

                } catch (
                    cancelled: CancellationException
                ) {
                    throw cancelled

                } catch (
                    exception: Exception
                ) {
                    if (
                        generation == requestGeneration
                    ) {
                        errorMessage =
                            exception.message
                                ?: "Não foi possível carregar a coleção."
                    }

                } finally {
                    if (
                        generation == requestGeneration
                    ) {
                        isLoading = false
                    }
                }
            }
    }

    /*
     * SELEÇÃO DE LETRA
     *
     * Tocar na mesma letra remove
     * o filtro alfabético.
     */
    fun selectLetter(value: String) {
        if (isLoading) {
            return
        }

        val normalized =
            value.trim().uppercase()

        if (
            normalized != "#" &&
            (
                    normalized.length != 1 ||
                            normalized[0] !in 'A'..'Z'
                    )
        ) {
            return
        }

        val nextLetter =
            if (
                uiState.appliedFilters.letter ==
                normalized
            ) {
                ""
            } else {
                normalized
            }

        loadCollection(
            uiState.appliedFilters.copy(
                letter = nextLetter
            )
        )
    }

    /*
     * Aplica os filtros em lote.
     *
     * Preserva letter e order.
     */
    fun applyFilters(
        search: String,
        year: String,
        ratingMin: String,
        type: String
    ) {
        loadCollection(
            uiState.appliedFilters.copy(
                q = search.trim(),
                year = year,
                ratingMin = ratingMin,
                type = type
            )
        )
    }

    fun onOrderChanged(value: String) {
        loadCollection(
            uiState.appliedFilters.copy(
                order =
                    value.ifBlank {
                        "created_desc"
                    }
            )
        )
    }

    fun onYearChanged(value: String) {
        loadCollection(
            uiState.appliedFilters.copy(
                year = value
            )
        )
    }

    fun onRatingMinChanged(value: String) {
        loadCollection(
            uiState.appliedFilters.copy(
                ratingMin = value
            )
        )
    }

    fun onTypeChanged(value: String) {
        loadCollection(
            uiState.appliedFilters.copy(
                type = value
            )
        )
    }

    /*
     * Mantido por compatibilidade:
     * busca com debounce quando usada
     * fora do novo painel.
     */
    fun onSearchChanged(value: String) {
        val next =
            uiState.appliedFilters.copy(
                q = value
            )

        uiState =
            uiState.copy(
                appliedFilters = next
            )

        searchJob?.cancel()

        searchJob =
            viewModelScope.launch {
                delay(350L)

                loadCollection(
                    next
                )
            }
    }

    fun clearFilters() {
        loadCollection(
            CollectionAppliedFiltersUi()
        )
    }
}
