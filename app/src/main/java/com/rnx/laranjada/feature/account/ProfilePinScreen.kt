package com.rnx.laranjada.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaOrange

@Composable
fun ProfilePinScreen(
    profileName: String,
    avatarUrl: String? = null,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onBackClick: () -> Unit,
    onSubmitClick: (String) -> Unit = {}
) {
    var pin by
    rememberSaveable {
        mutableStateOf(
            ""
        )
    }

    val canSubmit =
        pin.length in 4..6 &&
                !isSubmitting

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
                .imePadding()
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
                    460.dp

                else ->
                    maxWidth
            }

        val avatarSize =
            when {
                isExpanded ->
                    136.dp

                isTablet ->
                    124.dp

                else ->
                    108.dp
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
                                32.dp
                            } else {
                                18.dp
                            },

                        vertical =
                            if (
                                isTablet
                            ) {
                                28.dp
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
                    1.2.sp
            )

            Spacer(
                modifier =
                    Modifier.height(
                        if (
                            isTablet
                        ) {
                            42.dp
                        } else {
                            48.dp
                        }
                    )
            )

            ProfilePinCard(
                profileName =
                    profileName,

                avatarUrl =
                    avatarUrl,

                pin =
                    pin,

                isSubmitting =
                    isSubmitting,

                canSubmit =
                    canSubmit,

                errorMessage =
                    errorMessage,

                isTablet =
                    isTablet,

                avatarSize =
                    avatarSize,

                /*
                 * [ALTERAÇÃO]
                 *
                 * widthIn vem ANTES de fillMaxWidth.
                 *
                 * Assim o tablet limita primeiro
                 * a área disponível e só depois
                 * o card ocupa 100% dessa área.
                 */
                modifier =
                    Modifier
                        .widthIn(
                            max =
                                cardMaxWidth
                        )
                        .fillMaxWidth(),

                onPinChange = {
                        newValue ->

                    if (
                        newValue.length <=
                        6 &&
                        newValue.all {
                                character ->

                            character.isDigit()
                        }
                    ) {
                        pin =
                            newValue
                    }
                },

                onSubmitClick = {
                    if (
                        canSubmit
                    ) {
                        onSubmitClick(
                            pin
                        )
                    }
                },

                onBackClick =
                    onBackClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        if (
                            isTablet
                        ) {
                            46.dp
                        } else {
                            36.dp
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
                        bottom = 12.dp
                    )
            )
        }
    }
}

