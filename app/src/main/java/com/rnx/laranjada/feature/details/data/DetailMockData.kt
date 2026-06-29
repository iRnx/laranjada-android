package com.rnx.laranjada.feature.details.data

import com.rnx.laranjada.feature.details.DetailContentType
import com.rnx.laranjada.feature.details.DetailUiState
import com.rnx.laranjada.feature.details.EpisodeUi
import com.rnx.laranjada.feature.details.SeasonUi
import com.rnx.laranjada.feature.details.WatchProgressUi

object DetailMockData {

    val serieDetail = DetailUiState(
        contentType = DetailContentType.Series,
        title = "Zack & Cody: Gêmeos a Bordo",
        originalTitle = "The Suite Life on Deck",
        year = "2008",
        endYear = "2010",
        rating = "AL",
        genres = listOf("Amadurecimento", "Comédia", "Família"),
        durationInfo = "3 temporadas",
        synopsis = "Zack e Cody Martin estão a bordo do S.S. Tipton. Agora eles vivem novas aventuras, conhecem novos amigos e transformam cada viagem em confusão.",
        imageDetailUrl = "https://picsum.photos/seed/laranjada-detail-zack-cody/1200/1500",
        imageThumbUrl = "https://picsum.photos/seed/laranjada-thumb-zack-cody/640/360",
        watchProgress = WatchProgressUi(
            progress = 0.24f,
            remainingText = "Restam 24min",
            seasonNumber = 1,
            episodeNumber = 2,
            episodeTitle = "Ilha do Papagaio"
        ),
        seasons = listOf(
            SeasonUi(
                id = "season-1",
                number = 1,
                episodes = listOf(
                    episode(
                        number = 1,
                        title = "Prontos Para Zarpar",
                        runtime = "24min",
                        synopsis = "Zack, Cody e London mudam-se a bordo do S.S. Tipton."
                    ),
                    episode(
                        number = 2,
                        title = "Ilha do Papagaio",
                        runtime = "23min",
                        synopsis = "O navio desvia para a mansão de Sr. Tipton na Ilha do Papagaio."
                    ),
                    episode(
                        number = 3,
                        title = "Quebrando o Ioiô",
                        runtime = "24min",
                        synopsis = "Zack e Cody ficam responsáveis pelo dinheiro."
                    ),
                    episode(
                        number = 4,
                        title = "O Rim do Mar",
                        runtime = "24min",
                        synopsis = "Zack se apaixona por Violet."
                    )
                )
            ),
            SeasonUi(
                id = "season-2",
                number = 2,
                episodes = listOf(
                    episode(
                        number = 1,
                        title = "Novas Rotas",
                        runtime = "25min",
                        synopsis = "A turma começa uma nova viagem cheia de situações inesperadas."
                    ),
                    episode(
                        number = 2,
                        title = "Problemas no Convés",
                        runtime = "24min",
                        synopsis = "Uma confusão no navio coloca Zack e Cody em apuros."
                    ),
                    episode(
                        number = 3,
                        title = "Baile em Alto Mar",
                        runtime = "23min",
                        synopsis = "O baile do navio vira uma noite cheia de desencontros."
                    )
                )
            ),
            SeasonUi(
                id = "season-3",
                number = 3,
                episodes = listOf(
                    episode(
                        number = 1,
                        title = "Última Viagem",
                        runtime = "24min",
                        synopsis = "A turma se prepara para uma das viagens mais importantes do S.S. Tipton."
                    ),
                    episode(
                        number = 2,
                        title = "Adeus ao Convés",
                        runtime = "26min",
                        synopsis = "Zack, Cody e seus amigos precisam tomar decisões sobre o futuro."
                    )
                )
            )
        )
    )

    val movieDetail = DetailUiState(
        contentType = DetailContentType.Movie,
        title = "A Empregada",
        originalTitle = "A Empregada",
        year = "2024",
        rating = "16",
        genres = listOf("Drama", "Suspense"),
        durationInfo = "1h 48min",
        synopsis = "Duas vidas se cruzam dentro de uma casa cheia de segredos. Aos poucos, tudo que parecia simples começa a revelar uma verdade perigosa.",
        imageDetailUrl = "https://picsum.photos/seed/laranjada-detail-movie/1200/1500",
        imageThumbUrl = "https://picsum.photos/seed/laranjada-thumb-movie/640/360",
        watchProgress = WatchProgressUi(
            progress = 0.38f,
            remainingText = "Restam 1h07min"
        )
    )

    private fun episode(
        number: Int,
        title: String,
        runtime: String,
        synopsis: String
    ): EpisodeUi {
        return EpisodeUi(
            id = "episode-$number",
            uuid = "mock-episode-$number",
            number = number,
            title = title,
            runtime = runtime,
            rating = "AL",
            synopsis = synopsis,
            imageUrl = "https://picsum.photos/seed/laranjada-episode-$number/640/360",
            hasVideo = true
        )
    }
}