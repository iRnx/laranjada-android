package com.rnx.laranjada.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText

@Composable
internal fun PlayerControlsOverlay(
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
                    color = Color.White.copy(alpha = 0.92f)
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
                    color = LaranjadaText
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
internal fun PlayerTopIconButton(
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
internal fun PlayerProgressBar(
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
internal fun PlayerRoundButton(
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
internal fun PlayerBottomIconButton(
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