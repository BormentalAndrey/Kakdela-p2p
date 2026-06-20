package com.vasilisinaazbuka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.vasilisinaazbuka.ui.theme.VasilisinaAzbukaTheme

/**
 * Главная Activity приложения «Василисина азбука: Путешествие по России»
 * Точка входа в приложение
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Инициализация GameState с контекстом приложения
        GameState.init(applicationContext)

        setContent {
            VasilisinaAzbukaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VasilisinaAzbukaApp()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Очистка ресурсов AudioPlayer
        AudioPlayer.release()
    }
}
