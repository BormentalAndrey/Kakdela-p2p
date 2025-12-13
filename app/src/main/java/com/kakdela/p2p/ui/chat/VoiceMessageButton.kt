
package com.kakdela.p2p.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kakdela.p2p.ui.components.VoiceMessageRecorder

@Composable
fun VoiceMessageButton(onVoiceSent: (ByteArray) -> Unit) {
    VoiceMessageRecorder(onVoiceSent = onVoiceSent)
}
