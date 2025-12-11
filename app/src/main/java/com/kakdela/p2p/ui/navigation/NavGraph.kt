package com.kakdela.p2p.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kakdela.p2p.ui.screens.ChatScreen
import com.kakdela.p2p.ui.screens.ContactsScreen
import com.kakdela.p2p.ui.screens.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "contacts"  // ← теперь стартуем со списка контактов
    ) {
        composable("contacts") {
            ContactsScreen(navController = navController)
        }

        composable("chat/{peerId}") { backStackEntry ->
            val peerId = backStackEntry.toRoute<ChatRoute>().peerId
            ChatScreen(peerId = peerId)  // передаём ID контакта в чат
        }

        composable("settings") {
            SettingsScreen()
        }
    }
}

// Это нужно для безопасной передачи строки в маршрут
@kotlinx.serialization.Serializable
data class ChatRoute(val peerId: String)
