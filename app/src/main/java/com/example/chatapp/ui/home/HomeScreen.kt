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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatapp.ui.components.ChatListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import com.example.chatapp.CommonUiEvent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onEvent: (HomeScreenEvent) -> Unit,
    navController: NavHostController,
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val showLoader = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {

        homeViewModel.uiEvent.flowWithLifecycle(
            lifecycleOwner.lifecycle,
            Lifecycle.State.STARTED
        ).collect { event ->
            showLoader.value = false
            when (event) {

                is CommonUiEvent.Navigate -> {
                    navController.navigate(
                        event.route
                    )
                }

                is CommonUiEvent.ShowLoader ->{
                    showLoader.value = true
                }

                is CommonUiEvent.PopBackStack -> {
                    navController.popBackStack()
                }

                HomeScreenEvent.NavigateToNewChat -> {
                    navController.navigate("new_chat")
                }

                HomeScreenEvent.NavigateToLogin -> {
                    navController.navigate("login")
                }

                else -> Unit
            }
        }
    }

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
                                HomeScreenEvent.LogoutClicked
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

//        floatingActionButton = {
//
//            FloatingActionButton(
//                onClick = {
//
//                    onEvent(
//                        HomeScreenUiEvent.NavigateToNewChat
//                    )
//                }
//            ) {
//
//                Icon(
//                    imageVector =
//                        Icons.Default.Add,
//                    contentDescription =
//                        "New chat"
//                )
//            }
//        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when {

                showLoader.value -> {

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
                                        HomeScreenEvent.NavigateToChat(
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