package com.rnx.laranjada.feature.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.LaranjadaRepositoryImpl
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: LaranjadaRepository = LaranjadaRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf<DetailUiState?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var currentKey: String? = null

    fun loadDetail(
        contentType: String,
        uuid: String
    ) {
        val nextKey = "$contentType:$uuid"

        if (currentKey == nextKey && uiState != null) {
            return
        }

        currentKey = nextKey

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            uiState = null

            try {
                uiState = repository.getDetail(
                    contentType = contentType,
                    uuid = uuid
                )
            } catch (exception: Exception) {
                errorMessage = exception.message
            } finally {
                isLoading = false
            }
        }
    }
}