package com.vasilisinaazbuka

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Управление состоянием игры и сохранение прогресса
 * Использует SharedPreferences для хранения данных между сессиями
 * 
 * Поддерживает:
 * - Сохранение прогресса по уровням и песням
 * - Систему достижений
 * - Ежедневные бонусы
 * - Статистику игрока
 * - Миграцию данных при обновлении версии
 */
object GameState {

    private lateinit var prefs: SharedPreferences
    private var isInitialized = false
    private val gson = Gson()

    // Версия схемы данных для миграции
    private const val DATA_VERSION = 1

    // Максимальное количество уровней для каждой игры
    const val MAX_COLORING_LEVELS = 5
    const val MAX_MUSICBOX_LEVELS = 1      // Один уровень, но с тремя режимами
    const val MAX_MEMORYPUZZLE_LEVELS = 5
    const val MAX_FEEDKUZYA_LEVELS = 5
    const val MAX_SEASONS_LEVELS = 4
    const val MAX_KARAOKE_LEVELS = 20      // 20 песен в караоке

    // Ключи для SharedPreferences
    private object PrefKeys {
        const val DATA_VERSION = "data_version"
        const val TOTAL_STARS = "total_stars"
        const val TOTAL_GAMES_PLAYED = "total_games_played"
        const val LAST_LOGIN_DATE = "last_login_date"
        const val DAILY_STREAK = "daily_streak"
        const val ACHIEVEMENTS = "achievements"
        const val SETTINGS_MUSIC = "settings_music_enabled"
        const val SETTINGS_SFX = "settings_sfx_enabled"
        const val FIRST_LAUNCH = "first_launch"
        const val PLAYER_NAME = "player_name"
    }

