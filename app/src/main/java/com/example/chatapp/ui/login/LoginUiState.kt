package com.example.chatapp.ui.login


data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null
)
