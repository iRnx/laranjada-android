
package com.rnx.laranjada.feature.mediagrid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.feature.catalog.components.CatalogAlphabetBar
import com.rnx.laranjada.feature.catalog.components.CatalogFilterOption
import com.rnx.laranjada.feature.catalog.components.CatalogFilterToolbar
import com.rnx.laranjada.feature.catalog.components.CatalogFiltersSheet
import com.rnx.laranjada.feature.catalog.components.CatalogMediaCard
import com.rnx.laranjada.feature.catalog.components.CatalogOrderSheet

private val MediaGridBackground =
    Color(0xFF070B0F)

private val MediaGridMuted =
    Color(0xFFBFC4C8)

@Composable
fun MediaGridScreen(
    sectionSlug: String,
    title: String,
    onBackClick: () -> Unit = {},
    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit = { _, _ -> },
    viewModel: MediaGridViewModel = viewModel()
) {
    val state =
        viewModel.uiState

    val isLoading =
        viewModel.isLoading

    val error =
        viewModel.errorMessage

    val gridState =
        rememberLazyGridState()

    var orderSheetOpen by remember {
        mutableStateOf(false)
    }

    var filtersSheetOpen by remember {
        mutableStateOf(false)
    }

    /*
     * Conserva o tamanho aprovado:
     *
     * Celulares: 2 colunas.
     * Tablets médios: 3.
     * Tablets maiores: 4.
     */
    val screenWidth =
        LocalConfiguration.current.screenWidthDp

    val columnCount =
        when {
            screenWidth >= 840 -> 4
            screenWidth >= 600 -> 3
            else -> 2
        }

    LaunchedEffect(
        sectionSlug,
        title
    ) {
        viewModel.start(
            sectionSlug = sectionSlug,
            title = title
        )
    }

    /*
     * Volta ao topo quando mudam
     * página ou filtros aplicados.
     */
    LaunchedEffect(
        state.currentPage,
        state.appliedFilters
    ) {
        gridState.scrollToItem(0)
    }

    val rangeLabel =
        if (
            state.totalCount <= 0
        ) {
            "0 de 0"
        } else {
            val first =
                (state.currentPage - 1) *
                        state.pageSize + 1

            val last =
                minOf(
                    state.currentPage *
                            state.pageSize,

                    state.totalCount
                )

            "$first–$last de ${state.totalCount}"
        }

    val orderOptions =
        state.availableFilters.orders.map {
            CatalogFilterOption(
                label = it.label,
                value = it.value
            )
        }

    val ratingOptions =
        state.availableFilters.ratings.map {
            CatalogFilterOption(
                label = it.label,
                value = it.value
            )
        }

    val kindOptions =
        state.availableFilters.kinds.map {
            CatalogFilterOption(
                label = it.label,
                value = it.value
            )
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MediaGridBackground
            )
    ) {
        LazyVerticalGrid(
            columns =
                GridCells.Fixed(
                    columnCount
                ),

            state =
                gridState,

            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 8.dp,
                    bottom = 105.dp
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    22.dp
                )
        ) {
            item(
                span = {
                    GridItemSpan(
                        maxLineSpan
                    )
                }
            ) {
                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    MediaGridHeader(
                        title =
                            state.title.ifBlank {
                                title.ifBlank {
                                    "Ver tudo"
                                }
                            },

                        onBackClick =
                            onBackClick,

                        onSearchClick = {
                            filtersSheetOpen = true
                        }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(13.dp)
                    )

                    CatalogFilterToolbar(
                        rangeLabel =
                            rangeLabel,

                        canPrevious =
                            !isLoading &&
                                    state.hasPreviousPage,

                        canNext =
                            !isLoading &&
                                    state.hasNextPage,

                        onPrevious =
                            viewModel::previousPage,

                        onNext =
                            viewModel::nextPage,

                        onSort = {
                            orderSheetOpen = true
                        },

                        onFilters = {
                            filtersSheetOpen = true
                        }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(15.dp)
                    )
                }
            }

            when {
                isLoading -> {
                    item(
                        span = {
                            GridItemSpan(
                                maxLineSpan
                            )
                        }
                    ) {
                        GridCenterState {
                            CircularProgressIndicator(
                                color =
                                    LaranjadaOrange
                            )
                        }
                    }
                }

                error != null -> {
                    item(
                        span = {
                            GridItemSpan(
                                maxLineSpan
                            )
                        }
                    ) {
                        GridCenterState {
                            Text(
                                text = error,

                                color =
                                    Color.White,

                                textAlign =
                                    TextAlign.Center
                            )

                            TextButton(
                                onClick =
                                    viewModel::reload
                            ) {
                                Text(
                                    "Tentar novamente"
                                )
                            }
                        }
                    }
                }

                state.items.isEmpty() -> {
                    item(
                        span = {
                            GridItemSpan(
                                maxLineSpan
                            )
                        }
                    ) {
                        GridCenterState {
                            Text(
                                text =
                                    "Nenhum conteúdo encontrado.",

                                color =
                                    Color.White,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            Text(
                                text =
                                    "Altere a letra ou os filtros para encontrar outros conteúdos.",

                                color =
                                    MediaGridMuted,

                                fontSize =
                                    13.sp,

                                textAlign =
                                    TextAlign.Center
                            )

                            TextButton(
                                onClick =
                                    viewModel::clearFilters
                            ) {
                                Text(
                                    "Limpar filtros"
                                )
                            }
                        }
                    }
                }

                else -> {
                    items(
                        items =
                            state.items,

                        key = { item ->
                            "${item.contentType}:${item.uuid}"
                        }
                    ) { item ->

                        CatalogMediaCard(
                            item = item,

                            onClick = {
                                if (
                                    item.uuid.isNotBlank() &&
                                    item.contentType.isNotBlank()
                                ) {
                                    onMediaClick(
                                        item.contentType,
                                        item.uuid
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        /*
         * Barra fixa inferior.
         *
         * Considera a área de
         * navegação do Android.
         */
        CatalogAlphabetBar(
            selectedLetter =
                state.appliedFilters.letter,

            enabled =
                !isLoading,

            onLetterClick =
                viewModel::selectLetter,

            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .windowInsetsPadding(
                    WindowInsets.navigationBars
                )
        )
    }

    if (orderSheetOpen) {
        CatalogOrderSheet(
            options =
                orderOptions,

            selectedValue =
                viewModel.draftFilters.order,

            onSelect = { selectedOrder ->
                viewModel.applyOrder(
                    selectedOrder
                )

                orderSheetOpen = false
            },

            onDismiss = {
                orderSheetOpen = false
            }
        )
    }

    if (filtersSheetOpen) {
        CatalogFiltersSheet(
            search =
                viewModel.draftFilters.q,

            year =
                viewModel.draftFilters.year,

            ratingMin =
                viewModel.draftFilters.ratingMin,

            type =
                viewModel.draftFilters.kind,

            years =
                state.availableFilters.years,

            ratings =
                ratingOptions,

            types =
                kindOptions,

            typeTitle =
                "Tipo de conteúdo",

            onApply = {
                    search,
                    year,
                    rating,
                    kind ->

                viewModel.applyFilters(
                    search = search,
                    year = year,
                    ratingMin = rating,
                    kind = kind
                )

                filtersSheetOpen = false
            },

            onClear = {
                viewModel.clearFilters()
                filtersSheetOpen = false
            },

            onDismiss = {
                filtersSheetOpen = false
            }
        )
    }
}

@Composable
private fun MediaGridHeader(
    title: String,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
            .padding(top = 8.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        IconButton(
            onClick =
                onBackClick
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored.Rounded.ArrowBack,

                contentDescription =
                    "Voltar",

                tint =
                    Color.White
            )
        }

        Text(
            text =
                title,

            color =
                Color.White,

            fontSize =
                21.sp,

            lineHeight =
                25.sp,

            fontWeight =
                FontWeight.Bold,

            textAlign =
                TextAlign.Center,

            maxLines =
                2,

            overflow =
                TextOverflow.Ellipsis,

            modifier =
                Modifier.weight(1f)
        )

        IconButton(
            onClick =
                onSearchClick
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Search,

                contentDescription =
                    "Pesquisar nesta categoria",

                tint =
                    Color.White
            )
        }
    }
}

@Composable
private fun GridCenterState(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 52.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}
