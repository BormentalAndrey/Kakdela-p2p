package com.vasilisinaazbuka.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
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
import com.vasilisinaazbuka.ui.theme.*
import kotlinx.coroutines.delay

private enum class MusicMode { FREE_PLAY, REPEAT_MELODY, GUESS_SOUND }
private data class SoundItem(val id: Int, val name: String, val emoji: String, val soundRes: Int, val category: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicBoxScreen(onGameComplete: () -> Unit = {}, onBack: () -> Unit = {}) {
    var currentMode by remember { mutableStateOf(MusicMode.FREE_PLAY) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    val maxScore = 3

    val soundItems = remember {
        listOf(
            SoundItem(1, "Кошка", "🐱", R.raw.sound_cat, "animals"), SoundItem(2, "Собака", "🐶", R.raw.sound_dog, "animals"),
            SoundItem(3, "Корова", "🐮", R.raw.sound_cow, "animals"), SoundItem(4, "Лягушка", "🐸", R.raw.sound_frog, "animals"),
            SoundItem(5, "Машина", "🚗", R.raw.sound_car, "transport"), SoundItem(6, "Самолёт", "✈️", R.raw.sound_plane, "transport"),
            SoundItem(7, "Поезд", "🚂", R.raw.sound_train, "transport"), SoundItem(8, "Корабль", "🚢", R.raw.sound_ship, "transport"),
            SoundItem(9, "Гитара", "🎸", R.raw.sound_guitar, "instrument"), SoundItem(10, "Барабан", "🥁", R.raw.sound_drum, "instrument"),
            SoundItem(11, "Колокол", "🔔", R.raw.sound_bell, "instrument"), SoundItem(12, "Флейта", "🎵", R.raw.sound_flute, "instrument")
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
        options = (listOf(correct) + soundItems.filter { it.id != correct.id }.shuffled().take(3)).shuffled()
        AudioPlayer.playSFX(correct.soundRes)
    }

    Box(Modifier.fillMaxSize()) {
        // Фоновое изображение
        Image(
            painter = painterResource(R.drawable.bg_level_musicbox),
            contentDescription = "Фон",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )

        // Основной контент
        Row(
            Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая панель с управлением
            Column(
                Modifier.weight(0.33f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Музыкальная шкатулка",
                    style = MaterialTheme.typography.titleLarge,
                    color = FairyGreen,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))

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
                                when (mode) {
                                    MusicMode.FREE_PLAY -> "🎹 Свободная"
                                    MusicMode.REPEAT_MELODY -> "🔁 Повтори"
                                    MusicMode.GUESS_SOUND -> "❓ Угадай"
                                },
                                fontSize = 13.sp,
                                color = if (currentMode == mode) Color.White else FairyGreen
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FairyGreen,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                
                CharacterView(
                    "knopa",
                    if (score >= maxScore) "happy" else "neutral",
                    when (currentMode) {
                        MusicMode.FREE_PLAY -> "Нажимай на картинки\nи слушай звуки!"
                        MusicMode.REPEAT_MELODY -> "Повтори мою\nмелодию!"
                        MusicMode.GUESS_SOUND -> "Угадай, что\nзвучит?"
                    },
                    Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                if (currentMode != MusicMode.FREE_PLAY) {
                    Text(
                        "Правильно: $score из $maxScore",
                        style = MaterialTheme.typography.titleMedium,
                        color = FairyGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                }
                
                if (currentMode == MusicMode.REPEAT_MELODY && !isShowingMelody) {
                    Button(
                        {
                            playerSequence = emptyList()
                            generateMelody()
                        },
                        Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairyBlue)
                    ) {
                        Text("🔄 Новая мелодия", fontSize = 14.sp, color = Color.White)
                    }
                }
                
                if (currentMode == MusicMode.GUESS_SOUND && currentSound != null) {
                    Button(
                        {
                            AudioPlayer.playSFX(currentSound!!.soundRes)
                        },
                        Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairyGold)
                    ) {
                        Text("🔊 Прослушать", fontSize = 14.sp, color = Color.White)
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Правая панель с сеткой звуков
            Card(
                Modifier.weight(0.67f).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(soundItems) { item ->
                        val isHighlighted = highlightedItemId == item.id
                        val bgColor by animateColorAsState(
                            targetValue = if (isHighlighted) FairyGold.copy(alpha = 0.5f) else Color.White,
                            label = "hl_${item.id}"
                        )
                        Card(
                            Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    when (currentMode) {
                                        MusicMode.FREE_PLAY -> AudioPlayer.playSFX(item.soundRes)
                                        MusicMode.REPEAT_MELODY -> {
                                            if (!isShowingMelody) {
                                                playerSequence += item
                                                AudioPlayer.playSFX(item.soundRes)
                                                val i = playerSequence.size - 1
                                                if (i < melodySequence.size && playerSequence[i].id == melodySequence[i].id) {
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
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(item.emoji, fontSize = 36.sp)
                            }
                        }
                    }
                }
            }
        }

        // Кнопка «Назад» на переднем плане (поверх всего)
        Box(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.size(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FairyBlue.copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("←", fontSize = 24.sp, color = Color.White)
            }
        }

        // LevelComplete поверх всего
        if (showLevelComplete) {
            LevelComplete(
                3,
                "Молодец! Ты отлично справился с музыкой!",
                character = "knopa",
                onNext = { onGameComplete() }
            )
        }
    }
}
