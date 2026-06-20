package com.vasilisinaazbuka.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Игра «Времена года» — 4 уровня с распределением предметов по сезонам
 */
@Composable
fun SeasonsScreen(
    stage: Int = 1,
    onNextStage: () -> Unit = {},
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Сезоны
    enum class Season(val name: String, val emoji: String, val color: androidx.compose.ui.graphics.Color) {
        WINTER("Зима", "❄️", Color(0xFFB3E5FC)),
        SPRING("Весна", "🌸", Color(0xFFC8E6C9)),
        SUMMER("Лето", "☀️", Color(0xFFFFF9C4)),
        AUTUMN("Осень", "🍂", Color(0xFFFFCCBC))
    }

    // Предметы для распределения
    data class SeasonItem(val id: String, val name: String, val emoji: String, val correctSeason: Season)

    val allItems = listOf(
        SeasonItem("sled", "Санки", "🛷", Season.WINTER),
        SeasonItem("snowman", "Снеговик", "⛄", Season.WINTER),
        SeasonItem("hat", "Шапка", "🎩", Season.WINTER),
        SeasonItem("flower", "Цветок", "🌸", Season.SPRING),
        SeasonItem("umbrella", "Зонт", "☂️", Season.SPRING),
        SeasonItem("leaf", "Лист", "🍃", Season.SPRING),
        SeasonItem("ball", "Мяч", "⚽", Season.SUMMER),
        SeasonItem("swimsuit", "Купальник", "🩱", Season.SUMMER),
        SeasonItem("mushroom", "Гриб", "🍄", Season.AUTUMN),
        SeasonItem("pumpkin", "Тыква", "🎃", Season.AUTUMN)
    )

    // Перемешанные предметы для текущего уровня
    var availableItems by remember { mutableStateOf(allItems.shuffled().take(8)) }
    var placedItems by remember { mutableStateOf<Map<Season, MutableList<SeasonItem>>>(emptyMap()) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }

    // Инициализация
    LaunchedEffect(stage) {
        availableItems = allItems.shuffled().take(8)
        placedItems = Season.entries.associateWith { mutableListOf() }
        showLevelComplete = false
    }

    // Проверка правильности размещения
    val allPlaced = placedItems.values.sumOf { it.size } == availableItems.size

    LaunchedEffect(allPlaced) {
        if (allPlaced && !showLevelComplete) {
            val correctCount = placedItems.entries.sumOf { (season, items) ->
                items.count { it.correctSeason == season }
            }
            stars = when {
                correctCount == availableItems.size -> 3
                correctCount >= availableItems.size - 2 -> 2
                else -> 1
            }
            showLevelComplete = true
            AudioPlayer.playSFX(R.raw.sfx_success)
            GameState.completeLevel("seasons", stage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FairyBlue.copy(alpha = 0.1f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Времена года",
            style = MaterialTheme.typography.headlineMedium,
            color = FairyGold,
            fontWeight = FontWeight.Bold
        )

        StageProgressIndicator(currentStage = stage, maxStages = 4)

        Spacer(modifier = Modifier.height(8.dp))

        CharacterView(
            character = "vasilisa",
            emotion = "teacher",
            message = "Разложи предметы по временам года!",
            modifier = Modifier.height(80.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4 зоны сезонов
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Season.entries.forEach { season ->
                val seasonItems = placedItems[season] ?: emptyList()

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.8f)
                            .clickable {
                                // При нажатии на зону — можно добавить предмет (опционально)
                            },
                        colors = CardDefaults.cardColors(containerColor = season.color.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = season.emoji, fontSize = 28.sp)
                            Text(
                                text = season.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Показ размещённых предметов
                            seasonItems.forEach { item ->
                                Text(
                                    text = item.emoji,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Доступные предметы
        Text(
            text = "Доступные предметы (нажми на предмет, затем на сезон):",
            style = MaterialTheme.typography.bodyMedium,
            color = FairyPurple
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопки предметов
        val unplacedItems = availableItems.filter { item ->
            placedItems.values.none { list -> list.contains(item) }
        }

        if (unplacedItems.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(unplacedItems) { item ->
                    var selectedItem by remember { mutableStateOf<SeasonItem?>(null) }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedItem == item) FairyGold.copy(alpha = 0.5f)
                                else Color.White
                            )
                            .border(2.dp, FairyGold, RoundedCornerShape(12.dp))
                            .clickable {
                                selectedItem = item
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.emoji, fontSize = 32.sp)
                    }

                    // Здесь нужно реализовать логику выбора предмета и размещения в сезон
                    // Для упрощения: используем дополнительную кнопку
                }
            }

            // Временное решение: кнопки для размещения
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Season.entries.forEach { season ->
                    Button(
                        onClick = {
                            val item = unplacedItems.firstOrNull()
                            if (item != null) {
                                placedItems = placedItems.toMutableMap().apply {
                                    get(season)?.add(item)
                                }
                                AudioPlayer.playSFX(R.raw.sfx_drop)
                            }
                        },
                        modifier = Modifier.height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = season.color)
                    ) {
                        Text("В ${season.name}", fontSize = 14.sp)
                    }
                }
            }
        } else if (!showLevelComplete) {
            Text(
                text = "Все предметы размещены! Проверяем...",
                style = MaterialTheme.typography.bodyLarge,
                color = FairyGreen,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Кнопка сброса
        Button(
            onClick = {
                placedItems = Season.entries.associateWith { mutableListOf() }
                AudioPlayer.playSFX(R.raw.sfx_reset)
            },
            modifier = Modifier.height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FairyPink)
        ) {
            Text("🔄 Заново", fontSize = 18.sp, color = Color.White)
        }
    }

    // Окно завершения
    if (showLevelComplete) {
        LevelComplete(
            stars = stars,
            message = "Времена года изучены!",
            onNext = {
                if (stage < 4) onNextStage() else onGameComplete()
            }
        )
    }
}
