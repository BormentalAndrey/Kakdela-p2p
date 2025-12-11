// app/src/main/java/com/kakdela/p2p/ui/screens/MyQrScreen.kt
package com.kakdela.p2p.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakdela.p2p.utils.DeviceUtils
import com.kakdela.p2p.utils.QrUtils

@Composable
fun MyQrScreen() {
    // Уникальный ID устройства — вечный, даже после переустановки
    val rawId = DeviceUtils.getDeviceId() ?: "UNKNOWN"
    val myPeerId = "KAKDELA_${rawId.takeLast(12)}"  // короткий, но уникальный

    // ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
    // ВАЛЬТЕРНАТИВА 1: Через интернет (рекомендую)
    val qrData = "https://kakdela.app/add/$myPeerId"

    // АЛЬТЕРНАТИВА 2: Только оффлайн (если не хочешь сайт)
    // val qrData = "kakdela://peer/$myPeerId"
    // ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←

    val qrBitmap = QrUtils.generateQrBitmap(qrData, 900)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Добавь меня в Kakdela",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Покажи этот QR-код другу — и вы сразу сможете писать друг другу",
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.size(320.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Мой QR-код для добавления",
                    modifier = Modifier
                        .size(280.dp)
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = myPeerId,
            fontFamily = FontFamily.Monospace,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Работает через интернет и без него",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
