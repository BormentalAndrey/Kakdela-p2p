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
fun ColoringScreen(
    stage: Int = 1,
    onNextStage: () -> Unit = {},
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }
    val zoneCount = 6
    val zoneColors = remember { mutableStateListOf(*Array(zoneCount) { Color.White }) }

    val colorPalette = listOf(
        Color.Red, Color.Blue, Color.Green, Color.Yellow,
        Color.Magenta, Color.Cyan, Color(0xFFFFA500), Color(0xFF8B4513)
    )

    val levelImages = listOf(
        R.drawable.coloring_matryoshka,
        R.drawable.coloring_kremlin,
        R.drawable.coloring_samovar,
        R.drawable.coloring_birch,
        R.drawable.coloring_balalaika
    )

    val allZonesColored = zoneColors.all { it != Color.White }

    LaunchedEffect(stage) {
        zoneColors.replaceAll { Color.White }
        selectedColor = Color.Red
        showLevelComplete = false
    }

    Box(Modifier.fillMaxSize()) {
        // Фоновое изображение
        Image(
            painter = painterResource(R.drawable.bg_level_coloring),
            contentDescription = "Фон",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Раскраска", style = MaterialTheme.typography.headlineMedium, color = FairyGold, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            StageProgressIndicator(currentStage = stage, maxStages = 5)
            Spacer(Modifier.height(16.dp))

            CharacterView(character = "vasilisa", emotion = "happy", message = "Раскрась картинку! Выбери цвет и нажми на зону.", modifier = Modifier.height(100.dp))
            Spacer(Modifier.height(16.dp))

            // Область раскраски
            Box(
                Modifier.size(300.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).border(2.dp, FairyGold, RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(levelImages.getOrElse(stage - 1) { R.drawable.coloring_matryoshka }),
                    contentDescription = "Раскраска",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Row(Modifier.fillMaxSize()) {
                    Column(Modifier.weight(1f)) {
                        repeat(3) { row ->
                            Box(Modifier.weight(1f).fillMaxWidth().padding(4.dp).background(zoneColors[row].copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { zoneColors[row] = selectedColor; AudioPlayer.playSFX(R.raw.sfx_paint) }
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)))
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        repeat(3) { row ->
                            Box(Modifier.weight(1f).fillMaxWidth().padding(4.dp).background(zoneColors[row + 3].copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { zoneColors[row + 3] = selectedColor; AudioPlayer.playSFX(R.raw.sfx_paint) }
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Выбери цвет:", style = MaterialTheme.typography.titleMedium, color = FairyPurple)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                colorPalette.forEach { color ->
                    Box(Modifier.size(60.dp).clip(CircleShape).background(color)
                        .border(width = if (selectedColor == color) 4.dp else 2.dp, color = if (selectedColor == color) Color.Black else Color.Gray, shape = CircleShape)
                        .clickable { selectedColor = color; AudioPlayer.playSFX(R.raw.sfx_click) })
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { selectedColor = Color.White; AudioPlayer.playSFX(R.raw.sfx_click) }, Modifier.height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) { Text("🧹 Ластик", fontSize = 18.sp) }
                Button(onClick = { zoneColors.replaceAll { Color.White }; AudioPlayer.playSFX(R.raw.sfx_reset) }, Modifier.height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPink)) { Text("🔄 Заново", fontSize = 18.sp, color = Color.White) }
            }

            Spacer(Modifier.height(16.dp))

            if (allZonesColored && !showLevelComplete) {
                stars = 3
                showLevelComplete = true
                GameState.completeLevel("coloring", stage)
                AudioPlayer.playSFX(R.raw.sfx_success)
            }
        }

        if (showLevelComplete) {
            LevelComplete(stars = stars, message = "Отлично! Картинка раскрашена!", onNext = { if (stage < 5) onNextStage() else onGameComplete() })
        }
    }
}
