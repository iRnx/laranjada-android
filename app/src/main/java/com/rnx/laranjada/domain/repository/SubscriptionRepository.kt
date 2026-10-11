package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.domain.model.SubscriptionOverview

interface SubscriptionRepository {

    suspend fun getOverview():
            SubscriptionOverview
}