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
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    contentType: String,
    uuid: String,
    hlsUrl: String,
    modifier: Modifier = Modifier,
    seriesUuid: String = "",
    onBackClick: () -> Unit = {},
    playerViewModel: PlayerViewModel = viewModel()
) {
    var preferLandscape by remember {
        mutableStateOf(true)
    }

    var orientationLocked by remember {
        mutableStateOf(false)
    }

    PlayerImmersiveMode(
        preferLandscape = preferLandscape,
        orientationLocked = orientationLocked
    )

    BackHandler {
        onBackClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LaranjadaBlack)
    ) {
        if (hlsUrl.isBlank()) {
            MissingVideoMessage(
                contentType = contentType,
                onBackClick = onBackClick
            )
        } else {
            HlsPlayer(
                url = hlsUrl,
                initialEpisodeUuid = uuid,
                seriesUuid = seriesUuid,
                playerViewModel = playerViewModel,
                preferLandscape = preferLandscape,
                orientationLocked = orientationLocked,
                onToggleOrientation = {
                    preferLandscape = !preferLandscape
                },
                onToggleOrientationLock = {
                    orientationLocked = !orientationLocked
                },
                onBackClick = onBackClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun MissingVideoMessage(
    contentType: String,
    onBackClick: () -> Unit
) {
    val itemTypeText = when (contentType.lowercase(Locale.US)) {
        "episode" -> "Esse episódio"
        "series", "serie" -> "Essa série"
        else -> "Esse vídeo"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$itemTypeText ainda não tem uma playlist M3U8 cadastrada.",
            color = LaranjadaText
        )

        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LaranjadaOrange,
                contentColor = Color.White
            )
        ) {
            Text(text = "Voltar")
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun HlsPlayer(
    url: String,
    initialEpisodeUuid: String,
    seriesUuid: String,
    playerViewModel: PlayerViewModel,
    preferLandscape: Boolean,
    orientationLocked: Boolean,
    onToggleOrientation: () -> Unit,
    onToggleOrientationLock: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var currentEpisodeUuid by remember(initialEpisodeUuid) {
        mutableStateOf(initialEpisodeUuid)
    }

    val episodesUiState = playerViewModel.episodesUiState
    val isLoadingEpisodes = playerViewModel.isLoadingEpisodes
    val episodesErrorMessage = playerViewModel.episodesErrorMessage

    var controlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }

    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }

    var isDraggingProgress by remember { mutableStateOf(false) }
    var draggedProgress by remember { mutableFloatStateOf(0f) }

    var currentCues by remember {
        mutableStateOf<List<Cue>>(emptyList())
    }

    var subtitleSettings by remember {
        mutableStateOf(loadSubtitleVisualSettings(context))
    }

    var audioOptions by remember {
        mutableStateOf(
            listOf(
                PlayerTrackOption(
                    id = AUTO_AUDIO_ID,
                    label = "Automático",
                    isAuto = true
                )
            )
        )
    }

    var subtitleOptions by remember {
        mutableStateOf(
            listOf(
                PlayerTrackOption(
                    id = SUBTITLE_OFF_ID,
                    label = "Desligada",
                    isOff = true
                )
            )
        )
    }

    var selectedAudioId by remember { mutableStateOf(AUTO_AUDIO_ID) }
    var selectedSubtitleId by remember { mutableStateOf(SUBTITLE_OFF_ID) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var openedMenu by remember { mutableStateOf<TrackMenuType?>(null) }
    var playerViewForCapture by remember { mutableStateOf<PlayerView?>(null) }

    fun updateSubtitleSettings(newSettings: SubtitleVisualSettings) {
        val fixedSettings = newSettings.coerced()
        subtitleSettings = fixedSettings
        saveSubtitleVisualSettings(context, fixedSettings)
    }

    fun resetSubtitleSettings() {
        val defaultSettings = resetSubtitleVisualSettings(context)
        subtitleSettings = defaultSettings
    }

    val exoPlayer = remember(url) {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1_500,
                30_000,
                500,
                1_000
            )
            .build()

        ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
            .apply {
                val mediaItem = MediaItem.fromUri(url)

                trackSelectionParameters = trackSelectionParameters
                    .buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    .build()

                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
                play()
            }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                durationMs = exoPlayer.safeDuration()
            }

            override fun onTracksChanged(tracks: Tracks) {
                audioOptions = buildAudioOptions(tracks)
                subtitleOptions = buildSubtitleOptions(tracks)
            }

            override fun onCues(cueGroup: CueGroup) {
                currentCues = cueGroup.cues
            }
        }

        exoPlayer.addListener(listener)

        audioOptions = buildAudioOptions(exoPlayer.currentTracks)
        subtitleOptions = buildSubtitleOptions(exoPlayer.currentTracks)

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.safeDuration()
            isPlaying = exoPlayer.isPlaying
            delay(500.milliseconds)
        }
    }

    LaunchedEffect(currentPositionMs, isDraggingProgress) {
        if (!isDraggingProgress) {
            draggedProgress = currentPositionMs.toFloat()
        }
    }

    LaunchedEffect(controlsVisible, isPlaying, isDraggingProgress, openedMenu) {
        if (
            controlsVisible &&
            isPlaying &&
            !isDraggingProgress &&
            openedMenu == null
        ) {
            delay(3.seconds)
            controlsVisible = false
        }
    }

    LaunchedEffect(seriesUuid) {
        if (seriesUuid.isNotBlank()) {
            playerViewModel.loadSeriesEpisodes(seriesUuid)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    player = exoPlayer
                    useController = false
                    keepScreenOn = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

                    subtitleView?.visibility = android.view.View.GONE

                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    playerViewForCapture = this
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.subtitleView?.visibility = android.view.View.GONE
                playerViewForCapture = playerView
            }
        )

        CustomSubtitleOverlay(
            cues = currentCues,
            settings = subtitleSettings,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            controlsVisible = !controlsVisible
                        }
                    )
                }
        )

        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = LaranjadaOrange,
                trackColor = Color.White.copy(alpha = 0.18f)
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerControlsOverlay(
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                draggedProgress = draggedProgress,
                playbackSpeed = playbackSpeed,
                preferLandscape = preferLandscape,
                orientationLocked = orientationLocked,
                onCloseClick = onBackClick,
                onToggleOrientationClick = onToggleOrientation,
                onToggleOrientationLockClick = onToggleOrientationLock,
                onSpeedClick = {
                    openedMenu = TrackMenuType.SPEED
                    controlsVisible = true
                },
                onScreenshotClick = {
                    val playerView = playerViewForCapture

                    if (playerView == null) {
                        Toast.makeText(
                            context,
                            "Player ainda não está pronto para capturar.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        capturePlayerFrame(
                            context = context,
                            playerView = playerView
                        )
                    }
                },
                onPictureInPictureClick = {
                    controlsVisible = false
                    enterPictureInPictureMode(context)
                },
                onDraggedProgressChange = { value ->
                    isDraggingProgress = true
                    draggedProgress = value
                },
                onDragFinished = {
                    exoPlayer.seekTo(draggedProgress.toLong())
                    isDraggingProgress = false
                },
                onPlayPauseClick = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                },
                onReplayClick = {
                    val newPosition = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                    exoPlayer.seekTo(newPosition)
                },
                onForwardClick = {
                    val maxDuration = exoPlayer.safeDuration()
                    val newPosition = if (maxDuration > 0L) {
                        (exoPlayer.currentPosition + 10_000L).coerceAtMost(maxDuration)
                    } else {
                        exoPlayer.currentPosition + 10_000L
                    }

                    exoPlayer.seekTo(newPosition)
                },
                onAudioClick = {
                    openedMenu = TrackMenuType.AUDIO
                    controlsVisible = true
                },
                onSubtitleClick = {
                    openedMenu = TrackMenuType.SUBTITLE
                    controlsVisible = true
                },
                onSubtitleSettingsClick = {
                    openedMenu = TrackMenuType.SUBTITLE_SETTINGS
                    controlsVisible = true
                },
                showEpisodesButton = seriesUuid.isNotBlank(),
                onEpisodesClick = {
                    openedMenu = TrackMenuType.EPISODES
                    controlsVisible = true
                }
            )
        }

        when (openedMenu) {
            TrackMenuType.AUDIO -> {
                FloatingTrackOptionsPanel(
                    title = "Áudio",
                    emptyMessage = "Nenhuma faixa de áudio encontrada.",
                    options = audioOptions,
                    selectedOptionId = selectedAudioId,
                    onDismiss = {
                        openedMenu = null
                    },
                    onOptionClick = { option ->
                        selectedAudioId = option.id
                        applyAudioSelection(
                            player = exoPlayer,
                            option = option
                        )
                        openedMenu = null
                        controlsVisible = true
                    }
                )
            }

            TrackMenuType.SUBTITLE -> {
                FloatingTrackOptionsPanel(
                    title = "Legenda",
                    emptyMessage = "Nenhuma legenda encontrada nessa playlist.",
                    options = subtitleOptions,
                    selectedOptionId = selectedSubtitleId,
                    onDismiss = {
                        openedMenu = null
                    },
                    onOptionClick = { option ->
                        selectedSubtitleId = option.id

                        if (option.isOff) {
                            currentCues = emptyList()
                        }

                        applySubtitleSelection(
                            player = exoPlayer,
                            option = option
                        )

                        openedMenu = null
                        controlsVisible = true
                    }
                )
            }

            TrackMenuType.SUBTITLE_SETTINGS -> {
                SubtitleSettingsPanel(
                    settings = subtitleSettings,
                    onDismiss = {
                        openedMenu = null
                    },
                    onSettingsChange = { newSettings ->
                        updateSubtitleSettings(newSettings)
                    },
                    onResetClick = {
                        resetSubtitleSettings()
                    }
                )
            }

            TrackMenuType.SPEED -> {
                FloatingSpeedPanel(
                    selectedSpeed = playbackSpeed,
                    onDismiss = {
                        openedMenu = null
                    },
                    onSpeedClick = { speed ->
                        playbackSpeed = speed
                        exoPlayer.setPlaybackSpeed(speed)
                        openedMenu = null
                        controlsVisible = true
                    }
                )
            }

            TrackMenuType.EPISODES -> {
                PlayerEpisodesPanel(
                    uiState = episodesUiState,
                    currentEpisodeUuid = currentEpisodeUuid,
                    isLoading = isLoadingEpisodes,
                    errorMessage = episodesErrorMessage,
                    onDismiss = {
                        openedMenu = null
                    },
                    onRetryClick = {
                        playerViewModel.loadSeriesEpisodes(
                            seriesUuid = seriesUuid,
                            forceRefresh = true
                        )
                    },
                    onEpisodeClick = { episode ->
                        if (episode.hlsUrl.isNotBlank()) {
                            currentCues = emptyList()
                            currentEpisodeUuid = episode.uuid
                            currentPositionMs = 0L
                            draggedProgress = 0f
                            durationMs = 0L

                            exoPlayer.setMediaItem(
                                MediaItem.fromUri(episode.hlsUrl)
                            )
                            exoPlayer.prepare()
                            exoPlayer.setPlaybackSpeed(playbackSpeed)
                            exoPlayer.playWhenReady = true
                            exoPlayer.play()
                        }

                        openedMenu = null
                        controlsVisible = true
                    }
                )
            }

            null -> Unit
        }
    }
}