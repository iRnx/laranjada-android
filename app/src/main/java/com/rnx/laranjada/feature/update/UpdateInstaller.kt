package com.rnx.laranjada.feature.update

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/** A instalacao e solicitada ao Android; nunca e silenciosa. */
internal object UpdateInstaller {

    /** Evita instalar APK de outro pacote, versao ou conteudo. */
    suspend fun validatePackage(
        context: Context,
        file: File,
        release: MobileAppRelease
    ) = withContext(Dispatchers.IO) {
        require(file.exists() && file.isFile && file.length() == release.fileSize) {
            "Arquivo de instalacao incompleto ou ausente."
        }
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                digest.update(buffer, 0, n)
            }
        }
        val hash = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
        require(hash.equals(release.sha256, ignoreCase = true)) {
            "Falha na verificacao de integridade do APK."
        }
        @Suppress("DEPRECATION")
        val packageInfo = context.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
            ?: throw IllegalArgumentException("O arquivo nao e um APK Android valido.")
        require(packageInfo.packageName == context.packageName) {
            "Este APK nao pertence a Laranjada Mobile instalada."
        }
        require(PackageInfoCompat.getLongVersionCode(packageInfo) == release.buildNumber) {
            "A build do arquivo nao corresponde a release consultada."
        }
        require(release.buildNumber > MobileUpdateConfig.installedBuild(context)) {
            "Este APK nao e uma versao mais recente."
        }
    }

    /** Retorna false quando o usuario precisa autorizar esta origem nas Configuracoes. */
    fun requestInstall(context: Context, file: File): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val permissionIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(permissionIntent)
            return false
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.updates.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            clipData = ClipData.newUri(context.contentResolver, "Laranjada Mobile", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }
}
