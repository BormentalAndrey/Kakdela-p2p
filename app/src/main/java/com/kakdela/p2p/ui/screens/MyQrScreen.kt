package com.kakdela.p2p.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakdela.p2p.crypto.CryptoManager
import com.kakdela.p2p.utils.DeviceUtils
import com.kakdela.p2p.utils.QrUtils

@Composable
fun MyQrScreen() {
    val context = LocalContext.current

    val rawId = DeviceUtils.getDeviceId(context) ?: "UNKNOWN"
    val peerId = "KAKDELA_$rawId"
    val keyPair = CryptoManager.getMyKeyPair()
    val publicKeyHex = keyPair.publicKey.asHexString
    val qrData = "kakdela://connect?id=$peerId&pubkey=$publicKeyHex"
    val qrBitmap = QrUtils.generateQrCode(qrData)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "My QR",
            modifier = Modifier.size(250.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Мой QR-код",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            peerId,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}
