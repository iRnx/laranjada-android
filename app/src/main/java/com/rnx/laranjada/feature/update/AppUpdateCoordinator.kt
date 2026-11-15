package com.rnx.laranjada.feature.update

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "LaranjadaUpdates"

/** Estado unico compartilhado pela inicializacao e por Configuracoes. */
object AppUpdateCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val lock = Any()
    private lateinit var appContext: Context
    private var task: Job? = null
    private var automaticChecked = false

    private val _state = MutableStateFlow(MobileUpdateState())
    val state: StateFlow<MobileUpdateState> = _state.asStateFlow()

    fun initialize(context: Context) {
        if (!::appContext.isInitialized) appContext = context.applicationContext
    }

    /** Verificacao automatica apenas no APK oficial de producao. */
    fun checkOnStartup() {
        if (!MobileUpdateConfig.officialBuild || automaticChecked) return
        automaticChecked = true
        check(manual = false)
    }

    fun check(manual: Boolean) {
        if (!::appContext.isInitialized) return
        if (!MobileUpdateConfig.officialBuild && !manual) return

        synchronized(lock) {
            if (task?.isActive == true || _state.value.phase == MobileUpdatePhase.DOWNLOADING) {
                return
            }
            if (_state.value.dialogVisible && !manual) return
            task = scope.launch {
                val installed = runCatching { MobileUpdateConfig.installedBuild(appContext) }
                    .getOrDefault(0L)
                _state.value = MobileUpdateState(
                    phase = MobileUpdatePhase.CHECKING,
                    installedBuild = installed
                )
                try {
                    val release = UpdateRepository.latest()
                    _state.value = when {
                        release == null -> MobileUpdateState(
                            phase = MobileUpdatePhase.NO_RELEASE,
                            installedBuild = installed,
                            message = "Nenhuma versao foi publicada ainda."
                        )
                        // Nas builds de teste, mostra o modal real como PREVIA.
                        // Nunca habilita download ou instalacao.
                        !MobileUpdateConfig.officialBuild -> MobileUpdateState(
                            phase = MobileUpdatePhase.AVAILABLE,
                            release = release,
                            installedBuild = installed,
                            dialogVisible = true,
                            message = "Visualizacao de teste. Instalacao desabilitada."
                        )
                        release.buildNumber <= installed -> MobileUpdateState(
                            phase = MobileUpdatePhase.UP_TO_DATE,
                            installedBuild = installed,
                            message = "A Laranjada ja esta atualizada."
                        )
                        else -> MobileUpdateState(
                            phase = MobileUpdatePhase.AVAILABLE,
                            release = release,
                            installedBuild = installed,
                            dialogVisible = true,
                            message = "Nova versao disponivel."
                        )
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Log.w(TAG, "UPDATE_CHECK_FAILED", error)
                    _state.value = MobileUpdateState(
                        phase = MobileUpdatePhase.ERROR,
                        installedBuild = installed,
                        message = "Nao foi possivel verificar atualizacoes. Tente novamente."
                    )
                }
            }
        }
    }

    fun download() {
        if (!::appContext.isInitialized || !MobileUpdateConfig.officialBuild) return
        val current = _state.value
        val release = current.release ?: return
        if (current.phase == MobileUpdatePhase.DOWNLOADING) return

        synchronized(lock) {
            task?.cancel()
            task = scope.launch {
                _state.value = current.copy(
                    phase = MobileUpdatePhase.DOWNLOADING,
                    progress = 0,
                    dialogVisible = true,
                    message = "Baixando atualizacao...",
                    apkPath = null
                )
                try {
                    val file = UpdateRepository.download(
                        cacheDir = appContext.cacheDir,
                        release = release,
                        onProgress = { percent ->
                            _state.value = _state.value.copy(progress = percent)
                        }
                    )
                    UpdateInstaller.validatePackage(appContext, file, release)
                    _state.value = _state.value.copy(
                        phase = MobileUpdatePhase.READY_TO_INSTALL,
                        progress = 100,
                        apkPath = file.absolutePath,
                        message = "Arquivo validado. Pronto para instalar."
                    )
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Log.e(TAG, "UPDATE_DOWNLOAD_FAILED", error)
                    _state.value = _state.value.copy(
                        phase = MobileUpdatePhase.ERROR,
                        message = error.message ?: "Nao foi possivel baixar a atualizacao.",
                        apkPath = null
                    )
                }
            }
        }
    }

    fun install() {
        if (!::appContext.isInitialized || !MobileUpdateConfig.officialBuild) return
        val current = _state.value
        val release = current.release ?: return
        val file = current.apkPath?.let(::File) ?: return

        synchronized(lock) {
            if (task?.isActive == true) return
            task = scope.launch {
                try {
                    UpdateInstaller.validatePackage(appContext, file, release)
                    val opened = UpdateInstaller.requestInstall(appContext, file)
                    _state.value = _state.value.copy(
                        message = if (opened) {
                            "Siga as instrucoes do Android para concluir a instalacao."
                        } else {
                            "Autorize a instalacao por esta fonte e volte para a TV. Depois, selecione Instalar."
                        }
                    )
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Log.e(TAG, "UPDATE_INSTALL_FAILED", error)
                    _state.value = _state.value.copy(
                        phase = MobileUpdatePhase.ERROR,
                        message = error.message ?: "Nao foi possivel iniciar a instalacao."
                    )
                }
            }
        }
    }

    /** Descarta estado antigo depois de uma mudanca de dominio. */
    fun onDomainChanged() {
        synchronized(lock) { task?.cancel() }
        automaticChecked = false
        _state.value = MobileUpdateState()
    }

    fun dismiss() {
        val current = _state.value
        // Preview de Debug/HML deve sempre poder fechar, mesmo com flag mandatory.
        if (current.mandatory && MobileUpdateConfig.officialBuild) return
        if (current.phase == MobileUpdatePhase.DOWNLOADING) {
            synchronized(lock) { task?.cancel() }
        }
        _state.value = current.copy(
            dialogVisible = false,
            phase = if (current.phase == MobileUpdatePhase.DOWNLOADING) {
                MobileUpdatePhase.AVAILABLE
            } else {
                current.phase
            }
        )
    }

    fun cancelDownload() {
        val current = _state.value
        if (current.phase != MobileUpdatePhase.DOWNLOADING) return
        synchronized(lock) { task?.cancel() }
        _state.value = current.copy(
            phase = MobileUpdatePhase.AVAILABLE,
            progress = 0,
            message = "Download cancelado."
        )
    }
}
