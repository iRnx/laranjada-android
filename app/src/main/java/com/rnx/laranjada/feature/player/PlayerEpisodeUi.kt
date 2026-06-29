package com.rnx.laranjada.feature.player

import com.rnx.laranjada.feature.details.DetailUiState

data class PlayerEpisodesUiState(
    val seriesUuid: String,
    val seriesTitle: String,
    val seasons: List<PlayerSeasonUi>
) {
    val hasEpisodes: Boolean
        get() = seasons.any { season ->
            season.episodes.isNotEmpty()
        }
}

data class PlayerSeasonUi(
    val id: String,
    val number: Int,
    val episodes: List<PlayerEpisodeUi>
)

data class PlayerEpisodeUi(
    val id: String,
    val uuid: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val runtime: String,
    val imageUrl: String,
    val hasVideo: Boolean,
    val hlsUrl: String
)

fun DetailUiState.toPlayerEpisodesUiState(): PlayerEpisodesUiState {
    return PlayerEpisodesUiState(
        seriesUuid = uuid,
        seriesTitle = title,
        seasons = seasons.map { season ->
            PlayerSeasonUi(
                id = season.id,
                number = season.number,
                episodes = season.episodes.map { episode ->
                    PlayerEpisodeUi(
                        id = episode.id,
                        uuid = episode.uuid,
                        seasonNumber = season.number,
                        episodeNumber = episode.number,
                        title = episode.title,
                        runtime = episode.runtime,
                        imageUrl = episode.imageUrl,
                        hasVideo = episode.hasVideo,
                        hlsUrl = episode.hlsUrl
                    )
                }
            )
        }
    )
}