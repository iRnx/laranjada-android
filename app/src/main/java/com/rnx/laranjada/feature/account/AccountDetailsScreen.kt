package com.rnx.laranjada.feature.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.rnx.laranjada.core.design.theme.LaranjadaSurfaceLight
import com.rnx.laranjada.core.design.theme.LaranjadaText
import com.rnx.laranjada.domain.model.AccountDevice
import com.rnx.laranjada.domain.model.AccountProfilesSummary
import com.rnx.laranjada.domain.model.AccountUser
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val AccountPanel =
    Color(
        0xFF101417
    )

private val AccountPanelSecondary =
    Color(
        0xFF151A1F
    )

private val AccountGreen =
    Color(
        0xFF3FB950
    )

private val AccountRed =
    Color(
        0xFFFF6B6B
    )

@Composable
fun AccountDetailsScreen(
    onBackClick: () -> Unit,
    onManageProfilesClick: () -> Unit,
    onSessionEnded: () -> Unit,
    viewModel:
    AccountDetailsViewModel =
        viewModel()
) {
    val state =
        viewModel.uiState

    val lifecycleOwner =
        LocalLifecycleOwner.current

    val snackbar =
        remember {
            SnackbarHostState()
        }

    var showPasswordDialog by
    remember {
        mutableStateOf(
            false
        )
    }

    var personalInfoExpanded by
    rememberSaveable {
        mutableStateOf(
            false
        )
    }

    var devicesExpanded by
    rememberSaveable {
        mutableStateOf(
            false
        )
    }

    var pendingDevice by
    remember {
        mutableStateOf<
                AccountDevice?
                >(
            null
        )
    }

    var showDisconnectOthersDialog by
    remember {
        mutableStateOf(
            false
        )
    }

    var showDisconnectAllDialog by
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
                    viewModel
                        .refreshAfterReturn(
                            onSessionEnded =
                                onSessionEnded
                        )
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

    LaunchedEffect(
        viewModel.feedbackMessage
    ) {
        val message =
            viewModel.feedbackMessage
                ?: return@LaunchedEffect

        viewModel.consumeFeedback()

        snackbar.showSnackbar(
            message
        )
    }

    Scaffold(
        containerColor =
            LaranjadaBlack,

        snackbarHost = {
            SnackbarHost(
                hostState =
                    snackbar
            )
        }
    ) {
            scaffoldPadding ->

        when {
            state.isLoading &&
                    state.overview == null -> {
                AccountLoadingState(
                    modifier =
                        Modifier.padding(
                            scaffoldPadding
                        )
                )
            }

            state.overview == null -> {
                AccountErrorState(
                    message =
                        state.loadErrorMessage
                            ?: "Não foi possível carregar sua conta.",

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
                val overview =
                    state.overview

                val deviceActionsLocked =
                    state.deviceActionUuid !=
                            null ||
                            state.isDisconnectingOthers ||
                            state.isDisconnectingAll

                /*
                 * O dispositivo usado agora
                 * aparece sempre primeiro.
                 *
                 * Os demais mantêm a ordem
                 * recebida da API.
                 */
                val orderedDevices =
                    overview.devices.items
                        .filter {
                            it.isCurrentDevice
                        } +
                            overview.devices.items
                                .filterNot {
                                    it.isCurrentDevice
                                }

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
                                .fillMaxWidth()
                                .fillMaxHeight()
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
                            )
                    ) {
                        item {
                            AccountTopBar(
                                onBackClick =
                                    onBackClick
                            )
                        }

                        item {
                            AccountIdentityCard(
                                account =
                                    overview.account
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        20.dp
                                    )
                            )
                        }

                        /*
                         * INFORMAÇÕES PESSOAIS
                         */
                        item {
                            ExpandableAccountSection(
                                icon =
                                    Icons.Rounded.Person,

                                title =
                                    "Informações pessoais",

                                subtitle =
                                    "Dados principais da sua conta.",

                                expanded =
                                    personalInfoExpanded,

                                onClick = {
                                    personalInfoExpanded =
                                        !personalInfoExpanded
                                }
                            )

                            if (
                                personalInfoExpanded
                            ) {
                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            10.dp
                                        )
                                )

                                PersonalInformationCard(
                                    account =
                                        overview.account
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        14.dp
                                    )
                            )
                        }

                        /*
                         * SEGURANÇA
                         */
                        item {
                            SecurityCard(
                                onChangePasswordClick = {
                                    viewModel
                                        .clearPasswordError()

                                    showPasswordDialog =
                                        true
                                }
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        14.dp
                                    )
                            )
                        }

                        /*
                         * DISPOSITIVOS
                         */
                        item {
                            ExpandableAccountSection(
                                icon =
                                    Icons.Rounded.Devices,

                                title =
                                    "Dispositivos reconhecidos",

                                subtitle =
                                    buildString {
                                        append(
                                            quantityText(
                                                overview.devices
                                                    .connectedCount,

                                                "conectado",
                                                "conectados"
                                            )
                                        )

                                        append(
                                            " • "
                                        )

                                        append(
                                            quantityText(
                                                overview.devices
                                                    .count,

                                                "reconhecido",
                                                "reconhecidos"
                                            )
                                        )
                                    },

                                expanded =
                                    devicesExpanded,

                                onClick = {
                                    devicesExpanded =
                                        !devicesExpanded
                                }
                            )

                            if (
                                devicesExpanded
                            ) {
                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            10.dp
                                        )
                                )
                            }
                        }

                        if (
                            devicesExpanded
                        ) {
                            if (
                                orderedDevices.isEmpty()
                            ) {
                                item {
                                    EmptyDevicesCard()

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                12.dp
                                            )
                                    )
                                }
                            }

                            items(
                                items =
                                    orderedDevices,

                                key = {
                                        device ->

                                    device.uuid
                                }
                            ) {
                                    device ->

                                DeviceCard(
                                    device =
                                        device,

                                    isBusy =
                                        state.deviceActionUuid ==
                                                device.uuid,

                                    actionsEnabled =
                                        !deviceActionsLocked,

                                    onDisconnectClick = {
                                        pendingDevice =
                                            device
                                    }
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            12.dp
                                        )
                                )
                            }

                            if (
                                overview.devices
                                    .otherConnectedCount >
                                0
                            ) {
                                item {
                                    OutlinedButton(
                                        onClick = {
                                            showDisconnectOthersDialog =
                                                true
                                        },

                                        enabled =
                                            !deviceActionsLocked,

                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(
                                                    50.dp
                                                ),

                                        shape =
                                            RoundedCornerShape(
                                                8.dp
                                            ),

                                        border =
                                            BorderStroke(
                                                width =
                                                    1.dp,

                                                color =
                                                    Color.White.copy(
                                                        alpha =
                                                            0.18f
                                                    )
                                            ),

                                        colors =
                                            ButtonDefaults
                                                .outlinedButtonColors(
                                                    contentColor =
                                                        Color.White
                                                )
                                    ) {
                                        if (
                                            state.isDisconnectingOthers
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
                                                    "SAIR DOS OUTROS DISPOSITIVOS",

                                                fontWeight =
                                                    FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                6.dp
                                            )
                                    )
                                }
                            }

                            if (
                                overview.devices
                                    .connectedCount >
                                0
                            ) {
                                item {
                                    TextButton(
                                        onClick = {
                                            showDisconnectAllDialog =
                                                true
                                        },

                                        enabled =
                                            !deviceActionsLocked,

                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(
                                                    48.dp
                                                ),

                                        colors =
                                            ButtonDefaults
                                                .textButtonColors(
                                                    contentColor =
                                                        AccountRed
                                                )
                                    ) {
                                        if (
                                            state.isDisconnectingAll
                                        ) {
                                            CircularProgressIndicator(
                                                modifier =
                                                    Modifier.size(
                                                        18.dp
                                                    ),

                                                strokeWidth =
                                                    2.dp,

                                                color =
                                                    AccountRed
                                            )
                                        } else {
                                            Text(
                                                text =
                                                    "SAIR DE TODOS OS DISPOSITIVOS",

                                                fontWeight =
                                                    FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            8.dp
                                        )
                                )
                            }
                        } else {
                            item {
                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            14.dp
                                        )
                                )
                            }
                        }

                        /*
                         * PERFIS
                         */
                        item {
                            ProfilesSummaryCard(
                                profiles =
                                    overview.profiles,

                                onManageProfilesClick =
                                    onManageProfilesClick
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        24.dp
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

    if (
        showPasswordDialog
    ) {
        PasswordChangeDialog(
            isSubmitting =
                state.isChangingPassword,

            errorMessage =
                state.passwordErrorMessage,

            onClearError =
                viewModel::clearPasswordError,

            onDismiss = {
                if (
                    !state.isChangingPassword
                ) {
                    showPasswordDialog =
                        false

                    viewModel
                        .clearPasswordError()
                }
            },

            onSubmit = {
                    currentPassword,
                    newPassword,
                    confirmation ->

                viewModel.changePassword(
                    currentPassword =
                        currentPassword,

                    newPassword =
                        newPassword,

                    newPasswordConfirmation =
                        confirmation,

                    onSuccess = {
                        showPasswordDialog =
                            false
                    },

                    onSessionEnded =
                        onSessionEnded
                )
            }
        )
    }

    pendingDevice?.let {
            device ->

        ConfirmationDialog(
            title =
                if (
                    device.isCurrentDevice
                ) {
                    "Sair deste dispositivo?"
                } else {
                    "Desconectar dispositivo?"
                },

            message =
                if (
                    device.isCurrentDevice
                ) {
                    "Esta sessão será encerrada e você voltará para a tela de login."
                } else {
                    "O acesso deste dispositivo será encerrado. Ele poderá entrar novamente com as credenciais da conta."
                },

            confirmLabel =
                if (
                    device.isCurrentDevice
                ) {
                    "SAIR"
                } else {
                    "DESCONECTAR"
                },

            destructive =
                true,

            isLoading =
                state.deviceActionUuid ==
                        device.uuid,

            onDismiss = {
                if (
                    state.deviceActionUuid ==
                    null
                ) {
                    pendingDevice =
                        null
                }
            },

            onConfirm = {
                pendingDevice =
                    null

                viewModel.disconnectDevice(
                    deviceUuid =
                        device.uuid,

                    onSessionEnded =
                        onSessionEnded
                )
            }
        )
    }

    if (
        showDisconnectOthersDialog
    ) {
        ConfirmationDialog(
            title =
                "Sair dos outros dispositivos?",

            message =
                "Todos os outros dispositivos conectados serão desconectados. Este aparelho continuará conectado.",

            confirmLabel =
                "DESCONECTAR OUTROS",

            destructive =
                false,

            isLoading =
                state.isDisconnectingOthers,

            onDismiss = {
                if (
                    !state.isDisconnectingOthers
                ) {
                    showDisconnectOthersDialog =
                        false
                }
            },

            onConfirm = {
                showDisconnectOthersDialog =
                    false

                viewModel.disconnectOthers(
                    onSessionEnded =
                        onSessionEnded
                )
            }
        )
    }

    if (
        showDisconnectAllDialog
    ) {
        ConfirmationDialog(
            title =
                "Sair de todos os dispositivos?",

            message =
                "Todas as sessões da sua conta serão encerradas, incluindo este aparelho. Você precisará entrar novamente.",

            confirmLabel =
                "SAIR DE TODOS",

            destructive =
                true,

            isLoading =
                state.isDisconnectingAll,

            onDismiss = {
                if (
                    !state.isDisconnectingAll
                ) {
                    showDisconnectAllDialog =
                        false
                }
            },

            onConfirm = {
                showDisconnectAllDialog =
                    false

                viewModel.disconnectAll(
                    onSessionEnded =
                        onSessionEnded
                )
            }
        )
    }
}

@Composable
private fun AccountTopBar(
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
                "Conta",

            color =
                Color.White,

            fontSize =
                23.sp,

            fontWeight =
                FontWeight.ExtraBold
        )
    }
}

