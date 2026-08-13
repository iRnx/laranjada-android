package com.rnx.laranjada.feature.mediagrid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Tv
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.feature.home.MediaItemUi

private val LaranjadaDarkBackground = Color(0xFF070B0F)
private val LaranjadaPanelBackground = Color(0xFF101417)
private val LaranjadaFieldBackground = Color(0xFF070B0F)

@Composable
fun MediaGridScreen(
    sectionSlug: String,
    title: String,
    onBackClick: () -> Unit = {},
    onMediaClick: (contentType: String, uuid: String) -> Unit = { _, _ -> },
    viewModel: MediaGridViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val filtersExpanded = viewModel.filtersExpanded
    val draftFilters = viewModel.draftFilters

    LaunchedEffect(sectionSlug, title) {
        viewModel.start(
            sectionSlug = sectionSlug,
            title = title
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LaranjadaDarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            MediaGridTopBar(
                title = uiState.title.ifBlank { title.ifBlank { "Ver tudo" } },
                totalCount = uiState.totalCount,
                onBackClick = onBackClick
            )

            MediaGridFiltersHeader(
                expanded = filtersExpanded,
                onClick = viewModel::toggleFilters
            )

            if (filtersExpanded) {
                MediaGridFiltersPanel(
                    availableFilters = uiState.availableFilters,
                    draftFilters = draftFilters,
                    onSearchChange = viewModel::onSearchChanged,
                    onSearchSubmit = viewModel::applySearch,
                    onYearChange = viewModel::onYearChanged,
                    onOrderChange = viewModel::onOrderChanged,
                    onRatingMinChange = viewModel::onRatingMinChanged,
                    onKindChange = viewModel::onKindChanged,
                    onClearFilters = viewModel::clearFilters,
                    onClose = viewModel::closeFilters
                )
            }

            when {
                isLoading && uiState.items.isEmpty() -> {
                    InitialLoadingContent()
                }

                errorMessage != null && uiState.items.isEmpty() -> {
                    MediaGridErrorContent(
                        message = errorMessage,
                        onRetryClick = viewModel::reload
                    )
                }

                uiState.items.isEmpty() -> {
                    EmptyGridContent()
                }

                else -> {
                    MediaGridContent(
                        uiState = uiState,
                        isLoading = isLoading,
                        onMediaClick = onMediaClick,
                        onPreviousClick = viewModel::previousPage,
                        onNextClick = viewModel::nextPage,
                        onPageClick = viewModel::goToPage
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaGridTopBar(
    title: String,
    totalCount: Int,
    onBackClick: () -> Unit
) {
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
                tint = LaranjadaText
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = LaranjadaText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (totalCount > 0) {
                Text(
                    text = "$totalCount conteúdos",
                    color = LaranjadaMutedText,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MediaGridFiltersHeader(
    expanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = LaranjadaPanelBackground.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
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
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 4.dp)
            )

            Text(
                text = "Filtros",
                color = LaranjadaText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = LaranjadaText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MediaGridFiltersPanel(
    availableFilters: MediaGridAvailableFiltersUi,
    draftFilters: MediaGridAppliedFiltersUi,
    onSearchChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onYearChange: (String) -> Unit,
    onOrderChange: (String) -> Unit,
    onRatingMinChange: (String) -> Unit,
    onKindChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF090D11),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar filtros",
                        tint = LaranjadaText
                    )
                }
            }

            Text(
                text = "Buscar",
                color = LaranjadaMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = draftFilters.q,
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
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchSubmit()
                    }
                ),
                colors = filterTextFieldColors(),
                shape = RoundedCornerShape(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterDropdown(
                    label = "Ano",
                    selectedValue = draftFilters.year,
                    options = buildList {
                        add(MediaGridFilterOptionUi(label = "Todos", value = ""))
                        availableFilters.years.forEach { year ->
                            add(MediaGridFilterOptionUi(label = year.toString(), value = year.toString()))
                        }
                    },
                    onSelected = onYearChange,
                    modifier = Modifier.weight(1f)
                )

                FilterDropdown(
                    label = "Ordenar",
                    selectedValue = draftFilters.order,
                    options = availableFilters.orders,
                    onSelected = onOrderChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterDropdown(
                    label = "Nota mínima",
                    selectedValue = draftFilters.ratingMin,
                    options = availableFilters.ratings,
                    onSelected = onRatingMinChange,
                    modifier = Modifier.weight(1f)
                )

                ClearFiltersButton(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f)
                )
            }

            if (availableFilters.kinds.size > 1) {
                Text(
                    text = "Tipo",
                    color = LaranjadaMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableFilters.kinds.forEach { option ->
                        KindChip(
                            option = option,
                            selected = option.value == draftFilters.kind,
                            onClick = {
                                onKindChange(option.value)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    selectedValue: String,
    options: List<MediaGridFilterOptionUi>,
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
                color = LaranjadaFieldBackground,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
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
                modifier = Modifier.background(LaranjadaPanelBackground)
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
private fun ClearFiltersButton(
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
            color = LaranjadaFieldBackground,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
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
                    modifier = Modifier
                        .size(17.dp)
                        .padding(end = 4.dp)
                )

                Text(
                    text = "Limpar",
                    color = LaranjadaText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun KindChip(
    option: MediaGridFilterOptionUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (option.value) {
        "movies" -> Icons.Rounded.Movie
        "series" -> Icons.Rounded.Tv
        else -> Icons.Rounded.Tune
    }

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) LaranjadaOrange else LaranjadaFieldBackground,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) LaranjadaOrange else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LaranjadaText,
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 4.dp)
            )

            Text(
                text = option.label,
                color = LaranjadaText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun MediaGridContent(
    uiState: MediaGridUiState,
    isLoading: Boolean,
    onMediaClick: (contentType: String, uuid: String) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPageClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 108.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 8.dp,
            bottom = 28.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(
            items = uiState.items,
            key = { item -> "${item.contentType}-${item.uuid}" }
        ) { item ->
            MediaGridCard(
                item = item,
                onClick = {
                    if (item.uuid.isNotBlank() && item.contentType.isNotBlank()) {
                        onMediaClick(item.contentType, item.uuid)
                    }
                }
            )
        }

        item(
            span = { GridItemSpan(maxLineSpan) }
        ) {
            MediaGridPagination(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                hasPreviousPage = uiState.hasPreviousPage,
                hasNextPage = uiState.hasNextPage,
                isLoading = isLoading,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick,
                onPageClick = onPageClick
            )
        }
    }
}

@Composable
private fun MediaGridCard(
    item: MediaItemUi,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
private fun MediaGridPagination(
    currentPage: Int,
    totalPages: Int,
    hasPreviousPage: Boolean,
    hasNextPage: Boolean,
    isLoading: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPageClick: (Int) -> Unit
) {
    val pages = remember(currentPage, totalPages) {
        buildVisiblePages(
            currentPage = currentPage,
            totalPages = totalPages
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(top = 10.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(LaranjadaPanelBackground.copy(alpha = 0.94f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PaginationArrowButton(
                enabled = hasPreviousPage && !isLoading,
                isLoading = false,
                isNext = false,
                onClick = onPreviousClick
            )

            pages.forEach { page ->
                if (page == null) {
                    PaginationEllipsis()
                } else {
                    PaginationNumberButton(
                        page = page,
                        selected = page == currentPage,
                        enabled = !isLoading,
                        onClick = {
                            onPageClick(page)
                        }
                    )
                }
            }

            PaginationArrowButton(
                enabled = hasNextPage && !isLoading,
                isLoading = isLoading,
                isNext = true,
                onClick = onNextClick
            )
        }
    }
}

@Composable
private fun PaginationNumberButton(
    page: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    LaranjadaOrange
                } else {
                    Color.Transparent
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = page.toString(),
            color = if (selected) LaranjadaText else LaranjadaMutedText,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun PaginationArrowButton(
    enabled: Boolean,
    isLoading: Boolean,
    isNext: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading && isNext) {
            CircularProgressIndicator(
                modifier = Modifier.size(17.dp),
                strokeWidth = 2.dp,
                color = LaranjadaOrange
            )
        } else {
            Icon(
                imageVector = if (isNext) {
                    Icons.Rounded.ChevronRight
                } else {
                    Icons.Rounded.ChevronLeft
                },
                contentDescription = if (isNext) "Próxima página" else "Página anterior",
                tint = if (enabled) LaranjadaText else LaranjadaMutedText.copy(alpha = 0.35f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PaginationEllipsis() {
    Box(
        modifier = Modifier.size(26.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "...",
            color = LaranjadaMutedText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun buildVisiblePages(
    currentPage: Int,
    totalPages: Int
): List<Int?> {
    if (totalPages <= 1) {
        return listOf(1)
    }

    if (totalPages <= 7) {
        return (1..totalPages).toList()
    }

    val pages = mutableListOf<Int?>()
    pages.add(1)

    val start = (currentPage - 1).coerceAtLeast(2)
    val end = (currentPage + 1).coerceAtMost(totalPages - 1)

    if (start > 2) {
        pages.add(null)
    }

    for (page in start..end) {
        pages.add(page)
    }

    if (end < totalPages - 1) {
        pages.add(null)
    }

    pages.add(totalPages)

    return pages
}

@Composable
private fun InitialLoadingContent() {
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
private fun EmptyGridContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Nenhum conteúdo encontrado.",
            color = LaranjadaMutedText,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun MediaGridErrorContent(
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
            text = message,
            color = LaranjadaMutedText,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
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
private fun filterTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = LaranjadaText,
    unfocusedTextColor = LaranjadaText,
    focusedContainerColor = LaranjadaFieldBackground,
    unfocusedContainerColor = LaranjadaFieldBackground,
    cursorColor = LaranjadaOrange,
    focusedBorderColor = LaranjadaOrange.copy(alpha = 0.55f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.06f)
)