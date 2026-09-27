package com.rnx.laranjada.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rnx.laranjada.core.design.theme.LaranjadaOrange

@Composable
fun CreateProfileScreen(
    onBackClick: () -> Unit,
    onCancelClick: () -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onDoneClick: (
        name: String,
        usePin: Boolean,
        pin: String
    ) -> Unit = { _, _, _ -> }
) {
    var profileName by rememberSaveable {
        androidx.compose.runtime
            .mutableStateOf("")
    }

    var usePin by rememberSaveable {
        androidx.compose.runtime
            .mutableStateOf(false)
    }

    var pin by rememberSaveable {
        androidx.compose.runtime
            .mutableStateOf("")
    }

    var confirmPin by rememberSaveable {
        androidx.compose.runtime
            .mutableStateOf("")
    }

    var localErrorMessage
            by rememberSaveable {
                androidx.compose.runtime
                    .mutableStateOf<String?>(
                        null
                    )
            }

    val displayedError =
        localErrorMessage
            ?: errorMessage

    Box(
        modifier = Modifier
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
                    horizontal =
                        18.dp,
                    vertical =
                        18.dp
                )
        ) {
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
                modifier =
                    Modifier.align(
                        Alignment
                            .CenterHorizontally
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        30.dp
                    )
            )

            Box(
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                BackButton(
                    onClick =
                        onBackClick,
                    modifier =
                        Modifier.align(
                            Alignment
                                .CenterStart
                        )
                )

                Text(
                    text =
                        "Criar perfil",
                    color =
                        Color.White,
                    fontSize =
                        27.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    modifier =
                        Modifier.align(
                            Alignment.Center
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        30.dp
                    )
            )

            Box(
                modifier =
                    Modifier.fillMaxWidth(),
                contentAlignment =
                    Alignment.Center
            ) {
                ProfileAvatarPreview()
            }

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Text(
                text =
                    "O avatar poderá ser escolhido\ndepois que o perfil for criado.",
                color =
                    Color(
                        0xFFB6BCC5
                    ),
                fontSize =
                    12.sp,
                lineHeight =
                    17.sp,
                modifier =
                    Modifier.align(
                        Alignment
                            .CenterHorizontally
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        30.dp
                    )
            )

            Text(
                text =
                    "Nome do perfil",
                color =
                    Color.White,
                fontSize =
                    15.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            OutlinedTextField(
                value =
                    profileName,
                onValueChange = {
                        newValue ->

                    if (
                        newValue.length <=
                        40
                    ) {
                        profileName =
                            newValue

                        localErrorMessage =
                            null
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            58.dp
                        ),
                enabled =
                    !isSubmitting,
                placeholder = {
                    Text(
                        text =
                            "Nome do perfil",
                        color =
                            Color(
                                0xFF8F929A
                            ),
                        fontSize =
                            16.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                },
                singleLine =
                    true,
                shape =
                    RoundedCornerShape(
                        7.dp
                    ),
                colors =
                    OutlinedTextFieldDefaults
                        .colors(
                            focusedTextColor =
                                Color.White,
                            unfocusedTextColor =
                                Color.White,
                            disabledTextColor =
                                Color.White,
                            focusedBorderColor =
                                Color.White,
                            unfocusedBorderColor =
                                Color.White.copy(
                                    alpha =
                                        0.70f
                                ),
                            disabledBorderColor =
                                Color.White.copy(
                                    alpha =
                                        0.35f
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

            Spacer(
                modifier =
                    Modifier.height(
                        24.dp
                    )
            )

            Text(
                text =
                    "Configurações do perfil",
                color =
                    Color.White,
                fontSize =
                    14.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            PinSettingsCard(
                checked =
                    usePin,
                enabled =
                    !isSubmitting,
                onCheckedChange = {
                        checked ->

                    usePin =
                        checked

                    localErrorMessage =
                        null

                    if (
                        !checked
                    ) {
                        pin =
                            ""

                        confirmPin =
                            ""
                    }
                }
            )

            if (
                usePin
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                PinField(
                    label =
                        "PIN",
                    value =
                        pin,
                    placeholder =
                        "PIN de 4 a 6 números",
                    enabled =
                        !isSubmitting,
                    onValueChange = {
                            newValue ->

                        if (
                            newValue.length <=
                            6 &&
                            newValue.all {
                                    character ->

                                character
                                    .isDigit()
                            }
                        ) {
                            pin =
                                newValue

                            localErrorMessage =
                                null
                        }
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                PinField(
                    label =
                        "Confirmar PIN",
                    value =
                        confirmPin,
                    placeholder =
                        "Confirme o PIN",
                    enabled =
                        !isSubmitting,
                    onValueChange = {
                            newValue ->

                        if (
                            newValue.length <=
                            6 &&
                            newValue.all {
                                    character ->

                                character
                                    .isDigit()
                            }
                        ) {
                            confirmPin =
                                newValue

                            localErrorMessage =
                                null
                        }
                    }
                )
            }

            if (
                !displayedError
                    .isNullOrBlank()
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                Text(
                    text =
                        displayedError,
                    color =
                        Color(
                            0xFFFF8E8E
                        ),
                    fontSize =
                        13.sp,
                    lineHeight =
                        18.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        26.dp
                    )
            )

            PrimaryProfileButton(
                enabled =
                    !isSubmitting,
                isLoading =
                    isSubmitting,
                onClick = {
                    val name =
                        profileName
                            .trim()

                    when {
                        name.isBlank() -> {
                            localErrorMessage =
                                "Informe o nome do perfil."
                        }

                        usePin &&
                                pin.length !in
                                4..6 -> {
                            localErrorMessage =
                                "O PIN deve ter entre 4 e 6 números."
                        }

                        usePin &&
                                pin !=
                                confirmPin -> {
                            localErrorMessage =
                                "Os PINs não coincidem."
                        }

                        else -> {
                            localErrorMessage =
                                null

                            onDoneClick(
                                name,
                                usePin,
                                if (
                                    usePin
                                ) {
                                    pin
                                } else {
                                    ""
                                }
                            )
                        }
                    }
                }
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            SecondaryProfileButton(
                enabled =
                    !isSubmitting,
                onClick =
                    onCancelClick
            )

            Spacer(
                modifier =
                    Modifier.height(
                        48.dp
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
                            Alignment
                                .CenterHorizontally
                        )
                        .padding(
                            bottom =
                                16.dp
                        )
            )
        }
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier =
        Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Box(
        modifier =
            modifier
                .size(
                    42.dp
                )
                .clip(
                    CircleShape
                )
                .background(
                    Color.White.copy(
                        alpha =
                            0.09f
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
        Icon(
            imageVector =
                Icons.AutoMirrored
                    .Rounded
                    .ArrowBack,
            contentDescription =
                "Voltar",
            tint =
                Color.White,
            modifier =
                Modifier.size(
                    20.dp
                )
        )
    }
}

@Composable
private fun ProfileAvatarPreview() {
    Box(
        modifier =
            Modifier
                .size(
                    112.dp
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
                            alpha =
                                0.22f
                        ),
                    shape =
                        CircleShape
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Icon(
            imageVector =
                Icons.Rounded.Person,
            contentDescription =
                null,
            tint =
                Color.White,
            modifier =
                Modifier.size(
                    42.dp
                )
        )
    }
}

@Composable
private fun PinSettingsCard(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange:
        (Boolean) -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        8.dp
                    )
                )
                .background(
                    Color(
                        0xFF131313
                    )
                )
                .border(
                    width =
                        1.dp,
                    color =
                        Color.White.copy(
                            alpha =
                                0.14f
                        ),
                    shape =
                        RoundedCornerShape(
                            8.dp
                        )
                )
                .padding(
                    horizontal =
                        15.dp,
                    vertical =
                        12.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {
            Text(
                text =
                    "Bloquear perfil",
                color =
                    Color.White,
                fontSize =
                    16.sp,
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
                    "Exija um PIN para entrar neste perfil.",
                color =
                    Color.White.copy(
                        alpha =
                            0.72f
                    ),
                fontSize =
                    12.sp
            )
        }

        Switch(
            checked =
                checked,
            enabled =
                enabled,
            onCheckedChange =
                onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor =
                        Color.White,
                    checkedTrackColor =
                        LaranjadaOrange,
                    uncheckedThumbColor =
                        Color.White,
                    uncheckedTrackColor =
                        Color(
                            0xFF606166
                        ),
                    uncheckedBorderColor =
                        Color.Transparent
                )
        )
    }
}

@Composable
private fun PinField(
    label: String,
    value: String,
    placeholder: String,
    enabled: Boolean,
    onValueChange:
        (String) -> Unit
) {
    Column {
        Text(
            text =
                label,
            color =
                Color.White,
            fontSize =
                14.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

        OutlinedTextField(
            value =
                value,
            onValueChange =
                onValueChange,
            enabled =
                enabled,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        58.dp
                    ),
            placeholder = {
                Text(
                    text =
                        placeholder,
                    color =
                        Color(
                            0xFF8F929A
                        ),
                    fontSize =
                        15.sp
                )
            },
            singleLine =
                true,
            visualTransformation =
                PasswordVisualTransformation(),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType
                            .NumberPassword
                ),
            shape =
                RoundedCornerShape(
                    7.dp
                ),
            colors =
                OutlinedTextFieldDefaults
                    .colors(
                        focusedTextColor =
                            Color.White,
                        unfocusedTextColor =
                            Color.White,
                        disabledTextColor =
                            Color.White,
                        focusedBorderColor =
                            LaranjadaOrange,
                        unfocusedBorderColor =
                            Color.White.copy(
                                alpha =
                                    0.55f
                            ),
                        disabledBorderColor =
                            Color.White.copy(
                                alpha =
                                    0.30f
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
    }
}

@Composable
private fun PrimaryProfileButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Box(
        modifier =
            Modifier
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
                    if (
                        enabled
                    ) {
                        Color(
                            0xFFF4F4F4
                        )
                    } else {
                        Color(
                            0xFF909090
                        )
                    }
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
        if (
            isLoading
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        22.dp
                    ),
                color =
                    Color.Black,
                strokeWidth =
                    2.dp
            )
        } else {
            Text(
                text =
                    "Concluído",
                color =
                    Color.Black,
                fontSize =
                    15.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SecondaryProfileButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    54.dp
                )
                .clip(
                    RoundedCornerShape(
                        6.dp
                    )
                )
                .border(
                    width =
                        1.dp,
                    color =
                        Color.White.copy(
                            alpha =
                                if (
                                    enabled
                                ) {
                                    0.22f
                                } else {
                                    0.10f
                                }
                        ),
                    shape =
                        RoundedCornerShape(
                            6.dp
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
        Text(
            text =
                "Cancelar",
            color =
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
            fontSize =
                15.sp,
            fontWeight =
                FontWeight.ExtraBold
        )
    }
}