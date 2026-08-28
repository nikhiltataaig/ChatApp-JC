package com.example.chatapp.data.models

data class ChatListItem(
    val chatId: String = "",
    val user: User = User(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L
)
