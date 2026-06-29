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

@Composable
fun DetailRoute(
    contentType: String,
    uuid: String,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onFavoriteClick: (Boolean) -> Unit = {},
    onEpisodeClick: (EpisodeUi) -> Unit = {},
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
                onBackClick = onBackClick,
                onPlayClick = onPlayClick,
                onRestartClick = onRestartClick,
                onFavoriteClick = onFavoriteClick,
                onEpisodeClick = onEpisodeClick
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