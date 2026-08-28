package com.example.chatapp.data.repository


import com.example.chatapp.data.models.Chat
import com.example.chatapp.data.models.ChatListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) {

    private val chatsCollection =
        firestore.collection("chats")

    fun observeActiveChats(
        onResult: (Result<List<ChatListItem>>) -> Unit
    ): ListenerRegistration? {

        val currentUser =
            firebaseAuth.currentUser
                ?: return null

        return chatsCollection
            .whereArrayContains(
                "participants",
                currentUser.uid
            )
            .addSnapshotListener { snapshot, error ->

                if (error != null) {

                    onResult(
                        Result.failure(error)
                    )

                    return@addSnapshotListener
                }

                if (snapshot == null) {

                    onResult(
                        Result.success(emptyList())
                    )

                    return@addSnapshotListener
                }

                val chats = snapshot.documents.mapNotNull {
                    it.toObject(Chat::class.java)
                }

                // User profile loading is handled below.
                loadChatUsers(
                    chats = chats,
                    currentUserId = currentUser.uid,
                    onResult = onResult
                )
            }
    }

    private fun loadChatUsers(
        chats: List<Chat>,
        currentUserId: String,
        onResult: (Result<List<ChatListItem>>) -> Unit
    ) {

        if (chats.isEmpty()) {

            onResult(
                Result.success(emptyList())
            )

            return
        }

        val chatItems = mutableListOf<ChatListItem>()

        var completedRequests = 0

        chats.forEach { chat ->

            val otherUserId =
                chat.participants.firstOrNull {
                    it != currentUserId
                }

            if (otherUserId == null) {

                completedRequests++

                if (completedRequests == chats.size) {
                    onResult(Result.success(chatItems))
                }

                return@forEach
            }

            firestore
                .collection("users")
                .document(otherUserId)
                .get()
                .addOnSuccessListener { document ->

                    val user =
                        document.toObject(
                            com.example.chatapp.data.models.User::class.java
                        )

                    if (user != null) {

                        chatItems.add(
                            ChatListItem(
                                chatId = chat.chatId,
                                user = user,
                                lastMessage = chat.lastMessage,
                                lastMessageTime =
                                    chat.lastMessageTime
                            )
                        )
                    }

                    completedRequests++

                    if (completedRequests == chats.size) {

                        onResult(
                            Result.success(
                                chatItems.sortedByDescending {
                                    it.lastMessageTime
                                }
                            )
                        )
                    }
                }
                .addOnFailureListener { exception ->

                    completedRequests++

                    if (completedRequests == chats.size) {

                        onResult(
                            Result.failure(exception)
                        )
                    }
                }
        }
    }

    suspend fun createChat(
        otherUserId: String
    ): Result<String> {

        return try {

            val currentUser =
                firebaseAuth.currentUser
                    ?: throw Exception(
                        "User is not logged in."
                    )

            val existingChat =
                chatsCollection
                    .whereArrayContains(
                        "participants",
                        currentUser.uid
                    )
                    .get()
                    .await()

            val existingDocument =
                existingChat.documents.firstOrNull { document ->

                    val participants =
                        document.get("participants")
                                as? List<*>

                    participants?.contains(
                        otherUserId
                    ) == true
                }

            if (existingDocument != null) {

                return Result.success(
                    existingDocument.id
                )
            }

            val chatReference =
                chatsCollection.document()

            val chat = Chat(
                chatId = chatReference.id,
                participants = listOf(
                    currentUser.uid,
                    otherUserId
                ),
                lastMessage = "",
                lastMessageTime = 0L,
                lastMessageSenderId = ""
            )

            chatReference
                .set(chat)
                .await()

            Result.success(chatReference.id)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun getOrCreateChat(
        otherUserId: String
    ): Result<String> {

        return try {

            val currentUserId =
                firebaseAuth.currentUser?.uid
                    ?: throw Exception(
                        "User session expired."
                    )

            val chatId =
                createChatId(
                    currentUserId,
                    otherUserId
                )

            val chatReference =
                firestore
                    .collection("chats")
                    .document(chatId)

            val snapshot =
                chatReference
                    .get()
                    .await()

            if (!snapshot.exists()) {

                val chat = mapOf(
                    "chatId" to chatId,
                    "participants" to listOf(
                        currentUserId,
                        otherUserId
                    ),
                    "lastMessage" to "",
                    "lastMessageTime" to 0L,
                    "lastMessageSenderId" to ""
                )

                chatReference
                    .set(chat)
                    .await()
            }

            Result.success(chatId)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}


private fun createChatId(
    user1: String,
    user2: String
): String {

    return listOf(
        user1,
        user2
    ).sorted()
        .joinToString("_")
}