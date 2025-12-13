package com.kakdela.p2p.ui.navigation

sealed class Screen(val route: String) {
    object Contacts : Screen("contacts")
    object Chat : Screen("chat/{peerId}") {
        fun route(peerId: String) = "chat/$peerId"
    }
    object MyQr : Screen("myQr")
    object QrScanner : Screen("qrScanner")
}
