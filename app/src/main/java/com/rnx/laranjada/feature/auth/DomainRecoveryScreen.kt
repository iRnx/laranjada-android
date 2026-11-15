package com.rnx.laranjada.feature.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import com.rnx.laranjada.core.network.DomainRecoveryPhase
import com.rnx.laranjada.core.network.DomainRecoveryStatus

@Composable
fun DomainRecoveryScreen(status: DomainRecoveryStatus, onRetry: () -> Unit) {
    BackHandler { /* Mantém o usuário fora de telas dependentes da API. */ }
    val title = when (status.phase) {
        DomainRecoveryPhase.NO_NETWORK -> "Sem conexão com a internet"
        DomainRecoveryPhase.SECURITY_ERROR -> "Conexão segura indisponível"
        else -> "Reconectando o Laranjada"
    }
    val message = when (status.phase) {
        DomainRecoveryPhase.NO_NETWORK -> "Verifique seu Wi-Fi ou dados móveis."
        DomainRecoveryPhase.SECURITY_ERROR -> "Não foi possível validar a assinatura do servidor de descoberta."
        DomainRecoveryPhase.CHECKING_CURRENT -> "Verificando a conexão com o servidor..."
        DomainRecoveryPhase.WAITING_FOR_DOMAIN -> "Aguardando um endereço disponível. Não é necessário atualizar o aplicativo."
        else -> "Procurando uma conexão válida automaticamente..."
    }
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF050505)).padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = Color.White, fontSize = 23.sp,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(message, color = Color(0xFFAAAAAE), fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 18.dp, bottom = 26.dp).widthIn(max = 500.dp))
        Button(onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White,
                contentColor = Color.Black)) {
            Text("Tentar novamente")
        }
    }
}
