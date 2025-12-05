package com.kakdela.p2p

import android.app.Application
import com.kakdela.p2p.data.MessageDatabase

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MessageDatabase.getInstance(this)
    }
}
