package com.rnx.laranjada.feature.home.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rnx.laranjada.R
import com.rnx.laranjada.feature.home.CategoryUi

@Composable
fun CategoryChipsRow(
    categories: List<CategoryUi>,
    modifier: Modifier = Modifier,
    onCategoryClick: (CategoryUi) -> Unit = {}
) {
    val visualCategories = remember(categories) {
        categories.filter { category ->
            categoryImageResource(
                category.slug
            ) != null
        }
    }

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(
            end = 18.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(
            10.dp
        )
    ) {
        items(
            items = visualCategories,
            key = { category ->
                category.slug
            }
        ) { category ->
            CategoryImageCard(
                category = category,
                onClick = {
                    onCategoryClick(
                        category
                    )
                }
            )
        }
    }
}

@Composable
private fun CategoryImageCard(
    category: CategoryUi,
    onClick: () -> Unit
) {
    val imageResource =
        categoryImageResource(
            category.slug
        ) ?: return

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Box(
        modifier = Modifier
            .width(180.dp)
            .height(90.dp)
            .clip(
                RoundedCornerShape(
                    8.dp
                )
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Image(
            painter = painterResource(
                id = imageResource
            ),
            contentDescription =
                category.name,
            modifier = Modifier
                .fillMaxSize(),
            contentScale =
                ContentScale.Crop
        )
    }
}

@DrawableRes
private fun categoryImageResource(
    slug: String
): Int? {
    return when (
        slug.trim()
            .lowercase()
    ) {
        "movies" ->
            R.drawable.movies_home

        "series" ->
            R.drawable.series_home

        "cartoons" ->
            R.drawable.desenhos_home

        "animes" ->
            R.drawable.animes_home

        "doramas" ->
            R.drawable.doramas_home

        "documentaries" ->
            R.drawable.documentarios_home

        "reality-shows" ->
            R.drawable.reality_show_home

        "soap-operas" ->
            R.drawable.novelas_home

        else ->
            null
    }
}