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
import androidx.compose.ui.graphics.Color
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

// Сезоны вынесены из функции (name → seasonName)
private enum class Season(val seasonName: String, val emoji: String, val color: Color) {
    WINTER("Зима", "❄️", Color(0xFFB3E5FC)),
    SPRING("Весна", "🌸", Color(0xFFC8E6C9)),
    SUMMER("Лето", "☀️", Color(0xFFFFF9C4)),
    AUTUMN("Осень", "🍂", Color(0xFFFFCCBC))
}

// Модель предмета вынесена из функции
private data class SeasonItem(
    val id: String,
    val itemName: String,
    val emoji: String,
    val correctSeason: Season
)

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
    val allItems = remember {
        listOf(
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
    }

    var availableItems by remember { mutableStateOf(allItems.shuffled().take(8)) }
    var placedItems by remember { mutableStateOf(mapOf<Season, List<SeasonItem>>()) }
    var selectedItem by remember { mutableStateOf<SeasonItem?>(null) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }

    LaunchedEffect(stage) {
        availableItems = allItems.shuffled().take(8)
        placedItems = Season.entries.associateWith { emptyList() }
        selectedItem = null
        showLevelComplete = false
    }

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

    val unplacedItems = availableItems.filter { item ->
        placedItems.values.none { list -> list.contains(item) }
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
                                if (selectedItem != null) {
                                    placedItems = placedItems.toMutableMap().apply {
                                        put(season, (get(season) ?: emptyList()) + selectedItem!!)
                                    }
                                    selectedItem = null
                                    AudioPlayer.playSFX(R.raw.sfx_drop)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedItem != null)
                                season.color.copy(alpha = 0.7f)
                            else
                                season.color.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = season.emoji, fontSize = 28.sp)
                            Text(
                                text = season.seasonName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            seasonItems.forEach { item ->
                                Text(text = item.emoji, fontSize = 24.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (selectedItem != null)
                "Выбран: ${selectedItem!!.emoji} ${selectedItem!!.itemName} — нажми на сезон!"
            else
                "Выбери предмет, затем нажми на сезон:",
            style = MaterialTheme.typography.bodyMedium,
            color = if (selectedItem != null) FairyGold else FairyPurple,
            fontWeight = if (selectedItem != null) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (unplacedItems.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.height(80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(unplacedItems) { item ->
                    val isSelected = selectedItem == item

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) FairyGold.copy(alpha = 0.5f)
                                else Color.White
                            )
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isSelected) FairyGold else FairyGold.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedItem = if (isSelected) null else item
                                AudioPlayer.playSFX(R.raw.sfx_click)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.emoji, fontSize = 28.sp)
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

        Button(
            onClick = {
                placedItems = Season.entries.associateWith { emptyList() }
                selectedItem = null
                AudioPlayer.playSFX(R.raw.sfx_reset)
            },
            modifier = Modifier.height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FairyPink)
        ) {
            Text("🔄 Заново", fontSize = 16.sp, color = Color.White)
        }
    }

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
