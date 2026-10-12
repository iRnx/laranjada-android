package com.rnx.laranjada.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaOrange

@Composable
fun DeleteProfileScreen(
    profileName: String,
    avatarUrl: String? = null,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit
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
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .windowInsetsPadding(
                    WindowInsets.navigationBars
                )
    ) {
        val isTablet =
            maxWidth >=
                    600.dp

        val isExpanded =
            maxWidth >=
                    840.dp

        val cardMaxWidth =
            when {
                isExpanded ->
                    500.dp

                isTablet ->
                    470.dp

                else ->
                    maxWidth
            }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
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
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally
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
                    Modifier.fillMaxWidth(),

                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(
                        when {
                            isExpanded ->
                                48.dp

                            isTablet ->
                                72.dp

                            else ->
                                118.dp
                        }
                    )
            )

            /*
             * [ALTERAÇÃO]
             *
             * Mesma lógica já validada
             * na tela de PIN:
             * limita antes e preenche depois.
             */
            DeleteConfirmationCard(
                profileName =
                    profileName,

                avatarUrl =
                    avatarUrl,

                isSubmitting =
                    isSubmitting,

                errorMessage =
                    errorMessage,

                isTablet =
                    isTablet,

                modifier =
                    Modifier
                        .widthIn(
                            max =
                                cardMaxWidth
                        )
                        .fillMaxWidth(),

                onConfirmClick =
                    onConfirmClick,

                onCancelClick =
                    onCancelClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        if (
                            isTablet
                        ) {
                            46.dp
                        } else {
                            56.dp
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
                    Modifier.padding(
                        bottom =
                            12.dp
                    )
            )
        }
    }
}

@Composable
private fun DeleteConfirmationCard(
    profileName: String,
    avatarUrl: String?,
    isSubmitting: Boolean,
    errorMessage: String?,
    isTablet: Boolean,
    modifier: Modifier =
        Modifier,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        if (
                            isTablet
                        ) {
                            16.dp
                        } else {
                            12.dp
                        }
                    )
                )
                .background(
                    Color(
                        0xFF02070C
                    )
                )
                .border(
                    width =
                        1.dp,

                    color =
                        Color(
                            0xFF24303B
                        ),

                    shape =
                        RoundedCornerShape(
                            if (
                                isTablet
                            ) {
                                16.dp
                            } else {
                                12.dp
                            }
                        )
                )
                .padding(
                    horizontal =
                        if (
                            isTablet
                        ) {
                            28.dp
                        } else {
                            16.dp
                        },

                    vertical =
                        if (
                            isTablet
                        ) {
                            30.dp
                        } else {
                            24.dp
                        }
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        if (
                            isTablet
                        ) {
                            110.dp
                        } else {
                            92.dp
                        }
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        Color(
                            0xFF2B2C31
                        )
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            if (
                !avatarUrl.isNullOrBlank()
            ) {
                AsyncImage(
                    model =
                        avatarUrl,

                    contentDescription =
                        profileName,

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
                        profileName
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
                            32.sp
                        },

                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    22.dp
                )
        )

        Text(
            text =
                "EXCLUIR PERFIL?",

            color =
                Color.White,

            fontSize =
                if (
                    isTablet
                ) {
                    28.sp
                } else {
                    25.sp
                },

            fontWeight =
                FontWeight.Black,

            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

        Text(
            text =
                "O perfil $profileName será removido permanentemente desta conta.",

            color =
                Color.White.copy(
                    alpha = 0.76f
                ),

            fontSize =
                if (
                    isTablet
                ) {
                    15.sp
                } else {
                    14.sp
                },

            lineHeight =
                if (
                    isTablet
                ) {
                    21.sp
                } else {
                    19.sp
                },

            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(
                    22.dp
                )
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            7.dp
                        )
                    )
                    .background(
                        Color(
                            0xFF211304
                        )
                    )
                    .border(
                        width =
                            1.dp,

                        color =
                            Color(
                                0xFF9B6200
                            ),

                        shape =
                            RoundedCornerShape(
                                7.dp
                            )
                    )
                    .padding(
                        horizontal =
                            14.dp,

                        vertical =
                            13.dp
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Warning,

                    contentDescription =
                        null,

                    tint =
                        Color(
                            0xFFFFA800
                        ),

                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )

                Text(
                    text =
                        "Essa ação não pode ser desfeita.",

                    color =
                        Color(
                            0xFFFFB129
                        ),

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    modifier =
                        Modifier.padding(
                            start =
                                8.dp
                        )
                )
            }
        }

        if (
            !errorMessage.isNullOrBlank()
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            Text(
                text =
                    errorMessage,

                color =
                    Color(
                        0xFFFF8E8E
                    ),

                fontSize =
                    13.sp,

                textAlign =
                    TextAlign.Center
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        if (
                            isTablet
                        ) {
                            56.dp
                        } else {
                            52.dp
                        }
                    )
                    .clip(
                        RoundedCornerShape(
                            6.dp
                        )
                    )
                    .background(
                        if (
                            isSubmitting
                        ) {
                            Color(
                                0xFF8B2528
                            )
                        } else {
                            Color(
                                0xFFF1252D
                            )
                        }
                    )
                    .clickable(
                        enabled =
                            !isSubmitting,

                        onClick =
                            onConfirmClick
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            if (
                isSubmitting
            ) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(
                            22.dp
                        ),

                    color =
                        Color.White,

                    strokeWidth =
                        2.dp
                )
            } else {
                Text(
                    text =
                        "Excluir definitivamente",

                    color =
                        Color.White,

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )

        Text(
            text =
                "Cancelar",

            color =
                Color.White.copy(
                    alpha =
                        if (
                            isSubmitting
                        ) {
                            0.45f
                        } else {
                            1f
                        }
                ),

            fontSize =
                13.sp,

            fontWeight =
                FontWeight.ExtraBold,

            modifier =
                Modifier.clickable(
                    enabled =
                        !isSubmitting,

                    onClick =
                        onCancelClick
                )
        )
    }
}