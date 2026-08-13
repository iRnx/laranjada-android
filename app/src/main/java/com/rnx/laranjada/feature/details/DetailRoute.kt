package com.rnx.laranjada.feature.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.feature.home.MediaItemUi

@Composable
fun DetailRoute(
    contentType: String,
    uuid: String,
    onBackClick: () -> Unit,
    onPlayClick: (
        contentType: String,
        uuid: String,
        hlsUrl: String,
        seriesUuid: String
    ) -> Unit = { _, _, _, _ -> },
    onRestartClick: () -> Unit = {},
    onFavoriteClick: (Boolean) -> Unit = {},
    onRelatedClick: (MediaItemUi) -> Unit = {},
    onEpisodeClick: (
        episode: EpisodeUi,
        seriesUuid: String
    ) -> Unit = { _, _ -> },
    viewModel: DetailViewModel = viewModel()
) {
    LaunchedEffect(contentType, uuid) {
        viewModel.loadDetail(
            contentType = contentType,
            uuid = uuid
        )
    }

    val uiState = viewModel.uiState
    val errorMessage = viewModel.errorMessage

    when {
        viewModel.isLoading -> {
            DetailLoading()
        }

        errorMessage != null -> {
            DetailError(
                message = errorMessage,
                onBackClick = onBackClick,
                onRetryClick = {
                    viewModel.loadDetail(
                        contentType = contentType,
                        uuid = uuid
                    )
                }
            )
        }

        uiState != null -> {
            DetailScreen(
                uiState = uiState,
                relatedItems = viewModel.relatedItems,
                isRelatedLoading = viewModel.isRelatedLoading,
                relatedErrorMessage = viewModel.relatedErrorMessage,
                onBackClick = onBackClick,
                onPlayClick = {
                    if (uiState.isSeries) {
                        val firstEpisode = uiState.firstAvailableEpisode

                        if (firstEpisode != null) {
                            onPlayClick(
                                "episode",
                                firstEpisode.uuid,
                                firstEpisode.hlsUrl,
                                uiState.uuid
                            )
                        }
                    } else {
                        onPlayClick(
                            "movie",
                            uiState.uuid,
                            uiState.hlsUrl,
                            ""
                        )
                    }
                },
                onRestartClick = onRestartClick,
                onFavoriteClick = onFavoriteClick,
                onRelatedClick = onRelatedClick,
                onRetryRelatedClick = viewModel::reloadRelatedContent,
                onEpisodeClick = { episode ->
                    onEpisodeClick(
                        episode,
                        uiState.uuid
                    )
                }
            )
        }
    }
}

@Composable
private fun DetailLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LaranjadaBlack),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = LaranjadaOrange
        )
    }
}

@Composable
private fun DetailError(
    message: String,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LaranjadaBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Não foi possível carregar o detalhe.",
            color = LaranjadaText
        )

        Text(
            text = message,
            color = LaranjadaText,
            modifier = Modifier.padding(top = 8.dp)
        )

        Button(
            onClick = onRetryClick,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(text = "Tentar novamente")
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text(text = "Voltar")
        }
    }
}