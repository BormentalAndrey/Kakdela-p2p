package com.kakdela.p2p.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakdela.p2p.webrtc.FileTransferManager
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VoiceMessageRecorder(
    peerId: String,
    onVoiceSent: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0L) } // в секундах
    var isCancelled by remember { mutableStateOf(false) }

    val offsetX = remember { Animatable(0f) }
    val context = LocalContext.current

    val audioFile = remember {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        File(context.cacheDir, "voice_$timestamp.aac")
    }

    // Таймер записи
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTime = 0L
            while (isRecording) {
                delay(1000L)
                recordingTime++
            }
        }
    }

    // Логика начала/остановки записи
    val startRecording: () -> Unit = {
        isRecording = true
        isCancelled = false
        offsetX.snapTo(0f)
    }

    val stopRecording: (cancelled: Boolean) -> Unit = { cancelled ->
        isRecording = false
        offsetX.snapTo(0f)
        if (!cancelled && recordingTime > 0) {
            FileTransferManager.sendVoice(peerId, audioFile)
            onVoiceSent()
        }
        if (cancelled && audioFile.exists()) {
            audioFile.delete()
        }
        recordingTime = 0L
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(
                color = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isRecording) {
            IconButton(
                onClick = startRecording,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Запись голосового сообщения",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { },
                            onDragEnd = {
                                if (offsetX.value < -120f) {
                                    isCancelled = true
                                    stopRecording(cancelled = true)
                                } else {
                                    stopRecording(cancelled = false)
                                }
                            },
                            onDragCancel = { stopRecording(cancelled = true) },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val newOffset = (offsetX.value + dragAmount.x).coerceAtMost(0f)
                                offsetX.animateTo(newOffset, tween(0))
                            }
                        )
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = offsetX.value.dp.coerceAtLeast(-200.dp))
                        .padding(start = 16.dp)
                ) {
                    if (offsetX.value < -80f) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Смахни для отмены",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = formatTime(recordingTime),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                    }
                }

                IconButton(
                    onClick = { stopRecording(cancelled = false) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MicOff,
                        contentDescription = "Остановить запись",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
