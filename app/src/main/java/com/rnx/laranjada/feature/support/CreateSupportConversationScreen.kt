package com.rnx.laranjada.feature.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnx.laranjada.core.design.theme.LaranjadaBlack
import com.rnx.laranjada.core.design.theme.LaranjadaMutedText
import com.rnx.laranjada.core.design.theme.LaranjadaOrange
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.domain.model.SupportChoice
import com.rnx.laranjada.domain.model.SupportCreateRequest

@Composable
fun CreateSupportConversationScreen(
    onBackClick: () -> Unit,
    onConversationCreated: (String) -> Unit,
    onSessionEnded: () -> Unit,
    viewModel: SupportCreateViewModel = viewModel()
) {
    val state =
        viewModel.uiState

    var categoryCode by
    rememberSaveable {
        mutableStateOf("")
    }

    var subject by
    rememberSaveable {
        mutableStateOf("")
    }

    var contentTypeCode by
    rememberSaveable {
        mutableStateOf("")
    }

    var contentTitle by
    rememberSaveable {
        mutableStateOf("")
    }

    var releaseYear by
    rememberSaveable {
        mutableStateOf("")
    }

    var message by
    rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        viewModel.start(
            onSessionEnded =
                onSessionEnded
        )
    }

    Scaffold(
        containerColor =
            LaranjadaBlack
    ) {
            scaffoldPadding ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            val isTablet =
                maxWidth >= 600.dp

            val isExpanded =
                maxWidth >= 840.dp

            val contentMaxWidth =
                when {
                    isExpanded ->
                        860.dp

                    isTablet ->
                        740.dp

                    else ->
                        maxWidth
                }

            val horizontalPadding =
                if (isTablet) {
                    28.dp
                } else {
                    16.dp
                }

            when {
                state.isLoading &&
                        state.creation ==
                        null -> {

                    Box(
                        modifier =
                            Modifier.fillMaxSize(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color =
                                LaranjadaOrange
                        )
                    }
                }

                state.creation ==
                        null -> {

                    CreateSupportErrorState(
                        message =
                            state.loadErrorMessage
                                ?: "Não foi possível preparar um novo atendimento.",
                        onBackClick =
                            onBackClick,
                        onRetry = {
                            viewModel.retry(
                                onSessionEnded =
                                    onSessionEnded
                            )
                        },
                        isTablet =
                            isTablet
                    )
                }

                else -> {
                    val creation =
                        state.creation

                    val constraints =
                        creation.constraints

                    val isContentRequest =
                        categoryCode ==
                                "content_request"

                    val parsedYear =
                        releaseYear
                            .toIntOrNull()

                    val messageMinimum =
                        constraints
                            .initialMessageMinLength
                            .coerceAtLeast(1)

                    val messageValid =
                        message.trim().length >=
                                messageMinimum

                    val categoryValid =
                        categoryCode
                            .isNotBlank()

                    val generalValid =
                        !isContentRequest &&
                                subject.trim()
                                    .isNotBlank()

                    val contentRequestValid =
                        isContentRequest &&
                                contentTypeCode
                                    .isNotBlank() &&
                                contentTitle
                                    .trim()
                                    .isNotBlank() &&
                                parsedYear !=
                                null &&
                                (
                                        constraints.releaseYearMin <=
                                                0 ||
                                                parsedYear >=
                                                constraints.releaseYearMin
                                        ) &&
                                (
                                        constraints.releaseYearMax <=
                                                0 ||
                                                parsedYear <=
                                                constraints.releaseYearMax
                                        )

                    val canSubmit =
                        !state.isSubmitting &&
                                categoryValid &&
                                messageValid &&
                                (
                                        generalValid ||
                                                contentRequestValid
                                        )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(
                                WindowInsets.statusBars
                            )
                            .imePadding()
                            .verticalScroll(
                                rememberScrollState()
                            ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(
                                    max =
                                        contentMaxWidth
                                )
                                .padding(
                                    start =
                                        horizontalPadding,
                                    end =
                                        horizontalPadding,
                                    bottom =
                                        if (isTablet) {
                                            44.dp
                                        } else {
                                            30.dp
                                        }
                                )
                        ) {
                            CreateSupportHeader(
                                onBackClick =
                                    onBackClick,
                                isTablet =
                                    isTablet
                            )

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top =
                                            6.dp,
                                        bottom =
                                            if (isTablet) {
                                                28.dp
                                            } else {
                                                22.dp
                                            }
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        if (isTablet) {
                                            20.dp
                                        } else {
                                            16.dp
                                        }
                                    ),
                                color =
                                    SupportPanel
                            ) {
                                Column(
                                    modifier =
                                        Modifier.padding(
                                            if (isTablet) {
                                                28.dp
                                            } else {
                                                20.dp
                                            }
                                        ),
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(
                                                if (isTablet) {
                                                    74.dp
                                                } else {
                                                    62.dp
                                                }
                                            )
                                            .background(
                                                color =
                                                    LaranjadaOrange.copy(
                                                        alpha =
                                                            0.12f
                                                    ),
                                                shape =
                                                    RoundedCornerShape(
                                                        if (isTablet) {
                                                            21.dp
                                                        } else {
                                                            18.dp
                                                        }
                                                    )
                                            ),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector =
                                                Icons.Rounded
                                                    .ChatBubbleOutline,
                                            contentDescription =
                                                null,
                                            tint =
                                                LaranjadaOrange,
                                            modifier =
                                                Modifier.size(
                                                    if (isTablet) {
                                                        36.dp
                                                    } else {
                                                        30.dp
                                                    }
                                                )
                                        )
                                    }

                                    Text(
                                        text =
                                            "Como podemos ajudar?",
                                        color =
                                            Color.White,
                                        fontSize =
                                            if (isTablet) {
                                                28.sp
                                            } else {
                                                23.sp
                                            },
                                        fontWeight =
                                            FontWeight.ExtraBold,
                                        modifier =
                                            Modifier.padding(
                                                top =
                                                    16.dp
                                            )
                                    )

                                    Text(
                                        text =
                                            "Escolha uma categoria e envie as informações necessárias. Seus dados de conta são identificados com segurança pelo sistema.",
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
                                                22.sp
                                            } else {
                                                19.sp
                                            },
                                        textAlign =
                                            TextAlign.Center,
                                        modifier = Modifier
                                            .widthIn(
                                                max =
                                                    640.dp
                                            )
                                            .padding(
                                                top =
                                                    9.dp
                                            )
                                    )
                                }
                            }

                            if (
                                !state.submitErrorMessage
                                    .isNullOrBlank()
                            ) {
                                ErrorCard(
                                    message =
                                        state.submitErrorMessage,
                                    isTablet =
                                        isTablet
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            14.dp
                                        )
                                )
                            }

                            SupportChoiceField(
                                label =
                                    "Sobre o que você quer falar?",
                                selectedCode =
                                    categoryCode,
                                options =
                                    creation.categories,
                                errorMessage =
                                    state.fieldErrors[
                                        "category"
                                    ],
                                onSelected = {
                                        option ->

                                    categoryCode =
                                        option.code

                                    subject =
                                        ""

                                    contentTypeCode =
                                        ""

                                    contentTitle =
                                        ""

                                    releaseYear =
                                        ""

                                    viewModel
                                        .clearSubmitErrors()
                                },
                                isTablet =
                                    isTablet
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        if (isTablet) {
                                            20.dp
                                        } else {
                                            16.dp
                                        }
                                    )
                            )

                            if (
                                isContentRequest
                            ) {
                                SupportChoiceField(
                                    label =
                                        "Tipo de conteúdo",
                                    selectedCode =
                                        contentTypeCode,
                                    options =
                                        creation.contentTypes,
                                    errorMessage =
                                        state.fieldErrors[
                                            "content_type"
                                        ],
                                    onSelected = {
                                            option ->

                                        contentTypeCode =
                                            option.code

                                        viewModel
                                            .clearSubmitErrors()
                                    },
                                    isTablet =
                                        isTablet
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            if (isTablet) {
                                                20.dp
                                            } else {
                                                16.dp
                                            }
                                        )
                                )

                                SupportTextField(
                                    value =
                                        contentTitle,
                                    onValueChange = {
                                        contentTitle =
                                            it

                                        viewModel
                                            .clearSubmitErrors()
                                    },
                                    label =
                                        "Nome do conteúdo",
                                    placeholder =
                                        "Ex.: O Senhor dos Anéis",
                                    errorMessage =
                                        state.fieldErrors[
                                            "content_title"
                                        ],
                                    isTablet =
                                        isTablet
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            if (isTablet) {
                                                20.dp
                                            } else {
                                                16.dp
                                            }
                                        )
                                )

                                SupportTextField(
                                    value =
                                        releaseYear,
                                    onValueChange = {
                                            newValue ->

                                        releaseYear =
                                            newValue.filter {
                                                    character ->

                                                character.isDigit()
                                            }

                                        viewModel
                                            .clearSubmitErrors()
                                    },
                                    label =
                                        "Ano de lançamento",
                                    placeholder =
                                        "Ex.: 2001",
                                    keyboardType =
                                        KeyboardType.Number,
                                    errorMessage =
                                        state.fieldErrors[
                                            "release_year"
                                        ],
                                    supportingText =
                                        if (
                                            constraints.releaseYearMin >
                                            0 &&
                                            constraints.releaseYearMax >
                                            0
                                        ) {
                                            "Informe um ano entre ${constraints.releaseYearMin} e ${constraints.releaseYearMax}."
                                        } else {
                                            "Informe o ano da obra para evitar confusão com títulos semelhantes."
                                        },
                                    isTablet =
                                        isTablet
                                )
                            } else {
                                SupportTextField(
                                    value =
                                        subject,
                                    onValueChange = {
                                            newValue ->

                                        subject =
                                            if (
                                                constraints.subjectMaxLength >
                                                0
                                            ) {
                                                newValue.take(
                                                    constraints.subjectMaxLength
                                                )
                                            } else {
                                                newValue
                                            }

                                        viewModel
                                            .clearSubmitErrors()
                                    },
                                    label =
                                        "Assunto",
                                    placeholder =
                                        "Resuma o motivo do atendimento",
                                    errorMessage =
                                        state.fieldErrors[
                                            "subject"
                                        ],
                                    counter =
                                        constraints
                                            .subjectMaxLength
                                            .takeIf {
                                                it > 0
                                            }
                                            ?.let {
                                                    max ->

                                                "${subject.length}/$max"
                                            },
                                    isTablet =
                                        isTablet
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        if (isTablet) {
                                            20.dp
                                        } else {
                                            16.dp
                                        }
                                    )
                            )

                            SupportTextField(
                                value =
                                    message,
                                onValueChange = {
                                        newValue ->

                                    message =
                                        if (
                                            constraints.messageMaxLength >
                                            0
                                        ) {
                                            newValue.take(
                                                constraints.messageMaxLength
                                            )
                                        } else {
                                            newValue
                                        }

                                    viewModel
                                        .clearSubmitErrors()
                                },
                                label =
                                    "Mensagem",
                                placeholder =
                                    "Explique o pedido ou descreva como podemos ajudar.",
                                singleLine =
                                    false,
                                minLines =
                                    if (isTablet) {
                                        7
                                    } else {
                                        6
                                    },
                                errorMessage =
                                    state.fieldErrors[
                                        "message"
                                    ],
                                supportingText =
                                    "Não envie senha, CVV, cartão completo ou códigos de segurança.",
                                counter =
                                    constraints
                                        .messageMaxLength
                                        .takeIf {
                                            it > 0
                                        }
                                        ?.let {
                                                max ->

                                            "${message.length}/$max"
                                        },
                                isTablet =
                                    isTablet
                            )

                            val nonFieldError =
                                state.fieldErrors[
                                    "non_field_errors"
                                ]

                            if (
                                !nonFieldError
                                    .isNullOrBlank()
                            ) {
                                Text(
                                    text =
                                        nonFieldError,
                                    color =
                                        SupportRed,
                                    fontSize =
                                        if (isTablet) {
                                            14.sp
                                        } else {
                                            12.sp
                                        },
                                    lineHeight =
                                        if (isTablet) {
                                            20.sp
                                        } else {
                                            17.sp
                                        },
                                    modifier =
                                        Modifier.padding(
                                            top =
                                                12.dp
                                        )
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        if (isTablet) {
                                            28.dp
                                        } else {
                                            22.dp
                                        }
                                    )
                            )

                            Button(
                                onClick = {
                                    viewModel.createConversation(
                                        request =
                                            SupportCreateRequest(
                                                category =
                                                    categoryCode,
                                                subject =
                                                    subject.trim(),
                                                contentType =
                                                    contentTypeCode,
                                                contentTitle =
                                                    contentTitle.trim(),
                                                releaseYear =
                                                    parsedYear,
                                                message =
                                                    message.trim()
                                            ),
                                        onSuccess =
                                            onConversationCreated,
                                        onSessionEnded =
                                            onSessionEnded
                                    )
                                },
                                enabled =
                                    canSubmit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(
                                        if (isTablet) {
                                            60.dp
                                        } else {
                                            52.dp
                                        }
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        if (isTablet) {
                                            10.dp
                                        } else {
                                            8.dp
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
                                    state.isSubmitting
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
                                    Text(
                                        text =
                                            "ENVIAR ATENDIMENTO",
                                        fontSize =
                                            if (isTablet) {
                                                16.sp
                                            } else {
                                                14.sp
                                            },
                                        fontWeight =
                                            FontWeight.ExtraBold
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                8.dp
                                            )
                                    )

                                    Icon(
                                        imageVector =
                                            Icons.Rounded.Send,
                                        contentDescription =
                                            null,
                                        modifier =
                                            Modifier.size(
                                                if (isTablet) {
                                                    21.dp
                                                } else {
                                                    18.dp
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
    }
}

@Composable
private fun CreateSupportHeader(
    onBackClick: () -> Unit,
    isTablet: Boolean
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

        Spacer(
            modifier =
                Modifier.width(
                    if (isTablet) {
                        8.dp
                    } else {
                        0.dp
                    }
                )
        )

        Text(
            text =
                "Novo atendimento",
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
}

@Composable
private fun SupportChoiceField(
    label: String,
    selectedCode: String,
    options: List<SupportChoice>,
    errorMessage: String?,
    onSelected: (SupportChoice) -> Unit,
    isTablet: Boolean
) {
    var expanded by
    remember {
        mutableStateOf(false)
    }

    val selected =
        options.firstOrNull {
            it.code ==
                    selectedCode
        }

    Column {
        Text(
            text =
                label,
            color =
                LaranjadaText,
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
                    bottom =
                        if (isTablet) {
                            9.dp
                        } else {
                            7.dp
                        }
                )
        )

        Box(
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded =
                            true
                    },
                shape =
                    RoundedCornerShape(
                        if (isTablet) {
                            12.dp
                        } else {
                            9.dp
                        }
                    ),
                color =
                    SupportPanelSecondary
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            horizontal =
                                if (isTablet) {
                                    18.dp
                                } else {
                                    14.dp
                                },
                            vertical =
                                if (isTablet) {
                                    18.dp
                                } else {
                                    15.dp
                                }
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            selected?.label
                                ?: "Selecione",
                        color =
                            if (
                                selected !=
                                null
                            ) {
                                Color.White
                            } else {
                                LaranjadaMutedText
                            },
                        fontSize =
                            if (isTablet) {
                                16.sp
                            } else {
                                14.sp
                            },
                        fontWeight =
                            FontWeight.SemiBold,
                        modifier =
                            Modifier.weight(1f)
                    )

                    Icon(
                        imageVector =
                            Icons.Rounded.ExpandMore,
                        contentDescription =
                            null,
                        tint =
                            Color.White.copy(
                                alpha =
                                    0.65f
                            ),
                        modifier =
                            Modifier.size(
                                if (isTablet) {
                                    28.dp
                                } else {
                                    24.dp
                                }
                            )
                    )
                }
            }

            DropdownMenu(
                expanded =
                    expanded,
                onDismissRequest = {
                    expanded =
                        false
                }
            ) {
                options.forEach {
                        option ->

                    DropdownMenuItem(
                        text = {
                            Text(
                                text =
                                    option.label
                            )
                        },
                        onClick = {
                            expanded =
                                false

                            onSelected(
                                option
                            )
                        }
                    )
                }
            }
        }

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
                modifier =
                    Modifier.padding(
                        top =
                            5.dp
                    )
            )
        }
    }
}

