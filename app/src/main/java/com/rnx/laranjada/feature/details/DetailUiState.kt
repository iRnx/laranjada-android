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
        get() =
            contentType ==
                    DetailContentType.Series

    val contentTypeForApi: String
        get() =
            if (
                isSeries
            ) {
                "series"
            } else {
                "movie"
            }

    val hasWatchProgress: Boolean
        get() =
            watchProgress != null &&
                    watchProgress.progress > 0f

    /*
     * IMPORTANTE:
     *
     * A disponibilidade do episódio
     * agora é definida por has_video.
     *
     * Não usamos mais hls_url como
     * critério de reprodução.
     *
     * A URL oficial será recebida pelo:
     *
     * POST /api/v1/playback/reserve/
     */
    val firstAvailableEpisode:
            EpisodeUi?
        get() {
            seasons.forEach {
                    season ->

                val episode =
                    season.episodes
                        .firstOrNull {
                                item ->

                            item.hasVideo
                        }

                if (
                    episode != null
                ) {
                    return episode
                }
            }

            return null
        }

    /*
     * FILME:
     * has_video define se pode reproduzir.
     *
     * SÉRIE:
     * basta existir algum episódio
     * com has_video=true.
     */
    val canPlay: Boolean
        get() {
            return if (
                isSeries
            ) {
                firstAvailableEpisode !=
                        null
            } else {
                hasVideo
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
        get() =
            seasonNumber != null &&
                    episodeNumber != null &&
                    !episodeTitle
                        .isNullOrBlank()
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