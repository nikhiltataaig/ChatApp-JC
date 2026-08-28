package com.example.chatapp.ui.chat

import android.net.Uri
import com.example.chatapp.data.models.Message

sealed interface ChatUiEvent {

    data class MessageTextChanged(
        val text: String
    ) : ChatUiEvent


    data object SendMessageClicked : ChatUiEvent


    data class ImageSelected(
        val uri: Uri
    ) : ChatUiEvent


    data class VideoSelected(
        val uri: Uri
    ) : ChatUiEvent


    data class MessageClicked(
        val message: Message
    ) : ChatUiEvent


    data object BackClicked : ChatUiEvent
}
