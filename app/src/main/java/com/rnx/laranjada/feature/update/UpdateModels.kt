
package com.rnx.laranjada.feature.update

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import com.rnx.laranjada.BuildConfig
import com.rnx.laranjada.core.network.ApiConfig
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

/**
 * Release disponibilizada pela API de Downloads do Django.
 */
data class MobileAppRelease(
    val uuid: String,
    val versionName: String,
    val buildNumber: Long,
    val fileSize: Long,
    val sha256: String,
    val changelog: String,
    val mandatory: Boolean,
    val minimumSupportedBuild: Long?,
    val downloadUrl: String
)

enum class MobileUpdatePhase {
    IDLE,
    CHECKING,
    NO_RELEASE,
    UP_TO_DATE,
    AVAILABLE,
    DOWNLOADING,
    READY_TO_INSTALL,
    ERROR
}

data class MobileUpdateState(
    val phase: MobileUpdatePhase = MobileUpdatePhase.IDLE,
    val release: MobileAppRelease? = null,
    val installedBuild: Long = 0L,
    val progress: Int = 0,
    val message: String = "",
    val dialogVisible: Boolean = false,
    val apkPath: String? = null
) {
    val mandatory: Boolean
        get() = release?.let { item ->
            item.mandatory || (
                    item.minimumSupportedBuild != null &&
                            installedBuild < item.minimumSupportedBuild
                    )
        } == true
}

/**
 * Configuracao de atualizacoes da Laranjada Mobile.
 *
 * O slug e o identificador estavel do aplicativo
 * cadastrado no Django.
 *
 * Nao depende do nome exibido nem do dominio.
 *
 * A origem HTTP/HTTPS e sempre obtida do ApiConfig,
 * que acompanha o Domain Discovery.
 */
internal object MobileUpdateConfig {

    const val PLATFORM_SLUG = "android"

    /**
     * Apenas o APK oficial pode instalar atualizacoes.
     *
     * localDebug e hmlDebug podem consultar releases
     * e mostrar uma previa do modal.
     */
    val officialBuild: Boolean
        get() = ApiConfig.isProduction && !BuildConfig.DEBUG

    /**
     * Numero da build instalada.
     *
     * Corresponde ao versionCode do Gradle e ao
     * build_number cadastrado na AppRelease.
     */
    fun installedBuild(context: Context): Long {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            0
        )

        return PackageInfoCompat.getLongVersionCode(packageInfo)
    }

    /**
     * Converte a resposta oficial do Django.
     *
     * Resposta esperada:
     *
     * {
     *   "platform": "android",
     *   "requested_version": "latest",
     *   "available": true,
     *   "release": { ... }
     * }
     */
    fun parseRelease(json: JSONObject): MobileAppRelease? {

        val platform = json.optString("platform")

        require(platform == PLATFORM_SLUG) {
            "A API retornou uma plataforma diferente."
        }

        if (!json.optBoolean("available", false)) {
            return null
        }

        val release = json.optJSONObject("release")
            ?: throw IllegalArgumentException(
                "A resposta da release esta incompleta."
            )

        val uuid = release.optString("uuid")
        require(
            runCatching { UUID.fromString(uuid) }.isSuccess
        ) {
            "A release nao possui um UUID valido."
        }

        val versionName = release.optString("version_name")
        val buildNumber = release.optLong("build_number", 0L)
        val fileSize = release.optLong("file_size", 0L)

        val sha256 = release.optString("sha256")
            .lowercase(Locale.ROOT)

        val downloadUrl = release.optString("download_url")

        require(versionName.isNotBlank()) {
            "A versao publicada e invalida."
        }

        require(buildNumber > 0L) {
            "O numero da build publicada e invalido."
        }

        require(fileSize > 0L) {
            "O tamanho do arquivo publicado e invalido."
        }

        require(sha256.matches(Regex("[0-9a-f]{64}"))) {
            "O SHA-256 da release e invalido."
        }

        require(downloadUrl.isNotBlank()) {
            "A release nao possui URL de download."
        }

        val minimumSupportedBuild =
            if (release.isNull("minimum_supported_build")) {
                null
            } else {
                release.optLong("minimum_supported_build")
                    .takeIf { it > 0L }
            }

        return MobileAppRelease(
            uuid = uuid,
            versionName = versionName,
            buildNumber = buildNumber,
            fileSize = fileSize,
            sha256 = sha256,
            changelog = release.optString("changelog"),
            mandatory = release.optBoolean(
                "is_mandatory",
                false
            ),
            minimumSupportedBuild = minimumSupportedBuild,
            downloadUrl = downloadUrl
        )
    }
}
