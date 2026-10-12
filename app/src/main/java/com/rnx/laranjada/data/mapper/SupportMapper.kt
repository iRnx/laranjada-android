package com.rnx.laranjada.data.mapper

import com.rnx.laranjada.domain.model.SupportCenterPage
import com.rnx.laranjada.domain.model.SupportChoice
import com.rnx.laranjada.domain.model.SupportConstraints
import com.rnx.laranjada.domain.model.SupportContentRequest
import com.rnx.laranjada.domain.model.SupportConversationDetail
import com.rnx.laranjada.domain.model.SupportConversationInfo
import com.rnx.laranjada.domain.model.SupportConversationSummary
import com.rnx.laranjada.domain.model.SupportCreateResult
import com.rnx.laranjada.domain.model.SupportCreatedConversation
import com.rnx.laranjada.domain.model.SupportCreationConfig
import com.rnx.laranjada.domain.model.SupportMessage
import com.rnx.laranjada.domain.model.SupportPagination
import com.rnx.laranjada.domain.model.SupportReplyConversationState
import com.rnx.laranjada.domain.model.SupportReplyResult
import org.json.JSONArray
import org.json.JSONObject

object SupportMapper {

    fun centerPage(
        json: JSONObject
    ): SupportCenterPage {

        val creationJson =
            json.optJSONObject(
                "creation"
            )
                ?: JSONObject()

        val constraintsJson =
            creationJson.optJSONObject(
                "constraints"
            )
                ?: JSONObject()

        val paginationJson =
            json.optJSONObject(
                "pagination"
            )
                ?: JSONObject()

        return SupportCenterPage(
            creation =
                SupportCreationConfig(
                    canRequestContent =
                        creationJson.optBoolean(
                            "can_request_content",
                            false
                        ),

                    categories =
                        choices(
                            creationJson.optJSONArray(
                                "categories"
                            )
                        ),

                    contentTypes =
                        choices(
                            creationJson.optJSONArray(
                                "content_types"
                            )
                        ),

                    constraints =
                        SupportConstraints(
                            subjectMaxLength =
                                constraintsJson.optInt(
                                    "subject_max_length",
                                    0
                                )
                                    .coerceAtLeast(
                                        0
                                    ),

                            messageMaxLength =
                                constraintsJson.optInt(
                                    "message_max_length",
                                    0
                                )
                                    .coerceAtLeast(
                                        0
                                    ),

                            initialMessageMinLength =
                                constraintsJson.optInt(
                                    "initial_message_min_length",
                                    0
                                )
                                    .coerceAtLeast(
                                        0
                                    ),

                            replyMinLength =
                                constraintsJson.optInt(
                                    "reply_min_length",
                                    0
                                )
                                    .coerceAtLeast(
                                        0
                                    ),

                            releaseYearMin =
                                constraintsJson.optInt(
                                    "release_year_min",
                                    0
                                )
                                    .coerceAtLeast(
                                        0
                                    ),

                            releaseYearMax =
                                constraintsJson.optInt(
                                    "release_year_max",
                                    0
                                )
                                    .coerceAtLeast(
                                        0
                                    )
                        )
                ),

            pagination =
                SupportPagination(
                    page =
                        paginationJson.optInt(
                            "page",
                            1
                        )
                            .coerceAtLeast(
                                1
                            ),

                    pages =
                        paginationJson.optInt(
                            "pages",
                            1
                        )
                            .coerceAtLeast(
                                1
                            ),

                    count =
                        paginationJson.optInt(
                            "count",
                            0
                        )
                            .coerceAtLeast(
                                0
                            ),

                    hasPrevious =
                        paginationJson.optBoolean(
                            "has_previous",
                            false
                        ),

                    hasNext =
                        paginationJson.optBoolean(
                            "has_next",
                            false
                        )
                ),

            conversations =
                conversations(
                    json.optJSONArray(
                        "conversations"
                    )
                )
        )
    }

    fun conversationDetail(
        json: JSONObject
    ): SupportConversationDetail {

        val conversationJson =
            json.optJSONObject(
                "conversation"
            )
                ?: JSONObject()

        val contentRequest =
            json.optNullableObject(
                "content_request"
            )
                ?.let {
                        contentJson ->

                    SupportContentRequest(
                        contentType =
                            choice(
                                contentJson.optJSONObject(
                                    "content_type"
                                )
                            ),

                        title =
                            contentJson.optString(
                                "title"
                            )
                                .trim(),

                        releaseYear =
                            contentJson.optInt(
                                "release_year",
                                0
                            ),

                        decision =
                            choice(
                                contentJson.optJSONObject(
                                    "decision"
                                )
                            )
                    )
                }

        return SupportConversationDetail(
            conversation =
                SupportConversationInfo(
                    uuid =
                        conversationJson.optString(
                            "uuid"
                        )
                            .trim(),

                    subject =
                        conversationJson.optString(
                            "subject"
                        )
                            .trim(),

                    category =
                        choice(
                            conversationJson.optJSONObject(
                                "category"
                            )
                        ),

                    status =
                        choice(
                            conversationJson.optJSONObject(
                                "status"
                            )
                        ),

                    canReply =
                        conversationJson.optBoolean(
                            "can_reply",
                            false
                        ),

                    createdAt =
                        conversationJson.optNullableString(
                            "created_at"
                        ),

                    updatedAt =
                        conversationJson.optNullableString(
                            "updated_at"
                        ),

                    lastMessageAt =
                        conversationJson.optNullableString(
                            "last_message_at"
                        ),

                    closedAt =
                        conversationJson.optNullableString(
                            "closed_at"
                        )
                ),

            contentRequest =
                contentRequest,

            messages =
                messages(
                    json.optJSONArray(
                        "messages"
                    )
                )
        )
    }

