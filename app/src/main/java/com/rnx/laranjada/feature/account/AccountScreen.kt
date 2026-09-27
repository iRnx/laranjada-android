package com.rnx.laranjada.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.domain.model.AuthenticatedUser

data class MenuProfileUi(
    val uuid: String,
    val name: String,
    val avatarUrl: String? = null,
    val hasPin: Boolean = false,
    val isSelected: Boolean = false
)

@Composable
fun AccountScreen(
    user: AuthenticatedUser,
    isLoggingOut: Boolean,
    logoutErrorMessage: String?,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    profiles: List<MenuProfileUi> =
        emptyList(),
    canCreateProfile: Boolean =
        false,
    onProfileClick: (MenuProfileUi) -> Unit =
        {},
    onCreateProfileClick: () -> Unit =
        {},
    onEditProfilesClick: () -> Unit =
        {},
    onFavoritesClick: () -> Unit =
        {},
    onDownloadsClick: () -> Unit =
        {},
    onSubscriptionClick: () -> Unit =
        {},
    onSupportClick: () -> Unit =
        {},
    onSwitchProfileClick: () -> Unit =
        {},
    onAccountDetailsClick: () -> Unit =
        {},
    onSearchClick: () -> Unit =
        {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .windowInsetsPadding(
                    WindowInsets.navigationBars
                )
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 18.dp
                )
        ) {
            AccountMenuHeader(
                onMenuClick =
                    onBackClick,
                onSearchClick =
                    onSearchClick
            )

            Spacer(
                modifier = Modifier.height(
                    22.dp
                )
            )

            ProfilesSection(
                profiles =
                    profiles,
                canCreateProfile =
                    canCreateProfile,
                onProfileClick =
                    onProfileClick,
                onCreateProfileClick =
                    onCreateProfileClick
            )

            Spacer(
                modifier = Modifier.height(
                    34.dp
                )
            )

            EditProfilesButton(
                onClick =
                    onEditProfilesClick
            )

            Spacer(
                modifier = Modifier.height(
                    34.dp
                )
            )

            AccountMenuItem(
                title = "Favoritos",
                onClick = onFavoritesClick
            )

            AccountMenuItem(
                title = "Downloads",
                onClick = onDownloadsClick
            )

            AccountMenuItem(
                title = "Minha assinatura",
                onClick = onSubscriptionClick
            )

            AccountMenuItem(
                title = "Atendimento",
                onClick = onSupportClick
            )

            AccountMenuItem(
                title = "Trocar perfil",
                onClick = onSwitchProfileClick
            )

            AccountMenuItem(
                title = "Conta",
                onClick = onAccountDetailsClick
            )

            AccountMenuItem(
                title = "Sair",
                enabled = !isLoggingOut,
                showLoading = isLoggingOut,
                onClick = onLogoutClick
            )

            if (
                !logoutErrorMessage
                    .isNullOrBlank()
            ) {
                Text(
                    text =
                        logoutErrorMessage,
                    color =
                        Color(
                            0xFFFF9D9D
                        ),
                    fontSize =
                        13.sp,
                    modifier =
                        Modifier.padding(
                            top = 14.dp
                        )
                )
            }

            Spacer(
                modifier = Modifier.height(
                    26.dp
                )
            )

            Text(
                text =
                    "Versão: Android Mobile",
                color =
                    Color(
                        0xFF8E8E8E
                    ),
                fontSize =
                    14.sp
            )

            Spacer(
                modifier = Modifier.height(
                    72.dp
                )
            )

            Text(
                text = "LARANJADA",
                color = LaranjadaOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp,
                modifier = Modifier
                    .align(
                        Alignment.CenterHorizontally
                    )
                    .padding(
                        bottom = 22.dp
                    )
            )
        }
    }
}

@Composable
private fun AccountMenuHeader(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                68.dp
            )
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .align(
                    Alignment.CenterStart
                )
                .size(
                    48.dp
                )
        ) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier.size(
                    29.dp
                )
            )
        }

        Text(
            text = "LARANJADA",
            color = LaranjadaOrange,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.align(
                Alignment.Center
            )
        )

        IconButton(
            onClick = onSearchClick,
            modifier = Modifier
                .align(
                    Alignment.CenterEnd
                )
                .size(
                    48.dp
                )
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Pesquisar",
                tint = Color.White,
                modifier = Modifier.size(
                    30.dp
                )
            )
        }
    }
}

