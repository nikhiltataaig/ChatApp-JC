package com.example.chatapp.ui.profile

import android.net.Uri

data class ProfileUiState(
    val name: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
