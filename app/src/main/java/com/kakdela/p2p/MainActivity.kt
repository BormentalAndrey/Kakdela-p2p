package com.kakdela.p2p

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kakdela.p2p.ui.Theme.KakdelaP2PTheme
import com.kakdela.p2p.ui.screens.ChatListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KakdelaP2PTheme {
                ChatListScreen()
            }
        }
    }
}