@Composable
private fun ProfilesSection(
    profiles: List<MenuProfileUi>,
    canCreateProfile: Boolean,
    onProfileClick: (MenuProfileUi) -> Unit,
    onCreateProfileClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                18.dp
            )
    ) {
        items(
            items = profiles,
            key = {
                it.uuid
            }
        ) { profile ->
            MenuProfileItem(
                profile = profile,
                onClick = {
                    onProfileClick(
                        profile
                    )
                }
            )
        }

        if (
            canCreateProfile
        ) {
            item(
                key =
                    "create_profile"
            ) {
                CreateProfileItem(
                    onClick =
                        onCreateProfileClick
                )
            }
        }
    }
}

@Composable
private fun MenuProfileItem(
    profile: MenuProfileUi,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Column(
        modifier = Modifier
            .width(
                72.dp
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication =
                    null,
                onClick =
                    onClick
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(
                        72.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        Color(
                            0xFF28282C
                        )
                    )
                    .border(
                        width =
                            if (
                                profile.isSelected
                            ) {
                                2.dp
                            } else {
                                1.dp
                            },
                        color =
                            if (
                                profile.isSelected
                            ) {
                                Color.White
                            } else {
                                Color.White.copy(
                                    alpha = 0.20f
                                )
                            },
                        shape =
                            CircleShape
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                if (
                    !profile.avatarUrl
                        .isNullOrBlank()
                ) {
                    AsyncImage(
                        model =
                            profile.avatarUrl,
                        contentDescription =
                            profile.name,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clip(
                                    CircleShape
                                ),
                        contentScale =
                            ContentScale.Crop
                    )
                } else {
                    Text(
                        text =
                            profile.name
                                .trim()
                                .take(1)
                                .uppercase()
                                .ifBlank {
                                    "?"
                                },
                        color =
                            Color.White,
                        fontSize =
                            25.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }

            if (
                profile.hasPin
            ) {
                Box(
                    modifier = Modifier
                        .align(
                            Alignment.BottomEnd
                        )
                        .size(
                            20.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            Color(
                                0xFF080808
                            )
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Lock,
                        contentDescription =
                            "Perfil bloqueado",
                        tint =
                            Color.White,
                        modifier =
                            Modifier.size(
                                10.dp
                            )
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(
                8.dp
            )
        )

        Text(
            text =
                profile.name,
            color =
                Color.White,
            fontSize =
                13.sp,
            fontWeight =
                FontWeight.ExtraBold,
            maxLines =
                1,
            overflow =
                TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CreateProfileItem(
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Column(
        modifier = Modifier
            .width(
                72.dp
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication =
                    null,
                onClick =
                    onClick
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(
                    72.dp
                )
                .clip(
                    CircleShape
                )
                .border(
                    width = 2.dp,
                    color =
                        Color.White.copy(
                            alpha = 0.27f
                        ),
                    shape =
                        CircleShape
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Add,
                contentDescription =
                    "Novo perfil",
                tint =
                    Color.White,
                modifier =
                    Modifier.size(
                        30.dp
                    )
            )
        }

        Spacer(
            modifier = Modifier.height(
                8.dp
            )
        )

        Text(
            text = "Novo",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun EditProfilesButton(
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                54.dp
            )
            .clip(
                RoundedCornerShape(
                    6.dp
                )
            )
            .background(
                Color(
                    0xFF35343D
                )
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication =
                    null,
                onClick =
                    onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                "EDITAR PERFIS",
            color =
                Color.White,
            fontSize =
                16.sp,
            fontWeight =
                FontWeight.ExtraBold,
            letterSpacing =
                0.6.sp
        )
    }
}

@Composable
private fun AccountMenuItem(
    title: String,
    enabled: Boolean = true,
    showLoading: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    59.dp
                )
                .clickable(
                    enabled =
                        enabled,
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    onClick =
                        onClick
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    title,
                color =
                    if (
                        enabled
                    ) {
                        LaranjadaText
                    } else {
                        LaranjadaMutedText
                    },
                fontSize =
                    17.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            if (
                showLoading
            ) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(
                            20.dp
                        ),
                    color =
                        LaranjadaOrange,
                    strokeWidth =
                        2.dp
                )
            } else {
                Icon(
                    imageVector =
                        Icons.Rounded
                            .ChevronRight,
                    contentDescription =
                        null,
                    tint =
                        Color(
                            0xFFB7D8E8
                        ),
                    modifier =
                        Modifier.size(
                            28.dp
                        )
                )
            }
        }

        HorizontalDivider(
            thickness =
                1.dp,
            color =
                Color.White.copy(
                    alpha = 0.12f
                )
        )
    }
}