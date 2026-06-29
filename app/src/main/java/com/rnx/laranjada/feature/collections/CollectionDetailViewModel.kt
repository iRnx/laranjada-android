package com.rnx.laranjada.feature.collections

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.LaranjadaRepositoryImpl
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CollectionDetailViewModel(
    private val collectionUuid: String,
    private val repository: LaranjadaRepository = LaranjadaRepositoryImpl()
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

    init {
        loadCollection()
    }

    fun loadCollection(
        filters: CollectionAppliedFiltersUi = uiState.appliedFilters
    ) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            fetchCollection(filters)
        }
    }

    fun onSearchChanged(value: String) {
        val nextFilters = uiState.appliedFilters.copy(q = value)

        uiState = uiState.copy(
            appliedFilters = nextFilters
        )

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(350)
            loadCollection(nextFilters)
        }
    }

    fun onYearChanged(value: String) {
        applyFilters(
            uiState.appliedFilters.copy(year = value)
        )
    }

    fun onOrderChanged(value: String) {
        applyFilters(
            uiState.appliedFilters.copy(order = value.ifBlank { "created_desc" })
        )
    }

    fun onRatingMinChanged(value: String) {
        applyFilters(
            uiState.appliedFilters.copy(ratingMin = value)
        )
    }

    fun onTypeChanged(value: String) {
        applyFilters(
            uiState.appliedFilters.copy(type = value)
        )
    }

    fun clearFilters() {
        applyFilters(
            CollectionAppliedFiltersUi()
        )
    }

    private fun applyFilters(filters: CollectionAppliedFiltersUi) {
        searchJob?.cancel()

        uiState = uiState.copy(
            appliedFilters = filters
        )

        loadCollection(filters)
    }

    private suspend fun fetchCollection(filters: CollectionAppliedFiltersUi) {
        isLoading = true
        errorMessage = null

        try {
            uiState = repository.getCollectionDetail(
                uuid = collectionUuid,
                filters = filters
            )
        } catch (exception: Exception) {
            errorMessage = exception.message
        } finally {
            isLoading = false
        }
    }
}