    /**
     * Инициализация GameState. Должна быть вызвана из MainActivity.onCreate()
     */
    fun init(context: Context) {
        if (!isInitialized) {
            prefs = context.getSharedPreferences("vasilisina_azbuka_progress", Context.MODE_PRIVATE)
            isInitialized = true
            
            // Проверяем версию данных и выполняем миграцию при необходимости
            checkDataMigration()
            
            // Обновляем ежедневный бонус
            updateDailyStreak()
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
     * Проверка, пройдена ли песня в караоке
     */
    fun isSongCompleted(songIndex: Int): Boolean {
        return isLevelCompleted("karaoke", songIndex)
    }

    /**
     * Отметить уровень как пройденный и сохранить прогресс
     */
    fun completeLevel(gameId: String, stage: Int, stars: Int = 3) {
        checkInitialized()
        val editor = prefs.edit()

        // Сохраняем факт прохождения уровня
        editor.putBoolean("${gameId}_level_$stage", true)

        // Сохраняем количество звёзд за уровень (максимальное из попыток)
        val existingStars = prefs.getInt("${gameId}_stars_level_$stage", 0)
        editor.putInt("${gameId}_stars_level_$stage", maxOf(existingStars, stars))

        // Обновляем общее количество звёзд
        val currentStars = prefs.getInt("${gameId}_stars", 0)
        if (stars > existingStars) {
            editor.putInt("${gameId}_stars", currentStars + (stars - existingStars))
        }

        // Обновляем максимальный пройденный уровень
        val maxLevel = prefs.getInt("${gameId}_max_level", 0)
        if (stage > maxLevel) {
            editor.putInt("${gameId}_max_level", stage)
        }

        // Обновляем общую статистику
        val totalGamesPlayed = prefs.getInt(PrefKeys.TOTAL_GAMES_PLAYED, 0)
        editor.putInt(PrefKeys.TOTAL_GAMES_PLAYED, totalGamesPlayed + 1)

        // Обновляем общее количество звёзд во всех играх
        val totalStars = prefs.getInt(PrefKeys.TOTAL_STARS, 0)
        editor.putInt(PrefKeys.TOTAL_STARS, totalStars + stars - existingStars)

        // Проверяем достижения
        checkAchievements(editor)

        editor.apply()
    }

    /**
     * Получить количество звёзд за конкретный уровень
     */
    fun getLevelStars(gameId: String, stage: Int): Int {
        checkInitialized()
        return prefs.getInt("${gameId}_stars_level_$stage", 0)
    }

    /**
     * Получить общее количество звёзд для игры
     */
    fun getTotalStars(gameId: String): Int {
        checkInitialized()
        return prefs.getInt("${gameId}_stars", 0)
    }

    /**
     * Получить общее количество звёзд во всех играх
     */
    fun getOverallStars(): Int {
        checkInitialized()
        return prefs.getInt(PrefKeys.TOTAL_STARS, 0)
    }

    /**
     * Получить максимальный пройденный уровень для игры
     */
    fun getMaxLevel(gameId: String): Int {
        checkInitialized()
        return prefs.getInt("${gameId}_max_level", 0)
    }

    /**
     * Получить максимальное количество уровней для игры
     */
    fun getMaxLevels(gameId: String): Int {
        return when (gameId) {
            "coloring" -> MAX_COLORING_LEVELS
            "musicbox" -> MAX_MUSICBOX_LEVELS
            "memorypuzzle" -> MAX_MEMORYPUZZLE_LEVELS
            "feedkuzya" -> MAX_FEEDKUZYA_LEVELS
            "seasons" -> MAX_SEASONS_LEVELS
            "karaoke" -> MAX_KARAOKE_LEVELS
            else -> 5
        }
    }

    /**
     * Проверить, полностью ли завершена игра
     */
    fun isGameCompleted(gameId: String): Boolean {
        checkInitialized()
        val maxLevels = getMaxLevels(gameId)
        return (1..maxLevels).all { level -> isLevelCompleted(gameId, level) }
    }

    /**
     * Получить процент завершения игры
     */
    fun getGameCompletionPercent(gameId: String): Float {
        checkInitialized()
        val maxLevels = getMaxLevels(gameId)
        if (maxLevels == 0) return 0f
        val completed = (1..maxLevels).count { level -> isLevelCompleted(gameId, level) }
        return completed.toFloat() / maxLevels.toFloat()
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
        val maxLevels = getMaxLevels(gameId)
        for (i in 1..maxLevels) {
            editor.remove("${gameId}_level_$i")
            editor.remove("${gameId}_stars_level_$i")
        }

        editor.apply()
    }

    /**
     * Сбросить весь прогресс в игре
     */
    fun resetAllProgress() {
        checkInitialized()
        prefs.edit().clear().apply()
        // Перезаписываем версию данных
        prefs.edit().putInt(PrefKeys.DATA_VERSION, DATA_VERSION).apply()
    }

    /**
     * Получить статистику по всем играм
     */
    fun getOverallProgress(): Map<String, Pair<Int, Int>> {
        checkInitialized()
        val games = listOf("coloring", "musicbox", "memorypuzzle", "feedkuzya", "seasons", "karaoke")
        val maxLevels = listOf(
            MAX_COLORING_LEVELS,
            MAX_MUSICBOX_LEVELS,
            MAX_MEMORYPUZZLE_LEVELS,
            MAX_FEEDKUZYA_LEVELS,
            MAX_SEASONS_LEVELS,
            MAX_KARAOKE_LEVELS
        )

        return games.zip(maxLevels).associate { (gameId, maxLevel) ->
            val completed = (1..maxLevel).count { level ->
                isLevelCompleted(gameId, level)
            }
            gameId to Pair(completed, maxLevel)
        }
    }

    /**
     * Получить детальную статистику по игре
     */
    fun getGameStats(gameId: String): GameStats {
        checkInitialized()
        val maxLevels = getMaxLevels(gameId)
        val completed = (1..maxLevels).count { level -> isLevelCompleted(gameId, level) }
        val totalStars = getTotalStars(gameId)
        val maxPossibleStars = maxLevels * 3
        
        return GameStats(
            gameId = gameId,
            completedLevels = completed,
            totalLevels = maxLevels,
            totalStars = totalStars,
            maxPossibleStars = maxPossibleStars,
            completionPercent = if (maxLevels > 0) completed.toFloat() / maxLevels * 100f else 0f,
            starPercent = if (maxPossibleStars > 0) totalStars.toFloat() / maxPossibleStars * 100f else 0f
        )
    }

    /**
     * Получить общую статистику игрока
     */
    fun getPlayerStats(): PlayerStats {
        checkInitialized()
        return PlayerStats(
            totalGamesPlayed = prefs.getInt(PrefKeys.TOTAL_GAMES_PLAYED, 0),
            totalStars = prefs.getInt(PrefKeys.TOTAL_STARS, 0),
            dailyStreak = prefs.getInt(PrefKeys.DAILY_STREAK, 0),
            gamesCompleted = listOf(
                "coloring", "musicbox", "memorypuzzle", 
                "feedkuzya", "seasons", "karaoke"
            ).count { isGameCompleted(it) },
            totalGames = 6
        )
    }

    // ==================== Настройки ====================

    /**
     * Установить громкость музыки
     */
    fun setMusicEnabled(enabled: Boolean) {
        checkInitialized()
        prefs.edit().putBoolean(PrefKeys.SETTINGS_MUSIC, enabled).apply()
    }

    /**
     * Проверить, включена ли музыка
     */
    fun isMusicEnabled(): Boolean {
        checkInitialized()
        return prefs.getBoolean(PrefKeys.SETTINGS_MUSIC, true)
    }

    /**
     * Установить звуковые эффекты
     */
    fun setSfxEnabled(enabled: Boolean) {
        checkInitialized()
        prefs.edit().putBoolean(PrefKeys.SETTINGS_SFX, enabled).apply()
    }

    /**
     * Проверить, включены ли звуковые эффекты
     */
    fun isSfxEnabled(): Boolean {
        checkInitialized()
        return prefs.getBoolean(PrefKeys.SETTINGS_SFX, true)
    }

    // ==================== Достижения ====================

    /**
     * Получить список всех достижений
     */
    fun getAchievements(): List<Achievement> {
        checkInitialized()
        val json = prefs.getString(PrefKeys.ACHIEVEMENTS, null) ?: return Achievement.defaultAchievements
        return try {
            gson.fromJson(json, object : TypeToken<List<Achievement>>() {}.type)
        } catch (e: Exception) {
            Achievement.defaultAchievements
        }
    }

    /**
     * Проверить и обновить достижения
     */
    private fun checkAchievements(editor: SharedPreferences.Editor) {
        val achievements = getAchievements().toMutableList()
        var updated = false

        achievements.forEachIndexed { index, achievement ->
            if (!achievement.isUnlocked) {
                val shouldUnlock = when (achievement.id) {
                    "first_level" -> true
                    "all_coloring" -> isGameCompleted("coloring")
                    "all_music" -> isGameCompleted("musicbox")
                    "all_puzzle" -> isGameCompleted("memorypuzzle")
                    "all_feeding" -> isGameCompleted("feedkuzya")
                    "all_seasons" -> isGameCompleted("seasons")
                    "all_karaoke" -> isGameCompleted("karaoke")
                    "ten_stars" -> getOverallStars() >= 10
                    "fifty_stars" -> getOverallStars() >= 50
                    "hundred_stars" -> getOverallStars() >= 100
                    "all_games" -> listOf(
                        "coloring", "musicbox", "memorypuzzle",
                        "feedkuzya", "seasons", "karaoke"
                    ).all { isGameCompleted(it) }
                    else -> false
                }

                if (shouldUnlock) {
                    achievements[index] = achievement.copy(
                        isUnlocked = true,
                        unlockDate = System.currentTimeMillis()
                    )
                    updated = true
                }
            }
        }

        if (updated) {
            val json = gson.toJson(achievements)
            editor.putString(PrefKeys.ACHIEVEMENTS, json)
        }
    }

    // ==================== Ежедневные бонусы ====================

    /**
     * Обновить ежедневную серию
     */
    private fun updateDailyStreak() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastLogin = prefs.getString(PrefKeys.LAST_LOGIN_DATE, null)
        val currentStreak = prefs.getInt(PrefKeys.DAILY_STREAK, 0)

        val editor = prefs.edit()
        
        if (lastLogin == null) {
            // Первый вход
            editor.putString(PrefKeys.LAST_LOGIN_DATE, today)
            editor.putInt(PrefKeys.DAILY_STREAK, 1)
        } else if (lastLogin != today) {
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
            )
            
            if (lastLogin == yesterday) {
                // Последовательный день
                editor.putInt(PrefKeys.DAILY_STREAK, currentStreak + 1)
            } else {
                // Пропущенный день — сброс серии
                editor.putInt(PrefKeys.DAILY_STREAK, 1)
            }
            
            editor.putString(PrefKeys.LAST_LOGIN_DATE, today)
        }
        
        editor.apply()
    }

