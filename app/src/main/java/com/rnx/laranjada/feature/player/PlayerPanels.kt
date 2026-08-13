package com.rnx.laranjada.feature.player

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaSurface
import com.rnx.laranjada.core.design.theme.LaranjadaText

@Composable
internal fun FloatingTrackOptionsPanel(
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
internal fun FloatingSpeedPanel(
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
internal fun SubtitleSettingsPanel(
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