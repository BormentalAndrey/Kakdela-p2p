package com.kakdela.p2p.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kakdela.p2p.crypto.CryptoManager
import com.kakdela.p2p.utils.DeviceUtils
import com.kakdela.p2p.utils.QrUtils
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyQrScreen() {
    val context = LocalContext.current
    val cryptoManager = CryptoManager()
    val deviceId = DeviceUtils.getDeviceId(context) ?: "UNKNOWN"
    val peerId = "KAKDELA_$deviceId"
    val keyPair = cryptoManager.generateKeyPair()
    val publicKeyHex = keyPair.publicKey.asHexString
    val qrData = "kakdela://connect?peerId=$peerId&pubKey=$publicKeyHex&iceServers=stun:stun.l.google.com:19302"
    val qrBitmap = QrUtils.generateQrCode(qrData)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = Modifier.size(250.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Мой QR-код для добавления",
            textAlign = TextAlign.Center
        )
        Text(
            text = peerId,
            textAlign = TextAlign.Center
        )
    }
}
