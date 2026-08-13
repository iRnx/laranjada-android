package com.rnx.laranjada.feature.auth

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.core.network.ApiConfig
import com.rnx.laranjada.domain.model.AuthenticatedUser

private val AuthBackground = Color(0xFF050505)
private val AuthCardBackground = Color(0xD6080C14)

@Composable
fun LoginScreen(
    onLoginSuccess: (AuthenticatedUser) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    val authenticatedUser = viewModel.authenticatedUser

    LaunchedEffect(authenticatedUser) {
        if (authenticatedUser != null) {
            onLoginSuccess(authenticatedUser)
            viewModel.consumeAuthenticatedUser()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            LaranjadaOrange.copy(
                                alpha = 0.18f
                            ),
                            LaranjadaOrange.copy(
                                alpha = 0.06f
                            ),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.systemBars
                )
                .imePadding()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 28.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(
                        max = 560.dp
                    ),
                shape = RoundedCornerShape(
                    16.dp
                ),
                color = AuthCardBackground,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(
                        alpha = 0.14f
                    )
                ),
                shadowElevation = 18.dp
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 26.dp
                    )
                ) {
                    Text(
                        text = "ENTRAR",
                        color = LaranjadaText,
                        fontSize = 38.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Entre com sua conta para continuar",
                        color = Color.White.copy(
                            alpha = 0.82f
                        ),
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(
                            top = 7.dp,
                            bottom = 20.dp
                        )
                    )

                    MobilePosters()

                    MobileBenefits()

                    if (
                        !uiState.errorMessage.isNullOrBlank()
                    ) {
                        LoginErrorAlert(
                            message = uiState.errorMessage
                        )
                    }

                    LoginFieldLabel(
                        text = "E-mail ou nome de usuário"
                    )

                    OutlinedTextField(
                        value = uiState.usernameOrEmail,
                        onValueChange = viewModel::onUsernameOrEmailChanged,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        singleLine = true,
                        isError = uiState.usernameError != null,
                        placeholder = {
                            Text(
                                text = "Digite seu e-mail ou usuário"
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(
                            9.dp
                        ),
                        colors = authTextFieldColors()
                    )

                    if (uiState.usernameError != null) {
                        LoginFieldError(
                            text = uiState.usernameError
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 20.dp,
                                bottom = 8.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Senha",
                            color = LaranjadaText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "Esqueci minha senha",
                            color = LaranjadaOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(
                                enabled = !uiState.isLoading
                            ) {
                                openWebPage(
                                    context = context,
                                    path = "/accounts/password/reset/"
                                )
                            }
                        )
                    }

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        singleLine = true,
                        isError = uiState.passwordError != null,
                        placeholder = {
                            Text(
                                text = "Digite sua senha"
                            )
                        },
                        visualTransformation = if (
                            uiState.passwordVisible
                        ) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = viewModel::togglePasswordVisibility
                            ) {
                                Icon(
                                    imageVector = if (
                                        uiState.passwordVisible
                                    ) {
                                        Icons.Rounded.Visibility
                                    } else {
                                        Icons.Rounded.VisibilityOff
                                    },
                                    contentDescription = if (
                                        uiState.passwordVisible
                                    ) {
                                        "Ocultar senha"
                                    } else {
                                        "Mostrar senha"
                                    },
                                    tint = Color.White.copy(
                                        alpha = 0.72f
                                    )
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.login()
                            }
                        ),
                        shape = RoundedCornerShape(
                            9.dp
                        ),
                        colors = authTextFieldColors()
                    )

                    if (uiState.passwordError != null) {
                        LoginFieldError(
                            text = uiState.passwordError
                        )
                    }

                    LoginButton(
                        isLoading = uiState.isLoading,
                        onClick = viewModel::login,
                        modifier = Modifier.padding(
                            top = 22.dp
                        )
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 22.dp,
                            bottom = 18.dp
                        ),
                        color = Color.White.copy(
                            alpha = 0.10f
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ainda não tem uma conta? ",
                            color = Color.White.copy(
                                alpha = 0.82f
                            ),
                            fontSize = 14.sp
                        )

                        Text(
                            text = "Criar conta",
                            color = LaranjadaOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.clickable(
                                enabled = !uiState.isLoading
                            ) {
                                openWebPage(
                                    context = context,
                                    path = "/accounts/signup/"
                                )
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )
        }
    }
}

