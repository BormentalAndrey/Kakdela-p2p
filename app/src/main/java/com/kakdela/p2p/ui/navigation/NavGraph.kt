package com.kakdela.p2p.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kakdela.p2p.ui.screens.ChatScreen
import com.kakdela.p2p.ui.screens.ContactsScreen

@Composable
fun AppNavGraph(startRoute: String = Screen.Contacts.route) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startRoute) {

        composable(Screen.Contacts.route) {
            ContactsScreen(
                onOpenChat = { chatId ->
                    navController.navigate(Screen.Chat.route(chatId))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: "unknown"
            ChatScreen(chatId = chatId, onBack = { navController.popBackStack() })
        }
    }
}
