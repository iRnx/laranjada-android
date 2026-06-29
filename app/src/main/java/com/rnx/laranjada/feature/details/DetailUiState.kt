package com.rnx.laranjada.feature.details

enum class DetailContentType {
    Movie,
    Series
}

data class DetailUiState(
    val uuid: String = "",
    val contentType: DetailContentType,
    val title: String,
    val originalTitle: String,
    val year: String,
    val endYear: String? = null,
    val rating: String,
    val genres: List<String>,
    val durationInfo: String,
    val synopsis: String,
    val imageDetailUrl: String,
    val imageThumbUrl: String,
    val hlsUrl: String = "",
    val hasVideo: Boolean = false,
    val watchProgress: WatchProgressUi? = null,
    val seasons: List<SeasonUi> = emptyList()
) {
    val isSeries: Boolean
        get() = contentType == DetailContentType.Series

    val hasWatchProgress: Boolean
        get() = watchProgress != null && watchProgress.progress > 0f

    val firstAvailableEpisode: EpisodeUi?
        get() {
            seasons.forEach { season ->
                val episode = season.episodes.firstOrNull { item ->
                    item.hlsUrl.isNotBlank()
                }

                if (episode != null) {
                    return episode
                }
            }

            return null
        }

    val canPlay: Boolean
        get() {
            return if (isSeries) {
                firstAvailableEpisode != null
            } else {
                hlsUrl.isNotBlank()
            }
        }
}

data class WatchProgressUi(
    val progress: Float,
    val remainingText: String,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null
) {
    val hasEpisodeInfo: Boolean
        get() = seasonNumber != null &&
                episodeNumber != null &&
                !episodeTitle.isNullOrBlank()
}

data class SeasonUi(
    val id: String,
    val number: Int,
    val episodes: List<EpisodeUi>
)

data class EpisodeUi(
    val id: String,
    val uuid: String,
    val number: Int,
    val title: String,
    val runtime: String,
    val rating: String,
    val synopsis: String,
    val imageUrl: String,
    val hasVideo: Boolean,
    val hlsUrl: String = ""
)