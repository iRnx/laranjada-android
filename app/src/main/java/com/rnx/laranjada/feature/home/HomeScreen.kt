package com.rnx.laranjada.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.unit.Dp
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

    onFavoritesClick: () -> Unit = {},

    onAccountClick: () -> Unit = {},

    profileName: String = "",

    profileAvatarUrl: String? = null,

    viewModel: HomeViewModel = viewModel()
) {
    val uiState =
        viewModel.uiState

    val selectedBottomIndex =
        remember {
            mutableIntStateOf(
                0
            )
        }

    val continueWatchingListState =
        rememberLazyListState()

    LaunchedEffect(
        Unit
    ) {
        viewModel
            .refreshContinueWatching()
    }

    val firstContinueWatchingMarker =
        uiState.continueWatching
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
            uiState.continueWatching
                .isNotEmpty()
        ) {
            continueWatchingListState
                .scrollToItem(
                    0
                )
        }
    }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    LaranjadaDarkBackground
                )
    ) {
        val isTablet =
            maxWidth >=
                    600.dp

        val isExpandedTablet =
            maxWidth >=
                    840.dp

        /*
         * Cards das seções de conteúdo.
         */
        val mediaCardWidth =
            when {
                isExpandedTablet ->
                    240.dp

                isTablet ->
                    220.dp

                else ->
                    150.dp
            }

        val sectionHorizontalPadding =
            if (
                isTablet
            ) {
                28.dp
            } else {
                18.dp
            }

        /*
         * [ALTERAÇÃO]
         *
         * Categorias acompanham melhor
         * a escala visual da Home.
         */
        val categoryCardWidth =
            when {
                isExpandedTablet ->
                    240.dp

                isTablet ->
                    220.dp

                else ->
                    180.dp
            }

        val categoryCardHeight =
            when {
                isExpandedTablet ->
                    120.dp

                isTablet ->
                    110.dp

                else ->
                    90.dp
            }

        val categorySpacing =
            when {
                isExpandedTablet ->
                    14.dp

                isTablet ->
                    12.dp

                else ->
                    10.dp
            }

        /*
         * Como as categorias ficam parcialmente
         * sobre o final do Hero, o deslocamento
         * também precisa crescer junto.
         */
        val categoryOffsetY =
            when {
                isExpandedTablet ->
                    (-52).dp

                isTablet ->
                    (-48).dp

                else ->
                    (-42).dp
            }

        /*
         * Reserva o espaço necessário dentro
         * da LazyColumn.
         *
         * Sem isso, cards maiores poderiam
         * encostar na próxima seção.
         */
        val categoryContainerHeight =
            when {
                isExpandedTablet ->
                    122.dp

                isTablet ->
                    116.dp

                else ->
                    96.dp
            }

        Box(
            modifier =
                Modifier.fillMaxSize()
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
                            uiState.heroBanners,

                        continueWatching =
                            uiState.continueWatching,

                        onBannerClick = {
                                banner ->

                            if (
                                banner.uuid
                                    .isNotBlank() &&
                                banner.contentType
                                    .isNotBlank()
                            ) {
                                onMediaClick(
                                    banner.contentType,
                                    banner.uuid
                                )
                            }
                        },

                        onWatchClick = {
                                banner,
                                continueItem ->

                            when {
                                continueItem !=
                                        null -> {

                                    onContinueWatchingClick(
                                        continueItem
                                    )
                                }

                                banner
                                    .isSeriesBanner() -> {

                                    if (
                                        banner.uuid
                                            .isNotBlank() &&
                                        banner.contentType
                                            .isNotBlank()
                                    ) {
                                        onMediaClick(
                                            banner.contentType,
                                            banner.uuid
                                        )
                                    }
                                }

                                banner.uuid
                                    .isNotBlank() &&
                                        banner.contentType
                                            .isNotBlank() -> {

                                    onContinueWatchingClick(
                                        banner
                                            .toFreshPlaybackRequest()
                                    )
                                }
                            }
                        },

                        onRestartClick = {
                                continueItem ->

                            viewModel
                                .restartContinueWatching(
                                    item =
                                        continueItem,

                                    onSuccess = {
                                        onContinueWatchingClick(
                                            continueItem.copy(
                                                positionSeconds =
                                                    0L,

                                                progress =
                                                    0f
                                            )
                                        )
                                    }
                                )
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
                                    categoryContainerHeight
                                )
                    ) {
                        CategoryChipsRow(
                            categories =
                                uiState.categories,

                            cardWidth =
                                categoryCardWidth,

                            cardHeight =
                                categoryCardHeight,

                            itemSpacing =
                                categorySpacing,

                            endPadding =
                                sectionHorizontalPadding,

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .offset(
                                        y =
                                            categoryOffsetY
                                    )
                                    .zIndex(
                                        2f
                                    )
                                    .padding(
                                        start =
                                            sectionHorizontalPadding
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
                 *
                 * Mantido sem alteração.
                 * A proporção atual já está boa
                 * no tablet.
                 */
                if (
                    uiState.continueWatching
                        .isNotEmpty()
                ) {
                    item {
                        SectionHeader(
                            title =
                                "Continue assistindo",

                            modifier =
                                Modifier.padding(
                                    horizontal =
                                        sectionHorizontalPadding
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
                                        sectionHorizontalPadding
                                ),

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    14.dp
                                )
                        ) {
                            items(
                                items =
                                    uiState.continueWatching,

                                key = {
                                        item ->

                                    "${item.contentType}:${item.contentUuid}"
                                }
                            ) {
                                    item ->

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
                    items =
                        uiState.contentSections
                ) {
                        section ->

                    SectionHeader(
                        title =
                            section.title,

                        modifier =
                            Modifier.padding(
                                horizontal =
                                    sectionHorizontalPadding
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

                    ResponsiveMediaCarousel(
                        items =
                            section.items,

                        isTablet =
                            isTablet,

                        cardWidth =
                            mediaCardWidth,

                        horizontalPadding =
                            sectionHorizontalPadding,

                        onMediaClick =
                            onMediaClick
                    )
                }

                /*
                 * COLEÇÕES
                 */
                if (
                    uiState.collections
                        .isNotEmpty()
                ) {
                    item {
                        CollectionsSection(
                            collections =
                                uiState.collections,

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

                    when (
                        index
                    ) {
                        2 ->
                            onFavoritesClick()

                        3 ->
                            onAccountClick()

                        else -> {
                            selectedBottomIndex
                                .intValue =
                                index
                        }
                    }
                },

                modifier =
                    Modifier.align(
                        Alignment.BottomCenter
                    )
            )
        }
    }
}

private fun HeroBannerUi
        .toFreshPlaybackRequest():
        ContinueWatchingUi {

    return ContinueWatchingUi(
        title =
            title,

        episodeInfo =
            "",

        remainingTime =
            "",

        progress =
            0f,

        imageUrl =
            imageUrl,

        gradientColors =
            gradientColors,

        contentType =
            contentType,

        contentUuid =
            uuid,

        seriesUuid =
            null,

        positionSeconds =
            0L,

        durationSeconds =
            0L,

        seasonNumber =
            null,

        episodeNumber =
            null,

        lastWatchedAt =
            ""
    )
}

private fun HeroBannerUi
        .isSeriesBanner():
        Boolean {

    return contentType
        .trim()
        .lowercase() in
            setOf(
                "series",
                "serie"
            )
}

@Composable
private fun ResponsiveMediaCarousel(
    items: List<MediaItemUi>,
    isTablet: Boolean,
    cardWidth: Dp,
    horizontalPadding: Dp,

    onMediaClick: (
        contentType: String,
        uuid: String
    ) -> Unit
) {
    /*
     * TABLET
     *
     * Uma única linha horizontal.
     */
    if (
        isTablet
    ) {
        LazyRow(
            contentPadding =
                PaddingValues(
                    horizontal =
                        horizontalPadding
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    14.dp
                )
        ) {
            items(
                items =
                    items,

                key = {
                        item ->

                    "${item.contentType}:${item.uuid}"
                }
            ) {
                    item ->

                LandscapeMediaCard(
                    item =
                        item,

                    cardWidth =
                        cardWidth,

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

        return
    }

    /*
     * CELULAR
     *
     * Mantém dois cards empilhados
     * por coluna.
     */
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
                    horizontalPadding
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        items(
            items =
                columns
        ) {
                columnItems ->

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {
                columnItems.forEach {
                        item ->

                    LandscapeMediaCard(
                        item =
                            item,

                        cardWidth =
                            cardWidth,

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
        title
            .trim()
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