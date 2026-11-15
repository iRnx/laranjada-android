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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val configuration =
        LocalConfiguration.current

    /*
     * TABLET
     *
     * smallestScreenWidthDp não muda quando
     * o aparelho gira.
     *
     * Dessa forma o mesmo tamanho de controles
     * é utilizado no tablet tanto em portrait
     * quanto em landscape.
     *
     * O celular mantém exatamente a escala
     * anterior.
     */
    val isTablet =
        configuration.smallestScreenWidthDp >=
                600

    val topHorizontalPadding =
        if (
            isTablet
        ) {
            26.dp
        } else {
            18.dp
        }

    val topVerticalPadding =
        if (
            isTablet
        ) {
            18.dp
        } else {
            14.dp
        }

    val topCloseIconSize =
        if (
            isTablet
        ) {
            35.dp
        } else {
            30.dp
        }

    val topRotationIconSize =
        if (
            isTablet
        ) {
            33.dp
        } else {
            28.dp
        }

    val topActionIconSize =
        if (
            isTablet
        ) {
            32.dp
        } else {
            27.dp
        }

    val topPictureInPictureIconSize =
        if (
            isTablet
        ) {
            33.dp
        } else {
            28.dp
        }

    val topButtonSpacing =
        if (
            isTablet
        ) {
            16.dp
        } else {
            14.dp
        }

    val bottomHorizontalPadding =
        if (
            isTablet
        ) {
            28.dp
        } else {
            18.dp
        }

    val bottomPadding =
        if (
            isTablet
        ) {
            24.dp
        } else {
            18.dp
        }

    val bottomActionIconSize =
        if (
            isTablet
        ) {
            27.dp
        } else {
            21.dp
        }

    val bottomActionSpacing =
        if (
            isTablet
        ) {
            8.dp
        } else {
            6.dp
        }

    val speedTextSize =
        if (
            isTablet
        ) {
            15.sp
        } else {
            14.sp
        }

    val timeTextSize =
        if (
            isTablet
        ) {
            15.sp
        } else {
            14.sp
        }

    val progressTimeSpacing =
        if (
            isTablet
        ) {
            14.dp
        } else {
            10.dp
        }

    val transportAreaHeight =
        if (
            isTablet
        ) {
            74.dp
        } else {
            58.dp
        }

    val replayContainerSize =
        if (
            isTablet
        ) {
            58.dp
        } else {
            46.dp
        }

    val replayIconSize =
        if (
            isTablet
        ) {
            31.dp
        } else {
            25.dp
        }

    val playContainerSize =
        if (
            isTablet
        ) {
            74.dp
        } else {
            58.dp
        }

    val playIconSize =
        if (
            isTablet
        ) {
            43.dp
        } else {
            34.dp
        }

    val transportButtonSpacing =
        if (
            isTablet
        ) {
            28.dp
        } else {
            20.dp
        }

    val orientationLockIconSize =
        if (
            isTablet
        ) {
            29.dp
        } else {
            24.dp
        }

    val episodesIconSize =
        if (
            isTablet
        ) {
            30.dp
        } else {
            25.dp
        }

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {
        /*
         * Gradiente dos controles.
         *
         * Mantido igual para não alterar
         * a aparência atual do vídeo.
         */
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Black.copy(
                                        alpha =
                                            0.38f
                                    ),

                                    Color.Transparent,

                                    Color.Black.copy(
                                        alpha =
                                            0.78f
                                    )
                                )
                        )
                    )
        )

        /*
         * CONTROLES SUPERIORES
         */
        Row(
            modifier =
                Modifier
                    .align(
                        Alignment.TopCenter
                    )
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            topHorizontalPadding,

                        vertical =
                            topVerticalPadding
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            PlayerTopIconButton(
                onClick =
                    onCloseClick
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Close,

                    contentDescription =
                        "Fechar player",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            topCloseIconSize
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        topButtonSpacing
                    )
            )

            PlayerTopIconButton(
                onClick =
                    onToggleOrientationClick
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.ScreenRotation,

                    contentDescription =
                        if (
                            preferLandscape
                        ) {
                            "Virar para modo em pé"
                        } else {
                            "Virar para modo deitado"
                        },

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            topRotationIconSize
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            PlayerTopIconButton(
                onClick =
                    onSpeedClick
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Speed,

                    contentDescription =
                        "Velocidade",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            topActionIconSize
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        topButtonSpacing
                    )
            )

            PlayerTopIconButton(
                onClick =
                    onScreenshotClick
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.PhotoCamera,

                    contentDescription =
                        "Capturar tela",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            topActionIconSize
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        topButtonSpacing
                    )
            )

            PlayerTopIconButton(
                onClick =
                    onPictureInPictureClick
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.PictureInPictureAlt,

                    contentDescription =
                        "Picture-in-picture",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            topPictureInPictureIconSize
                        )
                )
            }
        }

        /*
         * CONTROLES INFERIORES
         */
        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .fillMaxWidth()
                    .padding(
                        start =
                            bottomHorizontalPadding,

                        end =
                            bottomHorizontalPadding,

                        bottom =
                            bottomPadding
                    )
        ) {
            /*
             * Velocidade + áudio +
             * legenda + configurações.
             */
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom =
                                if (
                                    isTablet
                                ) {
                                    8.dp
                                } else {
                                    6.dp
                                }
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        if (
                            playbackSpeed ==
                            1f
                        ) {
                            "1x"
                        } else {
                            "${formatSpeed(playbackSpeed)}x"
                        },

                    color =
                        Color.White.copy(
                            alpha =
                                0.92f
                        ),

                    fontSize =
                        speedTextSize
                )

                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                PlayerBottomIconButton(
                    onClick =
                        onAudioClick
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Audiotrack,

                        contentDescription =
                            "Áudio",

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(
                                bottomActionIconSize
                            )
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            bottomActionSpacing
                        )
                )

                PlayerBottomIconButton(
                    onClick =
                        onSubtitleClick
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.ClosedCaption,

                        contentDescription =
                            "Legenda",

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(
                                bottomActionIconSize
                            )
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            bottomActionSpacing
                        )
                )

                PlayerBottomIconButton(
                    onClick =
                        onSubtitleSettingsClick
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Settings,

                        contentDescription =
                            "Configurações de legenda",

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(
                                bottomActionIconSize
                            )
                    )
                }
            }

            /*
             * Barra de progresso + tempo.
             */
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                PlayerProgressBar(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    currentPositionMs =
                        currentPositionMs,

                    durationMs =
                        durationMs,

                    draggedProgress =
                        draggedProgress,

                    onDraggedProgressChange =
                        onDraggedProgressChange,

                    onDragFinished =
                        onDragFinished
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            progressTimeSpacing
                        )
                )

                Text(
                    text =
                        "${formatTime(currentPositionMs)} / " +
                                formatTime(
                                    durationMs
                                ),

                    color =
                        LaranjadaText,

                    fontSize =
                        timeTextSize
                )
            }

            /*
             * Linha inferior.
             *
             * Cadeado à esquerda.
             * Transporte no centro.
             * Episódios à direita.
             */
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            transportAreaHeight
                        )
                        .padding(
                            top =
                                if (
                                    isTablet
                                ) {
                                    7.dp
                                } else {
                                    5.dp
                                }
                        )
            ) {
                PlayerBottomIconButton(
                    onClick =
                        onToggleOrientationLockClick,

                    modifier =
                        Modifier.align(
                            Alignment.CenterStart
                        )
                ) {
                    Icon(
                        imageVector =
                            if (
                                orientationLocked
                            ) {
                                Icons.Rounded.Lock
                            } else {
                                Icons.Rounded.LockOpen
                            },

                        contentDescription =
                            if (
                                orientationLocked
                            ) {
                                "Destravar orientação"
                            } else {
                                "Travar orientação"
                            },

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(
                                orientationLockIconSize
                            )
                    )
                }

                Row(
                    modifier =
                        Modifier.align(
                            Alignment.Center
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.Center
                ) {
                    PlayerRoundButton(
                        onClick =
                            onReplayClick,

                        containerSize =
                            replayContainerSize,

                        iconSize =
                            replayIconSize
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Replay10,

                            contentDescription =
                                "Voltar 10 segundos",

                            tint =
                                Color.White,

                            modifier =
                                Modifier.size(
                                    replayIconSize
                                )
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(
                                transportButtonSpacing
                            )
                    )

                    PlayerRoundButton(
                        onClick =
                            onPlayPauseClick,

                        containerSize =
                            playContainerSize,

                        iconSize =
                            playIconSize
                    ) {
                        Icon(
                            imageVector =
                                if (
                                    isPlaying
                                ) {
                                    Icons.Rounded.Pause
                                } else {
                                    Icons.Rounded.PlayArrow
                                },

                            contentDescription =
                                if (
                                    isPlaying
                                ) {
                                    "Pausar"
                                } else {
                                    "Reproduzir"
                                },

                            tint =
                                Color.White,

                            modifier =
                                Modifier.size(
                                    playIconSize
                                )
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(
                                transportButtonSpacing
                            )
                    )

                    PlayerRoundButton(
                        onClick =
                            onForwardClick,

                        containerSize =
                            replayContainerSize,

                        iconSize =
                            replayIconSize
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Forward10,

                            contentDescription =
                                "Avançar 10 segundos",

                            tint =
                                Color.White,

                            modifier =
                                Modifier.size(
                                    replayIconSize
                                )
                        )
                    }
                }

                if (
                    showEpisodesButton
                ) {
                    PlayerBottomIconButton(
                        onClick =
                            onEpisodesClick,

                        modifier =
                            Modifier.align(
                                Alignment.CenterEnd
                            )
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Menu,

                            contentDescription =
                                "Episódios",

                            tint =
                                Color.White,

                            modifier =
                                Modifier.size(
                                    episodesIconSize
                                )
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
    val configuration =
        LocalConfiguration.current

    val isTablet =
        configuration.smallestScreenWidthDp >=
                600

    val buttonSize =
        if (
            isTablet
        ) {
            52.dp
        } else {
            44.dp
        }

    IconButton(
        onClick =
            onClick,

        modifier =
            Modifier
                .size(
                    buttonSize
                )
                .clip(
                    CircleShape
                )
                .background(
                    Color.Black.copy(
                        alpha =
                            0.24f
                    )
                )
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
    val configuration =
        LocalConfiguration.current

    val isTablet =
        configuration.smallestScreenWidthDp >=
                600

    val safeDuration =
        durationMs.coerceAtLeast(
            1L
        )

    val progressValue =
        if (
            draggedProgress >=
            0f
        ) {
            draggedProgress
        } else {
            currentPositionMs
                .toFloat()
        }

    val progressFraction =
        if (
            durationMs >
            0L
        ) {
            progressValue
                .coerceIn(
                    0f,
                    safeDuration
                        .toFloat()
                ) /
                    safeDuration
                        .toFloat()
        } else {
            0f
        }

    val interactionHeight =
        if (
            isTablet
        ) {
            36.dp
        } else {
            28.dp
        }

    val thumbSize =
        if (
            isTablet
        ) {
            12.dp
        } else {
            9.dp
        }

    val trackHeight =
        if (
            isTablet
        ) {
            6.dp
        } else {
            4.dp
        }

    BoxWithConstraints(
        modifier =
            modifier
                .height(
                    interactionHeight
                )
                .pointerInput(
                    safeDuration
                ) {
                    detectTapGestures {
                            offset ->

                        val position =
                            (
                                    (
                                            offset.x /
                                                    size.width
                                            )
                                        .coerceIn(
                                            0f,
                                            1f
                                        ) *
                                            safeDuration
                                    )

                        onDraggedProgressChange(
                            position
                        )

                        onDragFinished()
                    }
                }
                .pointerInput(
                    safeDuration
                ) {
                    detectDragGestures(
                        onDragStart = {
                                offset ->

                            val position =
                                (
                                        (
                                                offset.x /
                                                        size.width
                                                )
                                            .coerceIn(
                                                0f,
                                                1f
                                            ) *
                                                safeDuration
                                        )

                            onDraggedProgressChange(
                                position
                            )
                        },

                        onDragEnd = {
                            onDragFinished()
                        },

                        onDragCancel = {
                            onDragFinished()
                        },

                        onDrag = {
                                change,
                                _ ->

                            val position =
                                (
                                        (
                                                change.position.x /
                                                        size.width
                                                )
                                            .coerceIn(
                                                0f,
                                                1f
                                            ) *
                                                safeDuration
                                        )

                            onDraggedProgressChange(
                                position
                            )
                        }
                    )
                },

        contentAlignment =
            Alignment.CenterStart
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        trackHeight
                    )
                    .clip(
                        RoundedCornerShape(
                            99.dp
                        )
                    )
                    .background(
                        Color.White.copy(
                            alpha =
                                0.32f
                        )
                    )
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth(
                        progressFraction
                    )
                    .height(
                        trackHeight
                    )
                    .clip(
                        RoundedCornerShape(
                            99.dp
                        )
                    )
                    .background(
                        LaranjadaOrange
                    )
        )

        Box(
            modifier =
                Modifier
                    .offset(
                        x =
                            (
                                    maxWidth -
                                            thumbSize
                                    ) *
                                    progressFraction
                    )
                    .size(
                        thumbSize
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        LaranjadaOrange
                    )
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
        onClick =
            onClick,

        modifier =
            Modifier
                .size(
                    containerSize
                )
                .clip(
                    CircleShape
                )
                .background(
                    Color.Black.copy(
                        alpha =
                            0.56f
                    )
                )
    ) {
        Box(
            modifier =
                Modifier.size(
                    iconSize
                ),

            contentAlignment =
                Alignment.Center
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
    val configuration =
        LocalConfiguration.current

    val isTablet =
        configuration.smallestScreenWidthDp >=
                600

    val buttonSize =
        if (
            isTablet
        ) {
            48.dp
        } else {
            40.dp
        }

    IconButton(
        onClick =
            onClick,

        modifier =
            modifier
                .size(
                    buttonSize
                )
                .clip(
                    CircleShape
                )
                .background(
                    Color.Black.copy(
                        alpha =
                            0.36f
                    )
                )
    ) {
        content()
    }
}