package com.vasilisinaazbuka

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vasilisinaazbuka.data.GameState
import com.vasilisinaazbuka.games.*
import com.vasilisinaazbuka.navigation.Routes
import com.vasilisinaazbuka.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun VasilisinaAzbukaApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Menu.route) {
        composable(Routes.Menu.route) { MainMenuScreen { navController.navigate(it) } }
        composable(Routes.ColoringSelect.route) { ColoringSelectScreen({ navController.navigate(Routes.Coloring.createRoute(it)) }, { navController.popBackStack() }) }
        composable(Routes.Coloring.route, arguments = Routes.Coloring.arguments()) { backStackEntry ->
            val stage = backStackEntry.arguments?.getInt("stage") ?: 1
            ColoringScreen(stage, { if (stage < GameState.MAX_COLORING_LEVELS) navController.navigate(Routes.Coloring.createRoute(stage + 1)) { popUpTo(Routes.ColoringSelect.route) } }, { navController.navigate(Routes.Menu.route) { popUpTo(Routes.Menu.route) { inclusive = true } } }, { navController.popBackStack() })
        }
        composable(Routes.MusicBox.route) { MusicBoxScreen({ navController.navigate(Routes.Menu.route) { popUpTo(Routes.Menu.route) { inclusive = true } } }, { navController.popBackStack() }) }
        composable(Routes.MemoryPuzzle.route, arguments = Routes.MemoryPuzzle.arguments()) { backStackEntry ->
            val stage = backStackEntry.arguments?.getInt("stage") ?: 1
            MemoryPuzzleScreen(stage, { if (stage < GameState.MAX_MEMORYPUZZLE_LEVELS) navController.navigate(Routes.MemoryPuzzle.createRoute(stage + 1)) { popUpTo(Routes.MemoryPuzzle.createRoute(stage)) { inclusive = true } } }, { navController.navigate(Routes.Menu.route) { popUpTo(Routes.Menu.route) { inclusive = true } } }, { navController.popBackStack() })
        }
        composable(Routes.FeedKuzya.route, arguments = Routes.FeedKuzya.arguments()) { backStackEntry ->
            val stage = backStackEntry.arguments?.getInt("stage") ?: 1
            FeedKuzyaScreen(stage, { if (stage < GameState.MAX_FEEDKUZYA_LEVELS) navController.navigate(Routes.FeedKuzya.createRoute(stage + 1)) { popUpTo(Routes.FeedKuzya.createRoute(stage)) { inclusive = true } } }, { navController.navigate(Routes.Menu.route) { popUpTo(Routes.Menu.route) { inclusive = true } } }, { navController.popBackStack() })
        }
        composable(Routes.Seasons.route, arguments = Routes.Seasons.arguments()) { backStackEntry ->
            val stage = backStackEntry.arguments?.getInt("stage") ?: 1
            SeasonsScreen(stage, { if (stage < GameState.MAX_SEASONS_LEVELS) navController.navigate(Routes.Seasons.createRoute(stage + 1)) { popUpTo(Routes.Seasons.createRoute(stage)) { inclusive = true } } }, { navController.navigate(Routes.Menu.route) { popUpTo(Routes.Menu.route) { inclusive = true } } }, { navController.popBackStack() })
        }
        composable(Routes.Karaoke.route, arguments = Routes.Karaoke.arguments()) { backStackEntry ->
            KaraokeScreen(songIndex = backStackEntry.arguments?.getInt("songIndex") ?: 1, stage = 1, onNextStage = {}, onNextSong = {}, onGameComplete = { navController.navigate(Routes.Menu.route) { popUpTo(Routes.Menu.route) { inclusive = true } } }, onBack = { navController.popBackStack() })
        }
        composable(Routes.LearningSongs.route, arguments = Routes.LearningSongs.arguments()) { backStackEntry ->
            val songIndex = backStackEntry.arguments?.getInt("songIndex") ?: 1
            LearningSongsScreen(songIndex = songIndex, onNextSong = { if (songIndex < 10) navController.navigate(Routes.LearningSongs.createRoute(songIndex + 1)) { popUpTo(Routes.LearningSongs.createRoute(songIndex)) { inclusive = true } } else navController.navigate(Routes.Menu.route) { popUpTo(Routes.Menu.route) { inclusive = true } } }, onGameComplete = { navController.navigate(Routes.Menu.route) { popUpTo(Routes.Menu.route) { inclusive = true } } }, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun MainMenuScreen(onGameSelected: (String) -> Unit) {
    val context = LocalContext.current
    val gameProgress = remember { try { GameState.getOverallProgress() } catch (e: IllegalStateException) { emptyMap() } }

    var knopaState by remember { mutableStateOf(try { KuzyaSaveManager.loadState(context) } catch (e: Exception) { null }) }
    LaunchedEffect(Unit) { while (true) { delay(5000); knopaState = try { KuzyaSaveManager.loadState(context) } catch (e: Exception) { null } } }

    val knopaMood = knopaState?.mood?.name?.lowercase() ?: "neutral"; val knopaIsSleeping = knopaState?.isSleeping ?: false
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val knopaWant = when {
        knopaIsSleeping -> "Спит..."; knopaState == null -> "Познакомься!"; knopaState!!.hunger < 30 -> "Хочу есть!"; knopaState!!.cleanliness < 30 -> "Хочу купаться!"; knopaState!!.happiness < 30 -> "Хочу играть!"; knopaState!!.energy < 30 -> "Хочу спать!"; knopaState!!.health < 30 -> "Мне плохо..."; hour in 20..21 -> "Пора купаться!"; hour in 21..23 || hour in 0..7 -> "Спит..."; knopaState!!.mood == KuzyaMood.ANGRY -> "Ты забыл про меня!"; knopaState!!.mood == KuzyaMood.ECSTATIC -> "Мур-мур-мур!"; knopaState!!.mood == KuzyaMood.HAPPY -> "Я счастлив!"; else -> "Всё хорошо!"
    }

    val knopaImg = when {
        knopaIsSleeping -> R.drawable.character_kuzya_sleeping; knopaMood == "ecstatic" -> R.drawable.character_kuzya_ecstatic; knopaMood == "happy" -> R.drawable.character_kuzya_happy; knopaMood == "playing" -> R.drawable.character_kuzya_playing; knopaMood == "hungry" -> R.drawable.character_kuzya_hungry; knopaMood == "sad" -> R.drawable.character_kuzya_sad; knopaMood == "sick" -> R.drawable.character_kuzya_sick; knopaMood == "dirty" -> R.drawable.character_kuzya_dirty; knopaMood == "angry" -> R.drawable.character_kuzya_angry; knopaMood == "sleepy" -> R.drawable.character_kuzya_sleeping; else -> R.drawable.character_kuzya_neutral
    }

    val vasilisaEmotions = listOf("happy", "proud", "teacher"); var vasilisaIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(4000); vasilisaIndex = (vasilisaIndex + 1) % vasilisaEmotions.size } }

    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(FairyBlue.copy(alpha = 0.15f), FairyPurple.copy(alpha = 0.05f), FairyBlue.copy(alpha = 0.1f))))) {
        Image(painterResource(R.drawable.bg_level_menu), "Фон", Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.25f)
        Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.35f)))

        Row(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {

            Column(Modifier.weight(1f).fillMaxHeight().padding(end = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = FairyGold.copy(alpha = 0.15f)), elevation = CardDefaults.cardElevation(4.dp)) {
                    Text("В гостях у\nВасилисы", Modifier.padding(14.dp), style = MaterialTheme.typography.titleMedium, color = FairyGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(16.dp))
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(6.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(60.dp).clip(CircleShape).background(Brush.radialGradient(listOf(FairyBlue.copy(alpha = 0.3f), FairyBlue.copy(alpha = 0.1f)))).border(3.dp, FairyBlue, CircleShape), contentAlignment = Alignment.Center) {
                                Image(painterResource(when (vasilisaEmotions[vasilisaIndex]) { "proud" -> R.drawable.character_vasilisa_proud; "teacher" -> R.drawable.character_vasilisa_teacher; else -> R.drawable.character_vasilisa_happy }), "Василиса", Modifier.fillMaxSize().padding(6.dp), contentScale = ContentScale.Fit)
                            }
                            Spacer(Modifier.height(6.dp)); Text("Василиса", fontWeight = FontWeight.Bold, color = FairyBlue, fontSize = 14.sp); Text("Твой учитель", color = Color.Gray, fontSize = 10.sp)
                        }
                        Box(Modifier.width(2.dp).height(50.dp).background(FairyGold.copy(alpha = 0.3f)))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(60.dp).clip(CircleShape).background(Brush.radialGradient(listOf(FairyPink.copy(alpha = 0.3f), FairyPink.copy(alpha = 0.1f)))).border(3.dp, FairyPink, CircleShape), contentAlignment = Alignment.Center) {
                                Image(painterResource(knopaImg), "Кнопа", Modifier.fillMaxSize().padding(6.dp), contentScale = ContentScale.Fit)
                            }
                            Spacer(Modifier.height(6.dp)); Text("Кнопа", fontWeight = FontWeight.Bold, color = FairyPink, fontSize = 14.sp); Text(knopaWant, color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 2)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                val totalStars = GameState.getOverallStars()
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = FairyGold.copy(alpha = 0.1f))) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Text("⭐", fontSize = 20.sp); Spacer(Modifier.width(6.dp)); Text("Всего звёзд: $totalStars", fontWeight = FontWeight.Bold, color = FairyGold, fontSize = 13.sp) }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val games = listOf(
                    GameMenuItem("🎨", "Раскраска", Routes.ColoringSelect.route, "coloring", "Раскрась картинки"),
                    GameMenuItem("🎵", "Музыкальная шкатулка", Routes.MusicBox.route, "musicbox", "Слушай и угадывай"),
                    GameMenuItem("🧩", "Собери картинку", Routes.MemoryPuzzle.createRoute(1), "memorypuzzle", "Пазлы по памяти"),
                    GameMenuItem("🐱", "Накорми Кнопу", Routes.FeedKuzya.createRoute(1), "feedkuzya", "Тамагочи с котом"),
                    GameMenuItem("❄️", "Времена года", Routes.Seasons.createRoute(1), "seasons", "Изучай сезоны"),
                    GameMenuItem("🎬", "Караоке", Routes.Karaoke.createRoute(1), "karaoke", "Смотри и подпевай"),
                    GameMenuItem("🎶", "Поучительные песни", Routes.LearningSongs.createRoute(1), "learningsongs", "10 песен")
                )

                for (row in listOf(0..2, 3..5, 6..6)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (col in row) {
                            if (col < games.size) {
                                val g = games[col]; val p = gameProgress[g.gameId]; val c = p?.first ?: 0
                                val t = p?.second ?: when (g.gameId) { "coloring" -> 5; "musicbox" -> 1; "memorypuzzle" -> 5; "feedkuzya" -> 5; "seasons" -> 4; "karaoke" -> 1; "learningsongs" -> 10; else -> 5 }
                                GameCard(g, c, t, c >= t, { onGameSelected(g.route) }, Modifier.weight(1f))
                            } else { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCard(game: GameMenuItem, completed: Int, total: Int, isCompleted: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(targetValue = if (isCompleted) 1.03f else 1f, animationSpec = spring(dampingRatio = 0.5f), label = "cardScale")
    val isSmall = game.gameId == "learningsongs"
    Card(
        modifier
            .then(if (isSmall) Modifier.aspectRatio(0.35f) else Modifier.aspectRatio(1.5f))
            .scale(scale).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCompleted) FairyGreen.copy(alpha = 0.08f) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(game.emoji, fontSize = if (isSmall) 24.sp else 32.sp)
            Spacer(Modifier.height(4.dp))
            Text(game.name, fontWeight = FontWeight.Bold, color = if (isCompleted) FairyGreen else FairyBlue, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp, fontSize = if (isSmall) 11.sp else 13.sp)
            Text(game.description, style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (total > 1) {
                Spacer(Modifier.height(2.dp))
                LinearProgressIndicator(progress = completed.toFloat() / total.toFloat(), Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = if (isCompleted) FairyGreen else FairyGold, trackColor = Color.Gray.copy(alpha = 0.15f))
                Text("$completed/$total", style = MaterialTheme.typography.labelSmall, color = if (isCompleted) FairyGreen else Color.Gray, fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal, fontSize = 9.sp)
            }
            if (isCompleted) Text("✅", fontSize = 14.sp)
        }
    }
}

private data class GameMenuItem(val emoji: String, val name: String, val route: String, val gameId: String, val description: String)
