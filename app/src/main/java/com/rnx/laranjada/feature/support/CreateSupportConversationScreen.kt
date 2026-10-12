package com.rnx.laranjada.feature.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
    viewModel:
    SupportCreateViewModel =
        viewModel()
) {
    val state =
        viewModel.uiState

    var categoryCode by
    rememberSaveable {
        mutableStateOf(
            ""
        )
    }

    var subject by
    rememberSaveable {
        mutableStateOf(
            ""
        )
    }

    var contentTypeCode by
    rememberSaveable {
        mutableStateOf(
            ""
        )
    }

    var contentTitle by
    rememberSaveable {
        mutableStateOf(
            ""
        )
    }

    var releaseYear by
    rememberSaveable {
        mutableStateOf(
            ""
        )
    }

    var message by
    rememberSaveable {
        mutableStateOf(
            ""
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

    Scaffold(
        containerColor =
            LaranjadaBlack
    ) {
            scaffoldPadding ->

        when {
            state.isLoading &&
                    state.creation == null -> {

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

            state.creation == null -> {
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

                    modifier =
                        Modifier.padding(
                            scaffoldPadding
                        )
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
                        .coerceAtLeast(
                            1
                        )

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
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                scaffoldPadding
                            )
                            .windowInsetsPadding(
                                WindowInsets.statusBars
                            )
                            .imePadding()
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 30.dp
                            ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier =
                            Modifier
                                .widthIn(
                                    max = 760.dp
                                )
                                .fillMaxWidth()
                    ) {
                        CreateSupportHeader(
                            onBackClick =
                                onBackClick
                        )

                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 6.dp,
                                        bottom = 22.dp
                                    ),

                            shape =
                                RoundedCornerShape(
                                    16.dp
                                ),

                            color =
                                SupportPanel
                        ) {
                            Column(
                                modifier =
                                    Modifier.padding(
                                        20.dp
                                    ),

                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(
                                                62.dp
                                            )
                                            .background(
                                                color =
                                                    LaranjadaOrange.copy(
                                                        alpha = 0.12f
                                                    ),

                                                shape =
                                                    RoundedCornerShape(
                                                        18.dp
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
                                                30.dp
                                            )
                                    )
                                }

                                Text(
                                    text =
                                        "Como podemos ajudar?",

                                    color =
                                        Color.White,

                                    fontSize =
                                        23.sp,

                                    fontWeight =
                                        FontWeight.ExtraBold,

                                    modifier =
                                        Modifier.padding(
                                            top = 16.dp
                                        )
                                )

                                Text(
                                    text =
                                        "Escolha uma categoria e envie as informações necessárias. Seus dados de conta são identificados com segurança pelo sistema.",

                                    color =
                                        LaranjadaMutedText,

                                    fontSize =
                                        13.sp,

                                    lineHeight =
                                        19.sp,

                                    textAlign =
                                        TextAlign.Center,

                                    modifier =
                                        Modifier.padding(
                                            top = 8.dp
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
                                    state.submitErrorMessage
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
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    16.dp
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
                                }
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        16.dp
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
                                    ]
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        16.dp
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
                                    }
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
                                    "Resuma o assunto da conversa",

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
                                        }
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(
                                    16.dp
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
                                6,

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
                                    }
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
                                    12.sp,

                                lineHeight =
                                    17.sp,

                                modifier =
                                    Modifier.padding(
                                        top = 12.dp
                                    )
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(
                                    22.dp
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

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(
                                        52.dp
                                    ),

                            shape =
                                RoundedCornerShape(
                                    8.dp
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
                                            19.dp
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
                                            18.dp
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
private fun CreateSupportHeader(
    onBackClick: () -> Unit
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
                "Novo atendimento",

            color =
                Color.White,

            fontSize =
                21.sp,

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
    onSelected: (SupportChoice) -> Unit
) {
    var expanded by
    remember {
        mutableStateOf(
            false
        )
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
                14.sp,

            fontWeight =
                FontWeight.Bold,

            modifier =
                Modifier.padding(
                    bottom = 7.dp
                )
        )

        Box(
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                expanded =
                                    true
                            }
                        ),

                shape =
                    RoundedCornerShape(
                        9.dp
                    ),

                color =
                    SupportPanelSecondary
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 15.dp
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
                            14.sp,

                        fontWeight =
                            FontWeight.SemiBold,

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    Icon(
                        imageVector =
                            Icons.Rounded.ExpandMore,

                        contentDescription =
                            null,

                        tint =
                            Color.White.copy(
                                alpha = 0.65f
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
                    11.sp,

                modifier =
                    Modifier.padding(
                        top = 5.dp
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
    keyboardType: KeyboardType =
        KeyboardType.Text,
    singleLine: Boolean =
        true,
    minLines: Int =
        1,
    errorMessage: String? =
        null,
    supportingText: String? =
        null,
    counter: String? =
        null
) {
    Column {
        Text(
            text =
                label,

            color =
                LaranjadaText,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Bold,

            modifier =
                Modifier.padding(
                    bottom = 7.dp
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
                        LaranjadaMutedText
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
                            alpha = 0.18f
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
                    9.dp
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 5.dp
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
                        10.sp,

                    lineHeight =
                        14.sp,

                    modifier =
                        Modifier.weight(
                            1f
                        )
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
                            10.sp,

                        modifier =
                            Modifier.padding(
                                start = 8.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                10.dp
            ),

        color =
            SupportRed.copy(
                alpha = 0.10f
            )
    ) {
        Text(
            text =
                message,

            color =
                SupportRed,

            fontSize =
                12.sp,

            lineHeight =
                17.sp,

            modifier =
                Modifier.padding(
                    13.dp
                )
        )
    }
}

@Composable
private fun CreateSupportErrorState(
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
        CreateSupportHeader(
            onBackClick =
                onBackClick
        )

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