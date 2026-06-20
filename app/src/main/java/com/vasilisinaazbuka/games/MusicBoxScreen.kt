package com.vasilisinaazbuka.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vasilisinaazbuka.R
import com.vasilisinaazbuka.audio.AudioPlayer
import com.vasilisinaazbuka.data.GameState
import com.vasilisinaazbuka.ui.CharacterView
import com.vasilisinaazbuka.ui.LevelComplete
import com.vasilisinaazbuka.ui.theme.*
import kotlinx.coroutines.delay

// Режимы игры вынесены из функции
private enum class MusicMode { FREE_PLAY, REPEAT_MELODY, GUESS_SOUND }

// Модель звука вынесена из функции
private data class SoundItem(
    val id: Int,
    val name: String,
    val emoji: String,
    val soundRes: Int,
    val category: String
)

/**
 * Игра «Музыкальная шкатулка» — 3 режима: свободная игра, повтори мелодию, угадай звук
 */
@Composable
fun MusicBoxScreen(
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var currentMode by remember { mutableStateOf(MusicMode.FREE_PLAY) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    val maxScore = 3

    val soundItems = remember {
        listOf(
            SoundItem(1, "Кошка", "🐱", R.raw.sound_cat, "animals"),
            SoundItem(2, "Собака", "🐶", R.raw.sound_dog, "animals"),
            SoundItem(3, "Корова", "🐮", R.raw.sound_cow, "animals"),
            SoundItem(4, "Лягушка", "🐸", R.raw.sound_frog, "animals"),
            SoundItem(5, "Машина", "🚗", R.raw.sound_car, "transport"),
            SoundItem(6, "Самолёт", "✈️", R.raw.sound_plane, "transport"),
            SoundItem(7, "Поезд", "🚂", R.raw.sound_train, "transport"),
            SoundItem(8, "Корабль", "🚢", R.raw.sound_ship, "transport"),
            SoundItem(9, "Гитара", "🎸", R.raw.sound_guitar, "instrument"),
            SoundItem(10, "Барабан", "🥁", R.raw.sound_drum, "instrument"),
            SoundItem(11, "Колокол", "🔔", R.raw.sound_bell, "instrument"),
            SoundItem(12, "Флейта", "🎵", R.raw.sound_flute, "instrument")
        )
    }

    var melodySequence by remember { mutableStateOf(listOf<SoundItem>()) }
    var playerSequence by remember { mutableStateOf(listOf<SoundItem>()) }
    var isShowingMelody by remember { mutableStateOf(false) }
    var highlightedItemId by remember { mutableIntStateOf(-1) }

    var currentSound by remember { mutableStateOf<SoundItem?>(null) }
    var options by remember { mutableStateOf(listOf<SoundItem>()) }

    fun resetMode() {
        score = 0
        melodySequence = emptyList()
        playerSequence = emptyList()
        currentSound = null
        options = emptyList()
        showLevelComplete = false
    }

    fun generateMelody() {
        val pool = soundItems.take(8)
        melodySequence = List(4) { pool.random() }
        playerSequence = emptyList()
        isShowingMelody = true
    }

    LaunchedEffect(isShowingMelody) {
        if (isShowingMelody && melodySequence.isNotEmpty()) {
            for (item in melodySequence) {
                highlightedItemId = item.id
                AudioPlayer.playSFX(item.soundRes)
                delay(800)
                highlightedItemId = -1
                delay(300)
            }
            isShowingMelody = false
        }
    }

    fun generateGuessQuestion() {
        val correct = soundItems.random()
        currentSound = correct
        val wrongOptions = soundItems.filter { it.id != correct.id }.shuffled().take(3)
        options = (listOf(correct) + wrongOptions).shuffled()
        AudioPlayer.playSFX(correct.soundRes)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FairyBlue.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Музыкальная шкатулка",
                style = MaterialTheme.typography.headlineMedium,
                color = FairyGold,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MusicMode.entries.forEach { mode ->
                    FilterChip(
                        selected = currentMode == mode,
                        onClick = {
                            currentMode = mode
                            resetMode()
                            when (mode) {
                                MusicMode.REPEAT_MELODY -> generateMelody()
                                MusicMode.GUESS_SOUND -> generateGuessQuestion()
                                else -> {}
                            }
                        },
                        label = {
                            Text(
                                text = when (mode) {
                                    MusicMode.FREE_PLAY -> "🎹 Свободная"
                                    MusicMode.REPEAT_MELODY -> "🔁 Повтори"
                                    MusicMode.GUESS_SOUND -> "❓ Угадай"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FairyPurple
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CharacterView(
                character = "knopa",
                emotion = if (score >= maxScore) "happy" else "neutral",
                message = when (currentMode) {
                    MusicMode.FREE_PLAY -> "Нажимай на картинки и слушай звуки!"
                    MusicMode.REPEAT_MELODY -> "Повтори мою мелодию!"
                    MusicMode.GUESS_SOUND -> "Угадай, что звучит?"
                },
                modifier = Modifier.height(80.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (currentMode != MusicMode.FREE_PLAY) {
                Text(
                    text = "Правильно: $score из $maxScore",
                    style = MaterialTheme.typography.titleMedium,
                    color = FairyGreen,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(soundItems) { item ->
                    val isHighlighted = highlightedItemId == item.id
                    val bgColor by animateColorAsState(
                        targetValue = if (isHighlighted) FairyGold.copy(alpha = 0.5f) else Color.White,
                        label = "highlight"
                    )

                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                when (currentMode) {
                                    MusicMode.FREE_PLAY -> {
                                        AudioPlayer.playSFX(item.soundRes)
                                    }
                                    MusicMode.REPEAT_MELODY -> {
                                        if (!isShowingMelody) {
                                            playerSequence = playerSequence + item
                                            AudioPlayer.playSFX(item.soundRes)

                                            val currentIndex = playerSequence.size - 1
                                            if (currentIndex < melodySequence.size &&
                                                playerSequence[currentIndex].id == melodySequence[currentIndex].id
                                            ) {
                                                if (playerSequence.size == melodySequence.size) {
                                                    score++
                                                    if (score >= maxScore) {
                                                        showLevelComplete = true
                                                        GameState.completeLevel("musicbox", 1)
                                                    } else {
                                                        generateMelody()
                                                    }
                                                }
                                            } else {
                                                playerSequence = emptyList()
                                                AudioPlayer.playSFX(R.raw.sfx_error)
                                            }
                                        }
                                    }
                                    MusicMode.GUESS_SOUND -> {
                                        if (currentSound != null && item.id == currentSound!!.id) {
                                            score++
                                            AudioPlayer.playSFX(R.raw.sfx_success)
                                            if (score >= maxScore) {
                                                showLevelComplete = true
                                                GameState.completeLevel("musicbox", 1)
                                            } else {
                                                generateGuessQuestion()
                                            }
                                        } else if (currentSound != null) {
                                            AudioPlayer.playSFX(R.raw.sfx_error)
                                        }
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(text = item.emoji, fontSize = 36.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (currentMode == MusicMode.REPEAT_MELODY && !isShowingMelody) {
                Button(
                    onClick = {
                        playerSequence = emptyList()
                        generateMelody()
                    },
                    modifier = Modifier.height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FairyBlue)
                ) {
                    Text("🔄 Новая мелодия", fontSize = 18.sp, color = Color.White)
                }
            }

            if (currentMode == MusicMode.GUESS_SOUND && currentSound != null) {
                Button(
                    onClick = { AudioPlayer.playSFX(currentSound!!.soundRes) },
                    modifier = Modifier.height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FairyGold)
                ) {
                    Text("🔊 Прослушать ещё раз", fontSize = 18.sp, color = Color.White)
                }
            }
        }

        if (showLevelComplete) {
            LevelComplete(
                stars = 3,
                message = "Молодец! Ты отлично справился с музыкой!",
                onNext = { onGameComplete() }
            )
        }
    }
}
