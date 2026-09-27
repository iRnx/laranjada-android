package com.rnx.laranjada.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaOrange

@Composable
fun EditProfileScreen(
    profileUuid: String,
    initialName: String,
    avatarUrl: String? = null,
    initialHasPin: Boolean = false,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onBackClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onSaveClick: (
        name: String,
        usePin: Boolean,
        pin: String
    ) -> Unit = { _, _, _ -> },
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var profileName by rememberSaveable(
        profileUuid,
        initialName
    ) {
        androidx.compose.runtime.mutableStateOf(
            initialName
        )
    }

    /*
     * Estado final desejado para o PIN.
     *
     * Perfil que já possui PIN:
     * true = manter/alterar
     * false = remover
     *
     * Perfil sem PIN:
     * false = continuar sem PIN
     * true = criar PIN
     */
    var usePin by rememberSaveable(
        profileUuid,
        initialHasPin
    ) {
        androidx.compose.runtime.mutableStateOf(
            initialHasPin
        )
    }

    /*
     * Só fica true quando um perfil que já
     * possui PIN escolhe explicitamente:
     *
     * "Alterar PIN"
     */
    var isChangingExistingPin by rememberSaveable(
        profileUuid,
        initialHasPin
    ) {
        androidx.compose.runtime.mutableStateOf(
            false
        )
    }

    var pin by rememberSaveable(
        profileUuid
    ) {
        androidx.compose.runtime.mutableStateOf(
            ""
        )
    }

    var confirmPin by rememberSaveable(
        profileUuid
    ) {
        androidx.compose.runtime.mutableStateOf(
            ""
        )
    }

    var localErrorMessage by rememberSaveable(
        profileUuid
    ) {
        androidx.compose.runtime.mutableStateOf<String?>(
            null
        )
    }

    val displayedError =
        localErrorMessage
            ?: errorMessage

    /*
     * Perfil sem PIN:
     * mostra os campos quando o switch
     * "Bloquear perfil" estiver ligado.
     *
     * Perfil com PIN:
     * mostra somente quando o usuário
     * clicar em "Alterar PIN".
     */
    val shouldShowPinFields =
        if (
            initialHasPin
        ) {
            isChangingExistingPin
        } else {
            usePin
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
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
                    28.dp
                )
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularBackButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(
                        Alignment.CenterStart
                    )
                )

                Text(
                    text = "Editar perfil",
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(
                    28.dp
                )
            )

            EditableAvatar(
                name = profileName,
                avatarUrl = avatarUrl,
                enabled = !isSubmitting,
                onClick = onAvatarClick
            )

            Spacer(
                modifier = Modifier.height(
                    12.dp
                )
            )

            Text(
                text = "Escolher avatar",
                color = LaranjadaOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(
                        Alignment.CenterHorizontally
                    )
                    .clickable(
                        enabled = !isSubmitting,
                        onClick = onAvatarClick
                    )
            )

            Spacer(
                modifier = Modifier.height(
                    30.dp
                )
            )

            Text(
                text = "Nome do perfil",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(
                    9.dp
                )
            )

            OutlinedTextField(
                value = profileName,
                onValueChange = { value ->
                    if (
                        value.length <= 40
                    ) {
                        profileName = value

                        localErrorMessage =
                            null
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        58.dp
                    ),
                singleLine = true,
                shape = RoundedCornerShape(
                    7.dp
                ),
                colors = OutlinedTextFieldDefaults.colors(
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
                            alpha = 0.72f
                        ),
                    disabledBorderColor =
                        Color.White.copy(
                            alpha = 0.35f
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
                modifier = Modifier.height(
                    24.dp
                )
            )

            Text(
                text = "Segurança do perfil",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(
                    12.dp
                )
            )

            if (
                initialHasPin
            ) {
                ExistingPinSecurityCard(
                    pinWillBeRemoved =
                        !usePin,
                    isChangingPin =
                        isChangingExistingPin,
                    enabled =
                        !isSubmitting,
                    onChangePinClick = {
                        usePin =
                            true

                        isChangingExistingPin =
                            true

                        pin =
                            ""

                        confirmPin =
                            ""

                        localErrorMessage =
                            null
                    },
                    onCancelChangeClick = {
                        usePin =
                            true

                        isChangingExistingPin =
                            false

                        pin =
                            ""

                        confirmPin =
                            ""

                        localErrorMessage =
                            null
                    },
                    onRemovePinClick = {
                        usePin =
                            false

                        isChangingExistingPin =
                            false

                        pin =
                            ""

                        confirmPin =
                            ""

                        localErrorMessage =
                            null
                    },
                    onKeepPinClick = {
                        usePin =
                            true

                        isChangingExistingPin =
                            false

                        pin =
                            ""

                        confirmPin =
                            ""

                        localErrorMessage =
                            null
                    }
                )
            } else {
                EnablePinCard(
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
            }

            if (
                shouldShowPinFields
            ) {
                Spacer(
                    modifier = Modifier.height(
                        16.dp
                    )
                )

                EditProfilePinField(
                    label =
                        if (
                            initialHasPin
                        ) {
                            "Novo PIN"
                        } else {
                            "PIN"
                        },
                    value =
                        pin,
                    placeholder =
                        "PIN de 4 a 6 números",
                    enabled =
                        !isSubmitting,
                    onValueChange = {
                            value ->

                        if (
                            value.length <= 6 &&
                            value.all {
                                    character ->

                                character.isDigit()
                            }
                        ) {
                            pin =
                                value

                            localErrorMessage =
                                null
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(
                        14.dp
                    )
                )

                EditProfilePinField(
                    label =
                        if (
                            initialHasPin
                        ) {
                            "Confirmar novo PIN"
                        } else {
                            "Confirmar PIN"
                        },
                    value =
                        confirmPin,
                    placeholder =
                        if (
                            initialHasPin
                        ) {
                            "Confirme o novo PIN"
                        } else {
                            "Confirme o PIN"
                        },
                    enabled =
                        !isSubmitting,
                    onValueChange = {
                            value ->

                        if (
                            value.length <= 6 &&
                            value.all {
                                    character ->

                                character.isDigit()
                            }
                        ) {
                            confirmPin =
                                value

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
                    modifier = Modifier.height(
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
                modifier = Modifier.height(
                    26.dp
                )
            )

            SaveProfileButton(
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

                        /*
                         * Perfil sem PIN e usuário
                         * acabou de ativar o bloqueio.
                         */
                        !initialHasPin &&
                                usePin &&
                                pin.isBlank() -> {
                            localErrorMessage =
                                "Informe um PIN para bloquear o perfil."
                        }

                        /*
                         * Perfil já possuía PIN
                         * e o usuário escolheu
                         * Alterar PIN.
                         */
                        initialHasPin &&
                                isChangingExistingPin &&
                                pin.isBlank() -> {
                            localErrorMessage =
                                "Informe o novo PIN."
                        }

                        shouldShowPinFields &&
                                pin.length !in 4..6 -> {
                            localErrorMessage =
                                "O PIN deve ter entre 4 e 6 números."
                        }

                        shouldShowPinFields &&
                                pin != confirmPin -> {
                            localErrorMessage =
                                "Os PINs não coincidem."
                        }

                        else -> {
                            localErrorMessage =
                                null

                            /*
                             * Quando o PIN não está
                             * sendo criado/trocado,
                             * mandamos String vazia.
                             *
                             * O NavGraph transforma
                             * isso em pin = null no
                             * PATCH.
                             */
                            val pinForRequest =
                                if (
                                    shouldShowPinFields
                                ) {
                                    pin
                                } else {
                                    ""
                                }

                            onSaveClick(
                                name,
                                usePin,
                                pinForRequest
                            )
                        }
                    }
                }
            )

            Spacer(
                modifier = Modifier.height(
                    12.dp
                )
            )

            CancelProfileButton(
                enabled =
                    !isSubmitting,
                onClick =
                    onCancelClick
            )

            Spacer(
                modifier = Modifier.height(
                    34.dp
                )
            )

            DeleteProfileCard(
                enabled =
                    !isSubmitting,
                onDeleteClick =
                    onDeleteClick
            )

            Spacer(
                modifier = Modifier.height(
                    54.dp
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
private fun CircularBackButton(
    onClick: () -> Unit,
    modifier: Modifier =
        Modifier
) {
    Box(
        modifier = modifier
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
private fun EditableAvatar(
    name: String,
    avatarUrl: String?,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier =
            Modifier.fillMaxWidth(),
        contentAlignment =
            Alignment.Center
    ) {
        Box(
            modifier = Modifier.clickable(
                enabled =
                    enabled,
                onClick =
                    onClick
            )
        ) {
            Box(
                modifier = Modifier
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
                        width = 3.dp,
                        color =
                            Color.White.copy(
                                alpha = 0.26f
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
                            name,
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
                            name
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
                            36.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(
                        Alignment.BottomEnd
                    )
                    .size(
                        34.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        Color.White
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Edit,
                    contentDescription =
                        "Escolher avatar",
                    tint =
                        Color.Black,
                    modifier =
                        Modifier.size(
                            17.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun EnablePinCard(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange:
        (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
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
                width = 1.dp,
                color =
                    Color.White.copy(
                        alpha = 0.14f
                    ),
                shape =
                    RoundedCornerShape(
                        8.dp
                    )
            )
            .padding(
                horizontal = 15.dp,
                vertical = 12.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(
                1f
            )
        ) {
            Text(
                text = "Bloquear perfil",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(
                    4.dp
                )
            )

            Text(
                text =
                    "Exija um PIN para entrar neste perfil.",
                color =
                    Color.White.copy(
                        alpha = 0.72f
                    ),
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            enabled = enabled,
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
private fun ExistingPinSecurityCard(
    pinWillBeRemoved: Boolean,
    isChangingPin: Boolean,
    enabled: Boolean,
    onChangePinClick: () -> Unit,
    onCancelChangeClick: () -> Unit,
    onRemovePinClick: () -> Unit,
    onKeepPinClick: () -> Unit
) {
    Column(
        modifier = Modifier
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
                width = 1.dp,
                color =
                    Color.White.copy(
                        alpha = 0.14f
                    ),
                shape =
                    RoundedCornerShape(
                        8.dp
                    )
            )
            .padding(
                16.dp
            )
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(
                        38.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        if (
                            pinWillBeRemoved
                        ) {
                            Color(
                                0xFF391414
                            )
                        } else {
                            Color(
                                0xFF202A20
                            )
                        }
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Lock,
                    contentDescription =
                        null,
                    tint =
                        if (
                            pinWillBeRemoved
                        ) {
                            Color(
                                0xFFFF7777
                            )
                        } else {
                            Color(
                                0xFF8BD58B
                            )
                        },
                    modifier =
                        Modifier.size(
                            19.dp
                        )
                )
            }

            Spacer(
                modifier = Modifier.size(
                    12.dp
                )
            )

            Column(
                modifier = Modifier.weight(
                    1f
                )
            ) {
                Text(
                    text =
                        if (
                            pinWillBeRemoved
                        ) {
                            "PIN será removido"
                        } else {
                            "PIN ativo"
                        },
                    color =
                        Color.White,
                    fontSize =
                        16.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Spacer(
                    modifier = Modifier.height(
                        3.dp
                    )
                )

                Text(
                    text =
                        when {
                            pinWillBeRemoved -> {
                                "O perfil ficará sem bloqueio após salvar."
                            }

                            isChangingPin -> {
                                "Digite o novo PIN nos campos abaixo."
                            }

                            else -> {
                                "Este perfil está protegido por PIN."
                            }
                        },
                    color =
                        Color.White.copy(
                            alpha = 0.68f
                        ),
                    fontSize =
                        12.sp,
                    lineHeight =
                        17.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(
                16.dp
            )
        )

        when {
            pinWillBeRemoved -> {
                SecurityActionButton(
                    text = "Manter PIN",
                    enabled = enabled,
                    destructive = false,
                    onClick = onKeepPinClick
                )
            }

            isChangingPin -> {
                SecurityActionButton(
                    text = "Cancelar alteração do PIN",
                    enabled = enabled,
                    destructive = false,
                    onClick = onCancelChangeClick
                )

                Spacer(
                    modifier = Modifier.height(
                        10.dp
                    )
                )

                SecurityActionButton(
                    text = "Remover PIN",
                    enabled = enabled,
                    destructive = true,
                    onClick = onRemovePinClick
                )
            }

            else -> {
                SecurityActionButton(
                    text = "Alterar PIN",
                    enabled = enabled,
                    destructive = false,
                    onClick = onChangePinClick
                )

                Spacer(
                    modifier = Modifier.height(
                        10.dp
                    )
                )

                SecurityActionButton(
                    text = "Remover PIN",
                    enabled = enabled,
                    destructive = true,
                    onClick = onRemovePinClick
                )
            }
        }
    }
}

@Composable
private fun SecurityActionButton(
    text: String,
    enabled: Boolean,
    destructive: Boolean,
    onClick: () -> Unit
) {
    val borderColor =
        if (
            destructive
        ) {
            Color(
                0xFFE14A4A
            )
        } else {
            Color.White.copy(
                alpha = 0.24f
            )
        }

    val textColor =
        if (
            destructive
        ) {
            Color(
                0xFFFF7777
            )
        } else {
            Color.White
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                46.dp
            )
            .clip(
                RoundedCornerShape(
                    6.dp
                )
            )
            .border(
                width = 1.dp,
                color =
                    borderColor.copy(
                        alpha =
                            if (
                                enabled
                            ) {
                                1f
                            } else {
                                0.35f
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
                onClick =
                    onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                text,
            color =
                textColor.copy(
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
                14.sp,
            fontWeight =
                FontWeight.ExtraBold
        )
    }
}

@Composable
private fun EditProfilePinField(
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
            modifier = Modifier.height(
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
                        KeyboardType.NumberPassword
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
                                alpha = 0.55f
                            ),
                        disabledBorderColor =
                            Color.White.copy(
                                alpha = 0.30f
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
private fun SaveProfileButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
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
                modifier = Modifier.size(
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
                    "Salvar alterações",
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
private fun CancelProfileButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
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

@Composable
private fun DeleteProfileCard(
    enabled: Boolean,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    8.dp
                )
            )
            .background(
                Color(
                    0xFF0E0E0E
                )
            )
            .border(
                width =
                    1.dp,
                color =
                    Color.White.copy(
                        alpha = 0.12f
                    ),
                shape =
                    RoundedCornerShape(
                        8.dp
                    )
            )
            .padding(
                16.dp
            )
    ) {
        Text(
            text =
                "Excluir perfil",
            color =
                Color.White,
            fontSize =
                16.sp,
            fontWeight =
                FontWeight.ExtraBold
        )

        Spacer(
            modifier = Modifier.height(
                5.dp
            )
        )

        Text(
            text =
                "Remove permanentemente este perfil e os dados ligados a ele.",
            color =
                Color.White.copy(
                    alpha = 0.68f
                ),
            fontSize =
                12.sp
        )

        Spacer(
            modifier = Modifier.height(
                18.dp
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    48.dp
                )
                .clip(
                    RoundedCornerShape(
                        6.dp
                    )
                )
                .background(
                    Color(
                        0xFF310B0B
                    )
                )
                .border(
                    width =
                        1.dp,
                    color =
                        Color(
                            0xFFE43131
                        ),
                    shape =
                        RoundedCornerShape(
                            6.dp
                        )
                )
                .clickable(
                    enabled =
                        enabled,
                    onClick =
                        onDeleteClick
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    "Excluir perfil",
                color =
                    Color(
                        0xFFFF6868
                    ).copy(
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
                    14.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}