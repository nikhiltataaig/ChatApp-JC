package com.example.chatapp

import android.content.Context

sealed interface CommonUiEvent {
    data class ShowToast(val msg: String): CommonUiEvent
    data class Navigate(val route : String): CommonUiEvent //this is extending the appUiEvent class
    data object PopBackStack: CommonUiEvent
    data class ExecuteWithContext(val callback:(context:Context) -> Unit): CommonUiEvent

}