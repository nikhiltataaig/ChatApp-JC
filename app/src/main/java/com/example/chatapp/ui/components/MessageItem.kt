package com.example.chatapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.chatapp.data.models.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageItem(
    message: Message,
    onClick: () -> Unit
) {

    val currentUserId =
        FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid

    val isMine =
        message.senderId == currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),

        horizontalArrangement =
            if (isMine) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
    ) {

        Surface(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clickable {
                    onClick()
                },

            shape =
                RoundedCornerShape(16.dp),

            color =
                if (isMine) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
        ) {

            Column(
                modifier = Modifier.padding(
                    if (
                        message.type ==
                        Message.MESSAGE_TYPE_IMAGE
                    ) {
                        4.dp
                    } else {
                        12.dp
                    }
                )
            ) {

                when (message.type) {

                    Message.MESSAGE_TYPE_IMAGE -> {

                        AsyncImage(
                            model =
                                ImageRequest.Builder(
                                    LocalContext.current
                                )
                                    .data(
                                        message.mediaUrl
                                    )
                                    .crossfade(true)
                                    .build(),

                            contentDescription =
                                "Sent image",

                            modifier = Modifier
                                .size(240.dp)
                                .clip(
                                    RoundedCornerShape(
                                        12.dp
                                    )
                                ),

                            contentScale =
                                ContentScale.Crop
                        )
                    }

                    else -> {

                        Text(
                            text = message.text,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge
                        )
                    }
                }

                /*
                 * Timestamp
                 */
                if (message.timestamp > 0L) {

                    Text(
                        text =
                            formatTime(
                                message.timestamp
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        modifier = Modifier
                            .align(
                                Alignment.End
                            )
                            .padding(
                                horizontal = 4.dp,
                                vertical = 2.dp
                            )
                    )
                }
            }
        }
    }
}

private fun formatTime(
    timestamp: Long
): String {

    return SimpleDateFormat(
        "hh:mm a",
        Locale.getDefault()
    ).format(
        Date(timestamp)
    )
}