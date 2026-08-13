package com.rnx.laranjada.feature.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaSurfaceLight
import com.rnx.laranjada.core.design.theme.LaranjadaText

private val LaranjadaBottomBarBackground = Color(0xFF101417)

private enum class BottomNavItemType {
    Icon,
    Avatar
}

private data class BottomNavItemUi(
    val label: String,
    val icon: ImageVector? = null,
    val type: BottomNavItemType = BottomNavItemType.Icon
)

@Composable
fun HomeBottomBar(
    selectedIndex: Int = 0,
    modifier: Modifier = Modifier,
    onItemClick: (Int) -> Unit = {}
) {
    val items = listOf(
        BottomNavItemUi(
            label = "Início",
            icon = Icons.Rounded.Home
        ),
        BottomNavItemUi(
            label = "Pesquisar",
            icon = Icons.Rounded.Search
        ),
        BottomNavItemUi(
            label = "Favoritos",
            icon = Icons.Rounded.FavoriteBorder
        ),
        BottomNavItemUi(
            label = "Perfil",
            type = BottomNavItemType.Avatar
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        shape = RectangleShape,
        color = LaranjadaBottomBarBackground.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.05f)
                    )
                )
                .padding(horizontal = 34.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                BottomNavItem(
                    item = item,
                    selected = index == selectedIndex,
                    onClick = { onItemClick(index) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: BottomNavItemUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        when (item.type) {
            BottomNavItemType.Icon -> {
                Icon(
                    imageVector = requireNotNull(item.icon),
                    contentDescription = item.label,
                    tint = if (selected) LaranjadaOrange else LaranjadaMutedText,
                    modifier = Modifier.size(
                        if (selected) 28.dp else 26.dp
                    )
                )
            }

            BottomNavItemType.Avatar -> {
                ProfileAvatarPlaceholder(
                    selected = selected,
                    initial = "R"
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatarPlaceholder(
    selected: Boolean,
    initial: String
) {
    Box(
        modifier = Modifier
            .requiredSize(27.dp)
            .clip(CircleShape)
            .background(LaranjadaSurfaceLight.copy(alpha = 0.82f))
            .border(
                border = BorderStroke(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) {
                        LaranjadaOrange
                    } else {
                        LaranjadaMutedText.copy(alpha = 0.80f)
                    }
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.take(1).uppercase(),
            color = if (selected) LaranjadaOrange else LaranjadaText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 11.sp
        )
    }
}