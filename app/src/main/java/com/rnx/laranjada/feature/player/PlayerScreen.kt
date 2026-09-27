package com.rnx.laranjada.feature.player

import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.core.playback.PlaybackHttpDataSourceFactory
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(
    UnstableApi::class
)
@Composable
fun PlayerScreen(
    contentType: String,
    uuid: String,
    modifier: Modifier = Modifier,
    seriesUuid: String = "",
    onBackClick: () -> Unit = {},
    playerViewModel: PlayerViewModel =
        viewModel()
) {
    var preferLandscape by remember {
        mutableStateOf(
            true
        )
    }

    var orientationLocked by remember {
        mutableStateOf(
            false
        )
    }

    val playbackState =
        playerViewModel
            .playbackUiState

    val context =
        LocalContext.current

    PlayerImmersiveMode(
        preferLandscape =
            preferLandscape,

        orientationLocked =
            orientationLocked
    )

    LaunchedEffect(
        contentType,
        uuid
    ) {
        playerViewModel
            .ensureInitialPlayback(
                contentType =
                    contentType,

                contentUuid =
                    uuid
            )
    }

    /*
     * Se uma troca de episódio falhar,
     * mantemos o vídeo anterior tocando
     * e apenas avisamos o usuário.
     */
    LaunchedEffect(
        playbackState
            .reserveErrorMessage,
        playbackState
            .reservation
            ?.sessionUuid
    ) {
        if (
            playbackState.reservation !=
            null &&
            !playbackState
                .reserveErrorMessage
                .isNullOrBlank()
        ) {
            Toast.makeText(
                context,
                playbackState
                    .reserveErrorMessage,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun closePlayer() {
        /*
         * Nesta Fase 1 limpamos apenas
         * o estado local.
         *
         * O POST /stop/ entra na
         * próxima dupla de APIs.
         */
        playerViewModel
            .clearLocalPlayback()

        onBackClick()
    }

    BackHandler {
        closePlayer()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                LaranjadaBlack
            )
    ) {
        val reservation =
            playbackState
                .reservation

        when {
            reservation ==
                    null &&
                    playbackState.isReserving -> {
                SecurePlaybackLoading()
            }

            reservation ==
                    null &&
                    !playbackState
                        .reserveErrorMessage
                        .isNullOrBlank() -> {
                SecurePlaybackError(
                    message =
                        playbackState
                            .reserveErrorMessage
                            ?: "Não foi possível autorizar a reprodução.",

                    onRetryClick = {
                        playerViewModel
                            .retryLastPlayback()
                    },

                    onBackClick = {
                        closePlayer()
                    }
                )
            }

            reservation ==
                    null -> {
                SecurePlaybackLoading()
            }

            else -> {
                HlsPlayer(
                    playbackUrl =
                        reservation
                            .playback
                            .url,

                    activeContentUuid =
                        reservation
                            .contentUuid,

                    seriesUuid =
                        seriesUuid,

                    playerViewModel =
                        playerViewModel,

                    isSwitchingPlayback =
                        playbackState
                            .isReserving,

                    fatalAuthorizationError =
                        if (
                            playbackState
                                .fatalAuthorizationError
                        ) {
                            playbackState
                                .renewErrorMessage
                                ?: "A reprodução não está mais autorizada."
                        } else {
                            null
                        },

                    preferLandscape =
                        preferLandscape,

                    orientationLocked =
                        orientationLocked,

                    onToggleOrientation = {
                        preferLandscape =
                            !preferLandscape
                    },

                    onToggleOrientationLock = {
                        orientationLocked =
                            !orientationLocked
                    },

                    onBackClick = {
                        closePlayer()
                    },

                    modifier =
                        Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun SecurePlaybackLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                LaranjadaBlack
            ),

        contentAlignment =
            Alignment.Center
    ) {
        CircularProgressIndicator(
            color =
                LaranjadaOrange
        )
    }
}

@Composable
private fun SecurePlaybackError(
    message: String,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                LaranjadaBlack
            )
            .padding(
                24.dp
            ),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                "Não foi possível iniciar a reprodução.",

            color =
                LaranjadaText
        )

        Text(
            text =
                message,

            color =
                LaranjadaText,

            modifier =
                Modifier.padding(
                    top = 10.dp
                )
        )

        Button(
            onClick =
                onRetryClick,

            modifier =
                Modifier.padding(
                    top = 22.dp
                ),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        LaranjadaOrange,

                    contentColor =
                        Color.White
                )
        ) {
            Text(
                text =
                    "Tentar novamente"
            )
        }

        Button(
            onClick =
                onBackClick,

            modifier =
                Modifier.padding(
                    top = 10.dp
                )
        ) {
            Text(
                text =
                    "Voltar"
            )
        }
    }
}

