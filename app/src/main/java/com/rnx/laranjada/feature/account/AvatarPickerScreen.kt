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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
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
    isLoading: Boolean = false,
    isSubmitting: Boolean = false,
    submittingAvatarUuid: String? = null,
    errorMessage: String? = null,
    onBackClick: () -> Unit,
    onAvatarClick: (AvatarOptionUi) -> Unit = {},
    onRemoveAvatarClick: () -> Unit = {}
) {
    var searchQuery by rememberSaveable {
        mutableStateOf(
            ""
        )
    }

    val filteredAvatars =
        avatars.filter {
                avatar ->

            if (
                searchQuery.isBlank()
            ) {
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

    val groups =
        filteredAvatars.groupBy {
            it.collection
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush =
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(
                                0xFF160800
                            ),
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
            contentPadding =
                PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 18.dp,
                    bottom = 22.dp
                )
        ) {
            item {
                Text(
                    text =
                        "LARANJADA",
                    color =
                        LaranjadaOrange,
                    fontSize =
                        25.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    letterSpacing =
                        1.2.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = 34.dp
                        ),
                    textAlign =
                        TextAlign.Center
                )
            }

            item {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    AvatarBackButton(
                        enabled =
                            !isSubmitting,
                        onClick =
                            onBackClick
                    )

                    Column(
                        modifier =
                            Modifier.padding(
                                start = 16.dp
                            )
                    ) {
                        Text(
                            text =
                                "Escolha um avatar",
                            color =
                                Color.White,
                            fontSize =
                                26.sp,
                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    4.dp
                                )
                        )

                        Text(
                            text =
                                "Selecione um personagem para o perfil $profileName.",
                            color =
                                Color.White.copy(
                                    alpha = 0.72f
                                ),
                            fontSize =
                                13.sp
                        )
                    }
                }
            }

            item {
                Spacer(
                    modifier =
                        Modifier.height(
                            28.dp
                        )
                )

                OutlinedTextField(
                    value =
                        searchQuery,
                    onValueChange = {
                        if (
                            !isSubmitting
                        ) {
                            searchQuery =
                                it
                        }
                    },
                    enabled =
                        !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            54.dp
                        ),
                    placeholder = {
                        Text(
                            text =
                                "Pesquisar avatar ou coleção",
                            color =
                                Color(
                                    0xFF9699A1
                                ),
                            fontWeight =
                                FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Rounded.Search,
                            contentDescription =
                                null,
                            tint =
                                Color(
                                    0xFF8D96A6
                                ),
                            modifier =
                                Modifier.size(
                                    18.dp
                                )
                        )
                    },
                    singleLine =
                        true,
                    shape =
                        RoundedCornerShape(
                            28.dp
                        ),
                    colors =
                        OutlinedTextFieldDefaults
                            .colors(
                                focusedTextColor =
                                    Color.White,
                                unfocusedTextColor =
                                    Color.White,
                                disabledTextColor =
                                    Color.White.copy(
                                        alpha = 0.55f
                                    ),
                                focusedBorderColor =
                                    Color.White.copy(
                                        alpha = 0.24f
                                    ),
                                unfocusedBorderColor =
                                    Color.White.copy(
                                        alpha = 0.18f
                                    ),
                                disabledBorderColor =
                                    Color.White.copy(
                                        alpha = 0.10f
                                    ),
                                cursorColor =
                                    LaranjadaOrange,
                                focusedContainerColor =
                                    Color(
                                        0xFF121212
                                    ),
                                unfocusedContainerColor =
                                    Color(
                                        0xFF121212
                                    ),
                                disabledContainerColor =
                                    Color(
                                        0xFF121212
                                    )
                            )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            22.dp
                        )
                )
            }

            /*
             * Se o perfil já possui avatar,
             * oferecemos a API DELETE.
             */
            if (
                initialSelectedAvatarUuid != null
            ) {
                item {
                    RemoveAvatarButton(
                        enabled =
                            !isSubmitting,

                        isSubmitting =
                            isSubmitting &&
                                    submittingAvatarUuid ==
                                    null,

                        onClick =
                            onRemoveAvatarClick
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                24.dp
                            )
                    )
                }
            }

            if (
                !errorMessage.isNullOrBlank()
            ) {
                item {
                    Text(
                        text =
                            errorMessage,
                        color =
                            Color(
                                0xFFFF8E8E
                            ),
                        fontSize =
                            13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                bottom = 20.dp
                            ),
                        textAlign =
                            TextAlign.Center
                    )
                }
            }

            if (
                isLoading
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 48.dp
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color =
                                LaranjadaOrange
                        )
                    }
                }
            } else if (
                groups.isEmpty()
            ) {
                item {
                    Text(
                        text =
                            "Nenhum avatar encontrado.",
                        color =
                            Color.White.copy(
                                alpha = 0.70f
                            ),
                        fontSize =
                            14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 36.dp
                            ),
                        textAlign =
                            TextAlign.Center
                    )
                }
            } else {
                groups.forEach {
                        (
                            collection,
                            collectionAvatars
                        ) ->

                    item(
                        key =
                            "title_$collection"
                    ) {
                        Text(
                            text =
                                collection,
                            color =
                                Color.White,
                            fontSize =
                                18.sp,
                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    18.dp
                                )
                        )
                    }

                    item(
                        key =
                            "avatars_$collection"
                    ) {
                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    20.dp
                                )
                        ) {
                            items(
                                items =
                                    collectionAvatars,
                                key = {
                                    it.uuid
                                }
                            ) {
                                    avatar ->

                                val selected =
                                    avatar.uuid ==
                                            initialSelectedAvatarUuid

                                val submittingThisAvatar =
                                    isSubmitting &&
                                            avatar.uuid ==
                                            submittingAvatarUuid

                                AvatarOptionItem(
                                    avatar =
                                        avatar,

                                    selected =
                                        selected,

                                    enabled =
                                        !isSubmitting &&
                                                !selected,

                                    isSubmitting =
                                        submittingThisAvatar,

                                    onClick = {
                                        onAvatarClick(
                                            avatar
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(
                                    42.dp
                                )
                        )
                    }
                }
            }

            item {
                Spacer(
                    modifier =
                        Modifier.height(
                            28.dp
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
                        Modifier.fillMaxWidth(),
                    textAlign =
                        TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AvatarBackButton(
    enabled: Boolean,
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
                    alpha =
                        if (
                            enabled
                        ) {
                            0.09f
                        } else {
                            0.04f
                        }
                )
            )
            .clickable(
                enabled =
                    enabled,
                onClick =
                    onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Icon(
            imageVector =
                Icons.AutoMirrored
                    .Rounded
                    .ArrowBack,
            contentDescription =
                "Voltar",
            tint =
                Color.White.copy(
                    alpha =
                        if (
                            enabled
                        ) {
                            1f
                        } else {
                            0.45f
                        }
                ),
            modifier =
                Modifier.size(
                    20.dp
                )
        )
    }
}

@Composable
private fun RemoveAvatarButton(
    enabled: Boolean,
    isSubmitting: Boolean,
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
                50.dp
            )
            .clip(
                RoundedCornerShape(
                    7.dp
                )
            )
            .background(
                Color(
                    0xFF191919
                )
            )
            .border(
                width =
                    1.dp,
                color =
                    Color.White.copy(
                        alpha = 0.14f
                    ),
                shape =
                    RoundedCornerShape(
                        7.dp
                    )
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
        contentAlignment =
            Alignment.Center
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            if (
                isSubmitting
            ) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(
                            20.dp
                        ),
                    color =
                        Color.White,
                    strokeWidth =
                        2.dp
                )
            } else {
                Icon(
                    imageVector =
                        Icons.Rounded
                            .DeleteOutline,
                    contentDescription =
                        null,
                    tint =
                        Color(
                            0xFFFF7B7B
                        ),
                    modifier =
                        Modifier.size(
                            19.dp
                        )
                )

                Spacer(
                    modifier =
                        Modifier.size(
                            8.dp
                        )
                )

                Text(
                    text =
                        "Remover avatar atual",
                    color =
                        Color(
                            0xFFFFA0A0
                        ),
                    fontSize =
                        14.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun AvatarOptionItem(
    avatar: AvatarOptionUi,
    selected: Boolean,
    enabled: Boolean,
    isSubmitting: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Column(
        modifier = Modifier
            .size(
                width = 112.dp,
                height = 140.dp
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
        horizontalAlignment =
            Alignment.CenterHorizontally
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
                    width =
                        if (
                            selected
                        ) {
                            3.dp
                        } else {
                            2.dp
                        },
                    color =
                        if (
                            selected
                        ) {
                            LaranjadaOrange
                        } else {
                            Color.White.copy(
                                alpha = 0.14f
                            )
                        },
                    shape =
                        CircleShape
                ),
            contentAlignment =
                Alignment.Center
        ) {
            if (
                !avatar.imageUrl
                    .isNullOrBlank()
            ) {
                AsyncImage(
                    model =
                        avatar.imageUrl,
                    contentDescription =
                        avatar.name,
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
                        avatar.name
                            .trim()
                            .take(
                                1
                            )
                            .uppercase(),
                    color =
                        Color.White,
                    fontSize =
                        32.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }

            if (
                isSubmitting
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(
                                alpha = 0.52f
                            )
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                28.dp
                            ),
                        color =
                            Color.White,
                        strokeWidth =
                            2.dp
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    9.dp
                )
        )

        Text(
            text =
                avatar.name,
            color =
                Color.White.copy(
                    alpha =
                        if (
                            enabled ||
                            selected ||
                            isSubmitting
                        ) {
                            1f
                        } else {
                            0.55f
                        }
                ),
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