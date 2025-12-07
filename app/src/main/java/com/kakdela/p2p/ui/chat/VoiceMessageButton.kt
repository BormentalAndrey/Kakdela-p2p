package com.kakdela.p2p.ui.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import android.media.MediaRecorder
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import java.io.File

@Composable
fun VoiceMessageButton(
    onVoiceRecorded: (file: File, duration: Long) -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var startTime by remember { mutableStateOf(0L) }

    val tempFile = remember {
        File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a").apply { createNewFile() }
    }

    IconButton(
        onClick = {
            if (isRecording) {
                // Останавливаем запись
                recorder?.apply {
                    stop()
                    release()
                }
                recorder = null
                val duration = System.currentTimeMillis() - startTime
                onVoiceRecorded(tempFile, duration)
                isRecording = false
            } else {
                // Начинаем запись
                recorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(tempFile.absolutePath)
                    try {
                        prepare()
                        start()
                        startTime = System.currentTimeMillis()
                        isRecording = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        },
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = if (isRecording) androidx.compose.material.icons.Icons.Default.Stop
            else androidx.compose.material.icons.Icons.Default.Mic,
            contentDescription = if (isRecording) "Остановить" else "Голосовое",
            tint = if (isRecording) Color.Red else Color.White
        )
    }
}
