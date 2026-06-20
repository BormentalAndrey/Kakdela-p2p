package com.vasilisinaazbuka

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vasilisinaazbuka.games.*
import com.vasilisinaazbuka.navigation.Routes

/**
 * Корневой композабл приложения с навигацией
 * Определяет все маршруты и связывает их с экранами игр
 */
@Composable
fun VasilisinaAzbukaApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Menu.route
    ) {
        // Главное меню
        composable(Routes.Menu.route) {
            MainMenuScreen(
                onGameSelected = { gameRoute ->
                    navController.navigate(gameRoute)
                }
            )
        }

        // Игра 1: Раскраска (5 уровней)
        composable(Routes.Coloring.route) { backStackEntry ->
            val stage = backStackEntry.arguments?.getString("stage")?.toIntOrNull() ?: 1
            ColoringScreen(
                stage = stage,
                onNextStage = {
                    navController.navigate(Routes.Coloring.createRoute(stage + 1)) {
                        popUpTo(Routes.Coloring.createRoute(stage)) { inclusive = true }
                    }
                },
                onGameComplete = {
                    navController.navigate(Routes.Menu.route) {
                        popUpTo(Routes.Menu.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Игра 2: Музыкальная шкатулка
        composable(Routes.MusicBox.route) {
            MusicBoxScreen(
                onGameComplete = {
                    navController.navigate(Routes.Menu.route) {
                        popUpTo(Routes.Menu.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Игра 3: Пазл по памяти (5 уровней)
        composable(Routes.MemoryPuzzle.route) { backStackEntry ->
            val stage = backStackEntry.arguments?.getString("stage")?.toIntOrNull() ?: 1
            MemoryPuzzleScreen(
                stage = stage,
                onNextStage = {
                    navController.navigate(Routes.MemoryPuzzle.createRoute(stage + 1)) {
                        popUpTo(Routes.MemoryPuzzle.createRoute(stage)) { inclusive = true }
                    }
                },
                onGameComplete = {
                    navController.navigate(Routes.Menu.route) {
                        popUpTo(Routes.Menu.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Игра 4: Накорми Кузю (5 уровней)
        composable(Routes.FeedKuzya.route) { backStackEntry ->
            val stage = backStackEntry.arguments?.getString("stage")?.toIntOrNull() ?: 1
            FeedKuzyaScreen(
                stage = stage,
                onNextStage = {
                    navController.navigate(Routes.FeedKuzya.createRoute(stage + 1)) {
                        popUpTo(Routes.FeedKuzya.createRoute(stage)) { inclusive = true }
                    }
                },
                onGameComplete = {
                    navController.navigate(Routes.Menu.route) {
                        popUpTo(Routes.Menu.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Игра 5: Времена года (4 уровня)
        composable(Routes.Seasons.route) { backStackEntry ->
            val stage = backStackEntry.arguments?.getString("stage")?.toIntOrNull() ?: 1
            SeasonsScreen(
                stage = stage,
                onNextStage = {
                    navController.navigate(Routes.Seasons.createRoute(stage + 1)) {
                        popUpTo(Routes.Seasons.createRoute(stage)) { inclusive = true }
                    }
                },
                onGameComplete = {
                    navController.navigate(Routes.Menu.route) {
                        popUpTo(Routes.Menu.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Игра 6: Караоке-читалка (5 уровней)
        composable(Routes.Karaoke.route) { backStackEntry ->
            val stage = backStackEntry.arguments?.getString("stage")?.toIntOrNull() ?: 1
            KaraokeScreen(
                stage = stage,
                onNextStage = {
                    navController.navigate(Routes.Karaoke.createRoute(stage + 1)) {
                        popUpTo(Routes.Karaoke.createRoute(stage)) { inclusive = true }
                    }
                },
                onGameComplete = {
                    navController.navigate(Routes.Menu.route) {
                        popUpTo(Routes.Menu.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Экран главного меню с выбором игр
 */
@Composable
fun MainMenuScreen(onGameSelected: (String) -> Unit) {
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.vasilisinaazbuka.navigation.Routes
    import com.vasilisinaazbuka.ui.theme.*

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Фоновое изображение с полупрозрачным слоем
                FairyBlue.copy(alpha = 0.1f)
            )
    ) {
        // Фоновое изображение
        Image(
            painter = painterResource(R.drawable.bg_level_menu),
            contentDescription = "Фон",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                // Заголовок
                Text(
                    text = "Василисина азбука",
                    style = MaterialTheme.typography.headlineLarge,
                    color = FairyGold,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Путешествие по России",
                    style = MaterialTheme.typography.titleLarge,
                    color = FairyPurple,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Список игр
                val games = listOf(
                    Triple("🎨", "Раскраска", Routes.Coloring.createRoute(1)),
                    Triple("🎵", "Музыкальная шкатулка", Routes.MusicBox.route),
                    Triple("🧩", "Собери картинку", Routes.MemoryPuzzle.createRoute(1)),
                    Triple("🍎", "Накорми Кузю", Routes.FeedKuzya.createRoute(1)),
                    Triple("❄️", "Времена года", Routes.Seasons.createRoute(1)),
                    Triple("📖", "Караоке-читалка", Routes.Karaoke.createRoute(1))
                )

                games.forEach { (emoji, name, route) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onGameSelected(route) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 40.sp
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = FairyGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
