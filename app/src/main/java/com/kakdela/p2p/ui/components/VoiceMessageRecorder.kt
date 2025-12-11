// app/src/main/java/com/kakdela/p2p/ui/components/VoiceMessageRecorder.kt
package com.kakdela.p2p.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VoiceMessageRecorder(
    peerId: String,
    onVoiceSent: () -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }
    var slideToCancel by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val recorder = remember { android.media.MediaRecorder() }
    val audioFile = remember { File(context.cacheDir, "voice_${System.currentTimeMillis()}.aac") }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recorder.setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.AAC_ADTS)
            recorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(audioFile.absolutePath)
            recorder.prepare()
            recorder.start()

            while (isRecording) {
                delay(1000)
                recordingTime++
            }
        } else if (recordingTime > 0) {
            recorder.stop()
            recorder.release()
            // Отправляем голосовое
            com.kakela.p2p.webrtc.FileTransferManager.sendVoice(peerId, audioFile)
            onVoiceSent()
            recordingTime = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(if (isRecording) Color.Red.copy(0.9f) else MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isRecording = true },
                    onDragEnd = {
                        if (slideToCancel) {
                            isRecording = false // отмена
                        } else if (isRecording) {
                            isRecording = false // отправка
                        }
                    },
                    onDragCancel = { isRecording = false },
                    onDrag = { change, dragAmount ->
                        if (dragAmount.x < -100) slideToCancel = true
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (isRecording) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, "Запись", tint = Color.White)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (slideToCancel) "← Смахни для отмены" else formatTime(recordingTime),
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        } else {
            IconButton(onClick = { isRecording = true }) {
                Icon(Icons.Default.Mic, "Голосовое сообщение", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
