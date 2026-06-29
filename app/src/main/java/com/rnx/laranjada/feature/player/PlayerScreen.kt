package com.rnx.laranjada.feature.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaSurface
import com.rnx.laranjada.core.design.theme.LaranjadaText
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val PLAYER_LOG_TAG = "PlayerScreen"

private const val AUTO_AUDIO_ID = "audio_auto"
private const val SUBTITLE_OFF_ID = "subtitle_off"

private const val PLAYER_PREFS_NAME = "laranjada_player_prefs"

private const val KEY_SUBTITLE_SETTINGS_VERSION = "subtitle_settings_version"
private const val CURRENT_SUBTITLE_SETTINGS_VERSION = 3

private const val KEY_SUBTITLE_BOTTOM_PADDING = "subtitle_bottom_padding"
private const val KEY_SUBTITLE_TEXT_SIZE = "subtitle_text_size"
private const val KEY_SUBTITLE_TEXT_COLOR = "subtitle_text_color"
private const val KEY_SUBTITLE_BACKGROUND_COLOR = "subtitle_background_color"

private const val DEFAULT_SUBTITLE_BOTTOM_PADDING = 0.117f
private const val DEFAULT_SUBTITLE_TEXT_SIZE = 0.046f

private const val MIN_SUBTITLE_BOTTOM_PADDING = 0.04f
private const val MAX_SUBTITLE_BOTTOM_PADDING = 0.55f

private const val MIN_SUBTITLE_TEXT_SIZE = 0.035f
private const val MAX_SUBTITLE_TEXT_SIZE = 0.078f

private val BrazilianPortugueseLocale: Locale = Locale.forLanguageTag("pt-BR")

private val SubtitleYellow: Int = AndroidColor.rgb(255, 235, 59)
private val SubtitleOrange: Int = AndroidColor.rgb(253, 107, 0)
private val SubtitleAppleBackground: Int = AndroidColor.argb(120, 0, 0, 0)
private val SubtitleBlackBackground: Int = AndroidColor.argb(190, 0, 0, 0)

private enum class TrackMenuType {
    AUDIO,
    SUBTITLE,
    SUBTITLE_SETTINGS,
    SPEED,
    EPISODES
}

private data class PlayerTrackOption(
    val id: String,
    val label: String,
    val language: String? = null,
    val trackGroup: TrackGroup? = null,
    val trackIndex: Int = -1,
    val isAuto: Boolean = false,
    val isOff: Boolean = false
)

private data class SubtitleVisualSettings(
    val bottomPaddingFraction: Float = DEFAULT_SUBTITLE_BOTTOM_PADDING,
    val textSizeFraction: Float = DEFAULT_SUBTITLE_TEXT_SIZE,
    val textColorArgb: Int = AndroidColor.WHITE,
    val backgroundColorArgb: Int = SubtitleAppleBackground
)

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
private fun PlayerImmersiveMode(
    preferLandscape: Boolean,
    orientationLocked: Boolean
) {
    val view = LocalView.current
    val activity = view.context.findActivity()

    DisposableEffect(view) {
        val window = activity?.window
        val previousOrientation = activity?.requestedOrientation

        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val controller = WindowInsetsControllerCompat(
                window,
                window.decorView
            )

            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            controller.hide(WindowInsetsCompat.Type.systemBars())

            onDispose {
                controller.show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(window, true)

                if (previousOrientation != null) {
                    activity.requestedOrientation = previousOrientation
                }
            }
        } else {
            onDispose {}
        }
    }

    LaunchedEffect(activity, preferLandscape, orientationLocked) {
        if (activity == null) return@LaunchedEffect

        activity.requestedOrientation = when {
            orientationLocked && preferLandscape -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            orientationLocked && !preferLandscape -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            preferLandscape -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
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
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(42.dp),
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

@Composable
private fun CustomSubtitleOverlay(
    cues: List<Cue>,
    settings: SubtitleVisualSettings,
    modifier: Modifier = Modifier
) {
    val subtitleText = cues
        .mapNotNull { cue ->
            cue.text
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }
        .joinToString(separator = "\n")

    if (subtitleText.isBlank()) {
        return
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        val bottomPadding = (maxHeight.value * settings.bottomPaddingFraction).dp
        val backgroundAlpha = AndroidColor.alpha(settings.backgroundColorArgb)

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = bottomPadding
                )
                .then(
                    if (backgroundAlpha > 0) {
                        Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(settings.backgroundColorArgb))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    } else {
                        Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    }
                )
        ) {
            Text(
                text = subtitleText,
                color = Color(settings.textColorArgb),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = subtitleFontSize(settings.textSizeFraction).sp,
                lineHeight = (subtitleFontSize(settings.textSizeFraction) * 1.18f).sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(1.5f, 1.5f),
                        blurRadius = 3f
                    )
                )
            )
        }
    }
}

