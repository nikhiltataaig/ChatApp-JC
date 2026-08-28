package com.example.chatapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.models.User
import com.example.chatapp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(ProfileUiState())

    val uiState =
        _uiState.asStateFlow()


    private val _uiEvents =
        MutableSharedFlow<ProfileUiEvent>()

    val uiEvents =
        _uiEvents.asSharedFlow()


    fun onEvent(event: ProfileUiEvent) {

        when (event) {

            is ProfileUiEvent.NameChanged -> {

                _uiState.update {
                    it.copy(
                        name = event.name
                    )
                }
            }


            is ProfileUiEvent.ImageSelected -> {

                _uiState.update {
                    it.copy(
                        imageUri = event.uri
                    )
                }
            }


            ProfileUiEvent.SaveProfileClicked -> {

                createProfile()
            }


            ProfileUiEvent.ClearError -> {

                _uiState.update {
                    it.copy(
                        errorMessage = null
                    )
                }
            }


            ProfileUiEvent.ProfileCreated -> {
                // Output event.
                // Do not call this from the UI.
            }


            is ProfileUiEvent.ShowToast -> {
                // Output event.
                // Do not call this from the UI.
            }
        }
    }


    private fun createProfile() {

        val firebaseUser =
            firebaseAuth.currentUser

        if (firebaseUser == null) {

            emitUiEvent(
                ProfileUiEvent.ShowToast(
                    "User session expired. Please login again."
                )
            )

            return
        }


        viewModelScope.launch {

            _uiState.update {

                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }


            val uid =
                firebaseUser.uid

            val email =
                firebaseUser.email ?: ""


            var imageUrl = ""


            val imageUri =
                _uiState.value.imageUri


            if (imageUri != null) {

                val uploadResult =
                    userRepository.uploadProfileImage(
                        uid = uid,
                        imageUri = imageUri
                    )


                if (uploadResult.isFailure) {

                    _uiState.update {

                        it.copy(
                            isLoading = false,
                            errorMessage =
                                uploadResult
                                    .exceptionOrNull()
                                    ?.message
                                    ?: "Unable to upload profile image."
                        )
                    }

                    return@launch
                }


                imageUrl =
                    uploadResult.getOrNull() ?: ""
            }


            val user = User(
                uid = uid,
                name = _uiState.value.name.trim(),
                email = email,
                profileImageUrl = imageUrl,
                createdAt = System.currentTimeMillis()
            )


            val profileResult =
                userRepository.createUserProfile(user)


            profileResult
                .onSuccess {

                    _uiState.update {

                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }

                    emitUiEvent(
                        ProfileUiEvent.ProfileCreated
                    )
                }
                .onFailure { exception ->

                    _uiState.update {

                        it.copy(
                            isLoading = false,
                            errorMessage =
                                exception.message
                                    ?: "Unable to save profile."
                        )
                    }
                }
        }
    }


    private fun emitUiEvent(
        event: ProfileUiEvent
    ) {

        viewModelScope.launch {

            _uiEvents.emit(event)
        }
    }
}
