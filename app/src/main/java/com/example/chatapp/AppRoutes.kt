package com.example.chatapp

import kotlinx.serialization.Serializable

sealed interface AppRoutes {
    @Serializable
    data object LoginRoute : AppRoutes
    @Serializable
    data object SignupRoute: AppRoutes
    @Serializable
    data class ChatRoute(
        val chatId: String?=null,
        val userId: String?=null,
        val userName: String?=null,
        val profileImageUrl: String?=null
    ) : AppRoutes



    @Serializable
    data object SetupProfileRoute : AppRoutes

    @Serializable
    data object HomeRoute : AppRoutes

}