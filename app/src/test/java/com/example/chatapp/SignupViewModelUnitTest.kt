package com.example.chatapp

import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.ui.auth.AuthEvent
import com.example.chatapp.ui.auth.AuthViewModel
import com.example.chatapp.ui.login.LoginScreenViewModel
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SignupViewModelUnitTest {

    private lateinit var viewModel : AuthViewModel
    private lateinit var authRepository : AuthRepository

    @Before
    fun setup(){
        authRepository = mockk()
        viewModel = AuthViewModel(authRepository)
    }


    @Test
    fun signup_email_changed(){

        viewModel.onEvent(AuthEvent.EmailChanged("abc@a.com"))
        val state = viewModel.uiState.value


        assertEquals("abc@a.com" ,state.email)

    }

    @Test
    fun signup_password_changed(){

        viewModel.onEvent(AuthEvent.PasswordChanged("123456"))
        val state = viewModel.uiState.value


        assertEquals("123456" ,state.password)
    }

    @Test
    fun signup_confirm_password_changed(){

        viewModel.onEvent(AuthEvent.ConfirmPasswordChanged("123456"))
        val state = viewModel.uiState.value


        assertEquals("123456" ,state.confirmPassword)
    }

    @Test
    fun signup_on_login_clicked() = runTest {
        viewModel.onEvent(AuthEvent.LoginClicked)

        val event = viewModel.uiEvent.first()

        assertEquals(
            CommonUiEvent.Navigate(AppRoutes.LoginRoute),
            event
        )
    }



    @Test
    fun signup_with_empty_credentials_shows_error() {
        viewModel.onEvent(AuthEvent.SignupClicked)

        val state = viewModel.uiState.value
        assertEquals(
            "Please enter your email.",
            state.errorMessage
        )
    }

    @Test
    fun signup_with_empty_password_shows_error(){
        viewModel.onEvent(AuthEvent.EmailChanged("abc@a.com"))
        viewModel.onEvent(AuthEvent.SignupClicked)

        val state = viewModel.uiState.value
        assertEquals(
            "Please enter your password.",
            state.errorMessage
        )


    }

    @Test
    fun signup_with_different_password_shows_error(){
        viewModel.onEvent(AuthEvent.EmailChanged("abc@a.com"))

        viewModel.onEvent(AuthEvent.PasswordChanged("abc@a.com"))
        viewModel.onEvent(AuthEvent.ConfirmPasswordChanged("aa.com"))

        viewModel.onEvent(AuthEvent.SignupClicked)

        val state = viewModel.uiState.value
        assertEquals(
            "Passwords do not match.",
            state.errorMessage
        )
    }

    @Test
    fun signup_success_navigates_to_setup() = runTest {
        // Arrange
        val email = "test@gmail.com"
        val password = "123456"

        coEvery {
            authRepository.signup(
                email = email.trim(),
                password = password
            )
        } returns Result.success(mockk<FirebaseUser>())

        viewModel.onEvent(AuthEvent.EmailChanged(email))
        viewModel.onEvent(AuthEvent.PasswordChanged(password))
        viewModel.onEvent(AuthEvent.ConfirmPasswordChanged(password))

        // Act
        viewModel.onEvent(AuthEvent.SignupClicked)

        // Assert
        val loaderEvent = viewModel.uiEvent.first()

        assertEquals(
            CommonUiEvent.ShowLoader,
            loaderEvent
        )

        val navigationEvent = viewModel.uiEvent.first()

        assertEquals(
            CommonUiEvent.Navigate(AppRoutes.SetupProfileRoute),
            navigationEvent
        )
    }


    @Test
    fun signup_failure_show_error() = runTest {
        val email = "test@gmail.com"
        val password = "123456"


        val exception = Exception("Unable to create account.")
        coEvery {
            authRepository.signup(
                email = email.trim(),
                password = password
            )
        } returns Result.failure(exception)

        viewModel.onEvent(AuthEvent.EmailChanged(email))
        viewModel.onEvent(AuthEvent.PasswordChanged(password))
        viewModel.onEvent(AuthEvent.ConfirmPasswordChanged(password))

        // Act
        viewModel.onEvent(AuthEvent.SignupClicked)

        // Assert
        val loaderEvent = viewModel.uiEvent.first()

        assertEquals(
            CommonUiEvent.ShowLoader,
            loaderEvent
        )

        val doNothingEvent = viewModel.uiEvent.first()

        assertEquals(
            CommonUiEvent.DoNothing,
            doNothingEvent
        )
    }
}