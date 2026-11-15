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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

private val SubscriptionPanel =
    Color(0xFF101417)

private val SubscriptionPanelSecondary =
    Color(0xFF151A1F)

private val SubscriptionGreen =
    Color(0xFF3FB950)

private val SubscriptionRed =
    Color(0xFFFF6B6B)

private val SubscriptionWarning =
    Color(0xFFFFB547)

@Composable
fun SubscriptionScreen(
    onBackClick: () -> Unit,
    onSessionEnded: () -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val state =
        viewModel.uiState

    val context =
        LocalContext.current

    val lifecycleOwner =
        LocalLifecycleOwner.current

    var accessExpanded by
    rememberSaveable {
        mutableStateOf(false)
    }

    var featuresExpanded by
    rememberSaveable {
        mutableStateOf(false)
    }

    var detailsExpanded by
    rememberSaveable {
        mutableStateOf(false)
    }

    var historyExpanded by
    rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
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

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    LaranjadaBlack
                )
    ) {
        val isTablet =
            maxWidth >=
                    600.dp

        val isExpanded =
            maxWidth >=
                    840.dp

        when {
            state.isLoading &&
                    state.overview ==
                    null -> {

                SubscriptionLoadingState()
            }

            state.overview ==
                    null -> {

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

                    isTablet =
                        isTablet
                )
            }

            else -> {
                SubscriptionContent(
                    overview =
                        state.overview,

                    isTablet =
                        isTablet,

                    isExpanded =
                        isExpanded,

                    accessExpanded =
                        accessExpanded,

                    featuresExpanded =
                        featuresExpanded,

                    detailsExpanded =
                        detailsExpanded,

                    historyExpanded =
                        historyExpanded,

                    onBackClick =
                        onBackClick,

                    onToggleAccess = {
                        accessExpanded =
                            !accessExpanded
                    },

                    onToggleFeatures = {
                        featuresExpanded =
                            !featuresExpanded
                    },

                    onToggleDetails = {
                        detailsExpanded =
                            !detailsExpanded
                    },

                    onToggleHistory = {
                        historyExpanded =
                            !historyExpanded
                    },

                    onOpenPlans = {
                        if (
                            !openPlansPage(
                                context
                            )
                        ) {
                            Toast.makeText(
                                context,
                                "Não foi possível abrir a página de planos.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SubscriptionContent(
    overview: SubscriptionOverview,
    isTablet: Boolean,
    isExpanded: Boolean,
    accessExpanded: Boolean,
    featuresExpanded: Boolean,
    detailsExpanded: Boolean,
    historyExpanded: Boolean,
    onBackClick: () -> Unit,
    onToggleAccess: () -> Unit,
    onToggleFeatures: () -> Unit,
    onToggleDetails: () -> Unit,
    onToggleHistory: () -> Unit,
    onOpenPlans: () -> Unit
) {
    val contentMaxWidth =
        when {
            isExpanded ->
                1120.dp

            isTablet ->
                760.dp

            else ->
                560.dp
        }

    val horizontalPadding =
        when {
            isExpanded ->
                32.dp

            isTablet ->
                28.dp

            else ->
                16.dp
        }

    val sectionSpacing =
        if (
            isTablet
        ) {
            16.dp
        } else {
            14.dp
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .verticalScroll(
                    rememberScrollState()
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(
                        max =
                            contentMaxWidth
                    )
                    .padding(
                        horizontal =
                            horizontalPadding
                    )
        ) {
            SubscriptionTopBar(
                onBackClick =
                    onBackClick,

                isTablet =
                    isTablet
            )

            if (
                isExpanded
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            20.dp
                        ),

                    verticalAlignment =
                        Alignment.Top
                ) {
                    Column(
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    ) {
                        SubscriptionHeroCard(
                            overview =
                                overview,

                            isTablet =
                                true
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    sectionSpacing
                                )
                        )

                        overview.access
                            ?.let {
                                    access ->

                                SubscriptionAccessSection(
                                    overview =
                                        overview,

                                    access =
                                        access,

                                    expanded =
                                        accessExpanded,

                                    onToggle =
                                        onToggleAccess,

                                    isTablet =
                                        true
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            sectionSpacing
                                        )
                                )
                            }

                        val features =
                            overview.plan
                                ?.features
                                .orEmpty()

                        if (
                            features.isNotEmpty()
                        ) {
                            SubscriptionFeaturesSection(
                                overview =
                                    overview,

                                features =
                                    features,

                                expanded =
                                    featuresExpanded,

                                onToggle =
                                    onToggleFeatures,

                                isTablet =
                                    true
                            )
                        }
                    }

                    Column(
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    ) {
                        if (
                            hasSubscriptionDetails(
                                overview
                            )
                        ) {
                            SubscriptionDetailsSection(
                                overview =
                                    overview,

                                expanded =
                                    detailsExpanded,

                                onToggle =
                                    onToggleDetails,

                                isTablet =
                                    true
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        sectionSpacing
                                    )
                            )
                        }

                        SubscriptionHistorySection(
                            history =
                                overview.history,

                            expanded =
                                historyExpanded,

                            onToggle =
                                onToggleHistory,

                            isTablet =
                                true
                        )

                        if (
                            overview.supportReference
                                .isNotBlank()
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.height(
                                        sectionSpacing
                                    )
                            )

                            SupportReferenceCard(
                                reference =
                                    overview.supportReference,

                                isTablet =
                                    true
                            )
                        }

                        if (
                            shouldShowPlansButton(
                                overview
                            )
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.height(
                                        sectionSpacing
                                    )
                            )

                            PlansButton(
                                onClick =
                                    onOpenPlans,

                                isTablet =
                                    true
                            )
                        }
                    }
                }
            } else {
                SubscriptionHeroCard(
                    overview =
                        overview,

                    isTablet =
                        isTablet
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            sectionSpacing
                        )
                )

                overview.access
                    ?.let {
                            access ->

                        SubscriptionAccessSection(
                            overview =
                                overview,

                            access =
                                access,

                            expanded =
                                accessExpanded,

                            onToggle =
                                onToggleAccess,

                            isTablet =
                                isTablet
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    sectionSpacing
                                )
                        )
                    }

                val features =
                    overview.plan
                        ?.features
                        .orEmpty()

                if (
                    features.isNotEmpty()
                ) {
                    SubscriptionFeaturesSection(
                        overview =
                            overview,

                        features =
                            features,

                        expanded =
                            featuresExpanded,

                        onToggle =
                            onToggleFeatures,

                        isTablet =
                            isTablet
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                sectionSpacing
                            )
                    )
                }

                if (
                    hasSubscriptionDetails(
                        overview
                    )
                ) {
                    SubscriptionDetailsSection(
                        overview =
                            overview,

                        expanded =
                            detailsExpanded,

                        onToggle =
                            onToggleDetails,

                        isTablet =
                            isTablet
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                sectionSpacing
                            )
                    )
                }

                SubscriptionHistorySection(
                    history =
                        overview.history,

                    expanded =
                        historyExpanded,

                    onToggle =
                        onToggleHistory,

                    isTablet =
                        isTablet
                )

                if (
                    overview.supportReference
                        .isNotBlank()
                ) {
                    Spacer(
                        modifier =
                            Modifier.height(
                                sectionSpacing
                            )
                    )

                    SupportReferenceCard(
                        reference =
                            overview.supportReference,

                        isTablet =
                            isTablet
                    )
                }

                if (
                    shouldShowPlansButton(
                        overview
                    )
                ) {
                    Spacer(
                        modifier =
                            Modifier.height(
                                sectionSpacing
                            )
                    )

                    PlansButton(
                        onClick =
                            onOpenPlans,

                        isTablet =
                            isTablet
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        if (
                            isTablet
                        ) {
                            44.dp
                        } else {
                            32.dp
                        }
                    )
            )
        }
    }
}

