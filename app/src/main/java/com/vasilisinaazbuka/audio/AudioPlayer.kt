package com.vasilisinaazbuka

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Управление звуковыми эффектами и музыкой в игре
 * Использует SoundPool для коротких звуков и MediaPlayer для фоновой музыки
 */
object AudioPlayer {

    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null
    private var context: Context? = null
    private val soundCache = ConcurrentHashMap<Int, Int>()
    private var isInitialized = false
    private var musicVolume = 0.7f
    private var sfxVolume = 1.0f
    private var isMusicEnabled = true
    private var isSfxEnabled = true

    /**
     * Инициализация аудиоплеера
     */
    fun init(context: Context) {
        this.context = context.applicationContext

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        isInitialized = true
        Log.d("AudioPlayer", "Аудиоплеер инициализирован")
    }

    /**
     * Воспроизведение звукового эффекта
     */
    fun playSFX(soundResId: Int) {
        if (!isInitialized || !isSfxEnabled) return

        try {
            // Кешируем звук, если он ещё не загружен
            val soundId = soundCache.getOrPut(soundResId) {
                context?.let { ctx ->
                    soundPool?.load(ctx, soundResId, 1) ?: 0
                } ?: 0
            }

            if (soundId != 0) {
                soundPool?.play(soundId, sfxVolume, sfxVolume, 1, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Ошибка воспроизведения SFX: ${e.message}")
        }
    }

    /**
     * Воспроизведение фоновой музыки (зациклено)
     */
    fun playMusic(musicResId: Int) {
        if (!isInitialized || !isMusicEnabled) return

        try {
            stopMusic()
            mediaPlayer = MediaPlayer.create(context, musicResId)?.apply {
                isLooping = true
                setVolume(musicVolume, musicVolume)
                start()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Ошибка воспроизведения музыки: ${e.message}")
        }
    }

    /**
     * Остановка фоновой музыки
     */
    fun stopMusic() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    /**
     * Пауза музыки
     */
    fun pauseMusic() {
        mediaPlayer?.pause()
    }

    /**
     * Продолжить музыку
     */
    fun resumeMusic() {
        mediaPlayer?.start()
    }

    /**
     * Установить громкость музыки (0.0 - 1.0)
     */
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(musicVolume, musicVolume)
    }

    /**
     * Установить громкость звуковых эффектов (0.0 - 1.0)
     */
    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }

    /**
     * Включить/выключить музыку
     */
    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) {
            stopMusic()
        }
    }

    /**
     * Включить/выключить звуковые эффекты
     */
    fun setSfxEnabled(enabled: Boolean) {
        isSfxEnabled = enabled
    }

    /**
     * Очистка ресурсов
     */
    fun release() {
        stopMusic()
        soundPool?.release()
        soundPool = null
        soundCache.clear()
        isInitialized = false
        Log.d("AudioPlayer", "Аудиоплеер освобождён")
    }
}
