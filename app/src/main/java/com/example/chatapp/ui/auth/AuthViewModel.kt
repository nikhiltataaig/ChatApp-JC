package com.example.chatapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(AuthUiState())

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    private var email = ""
    private var password = ""
    private var confirmPassword = ""

    fun onEvent(event: AuthEvent) {

        when (event) {

            is AuthEvent.EmailChanged -> {
                email = event.email
            }

            is AuthEvent.PasswordChanged -> {
                password = event.password
            }

            is AuthEvent.ConfirmPasswordChanged -> {
                confirmPassword = event.password
            }

            AuthEvent.LoginClicked -> {
                login()
            }

            AuthEvent.SignupClicked -> {
                signup()
            }

            AuthEvent.LogoutClicked -> {
                logout()
            }

            AuthEvent.ClearError -> {

                _uiState.value =
                    _uiState.value.copy(
                        errorMessage = null
                    )
            }
        }
    }

    private fun login() {

        if (email.isBlank()) {
            showError("Please enter your email.")
            return
        }

        if (password.isBlank()) {
            showError("Please enter your password.")
            return
        }

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

            val result =
                authRepository.login(
                    email = email.trim(),
                    password = password
                )

            if (result.isSuccess) {

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )

            } else {

                showError(
                    result.exceptionOrNull()
                        ?.message
                        ?: "Login failed."
                )
            }
        }
    }

    private fun signup() {

        if (email.isBlank()) {
            showError("Please enter your email.")
            return
        }

        if (password.isBlank()) {
            showError("Please enter your password.")
            return
        }

        if (password != confirmPassword) {
            showError("Passwords do not match.")
            return
        }

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

            val result =
                authRepository.signup(
                    email = email.trim(),
                    password = password
                )

            if (result.isSuccess) {

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )

            } else {

                showError(
                    result.exceptionOrNull()
                        ?.message
                        ?: "Signup failed."
                )
            }
        }
    }

    fun logout() {

        authRepository.logout()

        _uiState.value =
            AuthUiState()
    }

    private fun showError(
        message: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                errorMessage = message
            )
    }
}