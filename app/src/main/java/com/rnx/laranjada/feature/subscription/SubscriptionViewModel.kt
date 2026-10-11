package com.rnx.laranjada.feature.subscription

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.remote.api.SubscriptionApiException
import com.rnx.laranjada.data.repository.SubscriptionRepositoryImpl
import com.rnx.laranjada.domain.model.SubscriptionOverview
import com.rnx.laranjada.domain.repository.SubscriptionRepository
import kotlinx.coroutines.launch

data class SubscriptionUiState(
    val overview: SubscriptionOverview? = null,
    val isLoading: Boolean = false,
    val loadErrorMessage: String? = null
)

class SubscriptionViewModel(
    private val repository:
    SubscriptionRepository =
        SubscriptionRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(
        SubscriptionUiState()
    )
        private set

    private var started =
        false

    fun start(
        onSessionEnded: () -> Unit
    ) {
        if (
            started
        ) {
            return
        }

        started =
            true

        loadOverview(
            showLoading =
                true,

            onSessionEnded =
                onSessionEnded
        )
    }

    fun retry(
        onSessionEnded: () -> Unit
    ) {
        loadOverview(
            showLoading =
                true,

            onSessionEnded =
                onSessionEnded
        )
    }

    fun refreshAfterReturn(
        onSessionEnded: () -> Unit
    ) {
        if (
            !started ||
            uiState.isLoading ||
            uiState.overview == null
        ) {
            return
        }

        loadOverview(
            showLoading =
                false,

            onSessionEnded =
                onSessionEnded
        )
    }

    private fun loadOverview(
        showLoading: Boolean,
        onSessionEnded: () -> Unit
    ) {
        if (
            showLoading &&
            uiState.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (
                showLoading
            ) {
                uiState =
                    uiState.copy(
                        isLoading =
                            true,

                        loadErrorMessage =
                            null
                    )
            }

            try {
                val overview =
                    repository.getOverview()

                uiState =
                    uiState.copy(
                        overview =
                            overview,

                        isLoading =
                            false,

                        loadErrorMessage =
                            null
                    )

            } catch (
                exception:
                Exception
            ) {
                if (
                    exception is
                            SubscriptionApiException &&
                    exception.statusCode ==
                    401
                ) {
                    onSessionEnded()

                    return@launch
                }

                if (
                    showLoading ||
                    uiState.overview == null
                ) {
                    uiState =
                        uiState.copy(
                            isLoading =
                                false,

                            loadErrorMessage =
                                exception.message
                                    ?: "Não foi possível carregar sua assinatura."
                        )
                }
            }
        }
    }
}