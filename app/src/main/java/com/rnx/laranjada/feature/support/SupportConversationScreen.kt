package com.rnx.laranjada.feature.support

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.rnx.laranjada.domain.model.SupportContentRequest
import com.rnx.laranjada.domain.model.SupportConversationInfo
import com.rnx.laranjada.domain.model.SupportMessage

@Composable
fun SupportConversationScreen(
    conversationUuid: String,
    onBackClick: () -> Unit,
    onNewConversationClick: () -> Unit,
    onSessionEnded: () -> Unit,
    viewModel: SupportConversationViewModel = viewModel()
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
        mutableStateOf("")
    }

    var resumedOnce by
    remember {
        mutableStateOf(false)
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
            .addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle
                .removeObserver(observer)
        }
    }

    val detail =
        state.detail

    LaunchedEffect(
        detail?.messages?.size
    ) {
        val currentDetail =
            detail
                ?: return@LaunchedEffect

        val messageCount =
            currentDetail.messages.size

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

    BoxWithConstraints(
        modifier =
            Modifier.fillMaxSize()
    ) {
        val isTablet =
            maxWidth >= 600.dp

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
                            },
                            isTablet =
                                isTablet
                        )
                    } else {
                        ClosedConversationBar(
                            isTablet =
                                isTablet
                        )
                    }
                }
            }
        ) {
                scaffoldPadding ->

            when {
                state.isLoading &&
                        detail ==
                        null -> {

                    Box(
                        modifier = Modifier
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

                detail ==
                        null -> {

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
                            ),
                        isTablet =
                            isTablet
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
                            ),
                        isTablet =
                            isTablet
                    )
                }
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
    modifier: Modifier = Modifier,
    isTablet: Boolean
) {
    Box(
        modifier =
            modifier.fillMaxSize(),
        contentAlignment =
            Alignment.TopCenter
    ) {
        LazyColumn(
            state =
                listState,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(
                    max =
                        if (isTablet) {
                            900.dp
                        } else {
                            560.dp
                        }
                )
                .windowInsetsPadding(
                    WindowInsets.statusBars
                ),
            contentPadding =
                PaddingValues(
                    start =
                        if (isTablet) {
                            28.dp
                        } else {
                            14.dp
                        },
                    end =
                        if (isTablet) {
                            28.dp
                        } else {
                            14.dp
                        },
                    bottom =
                        if (isTablet) {
                            24.dp
                        } else {
                            18.dp
                        }
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    if (isTablet) {
                        14.dp
                    } else {
                        10.dp
                    }
                )
        ) {
            item {
                ConversationHeader(
                    conversation =
                        conversation,
                    onBackClick =
                        onBackClick,
                    onNewConversationClick =
                        onNewConversationClick,
                    isTablet =
                        isTablet
                )
            }

            if (
                contentRequest !=
                null
            ) {
                item {
                    ContentRequestSummary(
                        contentRequest =
                            contentRequest,
                        isTablet =
                            isTablet
                    )
                }
            }

            item {
                PrivateConversationNotice(
                    isTablet =
                        isTablet
                )
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
                            if (isTablet) {
                                15.sp
                            } else {
                                13.sp
                            },
                        textAlign =
                            TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical =
                                    if (isTablet) {
                                        48.dp
                                    } else {
                                        36.dp
                                    }
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
                        messages[index],
                    isTablet =
                        isTablet
                )
            }
        }
    }
}