@Composable
private fun MobilePosters() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = 14.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(
            9.dp
        )
    ) {
        listOf(
            "/static/assets/imgs/poster-01.webp",
            "/static/assets/imgs/poster-02.webp",
            "/static/assets/imgs/poster-03.webp"
        ).forEachIndexed { index, path ->
            AsyncImage(
                model = ApiConfig.buildUrl(path),
                contentDescription = "Poster ${index + 1}",
                modifier = Modifier
                    .weight(1f)
                    .height(94.dp)
                    .clip(
                        RoundedCornerShape(
                            10.dp
                        )
                    ),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
    }
}

@Composable
private fun MobileBenefits() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            )
            .padding(
                bottom = 22.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(
            8.dp
        )
    ) {
        BenefitChip(
            text = "Sem anúncios"
        )

        BenefitChip(
            text = "Full HD e 4K"
        )

        BenefitChip(
            text = "Downloads"
        )
    }
}

@Composable
private fun BenefitChip(
    text: String
) {
    Row(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color.White.copy(
                    alpha = 0.09f
                ),
                shape = RoundedCornerShape(
                    50.dp
                )
            )
            .background(
                color = Color.White.copy(
                    alpha = 0.045f
                ),
                shape = RoundedCornerShape(
                    50.dp
                )
            )
            .padding(
                horizontal = 11.dp,
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = LaranjadaOrange,
            modifier = Modifier.size(
                16.dp
            )
        )

        Text(
            text = text,
            color = Color.White.copy(
                alpha = 0.84f
            ),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                start = 5.dp
            )
        )
    }
}

@Composable
private fun LoginFieldLabel(
    text: String
) {
    Text(
        text = text,
        color = LaranjadaText,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(
            bottom = 8.dp
        )
    )
}

@Composable
private fun LoginFieldError(
    text: String
) {
    Text(
        text = text,
        color = Color(0xFFFF9D9D),
        fontSize = 13.sp,
        modifier = Modifier.padding(
            top = 6.dp
        )
    )
}

@Composable
private fun LoginErrorAlert(
    message: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = 18.dp
            ),
        shape = RoundedCornerShape(
            9.dp
        ),
        color = Color(0x2EA00000),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0x73FF5959)
        )
    ) {
        Text(
            text = message,
            color = Color(0xFFFFD8D8),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(
                14.dp
            )
        )
    }
}

@Composable
private fun LoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(
                RoundedCornerShape(
                    9.dp
                )
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF9800),
                        Color(0xFFFF7800)
                    )
                )
            )
            .clickable(
                enabled = !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(
                    24.dp
                ),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = "ENTRAR",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun authTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = LaranjadaOrange,
        unfocusedBorderColor = Color.White.copy(
            alpha = 0.18f
        ),
        errorBorderColor = Color(0xFFFF5959),
        focusedContainerColor = Color.Black.copy(
            alpha = 0.42f
        ),
        unfocusedContainerColor = Color.Black.copy(
            alpha = 0.26f
        ),
        cursorColor = LaranjadaOrange,
        focusedPlaceholderColor = LaranjadaMutedText,
        unfocusedPlaceholderColor = LaranjadaMutedText
    )

private fun openWebPage(
    context: Context,
    path: String
) {
    val url = ApiConfig.buildUrl(path)

    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        )
    } catch (_: ActivityNotFoundException) {
        // Nenhum navegador disponível.
    }
}