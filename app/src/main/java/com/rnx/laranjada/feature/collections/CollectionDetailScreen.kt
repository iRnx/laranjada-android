package com.rnx.laranjada.feature.collections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaSurface
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.feature.home.MediaItemUi
import com.rnx.laranjada.feature.home.components.HomeBottomBar

private val LaranjadaCollectionBackground = Color(0xFF070B0F)
private val LaranjadaCollectionPanel = Color(0xFF101417)
private val LaranjadaCollectionField = Color(0xFF070B0F)

private enum class CollectionSectionType {
    Movies,
    Series
}

@Composable
fun CollectionDetailScreen(
    uiState: CollectionDetailUiState,
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit = {},
    onMediaClick: (contentType: String, uuid: String) -> Unit = { _, _ -> },
    onRetryClick: () -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onYearChange: (String) -> Unit = {},
    onOrderChange: (String) -> Unit = {},
    onRatingMinChange: (String) -> Unit = {},
    onTypeChange: (String) -> Unit = {},
    onClearFilters: () -> Unit = {}
) {
    val selectedBottomIndex = remember { mutableIntStateOf(0) }
    var filtersExpanded by remember { mutableStateOf(false) }
    var moviesExpanded by remember { mutableStateOf(true) }
    var seriesExpanded by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LaranjadaCollectionBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CollectionHero(
                title = uiState.title,
                imageUrl = uiState.imageUrl,
                totalItems = uiState.totalItems,
                onBackClick = onBackClick
            )

            CollectionFiltersHeader(
                expanded = filtersExpanded,
                onClick = {
                    filtersExpanded = !filtersExpanded
                }
            )

            if (filtersExpanded) {
                CollectionFiltersPanel(
                    uiState = uiState,
                    onSearchChange = onSearchChange,
                    onYearChange = onYearChange,
                    onOrderChange = onOrderChange,
                    onRatingMinChange = onRatingMinChange,
                    onTypeChange = onTypeChange,
                    onClearFilters = onClearFilters
                )
            }

            when {
                isLoading && uiState.isEmptyResult -> {
                    CollectionLoadingContent()
                }

                errorMessage != null && uiState.isEmptyResult -> {
                    CollectionErrorContent(
                        message = errorMessage,
                        onRetryClick = onRetryClick
                    )
                }

                uiState.isEmptyResult -> {
                    CollectionEmptyContent()
                }

                else -> {
                    CollectionContent(
                        uiState = uiState,
                        isLoading = isLoading,
                        moviesExpanded = moviesExpanded,
                        seriesExpanded = seriesExpanded,
                        onToggleMovies = {
                            moviesExpanded = !moviesExpanded
                        },
                        onToggleSeries = {
                            seriesExpanded = !seriesExpanded
                        },
                        onMediaClick = onMediaClick
                    )
                }
            }
        }

        HomeBottomBar(
            selectedIndex = selectedBottomIndex.intValue,
            onItemClick = { index ->
                selectedBottomIndex.intValue = index
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CollectionHero(
    title: String,
    imageUrl: String,
    totalItems: Int,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(LaranjadaCollectionBackground)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black.copy(alpha = 0.52f),
                            0.28f to Color.Black.copy(alpha = 0.16f),
                            0.68f to Color.Black.copy(alpha = 0.55f),
                            1.00f to LaranjadaCollectionBackground
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 6.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Voltar",
                    tint = LaranjadaText,
                    modifier = Modifier.size(27.dp)
                )
            }

            Text(
                text = title.ifBlank { "Coleção" },
                color = LaranjadaText,
                fontSize = 18.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 22.dp)
        ) {
            Text(
                text = title.ifBlank { "Coleção" },
                color = LaranjadaText,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (totalItems > 0) {
                Text(
                    text = "$totalItems conteúdos",
                    color = LaranjadaMutedText,
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CollectionFiltersHeader(
    expanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = LaranjadaCollectionPanel.copy(alpha = 0.96f),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.04f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                tint = LaranjadaText,
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = "Filtros",
                color = LaranjadaText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Icon(
                imageVector = if (expanded) {
                    Icons.Rounded.ExpandLess
                } else {
                    Icons.Rounded.ExpandMore
                },
                contentDescription = null,
                tint = LaranjadaText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CollectionFiltersPanel(
    uiState: CollectionDetailUiState,
    onSearchChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onOrderChange: (String) -> Unit,
    onRatingMinChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    val filters = uiState.appliedFilters
    val available = uiState.availableFilters

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF090D11),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.04f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Buscar",
                color = LaranjadaMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = filters.q,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Digite título em PT-BR/EN",
                        color = LaranjadaMutedText,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = LaranjadaMutedText
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions.Default,
                colors = collectionTextFieldColors(),
                shape = RoundedCornerShape(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CollectionFilterDropdown(
                    label = "Ano",
                    selectedValue = filters.year,
                    options = buildList {
                        add(CollectionFilterOptionUi(label = "Todos", value = ""))
                        available.years.forEach { year ->
                            add(
                                CollectionFilterOptionUi(
                                    label = year.toString(),
                                    value = year.toString()
                                )
                            )
                        }
                    },
                    onSelected = onYearChange,
                    modifier = Modifier.weight(1f)
                )

                CollectionFilterDropdown(
                    label = "Ordenar",
                    selectedValue = filters.order,
                    options = available.orders,
                    onSelected = onOrderChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CollectionFilterDropdown(
                    label = "Nota mínima",
                    selectedValue = filters.ratingMin,
                    options = available.ratings,
                    onSelected = onRatingMinChange,
                    modifier = Modifier.weight(1f)
                )

                CollectionClearButton(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f)
                )
            }

            if (available.types.isNotEmpty()) {
                Text(
                    text = "Tipo",
                    color = LaranjadaMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                CollectionFilterChipsRow(
                    options = available.types,
                    selectedValue = filters.type,
                    onSelected = onTypeChange
                )
            }

            if (available.specificTypes.isNotEmpty()) {
                Text(
                    text = "Filtros específicos",
                    color = LaranjadaMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                CollectionFilterChipsRow(
                    options = available.specificTypes,
                    selectedValue = filters.type,
                    onSelected = onTypeChange
                )
            }
        }
    }
}

@Composable
private fun CollectionFilterDropdown(
    label: String,
    selectedValue: String,
    options: List<CollectionFilterOptionUi>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = options
        .firstOrNull { it.value == selectedValue }
        ?.label
        ?: options.firstOrNull()?.label
        ?: "Todos"

    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            color = LaranjadaMutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 6.dp)
        )

        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = true
                    },
                shape = RoundedCornerShape(13.dp),
                color = LaranjadaCollectionField,
                border = BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedLabel,
                        color = LaranjadaText,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = LaranjadaMutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                modifier = Modifier.background(LaranjadaCollectionPanel)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                color = LaranjadaText,
                                fontSize = 13.sp
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelected(option.value)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionClearButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "",
            color = Color.Transparent,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(13.dp),
            color = LaranjadaCollectionField,
            border = BorderStroke(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = LaranjadaText,
                    modifier = Modifier.size(17.dp)
                )

                Text(
                    text = "Limpar",
                    color = LaranjadaText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CollectionFilterChipsRow(
    options: List<CollectionFilterOptionUi>,
    selectedValue: String,
    onSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            CollectionFilterChip(
                label = option.label,
                selected = option.value == selectedValue,
                onClick = {
                    onSelected(option.value)
                }
            )
        }
    }
}

@Composable
private fun CollectionFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) {
            LaranjadaOrange
        } else {
            LaranjadaCollectionField
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                LaranjadaOrange
            } else {
                Color.White.copy(alpha = 0.08f)
            }
        )
    ) {
        Text(
            text = label,
            color = LaranjadaText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun CollectionContent(
    uiState: CollectionDetailUiState,
    isLoading: Boolean,
    moviesExpanded: Boolean,
    seriesExpanded: Boolean,
    onToggleMovies: () -> Unit,
    onToggleSeries: () -> Unit,
    onMediaClick: (contentType: String, uuid: String) -> Unit
) {
    val rows = remember(
        uiState.movies,
        uiState.series,
        uiState.appliedFilters.type,
        moviesExpanded,
        seriesExpanded
    ) {
        buildList {
            if (uiState.movies.isNotEmpty()) {
                add(
                    CollectionSectionRow.Header(
                        title = "Filmes",
                        count = uiState.totalMovies,
                        expanded = moviesExpanded,
                        sectionType = CollectionSectionType.Movies
                    )
                )

                if (moviesExpanded) {
                    uiState.movies.chunked(2).forEach { chunk ->
                        add(CollectionSectionRow.Items(chunk))
                    }
                }
            }

            if (uiState.series.isNotEmpty()) {
                add(
                    CollectionSectionRow.Header(
                        title = "Séries",
                        count = uiState.totalSeries,
                        expanded = seriesExpanded,
                        sectionType = CollectionSectionType.Series
                    )
                )

                if (seriesExpanded) {
                    uiState.series.chunked(2).forEach { chunk ->
                        add(CollectionSectionRow.Items(chunk))
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 10.dp,
            bottom = 124.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoading) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = LaranjadaOrange,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )

                    Text(
                        text = "Atualizando...",
                        color = LaranjadaMutedText,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        items(rows) { row ->
            when (row) {
                is CollectionSectionRow.Header -> {
                    CollectionSectionHeader(
                        title = row.title,
                        count = row.count,
                        expanded = row.expanded,
                        onClick = {
                            when (row.sectionType) {
                                CollectionSectionType.Movies -> onToggleMovies()
                                CollectionSectionType.Series -> onToggleSeries()
                            }
                        }
                    )
                }

                is CollectionSectionRow.Items -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.items.forEach { item ->
                            CollectionMediaCard(
                                item = item,
                                onClick = {
                                    if (item.uuid.isNotBlank() && item.contentType.isNotBlank()) {
                                        onMediaClick(item.contentType, item.uuid)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (row.items.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private sealed class CollectionSectionRow {
    data class Header(
        val title: String,
        val count: Int,
        val expanded: Boolean,
        val sectionType: CollectionSectionType
    ) : CollectionSectionRow()

    data class Items(
        val items: List<MediaItemUi>
    ) : CollectionSectionRow()
}

@Composable
private fun CollectionSectionHeader(
    title: String,
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = LaranjadaText,
                fontSize = 19.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )

            if (count > 0) {
                Text(
                    text = "$count conteúdos",
                    color = LaranjadaMutedText,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Icon(
            imageVector = if (expanded) {
                Icons.Rounded.ExpandLess
            } else {
                Icons.Rounded.ExpandMore
            },
            contentDescription = if (expanded) {
                "Recolher $title"
            } else {
                "Expandir $title"
            },
            tint = LaranjadaText,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun CollectionMediaCard(
    item: MediaItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(item.gradientColors))
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.20f)
                            )
                        )
                    )
            )
        }

        Text(
            text = item.title,
            color = LaranjadaText,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )

        if (item.subtitle.isNotBlank()) {
            Text(
                text = item.subtitle,
                color = LaranjadaMutedText,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun CollectionLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = LaranjadaOrange
        )
    }
}

@Composable
private fun CollectionEmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Nenhum conteúdo encontrado nesta coleção.",
            color = LaranjadaMutedText,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun CollectionErrorContent(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Não foi possível carregar a coleção.",
            color = LaranjadaText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = message,
            color = LaranjadaMutedText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        TextButton(
            onClick = onRetryClick,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                tint = LaranjadaOrange,
                modifier = Modifier.padding(end = 6.dp)
            )

            Text(
                text = "Tentar novamente",
                color = LaranjadaOrange,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun collectionTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = LaranjadaText,
    unfocusedTextColor = LaranjadaText,
    focusedContainerColor = LaranjadaCollectionField,
    unfocusedContainerColor = LaranjadaCollectionField,
    cursorColor = LaranjadaOrange,
    focusedBorderColor = LaranjadaOrange.copy(alpha = 0.55f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.06f)
)