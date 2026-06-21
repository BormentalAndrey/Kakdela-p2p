package com.vasilisinaazbuka.games

import androidx.compose.foundation.Image
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

private enum class Season(val seasonName: String, val emoji: String, val color: Color) {
    WINTER("Зима", "❄️", Color(0xFFB3E5FC)), SPRING("Весна", "🌸", Color(0xFFC8E6C9)),
    SUMMER("Лето", "☀️", Color(0xFFFFF9C4)), AUTUMN("Осень", "🍂", Color(0xFFFFCCBC))
}
private data class SeasonItem(val id: String, val itemName: String, val emoji: String, val correctSeason: Season)

@Composable
fun SeasonsScreen(stage: Int = 1, onNextStage: () -> Unit = {}, onGameComplete: () -> Unit = {}, onBack: () -> Unit = {}) {
    val allItems = remember { listOf(SeasonItem("sled", "Санки", "🛷", Season.WINTER), SeasonItem("snowman", "Снеговик", "⛄", Season.WINTER), SeasonItem("hat", "Шапка", "🎩", Season.WINTER), SeasonItem("flower", "Цветок", "🌸", Season.SPRING), SeasonItem("umbrella", "Зонт", "☂️", Season.SPRING), SeasonItem("leaf", "Лист", "🍃", Season.SPRING), SeasonItem("ball", "Мяч", "⚽", Season.SUMMER), SeasonItem("swimsuit", "Купальник", "🩱", Season.SUMMER), SeasonItem("mushroom", "Гриб", "🍄", Season.AUTUMN), SeasonItem("pumpkin", "Тыква", "🎃", Season.AUTUMN)) }
    var availableItems by remember { mutableStateOf(allItems.shuffled().take(8)) }
    var placedItems by remember { mutableStateOf(mapOf<Season, List<SeasonItem>>()) }
    var selectedItem by remember { mutableStateOf<SeasonItem?>(null) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }

    LaunchedEffect(stage) { availableItems = allItems.shuffled().take(8); placedItems = Season.entries.associateWith { emptyList() }; selectedItem = null; showLevelComplete = false }
    val allPlaced = placedItems.values.sumOf { it.size } == availableItems.size

    LaunchedEffect(allPlaced) { if (allPlaced && !showLevelComplete) { val correctCount = placedItems.entries.sumOf { (season, items) -> items.count { it.correctSeason == season } }; stars = when { correctCount == availableItems.size -> 3; correctCount >= availableItems.size - 2 -> 2; else -> 1 }; showLevelComplete = true; AudioPlayer.playSFX(R.raw.sfx_success); GameState.completeLevel("seasons", stage) } }

    val unplacedItems = availableItems.filter { item -> placedItems.values.none { list -> list.contains(item) } }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_level_seasons), "Фон", Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)

        // Кнопка «Назад» справа вверху
        Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.TopEnd) {
            Button(onClick = onBack, Modifier.size(44.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyBlue.copy(alpha = 0.7f)), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(0.dp)) { Text("↩", fontSize = 18.sp, color = Color.White) }
        }

        Row(Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(0.33f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Времена года", style = MaterialTheme.typography.titleLarge, color = FairyGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp)); StageProgressIndicator(currentStage = stage, maxStages = 4, compact = true); Spacer(Modifier.height(12.dp))
                CharacterView("vasilisa", "teacher", "Разложи предметы\nпо временам года!", Modifier.fillMaxWidth()); Spacer(Modifier.height(12.dp))
                Text(if (selectedItem != null) "Выбран: ${selectedItem!!.emoji} ${selectedItem!!.itemName}\nНажми на сезон!" else "Выбери предмет,\nзатем нажми на сезон:", style = MaterialTheme.typography.bodySmall, color = if (selectedItem != null) FairyGold else FairyPurple, fontWeight = if (selectedItem != null) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center); Spacer(Modifier.height(8.dp))

                if (unplacedItems.isNotEmpty()) LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(120.dp), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(unplacedItems) { item -> val isSelected = selectedItem == item; Box(Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(if (isSelected) FairyGold.copy(alpha = 0.5f) else Color.White).border(if (isSelected) 3.dp else 2.dp, if (isSelected) FairyGold else FairyGold.copy(alpha = 0.3f), RoundedCornerShape(10.dp)).clickable { selectedItem = if (isSelected) null else item; AudioPlayer.playSFX(R.raw.sfx_click) }, contentAlignment = Alignment.Center) { Text(item.emoji, fontSize = 24.sp) } } }
                else if (!showLevelComplete) Text("Все предметы размещены!\nПроверяем...", style = MaterialTheme.typography.bodySmall, color = FairyGreen, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                Spacer(Modifier.height(12.dp))
                Button({ placedItems = Season.entries.associateWith { emptyList() }; selectedItem = null; AudioPlayer.playSFX(R.raw.sfx_reset) }, Modifier.fillMaxWidth().height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPink)) { Text("🔄 Заново", fontSize = 14.sp, color = Color.White) }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(0.67f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Season.entries.forEach { season -> val seasonItems = placedItems[season] ?: emptyList()
                        Card(Modifier.weight(1f).fillMaxHeight().aspectRatio(0.7f).clickable { if (selectedItem != null) { placedItems = placedItems.toMutableMap().apply { put(season, (get(season) ?: emptyList()) + selectedItem!!) }; selectedItem = null; AudioPlayer.playSFX(R.raw.sfx_drop) } }, colors = CardDefaults.cardColors(containerColor = if (selectedItem != null) season.color.copy(alpha = 0.7f) else season.color.copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                            Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) { Text(season.emoji, fontSize = 32.sp); Text(season.seasonName, fontSize = 14.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp)); seasonItems.forEach { item -> Text(item.emoji, fontSize = 26.sp) } }
                        }
                    }
                }
            }
        }
    }

    if (showLevelComplete) LevelComplete(stars, "Времена года изучены!", onNext = { if (stage < 4) onNextStage() else onGameComplete() })
}
