
package com.rnx.laranjada.feature.collections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CollectionDetailRoute(
    uuid: String,
    onBackClick: () -> Unit = {},
    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit = { _, _ -> }
) {
    val viewModel: CollectionDetailViewModel =
        viewModel(
            key = "collection-detail-$uuid",

            factory = object :
                ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    return CollectionDetailViewModel(
                        collectionUuid = uuid
                    ) as T
                }
            }
        )

    val lifecycleOwner =
        LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        viewModel,
        uuid
    ) {
        val observer =
            LifecycleEventObserver {
                    _,
                    event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME &&
                    !viewModel.isLoading
                ) {
                    viewModel.loadCollection()
                }
            }

        lifecycleOwner.lifecycle
            .addObserver(
                observer
            )

        onDispose {
            lifecycleOwner.lifecycle
                .removeObserver(
                    observer
                )
        }
    }

    CollectionDetailScreen(
        uiState =
            viewModel.uiState,

        isLoading =
            viewModel.isLoading,

        errorMessage =
            viewModel.errorMessage,

        onBackClick =
            onBackClick,

        onMediaClick =
            onMediaClick,

        onRetryClick = {
            viewModel.loadCollection()
        },

        onLetterClick =
            viewModel::selectLetter,

        onOrderChange =
            viewModel::onOrderChanged,

        onApplyFilters =
            viewModel::applyFilters,

        onClearFilters =
            viewModel::clearFilters
    )
}
