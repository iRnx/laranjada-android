package com.rnx.laranjada.feature.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.LaranjadaRepositoryImpl
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val repository: LaranjadaRepository = LaranjadaRepositoryImpl()
) : ViewModel() {

    var episodesUiState by mutableStateOf<PlayerEpisodesUiState?>(null)
        private set

    var isLoadingEpisodes by mutableStateOf(false)
        private set

    var episodesErrorMessage by mutableStateOf<String?>(null)
        private set

    private var loadedSeriesUuid: String? = null

    fun loadSeriesEpisodes(
        seriesUuid: String,
        forceRefresh: Boolean = false
    ) {
        if (seriesUuid.isBlank()) {
            episodesUiState = null
            episodesErrorMessage = null
            loadedSeriesUuid = null
            return
        }

        if (
            !forceRefresh &&
            loadedSeriesUuid == seriesUuid &&
            episodesUiState != null
        ) {
            return
        }

        loadedSeriesUuid = seriesUuid

        viewModelScope.launch {
            isLoadingEpisodes = true
            episodesErrorMessage = null

            try {
                val detail = repository.getDetail(
                    contentType = "series",
                    uuid = seriesUuid
                )

                episodesUiState = detail.toPlayerEpisodesUiState()
            } catch (exception: Exception) {
                episodesErrorMessage = exception.message
            } finally {
                isLoadingEpisodes = false
            }
        }
    }
}