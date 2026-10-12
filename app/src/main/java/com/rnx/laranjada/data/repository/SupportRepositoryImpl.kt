package com.rnx.laranjada.data.repository

import com.rnx.laranjada.data.mapper.SupportMapper
import com.rnx.laranjada.data.remote.api.SupportApiService
import com.rnx.laranjada.domain.model.SupportCenterPage
import com.rnx.laranjada.domain.model.SupportConversationDetail
import com.rnx.laranjada.domain.model.SupportCreateRequest
import com.rnx.laranjada.domain.model.SupportCreateResult
import com.rnx.laranjada.domain.model.SupportReplyResult
import com.rnx.laranjada.domain.repository.SupportRepository
import org.json.JSONObject

class SupportRepositoryImpl(
    private val api:
    SupportApiService =
        SupportApiService
) : SupportRepository {

    override suspend fun getCenter(
        page: Int
    ): SupportCenterPage {

        return SupportMapper.centerPage(
            api.getCenter(
                page
            )
        )
    }

    override suspend fun getConversation(
        conversationUuid: String
    ): SupportConversationDetail {

        return SupportMapper.conversationDetail(
            api.getConversation(
                conversationUuid
            )
        )
    }

    override suspend fun createConversation(
        request: SupportCreateRequest
    ): SupportCreateResult {

        val body =
            JSONObject()
                .put(
                    "category",
                    request.category
                )
                .put(
                    "message",
                    request.message
                )

        if (
            request.category ==
            "content_request"
        ) {
            body
                .put(
                    "content_type",
                    request.contentType
                )
                .put(
                    "content_title",
                    request.contentTitle
                )

            request.releaseYear
                ?.let {
                        year ->

                    body.put(
                        "release_year",
                        year
                    )
                }

        } else {
            body.put(
                "subject",
                request.subject
            )
        }

        return SupportMapper.createResult(
            api.createConversation(
                body
            )
        )
    }

    override suspend fun sendMessage(
        conversationUuid: String,
        body: String
    ): SupportReplyResult {

        val payload =
            JSONObject()
                .put(
                    "body",
                    body
                )

        return SupportMapper.replyResult(
            api.sendMessage(
                conversationUuid =
                    conversationUuid,

                body =
                    payload
            )
        )
    }
}