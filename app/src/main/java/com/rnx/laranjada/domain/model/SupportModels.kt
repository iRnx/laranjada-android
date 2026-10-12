package com.rnx.laranjada.domain.model

data class SupportChoice(
    val code: String,
    val label: String
)

data class SupportConstraints(
    val subjectMaxLength: Int,
    val messageMaxLength: Int,
    val initialMessageMinLength: Int,
    val replyMinLength: Int,
    val releaseYearMin: Int,
    val releaseYearMax: Int
)

data class SupportCreationConfig(
    val canRequestContent: Boolean,
    val categories: List<SupportChoice>,
    val contentTypes: List<SupportChoice>,
    val constraints: SupportConstraints
)

data class SupportPagination(
    val page: Int,
    val pages: Int,
    val count: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean
)

data class SupportConversationSummary(
    val uuid: String,
    val subject: String,
    val category: SupportChoice,
    val status: SupportChoice,
    val hasStaffReply: Boolean,
    val isNew: Boolean,
    val createdAt: String?,
    val lastMessageAt: String?
)

data class SupportCenterPage(
    val creation: SupportCreationConfig,
    val pagination: SupportPagination,
    val conversations: List<SupportConversationSummary>
)

data class SupportConversationInfo(
    val uuid: String,
    val subject: String,
    val category: SupportChoice,
    val status: SupportChoice,
    val canReply: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
    val lastMessageAt: String?,
    val closedAt: String?
)

data class SupportContentRequest(
    val contentType: SupportChoice,
    val title: String,
    val releaseYear: Int,
    val decision: SupportChoice
)

data class SupportMessage(
    val uuid: String,
    val senderType: String,
    val senderLabel: String,
    val body: String,
    val moderationStatus: String,
    val moderationLabel: String,
    val createdAt: String?
)

data class SupportConversationDetail(
    val conversation: SupportConversationInfo,
    val contentRequest: SupportContentRequest?,
    val messages: List<SupportMessage>
)

data class SupportCreateRequest(
    val category: String,
    val subject: String,
    val contentType: String,
    val contentTitle: String,
    val releaseYear: Int?,
    val message: String
)

data class SupportCreatedConversation(
    val uuid: String,
    val subject: String,
    val category: SupportChoice,
    val status: SupportChoice,
    val canReply: Boolean
)

data class SupportCreateResult(
    val message: String,
    val conversation: SupportCreatedConversation
)

data class SupportReplyConversationState(
    val uuid: String,
    val status: SupportChoice,
    val canReply: Boolean,
    val lastMessageAt: String?
)

data class SupportReplyResult(
    val message: String,
    val conversation: SupportReplyConversationState,
    val supportMessage: SupportMessage
)