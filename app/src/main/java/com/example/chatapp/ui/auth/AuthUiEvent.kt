package com.example.chatapp.ui.auth


sealed interface AuthEvent {

    data class EmailChanged(
        val email: String
    ) : AuthEvent

    data class PasswordChanged(
        val password: String
    ) : AuthEvent

    data class ConfirmPasswordChanged(
        val password: String
    ) : AuthEvent

    data object LoginClicked : AuthEvent

    data object SignupClicked : AuthEvent

    data object LogoutClicked : AuthEvent

    data object ClearError : AuthEvent
}