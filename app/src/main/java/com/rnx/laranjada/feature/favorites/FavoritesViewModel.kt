
package com.rnx.laranjada.feature.favorites

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.repository.FavoritesRepositoryImpl
import com.rnx.laranjada.domain.model.FavoritesQuery
import com.rnx.laranjada.domain.repository.FavoritesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoritesRepository =
        FavoritesRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf(
        FavoritesUiState()
    )
        private set

    var feedbackMessage by mutableStateOf<String?>(
        null
    )
        private set

    private var selectedProfileUuid = ""
    private var requestGeneration = 0
    private var requestJob: Job? = null
    private var loaded = false
    private var removing = false

    fun start(profileUuid: String) {
        if (profileUuid.isBlank()) {
            return
        }

        if (
            selectedProfileUuid == profileUuid &&
            (loaded || uiState.isLoading)
        ) {
            return
        }

        selectedProfileUuid = profileUuid
        loaded = false

        requestJob?.cancel()
        requestGeneration++

        uiState = FavoritesUiState()

        requestPage(
            FavoritesQuery()
        )
    }

    fun refreshAfterReturn() {
        if (
            loaded &&
            !uiState.isLoading &&
            selectedProfileUuid.isNotBlank()
        ) {
            requestPage(
                uiState.query
            )
        }
    }

    fun retry() {
        requestPage(
            uiState.query
        )
    }

    /*
     * ALFABETO
     *
     * Selecionar a letra atual
     * novamente remove o filtro.
     */
    fun selectLetter(letter: String) {
        val normalized =
            letter.uppercase()
                .takeIf {
                    it == "#" ||
                            (
                                    it.length == 1 &&
                                            it[0] in 'A'..'Z'
                                    )
                }
                .orEmpty()

        val next =
            if (
                normalized == uiState.query.letter
            ) {
                ""
            } else {
                normalized
            }

        requestPage(
            uiState.query.copy(
                letter = next,
                page = 1
            )
        )
    }

    fun applyOrder(order: String) {
        if (
            order !in setOf(
                "created_desc",
                "name_asc",
                "name_desc",
                "year_desc",
                "year_asc",
                "rating_desc"
            )
        ) {
            return
        }

        requestPage(
            uiState.query.copy(
                order = order,
                page = 1
            )
        )
    }

    /*
     * FILTROS COMBINADOS
     *
     * Preserva ordenação e letra.
     */
    fun applyFilters(
        contentType: String,
        category: String,
        search: String,
        year: String,
        ratingMin: String,
        genres: List<String>
    ) {
        val next =
            uiState.query.copy(
                contentType = contentType,
                category = category,
                search = search.trim(),
                year = year,
                ratingMin = ratingMin,
                genres = genres.distinct(),
                page = 1
            )

        requestPage(next)
    }

    fun clearFilters() {
        requestPage(
            FavoritesQuery()
        )
    }

    fun nextPage() {
        val page =
            uiState.nextPage
                ?: return

        if (!uiState.isLoading) {
            requestPage(
                uiState.query.copy(
                    page = page
                )
            )
        }
    }

    fun previousPage() {
        val page =
            uiState.previousPage
                ?: return

        if (!uiState.isLoading) {
            requestPage(
                uiState.query.copy(
                    page = page
                )
            )
        }
    }

    /*
     * LISTAGEM
     *
     * Cancela a requisição anterior
     * e protege contra resposta antiga.
     */
    private fun requestPage(
        query: FavoritesQuery
    ) {
        if (
            selectedProfileUuid.isBlank()
        ) {
            return
        }

        requestJob?.cancel()

        val generation =
            ++requestGeneration

        val expectedProfile =
            selectedProfileUuid

        uiState =
            uiState.copy(
                query = query,
                isLoading = true,
                error = null
            )

        requestJob =
            viewModelScope.launch {
                try {
                    val result =
                        repository.list(
                            query
                        )

                    if (
                        generation != requestGeneration ||
                        selectedProfileUuid != expectedProfile
                    ) {
                        return@launch
                    }

                    if (
                        result.profileUuid.isNotBlank() &&
                        result.profileUuid != expectedProfile
                    ) {
                        throw IllegalStateException(
                            "A API retornou favoritos de outro perfil."
                        )
                    }

                    loaded = true

                    uiState =
                        uiState.copy(
                            items = result.items,
                            totalCount = result.totalCount,
                            totalFavorites =
                                result.totalFavorites,
                            page = result.page,
                            pageSize = result.pageSize,
                            totalPages = result.totalPages,
                            nextPage = result.nextPage,
                            previousPage = result.previousPage,
                            availableYears =
                                result.availableYears,
                            availableGenres =
                                result.availableGenres,
                            isLoading = false,
                            error = null
                        )

                    Log.d(
                        TAG,
                        "Favoritos: pagina=${result.page} " +
                                "itens=${result.items.size}"
                    )

                } catch (
                    cancelled: CancellationException
                ) {
                    throw cancelled

                } catch (
                    exception: Exception
                ) {
                    if (
                        generation != requestGeneration ||
                        expectedProfile != selectedProfileUuid
                    ) {
                        return@launch
                    }

                    uiState =
                        uiState.copy(
                            items = emptyList(),
                            isLoading = false,
                            error =
                                exception.message
                                    ?: "Não foi possível carregar favoritos."
                        )

                    Log.w(
                        TAG,
                        "Erro ao carregar favoritos",
                        exception
                    )
                }
            }
    }

    /*
     * DESFAVORITAR
     *
     * A API é um toggle; por isso
     * impedimos requisições duplicadas.
     */
    fun removeFavorite(
        contentType: String,
        contentUuid: String
    ) {
        val key =
            "$contentType:$contentUuid"

        if (
            removing ||
            uiState.isLoading ||
            uiState.items.none {
                it.stableKey == key
            }
        ) {
            return
        }

        removing = true

        val expectedProfile =
            selectedProfileUuid

        val initialGeneration =
            requestGeneration

        uiState =
            uiState.copy(
                removingContentKey = key
            )

        viewModelScope.launch {
            try {
                val response =
                    repository.toggle(
                        contentType,
                        contentUuid
                    )

                if (
                    selectedProfileUuid != expectedProfile
                ) {
                    return@launch
                }

                if (!response.isFavorite) {
                    if (
                        initialGeneration != requestGeneration
                    ) {
                        requestPage(
                            uiState.query
                        )

                    } else {
                        val remaining =
                            uiState.items.filterNot {
                                it.stableKey == key
                            }

                        uiState =
                            uiState.copy(
                                items = remaining,

                                totalCount =
                                    (uiState.totalCount - 1)
                                        .coerceAtLeast(0),

                                totalFavorites =
                                    (uiState.totalFavorites - 1)
                                        .coerceAtLeast(0)
                            )

                        if (
                            remaining.isEmpty() &&
                            uiState.page > 1
                        ) {
                            requestPage(
                                uiState.query.copy(
                                    page = uiState.page - 1
                                )
                            )
                        }
                    }

                    feedbackMessage =
                        "Removido dos favoritos."

                } else {
                    feedbackMessage =
                        "O conteúdo continua nos favoritos."
                }

            } catch (
                cancelled: CancellationException
            ) {
                throw cancelled

            } catch (
                exception: Exception
            ) {
                if (
                    selectedProfileUuid == expectedProfile
                ) {
                    feedbackMessage =
                        exception.message
                            ?: "Não foi possível remover o favorito."
                }

                Log.w(
                    TAG,
                    "Erro no toggle de Favoritos",
                    exception
                )

            } finally {
                removing = false

                if (
                    selectedProfileUuid == expectedProfile
                ) {
                    uiState =
                        uiState.copy(
                            removingContentKey = null
                        )
                }
            }
        }
    }

    fun consumeFeedback() {
        feedbackMessage = null
    }

    private companion object {
        const val TAG =
            "LaranjadaFavorites"
    }
}
