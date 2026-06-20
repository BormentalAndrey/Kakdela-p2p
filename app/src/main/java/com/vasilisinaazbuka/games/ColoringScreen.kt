package com.vasilisinaazbuka.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vasilisinaazbuka.R
import com.vasilisinaazbuka.audio.AudioPlayer
import com.vasilisinaazbuka.data.GameState
import com.vasilisinaazbuka.ui.CharacterView
import com.vasilisinaazbuka.ui.LevelComplete
import com.vasilisinaazbuka.ui.StageProgressIndicator
import com.vasilisinaazbuka.ui.StarDisplay
import com.vasilisinaazbuka.ui.theme.*

/**
 * Игра «Раскраска» — 5 уровней раскрашивания картинок по зонам
 *
 * @param stage Текущий этап (1-5)
 * @param onNextStage Переход к следующему этапу
 * @param onGameComplete Завершение игры (все 5 этапов пройдены)
 * @param onBack Навигация назад
 */
@Composable
fun ColoringScreen(
    stage: Int = 1,
    onNextStage: () -> Unit = {},
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Состояния
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }

    // Цвета для зон раскраски (каждая зона хранит свой цвет)
    val zoneCount = 6 // Количество зон в каждой картинке
    val zoneColors = remember { mutableStateListOf(*Array(zoneCount) { Color.White }) }

    // Палитра цветов
    val colorPalette = listOf(
        Color.Red, Color.Blue, Color.Green, Color.Yellow,
        Color.Magenta, Color.Cyan, Color(0xFFFFA500), Color(0xFF8B4513)
    )

    // Изображения для каждого уровня
    val levelImages = listOf(
        R.drawable.coloring_matryoshka,  // Матрёшка
        R.drawable.coloring_kremlin,     // Кремль
        R.drawable.coloring_samovar,     // Самовар
        R.drawable.coloring_birch,       // Берёзка
        R.drawable.coloring_balalaika    // Балалайка
    )

    // Проверка, все ли зоны закрашены
    val allZonesColored = zoneColors.all { it != Color.White }

    // Сброс состояния при смене этапа
    LaunchedEffect(stage) {
        zoneColors.replaceAll { Color.White }
        selectedColor = Color.Red
        showLevelComplete = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FairyBlue.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок и прогресс
            Text(
                text = "Раскраска",
                style = MaterialTheme.typography.headlineMedium,
                color = FairyGold,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Прогресс-бар этапов
            StageProgressIndicator(
                currentStage = stage,
                maxStages = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Персонаж Василиса
            CharacterView(
                character = "vasilisa",
                emotion = "happy",
                message = "Раскрась картинку! Выбери цвет и нажми на зону.",
                modifier = Modifier.height(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Область раскраски (упрощённая версия с Canvas)
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, FairyGold, RoundedCornerShape(16.dp))
            ) {
                // Фоновое изображение для раскраски
                AsyncImage(
                    model = levelImages.getOrElse(stage - 1) { R.drawable.coloring_matryoshka },
                    contentDescription = "Раскраска",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Зоны для раскрашивания (упрощённая модель — 6 прямоугольных зон)
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        repeat(3) { row ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .background(
                                        zoneColors[row].copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        zoneColors[row] = selectedColor
                                        AudioPlayer.playSFX(R.raw.sfx_paint)
                                    }
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        repeat(3) { row ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .background(
                                        zoneColors[row + 3].copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        zoneColors[row + 3] = selectedColor
                                        AudioPlayer.playSFX(R.raw.sfx_paint)
                                    }
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Палитра цветов
            Text(
                text = "Выбери цвет:",
                style = MaterialTheme.typography.titleMedium,
                color = FairyPurple
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colorPalette.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 4.dp else 2.dp,
                                color = if (selectedColor == color) Color.Black else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable {
                                selectedColor = color
                                AudioPlayer.playSFX(R.raw.sfx_click)
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопки управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Ластик (белый цвет)
                Button(
                    onClick = {
                        selectedColor = Color.White
                        AudioPlayer.playSFX(R.raw.sfx_click)
                    },
                    modifier = Modifier.height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "🧹 Ластик",
                        fontSize = 18.sp
                    )
                }

                // Заново
                Button(
                    onClick = {
                        zoneColors.replaceAll { Color.White }
                        AudioPlayer.playSFX(R.raw.sfx_reset)
                    },
                    modifier = Modifier.height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FairyPink)
                ) {
                    Text(
                        text = "🔄 Заново",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка «Далее» (активна, когда все зоны закрашены)
            if (allZonesColored && !showLevelComplete) {
                stars = 3 // Все зоны закрашены — 3 звезды
                showLevelComplete = true
                GameState.completeLevel("coloring", stage)
                AudioPlayer.playSFX(R.raw.sfx_success)
            }
        }

        // Окно завершения уровня
        if (showLevelComplete) {
            LevelComplete(
                stars = stars,
                message = "Отлично! Картинка раскрашена!",
                onNext = {
                    if (stage < 5) {
                        onNextStage()
                    } else {
                        onGameComplete()
                    }
                }
            )
        }
    }
}
