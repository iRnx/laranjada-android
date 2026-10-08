
package com.rnx.laranjada.feature.collections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.feature.catalog.components.CatalogAlphabetBar
import com.rnx.laranjada.feature.catalog.components.CatalogFilterOption
import com.rnx.laranjada.feature.catalog.components.CatalogFiltersSheet
import com.rnx.laranjada.feature.catalog.components.CatalogMediaCard
import com.rnx.laranjada.feature.catalog.components.CatalogOrderSheet
import com.rnx.laranjada.feature.home.MediaItemUi
import com.rnx.laranjada.feature.home.components.HomeBottomBar

private val CollectionBackground =
    Color(0xFF070B0F)

private val CollectionMuted =
    Color(0xFFBFC4C8)

@Composable
fun CollectionDetailScreen(
    uiState: CollectionDetailUiState,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit = {},
    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit = { _, _ -> },
    onRetryClick: () -> Unit = {},
    onLetterClick: (String) -> Unit = {},
    onOrderChange: (String) -> Unit = {},
    onApplyFilters: (
        search: String,
        year: String,
        ratingMin: String,
        type: String
    ) -> Unit = { _, _, _, _ -> },
    onClearFilters: () -> Unit = {}
) {
    val listState =
        rememberLazyListState()

    var orderSheetOpen by remember {
        mutableStateOf(false)
    }

    var filtersSheetOpen by remember {
        mutableStateOf(false)
    }

    var moviesExpanded by remember {
        mutableStateOf(true)
    }

    var seriesExpanded by remember {
        mutableStateOf(true)
    }

    /*
     * Preserva o comportamento atual
     * da bottom bar de Coleções.
     *
     * A integração de navegação
     * pode ser tratada separadamente.
     */
    val selectedBottomIndex =
        remember {
            mutableIntStateOf(0)
        }

    LaunchedEffect(
        uiState.appliedFilters
    ) {
        listState.scrollToItem(0)
    }

    val orderOptions =
        uiState.availableFilters.orders.map {
            CatalogFilterOption(
                label = it.label,
                value = it.value
            )
        }

    val ratingOptions =
        uiState.availableFilters.ratings.map {
            CatalogFilterOption(
                label = it.label,
                value = it.value
            )
        }

    /*
     * Coleções têm opções gerais
     * e também tipos específicos.
     *
     * Não perdemos essas opções
     * ao adotar o painel compartilhado.
     */
    val typeOptions =
        (
                uiState.availableFilters.types +
                        uiState.availableFilters.specificTypes
                )
            .distinctBy {
                it.value
            }
            .map {
                CatalogFilterOption(
                    label = it.label,
                    value = it.value
                )
            }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                CollectionBackground
            )
    ) {
        LazyColumn(
            state = listState,

            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    bottom = 172.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(13.dp)
        ) {
            item {
                CollectionHero(
                    title =
                        uiState.title,

                    imageUrl =
                        uiState.imageUrl,

                    totalItems =
                        uiState.totalItems,

                    onBackClick =
                        onBackClick,

                    onSearchClick = {
                        filtersSheetOpen = true
                    }
                )
            }

            item {
                CollectionToolbar(
                    totalItems =
                        uiState.totalItems,

                    onSort = {
                        orderSheetOpen = true
                    },

                    onFilters = {
                        filtersSheetOpen = true
                    }
                )
            }

            when {
                isLoading -> {
                    item {
                        CollectionCenterState {
                            CircularProgressIndicator(
                                color =
                                    LaranjadaOrange
                            )
                        }
                    }
                }

                errorMessage != null -> {
                    item {
                        CollectionCenterState {
                            Text(
                                text =
                                    errorMessage,

                                color =
                                    Color.White,

                                textAlign =
                                    TextAlign.Center
                            )

                            TextButton(
                                onClick =
                                    onRetryClick
                            ) {
                                Text(
                                    "Tentar novamente"
                                )
                            }
                        }
                    }
                }

                uiState.isEmptyResult -> {
                    item {
                        CollectionCenterState {
                            Text(
                                text =
                                    "Nenhum conteúdo encontrado nesta coleção.",

                                color =
                                    Color.White,

                                fontWeight =
                                    FontWeight.Bold,

                                textAlign =
                                    TextAlign.Center
                            )

                            Text(
                                text =
                                    "Altere a letra ou os filtros para visualizar outros conteúdos.",

                                color =
                                    CollectionMuted,

                                fontSize =
                                    13.sp,

                                textAlign =
                                    TextAlign.Center
                            )

                            TextButton(
                                onClick =
                                    onClearFilters
                            ) {
                                Text(
                                    "Limpar filtros"
                                )
                            }
                        }
                    }
                }

                else -> {
                    /*
                     * FILMES
                     */
                    if (
                        uiState.movies.isNotEmpty()
                    ) {
                        item {
                            CollectionSectionHeader(
                                title = "Filmes",

                                count =
                                    uiState.totalMovies,

                                expanded =
                                    moviesExpanded,

                                onClick = {
                                    moviesExpanded =
                                        !moviesExpanded
                                }
                            )
                        }

                        if (moviesExpanded) {
                            items(
                                items =
                                    uiState.movies.chunked(2)
                            ) { rowItems ->

                                CollectionCardsRow(
                                    items = rowItems,
                                    onMediaClick =
                                        onMediaClick
                                )
                            }
                        }
                    }

                    /*
                     * SÉRIES
                     */
                    if (
                        uiState.series.isNotEmpty()
                    ) {
                        item {
                            CollectionSectionHeader(
                                title = "Séries",

                                count =
                                    uiState.totalSeries,

                                expanded =
                                    seriesExpanded,

                                onClick = {
                                    seriesExpanded =
                                        !seriesExpanded
                                }
                            )
                        }

                        if (seriesExpanded) {
                            items(
                                items =
                                    uiState.series.chunked(2)
                            ) { rowItems ->

                                CollectionCardsRow(
                                    items = rowItems,
                                    onMediaClick =
                                        onMediaClick
                                )
                            }
                        }
                    }
                }
            }
        }

        /*
         * ALFABETO E NAVEGAÇÃO
         *
         * A faixa fica acima da
         * bottom bar, como em Favoritos.
         */
        Column(
            modifier =
                Modifier.align(
                    Alignment.BottomCenter
                )
        ) {
            CatalogAlphabetBar(
                selectedLetter =
                    uiState.appliedFilters.letter,

                enabled =
                    !isLoading,

                onLetterClick =
                    onLetterClick
            )

            HomeBottomBar(
                selectedIndex =
                    selectedBottomIndex.intValue,

                onItemClick = { index ->
                    selectedBottomIndex.intValue =
                        index
                }
            )
        }
    }

    /*
     * PAINEL DE ORDENAÇÃO
     */
    if (orderSheetOpen) {
        CatalogOrderSheet(
            options =
                orderOptions,

            selectedValue =
                uiState.appliedFilters.order,

            onSelect = { order ->
                onOrderChange(order)
                orderSheetOpen = false
            },

            onDismiss = {
                orderSheetOpen = false
            }
        )
    }

    /*
     * PAINEL DE FILTROS
     *
     * Inclui os tipos específicos
     * disponibilizados pela API.
     */
    if (filtersSheetOpen) {
        CatalogFiltersSheet(
            search =
                uiState.appliedFilters.q,

            year =
                uiState.appliedFilters.year,

            ratingMin =
                uiState.appliedFilters.ratingMin,

            type =
                uiState.appliedFilters.type,

            years =
                uiState.availableFilters.years,

            ratings =
                ratingOptions,

            types =
                typeOptions,

            typeTitle =
                "Tipo de conteúdo",

            onApply = {
                    search,
                    year,
                    rating,
                    type ->

                onApplyFilters(
                    search,
                    year,
                    rating,
                    type
                )

                filtersSheetOpen = false
            },

            onClear = {
                onClearFilters()
                filtersSheetOpen = false
            },

            onDismiss = {
                filtersSheetOpen = false
            }
        )
    }
}

