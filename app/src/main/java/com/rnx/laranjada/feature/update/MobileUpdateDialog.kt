package com.rnx.laranjada.feature.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

private val darkSurface = Color(0xFF18181B)

@Composable
fun MobileUpdateDialog(state: MobileUpdateState) {
    if (!state.dialogVisible) return

    val release = state.release
    val preview = !MobileUpdateConfig.officialBuild
    val downloading = state.phase == MobileUpdatePhase.DOWNLOADING
    val ready = state.phase == MobileUpdatePhase.READY_TO_INSTALL
    val failed = state.phase == MobileUpdatePhase.ERROR
    val blocking = state.mandatory && !preview

    val title = when {
        release == null -> "Verificar atualizações"
        preview -> "Prévia da atualização"
        blocking -> "Atualização necessária"
        else -> "Atualização disponível"
    }

    AlertDialog(
        onDismissRequest = {
            if (!blocking && !downloading) AppUpdateCoordinator.dismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !blocking && !downloading,
            dismissOnClickOutside = !blocking && !downloading
        ),
        containerColor = darkSurface,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (release != null) {
                    Text(
                        text = "Laranjada ${release.versionName}",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Novidades da versão",
                        color = Color(0xFFBDBDC1),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = release.changelog.ifBlank { "Melhorias e correções." },
                        color = Color.White
                    )
                    Text(
                        text = "Tamanho: ${formatFileSize(release.fileSize)}",
                        color = Color(0xFFBDBDC1)
                    )
                }

                if (downloading || ready) {
                    LinearProgressIndicator(
                        progress = { state.progress.coerceIn(0, 100) / 100f },
                        modifier = Modifier.fillMaxWidth().height(7.dp),
                        color = Color.White,
                        trackColor = Color(0xFF3A3A40)
                    )
                    Text("${state.progress}%", color = Color.White)
                }

                if (state.message.isNotBlank()) {
                    Text(
                        state.message,
                        color = if (failed) Color(0xFFFFB0A0) else Color(0xFFBDBDC1)
                    )
                }
                if (state.phase == MobileUpdatePhase.CHECKING) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        },
        confirmButton = {
            when {
                release == null || preview -> TextButton(
                    onClick = AppUpdateCoordinator::dismiss
                ) {
                    Text("Fechar", color = Color.White)
                }
                else -> Button(
                    onClick = {
                        when {
                            downloading -> AppUpdateCoordinator.cancelDownload()
                            ready -> AppUpdateCoordinator.install()
                            else -> AppUpdateCoordinator.download()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        when {
                            downloading -> "Cancelar download"
                            ready -> "Instalar APK"
                            failed -> "Tentar novamente"
                            else -> "Atualizar agora"
                        }
                    )
                }
            }
        },
        dismissButton = {
            if (release != null && !preview && !blocking && !downloading) {
                TextButton(onClick = AppUpdateCoordinator::dismiss) {
                    Text("Mais tarde", color = Color.White)
                }
            }
        }
    )
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1024L * 1024L * 1024L ->
        "%.1f GB".format(bytes.toDouble() / (1024.0 * 1024.0 * 1024.0))
    bytes >= 1024L * 1024L ->
        "%.1f MB".format(bytes.toDouble() / (1024.0 * 1024.0))
    bytes >= 1024L ->
        "%.1f KB".format(bytes.toDouble() / 1024.0)
    else -> "$bytes B"
}