@Composable
private fun ConversationHeader(
    conversation: SupportConversationInfo,
    onBackClick: () -> Unit,
    onNewConversationClick: () -> Unit,
    isTablet: Boolean
) {
    val statusColor =
        supportStatusColor(
            conversation.status.code
        )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                if (isTablet) {
                    94.dp
                } else {
                    82.dp
                }
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        IconButton(
            onClick =
                onBackClick,
            modifier =
                Modifier.size(
                    if (isTablet) {
                        52.dp
                    } else {
                        48.dp
                    }
                )
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored
                        .Rounded
                        .ArrowBack,
                contentDescription =
                    "Voltar",
                tint =
                    Color.White,
                modifier =
                    Modifier.size(
                        if (isTablet) {
                            30.dp
                        } else {
                            24.dp
                        }
                    )
            )
        }

        Box(
            modifier = Modifier
                .size(
                    if (isTablet) {
                        52.dp
                    } else {
                        44.dp
                    }
                )
                .background(
                    color =
                        LaranjadaOrange.copy(
                            alpha =
                                0.12f
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
                        if (isTablet) {
                            26.dp
                        } else {
                            22.dp
                        }
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    if (isTablet) {
                        14.dp
                    } else {
                        11.dp
                    }
                )
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text =
                    conversation.subject,
                color =
                    Color.White,
                fontSize =
                    if (isTablet) {
                        18.sp
                    } else {
                        15.sp
                    },
                fontWeight =
                    FontWeight.Bold,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Row(
                modifier =
                    Modifier.padding(
                        top =
                            4.dp
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
                        if (isTablet) {
                            13.sp
                        } else {
                            11.sp
                        }
                )

                Text(
                    text =
                        " • ",
                    color =
                        LaranjadaMutedText,
                    fontSize =
                        if (isTablet) {
                            13.sp
                        } else {
                            11.sp
                        }
                )

                Text(
                    text =
                        conversation.status.label,
                    color =
                        statusColor,
                    fontSize =
                        if (isTablet) {
                            13.sp
                        } else {
                            11.sp
                        },
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        IconButton(
            onClick =
                onNewConversationClick,
            modifier =
                Modifier.size(
                    if (isTablet) {
                        52.dp
                    } else {
                        48.dp
                    }
                )
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Add,
                contentDescription =
                    "Novo atendimento",
                tint =
                    LaranjadaOrange,
                modifier =
                    Modifier.size(
                        if (isTablet) {
                            30.dp
                        } else {
                            24.dp
                        }
                    )
            )
        }
    }
}

@Composable
private fun ContentRequestSummary(
    contentRequest: SupportContentRequest,
    isTablet: Boolean
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
                if (isTablet) {
                    16.dp
                } else {
                    12.dp
                }
            ),
        color =
            SupportPanel,
        border =
            BorderStroke(
                width =
                    1.dp,
                color =
                    Color.White.copy(
                        alpha =
                            0.10f
                    )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    if (isTablet) {
                        20.dp
                    } else {
                        14.dp
                    }
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    if (isTablet) {
                        14.dp
                    } else {
                        10.dp
                    }
                )
        ) {
            ContentSummaryRow(
                label =
                    "CONTEÚDO",
                value =
                    contentRequest.title,
                isTablet =
                    isTablet
            )

            ContentSummaryRow(
                label =
                    "TIPO",
                value =
                    contentRequest
                        .contentType
                        .label,
                isTablet =
                    isTablet
            )

            ContentSummaryRow(
                label =
                    "ANO",
                value =
                    contentRequest
                        .releaseYear
                        .toString(),
                isTablet =
                    isTablet
            )

            ContentSummaryRow(
                label =
                    "DECISÃO",
                value =
                    contentRequest
                        .decision
                        .label,
                valueColor =
                    decisionColor,
                isTablet =
                    isTablet
            )
        }
    }
}

@Composable
private fun ContentSummaryRow(
    label: String,
    value: String,
    valueColor: Color = Color.White,
    isTablet: Boolean
) {
    Column {
        Text(
            text =
                label,
            color =
                LaranjadaMutedText,
            fontSize =
                if (isTablet) {
                    12.sp
                } else {
                    10.sp
                },
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
                if (isTablet) {
                    16.sp
                } else {
                    14.sp
                },
            fontWeight =
                FontWeight.Bold,
            modifier =
                Modifier.padding(
                    top =
                        3.dp
                )
        )
    }
}

@Composable
private fun PrivateConversationNotice(
    isTablet: Boolean
) {
    Surface(
        modifier = Modifier
            .widthIn(
                max =
                    if (isTablet) {
                        560.dp
                    } else {
                        420.dp
                    }
            )
            .fillMaxWidth(),
        shape =
            RoundedCornerShape(
                50.dp
            ),
        color =
            Color.White.copy(
                alpha =
                    0.04f
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal =
                        if (isTablet) {
                            16.dp
                        } else {
                            12.dp
                        },
                    vertical =
                        if (isTablet) {
                            10.dp
                        } else {
                            8.dp
                        }
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
                        if (isTablet) {
                            16.dp
                        } else {
                            13.dp
                        }
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
                    if (isTablet) {
                        12.sp
                    } else {
                        10.sp
                    },
                textAlign =
                    TextAlign.Center
            )
        }
    }
}

