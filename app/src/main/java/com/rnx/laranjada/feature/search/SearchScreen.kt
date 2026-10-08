package com.rnx.laranjada.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.feature.catalog.components.CatalogMediaCard

private val SearchBackground =
    Color(
        0xFF070B0F
    )

private val SearchMuted =
    Color(
        0xFFBFC4C8
    )

@Composable
fun SearchScreen(
    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit,
    viewModel: SearchViewModel =
        viewModel()
) {
    val state =
        viewModel.uiState

    val focusManager =
        LocalFocusManager.current

    val gridState =
        rememberLazyGridState()

    val screenWidth =
        LocalConfiguration.current
            .screenWidthDp

    val columnCount =
        when {
            screenWidth >= 840 ->
                4

            screenWidth >= 600 ->
                3

            else ->
                2
        }

    /*
     * Cada nova busca concluída
     * volta os resultados para o topo.
     */
    LaunchedEffect(
        state.submittedQuery
    ) {
        if (
            state.hasSearched &&
            state.results.isNotEmpty()
        ) {
            gridState.scrollToItem(
                0
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                SearchBackground
            )
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
            .padding(
                horizontal = 14.dp
            )
    ) {
        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )

        Text(
            text =
                "Pesquisar",

            color =
                Color.White,

            fontSize =
                28.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(
                    6.dp
                )
        )

        Text(
            text =
                "Encontre filmes, séries, animes, doramas, desenhos e muito mais.",

            color =
                SearchMuted,

            fontSize =
                13.sp,

            lineHeight =
                18.sp
        )

        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )

        OutlinedTextField(
            value =
                state.query,

            onValueChange =
                viewModel::onQueryChanged,

            modifier =
                Modifier.fillMaxWidth(),

            singleLine =
                true,

            placeholder = {
                Text(
                    text =
                        "Digite um título",

                    color =
                        SearchMuted
                )
            },

            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.Rounded.Search,

                    contentDescription =
                        null
                )
            },

            trailingIcon = {
                if (
                    state.query.isNotBlank()
                ) {
                    IconButton(
                        onClick = {
                            viewModel.clearSearch()

                            focusManager
                                .clearFocus()
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Close,

                            contentDescription =
                                "Limpar busca"
                        )
                    }
                }
            },

            keyboardOptions =
                KeyboardOptions(
                    imeAction =
                        ImeAction.Search
                ),

            keyboardActions =
                KeyboardActions(
                    onSearch = {
                        viewModel.submitSearch()

                        focusManager
                            .clearFocus()
                    }
                ),

            shape =
                RoundedCornerShape(
                    12.dp
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
                        Color.White.copy(
                            alpha = 0.24f
                        ),

                    focusedLeadingIconColor =
                        LaranjadaOrange,

                    unfocusedLeadingIconColor =
                        SearchMuted,

                    focusedTrailingIconColor =
                        Color.White,

                    unfocusedTrailingIconColor =
                        Color.White,

                    cursorColor =
                        LaranjadaOrange,

                    focusedContainerColor =
                        Color.White.copy(
                            alpha = 0.03f
                        ),

                    unfocusedContainerColor =
                        Color.White.copy(
                            alpha = 0.03f
                        )
                )
        )

        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )

        when {
            state.isLoading -> {
                SearchCenterState(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    CircularProgressIndicator(
                        color =
                            LaranjadaOrange,

                        modifier =
                            Modifier.size(
                                34.dp
                            ),

                        strokeWidth =
                            2.5.dp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                16.dp
                            )
                    )

                    Text(
                        text =
                            "Buscando no catálogo...",

                        color =
                            SearchMuted,

                        fontSize =
                            14.sp
                    )
                }
            }

            state.errorMessage != null -> {
                SearchCenterState(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Search,

                        contentDescription =
                            null,

                        tint =
                            LaranjadaOrange,

                        modifier =
                            Modifier.size(
                                36.dp
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                14.dp
                            )
                    )

                    Text(
                        text =
                            "Não foi possível pesquisar",

                        color =
                            Color.White,

                        fontSize =
                            18.sp,

                        fontWeight =
                            FontWeight.Bold,

                        textAlign =
                            TextAlign.Center
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                7.dp
                            )
                    )

                    Text(
                        text =
                            state.errorMessage,

                        color =
                            SearchMuted,

                        fontSize =
                            13.sp,

                        lineHeight =
                            18.sp,

                        textAlign =
                            TextAlign.Center
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                12.dp
                            )
                    )

                    TextButton(
                        onClick =
                            viewModel::retry
                    ) {
                        Text(
                            text =
                                "Tentar novamente",

                            color =
                                LaranjadaOrange,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }

            !state.hasSearched -> {
                SearchCenterState(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Search,

                        contentDescription =
                            null,

                        tint =
                            LaranjadaOrange,

                        modifier =
                            Modifier.size(
                                40.dp
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                15.dp
                            )
                    )

                    Text(
                        text =
                            "O que você quer assistir?",

                        color =
                            Color.White,

                        fontSize =
                            18.sp,

                        fontWeight =
                            FontWeight.Bold,

                        textAlign =
                            TextAlign.Center
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                7.dp
                            )
                    )

                    Text(
                        text =
                            "Digite o nome de um filme ou série para começar.",

                        color =
                            SearchMuted,

                        fontSize =
                            13.sp,

                        lineHeight =
                            18.sp,

                        textAlign =
                            TextAlign.Center
                    )
                }
            }

            state.results.isEmpty() -> {
                SearchCenterState(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Search,

                        contentDescription =
                            null,

                        tint =
                            SearchMuted,

                        modifier =
                            Modifier.size(
                                38.dp
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                14.dp
                            )
                    )

                    Text(
                        text =
                            "Nenhum resultado encontrado",

                        color =
                            Color.White,

                        fontSize =
                            18.sp,

                        fontWeight =
                            FontWeight.Bold,

                        textAlign =
                            TextAlign.Center
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                7.dp
                            )
                    )

                    Text(
                        text =
                            "Não encontramos nada para “${state.submittedQuery}”.",

                        color =
                            SearchMuted,

                        fontSize =
                            13.sp,

                        lineHeight =
                            18.sp,

                        textAlign =
                            TextAlign.Center
                    )
                }
            }

            else -> {
                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        text =
                            when (
                                state.totalCount
                            ) {
                                1 ->
                                    "1 resultado"

                                else ->
                                    "${state.totalCount} resultados"
                            },

                        color =
                            SearchMuted,

                        fontSize =
                            12.sp,

                        fontWeight =
                            FontWeight.SemiBold,

                        modifier =
                            Modifier.padding(
                                bottom = 12.dp
                            )
                    )

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
                                bottom = 28.dp
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
                        items(
                            items =
                                state.results,

                            key = {
                                    item ->

                                "${item.contentType}:${item.uuid}"
                            }
                        ) {
                                item ->

                            CatalogMediaCard(
                                item =
                                    item,

                                onClick = {
                                    onMediaClick(
                                        item.contentType,
                                        item.uuid
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCenterState(
    modifier: Modifier =
        Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp
                ),

        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}