package com.vasilisinaazbuka.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GameState {

    private lateinit var prefs: SharedPreferences
    private var isInitialized = false
    private val gson = Gson()
    private const val DATA_VERSION = 1

    const val MAX_COLORING_LEVELS = 5
    const val MAX_MUSICBOX_LEVELS = 1
    const val MAX_MEMORYPUZZLE_LEVELS = 5
    const val MAX_FEEDKUZYA_LEVELS = 5
    const val MAX_SEASONS_LEVELS = 4
    const val MAX_KARAOKE_LEVELS = 1
    const val MAX_LEARNINGSONGS_LEVELS = 10

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

    fun init(context: Context) {
        if (!isInitialized) {
            prefs = context.getSharedPreferences("vasilisina_azbuka_progress", Context.MODE_PRIVATE)
            isInitialized = true
            checkDataMigration()
            updateDailyStreak()
        }
    }

    fun isLevelCompleted(gameId: String, stage: Int): Boolean {
        checkInitialized()
        return prefs.getBoolean("${gameId}_level_$stage", false)
    }

    fun completeLevel(gameId: String, stage: Int, stars: Int = 3) {
        checkInitialized()
        val editor = prefs.edit()
        editor.putBoolean("${gameId}_level_$stage", true)
        val existingStars = prefs.getInt("${gameId}_stars_level_$stage", 0)
        editor.putInt("${gameId}_stars_level_$stage", maxOf(existingStars, stars))
        val currentStars = prefs.getInt("${gameId}_stars", 0)
        if (stars > existingStars) editor.putInt("${gameId}_stars", currentStars + (stars - existingStars))
        val maxLevel = prefs.getInt("${gameId}_max_level", 0)
        if (stage > maxLevel) editor.putInt("${gameId}_max_level", stage)
        editor.putInt(PrefKeys.TOTAL_GAMES_PLAYED, prefs.getInt(PrefKeys.TOTAL_GAMES_PLAYED, 0) + 1)
        editor.putInt(PrefKeys.TOTAL_STARS, prefs.getInt(PrefKeys.TOTAL_STARS, 0) + stars - existingStars)
        checkAchievements(editor)
        editor.apply()
    }

    fun getTotalStars(gameId: String): Int { checkInitialized(); return prefs.getInt("${gameId}_stars", 0) }
    fun getOverallStars(): Int { checkInitialized(); return prefs.getInt(PrefKeys.TOTAL_STARS, 0) }
    fun getMaxLevel(gameId: String): Int { checkInitialized(); return prefs.getInt("${gameId}_max_level", 0) }

    fun getMaxLevels(gameId: String): Int = when (gameId) {
        "coloring" -> MAX_COLORING_LEVELS; "musicbox" -> MAX_MUSICBOX_LEVELS
        "memorypuzzle" -> MAX_MEMORYPUZZLE_LEVELS; "feedkuzya" -> MAX_FEEDKUZYA_LEVELS
        "seasons" -> MAX_SEASONS_LEVELS; "karaoke" -> MAX_KARAOKE_LEVELS
        "learningsongs" -> MAX_LEARNINGSONGS_LEVELS; else -> 5
    }

    fun isGameCompleted(gameId: String): Boolean {
        checkInitialized()
        return (1..getMaxLevels(gameId)).all { isLevelCompleted(gameId, it) }
    }

    fun resetGame(gameId: String) {
        checkInitialized()
        val editor = prefs.edit()
        editor.remove("${gameId}_stars"); editor.remove("${gameId}_max_level")
        for (i in 1..getMaxLevels(gameId)) { editor.remove("${gameId}_level_$i"); editor.remove("${gameId}_stars_level_$i") }
        editor.apply()
    }

    fun resetAllProgress() { checkInitialized(); prefs.edit().clear().apply(); prefs.edit().putInt(PrefKeys.DATA_VERSION, DATA_VERSION).apply() }

    fun getOverallProgress(): Map<String, Pair<Int, Int>> {
        checkInitialized()
        val games = listOf("coloring", "musicbox", "memorypuzzle", "feedkuzya", "seasons", "karaoke", "learningsongs")
        val maxLevels = listOf(MAX_COLORING_LEVELS, MAX_MUSICBOX_LEVELS, MAX_MEMORYPUZZLE_LEVELS, MAX_FEEDKUZYA_LEVELS, MAX_SEASONS_LEVELS, MAX_KARAOKE_LEVELS, MAX_LEARNINGSONGS_LEVELS)
        return games.zip(maxLevels).associate { (gameId, maxLevel) -> gameId to Pair((1..maxLevel).count { isLevelCompleted(gameId, it) }, maxLevel) }
    }

    fun getPlayerStats(): PlayerStats {
        checkInitialized()
        return PlayerStats(prefs.getInt(PrefKeys.TOTAL_GAMES_PLAYED, 0), prefs.getInt(PrefKeys.TOTAL_STARS, 0), prefs.getInt(PrefKeys.DAILY_STREAK, 0), listOf("coloring", "musicbox", "memorypuzzle", "feedkuzya", "seasons", "karaoke", "learningsongs").count { isGameCompleted(it) }, 7)
    }

    fun setMusicEnabled(enabled: Boolean) { checkInitialized(); prefs.edit().putBoolean(PrefKeys.SETTINGS_MUSIC, enabled).apply() }
    fun isMusicEnabled(): Boolean { checkInitialized(); return prefs.getBoolean(PrefKeys.SETTINGS_MUSIC, true) }
    fun setSfxEnabled(enabled: Boolean) { checkInitialized(); prefs.edit().putBoolean(PrefKeys.SETTINGS_SFX, enabled).apply() }
    fun isSfxEnabled(): Boolean { checkInitialized(); return prefs.getBoolean(PrefKeys.SETTINGS_SFX, true) }

    fun getAchievements(): List<Achievement> {
        checkInitialized()
        val json = prefs.getString(PrefKeys.ACHIEVEMENTS, null) ?: return Achievement.defaultAchievements
        return try { gson.fromJson(json, object : TypeToken<List<Achievement>>() {}.type) } catch (e: Exception) { Achievement.defaultAchievements }
    }

    private fun checkAchievements(editor: SharedPreferences.Editor) {
        val achievements = getAchievements().toMutableList()
        var updated = false
        achievements.forEachIndexed { index, a ->
            if (!a.isUnlocked) {
                val unlock = when (a.id) {
                    "first_level" -> true; "all_coloring" -> isGameCompleted("coloring"); "all_music" -> isGameCompleted("musicbox")
                    "all_puzzle" -> isGameCompleted("memorypuzzle"); "all_feeding" -> isGameCompleted("feedkuzya")
                    "all_seasons" -> isGameCompleted("seasons"); "all_karaoke" -> isGameCompleted("karaoke")
                    "all_learningsongs" -> isGameCompleted("learningsongs"); "ten_stars" -> getOverallStars() >= 10
                    "fifty_stars" -> getOverallStars() >= 50; "hundred_stars" -> getOverallStars() >= 100
                    "all_games" -> listOf("coloring","musicbox","memorypuzzle","feedkuzya","seasons","karaoke","learningsongs").all { isGameCompleted(it) }
                    else -> false
                }
                if (unlock) { achievements[index] = a.copy(isUnlocked = true, unlockDate = System.currentTimeMillis()); updated = true }
            }
        }
        if (updated) editor.putString(PrefKeys.ACHIEVEMENTS, gson.toJson(achievements))
    }

    private fun updateDailyStreak() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val last = prefs.getString(PrefKeys.LAST_LOGIN_DATE, null)
        val streak = prefs.getInt(PrefKeys.DAILY_STREAK, 0)
        val editor = prefs.edit()
        if (last == null) { editor.putString(PrefKeys.LAST_LOGIN_DATE, today); editor.putInt(PrefKeys.DAILY_STREAK, 1) }
        else if (last != today) {
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000))
            editor.putInt(PrefKeys.DAILY_STREAK, if (last == yesterday) streak + 1 else 1)
            editor.putString(PrefKeys.LAST_LOGIN_DATE, today)
        }
        editor.apply()
    }

    fun getDailyStreak(): Int { checkInitialized(); return prefs.getInt(PrefKeys.DAILY_STREAK, 0) }

    fun isFirstLaunch(): Boolean {
        checkInitialized()
        return if (prefs.getBoolean(PrefKeys.FIRST_LAUNCH, true)) { prefs.edit().putBoolean(PrefKeys.FIRST_LAUNCH, false).apply(); true } else false
    }

    fun setPlayerName(name: String) { checkInitialized(); prefs.edit().putString(PrefKeys.PLAYER_NAME, name).apply() }
    fun getPlayerName(): String { checkInitialized(); return prefs.getString(PrefKeys.PLAYER_NAME, "Игрок") ?: "Игрок" }

    // ✅ ВОТ ЭТА ФУНКЦИЯ — добавлена!
    private fun checkDataMigration() {
        if (prefs.getInt(PrefKeys.DATA_VERSION, 0) < DATA_VERSION) {
            val old = prefs.getInt("karaoke_max_level", 0)
            if (old > 0) {
                val editor = prefs.edit()
                for (i in 1..old) {
                    if (prefs.getBoolean("karaoke_level_$i", false)) {
                        editor.putBoolean("karaoke_level_$i", true)
                        editor.putInt("karaoke_stars_level_$i", prefs.getInt("karaoke_stars_level_$i", 3))
                    }
                }
                editor.putInt("karaoke_max_level", old).apply()
            }
            prefs.edit().putInt(PrefKeys.DATA_VERSION, DATA_VERSION).apply()
        }
    }

    private fun checkInitialized() { if (!isInitialized) throw IllegalStateException("GameState не инициализирован!") }

    data class GameStats(val gameId: String, val completedLevels: Int, val totalLevels: Int, val totalStars: Int, val maxPossibleStars: Int, val completionPercent: Float, val starPercent: Float)
    data class PlayerStats(val totalGamesPlayed: Int, val totalStars: Int, val dailyStreak: Int, val gamesCompleted: Int, val totalGames: Int)

    data class Achievement(val id: String, val title: String, val description: String, val emoji: String, val isUnlocked: Boolean = false, val unlockDate: Long = 0L) {
        companion object {
            val defaultAchievements = listOf(
                Achievement("first_level", "Первый шаг", "Пройти первый уровень", "🎯"),
                Achievement("all_coloring", "Художник", "Пройти всю раскраску", "🎨"),
                Achievement("all_music", "Музыкант", "Пройти музыкальную шкатулку", "🎵"),
                Achievement("all_puzzle", "Пазломастер", "Собрать все пазлы", "🧩"),
                Achievement("all_feeding", "Заботливый друг", "Накормить Кнопу", "🐱"),
                Achievement("all_seasons", "Натуралист", "Изучить все времена года", "🌍"),
                Achievement("all_karaoke", "Певец", "Посмотреть караоке", "🎤"),
                Achievement("all_learningsongs", "Мудрец", "Прослушать все песни", "🎶"),
                Achievement("ten_stars", "Звёздный начинающий", "Собрать 10 звёзд", "⭐"),
                Achievement("fifty_stars", "Звёздный мастер", "Собрать 50 звёзд", "🌟"),
                Achievement("hundred_stars", "Звёздный чемпион", "Собрать 100 звёзд", "💫"),
                Achievement("all_games", "Почётный гость Василисы", "Пройти все игры", "👸")
            )
        }
    }

    data class DailyBonus(val stars: Int, val streak: Int, val message: String)
}
