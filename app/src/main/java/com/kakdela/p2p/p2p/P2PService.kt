package com.kakdela.p2p.p2p

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.os.Looper
import android.util.Log
import com.kakdela.p2p.App
import com.kakdela.p2p.webrtc.WebRtcManager

class P2PService(private val context: Context) {

    private val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = manager.initialize(context, Looper.getMainLooper(), null)
    private val p2pDiscoveryManager = P2pDiscoveryManager(manager, channel)

    fun init() {
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        context.registerReceiver(p2pDiscoveryManager.broadcastReceiver, intentFilter)
    }

    fun discoverPeers(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        manager.discoverPeers(channel, object : ActionListener {
            override fun onSuccess() {
                onSuccess()
            }

            override fun onFailure(reason: Int) {
                onFailure("Discovery failed: $reason")
            }
        })
    }

    fun connectToPeer(deviceAddress: String) {
        val config = WifiP2pConfig().apply {
            deviceAddress = deviceAddress
            groupOwnerIntent = 0  // Не быть GO
        }
        manager.connect(channel, config, object : ActionListener {
            override fun onSuccess() {
                Log.d("P2PService", "Connection initiated")
            }
            override fun onFailure(reason: Int) {
                Log.e("P2PService", "Connection failed: $reason")
            }
        })
    }

    // По соединению - инициализировать WebRTC
    fun onConnected(peerId: String) {
        App.instance.webRtcManager.initiateConnection(peerId, "", listOf("stun:stun.l.google.com:19302"))
    }
}
