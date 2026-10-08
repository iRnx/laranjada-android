
@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.rnx.laranjada.feature.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.domain.model.FavoriteGenre
import com.rnx.laranjada.domain.model.FavoritesQuery
import com.rnx.laranjada.feature.favorites.components.FavoriteCard
import com.rnx.laranjada.feature.home.components.HomeBottomBar

private val FavoritesBackground =
    Color(0xFF070B0F)

private val FavoritesPanel =
    Color(0xFF101417)

private val FavoritesMuted =
    Color(0xFFBFC4C8)

private val Alphabet =
    listOf("#") +
            ('A'..'Z').map {
                it.toString()
            }

private val Orders = listOf(
    "Nome A-Z" to "name_asc",
    "Nome Z-A" to "name_desc",
    "Adicionados recentemente" to "created_desc",
    "Lançamento mais novo" to "year_desc",
    "Lançamento mais antigo" to "year_asc",
    "Maior avaliação" to "rating_desc"
)

private val ContentTypes = listOf(
    "Todos" to "all",
    "Filmes" to "movie",
    "Séries" to "series"
)

private val Categories = listOf(
    "Todas" to "all",
    "Catálogo principal" to "main",
    "Animes" to "animes",
    "Doramas" to "doramas",
    "Desenhos" to "cartoons"
)

