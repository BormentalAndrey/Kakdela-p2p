package com.kakdela.p2p

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kakdela.p2p.ui.screens.ChatScreen
import com.kakdela.p2p.ui.theme.KakdelaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KakdelaTheme {
                ChatScreen()
            }
        }
    }
}
