package com.rnx.laranjada.feature.support

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
import com.rnx.laranjada.domain.model.SupportContentRequest
import com.rnx.laranjada.domain.model.SupportConversationInfo
import com.rnx.laranjada.domain.model.SupportMessage

@Composable
fun SupportConversationScreen(
    conversationUuid: String,
    onBackClick: () -> Unit,
    onNewConversationClick: () -> Unit,
    onSessionEnded: () -> Unit,
    viewModel:
    SupportConversationViewModel =
        viewModel()
) {
    val state =
        viewModel.uiState

    val lifecycleOwner =
        LocalLifecycleOwner.current

    val listState =
        rememberLazyListState()

    var messageBody by
    rememberSaveable(
        conversationUuid
    ) {
        mutableStateOf(
            ""
        )
    }

    var resumedOnce by
    remember {
        mutableStateOf(
            false
        )
    }

    LaunchedEffect(
        conversationUuid
    ) {
        viewModel.start(
            conversationUuid =
                conversationUuid,

            onSessionEnded =
                onSessionEnded
        )
    }

    DisposableEffect(
        lifecycleOwner,
        conversationUuid
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
                            conversationUuid =
                                conversationUuid,

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

    val detail =
        state.detail

    /*
     * [ALTERAÇÃO]
     *
     * O detalhe é nullable.
     *
     * Criamos uma referência local não nula
     * antes de acessar contentRequest.
     *
     * Isso resolve:
     *
     * Only safe (?.) or non-null asserted (!!.)
     * calls are allowed on a nullable receiver
     */
    LaunchedEffect(
        detail?.messages?.size
    ) {
        val currentDetail =
            detail
                ?: return@LaunchedEffect

        val messageCount =
            currentDetail
                .messages
                .size

        if (
            messageCount >
            0
        ) {
            listState.animateScrollToItem(
                index =
                    messageCount -
                            1 +
                            conversationHeaderItemCount(
                                currentDetail.contentRequest !=
                                        null
                            )
            )
        }
    }

    Scaffold(
        containerColor =
            LaranjadaBlack,

        bottomBar = {
            if (
                detail !=
                null
            ) {
                if (
                    detail.conversation
                        .canReply
                ) {
                    ConversationComposer(
                        value =
                            messageBody,

                        onValueChange = {
                            messageBody =
                                it

                            viewModel
                                .clearSendError()
                        },

                        isSending =
                            state.isSending,

                        errorMessage =
                            state.sendErrorMessage,

                        onSend = {
                            val normalized =
                                messageBody.trim()

                            if (
                                normalized.isBlank()
                            ) {
                                return@ConversationComposer
                            }

                            viewModel.sendMessage(
                                conversationUuid =
                                    conversationUuid,

                                body =
                                    normalized,

                                onSuccess = {
                                    messageBody =
                                        ""
                                },

                                onSessionEnded =
                                    onSessionEnded
                            )
                        }
                    )
                } else {
                    ClosedConversationBar()
                }
            }
        }
    ) {
            scaffoldPadding ->

        when {
            state.isLoading &&
                    detail == null -> {

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                scaffoldPadding
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

            detail == null -> {
                ConversationErrorState(
                    message =
                        state.loadErrorMessage
                            ?: "Não foi possível carregar o atendimento.",

                    onBackClick =
                        onBackClick,

                    onRetry = {
                        viewModel.retry(
                            conversationUuid =
                                conversationUuid,

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
                ConversationContent(
                    conversation =
                        detail.conversation,

                    contentRequest =
                        detail.contentRequest,

                    messages =
                        detail.messages,

                    listState =
                        listState,

                    onBackClick =
                        onBackClick,

                    onNewConversationClick =
                        onNewConversationClick,

                    modifier =
                        Modifier.padding(
                            scaffoldPadding
                        )
                )
            }
        }
    }
}

@Composable
private fun ConversationContent(
    conversation: SupportConversationInfo,
    contentRequest: SupportContentRequest?,
    messages: List<SupportMessage>,
    listState: LazyListState,
    onBackClick: () -> Unit,
    onNewConversationClick: () -> Unit,
    modifier: Modifier =
        Modifier
) {
    LazyColumn(
        state =
            listState,

        modifier =
            modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                ),

        contentPadding =
            PaddingValues(
                start = 14.dp,
                end = 14.dp,
                bottom = 18.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        item {
            ConversationHeader(
                conversation =
                    conversation,

                onBackClick =
                    onBackClick,

                onNewConversationClick =
                    onNewConversationClick
            )
        }

        if (
            contentRequest !=
            null
        ) {
            item {
                ContentRequestSummary(
                    contentRequest =
                        contentRequest
                )
            }
        }

        item {
            PrivateConversationNotice()
        }

        if (
            messages.isEmpty()
        ) {
            item {
                Text(
                    text =
                        "Nenhuma mensagem nesta conversa.",

                    color =
                        LaranjadaMutedText,

                    fontSize =
                        13.sp,

                    textAlign =
                        TextAlign.Center,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 36.dp
                            )
                )
            }
        }

        items(
            count =
                messages.size,

            key = {
                    index ->

                messages[index].uuid
            }
        ) {
                index ->

            SupportMessageBubble(
                message =
                    messages[index]
            )
        }
    }
}

@Composable
private fun ConversationHeader(
    conversation: SupportConversationInfo,
    onBackClick: () -> Unit,
    onNewConversationClick: () -> Unit
) {
    val statusColor =
        supportStatusColor(
            conversation.status.code
        )

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

        Box(
            modifier =
                Modifier
                    .size(
                        44.dp
                    )
                    .background(
                        color =
                            LaranjadaOrange.copy(
                                alpha = 0.12f
                            ),

                        shape =
                            CircleShape
                    ),

            contentAlignment =
                Alignment.Center
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
                        22.dp
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    11.dp
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
                    conversation.subject,

                color =
                    Color.White,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold,

                maxLines =
                    1
            )

            Row(
                modifier =
                    Modifier.padding(
                        top = 3.dp
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
                        11.sp
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
                        FontWeight.Bold
                )
            }
        }

        IconButton(
            onClick =
                onNewConversationClick
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Add,

                contentDescription =
                    "Novo atendimento",

                tint =
                    LaranjadaOrange
            )
        }
    }
}

@Composable
private fun ContentRequestSummary(
    contentRequest: SupportContentRequest
) {
    val decisionColor =
        supportDecisionColor(
            contentRequest.decision.code
        )

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                12.dp
            ),

        color =
            SupportPanel,

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    Color.White.copy(
                        alpha = 0.10f
                    )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    14.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            ContentSummaryRow(
                label =
                    "CONTEÚDO",

                value =
                    contentRequest.title
            )

            ContentSummaryRow(
                label =
                    "TIPO",

                value =
                    contentRequest
                        .contentType
                        .label
            )

            ContentSummaryRow(
                label =
                    "ANO",

                value =
                    contentRequest
                        .releaseYear
                        .toString()
            )

            ContentSummaryRow(
                label =
                    "DECISÃO",

                value =
                    contentRequest
                        .decision
                        .label,

                valueColor =
                    decisionColor
            )
        }
    }
}

