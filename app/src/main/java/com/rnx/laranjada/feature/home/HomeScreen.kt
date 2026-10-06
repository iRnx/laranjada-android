package com.rnx.laranjada.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnx.laranjada.feature.home.components.CategoryChipsRow
import com.rnx.laranjada.feature.home.components.CollectionsSection
import com.rnx.laranjada.feature.home.components.ContinueWatchingCard
import com.rnx.laranjada.feature.home.components.HeroSection
import com.rnx.laranjada.feature.home.components.HomeBottomBar
import com.rnx.laranjada.feature.home.components.HomeFooter
import com.rnx.laranjada.feature.home.components.LandscapeMediaCard
import com.rnx.laranjada.feature.home.components.SectionHeader

private val LaranjadaDarkBackground =
    Color(
        0xFF070B0F
    )

@Composable
fun HomeScreen(
    onBannerClick: (
        HeroBannerUi
    ) -> Unit = {},

    onContinueWatchingClick: (
        ContinueWatchingUi
    ) -> Unit = {},

    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit = { _, _ -> },

    onCollectionClick: (
        CollectionUi
    ) -> Unit = {},

    onSeeAllClick: (
        sectionSlug: String,
        title: String
    ) -> Unit = { _, _ -> },

    onCategoryGridClick: (
        sectionSlug: String,
        title: String
    ) -> Unit = { _, _ -> },

    onAccountClick: () -> Unit = {},

    profileName: String = "",

    profileAvatarUrl: String? = null,

    viewModel: HomeViewModel =
        viewModel()
) {
    val uiState =
        viewModel.uiState

    val selectedBottomIndex =
        remember {
            mutableIntStateOf(
                0
            )
        }

    /*
     * Estado independente da faixa
     * Continue Assistindo.
     *
     * Quando os dados forem atualizados,
     * podemos voltar a faixa ao item 0
     * sem mexer no scroll vertical da Home.
     */
    val continueWatchingListState =
        rememberLazyListState()

    /*
     * Quando entramos novamente na Home
     * depois de sair do Player, este
     * LaunchedEffect é criado novamente.
     *
     * O ViewModel permanece associado
     * à entrada da Home no Navigation,
     * portanto fazemos apenas um refresh
     * do Continue Assistindo.
     *
     * Na primeira abertura da aplicação,
     * loadHome() já estará executando e
     * refreshContinueWatching() simplesmente
     * ignora a chamada duplicada.
     */
    LaunchedEffect(
        Unit
    ) {
        viewModel
            .refreshContinueWatching()
    }

    /*
     * Marcador do primeiro item.
     *
     * Quando o backend atualiza
     * last_watched_at, o GET devolve o
     * conteúdo recém-assistido em primeiro.
     *
     * Ao detectar que o primeiro item mudou
     * ou recebeu um novo last_watched_at,
     * fazemos a faixa voltar ao início.
     */
    val firstContinueWatchingMarker =
        uiState
            .continueWatching
            .firstOrNull()
            ?.let {
                    item ->

                "${item.contentType}:" +
                        "${item.contentUuid}:" +
                        "${item.positionSeconds}:" +
                        item.lastWatchedAt
            }
            .orEmpty()

    LaunchedEffect(
        firstContinueWatchingMarker
    ) {
        if (
            firstContinueWatchingMarker
                .isNotBlank() &&
            uiState
                .continueWatching
                .isNotEmpty()
        ) {
            continueWatchingListState
                .scrollToItem(
                    0
                )
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    LaranjadaDarkBackground
                )
    ) {
        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    bottom =
                        118.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    18.dp
                )
        ) {

            /*
             * HERO
             */
            item {
                HeroSection(
                    banners =
                        uiState
                            .heroBanners,

                    onBannerClick = {
                            banner ->

                        if (
                            banner.uuid
                                .isNotBlank() &&
                            banner.contentType
                                .isNotBlank()
                        ) {
                            onBannerClick(
                                banner
                            )
                        }
                    }
                )
            }

            /*
             * CATEGORIAS
             */
            item {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                96.dp
                            )
                ) {
                    CategoryChipsRow(
                        categories =
                            uiState
                                .categories,

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .offset(
                                    y =
                                        (-42).dp
                                )
                                .zIndex(
                                    2f
                                )
                                .padding(
                                    start =
                                        18.dp
                                ),

                        onCategoryClick = {
                                category ->

                            val sectionSlug =
                                categoryToSectionSlug(
                                    category.slug
                                )

                            if (
                                sectionSlug
                                    .isNotBlank()
                            ) {
                                onCategoryGridClick(
                                    sectionSlug,
                                    category.name
                                )
                            }
                        }
                    )
                }
            }

            /*
             * CONTINUE ASSISTINDO
             */
            if (
                uiState
                    .continueWatching
                    .isNotEmpty()
            ) {
                item {
                    SectionHeader(
                        title =
                            "Continue assistindo",

                        modifier =
                            Modifier.padding(
                                horizontal =
                                    18.dp
                            ),

                        showSeeAll =
                            false
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                6.dp
                            )
                    )

                    LazyRow(
                        state =
                            continueWatchingListState,

                        contentPadding =
                            PaddingValues(
                                horizontal =
                                    18.dp
                            ),

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                14.dp
                            )
                    ) {
                        items(
                            uiState
                                .continueWatching,

                            /*
                             * UUID do conteúdo como chave.
                             *
                             * Agora que a ordem pode mudar
                             * depois de um refresh, uma chave
                             * estável ajuda o Compose a saber
                             * que é o mesmo card apenas em
                             * outra posição.
                             */
                            key = {
                                    item ->

                                "${item.contentType}:" +
                                        item.contentUuid
                            }
                        ) { item ->

                            ContinueWatchingCard(
                                item =
                                    item,

                                onClick = {
                                    if (
                                        item.contentType
                                            .isNotBlank() &&
                                        item.contentUuid
                                            .isNotBlank()
                                    ) {
                                        onContinueWatchingClick(
                                            item
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            /*
             * SEÇÕES
             */
            items(
                uiState
                    .contentSections
            ) { section ->

                SectionHeader(
                    title =
                        section.title,

                    modifier =
                        Modifier.padding(
                            horizontal =
                                18.dp
                        ),

                    showSeeAll =
                        true,

                    onSeeAllClick = {
                        val sectionSlug =
                            section.slug
                                .ifBlank {
                                    sectionTitleToSlug(
                                        section.title
                                    )
                                }

                        if (
                            sectionSlug
                                .isNotBlank()
                        ) {
                            onSeeAllClick(
                                sectionSlug,
                                section.title
                            )
                        }
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            6.dp
                        )
                )

                TwoRowsMediaCarousel(
                    items =
                        section.items,

                    onMediaClick =
                        onMediaClick
                )
            }

            /*
             * COLEÇÕES
             */
            if (
                uiState
                    .collections
                    .isNotEmpty()
            ) {
                item {
                    CollectionsSection(
                        collections =
                            uiState
                                .collections,

                        onCollectionClick =
                            onCollectionClick
                    )
                }
            }

            /*
             * FOOTER
             */
            item {
                HomeFooter(
                    modifier =
                        Modifier.padding(
                            start =
                                24.dp,

                            end =
                                24.dp,

                            top =
                                28.dp,

                            bottom =
                                20.dp
                        )
                )
            }
        }

        HomeBottomBar(
            selectedIndex =
                selectedBottomIndex
                    .intValue,

            profileName =
                profileName,

            profileAvatarUrl =
                profileAvatarUrl,

            onItemClick = {
                    index ->

                if (
                    index ==
                    3
                ) {
                    onAccountClick()
                } else {
                    selectedBottomIndex
                        .intValue =
                        index
                }
            },

            modifier =
                Modifier.align(
                    Alignment.BottomCenter
                )
        )
    }
}

@Composable
private fun TwoRowsMediaCarousel(
    items: List<MediaItemUi>,

    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit
) {
    val columns =
        remember(
            items
        ) {
            items.chunked(
                2
            )
        }

    LazyRow(
        contentPadding =
            PaddingValues(
                horizontal =
                    18.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        items(
            columns
        ) { columnItems ->

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {
                columnItems
                    .forEach {
                            item ->

                        LandscapeMediaCard(
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
                            }
                        )
                    }
            }
        }
    }
}

private fun categoryToSectionSlug(
    categorySlug: String
): String {
    return when (
        categorySlug
            .trim()
            .lowercase()
    ) {
        "movies" ->
            "movies"

        "series" ->
            "series"

        "cartoons" ->
            "all-cartoons"

        "animes" ->
            "all-animes"

        "doramas" ->
            "doramas"

        "documentaries" ->
            "documentaries"

        "reality-shows" ->
            "reality-shows"

        "soap-operas" ->
            "soap-operas"

        else ->
            ""
    }
}

private fun sectionTitleToSlug(
    title: String
): String {
    return when (
        title.trim()
            .lowercase()
    ) {
        "novos filmes" ->
            "recent-movies"

        "novas séries",
        "novas series" ->
            "recent-series"

        "filmes adicionados recentemente" ->
            "recent-movies"

        "séries adicionadas recentemente",
        "series adicionadas recentemente" ->
            "recent-series"

        "filmes em desenho" ->
            "cartoon-movies"

        "séries em desenho",
        "series em desenho" ->
            "cartoon-series"

        "animes filmes" ->
            "anime-movies"

        "animes séries",
        "animes series" ->
            "anime-series"

        "doramas" ->
            "doramas"

        "documentários",
        "documentarios" ->
            "documentaries"

        "reality shows" ->
            "reality-shows"

        "novelas" ->
            "soap-operas"

        "filmes" ->
            "movies"

        "séries",
        "series" ->
            "series"

        "desenhos" ->
            "all-cartoons"

        "animes" ->
            "all-animes"

        else ->
            ""
    }
}