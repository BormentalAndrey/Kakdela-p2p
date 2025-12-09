package com.kakdela.p2p.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kakdela.p2p.R

@Composable
fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    var text by remember { mutableStateOf(value) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(30.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onValueChange(it)
            },
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) Text("Напиши сообщение...", color = Color.Gray)
                innerTextField()
            },
            singleLine = true
        )

        IconButton(onClick = {
            if (text.isNotBlank()) {
                onSend()
                text = ""
            }
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_send),
                contentDescription = "Send",
                tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}
