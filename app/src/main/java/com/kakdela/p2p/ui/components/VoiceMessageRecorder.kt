package com.kakdela.p2p.ui.components

import android.media.MediaRecorder
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import java.io.File
import java.util.UUID

@Composable
fun VoiceMessageRecorder(onVoiceSent: (ByteArray) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? = null
    var outputFile: File? = null

    Row(
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Record Voice",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(8.dp)
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            isRecording = true
                            outputFile = File(context.cacheDir, "voice_${UUID.randomUUID()}.3gp")
                            recorder = MediaRecorder().apply {
                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                setOutputFile(outputFile!!.absolutePath)
                                prepare()
                                start()
                            }
                        },
                        onDrag = { change: PointerInputChange, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                offsetX.animateTo(offsetX.value + dragAmount.x)
                            }
                            if (offsetX.value < -100f) {
                                recorder?.release()
                                recorder = null
                                outputFile?.delete()
                                isRecording = false
                                coroutineScope.launch { offsetX.snapTo(0f) }
                            }
                        },
                        onDragEnd = {
                            if (isRecording && offsetX.value > -100f) {
                                recorder?.apply {
                                    stop()
                                    release()
                                }
                                recorder = null
                                val voiceData = outputFile!!.readBytes()
                                onVoiceSent(voiceData)
                                outputFile?.delete()
                            }
                            isRecording = false
                            coroutineScope.launch { offsetX.snapTo(0f) }
                        },
                        onDragCancel = {
                            recorder?.release()
                            recorder = null
                            outputFile?.delete()
                            isRecording = false
                            coroutineScope.launch { offsetX.snapTo(0f) }
                        }
                    )
                }
        )
    }
}
