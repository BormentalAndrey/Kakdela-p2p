package com.kakdela.p2p

import android.app.Application
import com.kakdela.p2p.db.AppDatabase
import com.kakdela.p2p.webrtc.WebRtcManager

class App : Application() {
    lateinit var database: AppDatabase
    lateinit var webRtcManager: WebRtcManager

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        webRtcManager = WebRtcManager(this)
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
