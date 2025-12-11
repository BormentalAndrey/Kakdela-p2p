package com.kakdela.p2p.ui.chat

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Правильные импорты для Material 3 иконок
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Mic
import androidx.compose.material3.icons.filled.MicOff

@Composable
fun VoiceMessageButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // Добавил для гибкости
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isRecording) Icons.Filled.MicOff else Icons.Filled.Mic,
            contentDescription = if (isRecording)
                "Остановить запись голосового сообщения"
            else
                "Начать запись голосового сообщения",
            tint = if (isRecording) Color.Red else Color.Unspecified, // Красный при записи
            modifier = Modifier.size(28.dp)
        )
    }
}
