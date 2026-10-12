package com.rnx.laranjada.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rnx.laranjada.feature.home.CollectionUi

@Composable
fun CollectionsSection(
    collections: List<CollectionUi>,
    modifier: Modifier = Modifier,
    onCollectionClick: (CollectionUi) -> Unit = {}
) {
    if (
        collections.isEmpty()
    ) {
        return
    }

    BoxWithConstraints(
        modifier =
            modifier.fillMaxWidth()
    ) {
        val isTablet =
            maxWidth >=
                    600.dp

        val isExpandedTablet =
            maxWidth >=
                    840.dp

        /*
         * Celular:
         * mantém as 2 colunas atuais.
         *
         * Tablet em pé:
         * 3 colunas.
         *
         * Tablet deitado / expandido:
         * 4 colunas.
         */
        val columnCount =
            when {
                isExpandedTablet ->
                    4

                isTablet ->
                    3

                else ->
                    2
            }

        val horizontalPadding =
            if (
                isTablet
            ) {
                28.dp
            } else {
                18.dp
            }

        val horizontalSpacing =
            when {
                isExpandedTablet ->
                    16.dp

                isTablet ->
                    14.dp

                else ->
                    14.dp
            }

        val verticalSpacing =
            when {
                isExpandedTablet ->
                    16.dp

                isTablet ->
                    14.dp

                else ->
                    12.dp
            }

        Column(
            modifier =
                Modifier.fillMaxWidth()
        ) {
            SectionHeader(
                title =
                    "Coleções",

                modifier =
                    Modifier.padding(
                        horizontal =
                            horizontalPadding
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        if (
                            isTablet
                        ) {
                            12.dp
                        } else {
                            8.dp
                        }
                    )
            )

            collections
                .chunked(
                    columnCount
                )
                .forEach {
                        rowItems ->

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
                            )
                    ) {
                        rowItems.forEach {
                                collection ->

                            CollectionCard(
                                collection =
                                    collection,

                                modifier =
                                    Modifier.weight(
                                        1f
                                    ),

                                onClick = {
                                    onCollectionClick(
                                        collection
                                    )
                                }
                            )
                        }

                        /*
                         * Mantém o tamanho dos cards
                         * da última linha.
                         *
                         * Se a grade possuir 4 colunas
                         * e sobrarem apenas 2 coleções,
                         * criamos dois espaços vazios
                         * com o mesmo peso.
                         */
                        repeat(
                            columnCount -
                                    rowItems.size
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                verticalSpacing
                            )
                    )
                }
        }
    }
}