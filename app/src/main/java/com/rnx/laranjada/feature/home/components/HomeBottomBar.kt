
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaSurfaceLight
import com.rnx.laranjada.core.design.theme.LaranjadaText

private val BottomBarBackground =
    Color(0xFF101417)

/*
 * Contexto global da navegação.
 *
 * A MainActivity define o destino
 * selecionado e controla a troca
 * entre as telas principais.
 */
data class BottomBarNavigationContext(
    val selectedIndex: Int,
    val profileName: String,
    val profileAvatarUrl: String?,
    val searchEnabled: Boolean = false,
    val onNavigate: (Int) -> Unit
)

val LocalBottomBarNavigation =
    compositionLocalOf<BottomBarNavigationContext?> {
        null
    }

private enum class BottomNavItemType {
    Icon,
    Avatar
}

private data class BottomNavItemUi(
    val label: String,
    val icon: ImageVector? = null,
    val type: BottomNavItemType =
        BottomNavItemType.Icon
)

/*
 * Componente único da bottom bar.
 *
 * isHostBar = true:
 * a MainActivity desenha a barra.
 *
 * isHostBar = false:
 * barras antigas das telas não são
 * desenhadas quando há navegação global.
 *
 * Assim evitamos duas barras na tela.
 */
@Composable
fun HomeBottomBar(
    selectedIndex: Int = 0,
    profileName: String = "",
    profileAvatarUrl: String? = null,
    modifier: Modifier = Modifier,
    onItemClick: (Int) -> Unit = {},
    isHostBar: Boolean = false
) {
    val navigationContext =
        LocalBottomBarNavigation.current

    /*
     * Impede barras duplicadas.
     *
     * Home, Detail, Favoritos e Coleções
     * ainda possuem chamadas antigas.
     *
     * Não precisamos reescrever
     * essas telas agora.
     */
    if (
        navigationContext != null &&
        !isHostBar
    ) {
        return
    }

    val effectiveSelectedIndex =
        navigationContext?.selectedIndex
            ?: selectedIndex

    val effectiveProfileName =
        navigationContext?.profileName
            ?.takeIf { it.isNotBlank() }
            ?: profileName

    val effectiveAvatarUrl =
        navigationContext?.profileAvatarUrl
            ?: profileAvatarUrl

    val searchEnabled =
        navigationContext?.searchEnabled
            ?: true

    val items = remember {
        listOf(
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
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.navigationBars
            ),
        shape = RectangleShape,
        color = BottomBarBackground.copy(
            alpha = 0.96f
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(
                            alpha = 0.05f
                        )
                    )
                )
                .padding(
                    horizontal = 34.dp,
                    vertical = 12.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->

                val enabled =
                    index != 1 || searchEnabled

                BottomNavItem(
                    item = item,
                    selected =
                        index == effectiveSelectedIndex,
                    enabled = enabled,
                    profileName =
                        effectiveProfileName,
                    profileAvatarUrl =
                        effectiveAvatarUrl,
                    onClick = {
                        if (navigationContext != null) {
                            navigationContext.onNavigate(
                                index
                            )
                        } else {
                            onItemClick(index)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: BottomNavItemUi,
    selected: Boolean,
    enabled: Boolean,
    profileName: String,
    profileAvatarUrl: String?,
    onClick: () -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        when (item.type) {
            BottomNavItemType.Icon -> {
                Icon(
                    imageVector =
                        requireNotNull(item.icon),

                    contentDescription =
                        item.label,

                    tint = when {
                        !enabled ->
                            LaranjadaMutedText.copy(
                                alpha = 0.45f
                            )

                        selected ->
                            LaranjadaOrange

                        else ->
                            LaranjadaMutedText
                    },

                    modifier = Modifier.size(
                        if (selected) 28.dp else 26.dp
                    )
                )
            }

            BottomNavItemType.Avatar -> {
                ProfileAvatar(
                    selected = selected,
                    profileName = profileName,
                    avatarUrl = profileAvatarUrl
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    selected: Boolean,
    profileName: String,
    avatarUrl: String?
) {
    Box(
        modifier = Modifier
            .requiredSize(32.dp)
            .clip(CircleShape)
            .background(
                LaranjadaSurfaceLight.copy(
                    alpha = 0.82f
                )
            )
            .border(
                border = BorderStroke(
                    width =
                        if (selected) 2.dp else 1.dp,

                    color =
                        if (selected) {
                            LaranjadaOrange
                        } else {
                            LaranjadaMutedText.copy(
                                alpha = 0.80f
                            )
                        }
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription =
                    profileName.ifBlank {
                        "Perfil"
                    },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = profileName
                    .trim()
                    .take(1)
                    .uppercase()
                    .ifBlank { "?" },
                color =
                    if (selected) {
                        LaranjadaOrange
                    } else {
                        LaranjadaText
                    },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 11.sp
            )
        }
    }
}
