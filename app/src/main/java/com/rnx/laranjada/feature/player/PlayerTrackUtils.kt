package com.rnx.laranjada.feature.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class)
internal fun buildAudioOptions(
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

                    options.add(
                        PlayerTrackOption(
                            id = "audio_${groupIndex}_$trackIndex",
                            label = buildTrackLabel(
                                fallback = "Áudio ${trackIndex + 1}",
                                language = format.language,
                                name = format.label
                            ),
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
internal fun buildSubtitleOptions(
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

                    options.add(
                        PlayerTrackOption(
                            id = "subtitle_${groupIndex}_$trackIndex",
                            label = buildTrackLabel(
                                fallback = "Legenda ${trackIndex + 1}",
                                language = format.language,
                                name = format.label
                            ),
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
internal fun applyAudioSelection(
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
internal fun applySubtitleSelection(
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