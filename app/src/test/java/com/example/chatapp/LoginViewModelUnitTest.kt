package com.example.chatapp

import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.ui.login.LoginScreenEvents
import com.example.chatapp.ui.login.LoginScreenViewModel
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import org.junit.Test
import kotlinx.coroutines.test.runTest


import org.junit.Assert.*
import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class LoginViewModelUnitTest {

    private lateinit var viewModel : LoginScreenViewModel
    private lateinit var authRepository : AuthRepository

    @Before
    fun setup(){
        authRepository = mockk()
        viewModel = LoginScreenViewModel(authRepository)
    }


    @Test
    fun login_email_changed(){

        viewModel.onEvent(LoginScreenEvents.onEmailChanged("abc@a.com"))
        val state = viewModel.uiState.value


        assertEquals("abc@a.com" ,state.email)

    }

    @Test
    fun login_password_changed(){

        viewModel.onEvent(LoginScreenEvents.onPasswordChanged("123456"))
        val state = viewModel.uiState.value


       assertEquals("123456" ,state.password)
    }
    @Test
    fun login_on_signup_clicked() = runTest {
        viewModel.onEvent(LoginScreenEvents.onSignupClicked)

        val event = viewModel.uiEvent.first()

        assertEquals(
            CommonUiEvent.Navigate(AppRoutes.SignupRoute),
            event
        )
    }



    @Test
    fun login_with_empty_credentials_shows_error() {
        viewModel.onEvent(LoginScreenEvents.onLoginClicked)

        val state = viewModel.uiState.value
        assertEquals(
            "Email and password cannot be empty",
            state.errorMessage
        )
    }

    @Test
    fun login_with_empty_password_shows_error(){
        viewModel.onEvent(LoginScreenEvents.onEmailChanged("abc@a.com"))
        viewModel.onEvent(LoginScreenEvents.onLoginClicked)

        val state = viewModel.uiState.value
        assertEquals(
            "Email and password cannot be empty",
            state.errorMessage
        )


    }

    @Test
    fun login_with_empty_email_shows_error(){
        viewModel.onEvent(LoginScreenEvents.onPasswordChanged("abc@a.com"))
        viewModel.onEvent(LoginScreenEvents.onLoginClicked)

        val state = viewModel.uiState.value
        assertEquals(
            "Email and password cannot be empty",
            state.errorMessage
        )
    }

    @Test
    fun login_success_navigates_to_home() = runTest {
        // Arrange
        val email = "test@gmail.com"
        val password = "123456"


        coEvery {
            authRepository.login(email, password)
        } returns Result.success(mockk<FirebaseUser>())

        // Set login state
        viewModel.onEvent(
            LoginScreenEvents.onEmailChanged(email)
        )

        viewModel.onEvent(
            LoginScreenEvents.onPasswordChanged(password)
        )

        // Act
        viewModel.onEvent(
            LoginScreenEvents.onLoginClicked
        )

        // Assert
        val loaderEvent = viewModel.uiEvent.first()

        assertEquals(
            CommonUiEvent.ShowLoader,
            loaderEvent
        )

        val navigationEvent = viewModel.uiEvent.first()

        assertEquals(
            CommonUiEvent.Navigate(AppRoutes.HomeRoute),
            navigationEvent
        )
    }


    @Test
    fun login_failure_show_toast() = runTest {
        // Arrange
        val email = "test@gmail.com"
        val password = "123456"

        val exception = Exception("Invalid credentials")

        coEvery {
            authRepository.login(email, password)
        } returns Result.failure(exception)

        // Set login state
        viewModel.onEvent(
            LoginScreenEvents.onEmailChanged(email)
        )

        viewModel.onEvent(
            LoginScreenEvents.onPasswordChanged(password)
        )

        // Act
        viewModel.onEvent(
            LoginScreenEvents.onLoginClicked
        )

        // Assert
        val loaderEvent = viewModel.uiEvent.first()

        assertEquals(
            CommonUiEvent.ShowLoader,
            loaderEvent
        )

        val showToast = viewModel.uiEvent.first()

        assertEquals(
            CommonUiEvent.ShowToast("Login failed"),
            showToast
        )
    }
}

