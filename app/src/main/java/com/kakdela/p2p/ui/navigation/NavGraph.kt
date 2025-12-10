package com.kakdela.p2p.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kakdela.p2p.ui.screens.ChatScreen
import com.kakdela.p2p.ui.screens.SettingsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "chat") {
        composable("chat") { ChatScreen() }
        composable("settings") { SettingsScreen() }
    }
}
