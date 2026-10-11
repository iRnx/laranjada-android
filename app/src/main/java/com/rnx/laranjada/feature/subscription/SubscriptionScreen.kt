package com.rnx.laranjada.feature.subscription

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.rnx.laranjada.core.network.ApiConfig
import com.rnx.laranjada.domain.model.SubscriptionAccessSummary
import com.rnx.laranjada.domain.model.SubscriptionDiscountSummary
import com.rnx.laranjada.domain.model.SubscriptionHistoryItem
import com.rnx.laranjada.domain.model.SubscriptionOverview
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val SubscriptionPanel = Color(0xFF101417)
private val SubscriptionPanelSecondary = Color(0xFF151A1F)
private val SubscriptionGreen = Color(0xFF3FB950)
private val SubscriptionRed = Color(0xFFFF6B6B)
private val SubscriptionWarning = Color(0xFFFFB547)

@Composable
fun SubscriptionScreen(
    onBackClick: () -> Unit,
    onSessionEnded: () -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var accessExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var featuresExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var detailsExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var historyExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        viewModel.start(
            onSessionEnded = onSessionEnded
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver {
                    _,
                    event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {
                    viewModel.refreshAfterReturn(
                        onSessionEnded =
                            onSessionEnded
                    )
                }
            }

        lifecycleOwner.lifecycle.addObserver(
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
                    state.overview == null -> {

                SubscriptionLoadingState(
                    modifier =
                        Modifier.padding(
                            scaffoldPadding
                        )
                )
            }

            state.overview == null -> {
                SubscriptionErrorState(
                    message =
                        state.loadErrorMessage
                            ?: "Não foi possível carregar sua assinatura.",

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
                            SubscriptionTopBar(
                                onBackClick =
                                    onBackClick
                            )
                        }

                        item {
                            SubscriptionHeroCard(
                                overview =
                                    overview
                            )

                            Spacer(
                                Modifier.height(
                                    16.dp
                                )
                            )
                        }

                        overview.access?.let {
                                access ->

                            item {
                                ExpandableSubscriptionSection(
                                    icon =
                                        Icons.Rounded.CalendarMonth,

                                    title =
                                        "Período de acesso",

                                    subtitle =
                                        accessPeriodSubtitle(
                                            overview
                                        ),

                                    expanded =
                                        accessExpanded,

                                    onClick = {
                                        accessExpanded =
                                            !accessExpanded
                                    }
                                )

                                if (
                                    accessExpanded
                                ) {
                                    Spacer(
                                        Modifier.height(
                                            10.dp
                                        )
                                    )

                                    AccessDetailsCard(
                                        access =
                                            access
                                    )
                                }

                                Spacer(
                                    Modifier.height(
                                        14.dp
                                    )
                                )
                            }
                        }

                        val features =
                            overview.plan
                                ?.features
                                .orEmpty()

                        if (
                            features.isNotEmpty()
                        ) {
                            item {
                                ExpandableSubscriptionSection(
                                    icon =
                                        Icons.Rounded.Star,

                                    title =
                                        "O que está incluído",

                                    subtitle =
                                        overview.plan
                                            ?.accessSummary
                                            ?.takeIf {
                                                it.isNotBlank()
                                            }
                                            ?: "Benefícios do seu plano.",

                                    expanded =
                                        featuresExpanded,

                                    onClick = {
                                        featuresExpanded =
                                            !featuresExpanded
                                    }
                                )

                                if (
                                    featuresExpanded
                                ) {
                                    Spacer(
                                        Modifier.height(
                                            10.dp
                                        )
                                    )

                                    FeaturesCard(
                                        features =
                                            features
                                    )
                                }

                                Spacer(
                                    Modifier.height(
                                        14.dp
                                    )
                                )
                            }
                        }

                        if (
                            overview.plan != null ||
                            overview.access != null ||
                            overview.payment != null ||
                            overview.discount != null
                        ) {
                            item {
                                ExpandableSubscriptionSection(
                                    icon =
                                        Icons.Rounded.Info,

                                    title =
                                        "Detalhes do acesso",

                                    subtitle =
                                        "Plano, pagamento e informações do acesso.",

                                    expanded =
                                        detailsExpanded,

                                    onClick = {
                                        detailsExpanded =
                                            !detailsExpanded
                                    }
                                )

                                if (
                                    detailsExpanded
                                ) {
                                    Spacer(
                                        Modifier.height(
                                            10.dp
                                        )
                                    )

                                    SubscriptionDetailsCard(
                                        overview =
                                            overview
                                    )
                                }

                                Spacer(
                                    Modifier.height(
                                        14.dp
                                    )
                                )
                            }
                        }

                        item {
                            ExpandableSubscriptionSection(
                                icon =
                                    Icons.Rounded.History,

                                title =
                                    "Histórico de pagamentos",

                                subtitle =
                                    historySubtitle(
                                        overview.history.size
                                    ),

                                expanded =
                                    historyExpanded,

                                onClick = {
                                    historyExpanded =
                                        !historyExpanded
                                }
                            )

                            if (
                                historyExpanded
                            ) {
                                Spacer(
                                    Modifier.height(
                                        10.dp
                                    )
                                )
                            }
                        }

                        if (
                            historyExpanded
                        ) {
                            if (
                                overview.history.isEmpty()
                            ) {
                                item {
                                    EmptyHistoryCard()

                                    Spacer(
                                        Modifier.height(
                                            14.dp
                                        )
                                    )
                                }
                            } else {
                                items(
                                    items =
                                        overview.history
                                ) {
                                        historyItem ->

                                    HistoryCard(
                                        item =
                                            historyItem
                                    )

                                    Spacer(
                                        Modifier.height(
                                            10.dp
                                        )
                                    )
                                }

                                item {
                                    Spacer(
                                        Modifier.height(
                                            4.dp
                                        )
                                    )
                                }
                            }
                        } else {
                            item {
                                Spacer(
                                    Modifier.height(
                                        14.dp
                                    )
                                )
                            }
                        }

                        if (
                            overview.supportReference
                                .isNotBlank()
                        ) {
                            item {
                                SupportReferenceCard(
                                    reference =
                                        overview.supportReference
                                )

                                Spacer(
                                    Modifier.height(
                                        18.dp
                                    )
                                )
                            }
                        }

                        if (
                            shouldShowPlansButton(
                                overview
                            )
                        ) {
                            item {
                                Button(
                                    onClick = {
                                        if (
                                            !openPlansPage(
                                                context
                                            )
                                        ) {
                                            Toast.makeText(
                                                context,
                                                "Não foi possível abrir a página de planos.",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        }
                                    },

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
                                    Text(
                                        text =
                                            "VER PLANOS",

                                        fontWeight =
                                            FontWeight.ExtraBold
                                    )

                                    Spacer(
                                        Modifier.width(
                                            8.dp
                                        )
                                    )

                                    Icon(
                                        imageVector =
                                            Icons.Rounded.OpenInNew,

                                        contentDescription =
                                            null,

                                        modifier =
                                            Modifier.size(
                                                18.dp
                                            )
                                    )
                                }

                                Text(
                                    text =
                                        "A contratação continua pela página Web do Laranjada.",

                                    color =
                                        LaranjadaMutedText,

                                    fontSize =
                                        11.sp,

                                    lineHeight =
                                        16.sp,

                                    textAlign =
                                        TextAlign.Center,

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                top = 8.dp
                                            )
                                )

                                Spacer(
                                    Modifier.height(
                                        16.dp
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
private fun SubscriptionTopBar(
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
                "Minha assinatura",

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
private fun SubscriptionHeroCard(
    overview: SubscriptionOverview
) {
    val statusColor =
        stateColor(
            overview.state.code
        )

    val plan =
        overview.plan

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                16.dp
            ),

        color =
            SubscriptionPanel,

        border =
            BorderStroke(
                1.dp,
                statusColor.copy(
                    alpha = 0.35f
                )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    18.dp
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
                                48.dp
                            )
                            .background(
                                LaranjadaOrange.copy(
                                    alpha = 0.12f
                                ),
                                RoundedCornerShape(
                                    13.dp
                                )
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Star,

                        contentDescription =
                            null,

                        tint =
                            LaranjadaOrange,

                        modifier =
                            Modifier.size(
                                24.dp
                            )
                    )
                }

                Spacer(
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
                            plan
                                ?.name
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Laranjada",

                        color =
                            LaranjadaText,

                        fontSize =
                            20.sp,

                        fontWeight =
                            FontWeight.ExtraBold,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        text =
                            when {
                                overview.hasAccess ->
                                    "Seu acesso atual"

                                plan != null ->
                                    "Último acesso"

                                else ->
                                    "Sua assinatura"
                            },

                        color =
                            LaranjadaMutedText,

                        fontSize =
                            12.sp,

                        modifier =
                            Modifier.padding(
                                top = 2.dp
                            )
                    )
                }

                SubscriptionStatusPill(
                    text =
                        overview.state.label,

                    color =
                        statusColor
                )
            }

            if (
                plan != null &&
                plan.priceLabel
                    .isNotBlank()
            ) {
                Spacer(
                    Modifier.height(
                        20.dp
                    )
                )

                Row(
                    verticalAlignment =
                        Alignment.Bottom
                ) {
                    Text(
                        text =
                            plan.priceLabel,

                        color =
                            Color.White,

                        fontSize =
                            27.sp,

                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    if (
                        plan.periodLabel
                            .isNotBlank()
                    ) {
                        Text(
                            text =
                                " ${plan.periodLabel}",

                            color =
                                LaranjadaMutedText,

                            fontSize =
                                13.sp,

                            modifier =
                                Modifier.padding(
                                    bottom = 4.dp
                                )
                        )
                    }
                }
            }

            val paymentMethod =
                overview.payment
                    ?.methodLabel
                    ?.takeIf {
                        it.isNotBlank()
                    }

            val accessType =
                overview.access
                    ?.typeLabel
                    ?.takeIf {
                        it.isNotBlank()
                    }

            if (
                paymentMethod != null ||
                accessType != null
            ) {
                Spacer(
                    Modifier.height(
                        12.dp
                    )
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Payments,

                        contentDescription =
                            null,

                        tint =
                            LaranjadaOrange,

                        modifier =
                            Modifier.size(
                                18.dp
                            )
                    )

                    Spacer(
                        Modifier.width(
                            7.dp
                        )
                    )

                    Text(
                        text =
                            paymentMethod
                                ?: accessType
                                    .orEmpty(),

                        color =
                            Color.White.copy(
                                alpha = 0.85f
                            ),

                        fontSize =
                            13.sp,

                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }

            if (
                !overview.hasAccess
            ) {
                Spacer(
                    Modifier.height(
                        14.dp
                    )
                )

                HorizontalDivider(
                    color =
                        Color.White.copy(
                            alpha = 0.08f
                        )
                )

                Text(
                    text =
                        stateSupportingText(
                            overview.state.code
                        ),

                    color =
                        LaranjadaMutedText,

                    fontSize =
                        12.sp,

                    lineHeight =
                        18.sp,

                    modifier =
                        Modifier.padding(
                            top = 12.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun ExpandableSubscriptionSection(
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
            SubscriptionPanel
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
                            LaranjadaOrange.copy(
                                alpha = 0.12f
                            ),
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
                            top = 3.dp
                        )
                )
            }

            Icon(
                imageVector =
                    if (
                        expanded
                    ) {
                        Icons.Rounded.ExpandLess
                    } else {
                        Icons.Rounded.ExpandMore
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
                        alpha = 0.55f
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
private fun AccessDetailsCard(
    access: SubscriptionAccessSummary
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            SubscriptionPanelSecondary
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {
            SubscriptionDetailRow(
                label =
                    "Início do período",

                value =
                    formatSubscriptionDate(
                        access.periodStartAt
                    )
            )

            DetailDivider()

            SubscriptionDetailRow(
                label =
                    "Fim do período",

                value =
                    formatSubscriptionDate(
                        access.periodEndAt
                    )
            )

            if (
                access.typeLabel
                    .isNotBlank()
            ) {
                DetailDivider()

                SubscriptionDetailRow(
                    label =
                        "Tipo de acesso",

                    value =
                        access.typeLabel
                )
            }
        }
    }
}

@Composable
private fun FeaturesCard(
    features: List<String>
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            SubscriptionPanelSecondary
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {
            features.forEach {
                    feature ->

                Row(
                    verticalAlignment =
                        Alignment.Top
                ) {
                    Box(
                        modifier =
                            Modifier
                                .padding(
                                    top = 2.dp
                                )
                                .size(
                                    18.dp
                                )
                                .background(
                                    SubscriptionGreen.copy(
                                        alpha = 0.12f
                                    ),
                                    CircleShape
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.CheckCircle,

                            contentDescription =
                                null,

                            tint =
                                SubscriptionGreen,

                            modifier =
                                Modifier.size(
                                    13.dp
                                )
                        )
                    }

                    Spacer(
                        Modifier.width(
                            10.dp
                        )
                    )

                    Text(
                        text =
                            feature,

                        color =
                            Color.White.copy(
                                alpha = 0.90f
                            ),

                        fontSize =
                            13.sp,

                        lineHeight =
                            19.sp,

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionDetailsCard(
    overview: SubscriptionOverview
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            SubscriptionPanelSecondary
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {
            val rows =
                buildList {
                    overview.plan
                        ?.accessSummary
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            add(
                                "Acesso do plano" to
                                        it
                            )
                        }

                    overview.access
                        ?.typeLabel
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            add(
                                "Tipo" to
                                        it
                            )
                        }

                    overview.payment
                        ?.methodLabel
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            add(
                                "Pagamento" to
                                        it
                            )
                        }

                    overview.payment
                        ?.amountLabel
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            add(
                                "Valor pago" to
                                        it
                            )
                        }

                    overview.payment
                        ?.paidAt
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            add(
                                "Pago em" to
                                        formatSubscriptionDate(
                                            it
                                        )
                            )
                        }
                }

            rows.forEachIndexed {
                    index,
                    row ->

                if (
                    index > 0
                ) {
                    DetailDivider()
                }

                SubscriptionDetailRow(
                    label =
                        row.first,

                    value =
                        row.second
                )
            }

            overview.discount
                ?.let {
                        discount ->

                    if (
                        rows.isNotEmpty()
                    ) {
                        DetailDivider()
                    }

                    DiscountDetails(
                        discount =
                            discount
                    )
                }
        }
    }
}

@Composable
private fun DiscountDetails(
    discount: SubscriptionDiscountSummary
) {
    Column {
        Text(
            text =
                "Desconto aplicado",

            color =
                LaranjadaOrange,

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.Bold,

            modifier =
                Modifier.padding(
                    vertical = 6.dp
                )
        )

        val rows =
            buildList {
                if (
                    discount.name
                        .isNotBlank()
                ) {
                    add(
                        "Benefício" to
                                discount.name
                    )
                }

                if (
                    discount.code
                        .isNotBlank()
                ) {
                    add(
                        "Código" to
                                discount.code
                    )
                }

                if (
                    discount.originalAmountLabel
                        .isNotBlank()
                ) {
                    add(
                        "Valor original" to
                                discount.originalAmountLabel
                    )
                }

                if (
                    discount.discountAmountLabel
                        .isNotBlank()
                ) {
                    add(
                        "Desconto" to
                                discount.discountAmountLabel
                    )
                }

                if (
                    discount.finalAmountLabel
                        .isNotBlank()
                ) {
                    add(
                        "Valor final" to
                                discount.finalAmountLabel
                    )
                }
            }

        rows.forEach {
                row ->

            SubscriptionDetailRow(
                label =
                    row.first,

                value =
                    row.second
            )
        }
    }
}

@Composable
private fun SubscriptionDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 7.dp
                ),

        verticalAlignment =
            Alignment.Top
    ) {
        Text(
            text =
                label,

            color =
                LaranjadaMutedText,

            fontSize =
                12.sp,

            modifier =
                Modifier.width(
                    118.dp
                )
        )

        Text(
            text =
                value.ifBlank {
                    "Não informado"
                },

            color =
                Color.White.copy(
                    alpha = 0.90f
                ),

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.SemiBold,

            lineHeight =
                17.sp,

            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}

@Composable
private fun HistoryCard(
    item: SubscriptionHistoryItem
) {
    val toneColor =
        historyToneColor(
            item.statusTone
        )

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            SubscriptionPanelSecondary
    ) {
        Row(
            modifier =
                Modifier.padding(
                    15.dp
                ),

            verticalAlignment =
                Alignment.Top
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            40.dp
                        )
                        .background(
                            toneColor.copy(
                                alpha = 0.12f
                            ),
                            RoundedCornerShape(
                                10.dp
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.ReceiptLong,

                    contentDescription =
                        null,

                    tint =
                        toneColor,

                    modifier =
                        Modifier.size(
                            21.dp
                        )
                )
            }

            Spacer(
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
                        item.planName
                            .ifBlank {
                                "Pagamento"
                            },

                    color =
                        LaranjadaText,

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.Bold,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                Spacer(
                    Modifier.height(
                        6.dp
                    )
                )

                HistoryStatusPill(
                    text =
                        item.statusLabel
                            .ifBlank {
                                item.status
                            },

                    color =
                        toneColor
                )

                val paymentLine =
                    listOfNotNull(
                        item.paymentMethod
                            .takeIf {
                                it.isNotBlank()
                            },

                        item.amountLabel
                            .takeIf {
                                it.isNotBlank()
                            }
                    )
                        .joinToString(
                            " • "
                        )

                if (
                    paymentLine.isNotBlank()
                ) {
                    Text(
                        text =
                            paymentLine,

                        color =
                            Color.White.copy(
                                alpha = 0.80f
                            ),

                        fontSize =
                            12.sp,

                        fontWeight =
                            FontWeight.SemiBold,

                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )
                    )
                }

                if (
                    !item.occurredAt
                        .isNullOrBlank()
                ) {
                    Text(
                        text =
                            formatSubscriptionDate(
                                item.occurredAt
                            ),

                        color =
                            LaranjadaMutedText,

                        fontSize =
                            11.sp,

                        modifier =
                            Modifier.padding(
                                top = 4.dp
                            )
                    )
                }

                if (
                    item.detail
                        .isNotBlank()
                ) {
                    Text(
                        text =
                            item.detail,

                        color =
                            LaranjadaMutedText,

                        fontSize =
                            11.sp,

                        lineHeight =
                            16.sp,

                        modifier =
                            Modifier.padding(
                                top = 7.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            SubscriptionPanelSecondary
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
                    Icons.Rounded.ReceiptLong,

                contentDescription =
                    null,

                tint =
                    LaranjadaMutedText,

                modifier =
                    Modifier.size(
                        30.dp
                    )
            )

            Text(
                text =
                    "Nenhum pagamento registrado",

                color =
                    LaranjadaText,

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.Bold,

                modifier =
                    Modifier.padding(
                        top = 10.dp
                    )
            )

            Text(
                text =
                    "Quando houver pagamentos associados à sua conta, eles aparecerão aqui.",

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
                        top = 5.dp
                    )
            )
        }
    }
}

@Composable
private fun SupportReferenceCard(
    reference: String
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            SubscriptionPanel
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
                            LaranjadaOrange.copy(
                                alpha = 0.12f
                            ),
                            RoundedCornerShape(
                                11.dp
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Info,

                    contentDescription =
                        null,

                    tint =
                        LaranjadaOrange
                )
            }

            Spacer(
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
                        "Código de atendimento",

                    color =
                        LaranjadaMutedText,

                    fontSize =
                        12.sp
                )

                Text(
                    text =
                        reference,

                    color =
                        LaranjadaText,

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    letterSpacing =
                        0.8.sp,

                    modifier =
                        Modifier.padding(
                            top = 3.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun SubscriptionStatusPill(
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
                alpha = 0.12f
            ),

        border =
            BorderStroke(
                1.dp,
                color.copy(
                    alpha = 0.30f
                )
            )
    ) {
        Text(
            text =
                text,

            color =
                color,

            fontSize =
                10.sp,

            fontWeight =
                FontWeight.Bold,

            maxLines =
                1,

            modifier =
                Modifier.padding(
                    horizontal = 9.dp,
                    vertical = 5.dp
                )
        )
    }
}

@Composable
private fun HistoryStatusPill(
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
                alpha = 0.12f
            )
    ) {
        Text(
            text =
                text,

            color =
                color,

            fontSize =
                9.sp,

            fontWeight =
                FontWeight.Bold,

            maxLines =
                1,

            modifier =
                Modifier.padding(
                    horizontal = 7.dp,
                    vertical = 4.dp
                )
        )
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        color =
            Color.White.copy(
                alpha = 0.07f
            )
    )
}

@Composable
private fun SubscriptionLoadingState(
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
private fun SubscriptionErrorState(
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
                Icons.Rounded.ReceiptLong,

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
                "Não foi possível carregar sua assinatura",

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

private fun accessPeriodSubtitle(
    overview: SubscriptionOverview
): String {

    val endAt =
        overview.access
            ?.periodEndAt
            ?.takeIf {
                it.isNotBlank()
            }
            ?: return "Consulte as datas do período."

    val formatted =
        formatSubscriptionDate(
            endAt
        )

    return if (
        overview.hasAccess
    ) {
        "Disponível até $formatted"
    } else {
        "Último período terminou em $formatted"
    }
}

private fun historySubtitle(
    count: Int
): String {

    return when (
        count
    ) {
        0 ->
            "Nenhum pagamento registrado."

        1 ->
            "1 pagamento registrado."

        else ->
            "$count pagamentos registrados."
    }
}

private fun shouldShowPlansButton(
    overview: SubscriptionOverview
): Boolean {

    if (
        overview.hasAccess
    ) {
        return false
    }

    return overview.state.code
        .trim()
        .lowercase() !=
            "pending"
}

private fun stateSupportingText(
    code: String
): String {

    return when (
        code
            .trim()
            .lowercase()
    ) {
        "pending" ->
            "Seu pagamento ainda está aguardando confirmação."

        "error" ->
            "O último pagamento não pôde ser confirmado."

        "expired" ->
            "Seu último período de acesso já terminou."

        "inactive" ->
            "Existe um registro anterior, mas nenhum período está ativo agora."

        else ->
            "Nenhum período de acesso está ativo agora."
    }
}

private fun stateColor(
    code: String
): Color {

    return when (
        code
            .trim()
            .lowercase()
    ) {
        "paid" ->
            SubscriptionGreen

        "pending" ->
            SubscriptionWarning

        "error",
        "expired" ->
            SubscriptionRed

        else ->
            LaranjadaMutedText
    }
}

private fun historyToneColor(
    tone: String
): Color {

    return when (
        tone
            .trim()
            .lowercase()
    ) {
        "success" ->
            SubscriptionGreen

        "warning" ->
            SubscriptionWarning

        "danger" ->
            SubscriptionRed

        else ->
            LaranjadaMutedText
    }
}

private fun openPlansPage(
    context: Context
): Boolean {

    val url =
        ApiConfig.buildUrl(
            "/payments/plans/"
        )

    val intent =
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                url
            )
        )

    return runCatching {
        context.startActivity(
            intent
        )
    }
        .isSuccess
}

private fun formatSubscriptionDate(
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

    /*
     * SimpleDateFormat trabalha com milissegundos.
     * A API pode devolver microssegundos.
     *
     * Reduzimos apenas a fração do segundo,
     * preservando completamente o offset ISO.
     */
    val normalized =
        raw.replace(
            Regex(
                """\.(\d{3})\d+(?=Z$|[+-]\d{2}:\d{2}$)"""
            )
        ) {
                match ->

            ".${match.groupValues[1]}"
        }

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