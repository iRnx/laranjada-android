package com.rnx.laranjada.feature.details

import android.util.Log

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.LaranjadaRepositoryImpl
import com.rnx.laranjada.data.repository.WatchingProgressRepositoryImpl
import com.rnx.laranjada.domain.repository.LaranjadaRepository
import com.rnx.laranjada.domain.repository.WatchingProgressRepository
import com.rnx.laranjada.feature.home.ContinueWatchingUi
import com.rnx.laranjada.feature.home.MediaItemUi
import kotlinx.coroutines.launch

private const val TAG = "LaranjadaDetail"

class DetailViewModel(
    private val repository: LaranjadaRepository =
        LaranjadaRepositoryImpl(),

    private val watchingProgressRepository: WatchingProgressRepository =
        WatchingProgressRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf<DetailUiState?>(null)
        private set

    var continueWatchingItem by mutableStateOf<ContinueWatchingUi?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var relatedItems by mutableStateOf<List<MediaItemUi>>(emptyList())
        private set

    var isRelatedLoading by mutableStateOf(false)
        private set

    var relatedErrorMessage by mutableStateOf<String?>(null)
        private set

    var isRestarting by mutableStateOf(false)
        private set

    private var currentKey: String? =
        null

    private var progressRequestVersion: Long =
        0L

    fun loadDetail(
        contentType: String,
        uuid: String
    ) {
        val normalizedContentType =
            contentType
                .trim()
                .lowercase()

        val normalizedUuid =
            uuid.trim()

        val nextKey =
            "$normalizedContentType:$normalizedUuid"

        if (
            currentKey == nextKey &&
            uiState != null
        ) {
            refreshContinueWatching()
            return
        }

        currentKey =
            nextKey

        progressRequestVersion++

        viewModelScope.launch {
            isLoading =
                true

            errorMessage =
                null

            uiState =
                null

            continueWatchingItem =
                null

            relatedItems =
                emptyList()

            relatedErrorMessage =
                null

            try {
                val detail =
                    repository.getDetail(
                        contentType =
                            normalizedContentType,

                        uuid =
                            normalizedUuid
                    )

                if (
                    currentKey !=
                    nextKey
                ) {
                    return@launch
                }

                uiState =
                    detail

                refreshContinueWatching()

                loadRelatedContent(
                    contentType =
                        detail.contentTypeForApi,

                    uuid =
                        detail.uuid
                )
            } catch (
                exception: Exception
            ) {
                if (
                    currentKey ==
                    nextKey
                ) {
                    errorMessage =
                        exception.message
                            ?: "Não foi possível carregar o detalhe."
                }
            } finally {
                if (
                    currentKey ==
                    nextKey
                ) {
                    isLoading =
                        false
                }
            }
        }
    }

    fun refreshAccessMetadata() {
        val current =
            uiState
                ?: return

        val expectedKey =
            currentKey
                ?: return

        viewModelScope.launch {
            try {
                val detail =
                    repository.getDetail(
                        contentType =
                            current.contentTypeForApi,

                        uuid =
                            current.uuid
                    )

                if (
                    currentKey !=
                    expectedKey
                ) {
                    return@launch
                }

                uiState =
                    detail.copy(
                        watchProgress =
                            uiState?.watchProgress
                    )

                loadRelatedContent(
                    contentType =
                        detail.contentTypeForApi,

                    uuid =
                        detail.uuid
                )
            } catch (
                exception:
                Exception
            ) {
                if (
                    currentKey ==
                    expectedKey
                ) {
                    Log.w(
                        TAG,
                        "Falha ao atualizar acesso da Detail: ${exception.message}"
                    )
                }
            }
        }
    }

    fun refreshContinueWatching() {
        val detail =
            uiState
                ?: return

        val requestKey =
            currentKey

        val requestVersion =
            ++progressRequestVersion

        viewModelScope.launch {
            try {
                val items =
                    repository
                        .getContinueWatching()

                if (
                    requestKey != currentKey ||
                    requestVersion != progressRequestVersion
                ) {
                    return@launch
                }

                val currentDetail =
                    uiState
                        ?: return@launch

                val progressItem =
                    findContinueWatchingItem(
                        detail =
                            currentDetail,

                        items =
                            items
                    )

                continueWatchingItem =
                    progressItem

                uiState =
                    currentDetail.copy(
                        watchProgress =
                            progressItem
                                ?.toWatchProgress(
                                    detail =
                                        currentDetail
                                )
                    )
            } catch (
                _: Exception
            ) {
                /*
                 * O Continue Assistindo é complementar.
                 *
                 * Uma falha ao consultar progresso não
                 * deve impedir o Detail de abrir.
                 */
            }
        }
    }

    fun restartContinueWatching(
        onSuccess: (
            item: ContinueWatchingUi
        ) -> Unit
    ) {
        val item =
            continueWatchingItem
                ?: return

        if (
            isRestarting
        ) {
            return
        }

        val contentType =
            item.contentType
                .trim()
                .lowercase()

        val contentUuid =
            item.contentUuid
                .trim()

        if (
            contentType !in setOf(
                "movie",
                "episode"
            ) ||
            contentUuid.isBlank()
        ) {
            return
        }

        isRestarting =
            true

        progressRequestVersion++

        viewModelScope.launch {
            try {
                watchingProgressRepository
                    .resetProgress(
                        contentType =
                            contentType,

                        contentUuid =
                            contentUuid
                    )

                continueWatchingItem =
                    null

                uiState =
                    uiState?.copy(
                        watchProgress =
                            null
                    )

                onSuccess(
                    item
                )
            } catch (
                _: Exception
            ) {
                /*
                 * Mantemos o progresso atual se
                 * o reset não puder ser concluído.
                 */
            } finally {
                isRestarting =
                    false
            }
        }
    }

    fun reloadRelatedContent() {
        val detail =
            uiState
                ?: return

        loadRelatedContent(
            contentType =
                detail.contentTypeForApi,

            uuid =
                detail.uuid
        )
    }

    private fun loadRelatedContent(
        contentType: String,
        uuid: String
    ) {
        val requestKey =
            currentKey

        viewModelScope.launch {
            isRelatedLoading =
                true

            relatedErrorMessage =
                null

            try {
                val results =
                    repository.getRelatedContent(
                        contentType =
                            contentType,

                        uuid =
                            uuid,

                        limit =
                            20
                    )

                if (
                    requestKey ==
                    currentKey
                ) {
                    relatedItems =
                        results
                }
            } catch (
                exception: Exception
            ) {
                if (
                    requestKey ==
                    currentKey
                ) {
                    relatedItems =
                        emptyList()

                    relatedErrorMessage =
                        exception.message
                            ?: "Não foi possível carregar sugestões."
                }
            } finally {
                if (
                    requestKey ==
                    currentKey
                ) {
                    isRelatedLoading =
                        false
                }
            }
        }
    }

    private fun findContinueWatchingItem(
        detail: DetailUiState,
        items: List<ContinueWatchingUi>
    ): ContinueWatchingUi? {

        if (
            detail.isSeries
        ) {
            return items.firstOrNull {
                    item ->

                val belongsToSeries =
                    item.seriesUuid
                        ?.equals(
                            detail.uuid,
                            ignoreCase = true
                        ) ==
                            true

                val isEpisode =
                    item.contentType
                        .trim()
                        .equals(
                            "episode",
                            ignoreCase = true
                        )

                val episodeExists =
                    detail.seasons
                        .any {
                                season ->

                            season.episodes
                                .any {
                                        episode ->

                                    episode.uuid
                                        .equals(
                                            item.contentUuid,
                                            ignoreCase = true
                                        ) &&
                                            episode.hasVideo
                                }
                        }

                isEpisode &&
                        belongsToSeries &&
                        episodeExists
            }
        }

        return items.firstOrNull {
                item ->

            item.contentType
                .trim()
                .equals(
                    "movie",
                    ignoreCase = true
                ) &&
                    item.contentUuid
                        .trim()
                        .equals(
                            detail.uuid,
                            ignoreCase = true
                        )
        }
    }

    private fun ContinueWatchingUi.toWatchProgress(
        detail: DetailUiState
    ): WatchProgressUi {

        val matchedEpisode =
            if (
                detail.isSeries
            ) {
                detail.seasons
                    .asSequence()
                    .flatMap {
                            season ->

                        season.episodes
                            .asSequence()
                    }
                    .firstOrNull {
                            episode ->

                        episode.uuid
                            .equals(
                                contentUuid,
                                ignoreCase = true
                            )
                    }
            } else {
                null
            }

        val fallbackEpisodeTitle =
            episodeInfo
                .substringAfter(
                    delimiter = " ",
                    missingDelimiterValue = ""
                )
                .trim()
                .ifBlank {
                    null
                }

        return WatchProgressUi(
            progress =
                progress.coerceIn(
                    0f,
                    1f
                ),

            remainingText =
                remainingTime,

            seasonNumber =
                seasonNumber,

            episodeNumber =
                episodeNumber,

            episodeTitle =
                matchedEpisode
                    ?.title
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: fallbackEpisodeTitle
        )
    }
}