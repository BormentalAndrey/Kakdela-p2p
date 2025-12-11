package com.kakdela.p2p.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    // Уникальный ID устройства (можно заменить на имя пользователя позже)
    val deviceId = DeviceUtils.getDeviceId() ?: "UNKNOWN"
    val myPeerId = "KAKDELA_$deviceId"
    val qrData = "kakdela://peer/$myPeerId"

    val qrBitmap = QrUtils.generateQrBitmap(qrData, 800)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Мой QR-код",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = "Мой QR-код",
            modifier = Modifier
                .size(300.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(20.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = myPeerId,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
