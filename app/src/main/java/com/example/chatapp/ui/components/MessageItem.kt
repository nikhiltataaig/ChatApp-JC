package com.example.chatapp.ui.components



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.chatapp.data.models.ChatListItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import com.example.chatapp.data.models.Message
import com.google.firebase.auth.FirebaseAuth


@Composable
 fun MessageItem(
    message: Message,
    onClick: () -> Unit
) {

    val isMine =
        message.senderId ==
                FirebaseAuth
                    .getInstance()
                    .currentUser
                    ?.uid

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isMine) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
    ) {

        Surface(
            modifier =
                Modifier
                    .widthIn(
                        max = 280.dp
                    )
                    .clickable(
                        onClick = onClick
                    ),
            shape =
                RoundedCornerShape(16.dp)
        ) {

            Text(
                text = message.text,
                modifier =
                    Modifier.padding(12.dp)
            )
        }
    }
}