package com.rnx.laranjada.feature.home.components

import androidx.compose.foundation.layout.Arrangement
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
    if (collections.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        SectionHeader(
            title = "Coleções",
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        collections.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                rowItems.forEach { collection ->
                    CollectionCard(
                        collection = collection,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCollectionClick(collection)
                        }
                    )
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}