package com.example.chatapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.AppRoutes
import com.example.chatapp.CommonUiEvent
import com.example.chatapp.data.repository.AuthRepository
import com.example.chatapp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.internal.ChannelFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<CommonUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()


    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {




            chatRepository.observeActiveChats { result ->

                viewModelScope.launch {

                    if (result.isSuccess) {

                        val chats = result.getOrNull() ?: emptyList()


                    } else {


                        _uiEvent.send(
                            CommonUiEvent.ShowError(
                                result.exceptionOrNull()?.message
                                    ?: "Failed to load chats"
                            )
                        )
                    }
                }
            }
        }
    }




    fun onEvent(event: HomeScreenEvent) {

        when (event) {


            is HomeScreenEvent.NavigateToChat -> {

                viewModelScope.launch {
                    _uiEvent.send(CommonUiEvent.ShowLoader)

                    _uiEvent.send(
                        CommonUiEvent.Navigate(
                            AppRoutes.ChatRoute(
                            chatId = event.chatId,
                            userId = event.userId,
                            userName = event.userName,
                            profileImageUrl = event.profileImageUrl)
                        )
                    )

                }
            }

            HomeScreenEvent.NavigateToNewChat -> {

//               NavigateToNewChat viewModelScope.launch {
//                    _uiEvent.send(
//
//                    )
//                }
            }

            HomeScreenEvent.LogoutClicked -> {
                viewModelScope.launch {
                    _uiEvent.send(CommonUiEvent.ShowLoader)

                    authRepository.logout()

                    _uiEvent.send(
                        CommonUiEvent.Navigate(AppRoutes.LoginRoute)
                    )
                }
            }

            else -> Unit
        }
    }
}