@OptIn(
    UnstableApi::class
)
@Composable
private fun HlsPlayer(
    playbackUrl: String,
    activeContentUuid: String,
    seriesUuid: String,
    playerViewModel: PlayerViewModel,
    isSwitchingPlayback: Boolean,
    fatalAuthorizationError: String?,
    preferLandscape: Boolean,
    orientationLocked: Boolean,
    onToggleOrientation: () -> Unit,
    onToggleOrientationLock: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context =
        LocalContext.current

    var currentEpisodeUuid by remember {
        mutableStateOf(
            activeContentUuid
        )
    }

    val episodesUiState =
        playerViewModel
            .episodesUiState

    val isLoadingEpisodes =
        playerViewModel
            .isLoadingEpisodes

    val episodesErrorMessage =
        playerViewModel
            .episodesErrorMessage

    var controlsVisible by remember {
        mutableStateOf(
            true
        )
    }

    var isPlaying by remember {
        mutableStateOf(
            false
        )
    }

    var isBuffering by remember {
        mutableStateOf(
            false
        )
    }

    var playerErrorMessage by remember {
        mutableStateOf<String?>(
            null
        )
    }

    var currentPositionMs by remember {
        mutableLongStateOf(
            0L
        )
    }

    var durationMs by remember {
        mutableLongStateOf(
            0L
        )
    }

    var isDraggingProgress by remember {
        mutableStateOf(
            false
        )
    }

    var draggedProgress by remember {
        mutableFloatStateOf(
            0f
        )
    }

    var currentCues by remember {
        mutableStateOf<List<Cue>>(
            emptyList()
        )
    }

    var subtitleSettings by remember {
        mutableStateOf(
            loadSubtitleVisualSettings(
                context
            )
        )
    }

    var audioOptions by remember {
        mutableStateOf(
            listOf(
                PlayerTrackOption(
                    id =
                        AUTO_AUDIO_ID,

                    label =
                        "Automático",

                    isAuto =
                        true
                )
            )
        )
    }

    var subtitleOptions by remember {
        mutableStateOf(
            listOf(
                PlayerTrackOption(
                    id =
                        SUBTITLE_OFF_ID,

                    label =
                        "Desligada",

                    isOff =
                        true
                )
            )
        )
    }

    var selectedAudioId by remember {
        mutableStateOf(
            AUTO_AUDIO_ID
        )
    }

    var selectedSubtitleId by remember {
        mutableStateOf(
            SUBTITLE_OFF_ID
        )
    }

    var playbackSpeed by remember {
        mutableFloatStateOf(
            1f
        )
    }

    var openedMenu by remember {
        mutableStateOf<
                TrackMenuType?
                >(
            null
        )
    }

    var playerViewForCapture by remember {
        mutableStateOf<
                PlayerView?
                >(
            null
        )
    }

    fun updateSubtitleSettings(
        newSettings:
        SubtitleVisualSettings
    ) {
        val fixedSettings =
            newSettings.coerced()

        subtitleSettings =
            fixedSettings

        saveSubtitleVisualSettings(
            context,
            fixedSettings
        )
    }

    fun resetSubtitleSettings() {
        val defaultSettings =
            resetSubtitleVisualSettings(
                context
            )

        subtitleSettings =
            defaultSettings
    }

    /*
     * Esta factory vive durante a vida
     * da instância do player.
     *
     * Ela não contém um token congelado.
     * Consulta o AuthorizationStore
     * em cada request.
     */
    val playbackDataSourceFactory =
        remember(
            playerViewModel
                .authorizationStore
        ) {
            PlaybackHttpDataSourceFactory(
                authorizationStore =
                    playerViewModel
                        .authorizationStore
            )
        }

    val exoPlayer =
        remember {
            val loadControl =
                DefaultLoadControl
                    .Builder()
                    .setBufferDurationsMs(
                        1_500,
                        30_000,
                        500,
                        1_000
                    )
                    .build()

            ExoPlayer.Builder(
                context
            )
                .setLoadControl(
                    loadControl
                )
                .build()
                .apply {
                    trackSelectionParameters =
                        trackSelectionParameters
                            .buildUpon()
                            .setTrackTypeDisabled(
                                C.TRACK_TYPE_TEXT,
                                true
                            )
                            .build()
                }
        }

    DisposableEffect(
        exoPlayer
    ) {
        val listener =
            object :
                Player.Listener {

                override fun onIsPlayingChanged(
                    isPlayingValue: Boolean
                ) {
                    isPlaying =
                        isPlayingValue
                }

                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {
                    isBuffering =
                        playbackState ==
                                Player.STATE_BUFFERING

                    durationMs =
                        exoPlayer
                            .safeDuration()
                }

                override fun onTracksChanged(
                    tracks: Tracks
                ) {
                    audioOptions =
                        buildAudioOptions(
                            tracks
                        )

                    subtitleOptions =
                        buildSubtitleOptions(
                            tracks
                        )
                }

                override fun onCues(
                    cueGroup: CueGroup
                ) {
                    currentCues =
                        cueGroup.cues
                }

                override fun onPlayerError(
                    error:
                    PlaybackException
                ) {
                    playerErrorMessage =
                        error.message
                            ?: "Falha ao carregar a mídia."
                }
            }

        exoPlayer.addListener(
            listener
        )

        audioOptions =
            buildAudioOptions(
                exoPlayer.currentTracks
            )

        subtitleOptions =
            buildSubtitleOptions(
                exoPlayer.currentTracks
            )

        onDispose {
            exoPlayer.removeListener(
                listener
            )
        }
    }

    /*
     * Troca a source somente quando
     * a URL de playback realmente muda.
     *
     * Renew normal devolve a mesma URL,
     * então esta parte NÃO executa no
     * simples Renew do token.
     */
    LaunchedEffect(
        playbackUrl,
        activeContentUuid
    ) {
        playerErrorMessage =
            null

        currentCues =
            emptyList()

        currentEpisodeUuid =
            activeContentUuid

        currentPositionMs =
            0L

        draggedProgress =
            0f

        durationMs =
            0L

        val mediaItem =
            MediaItem.fromUri(
                playbackUrl
            )

        val mediaSource =
            HlsMediaSource
                .Factory(
                    playbackDataSourceFactory
                )
                .createMediaSource(
                    mediaItem
                )

        exoPlayer.setMediaSource(
            mediaSource
        )

        exoPlayer.prepare()

        exoPlayer.setPlaybackSpeed(
            playbackSpeed
        )

        exoPlayer.playWhenReady =
            true

        exoPlayer.play()
    }

    LaunchedEffect(
        fatalAuthorizationError
    ) {
        if (
            !fatalAuthorizationError
                .isNullOrBlank()
        ) {
            exoPlayer.pause()
        }
    }

    DisposableEffect(
        exoPlayer
    ) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(
        exoPlayer
    ) {
        while (
            true
        ) {
            currentPositionMs =
                exoPlayer
                    .currentPosition
                    .coerceAtLeast(
                        0L
                    )

            durationMs =
                exoPlayer
                    .safeDuration()

            isPlaying =
                exoPlayer
                    .isPlaying

            delay(
                500.milliseconds
            )
        }
    }

    LaunchedEffect(
        currentPositionMs,
        isDraggingProgress
    ) {
        if (
            !isDraggingProgress
        ) {
            draggedProgress =
                currentPositionMs
                    .toFloat()
        }
    }

    LaunchedEffect(
        controlsVisible,
        isPlaying,
        isDraggingProgress,
        openedMenu
    ) {
        if (
            controlsVisible &&
            isPlaying &&
            !isDraggingProgress &&
            openedMenu ==
            null
        ) {
            delay(
                3.seconds
            )

            controlsVisible =
                false
        }
    }

    LaunchedEffect(
        seriesUuid
    ) {
        if (
            seriesUuid.isNotBlank()
        ) {
            playerViewModel
                .loadSeriesEpisodes(
                    seriesUuid
                )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Color.Black
            )
    ) {
        AndroidView(
            modifier =
                Modifier.fillMaxSize(),

            factory = {
                    viewContext ->

                PlayerView(
                    viewContext
                ).apply {
                    player =
                        exoPlayer

                    useController =
                        false

                    keepScreenOn =
                        true

                    resizeMode =
                        AspectRatioFrameLayout
                            .RESIZE_MODE_FIT

                    subtitleView
                        ?.visibility =
                        android.view.View.GONE

                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams
                                .MATCH_PARENT,

                            ViewGroup.LayoutParams
                                .MATCH_PARENT
                        )

                    playerViewForCapture =
                        this
                }
            },

            update = {
                    playerView ->

                playerView.player =
                    exoPlayer

                playerView
                    .subtitleView
                    ?.visibility =
                    android.view.View.GONE

                playerViewForCapture =
                    playerView
            }
        )

        CustomSubtitleOverlay(
            cues =
                currentCues,

            settings =
                subtitleSettings,

            modifier =
                Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(
                    Unit
                ) {
                    detectTapGestures(
                        onTap = {
                            controlsVisible =
                                !controlsVisible
                        }
                    )
                }
        )

        if (
            isBuffering ||
            isSwitchingPlayback
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier.align(
                        Alignment.Center
                    ),

                color =
                    LaranjadaOrange,

                trackColor =
                    Color.White.copy(
                        alpha = 0.18f
                    )
            )
        }

        AnimatedVisibility(
            visible =
                controlsVisible,

            enter =
                fadeIn(),

            exit =
                fadeOut(),

            modifier =
                Modifier.fillMaxSize()
        ) {
            PlayerControlsOverlay(
                isPlaying =
                    isPlaying,

                currentPositionMs =
                    currentPositionMs,

                durationMs =
                    durationMs,

                draggedProgress =
                    draggedProgress,

                playbackSpeed =
                    playbackSpeed,

                preferLandscape =
                    preferLandscape,

                orientationLocked =
                    orientationLocked,

                onCloseClick =
                    onBackClick,

                onToggleOrientationClick =
                    onToggleOrientation,

                onToggleOrientationLockClick =
                    onToggleOrientationLock,

                onSpeedClick = {
                    openedMenu =
                        TrackMenuType.SPEED

                    controlsVisible =
                        true
                },

                onScreenshotClick = {
                    val playerView =
                        playerViewForCapture

                    if (
                        playerView ==
                        null
                    ) {
                        Toast.makeText(
                            context,
                            "Player ainda não está pronto para capturar.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        capturePlayerFrame(
                            context =
                                context,

                            playerView =
                                playerView
                        )
                    }
                },

                onPictureInPictureClick = {
                    controlsVisible =
                        false

                    enterPictureInPictureMode(
                        context
                    )
                },

                onDraggedProgressChange = {
                        value ->

                    isDraggingProgress =
                        true

                    draggedProgress =
                        value
                },

                onDragFinished = {
                    exoPlayer.seekTo(
                        draggedProgress
                            .toLong()
                    )

                    isDraggingProgress =
                        false
                },

                onPlayPauseClick = {
                    if (
                        exoPlayer.isPlaying
                    ) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                },

                onReplayClick = {
                    val newPosition =
                        (
                                exoPlayer
                                    .currentPosition -
                                        10_000L
                                )
                            .coerceAtLeast(
                                0L
                            )

                    exoPlayer.seekTo(
                        newPosition
                    )
                },

                onForwardClick = {
                    val maxDuration =
                        exoPlayer
                            .safeDuration()

                    val newPosition =
                        if (
                            maxDuration > 0L
                        ) {
                            (
                                    exoPlayer
                                        .currentPosition +
                                            10_000L
                                    )
                                .coerceAtMost(
                                    maxDuration
                                )
                        } else {
                            exoPlayer
                                .currentPosition +
                                    10_000L
                        }

                    exoPlayer.seekTo(
                        newPosition
                    )
                },

                onAudioClick = {
                    openedMenu =
                        TrackMenuType.AUDIO

                    controlsVisible =
                        true
                },

                onSubtitleClick = {
                    openedMenu =
                        TrackMenuType.SUBTITLE

                    controlsVisible =
                        true
                },

                onSubtitleSettingsClick = {
                    openedMenu =
                        TrackMenuType
                            .SUBTITLE_SETTINGS

                    controlsVisible =
                        true
                },

                showEpisodesButton =
                    seriesUuid
                        .isNotBlank(),

                onEpisodesClick = {
                    openedMenu =
                        TrackMenuType.EPISODES

                    controlsVisible =
                        true
                }
            )
        }

        when (
            openedMenu
        ) {
            TrackMenuType.AUDIO -> {
                FloatingTrackOptionsPanel(
                    title =
                        "Áudio",

                    emptyMessage =
                        "Nenhuma faixa de áudio encontrada.",

                    options =
                        audioOptions,

                    selectedOptionId =
                        selectedAudioId,

                    onDismiss = {
                        openedMenu =
                            null
                    },

                    onOptionClick = {
                            option ->

                        selectedAudioId =
                            option.id

                        applyAudioSelection(
                            player =
                                exoPlayer,

                            option =
                                option
                        )

                        openedMenu =
                            null

                        controlsVisible =
                            true
                    }
                )
            }

            TrackMenuType.SUBTITLE -> {
                FloatingTrackOptionsPanel(
                    title =
                        "Legenda",

                    emptyMessage =
                        "Nenhuma legenda encontrada nessa playlist.",

                    options =
                        subtitleOptions,

                    selectedOptionId =
                        selectedSubtitleId,

                    onDismiss = {
                        openedMenu =
                            null
                    },

                    onOptionClick = {
                            option ->

                        selectedSubtitleId =
                            option.id

                        if (
                            option.isOff
                        ) {
                            currentCues =
                                emptyList()
                        }

                        applySubtitleSelection(
                            player =
                                exoPlayer,

                            option =
                                option
                        )

                        openedMenu =
                            null

                        controlsVisible =
                            true
                    }
                )
            }

            TrackMenuType.SUBTITLE_SETTINGS -> {
                SubtitleSettingsPanel(
                    settings =
                        subtitleSettings,

                    onDismiss = {
                        openedMenu =
                            null
                    },

                    onSettingsChange = {
                            newSettings ->

                        updateSubtitleSettings(
                            newSettings
                        )
                    },

                    onResetClick = {
                        resetSubtitleSettings()
                    }
                )
            }

            TrackMenuType.SPEED -> {
                FloatingSpeedPanel(
                    selectedSpeed =
                        playbackSpeed,

                    onDismiss = {
                        openedMenu =
                            null
                    },

                    onSpeedClick = {
                            speed ->

                        playbackSpeed =
                            speed

                        exoPlayer
                            .setPlaybackSpeed(
                                speed
                            )

                        openedMenu =
                            null

                        controlsVisible =
                            true
                    }
                )
            }

            TrackMenuType.EPISODES -> {
                PlayerEpisodesPanel(
                    uiState =
                        episodesUiState,

                    currentEpisodeUuid =
                        currentEpisodeUuid,

                    isLoading =
                        isLoadingEpisodes,

                    errorMessage =
                        episodesErrorMessage,

                    onDismiss = {
                        openedMenu =
                            null
                    },

                    onRetryClick = {
                        playerViewModel
                            .loadSeriesEpisodes(
                                seriesUuid =
                                    seriesUuid,

                                forceRefresh =
                                    true
                            )
                    },

                    onEpisodeClick = {
                            episode ->

                        /*
                         * Não utilizamos mais:
                         *
                         * episode.hlsUrl
                         * MediaItem.fromUri(episode.hlsUrl)
                         *
                         * Pedimos uma nova autorização
                         * ao Django usando apenas UUID.
                         */
                        playerViewModel
                            .startPlayback(
                                contentType =
                                    "episode",

                                contentUuid =
                                    episode.uuid
                            )

                        openedMenu =
                            null

                        controlsVisible =
                            true
                    }
                )
            }

            null ->
                Unit
        }

        if (
            !fatalAuthorizationError
                .isNullOrBlank()
        ) {
            FatalPlaybackAuthorizationOverlay(
                message =
                    fatalAuthorizationError,

                onBackClick =
                    onBackClick
            )
        } else if (
            !playerErrorMessage
                .isNullOrBlank()
        ) {
            PlayerMediaErrorOverlay(
                message =
                    playerErrorMessage
                        ?: "Falha ao carregar a mídia.",

                onRetryClick = {
                    playerErrorMessage =
                        null

                    exoPlayer.prepare()

                    exoPlayer.play()
                },

                onBackClick =
                    onBackClick
            )
        }
    }
}

