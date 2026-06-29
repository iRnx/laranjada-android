package com.rnx.laranjada.feature.home

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
    private val repository: LaranjadaRepository = LaranjadaRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(HomeMockData.uiState)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                uiState = repository.getHome()
            } catch (exception: Exception) {
                errorMessage = exception.message
            } finally {
                isLoading = false
            }
        }
    }
}