package com.example.chatapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
 fun MessageInput(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        OutlinedTextField(
            value = text,
            onValueChange =
                onTextChanged,
            modifier =
                Modifier.weight(1f),
            placeholder = {
                Text("Message...")
            },
            maxLines = 4
        )

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        IconButton(
            onClick = onSend,
            enabled = text.isNotBlank()
        ) {

            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled.Send,
                contentDescription =
                    "Send"
            )
        }
    }
}