package com.example.chatapp.data.repository

import android.net.Uri
import com.example.chatapp.data.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val storage: FirebaseStorage
) {

    fun observeMessages(
        chatId: String,
        onResult: (Result<List<Message>>) -> Unit
    ): ListenerRegistration {

        return firestore
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    onResult(Result.success(emptyList()))
                    return@addSnapshotListener
                }

                val messages =
                    snapshot.documents.mapNotNull {
                        it.toObject(Message::class.java)
                    }

                onResult(Result.success(messages))
            }
    }

    suspend fun sendTextMessage(
        chatId: String,
        receiverId: String,
        text: String
    ): Result<Unit> {

        return try {

            val currentUser =
                firebaseAuth.currentUser
                    ?: throw Exception(
                        "User session expired."
                    )

            val messageReference =
                firestore
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document()

            val message = Message(
                messageId = messageReference.id,
                senderId = currentUser.uid,
                receiverId = receiverId,
                text = text,
                type = Message.MESSAGE_TYPE_TEXT,
                mediaUrl = "",
                timestamp = System.currentTimeMillis()
            )

            messageReference
                .set(message)
                .await()

            updateLastMessage(
                chatId = chatId,
                text = text,
                senderId = currentUser.uid
            )

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun sendMediaMessage(
        chatId: String,
        receiverId: String,
        mediaUri: Uri,
        mediaType: String
    ): Result<Unit> {

        return try {

            val currentUser =
                firebaseAuth.currentUser
                    ?: throw Exception(
                        "User session expired."
                    )

            val fileExtension =
                getFileExtension(mediaUri)

            val fileName =
                "${System.currentTimeMillis()}.$fileExtension"

            val storageReference =
                storage
                    .reference
                    .child("chat_media")
                    .child(chatId)
                    .child(fileName)

            storageReference
                .putFile(mediaUri)
                .await()

            val downloadUrl =
                storageReference
                    .downloadUrl
                    .await()
                    .toString()

            val messageReference =
                firestore
                    .collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .document()

            val message = Message(
                messageId = messageReference.id,
                senderId = currentUser.uid,
                receiverId = receiverId,
                text = "",
                type = mediaType,
                mediaUrl = downloadUrl,
                timestamp = System.currentTimeMillis()
            )

            messageReference
                .set(message)
                .await()

            val lastMessage =
                when (mediaType) {

                    Message.MESSAGE_TYPE_IMAGE ->
                        "📷 Image"

                    Message.MESSAGE_TYPE_VIDEO ->
                        "🎥 Video"

                    else ->
                        "Media"
                }

            updateLastMessage(
                chatId = chatId,
                text = lastMessage,
                senderId = currentUser.uid
            )

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    private suspend fun updateLastMessage(
        chatId: String,
        text: String,
        senderId: String
    ) {

        firestore
            .collection("chats")
            .document(chatId)
            .update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTime" to
                            System.currentTimeMillis(),
                    "lastMessageSenderId" to senderId
                )
            )
            .await()
    }

    private fun getFileExtension(
        uri: Uri
    ): String {

        return when {
            uri.toString().contains("video") ->
                "mp4"

            else ->
                "jpg"
        }
    }
}

