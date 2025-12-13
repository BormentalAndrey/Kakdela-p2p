package com.kakdela.p2p

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.kakdela.p2p.ui.navigation.NavGraph
import com.kakdela.p2p.ui.theme.Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.instance = application as App
        setContent {
            Theme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
