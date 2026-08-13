package com.rnx.laranjada.feature.account

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.domain.model.AuthenticatedUser

@Composable
fun AccountScreen(
    user: AuthenticatedUser,
    isLoggingOut: Boolean,
    logoutErrorMessage: String?,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                LaranjadaBlack
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .padding(
                    horizontal = 20.dp
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Minha conta",
                    color = LaranjadaText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(
                modifier = Modifier.height(
                    24.dp
                )
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(
                    alpha = 0.06f
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                    14.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                        7.dp
                    )
                ) {
                    Text(
                        text = user.displayName,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "@${user.username}",
                        color = Color.White.copy(
                            alpha = 0.66f
                        ),
                        fontSize = 15.sp
                    )

                    Text(
                        text = user.email,
                        color = Color.White.copy(
                            alpha = 0.66f
                        ),
                        fontSize = 15.sp
                    )
                }
            }

            if (
                !logoutErrorMessage.isNullOrBlank()
            ) {
                Text(
                    text = logoutErrorMessage,
                    color = Color(0xFFFF9D9D),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(
                        top = 16.dp
                    )
                )
            }

            Button(
                onClick = onLogoutClick,
                enabled = !isLoggingOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 24.dp
                    )
                    .height(
                        54.dp
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LaranjadaOrange,
                    contentColor = Color.White
                )
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.height(
                            24.dp
                        ),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Logout,
                        contentDescription = null
                    )

                    Text(
                        text = "SAIR",
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(
                            start = 8.dp
                        )
                    )
                }
            }
        }
    }
}