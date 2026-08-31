package com.example.chatapp.ui.chat

import com.example.chatapp.data.models.Message


data class ChatUiState(



    val isSending: Boolean = false,

    val messages: List<Message> = emptyList(),

    val messageText: String = "",

    val errorMessage: String? = null
)