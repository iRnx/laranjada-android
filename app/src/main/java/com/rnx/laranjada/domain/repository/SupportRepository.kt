package com.rnx.laranjada.domain.repository

import com.rnx.laranjada.domain.model.SupportCenterPage
import com.rnx.laranjada.domain.model.SupportConversationDetail
import com.rnx.laranjada.domain.model.SupportCreateRequest
import com.rnx.laranjada.domain.model.SupportCreateResult
import com.rnx.laranjada.domain.model.SupportReplyResult

interface SupportRepository {

    suspend fun getCenter(
        page: Int = 1
    ): SupportCenterPage

    suspend fun getConversation(
        conversationUuid: String
    ): SupportConversationDetail

    suspend fun createConversation(
        request: SupportCreateRequest
    ): SupportCreateResult

    suspend fun sendMessage(
        conversationUuid: String,
        body: String
    ): SupportReplyResult
}