@Composable
private fun SubscriptionTopBar(
    onBackClick: () -> Unit,
    isTablet: Boolean
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    if (
                        isTablet
                    ) {
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
                    if (
                        isTablet
                    ) {
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
                        if (
                            isTablet
                        ) {
                            30.dp
                        } else {
                            26.dp
                        }
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    if (
                        isTablet
                    ) {
                        8.dp
                    } else {
                        2.dp
                    }
                )
        )

        Text(
            text =
                "Minha assinatura",

            color =
                Color.White,

            fontSize =
                if (
                    isTablet
                ) {
                    28.sp
                } else {
                    23.sp
                },

            fontWeight =
                FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SubscriptionHeroCard(
    overview: SubscriptionOverview,
    isTablet: Boolean
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
                if (
                    isTablet
                ) {
                    20.dp
                } else {
                    16.dp
                }
            ),

        color =
            SubscriptionPanel,

        border =
            BorderStroke(
                1.dp,
                statusColor.copy(
                    alpha =
                        0.35f
                )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    if (
                        isTablet
                    ) {
                        24.dp
                    } else {
                        18.dp
                    }
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
                                if (
                                    isTablet
                                ) {
                                    58.dp
                                } else {
                                    48.dp
                                }
                            )
                            .background(
                                LaranjadaOrange.copy(
                                    alpha =
                                        0.12f
                                ),
                                RoundedCornerShape(
                                    if (
                                        isTablet
                                    ) {
                                        16.dp
                                    } else {
                                        13.dp
                                    }
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
                                if (
                                    isTablet
                                ) {
                                    29.dp
                                } else {
                                    24.dp
                                }
                            )
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            if (
                                isTablet
                            ) {
                                16.dp
                            } else {
                                13.dp
                            }
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
                            if (
                                isTablet
                            ) {
                                24.sp
                            } else {
                                20.sp
                            },

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

                                plan !=
                                        null ->
                                    "Último acesso"

                                else ->
                                    "Sua assinatura"
                            },

                        color =
                            LaranjadaMutedText,

                        fontSize =
                            if (
                                isTablet
                            ) {
                                14.sp
                            } else {
                                12.sp
                            },

                        modifier =
                            Modifier.padding(
                                top =
                                    3.dp
                            )
                    )
                }

                SubscriptionStatusPill(
                    text =
                        overview.state.label,

                    color =
                        statusColor,

                    isTablet =
                        isTablet
                )
            }

            if (
                plan !=
                null &&
                plan.priceLabel
                    .isNotBlank()
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            if (
                                isTablet
                            ) {
                                24.dp
                            } else {
                                20.dp
                            }
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
                            if (
                                isTablet
                            ) {
                                32.sp
                            } else {
                                27.sp
                            },

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
                                if (
                                    isTablet
                                ) {
                                    15.sp
                                } else {
                                    13.sp
                                },

                            modifier =
                                Modifier.padding(
                                    bottom =
                                        5.dp
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
                paymentMethod !=
                null ||
                accessType !=
                null
            ) {
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
                    Icon(
                        imageVector =
                            Icons.Rounded.Payments,

                        contentDescription =
                            null,

                        tint =
                            LaranjadaOrange,

                        modifier =
                            Modifier.size(
                                if (
                                    isTablet
                                ) {
                                    22.dp
                                } else {
                                    18.dp
                                }
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                8.dp
                            )
                    )

                    Text(
                        text =
                            paymentMethod
                                ?: accessType
                                    .orEmpty(),

                        color =
                            Color.White.copy(
                                alpha =
                                    0.85f
                            ),

                        fontSize =
                            if (
                                isTablet
                            ) {
                                15.sp
                            } else {
                                13.sp
                            },

                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }

            if (
                !overview.hasAccess
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                HorizontalDivider(
                    color =
                        Color.White.copy(
                            alpha =
                                0.08f
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
                        if (
                            isTablet
                        ) {
                            14.sp
                        } else {
                            12.sp
                        },

                    lineHeight =
                        if (
                            isTablet
                        ) {
                            20.sp
                        } else {
                            18.sp
                        },

                    modifier =
                        Modifier.padding(
                            top =
                                14.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun SubscriptionAccessSection(
    overview: SubscriptionOverview,
    access: SubscriptionAccessSummary,
    expanded: Boolean,
    onToggle: () -> Unit,
    isTablet: Boolean
) {
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
            expanded,

        onClick =
            onToggle,

        isTablet =
            isTablet
    )

    if (
        expanded
    ) {
        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        AccessDetailsCard(
            access =
                access,

            isTablet =
                isTablet
        )
    }
}

@Composable
private fun SubscriptionFeaturesSection(
    overview: SubscriptionOverview,
    features: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit,
    isTablet: Boolean
) {
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
            expanded,

        onClick =
            onToggle,

        isTablet =
            isTablet
    )

    if (
        expanded
    ) {
        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        FeaturesCard(
            features =
                features,

            isTablet =
                isTablet
        )
    }
}

@Composable
private fun SubscriptionDetailsSection(
    overview: SubscriptionOverview,
    expanded: Boolean,
    onToggle: () -> Unit,
    isTablet: Boolean
) {
    ExpandableSubscriptionSection(
        icon =
            Icons.Rounded.Info,

        title =
            "Detalhes do acesso",

        subtitle =
            "Plano, pagamento e informações do acesso.",

        expanded =
            expanded,

        onClick =
            onToggle,

        isTablet =
            isTablet
    )

    if (
        expanded
    ) {
        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        SubscriptionDetailsCard(
            overview =
                overview,

            isTablet =
                isTablet
        )
    }
}

@Composable
private fun SubscriptionHistorySection(
    history: List<SubscriptionHistoryItem>,
    expanded: Boolean,
    onToggle: () -> Unit,
    isTablet: Boolean
) {
    ExpandableSubscriptionSection(
        icon =
            Icons.Rounded.History,

        title =
            "Histórico de pagamentos",

        subtitle =
            historySubtitle(
                history.size
            ),

        expanded =
            expanded,

        onClick =
            onToggle,

        isTablet =
            isTablet
    )

    if (
        expanded
    ) {
        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        if (
            history.isEmpty()
        ) {
            EmptyHistoryCard(
                isTablet =
                    isTablet
            )
        } else {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {
                history.forEach {
                        item ->

                    HistoryCard(
                        item =
                            item,

                        isTablet =
                            isTablet
                    )
                }
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
    onClick: () -> Unit,
    isTablet: Boolean
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
                if (
                    isTablet
                ) {
                    18.dp
                } else {
                    14.dp
                }
            ),

        color =
            SubscriptionPanel
    ) {
        Row(
            modifier =
                Modifier.padding(
                    if (
                        isTablet
                    ) {
                        20.dp
                    } else {
                        16.dp
                    }
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            if (
                                isTablet
                            ) {
                                50.dp
                            } else {
                                42.dp
                            }
                        )
                        .background(
                            LaranjadaOrange.copy(
                                alpha =
                                    0.12f
                            ),
                            RoundedCornerShape(
                                if (
                                    isTablet
                                ) {
                                    14.dp
                                } else {
                                    11.dp
                                }
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
                            if (
                                isTablet
                            ) {
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
                        if (
                            isTablet
                        ) {
                            16.dp
                        } else {
                            14.dp
                        }
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
                        if (
                            isTablet
                        ) {
                            18.sp
                        } else {
                            16.sp
                        },

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        subtitle,

                    color =
                        LaranjadaMutedText,

                    fontSize =
                        if (
                            isTablet
                        ) {
                            13.sp
                        } else {
                            12.sp
                        },

                    lineHeight =
                        if (
                            isTablet
                        ) {
                            19.sp
                        } else {
                            17.sp
                        },

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
                        alpha =
                            0.55f
                    ),

                modifier =
                    Modifier.size(
                        if (
                            isTablet
                        ) {
                            30.dp
                        } else {
                            26.dp
                        }
                    )
            )
        }
    }
}

@Composable
private fun AccessDetailsCard(
    access: SubscriptionAccessSummary,
    isTablet: Boolean
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                if (
                    isTablet
                ) {
                    18.dp
                } else {
                    14.dp
                }
            ),

        color =
            SubscriptionPanelSecondary
    ) {
        Column(
            modifier =
                Modifier.padding(
                    if (
                        isTablet
                    ) {
                        20.dp
                    } else {
                        16.dp
                    }
                )
        ) {
            SubscriptionDetailRow(
                label =
                    "Início do período",

                value =
                    formatSubscriptionDate(
                        access.periodStartAt
                    ),

                isTablet =
                    isTablet
            )

            DetailDivider()

            SubscriptionDetailRow(
                label =
                    "Fim do período",

                value =
                    formatSubscriptionDate(
                        access.periodEndAt
                    ),

                isTablet =
                    isTablet
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
                        access.typeLabel,

                    isTablet =
                        isTablet
                )
            }
        }
    }
}

@Composable
private fun FeaturesCard(
    features: List<String>,
    isTablet: Boolean
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                if (
                    isTablet
                ) {
                    18.dp
                } else {
                    14.dp
                }
            ),

        color =
            SubscriptionPanelSecondary
    ) {
        Column(
            modifier =
                Modifier.padding(
                    if (
                        isTablet
                    ) {
                        20.dp
                    } else {
                        16.dp
                    }
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    if (
                        isTablet
                    ) {
                        14.dp
                    } else {
                        12.dp
                    }
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
                                    top =
                                        2.dp
                                )
                                .size(
                                    if (
                                        isTablet
                                    ) {
                                        21.dp
                                    } else {
                                        18.dp
                                    }
                                )
                                .background(
                                    SubscriptionGreen.copy(
                                        alpha =
                                            0.12f
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
                                    if (
                                        isTablet
                                    ) {
                                        15.dp
                                    } else {
                                        13.dp
                                    }
                                )
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(
                                10.dp
                            )
                    )

                    Text(
                        text =
                            feature,

                        color =
                            Color.White.copy(
                                alpha =
                                    0.90f
                            ),

                        fontSize =
                            if (
                                isTablet
                            ) {
                                15.sp
                            } else {
                                13.sp
                            },

                        lineHeight =
                            if (
                                isTablet
                            ) {
                                21.sp
                            } else {
                                19.sp
                            },

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
    overview: SubscriptionOverview,
    isTablet: Boolean
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                if (
                    isTablet
                ) {
                    18.dp
                } else {
                    14.dp
                }
            ),

        color =
            SubscriptionPanelSecondary
    ) {
        Column(
            modifier =
                Modifier.padding(
                    if (
                        isTablet
                    ) {
                        20.dp
                    } else {
                        16.dp
                    }
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
                    index >
                    0
                ) {
                    DetailDivider()
                }

                SubscriptionDetailRow(
                    label =
                        row.first,

                    value =
                        row.second,

                    isTablet =
                        isTablet
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
                            discount,

                        isTablet =
                            isTablet
                    )
                }
        }
    }
}

@Composable
private fun DiscountDetails(
    discount: SubscriptionDiscountSummary,
    isTablet: Boolean
) {
    Column {
        Text(
            text =
                "Desconto aplicado",

            color =
                LaranjadaOrange,

            fontSize =
                if (
                    isTablet
                ) {
                    14.sp
                } else {
                    12.sp
                },

            fontWeight =
                FontWeight.Bold,

            modifier =
                Modifier.padding(
                    vertical =
                        6.dp
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
                    row.second,

                isTablet =
                    isTablet
            )
        }
    }
}

@Composable
private fun SubscriptionDetailRow(
    label: String,
    value: String,
    isTablet: Boolean
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical =
                        if (
                            isTablet
                        ) {
                            9.dp
                        } else {
                            7.dp
                        }
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
                if (
                    isTablet
                ) {
                    14.sp
                } else {
                    12.sp
                },

            modifier =
                Modifier.width(
                    if (
                        isTablet
                    ) {
                        142.dp
                    } else {
                        118.dp
                    }
                )
        )

        Text(
            text =
                value.ifBlank {
                    "Não informado"
                },

            color =
                Color.White.copy(
                    alpha =
                        0.90f
                ),

            fontSize =
                if (
                    isTablet
                ) {
                    14.sp
                } else {
                    12.sp
                },

            fontWeight =
                FontWeight.SemiBold,

            lineHeight =
                if (
                    isTablet
                ) {
                    20.sp
                } else {
                    17.sp
                },

            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}

@Composable
private fun HistoryCard(
    item: SubscriptionHistoryItem,
    isTablet: Boolean
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
                if (
                    isTablet
                ) {
                    18.dp
                } else {
                    14.dp
                }
            ),

        color =
            SubscriptionPanelSecondary
    ) {
        Row(
            modifier =
                Modifier.padding(
                    if (
                        isTablet
                    ) {
                        18.dp
                    } else {
                        15.dp
                    }
                ),

            verticalAlignment =
                Alignment.Top
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            if (
                                isTablet
                            ) {
                                46.dp
                            } else {
                                40.dp
                            }
                        )
                        .background(
                            toneColor.copy(
                                alpha =
                                    0.12f
                            ),
                            RoundedCornerShape(
                                if (
                                    isTablet
                                ) {
                                    12.dp
                                } else {
                                    10.dp
                                }
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
                            if (
                                isTablet
                            ) {
                                24.dp
                            } else {
                                21.dp
                            }
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
                        item.planName
                            .ifBlank {
                                "Pagamento"
                            },

                    color =
                        LaranjadaText,

                    fontSize =
                        if (
                            isTablet
                        ) {
                            16.sp
                        } else {
                            14.sp
                        },

                    fontWeight =
                        FontWeight.Bold,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                Spacer(
                    modifier =
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
                        toneColor,

                    isTablet =
                        isTablet
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
                    paymentLine
                        .isNotBlank()
                ) {
                    Text(
                        text =
                            paymentLine,

                        color =
                            Color.White.copy(
                                alpha =
                                    0.80f
                            ),

                        fontSize =
                            if (
                                isTablet
                            ) {
                                14.sp
                            } else {
                                12.sp
                            },

                        fontWeight =
                            FontWeight.SemiBold,

                        modifier =
                            Modifier.padding(
                                top =
                                    6.dp
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
                            if (
                                isTablet
                            ) {
                                13.sp
                            } else {
                                11.sp
                            },

                        modifier =
                            Modifier.padding(
                                top =
                                    4.dp
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
                            if (
                                isTablet
                            ) {
                                13.sp
                            } else {
                                11.sp
                            },

                        lineHeight =
                            if (
                                isTablet
                            ) {
                                18.sp
                            } else {
                                16.sp
                            },

                        modifier =
                            Modifier.padding(
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
private fun EmptyHistoryCard(
    isTablet: Boolean
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                if (
                    isTablet
                ) {
                    18.dp
                } else {
                    14.dp
                }
            ),

        color =
            SubscriptionPanelSecondary
    ) {
        Column(
            modifier =
                Modifier.padding(
                    if (
                        isTablet
                    ) {
                        28.dp
                    } else {
                        22.dp
                    }
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
                        if (
                            isTablet
                        ) {
                            36.dp
                        } else {
                            30.dp
                        }
                    )
            )

            Text(
                text =
                    "Nenhum pagamento registrado",

                color =
                    LaranjadaText,

                fontSize =
                    if (
                        isTablet
                    ) {
                        16.sp
                    } else {
                        14.sp
                    },

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
                    "Quando houver pagamentos associados à sua conta, eles aparecerão aqui.",

                color =
                    LaranjadaMutedText,

                fontSize =
                    if (
                        isTablet
                    ) {
                        14.sp
                    } else {
                        12.sp
                    },

                lineHeight =
                    if (
                        isTablet
                    ) {
                        20.sp
                    } else {
                        17.sp
                    },

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
private fun SupportReferenceCard(
    reference: String,
    isTablet: Boolean
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                if (
                    isTablet
                ) {
                    18.dp
                } else {
                    14.dp
                }
            ),

        color =
            SubscriptionPanel
    ) {
        Row(
            modifier =
                Modifier.padding(
                    if (
                        isTablet
                    ) {
                        20.dp
                    } else {
                        16.dp
                    }
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            if (
                                isTablet
                            ) {
                                50.dp
                            } else {
                                42.dp
                            }
                        )
                        .background(
                            LaranjadaOrange.copy(
                                alpha =
                                    0.12f
                            ),
                            RoundedCornerShape(
                                if (
                                    isTablet
                                ) {
                                    14.dp
                                } else {
                                    11.dp
                                }
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
                        LaranjadaOrange,

                    modifier =
                        Modifier.size(
                            if (
                                isTablet
                            ) {
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
                        if (
                            isTablet
                        ) {
                            14.sp
                        } else {
                            12.sp
                        }
                )

                Text(
                    text =
                        reference,

                    color =
                        LaranjadaText,

                    fontSize =
                        if (
                            isTablet
                        ) {
                            18.sp
                        } else {
                            16.sp
                        },

                    fontWeight =
                        FontWeight.ExtraBold,

                    letterSpacing =
                        0.8.sp,

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

@Composable
private fun PlansButton(
    onClick: () -> Unit,
    isTablet: Boolean
) {
    Button(
        onClick =
            onClick,

        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    if (
                        isTablet
                    ) {
                        58.dp
                    } else {
                        52.dp
                    }
                ),

        shape =
            RoundedCornerShape(
                if (
                    isTablet
                ) {
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
        Text(
            text =
                "VER PLANOS",

            fontSize =
                if (
                    isTablet
                ) {
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
                Icons.Rounded.OpenInNew,

            contentDescription =
                null,

            modifier =
                Modifier.size(
                    if (
                        isTablet
                    ) {
                        21.dp
                    } else {
                        18.dp
                    }
                )
        )
    }

    Text(
        text =
            "A contratação continua pela página Web do Laranjada.",

        color =
            LaranjadaMutedText,

        fontSize =
            if (
                isTablet
            ) {
                13.sp
            } else {
                11.sp
            },

        lineHeight =
            if (
                isTablet
            ) {
                18.sp
            } else {
                16.sp
            },

        textAlign =
            TextAlign.Center,

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top =
                        8.dp
                )
    )
}

@Composable
private fun SubscriptionStatusPill(
    text: String,
    color: Color,
    isTablet: Boolean
) {
    Surface(
        shape =
            RoundedCornerShape(
                50.dp
            ),

        color =
            color.copy(
                alpha =
                    0.12f
            ),

        border =
            BorderStroke(
                1.dp,
                color.copy(
                    alpha =
                        0.30f
                )
            )
    ) {
        Text(
            text =
                text,

            color =
                color,

            fontSize =
                if (
                    isTablet
                ) {
                    12.sp
                } else {
                    10.sp
                },

            fontWeight =
                FontWeight.Bold,

            maxLines =
                1,

            modifier =
                Modifier.padding(
                    horizontal =
                        if (
                            isTablet
                        ) {
                            11.dp
                        } else {
                            9.dp
                        },

                    vertical =
                        if (
                            isTablet
                        ) {
                            6.dp
                        } else {
                            5.dp
                        }
                )
        )
    }
}

@Composable
private fun HistoryStatusPill(
    text: String,
    color: Color,
    isTablet: Boolean
) {
    Surface(
        shape =
            RoundedCornerShape(
                50.dp
            ),

        color =
            color.copy(
                alpha =
                    0.12f
            )
    ) {
        Text(
            text =
                text,

            color =
                color,

            fontSize =
                if (
                    isTablet
                ) {
                    11.sp
                } else {
                    9.sp
                },

            fontWeight =
                FontWeight.Bold,

            maxLines =
                1,

            modifier =
                Modifier.padding(
                    horizontal =
                        if (
                            isTablet
                        ) {
                            9.dp
                        } else {
                            7.dp
                        },

                    vertical =
                        if (
                            isTablet
                        ) {
                            5.dp
                        } else {
                            4.dp
                        }
                )
        )
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        color =
            Color.White.copy(
                alpha =
                    0.07f
            )
    )
}

@Composable
private fun SubscriptionLoadingState() {
    Box(
        modifier =
            Modifier
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
    isTablet: Boolean
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    LaranjadaBlack
                )
                .padding(
                    if (
                        isTablet
                    ) {
                        40.dp
                    } else {
                        28.dp
                    }
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
                    if (
                        isTablet
                    ) {
                        52.dp
                    } else {
                        40.dp
                    }
                )
        )

        Text(
            text =
                "Não foi possível carregar sua assinatura",

            color =
                LaranjadaText,

            fontSize =
                if (
                    isTablet
                ) {
                    23.sp
                } else {
                    18.sp
                },

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
                if (
                    isTablet
                ) {
                    15.sp
                } else {
                    13.sp
                },

            lineHeight =
                if (
                    isTablet
                ) {
                    21.sp
                } else {
                    18.sp
                },

            textAlign =
                TextAlign.Center,

            modifier =
                Modifier
                    .widthIn(
                        max =
                            560.dp
                    )
                    .padding(
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

private fun hasSubscriptionDetails(
    overview: SubscriptionOverview
): Boolean {
    return overview.plan !=
            null ||
            overview.access !=
            null ||
            overview.payment !=
            null ||
            overview.discount !=
            null
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
            date !=
            null
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