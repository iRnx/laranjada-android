package com.rnx.laranjada.feature.collections

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CollectionDetailRoute(
    uuid: String,
    onBackClick: () -> Unit = {},
    onMediaClick: (contentType: String, uuid: String) -> Unit = { _, _ -> }
) {
    val viewModel: CollectionDetailViewModel = viewModel(
        key = "collection-detail-$uuid",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CollectionDetailViewModel(
                    collectionUuid = uuid
                ) as T
            }
        }
    )

    CollectionDetailScreen(
        uiState = viewModel.uiState,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        onBackClick = onBackClick,
        onMediaClick = onMediaClick,
        onRetryClick = {
            viewModel.loadCollection()
        },
        onSearchChange = viewModel::onSearchChanged,
        onYearChange = viewModel::onYearChanged,
        onOrderChange = viewModel::onOrderChanged,
        onRatingMinChange = viewModel::onRatingMinChanged,
        onTypeChange = viewModel::onTypeChanged,
        onClearFilters = viewModel::clearFilters
    )
}