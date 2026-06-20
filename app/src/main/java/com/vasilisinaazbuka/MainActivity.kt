package com.vasilisinaazbuka

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.vasilisinaazbuka.audio.AudioPlayer
import com.vasilisinaazbuka.data.GameState
import com.vasilisinaazbuka.ui.theme.VasilisinaAzbukaTheme

/**
 * Главная Activity приложения «В гостях у Василисы»
 * 
 * Управляет жизненным циклом приложения:
 * - Принудительная ландшафтная ориентация
 * - Инициализация игрового состояния (GameState)
 * - Инициализация аудиоплеера (AudioPlayer)
 * - Настройка полноэкранного режима
 * - Обработка паузы/возобновления для тамагочи Кнопы
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Принудительная ландшафтная ориентация
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // Полноэкранный режим с отсечением вырезов камеры
        enableEdgeToEdge()
        
        // Держим экран включённым во время игры
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Инициализация GameState с контекстом приложения
        GameState.init(applicationContext)
        
        // Инициализация AudioPlayer для звуковых эффектов
        AudioPlayer.init(applicationContext)
        
        // Загружаем сохранённое состояние Кнопы (тамагочи)
        KnopaStateManager.loadState(applicationContext)

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

    override fun onPause() {
        super.onPause()
        // Ставим музыку на паузу при сворачивании приложения
        AudioPlayer.pauseMusic()
        
        // Сохраняем состояние Кнопы
        KnopaStateManager.saveState(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        // Возобновляем музыку при возврате в приложение
        AudioPlayer.resumeMusic()
        
        // Обновляем состояние Кнопы после отсутствия
        KnopaStateManager.updateAfterAbsence(applicationContext)
        
        // Проверяем ежедневный бонус
        GameState.checkDailyBonus()
    }

    override fun onStop() {
        super.onStop()
        // Сохраняем состояние при выходе из приложения
        KnopaStateManager.saveState(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Финальное сохранение состояния Кнопы
        KnopaStateManager.saveState(applicationContext)
        
        // Очистка ресурсов AudioPlayer
        AudioPlayer.release()
        
        // Очистка кеша караоке-файлов
        KarFileManager.clearCache()
    }
}

/**
 * Вспомогательный объект для управления состоянием Кнопы
 * (Сохранение/загрузка/обновление тамагочи)
 */
private object KnopaStateManager {
    
    fun loadState(context: android.content.Context) {
        try {
            val prefs = context.getSharedPreferences("knopa_state", android.content.Context.MODE_PRIVATE)
            val isFirstLaunch = prefs.getBoolean("first_launch", true)
            
            if (isFirstLaunch) {
                // Первый запуск — создаём начальное состояние Кнопы
                prefs.edit()
                    .putBoolean("first_launch", false)
                    .putFloat("knopa_hunger", 100f)
                    .putFloat("knopa_happiness", 100f)
                    .putFloat("knopa_energy", 100f)
                    .putFloat("knopa_health", 100f)
                    .putFloat("knopa_cleanliness", 100f)
                    .putInt("knopa_coins", 50)
                    .putInt("knopa_level", 1)
                    .putLong("knopa_last_update", System.currentTimeMillis())
                    .apply()
            }
        } catch (e: Exception) {
            android.util.Log.e("KnopaManager", "Ошибка загрузки состояния Кнопы: ${e.message}")
        }
    }
    
    fun saveState(context: android.content.Context) {
        try {
            val prefs = context.getSharedPreferences("knopa_state", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putLong("knopa_last_update", System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("KnopaManager", "Ошибка сохранения состояния Кнопы: ${e.message}")
        }
    }
    
    fun updateAfterAbsence(context: android.content.Context) {
        try {
            val prefs = context.getSharedPreferences("knopa_state", android.content.Context.MODE_PRIVATE)
            val lastUpdate = prefs.getLong("knopa_last_update", System.currentTimeMillis())
            val currentTime = System.currentTimeMillis()
            val absenceMinutes = (currentTime - lastUpdate) / 60_000
            
            if (absenceMinutes > 1) {
                // Кнопа проголодалась и заскучала за время отсутствия
                val hungerDecrease = (absenceMinutes * 0.5f).toFloat()
                val happinessDecrease = (absenceMinutes * 0.3f).toFloat()
                
                val currentHunger = prefs.getFloat("knopa_hunger", 100f)
                val currentHappiness = prefs.getFloat("knopa_happiness", 100f)
                
                prefs.edit()
                    .putFloat("knopa_hunger", maxOf(0f, currentHunger - hungerDecrease))
                    .putFloat("knopa_happiness", maxOf(0f, currentHappiness - happinessDecrease))
                    .putLong("knopa_last_update", currentTime)
                    .apply()
            }
        } catch (e: Exception) {
            android.util.Log.e("KnopaManager", "Ошибка обновления состояния Кнопы: ${e.message}")
        }
    }
}

/**
 * Вспомогательный объект для управления кешем караоке-файлов
 */
private object KarFileManager {
    
    fun clearCache() {
        try {
            com.vasilisinaazbuka.games.KarFileLoader.clearCache()
        } catch (e: Exception) {
            android.util.Log.e("KarManager", "Ошибка очистки кеша караоке: ${e.message}")
        }
    }
}
