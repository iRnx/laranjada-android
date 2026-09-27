package com.rnx.laranjada.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaSurface
import com.rnx.laranjada.core.design.theme.LaranjadaSurfaceLight
import com.rnx.laranjada.core.design.theme.LaranjadaText

@Composable
fun PlayerEpisodesPanel(
    uiState: PlayerEpisodesUiState?,
    currentEpisodeUuid: String,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onRetryClick: () -> Unit,
    onEpisodeClick: (
        PlayerEpisodeUi
    ) -> Unit
) {
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .clickable(
                    onClick =
                        onDismiss
                ),

        contentAlignment =
            Alignment.CenterEnd
    ) {
        val panelWidth =
            if (
                maxWidth > 760.dp
            ) {
                430.dp
            } else {
                (
                        maxWidth *
                                0.82f
                        )
                    .coerceAtLeast(
                        320.dp
                    )
            }

        Column(
            modifier =
                Modifier
                    .padding(
                        14.dp
                    )
                    .width(
                        panelWidth
                    )
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            22.dp
                        )
                    )
                    .background(
                        LaranjadaSurface.copy(
                            alpha = 0.97f
                        )
                    )
                    .clickable {
                    }
        ) {
            PlayerEpisodesHeader(
                title =
                    uiState?.seriesTitle
                        ?: "Episódios",

                onDismiss =
                    onDismiss
            )

            when {
                isLoading -> {
                    PlayerEpisodesLoading()
                }

                errorMessage != null -> {
                    PlayerEpisodesError(
                        message =
                            errorMessage,

                        onRetryClick =
                            onRetryClick
                    )
                }

                uiState == null ||
                        !uiState.hasEpisodes -> {
                    PlayerEpisodesEmpty()
                }

                else -> {
                    LazyColumn(
                        modifier =
                            Modifier.fillMaxSize(),

                        contentPadding =
                            PaddingValues(
                                bottom = 20.dp
                            )
                    ) {
                        uiState.seasons
                            .forEach {
                                    season ->

                                item(
                                    key =
                                        "season_${season.id}"
                                ) {
                                    SeasonTitle(
                                        seasonNumber =
                                            season.number
                                    )
                                }

                                items(
                                    items =
                                        season.episodes,

                                    key = {
                                            episode ->

                                        episode.uuid
                                    }
                                ) {
                                        episode ->

                                    PlayerEpisodeCard(
                                        episode =
                                            episode,

                                        isCurrent =
                                            episode.uuid ==
                                                    currentEpisodeUuid,

                                        onClick = {
                                            /*
                                             * Não depende mais
                                             * de hlsUrl.
                                             */
                                            if (
                                                episode.hasVideo
                                            ) {
                                                onEpisodeClick(
                                                    episode
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerEpisodesHeader(
    title: String,
    onDismiss: () -> Unit
) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        58.dp
                    )
                    .padding(
                        start = 18.dp,
                        end = 8.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Text(
                    text =
                        "Episódios",

                    color =
                        LaranjadaText,

                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        title,

                    color =
                        LaranjadaMutedText,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick =
                    onDismiss,

                modifier =
                    Modifier.size(
                        42.dp
                    )
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Close,

                    contentDescription =
                        "Fechar episódios",

                    tint =
                        Color.White
                )
            }
        }

        HorizontalDivider(
            color =
                Color.White.copy(
                    alpha = 0.10f
                ),

            thickness =
                1.dp
        )
    }
}

@Composable
private fun PlayerEpisodesLoading() {
    Box(
        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center
    ) {
        CircularProgressIndicator(
            color =
                LaranjadaOrange
        )
    }
}

@Composable
private fun PlayerEpisodesError(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    22.dp
                ),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                "Não foi possível carregar os episódios.",

            color =
                LaranjadaText,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

        Text(
            text =
                message,

            color =
                LaranjadaMutedText
        )

        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )

        Button(
            onClick =
                onRetryClick,

            colors =
                ButtonDefaults
                    .buttonColors(
                        containerColor =
                            LaranjadaOrange,

                        contentColor =
                            Color.White
                    )
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Refresh,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        18.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            Text(
                text =
                    "Tentar novamente"
            )
        }
    }
}

@Composable
private fun PlayerEpisodesEmpty() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    22.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                "Nenhum episódio encontrado.",

            color =
                LaranjadaMutedText
        )
    }
}

@Composable
private fun SeasonTitle(
    seasonNumber: Int
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp
                )
                .padding(
                    top = 18.dp,
                    bottom = 10.dp
                )
    ) {
        Text(
            text =
                "Temporada $seasonNumber",

            color =
                LaranjadaText,

            fontWeight =
                FontWeight.ExtraBold
        )

        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

        HorizontalDivider(
            color =
                Color.White.copy(
                    alpha = 0.08f
                ),

            thickness =
                1.dp
        )
    }
}

@Composable
private fun PlayerEpisodeCard(
    episode: PlayerEpisodeUi,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    /*
     * Agora somente has_video define
     * se o episódio está disponível.
     */
    val hasVideo =
        episode.hasVideo

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 7.dp
                )
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .background(
                    if (
                        isCurrent
                    ) {
                        LaranjadaOrange.copy(
                            alpha = 0.16f
                        )
                    } else {
                        Color.Transparent
                    }
                )
                .then(
                    if (
                        isCurrent
                    ) {
                        Modifier.border(
                            width =
                                1.dp,

                            color =
                                LaranjadaOrange.copy(
                                    alpha = 0.75f
                                ),

                            shape =
                                RoundedCornerShape(
                                    14.dp
                                )
                        )
                    } else {
                        Modifier
                    }
                )
                .clickable(
                    enabled =
                        hasVideo,

                    onClick =
                        onClick
                )
                .padding(
                    8.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .width(
                        124.dp
                    )
                    .aspectRatio(
                        16f / 9f
                    )
                    .clip(
                        RoundedCornerShape(
                            10.dp
                        )
                    )
                    .background(
                        LaranjadaSurfaceLight
                    )
        ) {
            AsyncImage(
                model =
                    episode.imageUrl,

                contentDescription =
                    episode.title,

                modifier =
                    Modifier.fillMaxSize(),

                contentScale =
                    ContentScale.Crop
            )

            Surface(
                modifier =
                    Modifier
                        .size(
                            34.dp
                        )
                        .align(
                            Alignment.Center
                        ),

                shape =
                    CircleShape,

                color =
                    if (
                        hasVideo
                    ) {
                        Color.White.copy(
                            alpha = 0.95f
                        )
                    } else {
                        Color.White.copy(
                            alpha = 0.35f
                        )
                    }
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.PlayArrow,

                    contentDescription =
                        null,

                    tint =
                        Color.Black,

                    modifier =
                        Modifier.padding(
                            7.dp
                        )
                )
            }
        }

        Spacer(
            modifier =
                Modifier.width(
                    12.dp
                )
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {
            Text(
                text =
                    "EP ${episode.episodeNumber}",

                color =
                    if (
                        isCurrent
                    ) {
                        LaranjadaOrange
                    } else {
                        LaranjadaMutedText
                    },

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        3.dp
                    )
            )

            Text(
                text =
                    episode.title,

                color =
                    if (
                        hasVideo
                    ) {
                        LaranjadaText
                    } else {
                        LaranjadaMutedText
                    },

                fontWeight =
                    FontWeight.Bold,

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis
            )

            if (
                episode.runtime
                    .isNotBlank()
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            5.dp
                        )
                )

                Text(
                    text =
                        episode.runtime,

                    color =
                        LaranjadaMutedText,

                    maxLines =
                        1
                )
            }
        }
    }
}