package com.rnx.laranjada.feature.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.domain.model.SupportConversationSummary

@Composable
fun SupportScreen(
    onBackClick: () -> Unit,
    onNewConversationClick: () -> Unit,
    onConversationClick: (String) -> Unit,
    onSessionEnded: () -> Unit,
    viewModel:
    SupportListViewModel =
        viewModel()
) {
    val state =
        viewModel.uiState

    val lifecycleOwner =
        LocalLifecycleOwner.current

    var resumedOnce by
    remember {
        mutableStateOf(
            false
        )
    }

    LaunchedEffect(
        Unit
    ) {
        viewModel.start(
            onSessionEnded =
                onSessionEnded
        )
    }

    DisposableEffect(
        lifecycleOwner
    ) {
        val observer =
            LifecycleEventObserver {
                    _,
                    event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {
                    if (
                        resumedOnce
                    ) {
                        viewModel.refresh(
                            onSessionEnded =
                                onSessionEnded
                        )
                    } else {
                        resumedOnce =
                            true
                    }
                }
            }

        lifecycleOwner.lifecycle
            .addObserver(
                observer
            )

        onDispose {
            lifecycleOwner.lifecycle
                .removeObserver(
                    observer
                )
        }
    }

    Scaffold(
        containerColor =
            LaranjadaBlack
    ) {
            scaffoldPadding ->

        when {
            state.isLoading &&
                    state.conversations.isEmpty() &&
                    state.creation == null -> {

                SupportLoadingState(
                    modifier =
                        Modifier.padding(
                            scaffoldPadding
                        )
                )
            }

            state.loadErrorMessage !=
                    null &&
                    state.creation == null -> {

                SupportErrorState(
                    message =
                        state.loadErrorMessage,

                    onRetry = {
                        viewModel.retry(
                            onSessionEnded =
                                onSessionEnded
                        )
                    },

                    modifier =
                        Modifier.padding(
                            scaffoldPadding
                        )
                )
            }

            else -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                scaffoldPadding
                            )
                ) {
                    LazyColumn(
                        modifier =
                            Modifier
                                .widthIn(
                                    max = 760.dp
                                )
                                .fillMaxSize()
                                .align(
                                    Alignment.TopCenter
                                )
                                .windowInsetsPadding(
                                    WindowInsets.statusBars
                                ),

                        contentPadding =
                            PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 32.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {
                        item {
                            SupportHeader(
                                onBackClick =
                                    onBackClick,

                                onNewConversationClick =
                                    onNewConversationClick
                            )
                        }

                        item {
                            NewConversationCard(
                                onClick =
                                    onNewConversationClick
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        8.dp
                                    )
                            )
                        }

                        if (
                            state.conversations.isEmpty()
                        ) {
                            item {
                                EmptySupportState()
                            }
                        } else {
                            items(
                                items =
                                    state.conversations,

                                key = {
                                        conversation ->

                                    conversation.uuid
                                }
                            ) {
                                    conversation ->

                                SupportConversationCard(
                                    conversation =
                                        conversation,

                                    onClick = {
                                        onConversationClick(
                                            conversation.uuid
                                        )
                                    }
                                )
                            }
                        }

                        if (
                            state.pagination
                                ?.hasNext ==
                            true
                        ) {
                            item {
                                Button(
                                    onClick = {
                                        viewModel.loadMore(
                                            onSessionEnded =
                                                onSessionEnded
                                        )
                                    },

                                    enabled =
                                        !state.isLoadingMore,

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(
                                                48.dp
                                            ),

                                    shape =
                                        RoundedCornerShape(
                                            8.dp
                                        ),

                                    colors =
                                        ButtonDefaults
                                            .buttonColors(
                                                containerColor =
                                                    SupportPanel,

                                                contentColor =
                                                    Color.White
                                            )
                                ) {
                                    if (
                                        state.isLoadingMore
                                    ) {
                                        CircularProgressIndicator(
                                            modifier =
                                                Modifier.size(
                                                    18.dp
                                                ),

                                            strokeWidth =
                                                2.dp,

                                            color =
                                                LaranjadaOrange
                                        )
                                    } else {
                                        Text(
                                            text =
                                                "CARREGAR MAIS",

                                            fontWeight =
                                                FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (
                            !state.loadMoreErrorMessage
                                .isNullOrBlank()
                        ) {
                            item {
                                Text(
                                    text =
                                        state.loadMoreErrorMessage,

                                    color =
                                        SupportRed,

                                    fontSize =
                                        12.sp,

                                    textAlign =
                                        TextAlign.Center,

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                vertical = 8.dp
                                            )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportHeader(
    onBackClick: () -> Unit,
    onNewConversationClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    82.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        IconButton(
            onClick =
                onBackClick
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored
                        .Rounded
                        .ArrowBack,

                contentDescription =
                    "Voltar",

                tint =
                    Color.White
            )
        }

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {
            Text(
                text =
                    "CENTRAL PRIVADA",

                color =
                    LaranjadaOrange,

                fontSize =
                    10.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                letterSpacing =
                    0.8.sp
            )

            Text(
                text =
                    "Atendimento",

                color =
                    Color.White,

                fontSize =
                    23.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                modifier =
                    Modifier.padding(
                        top = 2.dp
                    )
            )
        }

        Surface(
            modifier =
                Modifier
                    .size(
                        44.dp
                    )
                    .clickable(
                        onClick =
                            onNewConversationClick
                    ),

            shape =
                CircleShape,

            color =
                LaranjadaOrange
        ) {
            Box(
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Add,

                    contentDescription =
                        "Novo atendimento",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            25.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun NewConversationCard(
    onClick: () -> Unit
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            SupportOrangeDark
    ) {
        Row(
            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            44.dp
                        )
                        .background(
                            color =
                                LaranjadaOrange.copy(
                                    alpha = 0.15f
                                ),

                            shape =
                                RoundedCornerShape(
                                    11.dp
                                )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.ChatBubbleOutline,

                    contentDescription =
                        null,

                    tint =
                        LaranjadaOrange,

                    modifier =
                        Modifier.size(
                            22.dp
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        13.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Text(
                    text =
                        "Nova conversa",

                    color =
                        Color.White,

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Pedidos, suporte, reclamações e sugestões",

                    color =
                        LaranjadaMutedText,

                    fontSize =
                        12.sp,

                    modifier =
                        Modifier.padding(
                            top = 3.dp
                        )
                )
            }

            Icon(
                imageVector =
                    Icons.Rounded.ChevronRight,

                contentDescription =
                    null,

                tint =
                    LaranjadaOrange
            )
        }
    }
}

@Composable
private fun SupportConversationCard(
    conversation: SupportConversationSummary,
    onClick: () -> Unit
) {
    val statusColor =
        supportStatusColor(
            conversation.status.code
        )

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                12.dp
            ),

        color =
            SupportPanel
    ) {
        Row(
            modifier =
                Modifier.padding(
                    14.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            44.dp
                        )
                        .background(
                            color =
                                LaranjadaOrange.copy(
                                    alpha = 0.10f
                                ),

                            shape =
                                CircleShape
                        ),

                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        supportCategoryIcon(
                            conversation.category.code
                        ),

                    contentDescription =
                        null,

                    tint =
                        LaranjadaOrange,

                    modifier =
                        Modifier.size(
                            20.dp
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        13.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            conversation.subject,

                        color =
                            LaranjadaText,

                        fontSize =
                            15.sp,

                        fontWeight =
                            FontWeight.Bold,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis,

                        modifier =
                            Modifier.weight(
                                1f,
                                fill = false
                            )
                    )

                    if (
                        conversation.isNew
                    ) {
                        Spacer(
                            modifier =
                                Modifier.width(
                                    8.dp
                                )
                        )

                        Surface(
                            shape =
                                RoundedCornerShape(
                                    50
                                ),

                            color =
                                LaranjadaOrange.copy(
                                    alpha = 0.15f
                                )
                        ) {
                            Text(
                                text =
                                    "NOVA",

                                color =
                                    LaranjadaOrange,

                                fontSize =
                                    9.sp,

                                fontWeight =
                                    FontWeight.ExtraBold,

                                modifier =
                                    Modifier.padding(
                                        horizontal = 7.dp,
                                        vertical = 4.dp
                                    )
                            )
                        }
                    }
                }

                Row(
                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            conversation.category.label,

                        color =
                            LaranjadaMutedText,

                        fontSize =
                            11.sp,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        text =
                            " • ",

                        color =
                            LaranjadaMutedText,

                        fontSize =
                            11.sp
                    )

                    Text(
                        text =
                            conversation.status.label,

                        color =
                            statusColor,

                        fontSize =
                            11.sp,

                        fontWeight =
                            FontWeight.SemiBold
                    )
                }

                val dateText =
                    formatSupportListDate(
                        conversation.lastMessageAt
                            ?: conversation.createdAt
                    )

                if (
                    dateText.isNotBlank()
                ) {
                    Text(
                        text =
                            dateText,

                        color =
                            LaranjadaMutedText.copy(
                                alpha = 0.75f
                            ),

                        fontSize =
                            10.sp,

                        modifier =
                            Modifier.padding(
                                top = 4.dp
                            )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            Icon(
                imageVector =
                    Icons.Rounded.ChevronRight,

                contentDescription =
                    null,

                tint =
                    Color.White.copy(
                        alpha = 0.35f
                    )
            )
        }
    }
}

@Composable
private fun EmptySupportState() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 48.dp,
                    horizontal = 22.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector =
                Icons.Rounded.HeadsetMic,

            contentDescription =
                null,

            tint =
                LaranjadaOrange,

            modifier =
                Modifier.size(
                    42.dp
                )
        )

        Text(
            text =
                "Nenhuma conversa ainda",

            color =
                LaranjadaText,

            fontSize =
                17.sp,

            fontWeight =
                FontWeight.Bold,

            modifier =
                Modifier.padding(
                    top = 14.dp
                )
        )

        Text(
            text =
                "Quando precisar, inicie um atendimento e acompanhe tudo por aqui.",

            color =
                LaranjadaMutedText,

            fontSize =
                13.sp,

            lineHeight =
                18.sp,

            textAlign =
                TextAlign.Center,

            modifier =
                Modifier.padding(
                    top = 6.dp
                )
        )
    }
}

@Composable
private fun SupportLoadingState(
    modifier: Modifier =
        Modifier
) {
    Box(
        modifier =
            modifier
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
private fun SupportErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier =
        Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    LaranjadaBlack
                )
                .padding(
                    28.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {
        Icon(
            imageVector =
                Icons.Rounded.HeadsetMic,

            contentDescription =
                null,

            tint =
                LaranjadaOrange,

            modifier =
                Modifier.size(
                    42.dp
                )
        )

        Text(
            text =
                "Não foi possível abrir o atendimento",

            color =
                LaranjadaText,

            fontSize =
                18.sp,

            fontWeight =
                FontWeight.Bold,

            textAlign =
                TextAlign.Center,

            modifier =
                Modifier.padding(
                    top = 14.dp
                )
        )

        Text(
            text =
                message,

            color =
                LaranjadaMutedText,

            fontSize =
                13.sp,

            lineHeight =
                18.sp,

            textAlign =
                TextAlign.Center,

            modifier =
                Modifier.padding(
                    top = 7.dp
                )
        )

        Button(
            onClick =
                onRetry,

            colors =
                ButtonDefaults
                    .buttonColors(
                        containerColor =
                            LaranjadaOrange,

                        contentColor =
                            Color.White
                    ),

            modifier =
                Modifier.padding(
                    top = 20.dp
                )
        ) {
            Text(
                text =
                    "TENTAR NOVAMENTE",

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}