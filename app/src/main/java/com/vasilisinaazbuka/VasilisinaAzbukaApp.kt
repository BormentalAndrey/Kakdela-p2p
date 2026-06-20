package com.vasilisinaazbuka

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vasilisinaazbuka.games.*
import com.vasilisinaazbuka.navigation.Routes
import com.vasilisinaazbuka.ui.theme.*

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
        composable(
            route = Routes.Coloring.route,
            arguments = listOf(
                navArgument("stage") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val stage = backStackEntry.arguments?.getInt("stage") ?: 1
            ColoringScreen(
                stage = stage,
                onNextStage = {
                    if (stage < GameState.MAX_COLORING_LEVELS) {
                        navController.navigate(Routes.Coloring.createRoute(stage + 1)) {
                            popUpTo(Routes.Coloring.createRoute(stage)) { inclusive = true }
                        }
                    }
                },
                onGameComplete = {
                    navController.navigate(Routes.Menu.route) {
                        popUpTo(Routes.Menu.route) { inclusive = true }
                    }
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Menu.route)
                    }
                }
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
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Menu.route)
                    }
                }
            )
        }

        // Игра 3: Пазл по памяти (5 уровней)
        composable(
            route = Routes.MemoryPuzzle.route,
            arguments = listOf(
                navArgument("stage") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val stage = backStackEntry.arguments?.getInt("stage") ?: 1
            MemoryPuzzleScreen(
                stage = stage,
                onNextStage = {
                    if (stage < GameState.MAX_MEMORYPUZZLE_LEVELS) {
                        navController.navigate(Routes.MemoryPuzzle.createRoute(stage + 1)) {
                            popUpTo(Routes.MemoryPuzzle.createRoute(stage)) { inclusive = true }
                        }
                    }
                },
                onGameComplete = {
                    navController.navigate(Routes.Menu.route) {
                        popUpTo(Routes.Menu.route) { inclusive = true }
                    }
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Menu.route)
                    }
                }
            )
        }

        // Игра 4: Накорми Кузю (5 уровней)
        composable(
            route = Routes.FeedKuzya.route,
            arguments = listOf(
                navArgument("stage") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val stage = backStackEntry.arguments?.getInt("stage") ?: 1
            FeedKuzyaScreen(
                stage = stage,
                onNextStage = {
                    if (stage < GameState.MAX_FEEDKUZYA_LEVELS) {
                        navController.navigate(Routes.FeedKuzya.createRoute(stage + 1)) {
                            popUpTo(Routes.FeedKuzya.createRoute(stage)) { inclusive = true }
                        }
                    }
                },
                onGameComplete = {
                    navController.navigate(Routes.Menu.route) {
                        popUpTo(Routes.Menu.route) { inclusive = true }
                    }
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Menu.route)
                    }
                }
            )
        }

        // Игра 5: Времена года (4 уровня)
        composable(
            route = Routes.Seasons.route,
            arguments = listOf(
                navArgument("stage") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val stage = backStackEntry.arguments?.getInt("stage") ?: 1
            SeasonsScreen(
                stage = stage,
                onNextStage = {
                    if (stage < GameState.MAX_SEASONS_LEVELS) {
                        navController.navigate(Routes.Seasons.createRoute(stage + 1)) {
                            popUpTo(Routes.Seasons.createRoute(stage)) { inclusive = true }
                        }
                    }
                },
                onGameComplete = {
                    navController.navigate(Routes.Menu.route) {
                        popUpTo(Routes.Menu.route) { inclusive = true }
                    }
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Menu.route)
                    }
                }
            )
        }

        // Игра 6: Караоке-читалка (20 песен, 5 уровней сложности)
        composable(
            route = Routes.Karaoke.route,
            arguments = listOf(
                navArgument("songIndex") {
                    type = NavType.IntType
                    defaultValue = 1
                },
                navArgument("stage") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val songIndex = backStackEntry.arguments?.getInt("songIndex") ?: 1
            val stage = backStackEntry.arguments?.getInt("stage") ?: 1
            
            KaraokeScreen(
                songIndex = songIndex,
                stage = stage,
                onNextStage = {
                    if (stage < 5) {
                        navController.navigate(Routes.Karaoke.createRoute(songIndex, stage + 1)) {
                            popUpTo(Routes.Karaoke.createRoute(songIndex, stage)) { inclusive = true }
                        }
                    }
                },
                onNextSong = {
                    if (songIndex < 20) {
                        navController.navigate(Routes.Karaoke.createRoute(songIndex + 1, 1)) {
                            popUpTo(Routes.Karaoke.createRoute(songIndex, stage)) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.Menu.route) {
                            popUpTo(Routes.Menu.route) { inclusive = true }
                        }
                    }
                },
                onGameComplete = {
                    navController.navigate(Routes.Menu.route) {
                        popUpTo(Routes.Menu.route) { inclusive = true }
                    }
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Menu.route)
                    }
                }
            )
        }
    }
}

