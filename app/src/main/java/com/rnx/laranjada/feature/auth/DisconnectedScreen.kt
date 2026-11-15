package com.rnx.laranjada.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tela exclusiva para sessao encerrada remotamente.
 * Nao confunde revogacao de acesso com falha de internet ou logout voluntario.
 */
@Composable
fun DisconnectedScreen(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .padding(horizontal = 22.dp, vertical = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .background(Color(0xFF161618), RoundedCornerShape(20.dp))
                .padding(horizontal = 26.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Este dispositivo foi desconectado",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Sua sessão foi encerrada remotamente ou deixou de ser válida. " +
                    "Para continuar usando o Laranjada, entre novamente na sua conta.",
                color = Color(0xFFBFC0C5),
                fontSize = 15.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF141416)
                )
            ) {
                Text("ENTRAR NOVAMENTE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
