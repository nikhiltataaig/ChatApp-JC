package com.example.chatapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomeScreenUiEvent>()
    val uiEvent: SharedFlow<HomeScreenUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true
            )

            chatRepository.observeActiveChats { result ->

                viewModelScope.launch {

                    if (result.isSuccess) {

                        val chats = result.getOrNull() ?: emptyList()

                        _uiState.value = HomeUiState(
                            isLoading = false,
                            chats = chats
                        )

                    } else {

                        _uiState.value = _uiState.value.copy(
                            isLoading = false
                        )

                        _uiEvent.emit(
                            HomeScreenUiEvent.ShowError(
                                result.exceptionOrNull()?.message
                                    ?: "Failed to load chats"
                            )
                        )
                    }
                }
            }
        }
    }




    fun onEvent(event: HomeScreenUiEvent) {

        when (event) {

            is HomeScreenUiEvent.NavigateToChat -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        HomeScreenUiEvent.NavigateToChat(
                            chatId = event.chatId,
                            userId = event.userId,
                            userName = event.userName,
                            profileImageUrl = event.profileImageUrl
                        )
                    )

                }
            }

            HomeScreenUiEvent.NavigateToNewChat -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        HomeScreenUiEvent.NavigateToNewChat
                    )
                }
            }

            HomeScreenUiEvent.LogoutClicked -> {
                viewModelScope.launch {

                    authRepository.logout()

                    _uiEvent.emit(
                        HomeScreenUiEvent.NavigateToLogin
                    )
                }
            }

            else -> Unit
        }
    }
}
