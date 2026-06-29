package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.feature.details.DetailUiState
import com.rnx.laranjada.feature.home.HomeUiState

interface LaranjadaRepository {
    suspend fun getHome(): HomeUiState

    suspend fun getDetail(
        contentType: String,
        uuid: String
    ): DetailUiState
}