@Composable
private fun SupportTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    errorMessage: String? = null,
    supportingText: String? = null,
    counter: String? = null,
    isTablet: Boolean
) {
    Column {
        Text(
            text =
                label,
            color =
                LaranjadaText,
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
                    bottom =
                        if (isTablet) {
                            9.dp
                        } else {
                            7.dp
                        }
                )
        )

        OutlinedTextField(
            value =
                value,
            onValueChange =
                onValueChange,
            placeholder = {
                Text(
                    text =
                        placeholder,
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
            singleLine =
                singleLine,
            minLines =
                minLines,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        keyboardType
                ),
            isError =
                !errorMessage
                    .isNullOrBlank(),
            textStyle =
                androidx.compose.ui.text.TextStyle(
                    fontSize =
                        if (isTablet) {
                            16.sp
                        } else {
                            14.sp
                        }
                ),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor =
                        Color.White,
                    unfocusedTextColor =
                        Color.White,
                    focusedBorderColor =
                        LaranjadaOrange,
                    unfocusedBorderColor =
                        Color.White.copy(
                            alpha =
                                0.18f
                        ),
                    errorBorderColor =
                        SupportRed,
                    cursorColor =
                        LaranjadaOrange,
                    focusedContainerColor =
                        SupportPanelSecondary,
                    unfocusedContainerColor =
                        SupportPanelSecondary
                ),
            shape =
                RoundedCornerShape(
                    if (isTablet) {
                        12.dp
                    } else {
                        9.dp
                    }
                ),
            modifier =
                Modifier.fillMaxWidth()
        )

        if (
            !errorMessage
                .isNullOrBlank() ||
            !supportingText
                .isNullOrBlank() ||
            !counter
                .isNullOrBlank()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            5.dp
                    ),
                verticalAlignment =
                    Alignment.Top
            ) {
                Text(
                    text =
                        errorMessage
                            ?: supportingText
                                .orEmpty(),
                    color =
                        if (
                            !errorMessage
                                .isNullOrBlank()
                        ) {
                            SupportRed
                        } else {
                            LaranjadaMutedText
                        },
                    fontSize =
                        if (isTablet) {
                            12.sp
                        } else {
                            10.sp
                        },
                    lineHeight =
                        if (isTablet) {
                            17.sp
                        } else {
                            14.sp
                        },
                    modifier =
                        Modifier.weight(1f)
                )

                if (
                    !counter
                        .isNullOrBlank()
                ) {
                    Text(
                        text =
                            counter,
                        color =
                            LaranjadaMutedText,
                        fontSize =
                            if (isTablet) {
                                12.sp
                            } else {
                                10.sp
                            },
                        modifier =
                            Modifier.padding(
                                start =
                                    8.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    isTablet: Boolean
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                if (isTablet) {
                    13.dp
                } else {
                    10.dp
                }
            ),
        color =
            SupportRed.copy(
                alpha =
                    0.10f
            )
    ) {
        Text(
            text =
                message,
            color =
                SupportRed,
            fontSize =
                if (isTablet) {
                    14.sp
                } else {
                    12.sp
                },
            lineHeight =
                if (isTablet) {
                    20.sp
                } else {
                    17.sp
                },
            modifier =
                Modifier.padding(
                    if (isTablet) {
                        16.dp
                    } else {
                        13.dp
                    }
                )
        )
    }
}

@Composable
private fun CreateSupportErrorState(
    message: String,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    isTablet: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                LaranjadaBlack
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
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(
                    max =
                        if (isTablet) {
                            760.dp
                        } else {
                            560.dp
                        }
                )
                .weight(1f)
        ) {
            CreateSupportHeader(
                onBackClick =
                    onBackClick,
                isTablet =
                    isTablet
            )

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