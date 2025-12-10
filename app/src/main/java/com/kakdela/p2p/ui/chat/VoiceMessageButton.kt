package com.kakdela.p2p.ui.chat

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Mic
import androidx.compose.material3.icons.filled.MicOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
@Composable
fun VoiceMessageButton(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isRecording) Icons.Filled.MicOff else Icons.Filled.Mic,
            contentDescription = if (isRecording) "Остановить запись" else "Запись голосового",
            tint = if (isRecording) Color.Red else Color.Unspecified,
            modifier = Modifier.size(28.dp)
        )
    }
}
