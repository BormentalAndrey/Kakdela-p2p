package com.vasilisinaazbuka.games

import androidx.compose.animation.animateColorAsState
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
 * Игра «Караоке-читалка» — 5 уровней чтения по слогам
 */
@Composable
fun KaraokeScreen(
    stage: Int = 1,
    onNextStage: () -> Unit = {},
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Слова по уровням (из сказки «Волшебный клубочек»)
    data class LevelWord(
        val word: String,
        val syllables: List<String>,
        val hint: String
    )

    val levels = mapOf(
        1 to LevelWord("МАМА", listOf("МА", "МА"), "Ма-ма"),
        2 to LevelWord("МОСКВА", listOf("МОС", "КВА"), "Мос-ква"),
        3 to LevelWord("РОДИНА", listOf("РО", "ДИ", "НА"), "Ро-ди-на"),
        4 to LevelWord("РОССИЯ", listOf("РОС", "СИ", "Я"), "Рос-си-я"),
        5 to LevelWord("ВАСИЛИСА", listOf("ВА", "СИ", "ЛИ", "СА"), "Ва-си-ли-са")
    )

    val currentLevel = levels[stage] ?: levels[1]!!
    var currentSyllableIndex by remember { mutableIntStateOf(0) }
    var constructedWord by remember { mutableStateOf("") }
    var showLevelComplete by remember { mutableStateOf(false) }
    var attempts by remember { mutableIntStateOf(0) }
    var stars by remember { mutableIntStateOf(0) }

    // Доступные буквы для клавиатуры (русский алфавит)
    val alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".toCharArray().toList()

    // Сброс состояния
    LaunchedEffect(stage) {
        currentSyllableIndex = 0
        constructedWord = ""
        showLevelComplete = false
        attempts = 0
    }

    // Проверка составленного слова
    LaunchedEffect(constructedWord) {
        if (constructedWord == currentLevel.word) {
            stars = when {
                attempts <= currentLevel.syllables.size -> 3
                attempts <= currentLevel.syllables.size + 2 -> 2
                else -> 1
            }
            showLevelComplete = true
            AudioPlayer.playSFX(R.raw.sfx_success)
            GameState.completeLevel("karaoke", stage)
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
            text = "Караоке-читалка",
            style = MaterialTheme.typography.headlineMedium,
            color = FairyGold,
            fontWeight = FontWeight.Bold
        )

        StageProgressIndicator(currentStage = stage, maxStages = 5)

        Spacer(modifier = Modifier.height(8.dp))

        // Персонаж Василиса
        val emotion = when {
            showLevelComplete -> "proud"
            constructedWord.length > currentLevel.word.length -> "sad"
            else -> "teacher"
        }

        CharacterView(
            character = "vasilisa",
            emotion = emotion,
            message = "Прочитай слово: ${currentLevel.hint}",
            modifier = Modifier.height(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Отображение слогов
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Слоги с подсветкой текущего
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    currentLevel.syllables.forEachIndexed { index, syllable ->
                        val isActive = index == currentSyllableIndex
                        val bgColor by animateColorAsState(
                            if (isActive) FairyGold.copy(alpha = 0.3f) else Color.Transparent,
                            label = "syllable"
                        )

                        Text(
                            text = syllable,
                            fontSize = 36.sp,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isActive) FairyPink else Color.Black,
                            modifier = Modifier
                                .padding(4.dp)
                                .background(bgColor, RoundedCornerShape(8.dp))
                                .clickable {
                                    currentSyllableIndex = index
                                    AudioPlayer.playSFX(R.raw.sfx_click)
                                }
                        )

                        if (index < currentLevel.syllables.size - 1) {
                            Text(
                                text = "-",
                                fontSize = 24.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Текущее составленное слово
                Text(
                    text = "Составь слово:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = FairyPurple
                )

                Text(
                    text = constructedWord.ifEmpty { "_".repeat(currentLevel.word.length) },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = FairyGreen,
                    letterSpacing = 8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Клавиатура из букв
        Text(
            text = "Нажми на буквы, чтобы составить слово:",
            style = MaterialTheme.typography.bodyMedium,
            color = FairyPurple
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(alphabet) { letter ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (constructedWord.contains(letter)) FairyGold.copy(alpha = 0.3f)
                            else Color.White
                        )
                        .border(2.dp, FairyGold, CircleShape)
                        .clickable {
                            if (constructedWord.length < currentLevel.word.length) {
                                constructedWord += letter
                                attempts++
                                AudioPlayer.playSFX(R.raw.sfx_type)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Кнопки управления
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Прослушать
            Button(
                onClick = {
                    AudioPlayer.playSFX(
                        when (stage) {
                            1 -> R.raw.word_mama
                            2 -> R.raw.word_moskva
                            3 -> R.raw.word_rodina
                            4 -> R.raw.word_rossiya
                            5 -> R.raw.word_vasilisa
                            else -> R.raw.word_mama
                        }
                    )
                },
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FairyBlue)
            ) {
                Text("🔊 Прослушать", fontSize = 16.sp, color = Color.White)
            }

            // Удалить последнюю букву
            Button(
                onClick = {
                    if (constructedWord.isNotEmpty()) {
                        constructedWord = constructedWord.dropLast(1)
                        AudioPlayer.playSFX(R.raw.sfx_reset)
                    }
                },
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FairyPink)
            ) {
                Text("⌫ Удалить", fontSize = 16.sp, color = Color.White)
            }

            // Очистить
            Button(
                onClick = {
                    constructedWord = ""
                    AudioPlayer.playSFX(R.raw.sfx_reset)
                },
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("🔄 Очистить", fontSize = 16.sp, color = Color.White)
            }
        }
    }

    // Окно завершения
    if (showLevelComplete) {
        LevelComplete(
            stars = stars,
            message = "Слово «${currentLevel.word}» составлено!",
            onNext = {
                if (stage < 5) onNextStage() else onGameComplete()
            }
        )
    }
}
