package com.vasilisinaazbuka.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Маршруты навигации приложения «В гостях у Василисы»
 * Все маршруты определены как sealed class для типобезопасности
 * 
 * Поддерживает:
 * - Игра 1: Раскраска (5 уровней)
 * - Игра 2: Музыкальная шкатулка (1 уровень, 3 режима)
 * - Игра 3: Собери картинку по памяти (5 уровней)
 * - Игра 4: Накорми Кнопу (тамагочи, 5 уровней)
 * - Игра 5: Времена года (4 уровня)
 * - Игра 6: Караоке-читалка (видео)
 * - Игра 7: Поучительные песни (10 песен)
 */
sealed class Routes(val route: String) {

    // Главное меню
    data object Menu : Routes("menu")

    // Игра 1: Раскраска (5 уровней)
    data object Coloring : Routes("coloring/{stage}") {
        fun createRoute(stage: Int) = "coloring/$stage"
        fun arguments() = listOf(navArgument("stage") { type = NavType.IntType; defaultValue = 1 })
    }

    // Игра 2: Музыкальная шкатулка (1 уровень с 3 режимами)
    data object MusicBox : Routes("musicbox")

    // Игра 3: Собери картинку по памяти (5 уровней)
    data object MemoryPuzzle : Routes("memorypuzzle/{stage}") {
        fun createRoute(stage: Int) = "memorypuzzle/$stage"
        fun arguments() = listOf(navArgument("stage") { type = NavType.IntType; defaultValue = 1 })
    }

    // Игра 4: Накорми Кнопу (тамагочи, 5 уровней)
    data object FeedKuzya : Routes("feedkuzya/{stage}") {
        fun createRoute(stage: Int) = "feedkuzya/$stage"
        fun arguments() = listOf(navArgument("stage") { type = NavType.IntType; defaultValue = 1 })
    }

    // Игра 5: Времена года (4 уровня)
    data object Seasons : Routes("seasons/{stage}") {
        fun createRoute(stage: Int) = "seasons/$stage"
        fun arguments() = listOf(navArgument("stage") { type = NavType.IntType; defaultValue = 1 })
    }

    // Игра 6: Караоке-читалка (видео)
    data object Karaoke : Routes("karaoke/{songIndex}/{stage}") {
        fun createRoute(songIndex: Int, stage: Int = 1) = "karaoke/$songIndex/$stage"
        fun arguments() = listOf(navArgument("songIndex") { type = NavType.IntType; defaultValue = 1 }, navArgument("stage") { type = NavType.IntType; defaultValue = 1 })
    }

    // Игра 7: Поучительные песни (10 песен)
    data object LearningSongs : Routes("learningsongs/{songIndex}") {
        fun createRoute(songIndex: Int) = "learningsongs/$songIndex"
        fun arguments() = listOf(navArgument("songIndex") { type = NavType.IntType; defaultValue = 1 })
    }

    companion object {
        val allRoutes = listOf(Menu, Coloring, MusicBox, MemoryPuzzle, FeedKuzya, Seasons, Karaoke, LearningSongs)

        val gameItems = listOf(
            GameInfo("coloring", "🎨", "Раскраска", "Раскрась картинки из разных городов", 5, Coloring.createRoute(1)),
            GameInfo("musicbox", "🎵", "Музыкальная шкатулка", "Слушай, повторяй и угадывай звуки", 1, MusicBox.route),
            GameInfo("memorypuzzle", "🧩", "Собери картинку", "Запомни и собери пазл по памяти", 5, MemoryPuzzle.createRoute(1)),
            GameInfo("feedkuzya", "🐱", "Накорми Кнопу", "Ухаживай за котом-тамагочи", 5, FeedKuzya.createRoute(1)),
            GameInfo("seasons", "❄️", "Времена года", "Разложи предметы по сезонам", 4, Seasons.createRoute(1)),
            GameInfo("karaoke", "🎬", "Караоке-читалка", "Смотри и подпевай!", 1, Karaoke.createRoute(1)),
            GameInfo("learningsongs", "🎵", "Поучительные песни", "10 песен с вопросами", 10, LearningSongs.createRoute(1))
        )

        fun getMaxLevels(gameId: String) = gameItems.find { it.id == gameId }?.maxLevels ?: 5
        fun getGameInfo(gameId: String) = gameItems.find { it.id == gameId }
        fun isValidGameId(gameId: String) = gameItems.any { it.id == gameId }
    }

    data class GameInfo(val id: String, val emoji: String, val title: String, val description: String, val maxLevels: Int, val route: String)
}
