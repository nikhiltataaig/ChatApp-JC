package com.example.chatapp.ui.home


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatapp.ui.components.ChatListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeScreenUiEvent) -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Chats")
                },

                actions = {

                    IconButton(
                        onClick = {
                            onEvent(
                                HomeScreenUiEvent.LogoutClicked
                            )
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Logout,
                            contentDescription =
                                "Logout"
                        )
                    }
                }
            )
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = {

                    onEvent(
                        HomeScreenUiEvent.NavigateToNewChat
                    )
                }
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Add,
                    contentDescription =
                        "New chat"
                )
            }
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when {

                uiState.isLoading -> {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            )
                    )
                }

                uiState.chats.isEmpty() -> {

                    EmptyChats()
                }

                else -> {

                    LazyColumn(
                        modifier =
                            Modifier.fillMaxSize()
                    ) {

                        items(
                            items = uiState.chats,
                            key = {
                                it.chatId
                            }
                        ) { chat ->

                            ChatListItem(
                                 chat = chat,
                                onClick = {

                                    onEvent(
                                        HomeScreenUiEvent.NavigateToChat(
                                            chat.chatId,
                                         chat.user.uid ,
                                         chat.lastMessage,
                                         chat.lastMessageTime.toString()
                                    )
                                        )

                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChats() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "No active chats"
        )

        Text(
            text =
                "Start a conversation with someone."
        )
    }
}