package com.vasilisinaazbuka.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Маршруты навигации приложения «Василисина азбука»
 * Все маршруты определены как sealed class для типобезопасности
 * 
 * Поддерживает:
 * - Игра 1: Раскраска (5 уровней)
 * - Игра 2: Музыкальная шкатулка (1 уровень, 3 режима)
 * - Игра 3: Собери картинку по памяти (5 уровней)
 * - Игра 4: Накорми Кузю (5 уровней)
 * - Игра 5: Времена года (4 уровня)
 * - Игра 6: Караоке-читалка (20 песен, до 5 этапов на песню)
 */
sealed class Routes(val route: String) {

    // Главное меню
    data object Menu : Routes("menu")

    // Игра 1: Раскраска (5 уровней)
    data object Coloring : Routes("coloring/{stage}") {
        fun createRoute(stage: Int) = "coloring/$stage"
        
        /**
         * Аргументы для навигации с параметром stage
         */
        fun arguments() = listOf(
            navArgument("stage") {
                type = NavType.IntType
                defaultValue = 1
            }
        )
    }

    // Игра 2: Музыкальная шкатулка (1 уровень с 3 режимами)
    data object MusicBox : Routes("musicbox")

    // Игра 3: Собери картинку по памяти (5 уровней)
    data object MemoryPuzzle : Routes("memorypuzzle/{stage}") {
        fun createRoute(stage: Int) = "memorypuzzle/$stage"
        
        fun arguments() = listOf(
            navArgument("stage") {
                type = NavType.IntType
                defaultValue = 1
            }
        )
    }

    // Игра 4: Накорми Кузю (5 уровней)
    data object FeedKuzya : Routes("feedkuzya/{stage}") {
        fun createRoute(stage: Int) = "feedkuzya/$stage"
        
        fun arguments() = listOf(
            navArgument("stage") {
                type = NavType.IntType
                defaultValue = 1
            }
        )
    }

    // Игра 5: Времена года (4 уровня)
    data object Seasons : Routes("seasons/{stage}") {
        fun createRoute(stage: Int) = "seasons/$stage"
        
        fun arguments() = listOf(
            navArgument("stage") {
                type = NavType.IntType
                defaultValue = 1
            }
        )
    }

    // Игра 6: Караоке-читалка (20 песен, до 5 этапов на песню)
    data object Karaoke : Routes("karaoke/{songIndex}/{stage}") {
        fun createRoute(songIndex: Int, stage: Int = 1) = "karaoke/$songIndex/$stage"
        
        fun arguments() = listOf(
            navArgument("songIndex") {
                type = NavType.IntType
                defaultValue = 1
            },
            navArgument("stage") {
                type = NavType.IntType
                defaultValue = 1
            }
        )
    }

    companion object {
        /**
         * Список всех маршрутов для удобного перебора
         */
        val allRoutes = listOf(
            Menu,
            Coloring,
            MusicBox,
            MemoryPuzzle,
            FeedKuzya,
            Seasons,
            Karaoke
        )

        /**
         * Список игр с их метаданными для отображения в меню
         */
        val gameItems = listOf(
            GameInfo(
                id = "coloring",
                emoji = "🎨",
                title = "Раскраска",
                description = "Раскрась картинки из разных городов",
                maxLevels = 5,
                route = Coloring.createRoute(1)
            ),
            GameInfo(
                id = "musicbox",
                emoji = "🎵",
                title = "Музыкальная шкатулка",
                description = "Слушай, повторяй и угадывай звуки",
                maxLevels = 1,
                route = MusicBox.route
            ),
            GameInfo(
                id = "memorypuzzle",
                emoji = "🧩",
                title = "Собери картинку",
                description = "Запомни и собери пазл по памяти",
                maxLevels = 5,
                route = MemoryPuzzle.createRoute(1)
            ),
            GameInfo(
                id = "feedkuzya",
                emoji = "🍎",
                title = "Накорми Кузю",
                description = "Посчитай продукты для кота Кузи",
                maxLevels = 5,
                route = FeedKuzya.createRoute(1)
            ),
            GameInfo(
                id = "seasons",
                emoji = "❄️",
                title = "Времена года",
                description = "Разложи предметы по сезонам",
                maxLevels = 4,
                route = Seasons.createRoute(1)
            ),
            GameInfo(
                id = "karaoke",
                emoji = "📖",
                title = "Караоке-читалка",
                description = "Пой и читай по слогам вместе с Василисой",
                maxLevels = 20,
                route = Karaoke.createRoute(1)
            )
        )

        /**
         * Получить максимальное количество уровней для игры по её ID
         */
        fun getMaxLevels(gameId: String): Int {
            return gameItems.find { it.id == gameId }?.maxLevels ?: 5
        }

        /**
         * Получить информацию об игре по её ID
         */
        fun getGameInfo(gameId: String): GameInfo? {
            return gameItems.find { it.id == gameId }
        }

        /**
         * Проверить, существует ли маршрут для данной игры
         */
        fun isValidGameId(gameId: String): Boolean {
            return gameItems.any { it.id == gameId }
        }
    }

    /**
     * Информация об игре для отображения в меню
     */
    data class GameInfo(
        val id: String,
        val emoji: String,
        val title: String,
        val description: String,
        val maxLevels: Int,
        val route: String
    )
}
