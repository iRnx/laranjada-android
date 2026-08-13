package com.rnx.laranjada.feature.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaSurface
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.feature.home.CategoryUi

@Composable
fun CategoryChipsRow(
    categories: List<CategoryUi>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onCategoryClick: (Int) -> Unit = {}
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 18.dp)
    ) {
        itemsIndexed(categories) { index, category ->
            val selected = index == selectedIndex

            OutlinedButton(
                onClick = { onCategoryClick(index) },
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) {
                        LaranjadaOrange
                    } else {
                        LaranjadaText.copy(alpha = 0.06f)
                    }
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selected) {
                        LaranjadaOrange
                    } else {
                        LaranjadaSurface.copy(alpha = 0.72f)
                    },
                    contentColor = if (selected) {
                        LaranjadaText
                    } else {
                        LaranjadaMutedText
                    }
                ),
                modifier = Modifier.height(42.dp),
                contentPadding = PaddingValues(
                    horizontal = 21.dp,
                    vertical = 0.dp
                )
            ) {
                Text(
                    text = category.name,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
        }
    }
}