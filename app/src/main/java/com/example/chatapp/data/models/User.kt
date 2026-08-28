package com.example.chatapp.data.models


data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val createdAt: Long = 0L
)