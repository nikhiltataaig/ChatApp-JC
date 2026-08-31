package com.example.chatapp.ui.home

import com.example.chatapp.data.models.ChatListItem


data class HomeUiState(
    val chats: List<ChatListItem> = emptyList()
)