@Composable
private fun ContentSummaryRow(
    label: String,
    value: String,
    valueColor: Color =
        Color.White
) {
    Column {
        Text(
            text =
                label,

            color =
                LaranjadaMutedText,

            fontSize =
                10.sp,

            fontWeight =
                FontWeight.Bold,

            letterSpacing =
                0.5.sp
        )

        Text(
            text =
                value,

            color =
                valueColor,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Bold,

            modifier =
                Modifier.padding(
                    top = 2.dp
                )
        )
    }
}

@Composable
private fun PrivateConversationNotice() {
    Surface(
        modifier =
            Modifier
                .widthIn(
                    max = 420.dp
                )
                .fillMaxWidth(),

        shape =
            RoundedCornerShape(
                50
            ),

        color =
            Color.White.copy(
                alpha = 0.04f
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),

            horizontalArrangement =
                Arrangement.Center,

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Lock,

                contentDescription =
                    null,

                tint =
                    LaranjadaMutedText,

                modifier =
                    Modifier.size(
                        13.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        6.dp
                    )
            )

            Text(
                text =
                    "Esta é uma conversa privada entre você e a equipe Laranjada.",

                color =
                    LaranjadaMutedText,

                fontSize =
                    10.sp,

                textAlign =
                    TextAlign.Center
            )
        }
    }
}

