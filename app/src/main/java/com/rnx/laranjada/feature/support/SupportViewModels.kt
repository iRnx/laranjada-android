package com.rnx.laranjada.feature.support

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnx.laranjada.data.remote.api.SupportApiException
import com.rnx.laranjada.data.repository.SupportRepositoryImpl
import com.rnx.laranjada.domain.model.SupportConversationDetail
import com.rnx.laranjada.domain.model.SupportConversationSummary
import com.rnx.laranjada.domain.model.SupportCreateRequest
import com.rnx.laranjada.domain.model.SupportCreationConfig
import com.rnx.laranjada.domain.model.SupportPagination
import com.rnx.laranjada.domain.repository.SupportRepository
import kotlinx.coroutines.launch

data class SupportListUiState(
    val creation: SupportCreationConfig? = null,
    val conversations: List<SupportConversationSummary> = emptyList(),
    val pagination: SupportPagination? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val loadErrorMessage: String? = null,
    val loadMoreErrorMessage: String? = null
)

class SupportListViewModel(
    private val repository:
    SupportRepository =
        SupportRepositoryImpl()
) : ViewModel() {

    var uiState by
    mutableStateOf(
        SupportListUiState()
    )
        private set

    private var started =
        false

    fun start(
        onSessionEnded: () -> Unit
    ) {
        if (
            started
        ) {
            return
        }

        started =
            true

        loadPage(
            page =
                1,

            append =
                false,

            onSessionEnded =
                onSessionEnded
        )
    }

    fun refresh(
        onSessionEnded: () -> Unit
    ) {
        if (
            uiState.isLoading ||
            uiState.isLoadingMore
        ) {
            return
        }

        loadPage(
            page =
                1,

            append =
                false,

            onSessionEnded =
                onSessionEnded
        )
    }

    fun retry(
        onSessionEnded: () -> Unit
    ) {
        loadPage(
            page =
                1,

            append =
                false,

            onSessionEnded =
                onSessionEnded
        )
    }

    fun loadMore(
        onSessionEnded: () -> Unit
    ) {
        val pagination =
            uiState.pagination
                ?: return

        if (
            !pagination.hasNext ||
            uiState.isLoading ||
            uiState.isLoadingMore
        ) {
            return
        }

        loadPage(
            page =
                pagination.page + 1,

            append =
                true,

            onSessionEnded =
                onSessionEnded
        )
    }

    private fun loadPage(
        page: Int,
        append: Boolean,
        onSessionEnded: () -> Unit
    ) {
        viewModelScope.launch {
            if (
                append
            ) {
                uiState =
                    uiState.copy(
                        isLoadingMore =
                            true,

                        loadMoreErrorMessage =
                            null
                    )
            } else {
                uiState =
                    uiState.copy(
                        isLoading =
                            true,

                        loadErrorMessage =
                            null
                    )
            }

            try {
                val result =
                    repository.getCenter(
                        page
                    )

                val conversations =
                    if (
                        append
                    ) {
                        (
                                uiState.conversations +
                                        result.conversations
                                )
                            .distinctBy {
                                it.uuid
                            }
                    } else {
                        result.conversations
                    }

                uiState =
                    uiState.copy(
                        creation =
                            result.creation,

                        conversations =
                            conversations,

                        pagination =
                            result.pagination,

                        isLoading =
                            false,

                        isLoadingMore =
                            false,

                        loadErrorMessage =
                            null,

                        loadMoreErrorMessage =
                            null
                    )

            } catch (
                exception:
                Exception
            ) {
                if (
                    handleSessionEnded(
                        exception =
                            exception,

                        onSessionEnded =
                            onSessionEnded
                    )
                ) {
                    return@launch
                }

                if (
                    append
                ) {
                    uiState =
                        uiState.copy(
                            isLoadingMore =
                                false,

                            loadMoreErrorMessage =
                                exception.message
                                    ?: "Não foi possível carregar mais atendimentos."
                        )
                } else {
                    uiState =
                        uiState.copy(
                            isLoading =
                                false,

                            loadErrorMessage =
                                exception.message
                                    ?: "Não foi possível carregar a Central de Atendimento."
                        )
                }
            }
        }
    }
}

