
package com.rnx.laranjada.feature.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    enabled: Boolean = true
) {
    val listState =
        rememberLazyListState()

    val selected =
        selectedLetter.trim().uppercase()

    LaunchedEffect(selected) {
        val index =
            AlphabetLetters.indexOf(selected)

        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    LazyRow(
        state = listState,

        modifier = modifier
            .fillMaxWidth()
            .background(
                Color(0xFF070B0F)
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
            count = AlphabetLetters.size,
            key = { index ->
                AlphabetLetters[index]
            }
        ) { index ->

            val letter =
                AlphabetLetters[index]

            val isSelected =
                selected == letter

            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(39.dp)
                    .clickable(
                        enabled = enabled
                    ) {
                        onLetterClick(letter)
                    },

                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = letter,

                    color = when {
                        isSelected ->
                            Color.White

                        !enabled ->
                            LaranjadaOrange.copy(
                                alpha = 0.4f
                            )

                        else ->
                            LaranjadaOrange
                    },

                    fontSize = 12.sp,

                    fontWeight =
                        if (isSelected) {
                            FontWeight.ExtraBold
                        } else {
                            FontWeight.Bold
                        }
                )
            }
        }
    }
}
