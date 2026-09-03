package com.example.chatapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import io.mockk.mockk
import com.example.chatapp.data.repository.AuthRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthRepositoryUnitTest {


    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup(){
        firebaseAuth = mockk()
        authRepository = AuthRepository(firebaseAuth)
    }



    @Test
    fun signup_when_user_is_null_returns_failure() = runTest {
        // Arrange
        val email = "test@gmail.com"
        val password = "123456"

        val authResult = mockk<AuthResult>()
        val task = mockk<Task<AuthResult>>()

        every {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
        } returns task

        every {
            authResult.user
        } returns null

        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        coEvery {
            task.await()
        } returns authResult

        // Act
        val result = authRepository.signup(email, password)

        // Assert
        assertTrue(result.isFailure)

        assertEquals(
            "Unable to create account.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun signup_when_firebase_throws_returns_failure() = runTest {
        // Arrange
        val email = "test@gmail.com"
        val password = "123456"

        val task = mockk<Task<AuthResult>>()
        val exception = Exception("Email already in use")

        every {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
        } returns task

        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        coEvery {
            task.await()
        } throws exception

        // Act
        val result = authRepository.signup(email, password)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(
            exception,
            result.exceptionOrNull()
        )
    }


    @Test
    fun login_success_returns_user() = runTest {
        // Arrange
        val email = "test@gmail.com"
        val password = "123456"

        val firebaseUser = mockk<FirebaseUser>()
        val authResult = mockk<AuthResult>()
        val task = mockk<Task<AuthResult>>()

        every {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        } returns task

        every {
            authResult.user
        } returns firebaseUser

        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        coEvery {
            task.await()
        } returns authResult

        // Act
        val result = authRepository.login(email, password)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(firebaseUser, result.getOrNull())

        verify {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        }
    }


    @Test
    fun login_when_user_is_null_returns_failure() = runTest {
        // Arrange
        val email = "test@gmail.com"
        val password = "123456"

        val authResult = mockk<AuthResult>()
        val task = mockk<Task<AuthResult>>()

        every {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        } returns task

        every {
            authResult.user
        } returns null

        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        coEvery {
            task.await()
        } returns authResult

        // Act
        val result = authRepository.login(email, password)

        // Assert
        assertTrue(result.isFailure)

        assertEquals(
            "Unable to login. Please try again.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun login_when_firebase_throws_returns_failure() = runTest {
        // Arrange
        val email = "test@gmail.com"
        val password = "123456"

        val task = mockk<Task<AuthResult>>()
        val exception = Exception("Invalid credentials")

        every {
            firebaseAuth.signInWithEmailAndPassword(email, password)
        } returns task

        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        coEvery {
            task.await()
        } throws exception

        // Act
        val result = authRepository.login(email, password)

        // Assert
        assertTrue(result.isFailure)
        assertEquals(
            exception,
            result.exceptionOrNull()
        )
    }


    @Test
    fun logout_calls_firebase_signOut() {
        // Act
        every {
            firebaseAuth.signOut()
        } returns Unit

        authRepository.logout()

        // Assert
        verify {
            firebaseAuth.signOut()
        }
    }



}