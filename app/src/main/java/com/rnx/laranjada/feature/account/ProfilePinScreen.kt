package com.rnx.laranjada.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
    var pin by rememberSaveable {
        mutableStateOf("")
    }

    val canSubmit =
        pin.length in 4..6 &&
                !isSubmitting

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
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
            .imePadding()
            .padding(
                horizontal = 18.dp,
                vertical = 18.dp
            )
    ) {
        Column(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = "LARANJADA",
                color = LaranjadaOrange,
                fontSize = 25.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            Spacer(
                modifier = Modifier.height(
                    48.dp
                )
            )

            ProfilePinCard(
                profileName =
                    profileName,
                avatarUrl =
                    avatarUrl,
                pin = pin,
                isSubmitting =
                    isSubmitting,
                canSubmit =
                    canSubmit,
                errorMessage =
                    errorMessage,
                onPinChange = {
                        newValue ->

                    if (
                        newValue.length <= 6 &&
                        newValue.all {
                                character ->
                            character.isDigit()
                        }
                    ) {
                        pin = newValue
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
        }

        Text(
            text = "LARANJADA",
            color = LaranjadaOrange,
            fontSize = 13.sp,
            fontWeight =
                FontWeight.ExtraBold,
            letterSpacing = 1.4.sp,
            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .padding(
                    bottom = 12.dp
                )
        )
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
    onPinChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    10.dp
                )
            )
            .background(
                Color(
                    0xFF02070C
                )
            )
            .border(
                width = 1.dp,
                color = Color(
                    0xFF28323C
                ),
                shape =
                    RoundedCornerShape(
                        10.dp
                    )
            )
            .padding(
                horizontal = 14.dp,
                vertical = 24.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        ProfilePinAvatar(
            profileName =
                profileName,
            avatarUrl =
                avatarUrl
        )

        Spacer(
            modifier = Modifier.height(
                14.dp
            )
        )

        Text(
            text = profileName,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight =
                FontWeight.ExtraBold,
            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(
                10.dp
            )
        )

        Text(
            text =
                "Este perfil está bloqueado. Digite o PIN para continuar.",
            color = Color.White.copy(
                alpha = 0.72f
            ),
            fontSize = 12.sp,
            lineHeight = 17.sp,
            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(
                22.dp
            )
        )

        OutlinedTextField(
            value = pin,
            onValueChange =
                onPinChange,
            enabled =
                !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    58.dp
                ),
            placeholder = {
                Text(
                    text =
                        "Digite o PIN do perfil",
                    color = Color(
                        0xFF8F929A
                    ),
                    fontSize = 15.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            },
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation(),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.NumberPassword
                ),
            shape =
                RoundedCornerShape(
                    5.dp
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
            !errorMessage.isNullOrBlank()
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Text(
                text = errorMessage,
                color = Color(
                    0xFFFF7272
                ),
                fontSize = 12.sp,
                textAlign =
                    TextAlign.Center
            )
        }

        Spacer(
            modifier = Modifier.height(
                12.dp
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    54.dp
                )
                .clip(
                    RoundedCornerShape(
                        5.dp
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
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text =
                        "Entrar no perfil",
                    color =
                        Color.Black,
                    fontSize = 14.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(
                28.dp
            )
        )

        Text(
            text = "Voltar",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight =
                FontWeight.ExtraBold,
            modifier = Modifier
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
    avatarUrl: String?
) {
    Box(
        modifier = Modifier
            .size(
                108.dp
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
                width = 2.dp,
                color =
                    Color.White.copy(
                        alpha = 0.12f
                    ),
                shape = CircleShape
            ),
        contentAlignment =
            Alignment.Center
    ) {
        if (
            !avatarUrl.isNullOrBlank()
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription =
                    profileName,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        CircleShape
                    ),
                contentScale =
                    ContentScale.Crop
            )
        } else {
            Text(
                text = profileName
                    .trim()
                    .take(1)
                    .uppercase()
                    .ifBlank {
                        "?"
                    },
                color = Color.White,
                fontSize = 36.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}