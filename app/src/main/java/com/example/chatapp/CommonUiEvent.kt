package com.example.chatapp

import android.content.Context
import kotlinx.serialization.Serializable

sealed interface CommonUiEvent {
    data class ShowToast(val msg: String): CommonUiEvent
    data class Navigate(val route : AppRoutes): CommonUiEvent //this is extending the appUiEvent class
    data object PopBackStack: CommonUiEvent
    data class ExecuteWithContext(val callback:(context:Context) -> Unit): CommonUiEvent
    data class ShowError(val value: String): CommonUiEvent

    data object ShowLoader: CommonUiEvent

    data object DoNothing: CommonUiEvent
}