@Composable
private fun CollectionHero(
    title: String,
    imageUrl: String,
    totalItems: Int,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                CollectionBackground
            )
    ) {
        AsyncImage(
            model =
                imageUrl,

            contentDescription =
                title,

            contentScale =
                ContentScale.Crop,

            modifier =
                Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black.copy(
                                alpha = 0.52f
                            ),

                            0.28f to Color.Black.copy(
                                alpha = 0.16f
                            ),

                            0.68f to Color.Black.copy(
                                alpha = 0.55f
                            ),

                            1.00f to CollectionBackground
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .padding(
                    start = 6.dp,
                    end = 7.dp,
                    top = 8.dp
                ),

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
                    title.ifBlank {
                        "Coleção"
                    },

                color =
                    Color.White,

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold,

                maxLines =
                    1,

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
                        "Buscar na coleção",

                    tint =
                        Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .align(
                    Alignment.BottomStart
                )
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp
                )
                .padding(
                    bottom = 22.dp
                )
        ) {
            Text(
                text =
                    title.ifBlank {
                        "Coleção"
                    },

                color =
                    Color.White,

                fontSize =
                    30.sp,

                lineHeight =
                    34.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis
            )

            if (totalItems > 0) {
                Text(
                    text =
                        "$totalItems conteúdos",

                    color =
                        CollectionMuted,

                    fontSize =
                        14.sp,

                    modifier =
                        Modifier.padding(
                            top = 6.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun CollectionToolbar(
    totalItems: Int,
    onSort: () -> Unit,
    onFilters: () -> Unit
) {
    /*
     * Coleções não são paginadas.
     * Por isso não exibimos setas
     * que não teriam ação real.
     *
     * Mantemos o mesmo A-Z e funil.
     */
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 14.dp,
                vertical = 3.dp
            ),

        horizontalArrangement =
            Arrangement.Center,

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text =
                if (totalItems == 0) {
                    "0 de 0"
                } else {
                    "1–$totalItems de $totalItems"
                },

            color =
                Color.White,

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.size(17.dp)
        )

        Text(
            text = "A-Z",

            color = Color.White,

            fontSize = 14.sp,

            fontWeight =
                FontWeight.ExtraBold,

            modifier = Modifier
                .clickable(
                    onClick = onSort
                )
                .padding(10.dp)
        )

        IconButton(
            onClick =
                onFilters,

            modifier =
                Modifier.size(38.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.FilterAlt,

                contentDescription =
                    "Abrir filtros da coleção",

                tint =
                    Color.White,

                modifier =
                    Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun CollectionSectionHeader(
    title: String,
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(
                start = 15.dp,
                end = 15.dp,
                top = 12.dp,
                bottom = 5.dp
            ),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text =
                    title,

                color =
                    Color.White,

                fontSize =
                    19.sp,

                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    "$count conteúdos",

                color =
                    CollectionMuted,

                fontSize =
                    12.sp,

                modifier =
                    Modifier.padding(
                        top = 2.dp
                    )
            )
        }

        Icon(
            imageVector =
                if (expanded) {
                    Icons.Rounded.ExpandLess
                } else {
                    Icons.Rounded.ExpandMore
                },

            contentDescription =
                if (expanded) {
                    "Recolher $title"
                } else {
                    "Expandir $title"
                },

            tint =
                Color.White,

            modifier =
                Modifier.size(27.dp)
        )
    }
}

@Composable
private fun CollectionCardsRow(
    items: List<MediaItemUi>,
    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 14.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(12.dp),

        verticalAlignment =
            Alignment.Top
    ) {
        items.forEach { item ->
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
                },

                modifier =
                    Modifier.weight(1f)
            )
        }

        if (items.size == 1) {
            Spacer(
                modifier =
                    Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CollectionCenterState(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 48.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}
