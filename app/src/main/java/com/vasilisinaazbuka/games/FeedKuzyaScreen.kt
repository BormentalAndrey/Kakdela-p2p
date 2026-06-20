package com.vasilisinaazbuka.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
 * Игра «Накорми Кузю» — 5 уровней с заданиями на счёт продуктов
 */
@Composable
fun FeedKuzyaScreen(
    stage: Int = 1,
    onNextStage: () -> Unit = {},
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Продукты
    data class FoodItem(val id: String, val name: String, val emoji: String, val imageRes: Int)

    val allFoods = listOf(
        FoodItem("apple", "Яблоко", "🍎", R.drawable.item_apple),
        FoodItem("pizza", "Пицца", "🍕", R.drawable.item_pizza),
        FoodItem("banana", "Банан", "🍌", R.drawable.item_banana),
        FoodItem("cookie", "Печенье", "🍪", R.drawable.item_cookie),
        FoodItem("cheese", "Сыр", "🧀", R.drawable.item_cheese),
        FoodItem("juice", "Сок", "🧃", R.drawable.item_juice)
    )

    // Задания по уровням (усложняются)
    data class Task(val foodId: String, val required: Int)
    val levelTasks = mapOf(
        1 to listOf(Task("apple", 3)),
        2 to listOf(Task("apple", 2), Task("pizza", 2)),
        3 to listOf(Task("banana", 3), Task("cookie", 2)),
        4 to listOf(Task("apple", 2), Task("pizza", 1), Task("juice", 2)),
        5 to listOf(Task("apple", 2), Task("banana", 2), Task("cheese", 2))
    )

    val currentTasks = levelTasks[stage] ?: levelTasks[1]!!
    val selectedFoods = remember { mutableStateListOf<FoodItem>() }
    var showLevelComplete by remember { mutableStateOf(false) }

    // Подсчёт собранных продуктов
    val collectedCounts = remember {
        derivedStateOf {
            currentTasks.map { task ->
                task to selectedFoods.count { it.id == task.foodId }
            }
        }
    }

    // Проверка выполнения задания
    val isTaskComplete = collectedCounts.value.all { (task, count) -> count >= task.required }

    LaunchedEffect(isTaskComplete) {
        if (isTaskComplete && !showLevelComplete) {
            showLevelComplete = true
            AudioPlayer.playSFX(R.raw.sfx_success)
            GameState.completeLevel("feedkuzya", stage)
        }
    }

    // Сброс при смене этапа
    LaunchedEffect(stage) {
        selectedFoods.clear()
        showLevelComplete = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FairyBlue.copy(alpha = 0.1f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Накорми Кузю",
            style = MaterialTheme.typography.headlineMedium,
            color = FairyGold,
            fontWeight = FontWeight.Bold
        )

        StageProgressIndicator(currentStage = stage, maxStages = 5)

        Spacer(modifier = Modifier.height(8.dp))

        // Персонаж Кузя
        CharacterView(
            character = "kuzya",
            emotion = if (isTaskComplete) "happy" else "hungry",
            message = buildString {
                append("Кузя хочет: ")
                currentTasks.forEach { task ->
                    val food = allFoods.find { it.id == task.foodId }
                    append("${task.required} ${food?.emoji} ")
                }
            },
            modifier = Modifier.height(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Прогресс выполнения
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Прогресс:",
                    style = MaterialTheme.typography.titleMedium,
                    color = FairyPurple
                )
                collectedCounts.value.forEach { (task, count) ->
                    val food = allFoods.find { it.id == task.foodId }
                    Text(
                        text = "${food?.emoji} ${food?.name}: $count из ${task.required}",
                        color = if (count >= task.required) FairyGreen else Color.Gray,
                        fontWeight = if (count >= task.required) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Сетка продуктов
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allFoods) { food ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            selectedFoods.add(food)
                            AudioPlayer.playSFX(R.raw.sfx_click)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FairyGold.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = food.emoji,
                            fontSize = 48.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Кнопки управления
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (selectedFoods.isNotEmpty()) {
                        selectedFoods.removeAt(selectedFoods.size - 1)
                        AudioPlayer.playSFX(R.raw.sfx_reset)
                    }
                },
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FairyPink)
            ) {
                Text("↩️ Отменить", fontSize = 16.sp, color = Color.White)
            }

            Button(
                onClick = {
                    selectedFoods.clear()
                    AudioPlayer.playSFX(R.raw.sfx_reset)
                },
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("🔄 Заново", fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Индикатор собранных продуктов
        if (selectedFoods.isNotEmpty()) {
            Text(
                text = "Собрано: ${selectedFoods.joinToString(" ") { it.emoji }}",
                style = MaterialTheme.typography.bodyLarge,
                color = FairyGreen
            )
        }
    }

    // Окно завершения
    if (showLevelComplete) {
        LevelComplete(
            stars = 3,
            message = "Кузя накормлен и доволен!",
            onNext = {
                if (stage < 5) onNextStage() else onGameComplete()
            }
        )
    }
}
