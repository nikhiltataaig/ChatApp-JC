package com.example.chatapp.ui.home



sealed interface HomeScreenEvent {

    data class NavigateToChat(
        val chatId: String,
        val userId: String,
        val userName: String,
        val profileImageUrl: String
    ) : HomeScreenEvent

    data object NavigateToNewChat : HomeScreenEvent

    data object NavigateToLogin : HomeScreenEvent

    data class ShowError(
        val message: String
    ) : HomeScreenEvent

    data object LogoutClicked : HomeScreenEvent
}