@Composable
private fun PlayerControlsOverlay(
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    draggedProgress: Float,
    playbackSpeed: Float,
    preferLandscape: Boolean,
    orientationLocked: Boolean,
    onCloseClick: () -> Unit,
    onToggleOrientationClick: () -> Unit,
    onToggleOrientationLockClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onScreenshotClick: () -> Unit,
    onPictureInPictureClick: () -> Unit,
    onDraggedProgressChange: (Float) -> Unit,
    onDragFinished: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onReplayClick: () -> Unit,
    onForwardClick: () -> Unit,
    onAudioClick: () -> Unit,
    onSubtitleClick: () -> Unit,
    onSubtitleSettingsClick: () -> Unit,
    showEpisodesButton: Boolean,
    onEpisodesClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.38f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerTopIconButton(
                onClick = onCloseClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Fechar player",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            PlayerTopIconButton(
                onClick = onToggleOrientationClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.ScreenRotation,
                    contentDescription = if (preferLandscape) {
                        "Virar para modo em pé"
                    } else {
                        "Virar para modo deitado"
                    },
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PlayerTopIconButton(
                onClick = onSpeedClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = "Velocidade",
                    tint = Color.White,
                    modifier = Modifier.size(27.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            PlayerTopIconButton(
                onClick = onScreenshotClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = "Capturar tela",
                    tint = Color.White,
                    modifier = Modifier.size(27.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            PlayerTopIconButton(
                onClick = onPictureInPictureClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.PictureInPictureAlt,
                    contentDescription = "Picture-in-picture",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (playbackSpeed == 1f) {
                        "1x"
                    } else {
                        "${formatSpeed(playbackSpeed)}x"
                    },
                    color = Color.White.copy(alpha = 0.92f),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                PlayerBottomIconButton(
                    onClick = onAudioClick
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Audiotrack,
                        contentDescription = "Áudio",
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                PlayerBottomIconButton(
                    onClick = onSubtitleClick
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ClosedCaption,
                        contentDescription = "Legenda",
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                PlayerBottomIconButton(
                    onClick = onSubtitleSettingsClick
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Configurações de legenda",
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerProgressBar(
                    modifier = Modifier.weight(1f),
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    draggedProgress = draggedProgress,
                    onDraggedProgressChange = onDraggedProgressChange,
                    onDragFinished = onDragFinished
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "${formatTime(currentPositionMs)} / ${formatTime(durationMs)}",
                    color = LaranjadaText,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(top = 5.dp)
            ) {
                PlayerBottomIconButton(
                    onClick = onToggleOrientationLockClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = if (orientationLocked) {
                            Icons.Rounded.Lock
                        } else {
                            Icons.Rounded.LockOpen
                        },
                        contentDescription = if (orientationLocked) {
                            "Destravar orientação"
                        } else {
                            "Travar orientação"
                        },
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    PlayerRoundButton(
                        onClick = onReplayClick,
                        containerSize = 46.dp,
                        iconSize = 25.dp
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Replay10,
                            contentDescription = "Voltar 10 segundos",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    PlayerRoundButton(
                        onClick = onPlayPauseClick,
                        containerSize = 58.dp,
                        iconSize = 34.dp
                    ) {
                        Icon(
                            imageVector = if (isPlaying) {
                                Icons.Rounded.Pause
                            } else {
                                Icons.Rounded.PlayArrow
                            },
                            contentDescription = if (isPlaying) {
                                "Pausar"
                            } else {
                                "Reproduzir"
                            },
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    PlayerRoundButton(
                        onClick = onForwardClick,
                        containerSize = 46.dp,
                        iconSize = 25.dp
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Forward10,
                            contentDescription = "Avançar 10 segundos",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                if (showEpisodesButton) {
                    PlayerBottomIconButton(
                        onClick = onEpisodesClick,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Episódios",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerTopIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.24f))
    ) {
        content()
    }
}

@Composable
private fun PlayerProgressBar(
    modifier: Modifier = Modifier,
    currentPositionMs: Long,
    durationMs: Long,
    draggedProgress: Float,
    onDraggedProgressChange: (Float) -> Unit,
    onDragFinished: () -> Unit
) {
    val safeDuration = durationMs.coerceAtLeast(1L)
    val progressValue = if (draggedProgress >= 0f) {
        draggedProgress
    } else {
        currentPositionMs.toFloat()
    }

    val progressFraction = if (durationMs > 0L) {
        progressValue
            .coerceIn(0f, safeDuration.toFloat()) / safeDuration.toFloat()
    } else {
        0f
    }

    BoxWithConstraints(
        modifier = modifier
            .height(28.dp)
            .pointerInput(safeDuration) {
                detectTapGestures { offset ->
                    val position = ((offset.x / size.width).coerceIn(0f, 1f) * safeDuration)
                    onDraggedProgressChange(position)
                    onDragFinished()
                }
            }
            .pointerInput(safeDuration) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val position = ((offset.x / size.width).coerceIn(0f, 1f) * safeDuration)
                        onDraggedProgressChange(position)
                    },
                    onDragEnd = {
                        onDragFinished()
                    },
                    onDragCancel = {
                        onDragFinished()
                    },
                    onDrag = { change, _ ->
                        val position = ((change.position.x / size.width).coerceIn(0f, 1f) * safeDuration)
                        onDraggedProgressChange(position)
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val thumbSize = 9.dp
        val trackHeight = 4.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.32f))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(progressFraction)
                .height(trackHeight)
                .clip(RoundedCornerShape(99.dp))
                .background(LaranjadaOrange)
        )

        Box(
            modifier = Modifier
                .offset(x = (maxWidth - thumbSize) * progressFraction)
                .size(thumbSize)
                .clip(CircleShape)
                .background(LaranjadaOrange)
        )
    }
}

@Composable
private fun PlayerRoundButton(
    onClick: () -> Unit,
    containerSize: Dp,
    iconSize: Dp,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(containerSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.56f))
    ) {
        Box(
            modifier = Modifier.size(iconSize),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun PlayerBottomIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.36f))
    ) {
        content()
    }
}

@Composable
private fun FloatingTrackOptionsPanel(
    title: String,
    emptyMessage: String,
    options: List<PlayerTrackOption>,
    selectedOptionId: String,
    onDismiss: () -> Unit,
    onOptionClick: (PlayerTrackOption) -> Unit
) {
    FloatingPlayerPanel(
        title = title,
        alignment = Alignment.BottomEnd,
        onDismiss = onDismiss
    ) {
        if (options.isEmpty()) {
            Text(
                text = emptyMessage,
                color = LaranjadaMutedText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        } else {
            options.forEach { option ->
                TrackOptionRow(
                    option = option,
                    selected = option.id == selectedOptionId,
                    onClick = {
                        onOptionClick(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun FloatingSpeedPanel(
    selectedSpeed: Float,
    onDismiss: () -> Unit,
    onSpeedClick: (Float) -> Unit
) {
    val speeds = listOf(
        0.5f,
        0.75f,
        1f,
        1.25f,
        1.5f,
        1.75f,
        2f
    )

    FloatingPlayerPanel(
        title = "Velocidade",
        alignment = Alignment.BottomEnd,
        onDismiss = onDismiss
    ) {
        speeds.forEach { speed ->
            SpeedOptionRow(
                speed = speed,
                selected = speed == selectedSpeed,
                onClick = {
                    onSpeedClick(speed)
                }
            )
        }
    }
}

@Composable
private fun SpeedOptionRow(
    speed: Float,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${formatSpeed(speed)}x",
            color = if (selected) LaranjadaOrange else LaranjadaText,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selecionado",
                tint = LaranjadaOrange,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun TrackOptionRow(
    option: PlayerTrackOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = option.label,
            color = if (selected) LaranjadaOrange else LaranjadaText,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selecionado",
                tint = LaranjadaOrange,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SubtitleSettingsPanel(
    settings: SubtitleVisualSettings,
    onDismiss: () -> Unit,
    onSettingsChange: (SubtitleVisualSettings) -> Unit,
    onResetClick: () -> Unit
) {
    FloatingPlayerPanel(
        title = "Ajustes da legenda",
        alignment = Alignment.CenterEnd,
        onDismiss = onDismiss
    ) {
        CompactStepperRow(
            title = "Posição",
            value = subtitlePositionLabel(settings.bottomPaddingFraction),
            decreaseText = "Descer",
            increaseText = "Subir",
            onDecrease = {
                onSettingsChange(
                    settings.copy(
                        bottomPaddingFraction = settings.bottomPaddingFraction - 0.04f
                    )
                )
            },
            onIncrease = {
                onSettingsChange(
                    settings.copy(
                        bottomPaddingFraction = settings.bottomPaddingFraction + 0.04f
                    )
                )
            }
        )

        CompactStepperRow(
            title = "Tamanho",
            value = subtitleSizeLabel(settings.textSizeFraction),
            decreaseText = "Menor",
            increaseText = "Maior",
            onDecrease = {
                onSettingsChange(
                    settings.copy(
                        textSizeFraction = settings.textSizeFraction - 0.004f
                    )
                )
            },
            onIncrease = {
                onSettingsChange(
                    settings.copy(
                        textSizeFraction = settings.textSizeFraction + 0.004f
                    )
                )
            }
        )

        CompactPresetRow(
            title = "Texto",
            selectedLabel = subtitleTextColorLabel(settings.textColorArgb),
            choices = listOf(
                SubtitleChoice("Branco", AndroidColor.WHITE),
                SubtitleChoice("Amarelo", SubtitleYellow),
                SubtitleChoice("Laranja", SubtitleOrange)
            ),
            selectedColorArgb = settings.textColorArgb,
            onSelected = { colorArgb ->
                onSettingsChange(
                    settings.copy(
                        textColorArgb = colorArgb
                    )
                )
            }
        )

        ColorHuePicker(
            title = "Cor livre do texto",
            colorArgb = settings.textColorArgb,
            includeAlpha = false,
            onColorChange = { colorArgb ->
                onSettingsChange(
                    settings.copy(
                        textColorArgb = colorArgb
                    )
                )
            }
        )

        CompactPresetRow(
            title = "Fundo",
            selectedLabel = subtitleBackgroundLabel(settings.backgroundColorArgb),
            choices = listOf(
                SubtitleChoice("Transparente", SubtitleAppleBackground),
                SubtitleChoice("Sem fundo", AndroidColor.TRANSPARENT),
                SubtitleChoice("Preto", SubtitleBlackBackground)
            ),
            selectedColorArgb = settings.backgroundColorArgb,
            onSelected = { colorArgb ->
                onSettingsChange(
                    settings.copy(
                        backgroundColorArgb = colorArgb
                    )
                )
            }
        )

        CompactStepperRow(
            title = "Transparência do fundo",
            value = backgroundAlphaLabel(settings.backgroundColorArgb),
            decreaseText = "Menos",
            increaseText = "Mais",
            onDecrease = {
                onSettingsChange(
                    settings.copy(
                        backgroundColorArgb = colorWithAlpha(
                            settings.backgroundColorArgb,
                            AndroidColor.alpha(settings.backgroundColorArgb) - 20
                        )
                    )
                )
            },
            onIncrease = {
                onSettingsChange(
                    settings.copy(
                        backgroundColorArgb = colorWithAlpha(
                            settings.backgroundColorArgb,
                            AndroidColor.alpha(settings.backgroundColorArgb) + 20
                        )
                    )
                )
            }
        )

        ColorHuePicker(
            title = "Cor livre do fundo",
            colorArgb = settings.backgroundColorArgb,
            includeAlpha = true,
            onColorChange = { colorArgb ->
                onSettingsChange(
                    settings.copy(
                        backgroundColorArgb = colorArgb
                    )
                )
            }
        )

        Button(
            onClick = onResetClick,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.12f),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.RestartAlt,
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Restaurar padrão",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FloatingPlayerPanel(
    title: String,
    alignment: Alignment,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
        contentAlignment = alignment
    ) {
        val panelWidth = if (maxWidth > 620.dp) {
            390.dp
        } else {
            (maxWidth - 32.dp).coerceAtLeast(280.dp)
        }

        val panelMaxHeight = if (maxHeight > 420.dp) {
            350.dp
        } else {
            (maxHeight - 40.dp).coerceAtLeast(240.dp)
        }

        val contentMaxHeight = (panelMaxHeight - 58.dp).coerceAtLeast(150.dp)

        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(panelWidth)
                .heightIn(max = panelMaxHeight)
                .clip(RoundedCornerShape(18.dp))
                .background(LaranjadaSurface.copy(alpha = 0.96f))
                .clickable { }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(start = 16.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = LaranjadaText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Minimizar",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fechar",
                        tint = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .heightIn(max = contentMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 6.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun CompactStepperRow(
    title: String,
    value: String,
    decreaseText: String,
    increaseText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = LaranjadaText,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                color = LaranjadaMutedText
            )
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepperButton(
                text = decreaseText,
                icon = Icons.Rounded.Remove,
                onClick = onDecrease
            )

            Spacer(modifier = Modifier.width(10.dp))

            StepperButton(
                text = increaseText,
                icon = Icons.Rounded.Add,
                onClick = onIncrease,
                highlighted = true
            )
        }
    }
}

@Composable
private fun StepperButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    highlighted: Boolean = false
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (highlighted) {
                LaranjadaOrange
            } else {
                Color.White.copy(alpha = 0.12f)
            },
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(text = text)
    }
}

private data class SubtitleChoice(
    val label: String,
    val colorArgb: Int
)

@Composable
private fun CompactPresetRow(
    title: String,
    selectedLabel: String,
    choices: List<SubtitleChoice>,
    selectedColorArgb: Int,
    onSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = LaranjadaText,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = selectedLabel,
                color = LaranjadaMutedText
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            choices.forEach { choice ->
                Button(
                    onClick = {
                        onSelected(choice.colorArgb)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (choice.colorArgb == selectedColorArgb) {
                            LaranjadaOrange
                        } else {
                            Color.White.copy(alpha = 0.12f)
                        },
                        contentColor = Color.White
                    )
                ) {
                    Text(text = choice.label)
                }
            }
        }
    }
}

@Composable
private fun ColorHuePicker(
    title: String,
    colorArgb: Int,
    includeAlpha: Boolean,
    onColorChange: (Int) -> Unit
) {
    val hue = hueFromColor(colorArgb)
    val hueFraction = (hue / 360f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = LaranjadaText,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            ColorPreview(colorArgb = colorArgb)

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = colorLabel(colorArgb),
                color = LaranjadaMutedText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(spectrumBrush())
                .pointerInput(colorArgb, includeAlpha) {
                    detectTapGestures { offset ->
                        val newHue = ((offset.x / size.width).coerceIn(0f, 1f) * 360f)

                        onColorChange(
                            colorWithHue(
                                colorArgb = colorArgb,
                                hue = newHue,
                                includeAlpha = includeAlpha
                            )
                        )
                    }
                }
                .pointerInput(colorArgb, includeAlpha) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newHue = ((offset.x / size.width).coerceIn(0f, 1f) * 360f)

                            onColorChange(
                                colorWithHue(
                                    colorArgb = colorArgb,
                                    hue = newHue,
                                    includeAlpha = includeAlpha
                                )
                            )
                        },
                        onDrag = { change, _ ->
                            val newHue = ((change.position.x / size.width).coerceIn(0f, 1f) * 360f)

                            onColorChange(
                                colorWithHue(
                                    colorArgb = colorArgb,
                                    hue = newHue,
                                    includeAlpha = includeAlpha
                                )
                            )
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val markerSize = 18.dp

            Box(
                modifier = Modifier
                    .offset(x = (maxWidth - markerSize) * hueFraction)
                    .size(markerSize)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(colorArgb))
                )
            }
        }
    }
}

@Composable
private fun ColorPreview(
    colorArgb: Int
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color(colorArgb))
        )
    }
}

private fun spectrumBrush(): Brush {
    return Brush.horizontalGradient(
        colors = listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red
        )
    )
}

@OptIn(UnstableApi::class)
private fun buildAudioOptions(
    tracks: Tracks
): List<PlayerTrackOption> {
    val options = mutableListOf(
        PlayerTrackOption(
            id = AUTO_AUDIO_ID,
            label = "Automático",
            isAuto = true
        )
    )

    tracks.groups.forEachIndexed { groupIndex, group ->
        if (group.type == C.TRACK_TYPE_AUDIO) {
            for (trackIndex in 0 until group.length) {
                if (group.isTrackSupported(trackIndex)) {
                    val format = group.getTrackFormat(trackIndex)

                    val label = buildTrackLabel(
                        fallback = "Áudio ${trackIndex + 1}",
                        language = format.language,
                        name = format.label
                    )

                    options.add(
                        PlayerTrackOption(
                            id = "audio_${groupIndex}_$trackIndex",
                            label = label,
                            language = format.language,
                            trackGroup = group.mediaTrackGroup,
                            trackIndex = trackIndex
                        )
                    )
                }
            }
        }
    }

    return options.distinctBy { option ->
        option.id
    }
}

@OptIn(UnstableApi::class)
private fun buildSubtitleOptions(
    tracks: Tracks
): List<PlayerTrackOption> {
    val options = mutableListOf(
        PlayerTrackOption(
            id = SUBTITLE_OFF_ID,
            label = "Desligada",
            isOff = true
        )
    )

    tracks.groups.forEachIndexed { groupIndex, group ->
        if (group.type == C.TRACK_TYPE_TEXT) {
            for (trackIndex in 0 until group.length) {
                if (group.isTrackSupported(trackIndex)) {
                    val format = group.getTrackFormat(trackIndex)

                    val label = buildTrackLabel(
                        fallback = "Legenda ${trackIndex + 1}",
                        language = format.language,
                        name = format.label
                    )

                    options.add(
                        PlayerTrackOption(
                            id = "subtitle_${groupIndex}_$trackIndex",
                            label = label,
                            language = format.language,
                            trackGroup = group.mediaTrackGroup,
                            trackIndex = trackIndex
                        )
                    )
                }
            }
        }
    }

    return options.distinctBy { option ->
        option.id
    }
}

@OptIn(UnstableApi::class)
private fun applyAudioSelection(
    player: ExoPlayer,
    option: PlayerTrackOption
) {
    val builder = player.trackSelectionParameters
        .buildUpon()
        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
        .clearOverridesOfType(C.TRACK_TYPE_AUDIO)

    if (!option.isAuto && option.trackGroup != null && option.trackIndex >= 0) {
        builder.setOverrideForType(
            TrackSelectionOverride(
                option.trackGroup,
                listOf(option.trackIndex)
            )
        )
    }

    player.trackSelectionParameters = builder.build()
}

@OptIn(UnstableApi::class)
private fun applySubtitleSelection(
    player: ExoPlayer,
    option: PlayerTrackOption
) {
    val builder = player.trackSelectionParameters
        .buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_TEXT)

    if (option.isOff) {
        builder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
    } else {
        builder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)

        if (option.trackGroup != null && option.trackIndex >= 0) {
            builder.setOverrideForType(
                TrackSelectionOverride(
                    option.trackGroup,
                    listOf(option.trackIndex)
                )
            )
        }
    }

    player.trackSelectionParameters = builder.build()
}

private fun buildTrackLabel(
    fallback: String,
    language: String?,
    name: String?
): String {
    val cleanName = name
        ?.trim()
        ?.takeIf { it.isNotBlank() }

    val languageName = languageDisplayName(language)

    return when {
        cleanName != null && languageName != null -> {
            if (cleanName.contains(languageName, ignoreCase = true)) {
                cleanName
            } else {
                "$cleanName ($languageName)"
            }
        }

        cleanName != null -> cleanName
        languageName != null -> languageName
        else -> fallback
    }
}

private fun languageDisplayName(language: String?): String? {
    val cleanLanguage = language
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: return null

    val lower = cleanLanguage.lowercase(Locale.US)

    val mapped = when (lower) {
        "pt", "pt-br", "por", "ptb" -> "Português"
        "en", "eng" -> "Inglês"
        "es", "spa" -> "Espanhol"
        "ja", "jpn" -> "Japonês"
        "ko", "kor" -> "Coreano"
        "fr", "fre", "fra" -> "Francês"
        "de", "ger", "deu" -> "Alemão"
        "it", "ita" -> "Italiano"
        else -> null
    }

    if (mapped != null) {
        return mapped
    }

    val locale = Locale.forLanguageTag(cleanLanguage)

    val displayName = locale
        .getDisplayLanguage(BrazilianPortugueseLocale)
        .trim()
        .takeIf { it.isNotBlank() && it != cleanLanguage }

    return displayName?.replaceFirstChar { char ->
        if (char.isLowerCase()) {
            char.titlecase(BrazilianPortugueseLocale)
        } else {
            char.toString()
        }
    } ?: cleanLanguage
}

private fun defaultSubtitleVisualSettings(): SubtitleVisualSettings {
    return SubtitleVisualSettings(
        bottomPaddingFraction = DEFAULT_SUBTITLE_BOTTOM_PADDING,
        textSizeFraction = DEFAULT_SUBTITLE_TEXT_SIZE,
        textColorArgb = AndroidColor.WHITE,
        backgroundColorArgb = SubtitleAppleBackground
    )
}

private fun SubtitleVisualSettings.coerced(): SubtitleVisualSettings {
    return copy(
        bottomPaddingFraction = bottomPaddingFraction.coerceIn(
            MIN_SUBTITLE_BOTTOM_PADDING,
            MAX_SUBTITLE_BOTTOM_PADDING
        ),
        textSizeFraction = textSizeFraction.coerceIn(
            MIN_SUBTITLE_TEXT_SIZE,
            MAX_SUBTITLE_TEXT_SIZE
        )
    )
}

private fun loadSubtitleVisualSettings(
    context: Context
): SubtitleVisualSettings {
    val prefs = context.getSharedPreferences(
        PLAYER_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    val savedVersion = prefs.getInt(
        KEY_SUBTITLE_SETTINGS_VERSION,
        0
    )

    if (savedVersion < CURRENT_SUBTITLE_SETTINGS_VERSION) {
        val defaultSettings = defaultSubtitleVisualSettings()
        saveSubtitleVisualSettings(context, defaultSettings)
        return defaultSettings
    }

    return SubtitleVisualSettings(
        bottomPaddingFraction = prefs.getFloat(
            KEY_SUBTITLE_BOTTOM_PADDING,
            DEFAULT_SUBTITLE_BOTTOM_PADDING
        ),
        textSizeFraction = prefs.getFloat(
            KEY_SUBTITLE_TEXT_SIZE,
            DEFAULT_SUBTITLE_TEXT_SIZE
        ),
        textColorArgb = prefs.getInt(
            KEY_SUBTITLE_TEXT_COLOR,
            AndroidColor.WHITE
        ),
        backgroundColorArgb = prefs.getInt(
            KEY_SUBTITLE_BACKGROUND_COLOR,
            SubtitleAppleBackground
        )
    ).coerced()
}

private fun saveSubtitleVisualSettings(
    context: Context,
    settings: SubtitleVisualSettings
) {
    context
        .getSharedPreferences(
            PLAYER_PREFS_NAME,
            Context.MODE_PRIVATE
        )
        .edit {
            putInt(KEY_SUBTITLE_SETTINGS_VERSION, CURRENT_SUBTITLE_SETTINGS_VERSION)
            putFloat(KEY_SUBTITLE_BOTTOM_PADDING, settings.bottomPaddingFraction)
            putFloat(KEY_SUBTITLE_TEXT_SIZE, settings.textSizeFraction)
            putInt(KEY_SUBTITLE_TEXT_COLOR, settings.textColorArgb)
            putInt(KEY_SUBTITLE_BACKGROUND_COLOR, settings.backgroundColorArgb)
        }
}

private fun resetSubtitleVisualSettings(
    context: Context
): SubtitleVisualSettings {
    val defaultSettings = defaultSubtitleVisualSettings()

    context
        .getSharedPreferences(
            PLAYER_PREFS_NAME,
            Context.MODE_PRIVATE
        )
        .edit {
            putInt(KEY_SUBTITLE_SETTINGS_VERSION, CURRENT_SUBTITLE_SETTINGS_VERSION)
            putFloat(KEY_SUBTITLE_BOTTOM_PADDING, defaultSettings.bottomPaddingFraction)
            putFloat(KEY_SUBTITLE_TEXT_SIZE, defaultSettings.textSizeFraction)
            putInt(KEY_SUBTITLE_TEXT_COLOR, defaultSettings.textColorArgb)
            putInt(KEY_SUBTITLE_BACKGROUND_COLOR, defaultSettings.backgroundColorArgb)
        }

    return defaultSettings
}

private fun subtitlePositionLabel(
    bottomPaddingFraction: Float
): String {
    val percentage = (
            (bottomPaddingFraction - MIN_SUBTITLE_BOTTOM_PADDING) /
                    (MAX_SUBTITLE_BOTTOM_PADDING - MIN_SUBTITLE_BOTTOM_PADDING) *
                    100f
            )
        .coerceIn(0f, 100f)
        .roundToInt()

    return "$percentage%"
}

private fun subtitleSizeLabel(
    textSizeFraction: Float
): String {
    return when {
        textSizeFraction < 0.048f -> "Pequena"
        textSizeFraction <= 0.064f -> "Normal"
        else -> "Grande"
    }
}

private fun subtitleFontSize(
    textSizeFraction: Float
): Float {
    return (textSizeFraction * 520f).coerceIn(18f, 42f)
}

private fun subtitleTextColorLabel(
    colorArgb: Int
): String {
    return when (colorArgb) {
        AndroidColor.WHITE -> "Branco"
        SubtitleYellow -> "Amarelo"
        SubtitleOrange -> "Laranja"
        else -> "Livre"
    }
}

private fun subtitleBackgroundLabel(
    colorArgb: Int
): String {
    return when (colorArgb) {
        SubtitleAppleBackground -> "Transparente"
        AndroidColor.TRANSPARENT -> "Sem fundo"
        SubtitleBlackBackground -> "Preto"
        else -> "Livre"
    }
}

private fun backgroundAlphaLabel(
    colorArgb: Int
): String {
    val alpha = AndroidColor.alpha(colorArgb)
    val percentage = ((alpha / 255f) * 100f).roundToInt()

    return "$percentage%"
}

private fun colorLabel(
    colorArgb: Int
): String {
    val alpha = AndroidColor.alpha(colorArgb)
    val red = AndroidColor.red(colorArgb)
    val green = AndroidColor.green(colorArgb)
    val blue = AndroidColor.blue(colorArgb)

    return if (alpha == 255) {
        String.format(Locale.US, "#%02X%02X%02X", red, green, blue)
    } else {
        String.format(Locale.US, "#%02X%02X%02X%02X", alpha, red, green, blue)
    }
}

private fun hueFromColor(
    colorArgb: Int
): Float {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(colorArgb, hsv)
    return hsv[0].coerceIn(0f, 360f)
}

private fun colorWithHue(
    colorArgb: Int,
    hue: Float,
    includeAlpha: Boolean
): Int {
    val alpha = if (includeAlpha) {
        AndroidColor.alpha(colorArgb)
    } else {
        255
    }

    return AndroidColor.HSVToColor(
        alpha,
        floatArrayOf(
            hue.coerceIn(0f, 360f),
            1f,
            1f
        )
    )
}

private fun colorWithAlpha(
    colorArgb: Int,
    alpha: Int
): Int {
    return AndroidColor.argb(
        alpha.coerceIn(0, 255),
        AndroidColor.red(colorArgb),
        AndroidColor.green(colorArgb),
        AndroidColor.blue(colorArgb)
    )
}

private fun ExoPlayer.safeDuration(): Long {
    val playerDuration = duration

    return if (playerDuration == C.TIME_UNSET || playerDuration < 0L) {
        0L
    } else {
        playerDuration
    }
}

private fun formatTime(
    timeMs: Long
): String {
    val safeTimeMs = timeMs.coerceAtLeast(0L)
    val totalSeconds = safeTimeMs / 1000L

    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L

    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

private fun formatSpeed(
    speed: Float
): String {
    return if (speed % 1f == 0f) {
        speed.toInt().toString()
    } else {
        speed.toString().trimEnd('0').trimEnd('.')
    }
}

private fun enterPictureInPictureMode(
    context: Context
) {
    val activity = context.findActivity()

    if (activity == null) {
        Toast.makeText(
            context,
            "Não foi possível abrir o picture-in-picture.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()

            activity.enterPictureInPictureMode(params)
        } else {
            activity.enterPictureInPictureMode()
        }
    } catch (exception: Exception) {
        Log.w(
            PLAYER_LOG_TAG,
            "Não foi possível abrir o picture-in-picture.",
            exception
        )

        Toast.makeText(
            context,
            "Não foi possível abrir o picture-in-picture.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@OptIn(UnstableApi::class)
private fun capturePlayerFrame(
    context: Context,
    playerView: PlayerView
) {
    val videoSurfaceView = playerView.videoSurfaceView

    when (videoSurfaceView) {
        is TextureView -> {
            val bitmap = videoSurfaceView.bitmap

            if (bitmap != null) {
                saveBitmapAndNotify(
                    context = context,
                    bitmap = bitmap
                )
            } else {
                captureViewFallback(
                    context = context,
                    playerView = playerView
                )
            }
        }

        is SurfaceView -> {
            if (
                videoSurfaceView.width <= 0 ||
                videoSurfaceView.height <= 0 ||
                !videoSurfaceView.holder.surface.isValid
            ) {
                Toast.makeText(
                    context,
                    "Não foi possível capturar esse frame.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val bitmap = createBitmap(
                videoSurfaceView.width,
                videoSurfaceView.height
            )

            PixelCopy.request(
                videoSurfaceView,
                bitmap,
                { result ->
                    if (result == PixelCopy.SUCCESS) {
                        saveBitmapAndNotify(
                            context = context,
                            bitmap = bitmap
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Não foi possível capturar esse frame.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }

        else -> {
            captureViewFallback(
                context = context,
                playerView = playerView
            )
        }
    }
}

private fun captureViewFallback(
    context: Context,
    playerView: PlayerView
) {
    if (playerView.width <= 0 || playerView.height <= 0) {
        Toast.makeText(
            context,
            "Player ainda não está pronto para captura.",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val bitmap = createBitmap(
        playerView.width,
        playerView.height
    )

    val canvas = Canvas(bitmap)
    playerView.draw(canvas)

    saveBitmapAndNotify(
        context = context,
        bitmap = bitmap
    )
}

private fun saveBitmapAndNotify(
    context: Context,
    bitmap: Bitmap
) {
    val uri = saveBitmapToPictures(
        context = context,
        bitmap = bitmap
    )

    if (uri != null) {
        Toast.makeText(
            context,
            "Captura salva na galeria.",
            Toast.LENGTH_SHORT
        ).show()
    } else {
        Toast.makeText(
            context,
            "Não foi possível salvar a captura.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun saveBitmapToPictures(
    context: Context,
    bitmap: Bitmap
): Uri? {
    val timestamp = SimpleDateFormat(
        "yyyyMMdd_HHmmss",
        Locale.US
    ).format(Date())

    val fileName = "laranjada_$timestamp.jpg"

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Laranjada"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver

            val uri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: return null

            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    95,
                    outputStream
                )
            } ?: return null

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)

            resolver.update(
                uri,
                values,
                null,
                null
            )

            uri
        } else {
            val directory = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "Laranjada"
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    95,
                    outputStream
                )
            }

            Uri.fromFile(file)
        }
    } catch (exception: Exception) {
        Log.w(
            PLAYER_LOG_TAG,
            "Não foi possível salvar a captura.",
            exception
        )

        null
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}