@Composable
private fun AccountIdentityCard(
    account: AccountUser
) {
    val initial =
        account.name
            .ifBlank {
                account.username
            }
            .trim()
            .take(
                1
            )
            .uppercase()
            .ifBlank {
                "?"
            }

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            AccountPanel
    ) {
        Row(
            modifier =
                Modifier.padding(
                    18.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            58.dp
                        )
                        .background(
                            color =
                                LaranjadaOrange
                                    .copy(
                                        alpha =
                                            0.15f
                                    ),

                            shape =
                                CircleShape
                        ),

                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        initial,

                    color =
                        LaranjadaOrange,

                    fontSize =
                        24.sp,

                    fontWeight =
                        FontWeight.ExtraBold
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        14.dp
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
                        account.name
                            .ifBlank {
                                account.username
                            },

                    color =
                        LaranjadaText,

                    fontSize =
                        19.sp,

                    fontWeight =
                        FontWeight.Bold,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                if (
                    account.username
                        .isNotBlank()
                ) {
                    Text(
                        text =
                            "@${account.username}",

                        color =
                            LaranjadaMutedText,

                        fontSize =
                            13.sp,

                        modifier =
                            Modifier.padding(
                                top =
                                    3.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableAccountSection(
    icon: ImageVector,
    title: String,
    subtitle: String,
    expanded: Boolean,
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
            AccountPanel
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
                            42.dp
                        )
                        .background(
                            color =
                                LaranjadaOrange
                                    .copy(
                                        alpha =
                                            0.12f
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
                        icon,

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
                        14.dp
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
                        title,

                    color =
                        LaranjadaText,

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        subtitle,

                    color =
                        LaranjadaMutedText,

                    fontSize =
                        12.sp,

                    lineHeight =
                        17.sp,

                    modifier =
                        Modifier.padding(
                            top =
                                3.dp
                        )
                )
            }

            Icon(
                imageVector =
                    if (
                        expanded
                    ) {
                        Icons.Rounded
                            .ExpandLess
                    } else {
                        Icons.Rounded
                            .ExpandMore
                    },

                contentDescription =
                    if (
                        expanded
                    ) {
                        "Recolher"
                    } else {
                        "Expandir"
                    },

                tint =
                    Color.White.copy(
                        alpha =
                            0.55f
                    ),

                modifier =
                    Modifier.size(
                        26.dp
                    )
            )
        }
    }
}

@Composable
private fun PersonalInformationCard(
    account: AccountUser
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            AccountPanelSecondary
    ) {
        Column {
            AccountInfoRow(
                icon =
                    Icons.Rounded.Person,

                label =
                    "Nome completo",

                value =
                    account.name
                        .ifBlank {
                            "Não informado"
                        }
            )

            InfoDivider()

            AccountInfoRow(
                icon =
                    Icons.Rounded
                        .AlternateEmail,

                label =
                    "Nome de usuário",

                value =
                    account.username
                        .takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            "@$it"
                        }
                        ?: "Não informado"
            )

            InfoDivider()

            AccountInfoRow(
                icon =
                    Icons.Rounded.Email,

                label =
                    "E-mail",

                value =
                    account.email
                        .ifBlank {
                            "Não informado"
                        }
            )

            InfoDivider()

            AccountInfoRow(
                icon =
                    Icons.Rounded.Phone,

                label =
                    "Telefone",

                value =
                    account.phone
                        .ifBlank {
                            "Não informado"
                        }
            )

            InfoDivider()

            AccountInfoRow(
                icon =
                    Icons.Rounded.Event,

                label =
                    "Conta criada em",

                value =
                    formatDateTime(
                        account.dateJoined
                    )
            )

            InfoDivider()

            AccountInfoRow(
                icon =
                    Icons.Rounded.Schedule,

                label =
                    "Último acesso",

                value =
                    account.lastLogin
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            formatDateTime(
                                it
                            )
                        }
                        ?: "Primeiro acesso"
            )
        }
    }
}