@Composable
fun FavoritesScreen(
    profileUuid: String,
    profileName: String,
    profileAvatarUrl: String?,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAccountClick: () -> Unit,
    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit,
    viewModel: FavoritesViewModel = viewModel()
) {
    val state =
        viewModel.uiState

    val gridState =
        rememberLazyGridState()

    val snackbar =
        remember {
            SnackbarHostState()
        }

    var sortOpen by remember {
        mutableStateOf(false)
    }

    var filtersOpen by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(profileUuid) {
        viewModel.start(profileUuid)
    }

    LaunchedEffect(
        viewModel.feedbackMessage
    ) {
        val message =
            viewModel.feedbackMessage
                ?: return@LaunchedEffect

        viewModel.consumeFeedback()

        snackbar.showSnackbar(message)
    }

    LaunchedEffect(
        state.query,
        state.page
    ) {
        gridState.scrollToItem(0)
    }

    Scaffold(
        containerColor =
            FavoritesBackground,

        snackbarHost = {
            SnackbarHost(
                hostState = snackbar,

                modifier =
                    Modifier.padding(
                        bottom = 128.dp
                    )
            )
        }
    ) { padding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    FavoritesBackground
                )
        ) {
            LazyVerticalGrid(
                columns =
                    GridCells.Fixed(2),

                state =
                    gridState,

                modifier =
                    Modifier.fillMaxSize(),

                contentPadding =
                    PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        top = 10.dp,
                        bottom = 160.dp
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp),

                verticalArrangement =
                    Arrangement.spacedBy(24.dp)
            ) {
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    Column {
                        FavoritesHeader(
                            profileName = profileName,

                            onBackClick =
                                onBackClick,

                            onSearchClick = {
                                filtersOpen = true
                            }
                        )

                        FavoritesToolbar(
                            label = state.rangeLabel,

                            canPrevious =
                                !state.isLoading &&
                                        state.previousPage != null,

                            canNext =
                                !state.isLoading &&
                                        state.nextPage != null,

                            onPrevious =
                                viewModel::previousPage,

                            onNext =
                                viewModel::nextPage,

                            onSort = {
                                sortOpen = true
                            },

                            onFilters = {
                                filtersOpen = true
                            }
                        )

                        Spacer(
                            Modifier.height(32.dp)
                        )
                    }
                }

                when {
                    state.isLoading -> {
                        item(
                            span = {
                                GridItemSpan(maxLineSpan)
                            }
                        ) {
                            CenterState {
                                CircularProgressIndicator(
                                    color =
                                        LaranjadaOrange
                                )
                            }
                        }
                    }

                    state.error != null -> {
                        item(
                            span = {
                                GridItemSpan(maxLineSpan)
                            }
                        ) {
                            CenterState {
                                Text(
                                    text =
                                        state.error.orEmpty(),

                                    color =
                                        Color.White,

                                    textAlign =
                                        TextAlign.Center
                                )

                                TextButton(
                                    onClick =
                                        viewModel::retry
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
                                GridItemSpan(maxLineSpan)
                            }
                        ) {
                            CenterState {
                                Icon(
                                    imageVector =
                                        Icons.Rounded.FavoriteBorder,

                                    contentDescription =
                                        null,

                                    tint =
                                        LaranjadaOrange,

                                    modifier =
                                        Modifier.size(32.dp)
                                )

                                Text(
                                    text =
                                        "Nenhum favorito encontrado",

                                    color =
                                        Color.White,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Text(
                                    text =
                                        if (
                                            state.totalFavorites == 0
                                        ) {
                                            "Use o coração nos detalhes de filmes e séries para criar sua lista."
                                        } else {
                                            "Altere os filtros para encontrar outros conteúdos."
                                        },

                                    color =
                                        FavoritesMuted,

                                    textAlign =
                                        TextAlign.Center,

                                    fontSize =
                                        13.sp
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
                            state.items,

                            key = {
                                it.stableKey
                            }
                        ) { favorite ->

                            FavoriteCard(
                                item = favorite,

                                removing =
                                    state.removingContentKey ==
                                            favorite.stableKey,

                                onOpen = {
                                    onMediaClick(
                                        favorite.contentType,
                                        favorite.contentUuid
                                    )
                                },

                                onRemove = {
                                    viewModel.removeFavorite(
                                        favorite.contentType,
                                        favorite.contentUuid
                                    )
                                }
                            )
                        }
                    }
                }
            }

            /*
             * ALFABETO + BOTTOM BAR
             *
             * O alfabeto permanece acima
             * da navegação nativa.
             */
            Column(
                modifier =
                    Modifier.align(
                        Alignment.BottomCenter
                    )
            ) {
                AlphabetBar(
                    selected =
                        state.query.letter,

                    onSelected =
                        viewModel::selectLetter
                )

                HomeBottomBar(
                    selectedIndex = 2,

                    profileName =
                        profileName,

                    profileAvatarUrl =
                        profileAvatarUrl,

                    onItemClick = { index ->
                        when (index) {
                            0 -> onHomeClick()
                            3 -> onAccountClick()
                        }
                    }
                )
            }
        }
    }

    /*
     * PAINEL DE ORDENAÇÃO
     *
     * ModalBottomSheet requer
     * ExperimentalMaterial3Api.
     */
    if (sortOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                sortOpen = false
            },

            containerColor =
                FavoritesPanel
        ) {
            SheetTitle(
                title = "Ordenar por",

                onClose = {
                    sortOpen = false
                }
            )

            Orders.forEach { (label, value) ->
                ChoiceRow(
                    label = label,

                    selected =
                        state.query.order == value,

                    onClick = {
                        viewModel.applyOrder(value)
                        sortOpen = false
                    }
                )
            }

            Spacer(
                Modifier.height(30.dp)
            )
        }
    }

    /*
     * PAINEL DE FILTROS
     *
     * Utiliza a mesma API experimental
     * do painel de ordenação.
     */
    if (filtersOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                filtersOpen = false
            },

            containerColor =
                FavoritesPanel
        ) {
            FiltersSheet(
                current =
                    state.query,

                years =
                    state.availableYears,

                genres =
                    state.availableGenres,

                onClose = {
                    filtersOpen = false
                },

                onApply = {
                        type,
                        category,
                        search,
                        year,
                        rating,
                        selectedGenres ->

                    viewModel.applyFilters(
                        type,
                        category,
                        search,
                        year,
                        rating,
                        selectedGenres
                    )

                    filtersOpen = false
                },

                onClear = {
                    viewModel.clearFilters()
                    filtersOpen = false
                }
            )
        }
    }
}

@Composable
private fun FavoritesHeader(
    profileName: String,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,

                    contentDescription =
                        "Voltar",

                    tint =
                        Color.White
                )
            }

            IconButton(
                onClick = onSearchClick
            ) {
                Icon(
                    Icons.Rounded.Search,

                    contentDescription =
                        "Buscar favoritos",

                    tint =
                        Color.White
                )
            }
        }

        Spacer(
            Modifier.height(27.dp)
        )

        Text(
            text =
                "PERFIL ${profileName.trim().uppercase()}",

            color =
                LaranjadaOrange,

            fontSize =
                11.sp,

            fontWeight =
                FontWeight.Bold,

            letterSpacing =
                1.4.sp,

            modifier =
                Modifier.align(
                    Alignment.CenterHorizontally
                )
        )

        Spacer(
            Modifier.height(5.dp)
        )

        Text(
            text = "Favoritos",

            color = Color.White,

            fontSize = 28.sp,

            fontWeight =
                FontWeight.Bold,

            modifier =
                Modifier.align(
                    Alignment.CenterHorizontally
                )
        )

        Spacer(
            Modifier.height(9.dp)
        )

        Text(
            text =
                "Filmes, séries, animes, doramas e desenhos ficam salvos somente neste perfil.",

            color =
                FavoritesMuted,

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.SemiBold,

            lineHeight =
                17.sp,

            textAlign =
                TextAlign.Center,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 15.dp
                    )
        )

        Spacer(
            Modifier.height(26.dp)
        )
    }
}

