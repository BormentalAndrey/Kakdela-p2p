package com.kakdela.p2p

import android.app.Application
import com.kakdela.p2p.data.MessageDatabase

class KakdelaApplication : Application() {
    lateinit var database: MessageDatabase

    override fun onCreate() {
        super.onCreate()
        database = MessageDatabase.getInstance(this)
    }
}
