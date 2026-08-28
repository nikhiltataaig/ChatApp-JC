package com.example.chatapp.ui.home



sealed interface HomeScreenUiEvent {

    data class NavigateToChat(
        val chatId: String,
        val userId: String,
        val userName: String,
        val profileImageUrl: String
    ) : HomeScreenUiEvent

    data object NavigateToNewChat : HomeScreenUiEvent

    data object NavigateToLogin : HomeScreenUiEvent

    data class ShowError(
        val message: String
    ) : HomeScreenUiEvent

    data object LogoutClicked : HomeScreenUiEvent
}