    fun createResult(
        json: JSONObject
    ): SupportCreateResult {

        val conversationJson =
            json.optJSONObject(
                "conversation"
            )
                ?: JSONObject()

        return SupportCreateResult(
            message =
                json.optString(
                    "message"
                )
                    .trim()
                    .ifBlank {
                        "Sua mensagem foi enviada."
                    },

            conversation =
                SupportCreatedConversation(
                    uuid =
                        conversationJson.optString(
                            "uuid"
                        )
                            .trim(),

                    subject =
                        conversationJson.optString(
                            "subject"
                        )
                            .trim(),

                    category =
                        choice(
                            conversationJson.optJSONObject(
                                "category"
                            )
                        ),

                    status =
                        choice(
                            conversationJson.optJSONObject(
                                "status"
                            )
                        ),

                    canReply =
                        conversationJson.optBoolean(
                            "can_reply",
                            true
                        )
                )
        )
    }

    fun replyResult(
        json: JSONObject
    ): SupportReplyResult {

        val conversationJson =
            json.optJSONObject(
                "conversation"
            )
                ?: JSONObject()

        val messageJson =
            json.optJSONObject(
                "support_message"
            )
                ?: JSONObject()

        return SupportReplyResult(
            message =
                json.optString(
                    "message"
                )
                    .trim()
                    .ifBlank {
                        "Mensagem enviada para a equipe."
                    },

            conversation =
                SupportReplyConversationState(
                    uuid =
                        conversationJson.optString(
                            "uuid"
                        )
                            .trim(),

                    status =
                        choice(
                            conversationJson.optJSONObject(
                                "status"
                            )
                        ),

                    canReply =
                        conversationJson.optBoolean(
                            "can_reply",
                            true
                        ),

                    lastMessageAt =
                        conversationJson.optNullableString(
                            "last_message_at"
                        )
                ),

            supportMessage =
                message(
                    messageJson
                )
        )
    }

    private fun choices(
        array: JSONArray?
    ): List<SupportChoice> {

        if (
            array == null
        ) {
            return emptyList()
        }

        return buildList {
            for (
            index in
            0 until array.length()
            ) {
                val item =
                    array.optJSONObject(
                        index
                    )
                        ?: continue

                add(
                    choice(
                        item
                    )
                )
            }
        }
    }

    private fun conversations(
        array: JSONArray?
    ): List<SupportConversationSummary> {

        if (
            array == null
        ) {
            return emptyList()
        }

        return buildList {
            for (
            index in
            0 until array.length()
            ) {
                val item =
                    array.optJSONObject(
                        index
                    )
                        ?: continue

                add(
                    SupportConversationSummary(
                        uuid =
                            item.optString(
                                "uuid"
                            )
                                .trim(),

                        subject =
                            item.optString(
                                "subject"
                            )
                                .trim(),

                        category =
                            choice(
                                item.optJSONObject(
                                    "category"
                                )
                            ),

                        status =
                            choice(
                                item.optJSONObject(
                                    "status"
                                )
                            ),

                        hasStaffReply =
                            item.optBoolean(
                                "has_staff_reply",
                                false
                            ),

                        isNew =
                            item.optBoolean(
                                "is_new",
                                false
                            ),

                        createdAt =
                            item.optNullableString(
                                "created_at"
                            ),

                        lastMessageAt =
                            item.optNullableString(
                                "last_message_at"
                            )
                    )
                )
            }
        }
    }

    private fun messages(
        array: JSONArray?
    ): List<SupportMessage> {

        if (
            array == null
        ) {
            return emptyList()
        }

        return buildList {
            for (
            index in
            0 until array.length()
            ) {
                val item =
                    array.optJSONObject(
                        index
                    )
                        ?: continue

                add(
                    message(
                        item
                    )
                )
            }
        }
    }

    private fun message(
        json: JSONObject
    ): SupportMessage {

        return SupportMessage(
            uuid =
                json.optString(
                    "uuid"
                )
                    .trim(),

            senderType =
                json.optString(
                    "sender_type"
                )
                    .trim()
                    .lowercase(),

            senderLabel =
                json.optString(
                    "sender_label"
                )
                    .trim(),

            body =
                json.optString(
                    "body"
                ),

            moderationStatus =
                json.optString(
                    "moderation_status"
                )
                    .trim()
                    .lowercase(),

            moderationLabel =
                json.optString(
                    "moderation_label"
                )
                    .trim(),

            createdAt =
                json.optNullableString(
                    "created_at"
                )
        )
    }

    private fun choice(
        json: JSONObject?
    ): SupportChoice {

        return SupportChoice(
            code =
                json
                    ?.optString(
                        "code"
                    )
                    ?.trim()
                    .orEmpty(),

            label =
                json
                    ?.optString(
                        "label"
                    )
                    ?.trim()
                    .orEmpty()
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
}