package com.rnx.laranjada.feature.collections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaSurface
import com.rnx.laranjada.core.design.theme.LaranjadaSurfaceLight
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.feature.home.MediaItemUi
import com.rnx.laranjada.feature.home.components.HomeBottomBar
import com.rnx.laranjada.feature.home.components.LandscapeMediaCard

@Composable
fun CollectionDetailScreen(
    uiState: CollectionDetailUiState,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LaranjadaBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                CollectionHero(
                    uiState = uiState
                )
            }

            item {
                CollectionFilters(
                    uiState = uiState,
                    onSearchChange = onSearchChange,
                    onYearChange = onYearChange,
                    onOrderChange = onOrderChange,
                    onRatingMinChange = onRatingMinChange,
                    onTypeChange = onTypeChange,
                    onClearFilters = onClearFilters
                )
            }

            if (isLoading) {
                item {
                    Text(
                        text = "Atualizando coleção...",
                        color = LaranjadaMutedText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item {
                    ErrorCard(
                        message = errorMessage,
                        onRetryClick = onRetryClick
                    )
                }
            }

            if (!isLoading && errorMessage.isNullOrBlank() && uiState.isEmptyResult) {
                item {
                    EmptyResultCard()
                }
            }

            if (uiState.movies.isNotEmpty()) {
                item {
                    CollectionMediaSection(
                        title = "Filmes",
                        items = uiState.movies,
                        onMediaClick = onMediaClick
                    )
                }
            }

            if (uiState.series.isNotEmpty()) {
                item {
                    CollectionMediaSection(
                        title = "Séries",
                        items = uiState.series,
                        onMediaClick = onMediaClick
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        CircleTopButton(
            icon = Icons.Rounded.Close,
            contentDescription = "Fechar",
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 8.dp, end = 18.dp)
        )

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
    uiState: CollectionDetailUiState
) {
    val title = uiState.title.ifBlank { "Coleção" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .clip(
                RoundedCornerShape(
                    bottomStart = 30.dp,
                    bottomEnd = 30.dp
                )
            )
            .background(LaranjadaSurface)
    ) {
        AsyncImage(
            model = uiState.imageUrl,
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
                            0.00f to Color.Black.copy(alpha = 0.03f),
                            0.36f to Color.Black.copy(alpha = 0.05f),
                            0.70f to Color.Black.copy(alpha = 0.46f),
                            1.00f to LaranjadaBlack
                        )
                    )
                )
        )

        Text(
            text = "Coleção \"$title\"",
            color = LaranjadaText,
            fontSize = 27.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun CircleTopButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.48f)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

@Composable
private fun CollectionFilters(
    uiState: CollectionDetailUiState,
    onSearchChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onOrderChange: (String) -> Unit,
    onRatingMinChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    ) {
        FilterToggleButton(
            expanded = showFilters,
            onClick = {
                showFilters = !showFilters
            }
        )

        if (showFilters) {
            Spacer(modifier = Modifier.height(14.dp))

            FiltersPanel(
                uiState = uiState,
                onSearchChange = onSearchChange,
                onYearChange = onYearChange,
                onOrderChange = onOrderChange,
                onRatingMinChange = onRatingMinChange,
                onTypeChange = onTypeChange,
                onClearFilters = onClearFilters
            )
        }
    }
}

@Composable
private fun FilterToggleButton(
    expanded: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = LaranjadaSurface,
        border = BorderStroke(
            width = 1.dp,
            color = LaranjadaSurfaceLight
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                tint = LaranjadaText,
                modifier = Modifier.size(23.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Filtros",
                color = LaranjadaText,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = if (expanded) {
                    Icons.Rounded.KeyboardArrowUp
                } else {
                    Icons.Rounded.KeyboardArrowDown
                },
                contentDescription = null,
                tint = LaranjadaText,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltersPanel(
    uiState: CollectionDetailUiState,
    onSearchChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onOrderChange: (String) -> Unit,
    onRatingMinChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    var showSpecificFilters by remember { mutableStateOf(false) }

    val yearOptions = remember(uiState.availableFilters.years) {
        listOf(CollectionFilterOptionUi(label = "Todos", value = "")) +
                uiState.availableFilters.years.map { year ->
                    CollectionFilterOptionUi(
                        label = year.toString(),
                        value = year.toString()
                    )
                }
    }

    val orderOptions = uiState.availableFilters.orders
    val ratingOptions = uiState.availableFilters.ratings
    val typeOptions = uiState.availableFilters.types
    val specificTypeOptions = uiState.availableFilters.specificTypes

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = LaranjadaSurface.copy(alpha = 0.82f),
        border = BorderStroke(
            width = 1.dp,
            color = LaranjadaSurfaceLight.copy(alpha = 0.82f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SearchBox(
                value = uiState.appliedFilters.q,
                onValueChange = onSearchChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterSelectBox(
                    label = "Ano",
                    selectedValue = uiState.appliedFilters.year,
                    fallbackLabel = "Todos",
                    options = yearOptions,
                    modifier = Modifier.weight(1f),
                    onOptionSelected = onYearChange
                )

                FilterSelectBox(
                    label = "Ordenar",
                    selectedValue = uiState.appliedFilters.order,
                    fallbackLabel = "Recentes",
                    options = orderOptions,
                    modifier = Modifier.weight(1f),
                    onOptionSelected = onOrderChange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterSelectBox(
                    label = "Nota mínima",
                    selectedValue = uiState.appliedFilters.ratingMin,
                    fallbackLabel = "Qualquer",
                    options = ratingOptions,
                    modifier = Modifier.weight(1f),
                    onOptionSelected = onRatingMinChange
                )

                ClearFilterButton(
                    modifier = Modifier.weight(1f),
                    onClick = onClearFilters
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tipo",
                color = LaranjadaMutedText,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                typeOptions.forEach { option ->
                    MainFilterChip(
                        text = option.label,
                        selected = uiState.appliedFilters.type == option.value,
                        icon = when (option.value) {
                            "movies_only" -> Icons.Rounded.Movie
                            "series_only" -> Icons.Rounded.Tv
                            else -> null
                        },
                        onClick = {
                            onTypeChange(option.value)
                        }
                    )
                }

                MainFilterChip(
                    text = "Mais opções",
                    icon = Icons.Rounded.Tune,
                    showArrow = true,
                    expanded = showSpecificFilters,
                    onClick = {
                        showSpecificFilters = !showSpecificFilters
                    }
                )
            }

            if (showSpecificFilters) {
                Spacer(modifier = Modifier.height(16.dp))

                SpecificFiltersPanel(
                    options = specificTypeOptions,
                    selectedValue = uiState.appliedFilters.type,
                    onTypeChange = onTypeChange
                )
            }
        }
    }
}

@Composable
private fun SearchBox(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Buscar",
            color = LaranjadaMutedText,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            color = LaranjadaBlack.copy(alpha = 0.24f),
            border = BorderStroke(
                width = 1.dp,
                color = LaranjadaSurfaceLight
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = LaranjadaMutedText,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isBlank()) {
                        Text(
                            text = "Digite título em PT-BR/EN",
                            color = LaranjadaMutedText,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = LaranjadaText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSelectBox(
    label: String,
    selectedValue: String,
    fallbackLabel: String,
    options: List<CollectionFilterOptionUi>,
    modifier: Modifier = Modifier,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = options
        .firstOrNull { option -> option.value == selectedValue }
        ?.label
        ?: fallbackLabel

    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            color = LaranjadaMutedText,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 6.dp)
        )

        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            expanded = true
                        }
                    ),
                shape = RoundedCornerShape(16.dp),
                color = LaranjadaBlack.copy(alpha = 0.24f),
                border = BorderStroke(
                    width = 1.dp,
                    color = LaranjadaSurfaceLight
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedLabel,
                        color = LaranjadaText,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = if (expanded) {
                            Icons.Rounded.KeyboardArrowUp
                        } else {
                            Icons.Rounded.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = LaranjadaMutedText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                color = LaranjadaText
                            )
                        },
                        onClick = {
                            expanded = false
                            onOptionSelected(option.value)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClearFilterButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "",
            color = Color.Transparent,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(16.dp),
            color = LaranjadaBlack.copy(alpha = 0.24f),
            border = BorderStroke(
                width = 1.dp,
                color = LaranjadaSurfaceLight
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = LaranjadaText,
                    modifier = Modifier.size(21.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Limpar",
                    color = LaranjadaText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun MainFilterChip(
    text: String,
    selected: Boolean = false,
    icon: ImageVector? = null,
    showArrow: Boolean = false,
    expanded: Boolean = false,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        ),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) LaranjadaOrange else LaranjadaBlack.copy(alpha = 0.24f),
        border = if (selected) {
            null
        } else {
            BorderStroke(
                width = 1.dp,
                color = LaranjadaSurfaceLight
            )
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LaranjadaText,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(7.dp))
            }

            Text(
                text = text,
                color = LaranjadaText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            if (showArrow) {
                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = if (expanded) {
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = LaranjadaText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpecificFiltersPanel(
    options: List<CollectionFilterOptionUi>,
    selectedValue: String,
    onTypeChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LaranjadaBlack.copy(alpha = 0.30f),
        border = BorderStroke(
            width = 1.dp,
            color = LaranjadaSurfaceLight.copy(alpha = 0.75f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filtros específicos",
                color = LaranjadaMutedText,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.forEach { option ->
                    SpecificFilterChip(
                        text = option.label,
                        selected = selectedValue == option.value,
                        onClick = {
                            onTypeChange(option.value)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecificFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) LaranjadaOrange.copy(alpha = 0.88f) else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                LaranjadaOrange
            } else {
                Color(0xFF355D8E).copy(alpha = 0.90f)
            }
        )
    ) {
        Text(
            text = text,
            color = LaranjadaText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun CollectionMediaSection(
    title: String,
    items: List<MediaItemUi>,
    onMediaClick: (contentType: String, uuid: String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = LaranjadaText,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Ver tudo",
                color = LaranjadaOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(items) { item ->
                LandscapeMediaCard(
                    item = item,
                    onClick = {
                        onMediaClick(
                            item.contentType,
                            item.uuid
                        )
                    }
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 18.dp),
            color = Color.Transparent
        )
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetryClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(18.dp),
        color = LaranjadaSurface,
        border = BorderStroke(
            width = 1.dp,
            color = LaranjadaSurfaceLight
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Não foi possível carregar a coleção.",
                color = LaranjadaText,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                color = LaranjadaMutedText,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRetryClick
                ),
                shape = RoundedCornerShape(16.dp),
                color = LaranjadaOrange
            ) {
                Text(
                    text = "Tentar novamente",
                    color = LaranjadaText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyResultCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(18.dp),
        color = LaranjadaSurface,
        border = BorderStroke(
            width = 1.dp,
            color = LaranjadaSurfaceLight
        )
    ) {
        Text(
            text = "Nenhum item encontrado com esses filtros.",
            color = LaranjadaMutedText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(18.dp)
        )
    }
}