@Composable
private fun AccountInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        16.dp,

                    vertical =
                        15.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        38.dp
                    )
                    .background(
                        color =
                            LaranjadaOrange
                                .copy(
                                    alpha =
                                        0.10f
                                ),

                        shape =
                            RoundedCornerShape(
                                10.dp
                            )
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    icon,

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
            Text(
                text =
                    label,

                color =
                    LaranjadaMutedText,

                fontSize =
                    12.sp
            )

            Text(
                text =
                    value,

                color =
                    LaranjadaText,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.SemiBold,

                modifier =
                    Modifier.padding(
                        top =
                            2.dp
                    )
            )
        }
    }
}

@Composable
private fun SecurityCard(
    onChangePasswordClick: () -> Unit
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onChangePasswordClick
                ),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            AccountPanel
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
                            42.dp
                        )
                        .background(
                            color =
                                LaranjadaOrange
                                    .copy(
                                        alpha =
                                            0.12f
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
                        Icons.Rounded.Key,

                    contentDescription =
                        null,

                    tint =
                        LaranjadaOrange
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        14.dp
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
                        "Alterar senha",

                    color =
                        LaranjadaText,

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Confirme sua senha atual e escolha uma nova.",

                    color =
                        LaranjadaMutedText,

                    fontSize =
                        12.sp,

                    lineHeight =
                        17.sp,

                    modifier =
                        Modifier.padding(
                            top =
                                3.dp
                        )
                )
            }

            Icon(
                imageVector =
                    Icons.Rounded
                        .ChevronRight,

                contentDescription =
                    null,

                tint =
                    Color.White.copy(
                        alpha =
                            0.55f
                    )
            )
        }
    }
}

