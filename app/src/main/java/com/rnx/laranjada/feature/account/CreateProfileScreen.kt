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
    onDoneClick: (
        name: String,
        usePin: Boolean,
        pin: String
    ) -> Unit = { _, _, _ -> }
) {
    var profileName by rememberSaveable {
        androidx.compose.runtime.mutableStateOf("")
    }

    var usePin by rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }

    var pin by rememberSaveable {
        androidx.compose.runtime.mutableStateOf("")
    }

    var confirmPin by rememberSaveable {
        androidx.compose.runtime.mutableStateOf("")
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
                    30.dp
                )
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                BackButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(
                        Alignment.CenterStart
                    )
                )

                Text(
                    text = "Criar perfil",
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
                    30.dp
                )
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ProfileAvatarPreview()
            }

            Spacer(
                modifier = Modifier.height(
                    10.dp
                )
            )

            Text(
                text = "O avatar poderá ser escolhido\ndepois que o perfil for criado.",
                color = Color(0xFFB6BCC5),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
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
                    10.dp
                )
            )

            OutlinedTextField(
                value = profileName,
                onValueChange = { newValue ->
                    if (newValue.length <= 40) {
                        profileName = newValue
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        58.dp
                    ),
                placeholder = {
                    Text(
                        text = "Nome do perfil",
                        color = Color(0xFF8F929A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(
                    7.dp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(
                        alpha = 0.70f
                    ),
                    cursorColor = LaranjadaOrange,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(
                modifier = Modifier.height(
                    24.dp
                )
            )

            Text(
                text = "Configurações do perfil",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(
                    12.dp
                )
            )

            PinSettingsCard(
                checked = usePin,
                onCheckedChange = { checked ->
                    usePin = checked

                    if (!checked) {
                        pin = ""
                        confirmPin = ""
                    }
                }
            )

            if (usePin) {
                Spacer(
                    modifier = Modifier.height(
                        16.dp
                    )
                )

                PinField(
                    label = "PIN",
                    value = pin,
                    placeholder = "PIN de 4 a 6 números",
                    onValueChange = { newValue ->
                        if (
                            newValue.length <= 6 &&
                            newValue.all { character ->
                                character.isDigit()
                            }
                        ) {
                            pin = newValue
                        }
                    }
                )

                Spacer(
                    modifier = Modifier.height(
                        14.dp
                    )
                )

                PinField(
                    label = "Confirmar PIN",
                    value = confirmPin,
                    placeholder = "Confirme o PIN",
                    onValueChange = { newValue ->
                        if (
                            newValue.length <= 6 &&
                            newValue.all { character ->
                                character.isDigit()
                            }
                        ) {
                            confirmPin = newValue
                        }
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(
                    26.dp
                )
            )

            PrimaryProfileButton(
                onClick = {
                    onDoneClick(
                        profileName.trim(),
                        usePin,
                        pin
                    )
                }
            )

            Spacer(
                modifier = Modifier.height(
                    12.dp
                )
            )

            SecondaryProfileButton(
                onClick = onCancelClick
            )

            /*
             * Apenas um pequeno respiro.
             * Não deixamos o espaço gigante que existe na versão Web.
             */
            Spacer(
                modifier = Modifier.height(
                    48.dp
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
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

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
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Voltar",
            tint = Color.White,
            modifier = Modifier.size(
                20.dp
            )
        )
    }
}

@Composable
private fun ProfileAvatarPreview() {
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
                width = 2.dp,
                color = Color.White.copy(
                    alpha = 0.22f
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(
                42.dp
            )
        )
    }
}

@Composable
private fun PinSettingsCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
                color = Color.White.copy(
                    alpha = 0.14f
                ),
                shape = RoundedCornerShape(
                    8.dp
                )
            )
            .padding(
                horizontal = 15.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
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
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(
                modifier = Modifier.height(
                    4.dp
                )
            )

            Text(
                text = "Exija um PIN para entrar neste perfil.",
                color = Color.White.copy(
                    alpha = 0.72f
                ),
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = LaranjadaOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(
                    0xFF606166
                ),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun PinField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(
            modifier = Modifier.height(
                8.dp
            )
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    58.dp
                ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(
                        0xFF8F929A
                    ),
                    fontSize = 15.sp
                )
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            shape = RoundedCornerShape(
                7.dp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = LaranjadaOrange,
                unfocusedBorderColor = Color.White.copy(
                    alpha = 0.55f
                ),
                cursorColor = LaranjadaOrange,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun PrimaryProfileButton(
    onClick: () -> Unit
) {
    val interactionSource = remember {
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
                    0xFFF4F4F4
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
            color = Color.Black,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SecondaryProfileButton(
    onClick: () -> Unit
) {
    val interactionSource = remember {
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
            .border(
                width = 1.dp,
                color = Color.White.copy(
                    alpha = 0.22f
                ),
                shape = RoundedCornerShape(
                    6.dp
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
            text = "Cancelar",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}