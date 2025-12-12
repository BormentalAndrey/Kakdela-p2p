package com.kakdela.p2p.webrtc

import android.net.Uri
import org.webrtc.DataChannel

class FileTransferManager(
    private val rtc: WebRtcManager
) {
    fun sendFile(uri: Uri, name: String) {
        val data = uri.toString().toByteArray()
        rtc.encryptAndSend(data)
    }
}
