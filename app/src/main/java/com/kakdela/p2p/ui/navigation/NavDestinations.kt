package com.kakdela.p2p.ui.navigation

sealed class Screen(val route: String) {
    object Contacts : Screen("contacts")
    object Chat : Screen("chat/{chatId}") {
        fun route(chatId: String) = "chat/$chatId"
    }
}
