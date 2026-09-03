package com.example.chatapp.data.repository

import android.net.Uri
import com.example.chatapp.data.models.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserRepositoryUnitTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        firestore = mockk()
        storage = mockk()
        firebaseAuth = mockk()
        userRepository = UserRepository(firestore, storage, firebaseAuth)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @After
    fun tearDown() {
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    @Test
    fun uploadProfileImage_success_returnsDownloadUrl() = runTest {
        // Arrange
        val uid = "user123"
        val imageUri = mockk<Uri>()
        val storageRef = mockk<StorageReference>()
        val profileImageRef = mockk<StorageReference>()
        val uidRef = mockk<StorageReference>()
        val fileRef = mockk<StorageReference>()
        val uploadTask = mockk<UploadTask>()
        val downloadUrlTask = mockk<Task<Uri>>()
        val downloadUrl = mockk<Uri>()

        every { storage.reference } returns storageRef
        every { storageRef.child("profile_images") } returns profileImageRef
        every { profileImageRef.child(uid) } returns uidRef
        every { uidRef.child("profile.jpg") } returns fileRef
        every { fileRef.putFile(imageUri) } returns uploadTask
        every { fileRef.downloadUrl } returns downloadUrlTask
        every { downloadUrl.toString() } returns "https://example.com/profile.jpg"

        coEvery { uploadTask.await() } returns mockk()
        coEvery { downloadUrlTask.await() } returns downloadUrl

        // Act
        val result = userRepository.uploadProfileImage(uid, imageUri)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("https://example.com/profile.jpg", result.getOrNull())
    }

    @Test
    fun uploadProfileImage_failure_returnsError() = runTest {
        // Arrange
        val uid = "user123"
        val imageUri = mockk<Uri>()
        val storageRef = mockk<StorageReference>()
        val exception = Exception("Upload failed")

        every { storage.reference } returns storageRef
        every { storageRef.child(any()) } throws exception

        // Act
        val result = userRepository.uploadProfileImage(uid, imageUri)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun createUserProfile_success_returnsUnit() = runTest {
        // Arrange
        val user = User(uid = "user123", name = "Test User")
        val collectionRef = mockk<CollectionReference>()
        val documentRef = mockk<DocumentReference>()
        val task = mockk<Task<Void>>()

        every { firestore.collection("users") } returns collectionRef
        every { collectionRef.document(user.uid) } returns documentRef
        every { documentRef.set(user) } returns task
        coEvery { task.await() } returns mockk()

        // Act
        val result = userRepository.createUserProfile(user)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun createUserProfile_failure_returnsError() = runTest {
        // Arrange
        val user = User(uid = "user123", name = "Test User")
        val collectionRef = mockk<CollectionReference>()
        val exception = Exception("Firestore error")

        every { firestore.collection("users") } returns collectionRef
        every { collectionRef.document(any()) } throws exception

        // Act
        val result = userRepository.createUserProfile(user)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun getUsers_success_returnsFilteredList() = runTest {
        // Arrange
        val currentUserId = "me"
        val currentUser = mockk<FirebaseUser>()
        val collectionRef = mockk<CollectionReference>()
        val querySnapshotTask = mockk<Task<QuerySnapshot>>()
        val querySnapshot = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()
        val user1 = User(uid = "user1", name = "User 1")
        val user2 = User(uid = "me", name = "Me")

        every { firebaseAuth.currentUser } returns currentUser
        every { currentUser.uid } returns currentUserId
        every { firestore.collection("users") } returns collectionRef
        every { collectionRef.get() } returns querySnapshotTask
        coEvery { querySnapshotTask.await() } returns querySnapshot
        every { querySnapshot.documents } returns listOf(doc1, doc2)
        every { doc1.toObject(User::class.java) } returns user1
        every { doc2.toObject(User::class.java) } returns user2

        // Act
        val result = userRepository.getUsers()

        // Assert
        assertTrue(result.isSuccess)
        val users = result.getOrNull()
        assertEquals(1, users?.size)
        assertEquals("user1", users?.get(0)?.uid)
    }

    @Test
    fun getUsers_sessionExpired_returnsFailure() = runTest {
        // Arrange
        every { firebaseAuth.currentUser } returns null

        // Act
        val result = userRepository.getUsers()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User session expired.", result.exceptionOrNull()?.message)
    }

    @Test
    fun getUserProfile_success_returnsUser() = runTest {
        // Arrange
        val uid = "user123"
        val collectionRef = mockk<CollectionReference>()
        val documentRef = mockk<DocumentReference>()
        val task = mockk<Task<DocumentSnapshot>>()
        val snapshot = mockk<DocumentSnapshot>()
        val user = User(uid = uid, name = "Test User")

        every { firestore.collection("users") } returns collectionRef
        every { collectionRef.document(uid) } returns documentRef
        every { documentRef.get() } returns task
        coEvery { task.await() } returns snapshot
        every { snapshot.toObject(User::class.java) } returns user

        // Act
        val result = userRepository.getUserProfile(uid)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }

    @Test
    fun getUserProfile_notFound_returnsFailure() = runTest {
        // Arrange
        val uid = "user123"
        val collectionRef = mockk<CollectionReference>()
        val documentRef = mockk<DocumentReference>()
        val task = mockk<Task<DocumentSnapshot>>()
        val snapshot = mockk<DocumentSnapshot>()

        every { firestore.collection("users") } returns collectionRef
        every { collectionRef.document(uid) } returns documentRef
        every { documentRef.get() } returns task
        coEvery { task.await() } returns snapshot
        every { snapshot.toObject(User::class.java) } returns null

        // Act
        val result = userRepository.getUserProfile(uid)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("User profile not found.", result.exceptionOrNull()?.message)
    }
}
