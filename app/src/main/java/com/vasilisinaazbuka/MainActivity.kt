package com.vasilisinaazbuka

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
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
 * - Скрытие системных кнопок и статус-бара (свайп сверху/снизу для показа)
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
        
        // Скрываем системные кнопки и статус-бар
        hideSystemUI()
        
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

    /**
     * Скрывает системные кнопки и статус-бар
     * Появляются только при свайпе сверху вниз или снизу вверх
     */
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    override fun onPause() {
        super.onPause()
        AudioPlayer.pauseMusic()
        KnopaStateManager.saveState(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        AudioPlayer.resumeMusic()
        KnopaStateManager.updateAfterAbsence(applicationContext)
    }

    override fun onStop() {
        super.onStop()
        KnopaStateManager.saveState(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        KnopaStateManager.saveState(applicationContext)
        AudioPlayer.release()
    }
}

/**
 * Вспомогательный объект для управления состоянием Кнопы
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
            prefs.edit().putLong(KEY_LAST_UPDATE, System.currentTimeMillis()).apply()
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
