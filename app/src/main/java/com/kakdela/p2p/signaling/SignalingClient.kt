package com.kakdela.p2p.signaling

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI

object SignalingClient {
    private var client: WebSocketClient? = null
    var onMessage: ((JSONObject) -> Unit)? = null
    var onConnected: (() -> Unit)? = null

    fun connect(serverUrl: String = "wss://kakdela-signaling.onrender.com") {
        val uri = URI(serverUrl)
        client = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("Signaling", "Connected")
                onConnected?.invoke()
            }

            override fun onMessage(message: String) {
                val json = JSONObject(message)
                onMessage?.invoke(json)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {}
            override fun onError(ex: Exception?) { Log.e("Signaling", "Error", ex) }
        }
        client?.connect()
    }

    fun send(message: JSONObject) {
        client?.send(message.toString())
    }

    fun disconnect() {
        client?.close()
    }
}