@Composable
private fun FavoritesToolbar(
    label: String,
    canPrevious: Boolean,
    canNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSort: () -> Unit,
    onFilters: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.Center
    ) {
        Text(
            text = label,

            color = Color.White,

            fontWeight =
                FontWeight.Bold,

            fontSize = 12.sp
        )

        Spacer(
            Modifier.size(13.dp)
        )

        IconButton(
            onClick = onPrevious,

            enabled = canPrevious,

            modifier =
                Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Rounded.ChevronLeft,

                contentDescription =
                    "Página anterior",

                tint =
                    if (canPrevious) {
                        Color.White
                    } else {
                        Color.DarkGray
                    }
            )
        }

        IconButton(
            onClick = onNext,

            enabled = canNext,

            modifier =
                Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Rounded.ChevronRight,

                contentDescription =
                    "Próxima página",

                tint =
                    if (canNext) {
                        Color.White
                    } else {
                        Color.DarkGray
                    }
            )
        }

        Text(
            text = "A-Z",

            color = Color.White,

            fontWeight =
                FontWeight.ExtraBold,

            fontSize = 14.sp,

            modifier =
                Modifier
                    .clickable(
                        onClick = onSort
                    )
                    .padding(10.dp)
        )

        IconButton(
            onClick = onFilters,

            modifier =
                Modifier.size(38.dp)
        ) {
            Icon(
                Icons.Rounded.FilterAlt,

                contentDescription =
                    "Abrir filtros",

                tint =
                    Color.White,

                modifier =
                    Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun AlphabetBar(
    selected: String,
    onSelected: (String) -> Unit
) {
    val listState =
        rememberLazyListState()

    LaunchedEffect(selected) {
        val selectedIndex =
            Alphabet.indexOf(selected)

        if (selectedIndex >= 0) {
            listState.animateScrollToItem(
                selectedIndex
            )
        }
    }

    LazyRow(
        state = listState,

        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    FavoritesBackground
                ),

        contentPadding =
            PaddingValues(
                horizontal = 8.dp,
                vertical = 5.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(0.dp)
    ) {
        items(
            Alphabet.size
        ) { index ->

            val letter =
                Alphabet[index]

            Box(
                Modifier
                    .size(
                        width = 34.dp,
                        height = 39.dp
                    )
                    .clickable {
                        onSelected(letter)
                    },

                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = letter,

                    color =
                        if (selected == letter) {
                            Color.White
                        } else {
                            LaranjadaOrange
                        },

                    fontSize = 12.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FiltersSheet(
    current: FavoritesQuery,
    years: List<Int>,
    genres: List<FavoriteGenre>,
    onClose: () -> Unit,
    onApply: (
        String,
        String,
        String,
        String,
        String,
        List<String>
    ) -> Unit,
    onClear: () -> Unit
) {
    var type by remember(current) {
        mutableStateOf(current.contentType)
    }

    var category by remember(current) {
        mutableStateOf(current.category)
    }

    var search by remember(current) {
        mutableStateOf(current.search)
    }

    var year by remember(current) {
        mutableStateOf(current.year)
    }

    var rating by remember(current) {
        mutableStateOf(current.ratingMin)
    }

    var chosenGenres by remember(current) {
        mutableStateOf(current.genres)
    }

    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 640.dp)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 19.dp
            )
    ) {
        SheetTitle(
            title = "Filtros",

            onClose = onClose
        )

        Text(
            text = "Buscar",

            color = FavoritesMuted,

            fontSize = 12.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            Modifier.height(7.dp)
        )

        OutlinedTextField(
            value = search,

            onValueChange = {
                search = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            placeholder = {
                Text(
                    text = "Digite o título",

                    color = FavoritesMuted
                )
            },

            leadingIcon = {
                Icon(
                    Icons.Rounded.Search,

                    contentDescription = null
                )
            },

            keyboardOptions =
                KeyboardOptions(
                    imeAction =
                        ImeAction.Search
                ),

            keyboardActions =
                KeyboardActions(
                    onSearch = {
                        onApply(
                            type,
                            category,
                            search,
                            year,
                            rating,
                            chosenGenres
                        )
                    }
                ),

            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor =
                        Color.White,

                    unfocusedTextColor =
                        Color.White,

                    focusedBorderColor =
                        LaranjadaOrange,

                    unfocusedBorderColor =
                        FavoritesMuted,

                    focusedPlaceholderColor =
                        FavoritesMuted,

                    unfocusedPlaceholderColor =
                        FavoritesMuted
                )
        )

        SheetLabel(
            "Tipo de conteúdo"
        )

        ContentTypes.forEach { (label, value) ->
            ChoiceRow(
                label = label,

                selected =
                    type == value,

                onClick = {
                    type = value
                }
            )
        }

        SheetLabel(
            "Categoria"
        )

        Categories.forEach { (label, value) ->
            ChoiceRow(
                label = label,

                selected =
                    category == value,

                onClick = {
                    category = value
                }
            )
        }

        SheetLabel(
            "Ano"
        )

        SelectionRow(
            label = "Todos",

            selected =
                year.isBlank(),

            onClick = {
                year = ""
            }
        )

        years.forEach { available ->
            SelectionRow(
                label =
                    available.toString(),

                selected =
                    year == available.toString(),

                onClick = {
                    year =
                        available.toString()
                }
            )
        }

        SheetLabel(
            "Nota mínima"
        )

        listOf(
            "Qualquer" to "",
            "5+" to "5",
            "6+" to "6",
            "7+" to "7",
            "8+" to "8",
            "9+" to "9"
        ).forEach { (label, value) ->

            SelectionRow(
                label = label,

                selected =
                    rating == value,

                onClick = {
                    rating = value
                }
            )
        }

        SheetLabel(
            "Gêneros"
        )

        if (genres.isEmpty()) {
            Text(
                text =
                    "Nenhum gênero disponível para estes filtros.",

                color =
                    FavoritesMuted,

                fontSize =
                    12.sp
            )

        } else {
            genres.forEach { genre ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                chosenGenres =
                                    if (
                                        genre.id in chosenGenres
                                    ) {
                                        chosenGenres - genre.id
                                    } else {
                                        chosenGenres + genre.id
                                    }
                            },

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked =
                            genre.id in chosenGenres,

                        onCheckedChange = { checked ->
                            chosenGenres =
                                if (checked) {
                                    chosenGenres + genre.id
                                } else {
                                    chosenGenres - genre.id
                                }
                        },

                        colors =
                            CheckboxDefaults.colors(
                                checkedColor =
                                    LaranjadaOrange
                            )
                    )

                    Text(
                        text = genre.name,

                        color = Color.White,

                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(
            Modifier.height(18.dp)
        )

        Button(
            onClick = {
                onApply(
                    type,
                    category,
                    search,
                    year,
                    rating,
                    chosenGenres
                )
            },

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        LaranjadaOrange,

                    contentColor =
                        Color.Black
                ),

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Aplicar filtros",

                fontWeight =
                    FontWeight.Bold
            )
        }

        TextButton(
            onClick = onClear,

            modifier =
                Modifier.align(
                    Alignment.CenterHorizontally
                )
        ) {
            Text(
                text = "Limpar filtros",

                color = Color.White
            )
        }

        Spacer(
            Modifier.height(26.dp)
        )
    }
}

@Composable
private fun SheetTitle(
    title: String,
    onClose: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {
        Text(
            text = title,

            color = Color.White,

            fontWeight =
                FontWeight.Bold,

            fontSize = 20.sp
        )

        IconButton(
            onClick = onClose
        ) {
            Icon(
                Icons.Rounded.Close,

                contentDescription =
                    "Fechar",

                tint = Color.White
            )
        }
    }
}

@Composable
private fun SheetLabel(
    label: String
) {
    Spacer(
        Modifier.height(15.dp)
    )

    HorizontalDivider(
        color =
            Color.White.copy(
                alpha = 0.10f
            )
    )

    Spacer(
        Modifier.height(13.dp)
    )

    Text(
        text = label,

        color = Color.White,

        fontWeight =
            FontWeight.Bold,

        fontSize = 14.sp
    )

    Spacer(
        Modifier.height(5.dp)
    )
}

@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(
                vertical = 7.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,

            onClick = onClick,

            colors =
                RadioButtonDefaults.colors(
                    selectedColor =
                        LaranjadaOrange
                )
        )

        Text(
            text = label,

            color = Color.White,

            fontSize = 13.sp
        )
    }
}

@Composable
private fun SelectionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ChoiceRow(
        label = label,

        selected = selected,

        onClick = onClick
    )
}

@Composable
private fun CenterState(
    content: @Composable () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 42.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}
