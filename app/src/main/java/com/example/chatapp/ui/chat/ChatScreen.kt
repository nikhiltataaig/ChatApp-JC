package com.example.chatapp.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.chatapp.ui.components.MessageInput
import com.example.chatapp.ui.components.MessageItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userName: String,
    profileImageUrl: String,
    uiState: ChatUiState,
    onEvent: (ChatUiEvent) -> Unit
) {

    val listState =
        rememberLazyListState()


    LaunchedEffect(
        uiState.messages.size
    ) {

        if (uiState.messages.isNotEmpty()) {

            listState.animateScrollToItem(
                uiState.messages.lastIndex
            )
        }
    }


    Scaffold(

        topBar = {

            TopAppBar(

                navigationIcon = {

                    IconButton(
                        onClick = {

                            onEvent(
                                ChatUiEvent.BackClicked
                            )
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,
                            contentDescription =
                                "Back"
                        )
                    }
                },

                title = {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        AsyncImage(
                            model =
                                profileImageUrl,
                            contentDescription =
                                "Profile picture",
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .clip(
                                        CircleShape
                                    ),
                            contentScale =
                                ContentScale.Crop
                        )

                        Spacer(
                            modifier =
                                Modifier.width(12.dp)
                        )

                        Text(
                            text = userName
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            LazyColumn(
                modifier =
                    Modifier.weight(1f),

                state =
                    listState,

                contentPadding =
                    PaddingValues(12.dp),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = uiState.messages,

                    key = {
                        it.messageId
                    }
                ) { message ->

                    MessageItem(
                        message = message,

                        onClick = {

                            onEvent(
                                ChatUiEvent.MessageClicked(
                                    message
                                )
                            )
                        }
                    )
                }
            }


            MessageInput(
                text =
                    uiState.messageText,

                onTextChanged = {

                    onEvent(
                        ChatUiEvent.MessageTextChanged(
                            it
                        )
                    )
                },

                onSend = {

                    onEvent(
                        ChatUiEvent.SendMessageClicked
                    )
                }
            )
        }
    }
}
