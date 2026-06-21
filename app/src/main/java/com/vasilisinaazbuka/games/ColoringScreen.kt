package com.vasilisinaazbuka.games

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vasilisinaazbuka.R
import com.vasilisinaazbuka.audio.AudioPlayer
import com.vasilisinaazbuka.data.GameState
import com.vasilisinaazbuka.ui.CharacterView
import com.vasilisinaazbuka.ui.LevelComplete
import com.vasilisinaazbuka.ui.StageProgressIndicator
import com.vasilisinaazbuka.ui.theme.*

@Composable
fun ColoringScreen(stage: Int = 1, onNextStage: () -> Unit = {}, onGameComplete: () -> Unit = {}, onBack: () -> Unit = {}) {
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }
    val zoneCount = 6
    val zoneColors = remember { mutableStateListOf(*Array(zoneCount) { Color.White }) }

    val colorPalette = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan, Color(0xFFFFA500), Color(0xFF8B4513))
    val levelImages = listOf(R.drawable.coloring_matryoshka, R.drawable.coloring_kremlin, R.drawable.coloring_samovar, R.drawable.coloring_birch, R.drawable.coloring_balalaika)
    val allZonesColored = zoneColors.all { it != Color.White }

    LaunchedEffect(stage) { zoneColors.replaceAll { Color.White }; selectedColor = Color.Red; showLevelComplete = false }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_level_coloring), "Фон", Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)

        Row(Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Левая панель — персонаж и управление (1/3)
            Column(Modifier.weight(0.33f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Раскраска", style = MaterialTheme.typography.titleLarge, color = FairyGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                StageProgressIndicator(currentStage = stage, maxStages = 5, compact = true)
                Spacer(Modifier.height(12.dp))

                CharacterView("vasilisa", "happy", "Раскрась картинку!\nВыбери цвет и нажми на зону.", Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))

                // Палитра цветов
                Text("Выбери цвет:", style = MaterialTheme.typography.bodySmall, color = FairyPurple)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    colorPalette.take(4).forEach { color ->
                        Box(Modifier.size(44.dp).clip(CircleShape).background(color).border(if (selectedColor == color) 3.dp else 1.dp, if (selectedColor == color) Color.Black else Color.Gray, CircleShape).clickable { selectedColor = color; AudioPlayer.playSFX(R.raw.sfx_click) })
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    colorPalette.drop(4).forEach { color ->
                        Box(Modifier.size(44.dp).clip(CircleShape).background(color).border(if (selectedColor == color) 3.dp else 1.dp, if (selectedColor == color) Color.Black else Color.Gray, CircleShape).clickable { selectedColor = color; AudioPlayer.playSFX(R.raw.sfx_click) })
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Кнопки управления
                Button(onClick = { selectedColor = Color.White; AudioPlayer.playSFX(R.raw.sfx_click) }, Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) { Text("🧹 Ластик", fontSize = 16.sp) }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { zoneColors.replaceAll { Color.White }; AudioPlayer.playSFX(R.raw.sfx_reset) }, Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPink)) { Text("🔄 Заново", fontSize = 16.sp, color = Color.White) }

                if (allZonesColored && !showLevelComplete) {
                    stars = 3; showLevelComplete = true; GameState.completeLevel("coloring", stage); AudioPlayer.playSFX(R.raw.sfx_success)
                }
            }

            Spacer(Modifier.width(12.dp))

            // Правая часть — раскраска (2/3)
            Card(Modifier.weight(0.67f).fillMaxHeight().aspectRatio(1f), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                Box(Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color.White).border(2.dp, FairyGold, RoundedCornerShape(16.dp))) {
                    Image(painterResource(levelImages.getOrElse(stage - 1) { R.drawable.coloring_matryoshka }), "Раскраска", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    Row(Modifier.fillMaxSize()) {
                        Column(Modifier.weight(1f)) {
                            repeat(3) { row ->
                                Box(Modifier.weight(1f).fillMaxWidth().padding(3.dp).background(zoneColors[row].copy(alpha = 0.5f), RoundedCornerShape(6.dp)).clickable { zoneColors[row] = selectedColor; AudioPlayer.playSFX(R.raw.sfx_paint) }.border(1.dp, Color.Gray, RoundedCornerShape(6.dp)))
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            repeat(3) { row ->
                                Box(Modifier.weight(1f).fillMaxWidth().padding(3.dp).background(zoneColors[row + 3].copy(alpha = 0.5f), RoundedCornerShape(6.dp)).clickable { zoneColors[row + 3] = selectedColor; AudioPlayer.playSFX(R.raw.sfx_paint) }.border(1.dp, Color.Gray, RoundedCornerShape(6.dp)))
                            }
                        }
                    }
                }
            }
        }

        if (showLevelComplete) LevelComplete(stars, "Отлично! Картинка раскрашена!", onNext = { if (stage < 5) onNextStage() else onGameComplete() })
    }
}
