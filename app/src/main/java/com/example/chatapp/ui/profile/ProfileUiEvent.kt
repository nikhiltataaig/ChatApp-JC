package com.example.chatapp.ui.profile

import android.net.Uri

sealed interface ProfileUiEvent {

    data class NameChanged(
        val name: String
    ) : ProfileUiEvent

    data class ImageSelected(
        val uri: Uri
    ) : ProfileUiEvent

    data object SaveProfileClicked : ProfileUiEvent

    data object ClearError : ProfileUiEvent

    data object ProfileCreated : ProfileUiEvent


}