@Composable
private fun SupportMessageBubble(
    message: SupportMessage
) {
    val isUser =
        message.senderType ==
                "user"

    val isSystem =
        message.senderType ==
                "system"

    Box(
        modifier =
            Modifier.fillMaxWidth(),

        contentAlignment =
            when {
                isSystem ->
                    Alignment.Center

                isUser ->
                    Alignment.CenterEnd

                else ->
                    Alignment.CenterStart
            }
    ) {
        if (
            isSystem
        ) {
            Surface(
                shape =
                    RoundedCornerShape(
                        50
                    ),

                color =
                    Color.White.copy(
                        alpha = 0.06f
                    )
            ) {
                Text(
                    text =
                        message.body,

                    color =
                        LaranjadaMutedText,

                    fontSize =
                        11.sp,

                    lineHeight =
                        16.sp,

                    textAlign =
                        TextAlign.Center,

                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        )
                )
            }

            return@Box
        }

        Surface(
            modifier =
                Modifier.fillMaxWidth(
                    0.82f
                ),

            shape =
                RoundedCornerShape(
                    topStart =
                        14.dp,

                    topEnd =
                        14.dp,

                    bottomStart =
                        if (
                            isUser
                        ) {
                            14.dp
                        } else {
                            4.dp
                        },

                    bottomEnd =
                        if (
                            isUser
                        ) {
                            4.dp
                        } else {
                            14.dp
                        }
                ),

            color =
                if (
                    isUser
                ) {
                    SupportOrangeDark
                } else {
                    SupportPanelSecondary
                },

            border =
                if (
                    isUser
                ) {
                    BorderStroke(
                        width =
                            1.dp,

                        color =
                            LaranjadaOrange.copy(
                                alpha = 0.35f
                            )
                    )
                } else {
                    null
                }
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        horizontal = 13.dp,
                        vertical = 11.dp
                    )
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            message.senderLabel
                                .ifBlank {
                                    if (
                                        isUser
                                    ) {
                                        "Você"
                                    } else {
                                        "Equipe Laranjada"
                                    }
                                },

                        color =
                            if (
                                isUser
                            ) {
                                Color.White
                            } else {
                                LaranjadaOrange
                            },

                        fontSize =
                            11.sp,

                        fontWeight =
                            FontWeight.Bold,

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    Text(
                        text =
                            formatSupportTime(
                                message.createdAt
                            ),

                        color =
                            LaranjadaMutedText,

                        fontSize =
                            9.sp
                    )
                }

                Text(
                    text =
                        message.body,

                    color =
                        Color.White,

                    fontSize =
                        14.sp,

                    lineHeight =
                        20.sp,

                    modifier =
                        Modifier.padding(
                            top = 7.dp
                        )
                )

                if (
                    isUser &&
                    message.moderationLabel
                        .isNotBlank()
                ) {
                    Text(
                        text =
                            message.moderationLabel,

                        color =
                            LaranjadaMutedText,

                        fontSize =
                            9.sp,

                        modifier =
                            Modifier
                                .align(
                                    Alignment.End
                                )
                                .padding(
                                    top = 7.dp
                                )
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationComposer(
    value: String,
    onValueChange: (String) -> Unit,
    isSending: Boolean,
    errorMessage: String?,
    onSend: () -> Unit
) {
    Surface(
        color =
            Color(
                0xFF080A0C
            ),

        tonalElevation =
            0.dp
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    )
        ) {
            if (
                !errorMessage
                    .isNullOrBlank()
            ) {
                Text(
                    text =
                        errorMessage,

                    color =
                        SupportRed,

                    fontSize =
                        11.sp,

                    lineHeight =
                        15.sp,

                    modifier =
                        Modifier.padding(
                            start = 4.dp,
                            bottom = 6.dp
                        )
                )
            }

            Row(
                verticalAlignment =
                    Alignment.Bottom
            ) {
                OutlinedTextField(
                    value =
                        value,

                    onValueChange =
                        onValueChange,

                    placeholder = {
                        Text(
                            text =
                                "Escreva uma mensagem...",

                            color =
                                LaranjadaMutedText
                        )
                    },

                    minLines =
                        1,

                    maxLines =
                        4,

                    enabled =
                        !isSending,

                    keyboardOptions =
                        KeyboardOptions(
                            imeAction =
                                ImeAction.Send
                        ),

                    keyboardActions =
                        KeyboardActions(
                            onSend = {
                                if (
                                    value.trim()
                                        .isNotBlank() &&
                                    !isSending
                                ) {
                                    onSend()
                                }
                            }
                        ),

                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedTextColor =
                                Color.White,

                            unfocusedTextColor =
                                Color.White,

                            cursorColor =
                                LaranjadaOrange,

                            focusedBorderColor =
                                LaranjadaOrange,

                            unfocusedBorderColor =
                                Color.White.copy(
                                    alpha = 0.18f
                                ),

                            focusedContainerColor =
                                SupportPanel,

                            unfocusedContainerColor =
                                SupportPanel
                        ),

                    shape =
                        RoundedCornerShape(
                            12.dp
                        ),

                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Button(
                    onClick =
                        onSend,

                    enabled =
                        value.trim()
                            .isNotBlank() &&
                                !isSending,

                    modifier =
                        Modifier.size(
                            52.dp
                        ),

                    contentPadding =
                        PaddingValues(
                            0.dp
                        ),

                    shape =
                        RoundedCornerShape(
                            12.dp
                        ),

                    colors =
                        ButtonDefaults
                            .buttonColors(
                                containerColor =
                                    LaranjadaOrange,

                                contentColor =
                                    Color.White
                            )
                ) {
                    if (
                        isSending
                    ) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    19.dp
                                ),

                            strokeWidth =
                                2.dp,

                            color =
                                Color.White
                        )
                    } else {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored
                                    .Rounded
                                    .Send,

                            contentDescription =
                                "Enviar",

                            modifier =
                                Modifier.size(
                                    20.dp
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClosedConversationBar() {
    Surface(
        color =
            Color(
                0xFF080A0C
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        16.dp
                    ),

            horizontalArrangement =
                Arrangement.Center,

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Lock,

                contentDescription =
                    null,

                tint =
                    LaranjadaMutedText,

                modifier =
                    Modifier.size(
                        17.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        7.dp
                    )
            )

            Text(
                text =
                    "Esta conversa foi encerrada.",

                color =
                    LaranjadaMutedText,

                fontSize =
                    12.sp,

                fontWeight =
                    FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ConversationErrorState(
    message: String,
    onBackClick: () -> Unit,
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
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .padding(
                    horizontal = 16.dp
                )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        68.dp
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

            Text(
                text =
                    "Atendimento",

                color =
                    Color.White,

                fontSize =
                    21.sp,

                fontWeight =
                    FontWeight.ExtraBold
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(
                        1f
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {
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
                    TextAlign.Center
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
                        top = 18.dp
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
}

private fun conversationHeaderItemCount(
    hasContentRequest: Boolean
): Int {

    return if (
        hasContentRequest
    ) {
        3
    } else {
        2
    }
}