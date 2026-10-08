
package com.rnx.laranjada.feature.favorites.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.data.repository.FavoritesRepositoryImpl
import com.rnx.laranjada.domain.repository.FavoritesRepository
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val TAG = "FavoriteActionIcon"

@Composable
fun FavoriteActionIcon(
    contentType: String,
    contentUuid: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 28.dp,
    inactiveTint: Color = Color.White,
    activeTint: Color = LaranjadaOrange,
    profileKey: String = ""
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val repository: FavoritesRepository = remember {
        FavoritesRepositoryImpl()
    }

    val normalizedType = remember(contentType) {
        when (contentType.trim().lowercase()) {
            "movie", "movies" -> "movie"
            "series", "serie" -> "series"
            else -> ""
        }
    }

    val normalizedUuid = contentUuid.trim()

    val hasValidUuid = remember(normalizedUuid) {
        runCatching {
            UUID.fromString(normalizedUuid)
        }.isSuccess
    }

    val validContent =
        normalizedType.isNotBlank() &&
                hasValidUuid

    var isFavorite by remember(
        normalizedType,
        normalizedUuid,
        profileKey
    ) {
        mutableStateOf<Boolean?>(null)
    }

    var isChecking by remember(
        normalizedType,
        normalizedUuid,
        profileKey
    ) {
        mutableStateOf(false)
    }

    var isSaving by remember(
        normalizedType,
        normalizedUuid,
        profileKey
    ) {
        mutableStateOf(false)
    }

    var refreshVersion by remember(
        normalizedType,
        normalizedUuid,
        profileKey
    ) {
        mutableIntStateOf(0)
    }

    var showStatusError by remember(
        normalizedType,
        normalizedUuid,
        profileKey
    ) {
        mutableStateOf(false)
    }

    DisposableEffect(
        lifecycleOwner,
        normalizedType,
        normalizedUuid,
        profileKey
    ) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                event == Lifecycle.Event.ON_RESUME &&
                validContent
            ) {
                refreshVersion++
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(
        normalizedType,
        normalizedUuid,
        profileKey,
        refreshVersion
    ) {
        if (!validContent || isSaving) {
            return@LaunchedEffect
        }

        isChecking = true

        try {
            val result = repository.status(
                contentType = normalizedType,
                contentUuid = normalizedUuid
            )

            isFavorite = result.isFavorite
            showStatusError = false

        } catch (cancelled: CancellationException) {
            throw cancelled

        } catch (exception: Exception) {
            Log.w(
                TAG,
                "Falha ao consultar status do favorito.",
                exception
            )

            if (showStatusError) {
                Toast.makeText(
                    context,
                    exception.message
                        ?: "Não foi possível consultar Favoritos.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } finally {
            isChecking = false
        }
    }

    IconButton(
        onClick = {
            if (
                !validContent ||
                isChecking ||
                isSaving
            ) {
                return@IconButton
            }

            if (isFavorite == null) {
                showStatusError = true
                refreshVersion++
                return@IconButton
            }

            isSaving = true

            scope.launch {
                try {
                    val result = repository.toggle(
                        contentType = normalizedType,
                        contentUuid = normalizedUuid
                    )

                    isFavorite = result.isFavorite

                } catch (cancelled: CancellationException) {
                    throw cancelled

                } catch (exception: Exception) {
                    Log.w(
                        TAG,
                        "Falha ao atualizar favorito.",
                        exception
                    )

                    Toast.makeText(
                        context,
                        exception.message
                            ?: "Não foi possível atualizar Favoritos.",
                        Toast.LENGTH_SHORT
                    ).show()

                } finally {
                    isSaving = false
                }
            }
        },
        enabled =
            validContent &&
                    !isChecking &&
                    !isSaving,
        modifier = modifier
    ) {
        val selected = isFavorite == true

        Icon(
            imageVector =
                if (selected) {
                    Icons.Rounded.Favorite
                } else {
                    Icons.Rounded.FavoriteBorder
                },
            contentDescription =
                if (selected) {
                    "Remover dos favoritos"
                } else {
                    "Adicionar aos favoritos"
                },
            tint =
                if (selected) {
                    activeTint
                } else {
                    inactiveTint
                },
            modifier = Modifier.size(iconSize)
        )
    }
}