@Composable
private fun ProfilePinCard(
    profileName: String,
    avatarUrl: String?,
    pin: String,
    isSubmitting: Boolean,
    canSubmit: Boolean,
    errorMessage: String?,
    isTablet: Boolean,
    avatarSize: Dp,
    modifier: Modifier = Modifier,
    onPinChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onBackClick: () -> Unit
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
                            10.dp
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
                            0xFF28323C
                        ),

                    shape =
                        RoundedCornerShape(
                            if (
                                isTablet
                            ) {
                                16.dp
                            } else {
                                10.dp
                            }
                        )
                )
                .padding(
                    horizontal =
                        if (
                            isTablet
                        ) {
                            30.dp
                        } else {
                            14.dp
                        },

                    vertical =
                        if (
                            isTablet
                        ) {
                            32.dp
                        } else {
                            24.dp
                        }
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        ProfilePinAvatar(
            profileName =
                profileName,

            avatarUrl =
                avatarUrl,

            avatarSize =
                avatarSize
        )

        Spacer(
            modifier =
                Modifier.height(
                    if (
                        isTablet
                    ) {
                        18.dp
                    } else {
                        14.dp
                    }
                )
        )

        Text(
            text =
                profileName,

            color =
                Color.White,

            fontSize =
                if (
                    isTablet
                ) {
                    24.sp
                } else {
                    20.sp
                },

            fontWeight =
                FontWeight.ExtraBold,

            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        Text(
            text =
                "Este perfil está bloqueado. Digite o PIN para continuar.",

            color =
                Color.White.copy(
                    alpha = 0.72f
                ),

            fontSize =
                if (
                    isTablet
                ) {
                    14.sp
                } else {
                    12.sp
                },

            lineHeight =
                if (
                    isTablet
                ) {
                    20.sp
                } else {
                    17.sp
                },

            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(
                    if (
                        isTablet
                    ) {
                        28.dp
                    } else {
                        22.dp
                    }
                )
        )

        OutlinedTextField(
            value =
                pin,

            onValueChange =
                onPinChange,

            enabled =
                !isSubmitting,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        if (
                            isTablet
                        ) {
                            62.dp
                        } else {
                            58.dp
                        }
                    ),

            placeholder = {
                Text(
                    text =
                        "Digite o PIN do perfil",

                    color =
                        Color(
                            0xFF8F929A
                        ),

                    fontSize =
                        if (
                            isTablet
                        ) {
                            16.sp
                        } else {
                            15.sp
                        },

                    fontWeight =
                        FontWeight.Bold
                )
            },

            singleLine =
                true,

            visualTransformation =
                PasswordVisualTransformation(),

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.NumberPassword
                ),

            shape =
                RoundedCornerShape(
                    if (
                        isTablet
                    ) {
                        8.dp
                    } else {
                        5.dp
                    }
                ),

            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor =
                        Color.White,

                    unfocusedTextColor =
                        Color.White,

                    disabledTextColor =
                        Color.White.copy(
                            alpha = 0.65f
                        ),

                    focusedBorderColor =
                        Color.White,

                    unfocusedBorderColor =
                        Color.White.copy(
                            alpha = 0.72f
                        ),

                    cursorColor =
                        LaranjadaOrange,

                    focusedContainerColor =
                        Color.Transparent,

                    unfocusedContainerColor =
                        Color.Transparent,

                    disabledContainerColor =
                        Color.Transparent
                )
        )

        if (
            !errorMessage
                .isNullOrBlank()
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Text(
                text =
                    errorMessage,

                color =
                    Color(
                        0xFFFF7272
                    ),

                fontSize =
                    12.sp,

                textAlign =
                    TextAlign.Center
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    if (
                        isTablet
                    ) {
                        16.dp
                    } else {
                        12.dp
                    }
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
                            58.dp
                        } else {
                            54.dp
                        }
                    )
                    .clip(
                        RoundedCornerShape(
                            if (
                                isTablet
                            ) {
                                8.dp
                            } else {
                                5.dp
                            }
                        )
                    )
                    .background(
                        if (
                            canSubmit
                        ) {
                            Color(
                                0xFFF4F4F4
                            )
                        } else {
                            Color(
                                0xFF777777
                            )
                        }
                    )
                    .clickable(
                        enabled =
                            canSubmit,

                        onClick =
                            onSubmitClick
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
                            20.dp
                        ),

                    color =
                        Color.Black,

                    strokeWidth =
                        2.dp
                )
            } else {
                Text(
                    text =
                        "Entrar no perfil",

                    color =
                        Color.Black,

                    fontSize =
                        if (
                            isTablet
                        ) {
                            15.sp
                        } else {
                            14.sp
                        },

                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    if (
                        isTablet
                    ) {
                        24.dp
                    } else {
                        28.dp
                    }
                )
        )

        Text(
            text =
                "Voltar",

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

            modifier =
                Modifier
                    .clickable(
                        enabled =
                            !isSubmitting,

                        onClick =
                            onBackClick
                    )
                    .padding(
                        horizontal =
                            18.dp,

                        vertical =
                            8.dp
                    )
        )
    }
}

@Composable
private fun ProfilePinAvatar(
    profileName: String,
    avatarUrl: String?,
    avatarSize: Dp
) {
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
                        0xFF292A2F
                    )
                )
                .border(
                    width =
                        2.dp,

                    color =
                        Color.White.copy(
                            alpha = 0.12f
                        ),

                    shape =
                        CircleShape
                ),

        contentAlignment =
            Alignment.Center
    ) {
        if (
            !avatarUrl
                .isNullOrBlank()
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
                        avatarSize >=
                        120.dp
                    ) {
                        42.sp
                    } else {
                        36.sp
                    },

                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}