package com.rnx.laranjada.feature.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.LaranjadaRepositoryImpl
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import com.rnx.laranjada.feature.home.MediaItemUi
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

    var relatedItems by mutableStateOf<List<MediaItemUi>>(emptyList())
        private set

    var isRelatedLoading by mutableStateOf(false)
        private set

    var relatedErrorMessage by mutableStateOf<String?>(null)
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
            relatedItems = emptyList()
            relatedErrorMessage = null

            try {
                val detail = repository.getDetail(
                    contentType = contentType,
                    uuid = uuid
                )

                uiState = detail

                loadRelatedContent(
                    contentType = detail.contentTypeForApi,
                    uuid = detail.uuid
                )
            } catch (exception: Exception) {
                errorMessage = exception.message
            } finally {
                isLoading = false
            }
        }
    }

    fun reloadRelatedContent() {
        val detail = uiState ?: return

        loadRelatedContent(
            contentType = detail.contentTypeForApi,
            uuid = detail.uuid
        )
    }

    private fun loadRelatedContent(
        contentType: String,
        uuid: String
    ) {
        viewModelScope.launch {
            isRelatedLoading = true
            relatedErrorMessage = null

            try {
                relatedItems = repository.getRelatedContent(
                    contentType = contentType,
                    uuid = uuid,
                    limit = 20
                )
            } catch (exception: Exception) {
                relatedItems = emptyList()
                relatedErrorMessage = exception.message ?: "Não foi possível carregar sugestões."
            } finally {
                isRelatedLoading = false
            }
        }
    }
}