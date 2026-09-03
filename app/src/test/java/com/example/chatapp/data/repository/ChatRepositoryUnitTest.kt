package com.example.chatapp.data.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ChatRepositoryUnitTest {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var repository: ChatRepository
    private lateinit var chatsCollection : CollectionReference


    @Before
    fun setup() {
        firestore = mockk()
        firebaseAuth = mockk()


        chatsCollection = mockk()
        every {
            firestore.collection("chats")
        } returns chatsCollection

        repository = ChatRepository(
            firestore = firestore,
            firebaseAuth = firebaseAuth,
        )
    }

    @Test
    fun createChat_when_user_not_logged_in_returns_failure() = runTest {

        // Arrange
        every {
            firebaseAuth.currentUser
        } returns null

        // Act



        every { firestore.collection("chats") } returns chatsCollection


        val result = repository.createChat("other-user-id")
        // Assert
        assertTrue(result.isFailure)

        assertEquals(
            "User is not logged in.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun getOrCreateChat_when_user_not_logged_in_returns_failure() = runTest {

        // Arrange
        every {
            firebaseAuth.currentUser
        } returns null

        // Act
        val result = repository.getOrCreateChat("other-user-id")

        // Assert
        assertTrue(result.isFailure)

        assertEquals(
            "User session expired.",
            result.exceptionOrNull()?.message
        )
    }


    @Test
    fun getOrCreateChat_when_chat_exists_returns_existing_chat_id() = runTest {

        // Arrange
        val currentUser = mockk<FirebaseUser>()
        val collection = mockk<CollectionReference>()
        val documentReference = mockk<DocumentReference>()
        val documentSnapshot = mockk<DocumentSnapshot>()

        every {
            firebaseAuth.currentUser
        } returns currentUser

        every {
            currentUser.uid
        } returns "user123"

        every {
            firestore.collection("chats")
        } returns collection

        every {
            collection.document("user123_user456")
        } returns documentReference

        every {
            documentSnapshot.exists()
        } returns true

        every {
            documentReference.get()
        } returns Tasks.forResult(documentSnapshot)

        // Act
        val result = repository.getOrCreateChat("user456")

        // Assert
        assertTrue(result.isSuccess)

        assertEquals(
            "user123_user456",
            result.getOrNull()
        )

        verify(exactly = 1) {
            documentReference.get()
        }

        verify(exactly = 0) {
            documentReference.set(any<Map<String, Any>>())
        }
    }

    @Test
    fun getOrCreateChat_when_chat_does_not_exist_creates_chat() = runTest {

        // Arrange
        val currentUser = mockk<FirebaseUser>()
        val collection = mockk<CollectionReference>()
        val documentReference = mockk<DocumentReference>()
        val getTask = mockk<Task<DocumentSnapshot>>()
        val setTask = mockk<Task<Void>>()
        val snapshot = mockk<DocumentSnapshot>()

        every {
            firebaseAuth.currentUser
        } returns currentUser

        every {
            currentUser.uid
        } returns "user123"

        every {
            firestore.collection("chats")
        } returns collection

        every {
            collection.document("user123_user456")
        } returns documentReference

        every {
            documentReference.get()
        } returns getTask

        every {
            documentReference.set(any<Map<String, Any>>())
        } returns setTask

        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        coEvery {
            getTask.await()
        } returns snapshot

        coEvery {
            setTask.await()
        } returns mockk()

        every {
            snapshot.exists()
        } returns false

        // Act
        val result = repository.getOrCreateChat("user456")

        // Assert
        assertTrue(result.isSuccess)

        assertEquals(
            "user123_user456",
            result.getOrNull()
        )

        verify {
            documentReference.set(any<Map<String, Any>>())
        }
    }


//
//    @Test
//    fun createChat_when_firestore_throws_returns_failure() = runTest {
//
//        // Arrange
//        val currentUser = mockk<FirebaseUser>()
//        val collection = mockk<CollectionReference>()
//        val query = mockk<Query>()
//
//        val exception = Exception("Firestore error")
//
//        every {
//            firebaseAuth.currentUser
//        } returns currentUser
//
//        every {
//            currentUser.uid
//        } returns "user123"
//
//        every {
//            firestore.collection("chats")
//        } returns collection
//
//        every {
//            collection.whereArrayContains(
//                "participants",
//                "user123"
//            )
//        } returns query
//
//        every {
//            query.get()
//        } returns Tasks.forException(exception)
//
//        // Act
//        val result = repository.createChat("user456")
//
//        // Assert
//        assertTrue(result.isFailure)
//
//        assertEquals(
//            exception,
//            result.exceptionOrNull()
//        )
//    }




}