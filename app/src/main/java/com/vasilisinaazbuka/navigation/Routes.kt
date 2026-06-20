package com.vasilisinaazbuka.navigation

/**
 * Маршруты навигации приложения «Василисина азбука»
 * Все маршруты определены как sealed class для типобезопасности
 */
sealed class Routes(val route: String) {

    // Главное меню
    data object Menu : Routes("menu")

    // Игра 1: Раскраска (5 уровней)
    data object Coloring : Routes("coloring/{stage}") {
        fun createRoute(stage: Int) = "coloring/$stage"
    }

    // Игра 2: Музыкальная шкатулка (1 уровень с 3 режимами)
    data object MusicBox : Routes("musicbox")

    // Игра 3: Собери картинку по памяти (5 уровней)
    data object MemoryPuzzle : Routes("memorypuzzle/{stage}") {
        fun createRoute(stage: Int) = "memorypuzzle/$stage"
    }

    // Игра 4: Накорми Кузю (5 уровней)
    data object FeedKuzya : Routes("feedkuzya/{stage}") {
        fun createRoute(stage: Int) = "feedkuzya/$stage"
    }

    // Игра 5: Времена года (4 уровня)
    data object Seasons : Routes("seasons/{stage}") {
        fun createRoute(stage: Int) = "seasons/$stage"
    }

    // Игра 6: Караоке-читалка (5 уровней)
    data object Karaoke : Routes("karaoke/{stage}") {
        fun createRoute(stage: Int) = "karaoke/$stage"
    }
}
