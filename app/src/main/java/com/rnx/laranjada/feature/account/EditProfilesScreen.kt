package com.rnx.laranjada.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF160800),
                        Color.Black,
                        Color.Black
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 18.dp,
                    vertical = 18.dp
                )
        ) {
            Text(
                text = "LARANJADA",
                color = LaranjadaOrange,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )

            Spacer(
                modifier = Modifier.height(
                    48.dp
                )
            )

            Text(
                text = "Quem está assistindo?",
                color = Color.White,
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )

            Spacer(
                modifier = Modifier.height(
                    36.dp
                )
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    18.dp
                )
            ) {
                items(
                    items = profiles,
                    key = {
                        it.uuid
                    }
                ) { profile ->
                    EditableProfileItem(
                        profile = profile,
                        onClick = {
                            onProfileClick(
                                profile
                            )
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(
                    48.dp
                )
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DoneButton(
                    onClick = onDoneClick
                )
            }

            /*
             * Espaço curto de propósito.
             * Não queremos aquele scroll enorme vazio da versão Web.
             */
            Spacer(
                modifier = Modifier.height(
                    58.dp
                )
            )

            Text(
                text = "LARANJADA",
                color = LaranjadaOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp,
                modifier = Modifier
                    .align(
                        Alignment.CenterHorizontally
                    )
                    .padding(
                        bottom = 16.dp
                    )
            )
        }
    }
}

@Composable
private fun EditableProfileItem(
    profile: MenuProfileUi,
    onClick: () -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    Column(
        modifier = Modifier
            .width(
                108.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(
                        102.dp
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
                        width = 3.dp,
                        color = Color.White.copy(
                            alpha = 0.30f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (
                    !profile.avatarUrl.isNullOrBlank()
                ) {
                    AsyncImage(
                        model = profile.avatarUrl,
                        contentDescription = profile.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(
                                CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = profile.name
                            .trim()
                            .take(1)
                            .uppercase()
                            .ifBlank {
                                "?"
                            },
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(
                        Alignment.BottomEnd
                    )
                    .size(
                        25.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        LaranjadaOrange
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Editar ${profile.name}",
                    tint = Color.White,
                    modifier = Modifier.size(
                        14.dp
                    )
                )
            }
        }

        Spacer(
            modifier = Modifier.height(
                10.dp
            )
        )

        Text(
            text = profile.name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DoneButton(
    onClick: () -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    Box(
        modifier = Modifier
            .width(
                260.dp
            )
            .height(
                58.dp
            )
            .clip(
                RoundedCornerShape(
                    6.dp
                )
            )
            .background(
                Color(
                    0xFF343434
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Concluído",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}