    /**
     * Получить текущую ежедневную серию
     */
    fun getDailyStreak(): Int {
        checkInitialized()
        return prefs.getInt(PrefKeys.DAILY_STREAK, 0)
    }

    /**
     * Проверить, можно ли получить ежедневный бонус
     */
    fun canClaimDailyBonus(): Boolean {
        checkInitialized()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastClaimDate = prefs.getString("daily_bonus_claim_date", null)
        return lastClaimDate != today
    }

    /**
     * Получить ежедневный бонус
     */
    fun claimDailyBonus(): DailyBonus {
        checkInitialized()
        val streak = getDailyStreak()
        val bonusStars = minOf(streak, 7) // Максимум 7 звёзд за 7-дневную серию
        
        // Добавляем звёзды
        val editor = prefs.edit()
        val totalStars = prefs.getInt(PrefKeys.TOTAL_STARS, 0)
        editor.putInt(PrefKeys.TOTAL_STARS, totalStars + bonusStars)
        
        // Отмечаем получение бонуса
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        editor.putString("daily_bonus_claim_date", today)
        editor.apply()
        
        return DailyBonus(
            stars = bonusStars,
            streak = streak,
            message = "Ежедневный бонус: $bonusStars звёзд за ${streak}-дневную серию!"
        )
    }