@Composable
private fun FatalPlaybackAuthorizationOverlay(
    message: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = 0.86f
                )
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally,

            modifier =
                Modifier.padding(
                    28.dp
                )
        ) {
            Text(
                text =
                    "A reprodução foi interrompida.",

                color =
                    Color.White
            )

            Text(
                text =
                    message,

                color =
                    Color.White,

                modifier =
                    Modifier.padding(
                        top = 10.dp
                    )
            )

            Button(
                onClick =
                    onBackClick,

                modifier =
                    Modifier.padding(
                        top = 22.dp
                    )
            ) {
                Text(
                    text =
                        "Voltar"
                )
            }
        }
    }
}

@Composable
private fun PlayerMediaErrorOverlay(
    message: String,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = 0.82f
                )
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally,

            modifier =
                Modifier.padding(
                    28.dp
                )
        ) {
            Text(
                text =
                    "Não foi possível carregar o vídeo.",

                color =
                    Color.White
            )

            Text(
                text =
                    message,

                color =
                    Color.White,

                modifier =
                    Modifier.padding(
                        top = 10.dp
                    )
            )

            Button(
                onClick =
                    onRetryClick,

                modifier =
                    Modifier.padding(
                        top = 22.dp
                    )
            ) {
                Text(
                    text =
                        "Tentar novamente"
                )
            }

            Button(
                onClick =
                    onBackClick,

                modifier =
                    Modifier.padding(
                        top = 10.dp
                    )
            ) {
                Text(
                    text =
                        "Voltar"
                )
            }
        }
    }
}