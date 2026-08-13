package com.rnx.laranjada.feature.mediagrid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.LaranjadaRepositoryImpl
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import kotlinx.coroutines.launch

class MediaGridViewModel(
    private val repository: LaranjadaRepository = LaranjadaRepositoryImpl()
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 60
    }

    var uiState by mutableStateOf(MediaGridUiState(pageSize = PAGE_SIZE))
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var filtersExpanded by mutableStateOf(false)
        private set

    private var sectionSlug: String = ""
    private var initialTitle: String = ""

    var currentPage by mutableIntStateOf(1)
        private set

    var draftFilters by mutableStateOf(MediaGridAppliedFiltersUi())
        private set

    fun start(
        sectionSlug: String,
        title: String
    ) {
        if (this.sectionSlug == sectionSlug && uiState.items.isNotEmpty()) {
            return
        }

        this.sectionSlug = sectionSlug
        this.initialTitle = title

        draftFilters = MediaGridAppliedFiltersUi()
        loadPage(page = 1)
    }

    fun toggleFilters() {
        filtersExpanded = !filtersExpanded
    }

    fun closeFilters() {
        filtersExpanded = false
    }

    fun reload() {
        loadPage(page = currentPage)
    }

    fun nextPage() {
        if (!uiState.hasNextPage || isLoading) {
            return
        }

        loadPage(page = currentPage + 1)
    }

    fun previousPage() {
        if (!uiState.hasPreviousPage || currentPage <= 1 || isLoading) {
            return
        }

        loadPage(page = currentPage - 1)
    }

    fun goToPage(page: Int) {
        val safePage = page.coerceIn(1, uiState.totalPages)

        if (safePage == currentPage || isLoading) {
            return
        }

        loadPage(page = safePage)
    }

    fun onSearchChanged(value: String) {
        draftFilters = draftFilters.copy(q = value)
    }

    fun onYearChanged(value: String) {
        draftFilters = draftFilters.copy(year = value)
        applyFilters()
    }

    fun onOrderChanged(value: String) {
        draftFilters = draftFilters.copy(order = value.ifBlank { "updated_desc" })
        applyFilters()
    }

    fun onRatingMinChanged(value: String) {
        draftFilters = draftFilters.copy(ratingMin = value)
        applyFilters()
    }

    fun onKindChanged(value: String) {
        draftFilters = draftFilters.copy(kind = value)
        applyFilters()
    }

    fun applySearch() {
        applyFilters()
    }

    fun clearFilters() {
        draftFilters = MediaGridAppliedFiltersUi()
        loadPage(page = 1)
    }

    private fun applyFilters() {
        loadPage(page = 1)
    }

    private fun loadPage(page: Int) {
        if (sectionSlug.isBlank()) {
            errorMessage = "Seção inválida."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val result = repository.getHomeSection(
                    sectionSlug = sectionSlug,
                    page = page,
                    pageSize = PAGE_SIZE,
                    filters = draftFilters
                )

                currentPage = page

                uiState = result.copy(
                    title = result.title.ifBlank { initialTitle },
                    currentPage = page,
                    pageSize = PAGE_SIZE
                )

                draftFilters = result.appliedFilters
            } catch (exception: Exception) {
                errorMessage = exception.message ?: "Não foi possível carregar os conteúdos."
            } finally {
                isLoading = false
            }
        }
    }
}