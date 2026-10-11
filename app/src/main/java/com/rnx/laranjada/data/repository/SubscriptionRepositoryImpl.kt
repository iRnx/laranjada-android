package com.rnx.laranjada.data.repository

import com.rnx.laranjada.data.mapper.SubscriptionMapper
import com.rnx.laranjada.data.remote.api.SubscriptionApiService
import com.rnx.laranjada.domain.model.SubscriptionOverview
import com.rnx.laranjada.domain.repository.SubscriptionRepository

class SubscriptionRepositoryImpl(
    private val api:
    SubscriptionApiService =
        SubscriptionApiService
) : SubscriptionRepository {

    override suspend fun getOverview():
            SubscriptionOverview {

        return SubscriptionMapper.overview(
            api.getOverview()
        )
    }
}