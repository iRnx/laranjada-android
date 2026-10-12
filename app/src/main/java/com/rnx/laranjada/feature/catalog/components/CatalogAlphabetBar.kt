package com.rnx.laranjada.feature.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rnx.laranjada.core.design.theme.LaranjadaOrange

private val AlphabetLetters =
    listOf("#") +
            ('A'..'Z').map {
                it.toString()
            }

@Composable
fun CatalogAlphabetBar(
    selectedLetter: String,
    onLetterClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,

    /*
     * [ALTERAÇÃO]
     *
     * false:
     * comportamento original do celular,
     * com rolagem horizontal.
     *
     * true:
     * distribui todas as letras pela
     * largura disponível.
     *
     * Ideal para tablet em retrato
     * e landscape.
     */
    distributeAcrossWidth: Boolean = false
) {
    val listState =
        rememberLazyListState()

    val selected =
        selectedLetter
            .trim()
            .uppercase()

    LaunchedEffect(
        selected,
        distributeAcrossWidth
    ) {
        /*
         * Quando todas as letras já estão
         * visíveis, não precisamos mover
         * nenhuma LazyRow.
         */
        if (
            distributeAcrossWidth
        ) {
            return@LaunchedEffect
        }

        val index =
            AlphabetLetters.indexOf(
                selected
            )

        if (
            index >= 0
        ) {
            listState.animateScrollToItem(
                index
            )
        }
    }

    /*
     * TABLET
     *
     * Todos os caracteres dividem
     * igualmente a largura da tela.
     */
    if (
        distributeAcrossWidth
    ) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(
                        Color(
                            0xFF070B0F
                        )
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 5.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            AlphabetLetters.forEach {
                    letter ->

                val isSelected =
                    selected ==
                            letter

                Box(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .height(
                                44.dp
                            )
                            .clickable(
                                enabled =
                                    enabled
                            ) {
                                onLetterClick(
                                    letter
                                )
                            },

                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            letter,

                        color =
                            when {
                                isSelected ->
                                    Color.White

                                !enabled ->
                                    LaranjadaOrange.copy(
                                        alpha = 0.40f
                                    )

                                else ->
                                    LaranjadaOrange
                            },

                        fontSize =
                            13.sp,

                        fontWeight =
                            if (
                                isSelected
                            ) {
                                FontWeight.ExtraBold
                            } else {
                                FontWeight.Bold
                            }
                    )
                }
            }
        }

        return
    }

    /*
     * CELULAR
     *
     * Mantém exatamente a barra
     * horizontal rolável original.
     */
    LazyRow(
        state =
            listState,

        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    Color(
                        0xFF070B0F
                    )
                ),

        contentPadding =
            PaddingValues(
                horizontal = 8.dp,
                vertical = 5.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                0.dp
            )
    ) {
        items(
            count =
                AlphabetLetters.size,

            key = {
                    index ->

                AlphabetLetters[
                    index
                ]
            }
        ) {
                index ->

            val letter =
                AlphabetLetters[
                    index
                ]

            val isSelected =
                selected ==
                        letter

            Box(
                modifier =
                    Modifier
                        .width(
                            34.dp
                        )
                        .height(
                            39.dp
                        )
                        .clickable(
                            enabled =
                                enabled
                        ) {
                            onLetterClick(
                                letter
                            )
                        },

                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        letter,

                    color =
                        when {
                            isSelected ->
                                Color.White

                            !enabled ->
                                LaranjadaOrange.copy(
                                    alpha = 0.40f
                                )

                            else ->
                                LaranjadaOrange
                        },

                    fontSize =
                        12.sp,

                    fontWeight =
                        if (
                            isSelected
                        ) {
                            FontWeight.ExtraBold
                        } else {
                            FontWeight.Bold
                        }
                )
            }
        }
    }
}