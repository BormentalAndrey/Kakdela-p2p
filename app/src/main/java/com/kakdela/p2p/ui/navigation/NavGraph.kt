package com.kakdela.p2p.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kakdela.p2p.ui.screens.ChatScreen
import com.kakdela.p2p.ui.screens.ContactsScreen
import com.kakdela.p2p.ui.screens.MyQrScreen
import com.kakdela.p2p.ui.screens.QrScannerScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Contacts.route) {
        composable(Screen.Contacts.route) {
            ContactsScreen(onOpenChat = { peerId ->
                navController.navigate(Screen.Chat.route(peerId))
            })
        }
        composable(Screen.Chat.route) { backStackEntry ->
            val peerId = backStackEntry.arguments?.getString("peerId") ?: ""
            ChatScreen(peerId = peerId, onBack = { navController.popBackStack() })
        }
        composable(Screen.MyQr.route) {
            MyQrScreen()
        }
        composable(Screen.QrScanner.route) {
            QrScannerScreen()
        }
    }
}
