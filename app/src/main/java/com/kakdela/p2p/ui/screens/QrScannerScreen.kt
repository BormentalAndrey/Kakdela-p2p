package com.kakdela.p2p.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect

@Composable
fun QrScannerScreen(onPeerAdded: (String, String, List<String>) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intent = result.data
        if (intent != null) {
            val contents = intent.getStringExtra("SCAN_RESULT")
            // Парсинг qrData: peerId, pubKey, iceServers
            val peerId = contents?.split("?")?.get(1)?.split("&")?.get(0)?.split("=")?.get(1) ?: ""
            val publicKeyHex = contents.split("&") [0].split("=")[1]
            val iceServers = contents.split("&")[1].split("=")[1].split(",")
            onPeerAdded(peerId, publicKeyHex, iceServers)
        }
    }

    LaunchedEffect(Unit) {
        val options = ScanOptions()
        options.setPrompt("Сканируйте QR")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        launcher.launch(options.createScanIntent(LocalContext.current))
    }
}