data class SupportCreateUiState(
    val creation: SupportCreationConfig? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val loadErrorMessage: String? = null,
    val submitErrorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

class SupportCreateViewModel(
    private val repository:
    SupportRepository =
        SupportRepositoryImpl()
) : ViewModel() {

    var uiState by
    mutableStateOf(
        SupportCreateUiState()
    )
        private set

    private var started =
        false

    fun start(
        onSessionEnded: () -> Unit
    ) {
        if (
            started
        ) {
            return
        }

        started =
            true

        loadCreation(
            onSessionEnded =
                onSessionEnded
        )
    }

    fun retry(
        onSessionEnded: () -> Unit
    ) {
        loadCreation(
            onSessionEnded =
                onSessionEnded
        )
    }

    fun clearSubmitErrors() {
        if (
            uiState.submitErrorMessage !=
            null ||
            uiState.fieldErrors
                .isNotEmpty()
        ) {
            uiState =
                uiState.copy(
                    submitErrorMessage =
                        null,

                    fieldErrors =
                        emptyMap()
                )
        }
    }

    fun createConversation(
        request: SupportCreateRequest,
        onSuccess: (String) -> Unit,
        onSessionEnded: () -> Unit
    ) {
        if (
            uiState.isSubmitting
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isSubmitting =
                        true,

                    submitErrorMessage =
                        null,

                    fieldErrors =
                        emptyMap()
                )

            try {
                val result =
                    repository.createConversation(
                        request
                    )

                uiState =
                    uiState.copy(
                        isSubmitting =
                            false,

                        submitErrorMessage =
                            null,

                        fieldErrors =
                            emptyMap()
                    )

                onSuccess(
                    result.conversation.uuid
                )

            } catch (
                exception:
                Exception
            ) {
                if (
                    handleSessionEnded(
                        exception =
                            exception,

                        onSessionEnded =
                            onSessionEnded
                    )
                ) {
                    return@launch
                }

                val apiException =
                    exception as?
                            SupportApiException

                uiState =
                    uiState.copy(
                        isSubmitting =
                            false,

                        submitErrorMessage =
                            exception.message
                                ?: "Não foi possível enviar o atendimento.",

                        fieldErrors =
                            apiException
                                ?.fieldErrors
                                .orEmpty()
                    )
            }
        }
    }

    private fun loadCreation(
        onSessionEnded: () -> Unit
    ) {
        if (
            uiState.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isLoading =
                        true,

                    loadErrorMessage =
                        null
                )

            try {
                val result =
                    repository.getCenter(
                        page =
                            1
                    )

                uiState =
                    uiState.copy(
                        creation =
                            result.creation,

                        isLoading =
                            false,

                        loadErrorMessage =
                            null
                    )

            } catch (
                exception:
                Exception
            ) {
                if (
                    handleSessionEnded(
                        exception =
                            exception,

                        onSessionEnded =
                            onSessionEnded
                    )
                ) {
                    return@launch
                }

                uiState =
                    uiState.copy(
                        isLoading =
                            false,

                        loadErrorMessage =
                            exception.message
                                ?: "Não foi possível preparar um novo atendimento."
                    )
            }
        }
    }
}

data class SupportConversationUiState(
    val detail: SupportConversationDetail? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSending: Boolean = false,
    val loadErrorMessage: String? = null,
    val sendErrorMessage: String? = null
)

