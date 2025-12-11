// app/src/main/java/com/kakdela/p2p/ui/screens/MyQrScreen.kt
package com.kakdela.p2p.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakdela.p2p.crypto.CryptoManager
import com.kakdela.p2p.utils.DeviceUtils
import com.kakdela.p2p.utils.QrUtils

@Composable
fun MyQrScreen() {
    val context = LocalContext.current

    // 1. Уникальный ID устройства
    val rawId = DeviceUtils.getDeviceId(context) ?: "UNKNOWN"
    val myPeerId = "KAKDELA_${rawId.takeLast(12)}"

    // 2. Публичный ключ для E2E-шифрования
    val publicKeyHex = CryptoManager.getMyKeyPair().publicKey.asHexString

    // 3. Бесплатные STUN-серверы Google (для пробивания NAT)
    val iceServers = "stun:stun.l.google.com:19302,stun:stun1.l.google.com:19302"

    // 4. Финальная строка для QR — всё в одном!
    val qrData = "kakdela://connect?v=2&id=$myPeerId&pk=$publicKeyHex&ice=$iceServers"

    val qrBitmap = QrUtils.generateQrBitmap(qrData, 1000)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Подключись ко мне",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Отсканируй этот QR-код — и мы сможем писать друг другу в любой точке мира",
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Card(
            modifier = Modifier.size(340.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR-код для подключения",
                    modifier = Modifier
                        .size(300.dp)
                        .padding(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = myPeerId,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Работает через интернет и без него · Полностью зашифровано",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}
