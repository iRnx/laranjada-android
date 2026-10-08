
package com.rnx.laranjada.feature.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.feature.favorites.components.FavoriteActionIcon

@Composable
fun DetailFavoriteActionsRow(
    contentType: String,
    contentUuid: String,
    hasWatchProgress: Boolean,
    onRestartClick: () -> Unit,
    profileKey: String = ""
) {
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasWatchProgress) {
            IconButton(
                onClick = onRestartClick,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Replay,
                    contentDescription = "Reiniciar",
                    tint = LaranjadaText,
                    modifier = Modifier.size(31.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(56.dp)
            )
        }

        FavoriteActionIcon(
            contentType = contentType,
            contentUuid = contentUuid,
            profileKey = profileKey,
            inactiveTint = LaranjadaText,
            iconSize = 31.dp,
            modifier = Modifier.size(52.dp)
        )
    }
}
