package com.vasilisinaazbuka

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
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
    
    private const val PREFS_NAME = "knopa_state"
    private const val KEY_FIRST_LAUNCH = "first_launch"
    private const val KEY_HUNGER = "knopa_hunger"
    private const val KEY_HAPPINESS = "knopa_happiness"
    private const val KEY_ENERGY = "knopa_energy"
    private const val KEY_HEALTH = "knopa_health"
    private const val KEY_CLEANLINESS = "knopa_cleanliness"
    private const val KEY_COINS = "knopa_coins"
    private const val KEY_LEVEL = "knopa_level"
    private const val KEY_LAST_UPDATE = "knopa_last_update"
    
    fun loadState(context: Context) {
        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
            
            if (isFirstLaunch) {
                // Первый запуск — создаём начальное состояние Кнопы
                prefs.edit()
                    .putBoolean(KEY_FIRST_LAUNCH, false)
                    .putFloat(KEY_HUNGER, 100f)
                    .putFloat(KEY_HAPPINESS, 100f)
                    .putFloat(KEY_ENERGY, 100f)
                    .putFloat(KEY_HEALTH, 100f)
                    .putFloat(KEY_CLEANLINESS, 100f)
                    .putInt(KEY_COINS, 50)
                    .putInt(KEY_LEVEL, 1)
                    .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                    .apply()
            }
        } catch (e: Exception) {
            Log.e("KnopaManager", "Ошибка загрузки состояния Кнопы: ${e.message}")
        }
    }
    
    fun saveState(context: Context) {
        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            Log.e("KnopaManager", "Ошибка сохранения состояния Кнопы: ${e.message}")
        }
    }
    
    fun updateAfterAbsence(context: Context) {
        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            val currentTime = System.currentTimeMillis()
            val absenceMinutes = (currentTime - lastUpdate) / 60_000
            
            if (absenceMinutes > 1) {
                // Кнопа проголодалась и заскучала за время отсутствия
                val hungerDecrease = (absenceMinutes * 0.5f)
                val happinessDecrease = (absenceMinutes * 0.3f)
                
                val currentHunger = prefs.getFloat(KEY_HUNGER, 100f)
                val currentHappiness = prefs.getFloat(KEY_HAPPINESS, 100f)
                
                prefs.edit()
                    .putFloat(KEY_HUNGER, maxOf(0f, currentHunger - hungerDecrease))
                    .putFloat(KEY_HAPPINESS, maxOf(0f, currentHappiness - happinessDecrease))
                    .putLong(KEY_LAST_UPDATE, currentTime)
                    .apply()
            }
        } catch (e: Exception) {
            Log.e("KnopaManager", "Ошибка обновления состояния Кнопы: ${e.message}")
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
            Log.e("KarManager", "Ошибка очистки кеша караоке: ${e.message}")
        }
    }
}
