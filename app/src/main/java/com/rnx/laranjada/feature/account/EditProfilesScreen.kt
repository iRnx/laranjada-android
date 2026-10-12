package com.rnx.laranjada.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaOrange

@Composable
fun EditProfilesScreen(
    profiles: List<MenuProfileUi>,
    onProfileClick: (MenuProfileUi) -> Unit,
    onDoneClick: () -> Unit
) {
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color(
                                        0xFF160800
                                    ),
                                    Color.Black,
                                    Color.Black
                                )
                        )
                )
    ) {
        val isTablet =
            maxWidth >=
                    600.dp

        val isExpanded =
            maxWidth >=
                    840.dp

        val contentMaxWidth =
            when {
                isExpanded ->
                    760.dp

                isTablet ->
                    680.dp

                else ->
                    maxWidth
            }

        val avatarSize =
            when {
                isExpanded ->
                    118.dp

                isTablet ->
                    112.dp

                else ->
                    102.dp
            }

        val itemWidth =
            when {
                isExpanded ->
                    126.dp

                isTablet ->
                    120.dp

                else ->
                    108.dp
            }

        val editBadgeSize =
            when {
                isExpanded ->
                    30.dp

                isTablet ->
                    28.dp

                else ->
                    25.dp
            }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                    )
                    .windowInsetsPadding(
                        WindowInsets.navigationBars
                    )
                    .verticalScroll(
                        rememberScrollState()
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Column(
                modifier =
                    Modifier
                        .widthIn(
                            max =
                                contentMaxWidth
                        )
                        .fillMaxWidth()
                        .padding(
                            horizontal =
                                if (
                                    isTablet
                                ) {
                                    28.dp
                                } else {
                                    18.dp
                                },

                            vertical =
                                if (
                                    isTablet
                                ) {
                                    26.dp
                                } else {
                                    18.dp
                                }
                        )
            ) {
                Text(
                    text =
                        "LARANJADA",

                    color =
                        LaranjadaOrange,

                    fontSize =
                        if (
                            isTablet
                        ) {
                            29.sp
                        } else {
                            25.sp
                        },

                    fontWeight =
                        FontWeight.ExtraBold,

                    letterSpacing =
                        1.2.sp,

                    modifier =
                        Modifier.align(
                            Alignment.CenterHorizontally
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            if (
                                isTablet
                            ) {
                                54.dp
                            } else {
                                48.dp
                            }
                        )
                )

                Text(
                    text =
                        "Quem está assistindo?",

                    color =
                        Color.White,

                    fontSize =
                        if (
                            isTablet
                        ) {
                            30.sp
                        } else {
                            27.sp
                        },

                    fontWeight =
                        FontWeight.ExtraBold,

                    modifier =
                        Modifier.align(
                            Alignment.CenterHorizontally
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            if (
                                isTablet
                            ) {
                                42.dp
                            } else {
                                36.dp
                            }
                        )
                )

                LazyRow(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        if (
                            isTablet
                        ) {
                            Arrangement.spacedBy(
                                24.dp,
                                Alignment.CenterHorizontally
                            )
                        } else {
                            Arrangement.spacedBy(
                                18.dp
                            )
                        }
                ) {
                    items(
                        items =
                            profiles,

                        key = {
                            it.uuid
                        }
                    ) {
                            profile ->

                        EditableProfileItem(
                            profile =
                                profile,

                            avatarSize =
                                avatarSize,

                            itemWidth =
                                itemWidth,

                            editBadgeSize =
                                editBadgeSize,

                            isTablet =
                                isTablet,

                            onClick = {
                                onProfileClick(
                                    profile
                                )
                            }
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            if (
                                isTablet
                            ) {
                                54.dp
                            } else {
                                48.dp
                            }
                        )
                )

                Box(
                    modifier =
                        Modifier.fillMaxWidth(),

                    contentAlignment =
                        Alignment.Center
                ) {
                    DoneButton(
                        isTablet =
                            isTablet,

                        onClick =
                            onDoneClick
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            if (
                                isTablet
                            ) {
                                52.dp
                            } else {
                                58.dp
                            }
                        )
                )

                Text(
                    text =
                        "LARANJADA",

                    color =
                        LaranjadaOrange,

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    letterSpacing =
                        1.4.sp,

                    modifier =
                        Modifier
                            .align(
                                Alignment.CenterHorizontally
                            )
                            .padding(
                                bottom =
                                    16.dp
                            )
                )
            }
        }
    }
}

@Composable
private fun EditableProfileItem(
    profile: MenuProfileUi,
    avatarSize: Dp,
    itemWidth: Dp,
    editBadgeSize: Dp,
    isTablet: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Column(
        modifier =
            Modifier
                .width(
                    itemWidth
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
                modifier =
                    Modifier
                        .size(
                            avatarSize
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
                                3.dp,

                            color =
                                Color.White.copy(
                                    alpha =
                                        0.30f
                                ),

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
                                .take(
                                    1
                                )
                                .uppercase()
                                .ifBlank {
                                    "?"
                                },

                        color =
                            Color.White,

                        fontSize =
                            if (
                                isTablet
                            ) {
                                38.sp
                            } else {
                                34.sp
                            },

                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomEnd
                        )
                        .size(
                            editBadgeSize
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            LaranjadaOrange
                        ),

                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Edit,

                    contentDescription =
                        "Editar ${profile.name}",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            if (
                                isTablet
                            ) {
                                16.dp
                            } else {
                                14.dp
                            }
                        )
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    if (
                        isTablet
                    ) {
                        12.dp
                    } else {
                        10.dp
                    }
                )
        )

        Text(
            text =
                profile.name,

            color =
                Color.White,

            fontSize =
                if (
                    isTablet
                ) {
                    14.sp
                } else {
                    13.sp
                },

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
private fun DoneButton(
    isTablet: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Box(
        modifier =
            Modifier
                .width(
                    if (
                        isTablet
                    ) {
                        320.dp
                    } else {
                        260.dp
                    }
                )
                .height(
                    if (
                        isTablet
                    ) {
                        60.dp
                    } else {
                        58.dp
                    }
                )
                .clip(
                    RoundedCornerShape(
                        if (
                            isTablet
                        ) {
                            8.dp
                        } else {
                            6.dp
                        }
                    )
                )
                .background(
                    Color(
                        0xFF343434
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
                "Concluído",

            color =
                Color.White,

            fontSize =
                if (
                    isTablet
                ) {
                    17.sp
                } else {
                    16.sp
                },

            fontWeight =
                FontWeight.ExtraBold
        )
    }
}