package com.rnx.laranjada.domain.model

data class SubscriptionOverview(
    val hasAccess: Boolean,
    val state: SubscriptionState,
    val plan: SubscriptionPlanSummary?,
    val access: SubscriptionAccessSummary?,
    val payment: SubscriptionPaymentSummary?,
    val discount: SubscriptionDiscountSummary?,
    val supportReference: String,
    val history: List<SubscriptionHistoryItem>
)

data class SubscriptionState(
    val code: String,
    val label: String
)

data class SubscriptionPlanSummary(
    val uuid: String,
    val name: String,
    val price: String,
    val priceLabel: String,
    val periodLabel: String,
    val accessSummary: String,
    val features: List<String>
)

data class SubscriptionAccessSummary(
    val type: String,
    val typeLabel: String,
    val periodStartAt: String?,
    val periodEndAt: String?
)

data class SubscriptionPaymentSummary(
    val method: String,
    val methodLabel: String,
    val amountLabel: String,
    val paidAt: String?
)

data class SubscriptionDiscountSummary(
    val name: String,
    val code: String,
    val originalAmountLabel: String,
    val discountAmountLabel: String,
    val finalAmountLabel: String
)

data class SubscriptionHistoryItem(
    val kind: String,
    val planName: String,
    val amountLabel: String,
    val status: String,
    val statusLabel: String,
    val statusTone: String,
    val paymentMethod: String,
    val occurredAt: String?,
    val detail: String
)