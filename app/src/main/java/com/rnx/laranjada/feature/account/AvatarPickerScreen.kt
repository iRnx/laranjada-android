package com.rnx.laranjada.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

data class AvatarOptionUi(
    val uuid: String,
    val name: String,
    val collection: String,
    val imageUrl: String? = null
)

@Composable
fun AvatarPickerScreen(
    profileName: String,
    avatars: List<AvatarOptionUi>,
    initialSelectedAvatarUuid: String? = null,
    onBackClick: () -> Unit,
    onAvatarClick: (AvatarOptionUi) -> Unit = {}
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var selectedAvatarUuid by rememberSaveable(
        initialSelectedAvatarUuid
    ) {
        mutableStateOf(
            initialSelectedAvatarUuid
        )
    }

    val filteredAvatars = avatars.filter { avatar ->
        if (searchQuery.isBlank()) {
            true
        } else {
            avatar.name.contains(
                searchQuery,
                ignoreCase = true
            ) ||
                    avatar.collection.contains(
                        searchQuery,
                        ignoreCase = true
                    )
        }
    }

    val groups = filteredAvatars.groupBy {
        it.collection
    }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                ),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 18.dp,
                bottom = 22.dp
            )
        ) {
            item {
                Text(
                    text = "LARANJADA",
                    color = LaranjadaOrange,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = 34.dp
                        ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarBackButton(
                        onClick = onBackClick
                    )

                    Column(
                        modifier = Modifier.padding(
                            start = 16.dp
                        )
                    ) {
                        Text(
                            text = "Escolha um avatar",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(
                            modifier = Modifier.height(
                                4.dp
                            )
                        )

                        Text(
                            text = "Selecione um personagem para o perfil $profileName.",
                            color = Color.White.copy(
                                alpha = 0.72f
                            ),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item {
                Spacer(
                    modifier = Modifier.height(
                        28.dp
                    )
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            54.dp
                        ),
                    placeholder = {
                        Text(
                            text = "Pesquisar avatar ou coleção",
                            color = Color(
                                0xFF9699A1
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = Color(
                                0xFF8D96A6
                            ),
                            modifier = Modifier.size(
                                18.dp
                            )
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(
                        28.dp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(
                            alpha = 0.24f
                        ),
                        unfocusedBorderColor = Color.White.copy(
                            alpha = 0.18f
                        ),
                        cursorColor = LaranjadaOrange,
                        focusedContainerColor = Color(
                            0xFF121212
                        ),
                        unfocusedContainerColor = Color(
                            0xFF121212
                        )
                    )
                )

                Spacer(
                    modifier = Modifier.height(
                        28.dp
                    )
                )
            }

            groups.forEach { (collection, collectionAvatars) ->
                item(
                    key = "title_$collection"
                ) {
                    Text(
                        text = collection,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(
                        modifier = Modifier.height(
                            18.dp
                        )
                    )
                }

                item(
                    key = "avatars_$collection"
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(
                            20.dp
                        )
                    ) {
                        items(
                            items = collectionAvatars,
                            key = {
                                it.uuid
                            }
                        ) { avatar ->
                            AvatarOptionItem(
                                avatar = avatar,
                                selected = avatar.uuid ==
                                        selectedAvatarUuid,
                                onClick = {
                                    selectedAvatarUuid =
                                        avatar.uuid

                                    onAvatarClick(
                                        avatar
                                    )
                                }
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(
                            42.dp
                        )
                    )
                }
            }

            item {
                Spacer(
                    modifier = Modifier.height(
                        28.dp
                    )
                )

                Text(
                    text = "LARANJADA",
                    color = LaranjadaOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AvatarBackButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(
                42.dp
            )
            .clip(
                CircleShape
            )
            .background(
                Color.White.copy(
                    alpha = 0.09f
                )
            )
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector =
                Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Voltar",
            tint = Color.White,
            modifier = Modifier.size(
                20.dp
            )
        )
    }
}

@Composable
private fun AvatarOptionItem(
    avatar: AvatarOptionUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        androidx.compose.runtime.remember {
            MutableInteractionSource()
        }

    Column(
        modifier = Modifier
            .size(
                width = 112.dp,
                height = 140.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(
                    100.dp
                )
                .clip(
                    CircleShape
                )
                .background(
                    Color(
                        0xFF25262A
                    )
                )
                .border(
                    width = if (
                        selected
                    ) {
                        3.dp
                    } else {
                        2.dp
                    },
                    color = if (
                        selected
                    ) {
                        LaranjadaOrange
                    } else {
                        Color.White.copy(
                            alpha = 0.14f
                        )
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (
                !avatar.imageUrl.isNullOrBlank()
            ) {
                AsyncImage(
                    model = avatar.imageUrl,
                    contentDescription = avatar.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = avatar.name
                        .trim()
                        .take(1)
                        .uppercase(),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(
                9.dp
            )
        )

        Text(
            text = avatar.name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}