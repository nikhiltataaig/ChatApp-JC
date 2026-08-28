package com.example.chatapp.data.models


data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val type: String = MESSAGE_TYPE_TEXT,
    val mediaUrl: String = "",
    val timestamp: Long = 0L
) {
    companion object {
        const val MESSAGE_TYPE_TEXT = "TEXT"
        const val MESSAGE_TYPE_IMAGE = "IMAGE"
        const val MESSAGE_TYPE_VIDEO = "VIDEO"
    }
}