package com.example.chatapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.AppRoutes
import com.example.chatapp.CommonUiEvent
import com.example.chatapp.data.models.User
import com.example.chatapp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
        Channel<CommonUiEvent>()

    val uiEvents =
        _uiEvents.receiveAsFlow()


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

            else -> {}
        }
    }


    private fun createProfile() {

        val firebaseUser =
            firebaseAuth.currentUser

        if (firebaseUser == null) {
            viewModelScope.launch {

                _uiEvents.send(CommonUiEvent.ShowToast("User session expired. Please login again."))

            }
            return
        }


        viewModelScope.launch {

            _uiState.update {

                _uiEvents.send(CommonUiEvent.ShowLoader)
                it.copy(

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
                            errorMessage = null
                        )
                    }

                    _uiEvents.send(CommonUiEvent.Navigate(AppRoutes.HomeRoute))
                }
                .onFailure { exception ->


                    _uiState.update {

                        it.copy(
                            errorMessage =
                                exception.message
                                    ?: "Unable to save profile."
                        )
                    }
                }
        }
    }


}
