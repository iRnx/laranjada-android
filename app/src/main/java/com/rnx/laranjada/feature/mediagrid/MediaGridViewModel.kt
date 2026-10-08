
package com.rnx.laranjada.feature.mediagrid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.LaranjadaRepositoryImpl
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MediaGridViewModel(
    private val repository: LaranjadaRepository =
        LaranjadaRepositoryImpl()
) : ViewModel() {

    private companion object {
        const val PAGE_SIZE = 60
    }

    var uiState by mutableStateOf(
        MediaGridUiState(
            pageSize = PAGE_SIZE
        )
    )
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var filtersExpanded by mutableStateOf(false)
        private set

    var currentPage by mutableIntStateOf(1)
        private set

    var draftFilters by mutableStateOf(
        MediaGridAppliedFiltersUi()
    )
        private set

    private var sectionSlug = ""
    private var initialTitle = ""

    private var loadJob: Job? = null
    private var requestGeneration = 0
    private var loaded = false

    fun start(
        sectionSlug: String,
        title: String
    ) {
        if (
            this.sectionSlug == sectionSlug &&
            (loaded || isLoading)
        ) {
            return
        }

        this.sectionSlug = sectionSlug
        this.initialTitle = title

        loaded = false
        currentPage = 1
        filtersExpanded = false

        draftFilters =
            MediaGridAppliedFiltersUi()

        uiState =
            MediaGridUiState(
                title = title,
                sectionSlug = sectionSlug,
                pageSize = PAGE_SIZE
            )

        loadPage(1)
    }

    fun reload() {
        loadPage(currentPage)
    }

    fun nextPage() {
        if (
            isLoading ||
            !uiState.hasNextPage
        ) {
            return
        }

        loadPage(currentPage + 1)
    }

    fun previousPage() {
        if (
            isLoading ||
            !uiState.hasPreviousPage ||
            currentPage <= 1
        ) {
            return
        }

        loadPage(currentPage - 1)
    }

    fun goToPage(page: Int) {
        val safePage =
            page.coerceIn(
                1,
                uiState.totalPages
            )

        if (
            isLoading ||
            safePage == currentPage
        ) {
            return
        }

        loadPage(safePage)
    }

    /*
     * FILTRAGEM ALFABÉTICA
     *
     * - Reutiliza os demais filtros.
     * - Volta para a página 1.
     * - Tocar novamente remove a letra.
     * - O backend filtra antes da paginação.
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
                draftFilters.letter == normalized
            ) {
                ""
            } else {
                normalized
            }

        draftFilters =
            draftFilters.copy(
                letter = nextLetter
            )

        loadPage(1)
    }

    /*
     * ORDENAÇÃO
     *
     * Mantém a letra ativa.
     */
    fun applyOrder(order: String) {
        val normalized =
            order.ifBlank {
                "updated_desc"
            }

        if (
            normalized == draftFilters.order
        ) {
            return
        }

        draftFilters =
            draftFilters.copy(
                order = normalized
            )

        loadPage(1)
    }

    /*
     * FILTROS EM LOTE
     *
     * Mantém letter, pois usamos copy().
     * Assim a seleção alfabética não some
     * ao alterar busca, ano, nota ou tipo.
     */
    fun applyFilters(
        search: String,
        year: String,
        ratingMin: String,
        kind: String
    ) {
        draftFilters =
            draftFilters.copy(
                q = search.trim(),
                year = year,
                ratingMin = ratingMin,
                kind = kind
            )

        loadPage(1)
    }

    fun clearFilters() {
        draftFilters =
            MediaGridAppliedFiltersUi()

        loadPage(1)
    }

    /*
     * Métodos mantidos para
     * compatibilidade com a feature.
     */
    fun toggleFilters() {
        filtersExpanded = !filtersExpanded
    }

    fun closeFilters() {
        filtersExpanded = false
    }

    fun onSearchChanged(value: String) {
        draftFilters =
            draftFilters.copy(q = value)
    }

    fun applySearch() {
        loadPage(1)
    }

    fun onYearChanged(value: String) {
        draftFilters =
            draftFilters.copy(year = value)

        loadPage(1)
    }

    fun onOrderChanged(value: String) {
        applyOrder(value)
    }

    fun onRatingMinChanged(value: String) {
        draftFilters =
            draftFilters.copy(
                ratingMin = value
            )

        loadPage(1)
    }

    fun onKindChanged(value: String) {
        draftFilters =
            draftFilters.copy(kind = value)

        loadPage(1)
    }

    private fun loadPage(page: Int) {
        if (sectionSlug.isBlank()) {
            errorMessage = "Seção inválida."
            return
        }

        loadJob?.cancel()

        val generation =
            ++requestGeneration

        val requestedSection =
            sectionSlug

        val requestedFilters =
            draftFilters

        isLoading = true
        errorMessage = null

        loadJob =
            viewModelScope.launch {
                try {
                    val result =
                        repository.getHomeSection(
                            sectionSlug =
                                requestedSection,

                            page = page,

                            pageSize =
                                PAGE_SIZE,

                            filters =
                                requestedFilters
                        )

                    if (
                        generation != requestGeneration ||
                        sectionSlug != requestedSection
                    ) {
                        return@launch
                    }

                    currentPage = page

                    uiState =
                        result.copy(
                            title =
                                result.title.ifBlank {
                                    initialTitle
                                },

                            currentPage = page,

                            pageSize =
                                PAGE_SIZE
                        )

                    /*
                     * O Django devolve a letra
                     * normalizada em applied_filters.
                     */
                    draftFilters =
                        result.appliedFilters

                    loaded = true

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
                                ?: "Não foi possível carregar os conteúdos."
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
}