    // ==================== Миграция данных ====================

    /**
     * Проверка и выполнение миграции данных при обновлении версии
     */
    private fun checkDataMigration() {
        val currentVersion = prefs.getInt(PrefKeys.DATA_VERSION, 0)
        
        if (currentVersion < DATA_VERSION) {
            // Выполняем миграцию с версии currentVersion до DATA_VERSION
            when {
                currentVersion < 1 -> {
                    // Миграция на версию 1: добавляем поддержку 20 песен в караоке
                    migrateToV1()
                }
            }
            
            // Обновляем версию данных
            prefs.edit().putInt(PrefKeys.DATA_VERSION, DATA_VERSION).apply()
        }
    }

    /**
     * Миграция на версию 1 данных
     */
    private fun migrateToV1() {
        // Если есть старые данные караоке (5 уровней), конвертируем в новые (20 песен)
        val oldKaraokeMaxLevel = prefs.getInt("karaoke_max_level", 0)
        if (oldKaraokeMaxLevel > 0) {
            val editor = prefs.edit()
            // Переносим старые уровни как пройденные песни
            for (i in 1..oldKaraokeMaxLevel) {
                val wasCompleted = prefs.getBoolean("karaoke_level_$i", false)
                if (wasCompleted) {
                    editor.putBoolean("karaoke_level_$i", true)
                    val stars = prefs.getInt("karaoke_stars_level_$i", 3)
                    editor.putInt("karaoke_stars_level_$i", stars)
                }
            }
            editor.putInt("karaoke_max_level", oldKaraokeMaxLevel)
            editor.apply()
        }
    }

    // ==================== Вспомогательные методы ====================

    /**
     * Проверить, первый ли это запуск приложения
     */
    fun isFirstLaunch(): Boolean {
        checkInitialized()
        val isFirst = prefs.getBoolean(PrefKeys.FIRST_LAUNCH, true)
        if (isFirst) {
            prefs.edit().putBoolean(PrefKeys.FIRST_LAUNCH, false).apply()
        }
        return isFirst
    }

    /**
     * Сохранить имя игрока
     */
    fun setPlayerName(name: String) {
        checkInitialized()
        prefs.edit().putString(PrefKeys.PLAYER_NAME, name).apply()
    }

    /**
     * Получить имя игрока
     */
    fun getPlayerName(): String {
        checkInitialized()
        return prefs.getString(PrefKeys.PLAYER_NAME, "Игрок") ?: "Игрок"
    }

    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException(
                "GameState не инициализирован! Вызовите GameState.init(context) в MainActivity.onCreate()"
            )
        }
    }

    // ==================== Модели данных ====================

    /**
     * Статистика по конкретной игре
     */
    data class GameStats(
        val gameId: String,
        val completedLevels: Int,
        val totalLevels: Int,
        val totalStars: Int,
        val maxPossibleStars: Int,
        val completionPercent: Float,
        val starPercent: Float
    )

    /**
     * Общая статистика игрока
     */
    data class PlayerStats(
        val totalGamesPlayed: Int,
        val totalStars: Int,
        val dailyStreak: Int,
        val gamesCompleted: Int,
        val totalGames: Int
    )

    /**
     * Достижение
     */
    data class Achievement(
        val id: String,
        val title: String,
        val description: String,
        val emoji: String,
        val isUnlocked: Boolean = false,
        val unlockDate: Long = 0L
    ) {
        companion object {
            val defaultAchievements = listOf(
                Achievement("first_level", "Первый шаг", "Пройти первый уровень", "🎯"),
                Achievement("all_coloring", "Художник", "Пройти всю раскраску", "🎨"),
                Achievement("all_music", "Музыкант", "Пройти музыкальную шкатулку", "🎵"),
                Achievement("all_puzzle", "Пазломастер", "Собрать все пазлы", "🧩"),
                Achievement("all_feeding", "Повар", "Накормить Кузю во всех уровнях", "👨‍🍳"),
                Achievement("all_seasons", "Натуралист", "Изучить все времена года", "🌍"),
                Achievement("all_karaoke", "Певец", "Спеть все 20 песен", "🎤"),
                Achievement("ten_stars", "Звёздный начинающий", "Собрать 10 звёзд", "⭐"),
                Achievement("fifty_stars", "Звёздный мастер", "Собрать 50 звёзд", "🌟"),
                Achievement("hundred_stars", "Звёздный чемпион", "Собрать 100 звёзд", "💫"),
                Achievement("all_games", "Василисин ученик", "Пройти все игры полностью", "👸")
            )
        }
    }

    /**
     * Ежедневный бонус
     */
    data class DailyBonus(
        val stars: Int,
        val streak: Int,
        val message: String
    )
}
