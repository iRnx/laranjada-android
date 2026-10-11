package com.rnx.laranjada.data.mapper

import com.rnx.laranjada.domain.model.SubscriptionAccessSummary
import com.rnx.laranjada.domain.model.SubscriptionDiscountSummary
import com.rnx.laranjada.domain.model.SubscriptionHistoryItem
import com.rnx.laranjada.domain.model.SubscriptionOverview
import com.rnx.laranjada.domain.model.SubscriptionPaymentSummary
import com.rnx.laranjada.domain.model.SubscriptionPlanSummary
import com.rnx.laranjada.domain.model.SubscriptionState
import org.json.JSONArray
import org.json.JSONObject

object SubscriptionMapper {

    fun overview(
        json: JSONObject
    ): SubscriptionOverview {

        val stateJson =
            json.optJSONObject(
                "state"
            )
                ?: JSONObject()

        val plan =
            json.optNullableObject(
                "plan"
            )
                ?.let {
                        planJson ->

                    SubscriptionPlanSummary(
                        uuid =
                            planJson
                                .optString(
                                    "uuid"
                                )
                                .trim(),

                        name =
                            planJson
                                .optString(
                                    "name"
                                )
                                .trim(),

                        price =
                            planJson
                                .optString(
                                    "price"
                                )
                                .trim(),

                        priceLabel =
                            planJson
                                .optString(
                                    "price_label"
                                )
                                .trim(),

                        periodLabel =
                            planJson
                                .optString(
                                    "period_label"
                                )
                                .trim(),

                        accessSummary =
                            planJson
                                .optString(
                                    "access_summary"
                                )
                                .trim(),

                        features =
                            planJson
                                .optJSONArray(
                                    "features"
                                )
                                .toStringList()
                    )
                }

        val access =
            json.optNullableObject(
                "access"
            )
                ?.let {
                        accessJson ->

                    SubscriptionAccessSummary(
                        type =
                            accessJson
                                .optString(
                                    "type"
                                )
                                .trim(),

                        typeLabel =
                            accessJson
                                .optString(
                                    "type_label"
                                )
                                .trim(),

                        periodStartAt =
                            accessJson
                                .optNullableString(
                                    "period_start_at"
                                ),

                        periodEndAt =
                            accessJson
                                .optNullableString(
                                    "period_end_at"
                                )
                    )
                }

        val payment =
            json.optNullableObject(
                "payment"
            )
                ?.let {
                        paymentJson ->

                    SubscriptionPaymentSummary(
                        method =
                            paymentJson
                                .optString(
                                    "method"
                                )
                                .trim(),

                        methodLabel =
                            paymentJson
                                .optString(
                                    "method_label"
                                )
                                .trim(),

                        amountLabel =
                            paymentJson
                                .optString(
                                    "amount_label"
                                )
                                .trim(),

                        paidAt =
                            paymentJson
                                .optNullableString(
                                    "paid_at"
                                )
                    )
                }

        val discount =
            json.optNullableObject(
                "discount"
            )
                ?.let {
                        discountJson ->

                    SubscriptionDiscountSummary(
                        name =
                            discountJson
                                .optString(
                                    "name"
                                )
                                .trim(),

                        code =
                            discountJson
                                .optString(
                                    "code"
                                )
                                .trim(),

                        originalAmountLabel =
                            discountJson
                                .optString(
                                    "original_amount_label"
                                )
                                .trim(),

                        discountAmountLabel =
                            discountJson
                                .optString(
                                    "discount_amount_label"
                                )
                                .trim(),

                        finalAmountLabel =
                            discountJson
                                .optString(
                                    "final_amount_label"
                                )
                                .trim()
                    )
                }

        val historyArray =
            json.optJSONArray(
                "history"
            )

        val history =
            buildList {
                if (
                    historyArray != null
                ) {
                    for (
                    index in
                    0 until historyArray.length()
                    ) {
                        val item =
                            historyArray
                                .optJSONObject(
                                    index
                                )
                                ?: continue

                        add(
                            historyItem(
                                item
                            )
                        )
                    }
                }
            }

        return SubscriptionOverview(
            hasAccess =
                json.optBoolean(
                    "has_access",
                    false
                ),

            state =
                SubscriptionState(
                    code =
                        stateJson
                            .optString(
                                "code"
                            )
                            .trim()
                            .ifBlank {
                                "none"
                            },

                    label =
                        stateJson
                            .optString(
                                "label"
                            )
                            .trim()
                            .ifBlank {
                                "Sem acesso ativo"
                            }
                ),

            plan =
                plan,

            access =
                access,

            payment =
                payment,

            discount =
                discount,

            supportReference =
                json.optString(
                    "support_reference"
                )
                    .trim(),

            history =
                history
        )
    }

    private fun historyItem(
        json: JSONObject
    ): SubscriptionHistoryItem {

        return SubscriptionHistoryItem(
            kind =
                json.optString(
                    "kind"
                )
                    .trim(),

            planName =
                json.optString(
                    "plan_name"
                )
                    .trim(),

            amountLabel =
                json.optString(
                    "amount_label"
                )
                    .trim(),

            status =
                json.optString(
                    "status"
                )
                    .trim(),

            statusLabel =
                json.optString(
                    "status_label"
                )
                    .trim(),

            statusTone =
                json.optString(
                    "status_tone"
                )
                    .trim()
                    .lowercase(),

            paymentMethod =
                json.optString(
                    "payment_method"
                )
                    .trim(),

            occurredAt =
                json.optNullableString(
                    "occurred_at"
                ),

            detail =
                json.optString(
                    "detail"
                )
                    .trim()
        )
    }

    private fun JSONObject.optNullableObject(
        name: String
    ): JSONObject? {

        if (
            !has(name) ||
            isNull(name)
        ) {
            return null
        }

        return optJSONObject(
            name
        )
    }

    private fun JSONObject.optNullableString(
        name: String
    ): String? {

        if (
            !has(name) ||
            isNull(name)
        ) {
            return null
        }

        return optString(
            name
        )
            .trim()
            .takeIf {
                it.isNotBlank() &&
                        it.lowercase() !=
                        "null"
            }
    }

    private fun JSONArray?.toStringList():
            List<String> {

        if (
            this == null
        ) {
            return emptyList()
        }

        return buildList {
            for (
            index in
            0 until length()
            ) {
                val value =
                    optString(
                        index
                    )
                        .trim()

                if (
                    value.isNotBlank()
                ) {
                    add(
                        value
                    )
                }
            }
        }
    }
}