class SupportConversationViewModel(
    private val repository:
    SupportRepository =
        SupportRepositoryImpl()
) : ViewModel() {

    var uiState by
    mutableStateOf(
        SupportConversationUiState()
    )
        private set

    private var startedUuid:
            String? =
        null

    fun start(
        conversationUuid: String,
        onSessionEnded: () -> Unit
    ) {
        if (
            startedUuid ==
            conversationUuid
        ) {
            return
        }

        startedUuid =
            conversationUuid

        loadConversation(
            conversationUuid =
                conversationUuid,

            showLoading =
                true,

            onSessionEnded =
                onSessionEnded
        )
    }

    fun retry(
        conversationUuid: String,
        onSessionEnded: () -> Unit
    ) {
        loadConversation(
            conversationUuid =
                conversationUuid,

            showLoading =
                true,

            onSessionEnded =
                onSessionEnded
        )
    }

    fun refresh(
        conversationUuid: String,
        onSessionEnded: () -> Unit
    ) {
        if (
            uiState.isLoading ||
            uiState.isRefreshing ||
            uiState.isSending
        ) {
            return
        }

        loadConversation(
            conversationUuid =
                conversationUuid,

            showLoading =
                false,

            onSessionEnded =
                onSessionEnded
        )
    }

    fun clearSendError() {
        if (
            uiState.sendErrorMessage !=
            null
        ) {
            uiState =
                uiState.copy(
                    sendErrorMessage =
                        null
                )
        }
    }

    fun sendMessage(
        conversationUuid: String,
        body: String,
        onSuccess: () -> Unit,
        onSessionEnded: () -> Unit
    ) {
        if (
            uiState.isSending
        ) {
            return
        }

        viewModelScope.launch {
            uiState =
                uiState.copy(
                    isSending =
                        true,

                    sendErrorMessage =
                        null
                )

            try {
                val result =
                    repository.sendMessage(
                        conversationUuid =
                            conversationUuid,

                        body =
                            body
                    )

                val currentDetail =
                    uiState.detail

                val updatedDetail =
                    if (
                        currentDetail !=
                        null
                    ) {
                        currentDetail.copy(
                            conversation =
                                currentDetail
                                    .conversation
                                    .copy(
                                        status =
                                            result
                                                .conversation
                                                .status,

                                        canReply =
                                            result
                                                .conversation
                                                .canReply,

                                        lastMessageAt =
                                            result
                                                .conversation
                                                .lastMessageAt
                                    ),

                            messages =
                                currentDetail.messages +
                                        result.supportMessage
                        )
                    } else {
                        null
                    }

                uiState =
                    uiState.copy(
                        detail =
                            updatedDetail,

                        isSending =
                            false,

                        sendErrorMessage =
                            null
                    )

                onSuccess()

            } catch (
                exception:
                Exception
            ) {
                if (
                    handleSessionEnded(
                        exception =
                            exception,

                        onSessionEnded =
                            onSessionEnded
                    )
                ) {
                    return@launch
                }

                val apiException =
                    exception as?
                            SupportApiException

                if (
                    apiException?.code ==
                    "conversation_closed"
                ) {
                    val currentDetail =
                        uiState.detail

                    if (
                        currentDetail !=
                        null
                    ) {
                        uiState =
                            uiState.copy(
                                detail =
                                    currentDetail.copy(
                                        conversation =
                                            currentDetail
                                                .conversation
                                                .copy(
                                                    canReply =
                                                        false
                                                )
                                    )
                            )
                    }
                }

                uiState =
                    uiState.copy(
                        isSending =
                            false,

                        sendErrorMessage =
                            exception.message
                                ?: "Não foi possível enviar a mensagem."
                    )
            }
        }
    }

    private fun loadConversation(
        conversationUuid: String,
        showLoading: Boolean,
        onSessionEnded: () -> Unit
    ) {
        viewModelScope.launch {
            uiState =
                if (
                    showLoading
                ) {
                    uiState.copy(
                        isLoading =
                            true,

                        loadErrorMessage =
                            null
                    )
                } else {
                    uiState.copy(
                        isRefreshing =
                            true
                    )
                }

            try {
                val result =
                    repository.getConversation(
                        conversationUuid
                    )

                uiState =
                    uiState.copy(
                        detail =
                            result,

                        isLoading =
                            false,

                        isRefreshing =
                            false,

                        loadErrorMessage =
                            null
                    )

            } catch (
                exception:
                Exception
            ) {
                if (
                    handleSessionEnded(
                        exception =
                            exception,

                        onSessionEnded =
                            onSessionEnded
                    )
                ) {
                    return@launch
                }

                if (
                    showLoading ||
                    uiState.detail ==
                    null
                ) {
                    uiState =
                        uiState.copy(
                            isLoading =
                                false,

                            isRefreshing =
                                false,

                            loadErrorMessage =
                                exception.message
                                    ?: "Não foi possível carregar o atendimento."
                        )
                } else {
                    uiState =
                        uiState.copy(
                            isRefreshing =
                                false
                        )
                }
            }
        }
    }
}

private fun handleSessionEnded(
    exception: Exception,
    onSessionEnded: () -> Unit
): Boolean {

    val sessionEnded =
        exception is
                SupportApiException &&
                exception.statusCode ==
                401

    if (
        sessionEnded
    ) {
        onSessionEnded()

        return true
    }

    return false
}