package com.example.chatapp.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.AppRoutes
import com.example.chatapp.CommonUiEvent
import com.example.chatapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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


    private val _uiEvent = Channel<CommonUiEvent>()

    val uiEvent = _uiEvent.receiveAsFlow()


    fun onEvent(event: AuthEvent) {

        when (event) {

            is AuthEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(email = event.email, errorMessage = null)
            }

            is AuthEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = event.password, errorMessage = null)
            }

            is AuthEvent.ConfirmPasswordChanged -> {
                _uiState.value = _uiState.value.copy(confirmPassword = event.password, errorMessage = null)
            }

            AuthEvent.LoginClicked -> {
                viewModelScope.launch {
                    _uiEvent.send(
                        CommonUiEvent.Navigate(AppRoutes.LoginRoute)
                    )
                }
            }

            AuthEvent.SignupClicked -> {
                signup()
            }



            AuthEvent.ClearError -> {

                _uiState.value =
                    _uiState.value.copy(
                        errorMessage = null
                    )
            }
        }
    }


    private fun signup() {
        val email = _uiState.value.email
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

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

            _uiEvent.send(CommonUiEvent.ShowLoader)

            _uiState.value =
                _uiState.value.copy(

                    errorMessage = null
                )

            val result =
                authRepository.signup(
                    email = email.trim(),
                    password = password
                )

            if (result.isSuccess) {
               // Log.d("AuthViewModel","result success called => $result")
                _uiEvent.send(CommonUiEvent.Navigate(AppRoutes.SetupProfileRoute))
            } else {
                _uiEvent.send(CommonUiEvent.DoNothing)
              //  Log.d("AuthViewModel","result error called => $result")
                showError(
                    result.exceptionOrNull()
                        ?.message
                        ?: "Signup failed."
                )
            }
        }
    }



    private fun showError(
        message: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                errorMessage = message
            )
    }
}