@Composable
private fun DeviceCard(
    device: AccountDevice,
    isBusy: Boolean,
    actionsEnabled: Boolean,
    onDisconnectClick: () -> Unit
) {
    val statusColor =
        when {
            !device.isActive ->
                AccountRed

            device.isConnected ->
                AccountGreen

            else ->
                LaranjadaMutedText
        }

    val statusText =
        when {
            !device.isActive ->
                "Não autorizado"

            device.isConnected ->
                "Conectado"

            else ->
                "Desconectado"
        }

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            AccountPanelSecondary,

        border =
            if (
                device.isCurrentDevice
            ) {
                BorderStroke(
                    width =
                        1.dp,

                    color =
                        LaranjadaOrange
                            .copy(
                                alpha =
                                    0.55f
                            )
                )
            } else {
                null
            }
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(
                                42.dp
                            )
                            .background(
                                color =
                                    AccountPanel,

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
                            deviceIcon(
                                device.deviceType
                            ),

                        contentDescription =
                            null,

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(
                                22.dp
                            )
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            12.dp
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
                            device.deviceName
                                .ifBlank {
                                    deviceTypeName(
                                        device.deviceType
                                    )
                                },

                        color =
                            LaranjadaText,

                        fontSize =
                            15.sp,

                        fontWeight =
                            FontWeight.Bold,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        text =
                            deviceTypeDescription(
                                device.deviceType
                            ),

                        color =
                            LaranjadaMutedText,

                        fontSize =
                            11.sp,

                        modifier =
                            Modifier.padding(
                                top =
                                    2.dp
                            )
                    )
                }

                if (
                    device.isCurrentDevice
                ) {
                    StatusPill(
                        text =
                            "Este dispositivo",

                        color =
                            LaranjadaOrange
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(
                                7.dp
                            )
                            .background(
                                color =
                                    statusColor,

                                shape =
                                    CircleShape
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
                        statusText,

                    color =
                        statusColor,

                    fontSize =
                        12.sp,

                    fontWeight =
                        FontWeight.SemiBold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )

            DeviceMetaLine(
                label =
                    "Plataforma",

                value =
                    platformName(
                        device.platform
                    )
            )

            if (
                device.os
                    .isNotBlank()
            ) {
                DeviceMetaLine(
                    label =
                        "Sistema",

                    value =
                        device.os
                )
            }

            if (
                device.browser
                    .isNotBlank()
            ) {
                DeviceMetaLine(
                    label =
                        "Navegador",

                    value =
                        device.browser
                )
            }

            DeviceMetaLine(
                label =
                    "Último acesso",

                value =
                    if (
                        device.isCurrentDevice &&
                        device.isConnected
                    ) {
                        "Agora"
                    } else {
                        formatDateTime(
                            device.lastSeenAt
                        )
                    }
            )

            if (
                device.isConnected &&
                !device.connectedAt
                    .isNullOrBlank()
            ) {
                DeviceMetaLine(
                    label =
                        "Conectado em",

                    value =
                        formatDateTime(
                            device.connectedAt
                        )
                )
            }

            if (
                device.isConnected
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                HorizontalDivider(
                    color =
                        Color.White.copy(
                            alpha =
                                0.08f
                        )
                )

                TextButton(
                    onClick =
                        onDisconnectClick,

                    enabled =
                        actionsEnabled,

                    modifier =
                        Modifier
                            .align(
                                Alignment.End
                            )
                            .padding(
                                top =
                                    4.dp
                            ),

                    colors =
                        ButtonDefaults
                            .textButtonColors(
                                contentColor =
                                    if (
                                        device.isCurrentDevice
                                    ) {
                                        AccountRed
                                    } else {
                                        LaranjadaOrange
                                    }
                            )
                ) {
                    if (
                        isBusy
                    ) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    17.dp
                                ),

                            strokeWidth =
                                2.dp,

                            color =
                                LaranjadaOrange
                        )
                    } else {
                        Text(
                            text =
                                if (
                                    device.isCurrentDevice
                                ) {
                                    "SAIR DESTE DISPOSITIVO"
                                } else {
                                    "DESCONECTAR"
                                },

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceMetaLine(
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical =
                        3.dp
                )
    ) {
        Text(
            text =
                label,

            color =
                LaranjadaMutedText,

            fontSize =
                11.sp,

            modifier =
                Modifier.width(
                    105.dp
                )
        )

        Text(
            text =
                value,

            color =
                Color.White.copy(
                    alpha =
                        0.88f
                ),

            fontSize =
                11.sp,

            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}

@Composable
private fun EmptyDevicesCard() {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            AccountPanelSecondary
    ) {
        Column(
            modifier =
                Modifier.padding(
                    22.dp
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Devices,

                contentDescription =
                    null,

                tint =
                    LaranjadaMutedText,

                modifier =
                    Modifier.size(
                        32.dp
                    )
            )

            Text(
                text =
                    "Nenhum dispositivo registrado",

                color =
                    LaranjadaText,

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold,

                modifier =
                    Modifier.padding(
                        top =
                            10.dp
                    )
            )

            Text(
                text =
                    "Os dispositivos reconhecidos aparecerão aqui conforme forem registrados.",

                color =
                    LaranjadaMutedText,

                fontSize =
                    12.sp,

                lineHeight =
                    17.sp,

                textAlign =
                    TextAlign.Center,

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
private fun ProfilesSummaryCard(
    profiles: AccountProfilesSummary,
    onManageProfilesClick: () -> Unit
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onManageProfilesClick
                ),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            AccountPanel
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
                            42.dp
                        )
                        .background(
                            color =
                                LaranjadaOrange
                                    .copy(
                                        alpha =
                                            0.12f
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
                        Icons.Rounded.People,

                    contentDescription =
                        null,

                    tint =
                        LaranjadaOrange
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        14.dp
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
                        "Perfis",

                    color =
                        LaranjadaText,

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        if (
                            profiles.maxProfiles >
                            0
                        ) {
                            "${profiles.count} de ${profiles.maxProfiles} utilizados • ${profiles.remainingProfiles} disponíveis"
                        } else {
                            quantityText(
                                profiles.count,
                                "perfil utilizado",
                                "perfis utilizados"
                            )
                        },

                    color =
                        LaranjadaMutedText,

                    fontSize =
                        12.sp,

                    lineHeight =
                        17.sp,

                    modifier =
                        Modifier.padding(
                            top =
                                3.dp
                        )
                )
            }

            Icon(
                imageVector =
                    Icons.Rounded
                        .ChevronRight,

                contentDescription =
                    "Gerenciar perfis",

                tint =
                    Color.White.copy(
                        alpha =
                            0.55f
                    )
            )
        }
    }
}

@Composable
private fun PasswordChangeDialog(
    isSubmitting: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (
        currentPassword: String,
        newPassword: String,
        confirmation: String
    ) -> Unit
) {
    var currentPassword by
    remember {
        mutableStateOf(
            ""
        )
    }

    var newPassword by
    remember {
        mutableStateOf(
            ""
        )
    }

    var confirmation by
    remember {
        mutableStateOf(
            ""
        )
    }

    val canSubmit =
        currentPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                confirmation.isNotBlank() &&
                !isSubmitting

    val fieldColors =
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
                        0.20f
                ),

            cursorColor =
                LaranjadaOrange,

            focusedLabelColor =
                LaranjadaOrange,

            unfocusedLabelColor =
                LaranjadaMutedText
        )

    AlertDialog(
        onDismissRequest =
            onDismiss,

        containerColor =
            LaranjadaSurfaceLight,

        titleContentColor =
            LaranjadaText,

        textContentColor =
            LaranjadaMutedText,

        title = {
            Text(
                text =
                    "Alterar senha",

                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {
            Column {
                Text(
                    text =
                        "Confirme sua senha atual e escolha uma nova senha.",

                    color =
                        LaranjadaMutedText,

                    fontSize =
                        13.sp,

                    lineHeight =
                        18.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                OutlinedTextField(
                    value =
                        currentPassword,

                    onValueChange = {
                        currentPassword =
                            it

                        onClearError()
                    },

                    label = {
                        Text(
                            "Senha atual"
                        )
                    },

                    singleLine =
                        true,

                    enabled =
                        !isSubmitting,

                    visualTransformation =
                        PasswordVisualTransformation(),

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Password,

                            imeAction =
                                ImeAction.Next
                        ),

                    colors =
                        fieldColors,

                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                OutlinedTextField(
                    value =
                        newPassword,

                    onValueChange = {
                        newPassword =
                            it

                        onClearError()
                    },

                    label = {
                        Text(
                            "Nova senha"
                        )
                    },

                    singleLine =
                        true,

                    enabled =
                        !isSubmitting,

                    visualTransformation =
                        PasswordVisualTransformation(),

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Password,

                            imeAction =
                                ImeAction.Next
                        ),

                    colors =
                        fieldColors,

                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                OutlinedTextField(
                    value =
                        confirmation,

                    onValueChange = {
                        confirmation =
                            it

                        onClearError()
                    },

                    label = {
                        Text(
                            "Confirmar nova senha"
                        )
                    },

                    singleLine =
                        true,

                    enabled =
                        !isSubmitting,

                    visualTransformation =
                        PasswordVisualTransformation(),

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Password,

                            imeAction =
                                ImeAction.Done
                        ),

                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                if (
                                    canSubmit
                                ) {
                                    onSubmit(
                                        currentPassword,
                                        newPassword,
                                        confirmation
                                    )
                                }
                            }
                        ),

                    colors =
                        fieldColors,

                    modifier =
                        Modifier.fillMaxWidth()
                )

                if (
                    !errorMessage
                        .isNullOrBlank()
                ) {
                    Text(
                        text =
                            errorMessage,

                        color =
                            AccountRed,

                        fontSize =
                            12.sp,

                        lineHeight =
                            17.sp,

                        modifier =
                            Modifier.padding(
                                top =
                                    12.dp
                            )
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(
                        currentPassword,
                        newPassword,
                        confirmation
                    )
                },

                enabled =
                    canSubmit,

                colors =
                    ButtonDefaults
                        .textButtonColors(
                            contentColor =
                                LaranjadaOrange
                        )
            ) {
                if (
                    isSubmitting
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
                            "ALTERAR",

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        },

        dismissButton = {
            TextButton(
                onClick =
                    onDismiss,

                enabled =
                    !isSubmitting
            ) {
                Text(
                    text =
                        "CANCELAR",

                    color =
                        LaranjadaMutedText
                )
            }
        }
    )
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    destructive: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val actionColor =
        if (
            destructive
        ) {
            AccountRed
        } else {
            LaranjadaOrange
        }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        containerColor =
            LaranjadaSurfaceLight,

        titleContentColor =
            LaranjadaText,

        textContentColor =
            LaranjadaMutedText,

        title = {
            Text(
                text =
                    title,

                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {
            Text(
                text =
                    message,

                lineHeight =
                    19.sp
            )
        },

        confirmButton = {
            TextButton(
                onClick =
                    onConfirm,

                enabled =
                    !isLoading,

                colors =
                    ButtonDefaults
                        .textButtonColors(
                            contentColor =
                                actionColor
                        )
            ) {
                if (
                    isLoading
                ) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                18.dp
                            ),

                        strokeWidth =
                            2.dp,

                        color =
                            actionColor
                    )
                } else {
                    Text(
                        text =
                            confirmLabel,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        },

        dismissButton = {
            TextButton(
                onClick =
                    onDismiss,

                enabled =
                    !isLoading
            ) {
                Text(
                    text =
                        "CANCELAR",

                    color =
                        LaranjadaMutedText
                )
            }
        }
    )
}

@Composable
private fun StatusPill(
    text: String,
    color: Color
) {
    Surface(
        shape =
            RoundedCornerShape(
                50
            ),

        color =
            color.copy(
                alpha =
                    0.12f
            ),

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    color.copy(
                        alpha =
                            0.32f
                    )
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal =
                        9.dp,

                    vertical =
                        5.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    Icons.Rounded
                        .CheckCircle,

                contentDescription =
                    null,

                tint =
                    color,

                modifier =
                    Modifier.size(
                        13.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        5.dp
                    )
            )

            Text(
                text =
                    text,

                color =
                    color,

                fontSize =
                    10.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoDivider() {
    HorizontalDivider(
        color =
            Color.White.copy(
                alpha =
                    0.07f
            ),

        modifier =
            Modifier.padding(
                start =
                    67.dp
            )
    )
}

@Composable
private fun AccountLoadingState(
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
private fun AccountErrorState(
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
                Icons.Rounded.Devices,

            contentDescription =
                null,

            tint =
                LaranjadaOrange,

            modifier =
                Modifier.size(
                    40.dp
                )
        )

        Text(
            text =
                "Não foi possível carregar sua conta",

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
                    top =
                        14.dp
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
                    top =
                        7.dp
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
                    top =
                        20.dp
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

private fun deviceIcon(
    deviceType: String
): ImageVector {

    return when (
        deviceType.lowercase()
    ) {
        "web" ->
            Icons.Rounded.Language

        "android",
        "ios" ->
            Icons.Rounded.Smartphone

        "tv" ->
            Icons.Rounded.Tv

        else ->
            Icons.Rounded.Devices
    }
}

private fun deviceTypeName(
    deviceType: String
): String {

    return when (
        deviceType.lowercase()
    ) {
        "web" ->
            "Web"

        "android" ->
            "Android"

        "ios" ->
            "iOS"

        "tv" ->
            "TV"

        else ->
            "Dispositivo"
    }
}

private fun deviceTypeDescription(
    deviceType: String
): String {

    return when (
        deviceType.lowercase()
    ) {
        "web" ->
            "Acesso pela Web"

        "android" ->
            "Aplicativo Android"

        "ios" ->
            "Aplicativo iOS"

        "tv" ->
            "Aplicativo para TV"

        else ->
            "Dispositivo reconhecido"
    }
}

private fun platformName(
    platform: String
): String {

    return when (
        platform
            .trim()
            .lowercase()
    ) {
        "desktop" ->
            "Computador"

        "mobile" ->
            "Celular"

        "tablet" ->
            "Tablet"

        "tv" ->
            "TV"

        "" ->
            "Não informada"

        else ->
            platform
    }
}

private fun quantityText(
    count: Int,
    singular: String,
    plural: String
): String {

    return if (
        count ==
        1
    ) {
        "$count $singular"
    } else {
        "$count $plural"
    }
}

private fun formatDateTime(
    value: String?
): String {

    val raw =
        value
            ?.trim()
            .orEmpty()

    if (
        raw.isBlank()
    ) {
        return "Não informado"
    }

    val normalized =
        raw.replace(
            Regex(
                "(\\.\\d{3})\\d+"
            ),
            "$1"
        )

    val patterns =
        listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )

    for (
    pattern in
    patterns
    ) {
        val parser =
            SimpleDateFormat(
                pattern,
                Locale.US
            )
                .apply {
                    isLenient =
                        false

                    if (
                        pattern.endsWith(
                            "'Z'"
                        )
                    ) {
                        timeZone =
                            TimeZone.getTimeZone(
                                "UTC"
                            )
                    }
                }

        val date =
            runCatching {
                parser.parse(
                    normalized
                )
            }
                .getOrNull()

        if (
            date != null
        ) {
            return SimpleDateFormat(
                "dd/MM/yyyy 'às' HH:mm",
                Locale(
                    "pt",
                    "BR"
                )
            )
                .format(
                    date
                )
        }
    }

    return raw
}