/**
 * Экран главного меню с выбором игр
 * Отображает все доступные игры с прогрессом и навигацией
 */
@Composable
fun MainMenuScreen(onGameSelected: (String) -> Unit) {
    // Получаем прогресс по всем играм
    val gameProgress = remember {
        try {
            GameState.getOverallProgress()
        } catch (e: IllegalStateException) {
            emptyMap()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
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

        // Полупрозрачный белый слой для читаемости
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.4f))
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

                // Список игр с прогрессом
                val games = listOf(
                    GameMenuItem(
                        emoji = "🎨",
                        name = "Раскраска",
                        route = Routes.Coloring.createRoute(1),
                        gameId = "coloring",
                        description = "Раскрась картинки из разных городов"
                    ),
                    GameMenuItem(
                        emoji = "🎵",
                        name = "Музыкальная шкатулка",
                        route = Routes.MusicBox.route,
                        gameId = "musicbox",
                        description = "Слушай, повторяй и угадывай звуки"
                    ),
                    GameMenuItem(
                        emoji = "🧩",
                        name = "Собери картинку",
                        route = Routes.MemoryPuzzle.createRoute(1),
                        gameId = "memorypuzzle",
                        description = "Запомни и собери пазл"
                    ),
                    GameMenuItem(
                        emoji = "🍎",
                        name = "Накорми Кузю",
                        route = Routes.FeedKuzya.createRoute(1),
                        gameId = "feedkuzya",
                        description = "Посчитай продукты для Кузи"
                    ),
                    GameMenuItem(
                        emoji = "❄️",
                        name = "Времена года",
                        route = Routes.Seasons.createRoute(1),
                        gameId = "seasons",
                        description = "Разложи предметы по сезонам"
                    ),
                    GameMenuItem(
                        emoji = "📖",
                        name = "Караоке-читалка",
                        route = Routes.Karaoke.createRoute(1),
                        gameId = "karaoke",
                        description = "Пой и читай по слогам"
                    )
                )

                games.forEach { game ->
                    val progress = gameProgress[game.gameId]
                    val completedLevels = progress?.first ?: 0
                    val totalLevels = progress?.second ?: when (game.gameId) {
                        "coloring" -> GameState.MAX_COLORING_LEVELS
                        "musicbox" -> GameState.MAX_MUSICBOX_LEVELS
                        "memorypuzzle" -> GameState.MAX_MEMORYPUZZLE_LEVELS
                        "feedkuzya" -> GameState.MAX_FEEDKUZYA_LEVELS
                        "seasons" -> GameState.MAX_SEASONS_LEVELS
                        "karaoke" -> GameState.MAX_KARAOKE_LEVELS
                        else -> 5
                    }
                    val isCompleted = completedLevels >= totalLevels

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onGameSelected(game.route) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCompleted) 
                                FairyGreen.copy(alpha = 0.1f) 
                            else 
                                Color.White
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Эмодзи игры
                            Text(
                                text = game.emoji,
                                fontSize = 40.sp
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Информация об игре
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = game.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) FairyGreen else FairyBlue
                                )

                                Text(
                                    text = game.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Прогресс игры
                                if (totalLevels > 1) {
                                    LinearProgressIndicator(
                                        progress = { 
                                            if (totalLevels > 0) completedLevels.toFloat() / totalLevels 
                                            else 0f 
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (isCompleted) FairyGreen else FairyGold,
                                        trackColor = Color.Gray.copy(alpha = 0.2f)
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "$completedLevels из $totalLevels уровней",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isCompleted) FairyGreen else Color.Gray
                                    )
                                }
                            }

                            // Индикатор завершения
                            if (isCompleted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✅",
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Информация о персонажах
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FairyGold.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "👧", fontSize = 32.sp)
                            Text(
                                text = "Василиса",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = FairyBlue
                            )
                            Text(
                                text = "Твой учитель",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🐱", fontSize = 32.sp)
                            Text(
                                text = "Кузя",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = FairyPink
                            )
                            Text(
                                text = "Твой друг",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Модель данных для элемента меню игры
 */
private data class GameMenuItem(
    val emoji: String,
    val name: String,
    val route: String,
    val gameId: String,
    val description: String
)