@Composable
private fun SupportMessageBubble(
    message: SupportMessage,
    isTablet: Boolean
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
                        50.dp
                    ),
                color =
                    Color.White.copy(
                        alpha =
                            0.06f
                    )
            ) {
                Text(
                    text =
                        message.body,
                    color =
                        LaranjadaMutedText,
                    fontSize =
                        if (isTablet) {
                            13.sp
                        } else {
                            11.sp
                        },
                    lineHeight =
                        if (isTablet) {
                            18.sp
                        } else {
                            16.sp
                        },
                    textAlign =
                        TextAlign.Center,
                    modifier =
                        Modifier.padding(
                            horizontal =
                                if (isTablet) {
                                    16.dp
                                } else {
                                    12.dp
                                },
                            vertical =
                                if (isTablet) {
                                    9.dp
                                } else {
                                    7.dp
                                }
                        )
                )
            }

            return@Box
        }

        Surface(
            modifier =
                Modifier.fillMaxWidth(
                    if (isTablet) {
                        0.72f
                    } else {
                        0.82f
                    }
                ),
            shape =
                RoundedCornerShape(
                    topStart =
                        if (isTablet) {
                            18.dp
                        } else {
                            14.dp
                        },
                    topEnd =
                        if (isTablet) {
                            18.dp
                        } else {
                            14.dp
                        },
                    bottomStart =
                        if (isUser) {
                            if (isTablet) {
                                18.dp
                            } else {
                                14.dp
                            }
                        } else {
                            4.dp
                        },
                    bottomEnd =
                        if (isUser) {
                            4.dp
                        } else {
                            if (isTablet) {
                                18.dp
                            } else {
                                14.dp
                            }
                        }
                ),
            color =
                if (isUser) {
                    SupportOrangeDark
                } else {
                    SupportPanelSecondary
                },
            border =
                if (isUser) {
                    BorderStroke(
                        width =
                            1.dp,
                        color =
                            LaranjadaOrange.copy(
                                alpha =
                                    0.35f
                            )
                    )
                } else {
                    null
                }
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        horizontal =
                            if (isTablet) {
                                17.dp
                            } else {
                                13.dp
                            },
                        vertical =
                            if (isTablet) {
                                14.dp
                            } else {
                                11.dp
                            }
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
                                    if (isUser) {
                                        "Você"
                                    } else {
                                        "Equipe Laranjada"
                                    }
                                },
                        color =
                            if (isUser) {
                                Color.White
                            } else {
                                LaranjadaOrange
                            },
                        fontSize =
                            if (isTablet) {
                                13.sp
                            } else {
                                11.sp
                            },
                        fontWeight =
                            FontWeight.Bold,
                        modifier =
                            Modifier.weight(1f)
                    )

                    Text(
                        text =
                            formatSupportTime(
                                message.createdAt
                            ),
                        color =
                            LaranjadaMutedText,
                        fontSize =
                            if (isTablet) {
                                11.sp
                            } else {
                                9.sp
                            }
                    )
                }

                Text(
                    text =
                        message.body,
                    color =
                        Color.White,
                    fontSize =
                        if (isTablet) {
                            16.sp
                        } else {
                            14.sp
                        },
                    lineHeight =
                        if (isTablet) {
                            23.sp
                        } else {
                            20.sp
                        },
                    modifier =
                        Modifier.padding(
                            top =
                                8.dp
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
                            if (isTablet) {
                                11.sp
                            } else {
                                9.sp
                            },
                        modifier = Modifier
                            .align(
                                Alignment.End
                            )
                            .padding(
                                top =
                                    7.dp
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
    onSend: () -> Unit,
    isTablet: Boolean
) {
    Surface(
        color =
            Color(0xFF080A0C),
        tonalElevation =
            0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment =
                Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(
                        max =
                            if (isTablet) {
                                900.dp
                            } else {
                                560.dp
                            }
                    )
                    .padding(
                        horizontal =
                            if (isTablet) {
                                28.dp
                            } else {
                                12.dp
                            },
                        vertical =
                            if (isTablet) {
                                14.dp
                            } else {
                                10.dp
                            }
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
                            if (isTablet) {
                                13.sp
                            } else {
                                11.sp
                            },
                        lineHeight =
                            if (isTablet) {
                                18.sp
                            } else {
                                15.sp
                            },
                        modifier =
                            Modifier.padding(
                                start =
                                    4.dp,
                                bottom =
                                    6.dp
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
                                    LaranjadaMutedText,
                                fontSize =
                                    if (isTablet) {
                                        15.sp
                                    } else {
                                        14.sp
                                    }
                            )
                        },
                        minLines =
                            1,
                        maxLines =
                            if (isTablet) {
                                5
                            } else {
                                4
                            },
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
                        textStyle =
                            TextStyle(
                                fontSize =
                                    if (isTablet) {
                                        16.sp
                                    } else {
                                        14.sp
                                    }
                            ),
                        colors =
                            OutlinedTextFieldDefaults
                                .colors(
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
                                            alpha =
                                                0.18f
                                        ),
                                    focusedContainerColor =
                                        SupportPanel,
                                    unfocusedContainerColor =
                                        SupportPanel
                                ),
                        shape =
                            RoundedCornerShape(
                                if (isTablet) {
                                    14.dp
                                } else {
                                    12.dp
                                }
                            ),
                        modifier =
                            Modifier.weight(1f)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                if (isTablet) {
                                    12.dp
                                } else {
                                    8.dp
                                }
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
                                if (isTablet) {
                                    60.dp
                                } else {
                                    52.dp
                                }
                            ),
                        contentPadding =
                            PaddingValues(0.dp),
                        shape =
                            RoundedCornerShape(
                                if (isTablet) {
                                    14.dp
                                } else {
                                    12.dp
                                }
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
                                        if (isTablet) {
                                            22.dp
                                        } else {
                                            19.dp
                                        }
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
                                        if (isTablet) {
                                            24.dp
                                        } else {
                                            20.dp
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClosedConversationBar(
    isTablet: Boolean
) {
    Surface(
        color =
            Color(0xFF080A0C)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    if (isTablet) {
                        20.dp
                    } else {
                        16.dp
                    }
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
                        if (isTablet) {
                            20.dp
                        } else {
                            17.dp
                        }
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(7.dp)
            )

            Text(
                text =
                    "Esta conversa foi encerrada.",
                color =
                    LaranjadaMutedText,
                fontSize =
                    if (isTablet) {
                        14.sp
                    } else {
                        12.sp
                    },
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
    modifier: Modifier = Modifier,
    isTablet: Boolean
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    LaranjadaBlack
                ),
        contentAlignment =
            Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(
                    max =
                        if (isTablet) {
                            760.dp
                        } else {
                            560.dp
                        }
                )
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .padding(
                    horizontal =
                        if (isTablet) {
                            28.dp
                        } else {
                            16.dp
                        }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        if (isTablet) {
                            82.dp
                        } else {
                            68.dp
                        }
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                IconButton(
                    onClick =
                        onBackClick,
                    modifier =
                        Modifier.size(
                            if (isTablet) {
                                52.dp
                            } else {
                                48.dp
                            }
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored
                                .Rounded
                                .ArrowBack,
                        contentDescription =
                            "Voltar",
                        tint =
                            Color.White,
                        modifier =
                            Modifier.size(
                                if (isTablet) {
                                    30.dp
                                } else {
                                    24.dp
                                }
                            )
                    )
                }

                Text(
                    text =
                        "Atendimento",
                    color =
                        Color.White,
                    fontSize =
                        if (isTablet) {
                            27.sp
                        } else {
                            21.sp
                        },
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                        if (isTablet) {
                            15.sp
                        } else {
                            13.sp
                        },
                    lineHeight =
                        if (isTablet) {
                            21.sp
                        } else {
                            18.sp
                        },
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
                            top =
                                18.dp
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