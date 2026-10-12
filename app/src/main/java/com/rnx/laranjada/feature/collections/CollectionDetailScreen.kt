package com.rnx.laranjada.feature.collections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.unit.Dp
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
    Color(
        0xFF070B0F
    )

private val CollectionMuted =
    Color(
        0xFFBFC4C8
    )

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

    var orderSheetOpen by
    remember {
        mutableStateOf(
            false
        )
    }

    var filtersSheetOpen by
    remember {
        mutableStateOf(
            false
        )
    }

    val selectedBottomIndex =
        remember {
            mutableIntStateOf(
                0
            )
        }

    LaunchedEffect(
        uiState.appliedFilters
    ) {
        listState.scrollToItem(
            0
        )
    }

    val orderOptions =
        uiState.availableFilters.orders
            .map {
                CatalogFilterOption(
                    label =
                        it.label,

                    value =
                        it.value
                )
            }

    val ratingOptions =
        uiState.availableFilters.ratings
            .map {
                CatalogFilterOption(
                    label =
                        it.label,

                    value =
                        it.value
                )
            }

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
                    label =
                        it.label,

                    value =
                        it.value
                )
            }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    CollectionBackground
                )
    ) {
        val availableWidth =
            maxWidth

        val isTablet =
            availableWidth >=
                    600.dp

        val isExpandedTablet =
            availableWidth >=
                    840.dp

        val gridColumnCount =
            when {
                isExpandedTablet ->
                    4

                isTablet ->
                    3

                else ->
                    2
            }

        val contentMaxWidth =
            when {
                isExpandedTablet ->
                    availableWidth

                isTablet ->
                    820.dp

                else ->
                    availableWidth
            }

        val horizontalPadding =
            when {
                isExpandedTablet ->
                    40.dp

                isTablet ->
                    28.dp

                else ->
                    14.dp
            }

        val gridHorizontalSpacing =
            when {
                isExpandedTablet ->
                    18.dp

                isTablet ->
                    16.dp

                else ->
                    12.dp
            }

        /*
         * Landscape já aprovado.
         *
         * Não estamos alterando a lógica
         * visual dele.
         */
        val heroHeight =
            when {
                isExpandedTablet ->
                    availableWidth *
                            (
                                    9f /
                                            16f
                                    )

                isTablet ->
                    330.dp

                else ->
                    260.dp
            }

        Box(
            modifier =
                Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state =
                    listState,

                modifier =
                    Modifier.fillMaxSize(),

                contentPadding =
                    PaddingValues(
                        bottom =
                            172.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        if (
                            isTablet
                        ) {
                            16.dp
                        } else {
                            13.dp
                        }
                    )
            ) {
                item {
                    CollectionHero(
                        title =
                            uiState.title,

                        imageUrl =
                            uiState.imageUrl,

                        totalItems =
                            uiState.totalItems,

                        totalMovies =
                            uiState.totalMovies,

                        totalSeries =
                            uiState.totalSeries,

                        heroHeight =
                            heroHeight,

                        isTablet =
                            isTablet,

                        isExpandedTablet =
                            isExpandedTablet,

                        onBackClick =
                            onBackClick,

                        onSearchClick = {
                            filtersSheetOpen =
                                true
                        }
                    )
                }

                item {
                    CenteredCollectionContent(
                        maxWidth =
                            contentMaxWidth
                    ) {
                        CollectionToolbar(
                            totalItems =
                                uiState.totalItems,

                            isTablet =
                                isTablet,

                            horizontalPadding =
                                horizontalPadding,

                            onSort = {
                                orderSheetOpen =
                                    true
                            },

                            onFilters = {
                                filtersSheetOpen =
                                    true
                            }
                        )
                    }
                }

                when {
                    isLoading -> {
                        item {
                            CenteredCollectionContent(
                                maxWidth =
                                    contentMaxWidth
                            ) {
                                CollectionCenterState {
                                    CircularProgressIndicator(
                                        color =
                                            LaranjadaOrange
                                    )
                                }
                            }
                        }
                    }

                    errorMessage !=
                            null -> {

                        item {
                            CenteredCollectionContent(
                                maxWidth =
                                    contentMaxWidth
                            ) {
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
                                            text =
                                                "Tentar novamente"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    uiState.isEmptyResult -> {
                        item {
                            CenteredCollectionContent(
                                maxWidth =
                                    contentMaxWidth
                            ) {
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
                                            text =
                                                "Limpar filtros"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        /*
                         * [ALTERAÇÃO]
                         *
                         * Acabaram:
                         *
                         * Filmes
                         *   expandir/recolher
                         *
                         * Séries
                         *   expandir/recolher
                         *
                         * Agora existe exatamente
                         * o mesmo conceito do Web:
                         *
                         * CONTEÚDOS DA COLEÇÃO
                         *
                         * [filme] [série] [anime]
                         * [dorama] [filme] [desenho]
                         */
                        item {
                            CenteredCollectionContent(
                                maxWidth =
                                    contentMaxWidth
                            ) {
                                CollectionContentHeader(
                                    totalItems =
                                        uiState.items.size,

                                    isTablet =
                                        isTablet,

                                    horizontalPadding =
                                        horizontalPadding
                                )
                            }
                        }

                        items(
                            items =
                                uiState.items
                                    .chunked(
                                        gridColumnCount
                                    )
                        ) {
                                rowItems ->

                            CenteredCollectionContent(
                                maxWidth =
                                    contentMaxWidth
                            ) {
                                CollectionCardsRow(
                                    items =
                                        rowItems,

                                    columnCount =
                                        gridColumnCount,

                                    horizontalPadding =
                                        horizontalPadding,

                                    horizontalSpacing =
                                        gridHorizontalSpacing,

                                    onMediaClick =
                                        onMediaClick
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier =
                    Modifier.align(
                        Alignment.BottomCenter
                    )
            ) {
                CatalogAlphabetBar(
                    selectedLetter =
                        uiState
                            .appliedFilters
                            .letter,

                    enabled =
                        !isLoading,

                    distributeAcrossWidth =
                        isExpandedTablet,

                    onLetterClick =
                        onLetterClick
                )

                HomeBottomBar(
                    selectedIndex =
                        selectedBottomIndex
                            .intValue,

                    onItemClick = {
                            index ->

                        selectedBottomIndex
                            .intValue =
                            index
                    }
                )
            }
        }
    }

    if (
        orderSheetOpen
    ) {
        CatalogOrderSheet(
            options =
                orderOptions,

            selectedValue =
                uiState
                    .appliedFilters
                    .order,

            onSelect = {
                    order ->

                onOrderChange(
                    order
                )

                orderSheetOpen =
                    false
            },

            onDismiss = {
                orderSheetOpen =
                    false
            }
        )
    }

    if (
        filtersSheetOpen
    ) {
        CatalogFiltersSheet(
            search =
                uiState.appliedFilters.q,

            year =
                uiState.appliedFilters.year,

            ratingMin =
                uiState
                    .appliedFilters
                    .ratingMin,

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

                filtersSheetOpen =
                    false
            },

            onClear = {
                onClearFilters()

                filtersSheetOpen =
                    false
            },

            onDismiss = {
                filtersSheetOpen =
                    false
            }
        )
    }
}

@Composable
private fun CenteredCollectionContent(
    maxWidth: Dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier =
            Modifier.fillMaxWidth(),

        contentAlignment =
            Alignment.TopCenter
    ) {
        Box(
            modifier =
                Modifier
                    .widthIn(
                        max =
                            maxWidth
                    )
                    .fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
private fun CollectionHero(
    title: String,
    imageUrl: String,
    totalItems: Int,
    totalMovies: Int,
    totalSeries: Int,
    heroHeight: Dp,
    isTablet: Boolean,
    isExpandedTablet: Boolean,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    heroHeight
                )
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
                if (
                    isExpandedTablet
                ) {
                    ContentScale.FillWidth
                } else {
                    ContentScale.Crop
                },

            alignment =
                if (
                    isExpandedTablet
                ) {
                    Alignment.TopCenter
                } else {
                    Alignment.Center
                },

            modifier =
                Modifier.matchParentSize()
        )

        if (
            isExpandedTablet
        ) {
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colorStops =
                                    arrayOf(
                                        0.00f to
                                                Color.Black.copy(
                                                    alpha =
                                                        0.98f
                                                ),

                                        0.28f to
                                                Color.Black.copy(
                                                    alpha =
                                                        0.82f
                                                ),

                                        0.62f to
                                                Color.Black.copy(
                                                    alpha =
                                                        0.38f
                                                ),

                                        1.00f to
                                                Color.Black.copy(
                                                    alpha =
                                                        0.10f
                                                )
                                    )
                            )
                        )
            )

            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops =
                                    arrayOf(
                                        0.00f to
                                                Color.Black.copy(
                                                    alpha =
                                                        0.22f
                                                ),

                                        0.42f to
                                                Color.Black.copy(
                                                    alpha =
                                                        0.18f
                                                ),

                                        1.00f to
                                                CollectionBackground
                                    )
                            )
                        )
            )
        } else {
            if (
                isTablet
            ) {
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    colorStops =
                                        arrayOf(
                                            0.00f to
                                                    Color.Black.copy(
                                                        alpha =
                                                            0.64f
                                                    ),

                                            0.34f to
                                                    Color.Black.copy(
                                                        alpha =
                                                            0.32f
                                                    ),

                                            0.68f to
                                                    Color.Transparent,

                                            1.00f to
                                                    Color.Transparent
                                        )
                                )
                            )
                )
            }

            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops =
                                    arrayOf(
                                        0.00f to
                                                Color.Black.copy(
                                                    alpha =
                                                        0.52f
                                                ),

                                        0.28f to
                                                Color.Black.copy(
                                                    alpha =
                                                        0.16f
                                                ),

                                        0.68f to
                                                Color.Black.copy(
                                                    alpha =
                                                        0.55f
                                                ),

                                        1.00f to
                                                CollectionBackground
                                    )
                            )
                        )
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                    )
                    .padding(
                        start =
                            if (
                                isTablet
                            ) {
                                20.dp
                            } else {
                                6.dp
                            },

                        end =
                            if (
                                isTablet
                            ) {
                                20.dp
                            } else {
                                7.dp
                            },

                        top =
                            8.dp
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
                        Icons.AutoMirrored
                            .Rounded
                            .ArrowBack,

                    contentDescription =
                        "Voltar",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            if (
                                isTablet
                            ) {
                                26.dp
                            } else {
                                24.dp
                            }
                        )
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
                    if (
                        isTablet
                    ) {
                        19.sp
                    } else {
                        17.sp
                    },

                fontWeight =
                    FontWeight.Bold,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,

                modifier =
                    Modifier.weight(
                        1f
                    )
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
                        Color.White,

                    modifier =
                        Modifier.size(
                            if (
                                isTablet
                            ) {
                                26.dp
                            } else {
                                24.dp
                            }
                        )
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.BottomStart
                    )
                    .widthIn(
                        max =
                            if (
                                isExpandedTablet
                            ) {
                                720.dp
                            } else if (
                                isTablet
                            ) {
                                600.dp
                            } else {
                                520.dp
                            }
                    )
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            if (
                                isExpandedTablet
                            ) {
                                40.dp
                            } else if (
                                isTablet
                            ) {
                                28.dp
                            } else {
                                18.dp
                            }
                    )
                    .padding(
                        bottom =
                            if (
                                isExpandedTablet
                            ) {
                                48.dp
                            } else if (
                                isTablet
                            ) {
                                28.dp
                            } else {
                                22.dp
                            }
                    )
        ) {
            if (
                isExpandedTablet
            ) {
                Text(
                    text =
                        "COLEÇÃO",

                    color =
                        LaranjadaOrange,

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    letterSpacing =
                        1.2.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
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
                    when {
                        isExpandedTablet ->
                            40.sp

                        isTablet ->
                            34.sp

                        else ->
                            30.sp
                    },

                lineHeight =
                    when {
                        isExpandedTablet ->
                            44.sp

                        isTablet ->
                            38.sp

                        else ->
                            34.sp
                    },

                fontWeight =
                    FontWeight.ExtraBold,

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis
            )

            if (
                totalItems >
                0
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            9.dp
                        )
                )

                if (
                    isExpandedTablet
                ) {
                    CollectionHeroMeta(
                        totalItems =
                            totalItems,

                        totalMovies =
                            totalMovies,

                        totalSeries =
                            totalSeries
                    )
                } else {
                    Text(
                        text =
                            "$totalItems conteúdos",

                        color =
                            CollectionMuted,

                        fontSize =
                            if (
                                isTablet
                            ) {
                                15.sp
                            } else {
                                14.sp
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionHeroMeta(
    totalItems: Int,
    totalMovies: Int,
    totalSeries: Int
) {
    Row(
        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        Text(
            text =
                "$totalItems " +
                        if (
                            totalItems ==
                            1
                        ) {
                            "conteúdo"
                        } else {
                            "conteúdos"
                        },

            color =
                Color.White.copy(
                    alpha =
                        0.84f
                ),

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Bold
        )

        if (
            totalMovies >
            0
        ) {
            Text(
                text =
                    "•",

                color =
                    Color.White.copy(
                        alpha =
                            0.58f
                    )
            )

            Text(
                text =
                    "$totalMovies " +
                            if (
                                totalMovies ==
                                1
                            ) {
                                "filme"
                            } else {
                                "filmes"
                            },

                color =
                    Color.White.copy(
                        alpha =
                            0.84f
                    ),

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }

        if (
            totalSeries >
            0
        ) {
            Text(
                text =
                    "•",

                color =
                    Color.White.copy(
                        alpha =
                            0.58f
                    )
            )

            Text(
                text =
                    "$totalSeries " +
                            if (
                                totalSeries ==
                                1
                            ) {
                                "série"
                            } else {
                                "séries"
                            },

                color =
                    Color.White.copy(
                        alpha =
                            0.84f
                    ),

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CollectionToolbar(
    totalItems: Int,
    isTablet: Boolean,
    horizontalPadding: Dp,
    onSort: () -> Unit,
    onFilters: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        horizontalPadding,

                    vertical =
                        if (
                            isTablet
                        ) {
                            6.dp
                        } else {
                            3.dp
                        }
                ),

        horizontalArrangement =
            Arrangement.Center,

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text =
                if (
                    totalItems ==
                    0
                ) {
                    "0 de 0"
                } else {
                    "1–$totalItems de $totalItems"
                },

            color =
                Color.White,

            fontSize =
                if (
                    isTablet
                ) {
                    13.sp
                } else {
                    12.sp
                },

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.size(
                    if (
                        isTablet
                    ) {
                        22.dp
                    } else {
                        17.dp
                    }
                )
        )

        Text(
            text =
                "A-Z",

            color =
                Color.White,

            fontSize =
                if (
                    isTablet
                ) {
                    15.sp
                } else {
                    14.sp
                },

            fontWeight =
                FontWeight.ExtraBold,

            modifier =
                Modifier
                    .clickable(
                        onClick =
                            onSort
                    )
                    .padding(
                        10.dp
                    )
        )

        IconButton(
            onClick =
                onFilters,

            modifier =
                Modifier.size(
                    if (
                        isTablet
                    ) {
                        42.dp
                    } else {
                        38.dp
                    }
                )
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.FilterAlt,

                contentDescription =
                    "Abrir filtros da coleção",

                tint =
                    Color.White,

                modifier =
                    Modifier.size(
                        if (
                            isTablet
                        ) {
                            23.dp
                        } else {
                            21.dp
                        }
                    )
            )
        }
    }
}

@Composable
private fun CollectionContentHeader(
    totalItems: Int,
    isTablet: Boolean,
    horizontalPadding: Dp
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        horizontalPadding
                )
                .padding(
                    top =
                        if (
                            isTablet
                        ) {
                            12.dp
                        } else {
                            8.dp
                        },

                    bottom =
                        if (
                            isTablet
                        ) {
                            12.dp
                        } else {
                            8.dp
                        }
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                "Conteúdos da coleção",

            color =
                Color.White,

            fontSize =
                if (
                    isTablet
                ) {
                    26.sp
                } else {
                    21.sp
                },

            fontWeight =
                FontWeight.ExtraBold,

            textAlign =
                TextAlign.Center
        )

        if (
            totalItems >
            0
        ) {
            Text(
                text =
                    "$totalItems " +
                            if (
                                totalItems ==
                                1
                            ) {
                                "resultado"
                            } else {
                                "resultados"
                            },

                color =
                    CollectionMuted,

                fontSize =
                    if (
                        isTablet
                    ) {
                        13.sp
                    } else {
                        12.sp
                    },

                modifier =
                    Modifier.padding(
                        top =
                            4.dp
                    )
            )
        }
    }
}

@Composable
private fun CollectionCardsRow(
    items: List<MediaItemUi>,
    columnCount: Int,
    horizontalPadding: Dp,
    horizontalSpacing: Dp,
    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        horizontalPadding
                ),

        horizontalArrangement =
            Arrangement.spacedBy(
                horizontalSpacing
            ),

        verticalAlignment =
            Alignment.Top
    ) {
        items.forEach {
                item ->

            CatalogMediaCard(
                item =
                    item,

                onClick = {
                    if (
                        item.uuid
                            .isNotBlank() &&
                        item.contentType
                            .isNotBlank()
                    ) {
                        onMediaClick(
                            item.contentType,
                            item.uuid
                        )
                    }
                },

                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }

        repeat(
            columnCount -
                    items.size
        ) {
            Spacer(
                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }
    }
}

@Composable
private fun CollectionCenterState(
    content: @Composable () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        24.dp,

                    vertical =
                        48.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {
        content()
    }
}