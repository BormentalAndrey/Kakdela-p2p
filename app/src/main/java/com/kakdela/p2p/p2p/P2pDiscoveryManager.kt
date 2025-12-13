
package com.kakdela.p2p.p2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.util.Log

class P2pDiscoveryManager(private val manager: WifiP2pManager, private val channel: WifiP2pManager.Channel) {

    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.d("P2pDiscovery", "Wi-Fi P2P is enabled")
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    manager.requestPeers(channel, peerListListener)
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    // Обработка соединения
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    // Обновление устройства
                }
            }
        }
    }

    private val peerListListener = PeerListListener { peerList ->
        val peers = peerList.deviceList
        peers.forEach { device ->
            Log.d("P2pDiscovery", "Found peer: \( {device.deviceName} - \){device.deviceAddress}")
            // Добавьте в UI или TrustedPeersManager
        }
    }
}
