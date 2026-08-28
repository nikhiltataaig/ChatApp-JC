package com.example.chatapp.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.models.Message
import com.example.chatapp.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(ChatUiState())

    val uiState =
        _uiState.asStateFlow()


    private val _uiEvent =
        MutableSharedFlow<ChatUiEvent>()

    val uiEvent =
        _uiEvent.asSharedFlow()


    private var chatId: String? = null
    private var receiverId: String? = null


    fun initialize(
        chatId: String,
        receiverId: String
    ) {

        if (this.chatId == chatId) {
            return
        }

        this.chatId = chatId
        this.receiverId = receiverId

        observeMessages(chatId)
    }


    private fun observeMessages(
        chatId: String
    ) {

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        messageRepository.observeMessages(
            chatId = chatId
        ) { result ->

            result
                .onSuccess { messages ->

                    _uiState.update {

                        it.copy(
                            isLoading = false,
                            messages = messages,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { exception ->

                    _uiState.update {

                        it.copy(
                            isLoading = false,
                            errorMessage =
                                exception.message
                                    ?: "Unable to load messages."
                        )
                    }
                }
        }
    }


    fun onEvent(
        event: ChatUiEvent
    ) {

        when (event) {

            is ChatUiEvent.MessageTextChanged -> {

                _uiState.update {

                    it.copy(
                        messageText = event.text
                    )
                }
            }


            ChatUiEvent.SendMessageClicked -> {

                sendTextMessage()
            }


            is ChatUiEvent.ImageSelected -> {

                sendMediaMessage(
                    uri = event.uri,
                    mediaType = Message.MESSAGE_TYPE_IMAGE
                )
            }


            is ChatUiEvent.VideoSelected -> {

                sendMediaMessage(
                    uri = event.uri,
                    mediaType = Message.MESSAGE_TYPE_VIDEO
                )
            }


            is ChatUiEvent.MessageClicked -> {

                handleMessageClick(
                    event.message
                )
            }


            ChatUiEvent.BackClicked -> {

                emitUiEvent(
                    ChatUiEvent.BackClicked
                )
            }
        }
    }


    private fun sendTextMessage() {

        val text =
            _uiState.value.messageText
                .trim()

        if (text.isBlank()) {
            return
        }


        val currentChatId =
            chatId ?: return

        val currentReceiverId =
            receiverId ?: return


        viewModelScope.launch {

            _uiState.update {

                it.copy(
                    isSending = true,
                    errorMessage = null
                )
            }


            val result =
                messageRepository.sendTextMessage(
                    chatId = currentChatId,
                    receiverId = currentReceiverId,
                    text = text
                )


            if (result.isSuccess) {

                _uiState.update {

                    it.copy(
                        messageText = "",
                        isSending = false
                    )
                }

            } else {

                _uiState.update {

                    it.copy(
                        isSending = false,
                        errorMessage =
                            result.exceptionOrNull()
                                ?.message
                                ?: "Unable to send message."
                    )
                }


            }
        }
    }


    private fun sendMediaMessage(
        uri: Uri,
        mediaType: String
    ) {

        val currentChatId =
            chatId ?: return

        val currentReceiverId =
            receiverId ?: return


        viewModelScope.launch {

            _uiState.update {

                it.copy(
                    isSending = true,
                    errorMessage = null
                )
            }


            val result =
                messageRepository.sendMediaMessage(
                    chatId = currentChatId,
                    receiverId = currentReceiverId,
                    mediaUri = uri,
                    mediaType = mediaType
                )


            if (result.isSuccess) {

                _uiState.update {

                    it.copy(
                        isSending = false
                    )
                }

            } else {

                _uiState.update {

                    it.copy(
                        isSending = false,
                        errorMessage =
                            result.exceptionOrNull()
                                ?.message
                                ?: "Unable to send media."
                    )
                }


            }
        }
    }


    private fun handleMessageClick(
        message: Message
    ) {

        if (
            message.type ==
            Message.MESSAGE_TYPE_IMAGE ||
            message.type ==
            Message.MESSAGE_TYPE_VIDEO
        ) {

//            emitUiEvent(
//                ChatUiEvent.OpenMedia(
//                    url = message.mediaUrl,
//                    type = message.type
//                )
//            )
        }
    }


    private fun emitUiEvent(
        event: ChatUiEvent
    ) {

        viewModelScope.launch {

            _uiEvent.emit(event)
        }
    }
}
