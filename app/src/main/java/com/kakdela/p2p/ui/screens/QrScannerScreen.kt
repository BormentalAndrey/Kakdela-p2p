package com.kakdela.p2p.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun QrScannerScreen(
    onPeerScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val data = result.contents
            if (data.startsWith("kakdela://peer/")) {
                val peerId = data.removePrefix("kakdela://peer/")
                onPeerScanned(peerId)
            }
        }
        onDismiss()
    }

    LaunchedEffect(Unit) {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Наведи камеру на QR-код Kakdela")
            setCameraId(0)
            setBeepEnabled(true)
            setOrientationLocked(false)
        }
        launcher.launch(options)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Сканирование QR-кода…", color = Color.White)
    }
}
