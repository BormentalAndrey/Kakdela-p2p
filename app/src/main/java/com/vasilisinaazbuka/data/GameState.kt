package com.vasilisinaazbuka

import android.content.Context
import android.content.SharedPreferences

/**
 * Управление состоянием игры и сохранение прогресса
 * Использует SharedPreferences для хранения данных между сессиями
 */
object GameState {

    private lateinit var prefs: SharedPreferences
    private var isInitialized = false

    // Максимальное количество уровней для каждой игры
    const val MAX_COLORING_LEVELS = 5
    const val MAX_MUSICBOX_LEVELS = 1      // Один уровень, но с тремя режимами
    const val MAX_MEMORYPUZZLE_LEVELS = 5
    const val MAX_FEEDKUZYA_LEVELS = 5
    const val MAX_SEASONS_LEVELS = 4
    const val MAX_KARAOKE_LEVELS = 5

    /**
     * Инициализация GameState. Должна быть вызвана из MainActivity.onCreate()
     */
    fun init(context: Context) {
        if (!isInitialized) {
            prefs = context.getSharedPreferences("vasilisina_azbuka_progress", Context.MODE_PRIVATE)
            isInitialized = true
        }
    }

    /**
     * Проверка, пройден ли уровень
     */
    fun isLevelCompleted(gameId: String, stage: Int): Boolean {
        checkInitialized()
        return prefs.getBoolean("${gameId}_level_$stage", false)
    }

    /**
     * Отметить уровень как пройденный и сохранить прогресс
     */
    fun completeLevel(gameId: String, stage: Int, stars: Int = 3) {
        checkInitialized()
        val editor = prefs.edit()

        // Сохраняем факт прохождения уровня
        editor.putBoolean("${gameId}_level_$stage", true)

        // Обновляем общее количество звёзд
        val currentStars = prefs.getInt("${gameId}_stars", 0)
        editor.putInt("${gameId}_stars", currentStars + stars)

        // Обновляем максимальный пройденный уровень
        val maxLevel = prefs.getInt("${gameId}_max_level", 0)
        if (stage > maxLevel) {
            editor.putInt("${gameId}_max_level", stage)
        }

        editor.apply()
    }

    /**
     * Получить общее количество звёзд для игры
     */
    fun getTotalStars(gameId: String): Int {
        checkInitialized()
        return prefs.getInt("${gameId}_stars", 0)
    }

    /**
     * Получить максимальный пройденный уровень для игры
     */
    fun getMaxLevel(gameId: String): Int {
        checkInitialized()
        return prefs.getInt("${gameId}_max_level", 0)
    }

    /**
     * Сбросить прогресс для конкретной игры
     */
    fun resetGame(gameId: String) {
        checkInitialized()
        val editor = prefs.edit()
        editor.remove("${gameId}_stars")
        editor.remove("${gameId}_max_level")

        // Удаляем все записи об уровнях
        val maxLevels = when (gameId) {
            "coloring" -> MAX_COLORING_LEVELS
            "musicbox" -> MAX_MUSICBOX_LEVELS
            "memorypuzzle" -> MAX_MEMORYPUZZLE_LEVELS
            "feedkuzya" -> MAX_FEEDKUZYA_LEVELS
            "seasons" -> MAX_SEASONS_LEVELS
            "karaoke" -> MAX_KARAOKE_LEVELS
            else -> 5
        }

        for (i in 1..maxLevels) {
            editor.remove("${gameId}_level_$i")
        }

        editor.apply()
    }

    /**
     * Сбросить весь прогресс в игре
     */
    fun resetAllProgress() {
        checkInitialized()
        prefs.edit().clear().apply()
    }

    /**
     * Получить статистику по всем играм
     */
    fun getOverallProgress(): Map<String, Pair<Int, Int>> {
        checkInitialized()
        val games = listOf("coloring", "musicbox", "memorypuzzle", "feedkuzya", "seasons", "karaoke")
        val maxLevels = listOf(MAX_COLORING_LEVELS, MAX_MUSICBOX_LEVELS, MAX_MEMORYPUZZLE_LEVELS,
            MAX_FEEDKUZYA_LEVELS, MAX_SEASONS_LEVELS, MAX_KARAOKE_LEVELS)

        return games.zip(maxLevels).associate { (gameId, maxLevel) ->
            val completed = (1..maxLevel).count { level ->
                isLevelCompleted(gameId, level)
            }
            gameId to Pair(completed, maxLevel)
        }
    }

    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("GameState не инициализирован! Вызовите GameState.init(context) в MainActivity")
        }
    }
}
