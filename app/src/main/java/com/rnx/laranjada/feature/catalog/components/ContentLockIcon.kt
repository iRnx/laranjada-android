package com.rnx.laranjada.feature.catalog.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ContentLockIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Filled.Lock,
        contentDescription = "Conteúdo indisponível no seu plano",
        tint = Color(0xFFFFD62C),
        modifier = modifier.size(14.dp)
    )
}
