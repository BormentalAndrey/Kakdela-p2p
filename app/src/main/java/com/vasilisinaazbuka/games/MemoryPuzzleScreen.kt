package com.vasilisinaazbuka.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
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
import kotlinx.coroutines.delay

/**
 * Игра «Собери картинку по памяти» — 5 уровней с пазлами 3×2
 */
@Composable
fun MemoryPuzzleScreen(
    stage: Int = 1,
    onNextStage: () -> Unit = {},
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    // Изображения для уровней
    val levelImages = listOf(
        R.drawable.puzzle_kremlin,   // Кремль
        R.drawable.puzzle_baikal,    // Байкал
        R.drawable.puzzle_matryoshka,// Матрёшка
        R.drawable.puzzle_birch,     // Берёзка
        R.drawable.puzzle_samovar    // Самовар
    )

    val currentImage = levelImages.getOrElse(stage - 1) { R.drawable.puzzle_kremlin }

    // Позиции кусочков (3×2 = 6 кусочков)
    data class PuzzlePiece(val id: Int, val correctPosition: Int)
    var pieces by remember { mutableStateOf(List(6) { PuzzlePiece(it, it) }.shuffled()) }
    var userPieces by remember { mutableStateOf(mutableMapOf<Int, PuzzlePiece>()) }
    var showImage by remember { mutableStateOf(true) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }
    var viewCount by remember { mutableIntStateOf(0) }

    // Показ картинки на 3 секунды
    LaunchedEffect(stage) {
        showImage = true
        pieces = List(6) { PuzzlePiece(it, it) }.shuffled()
        userPieces = mutableMapOf()
        viewCount = 0
        delay(3000)
        showImage = false
    }

    // Проверка правильности сборки
    val isComplete = userPieces.size == 6 && userPieces.all { (pos, piece) ->
        piece.correctPosition == pos
    }

    LaunchedEffect(isComplete) {
        if (isComplete) {
            stars = when (viewCount) {
                0 -> 3
                1 -> 2
                else -> 1
            }
            showLevelComplete = true
            AudioPlayer.playSFX(R.raw.sfx_success)
            GameState.completeLevel("memorypuzzle", stage)
        }
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
                text = "Собери картинку",
                style = MaterialTheme.typography.headlineMedium,
                color = FairyGold,
                fontWeight = FontWeight.Bold
            )

            StageProgressIndicator(currentStage = stage, maxStages = 5)

            Spacer(modifier = Modifier.height(8.dp))

            CharacterView(
                character = "vasilisa",
                emotion = "thinking",
                message = if (showImage) "Запомни картинку!" else "Собери её из кусочков!",
                modifier = Modifier.height(80.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Область сборки или показа картинки
            if (showImage) {
                // Показ полной картинки
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Image(
                        painter = painterResource(currentImage),
                        contentDescription = "Запомни картинку",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "Запоминай! ${3} сек...",
                    style = MaterialTheme.typography.titleLarge,
                    color = FairyPink,
                    fontWeight = FontWeight.Bold
                )
            } else {
                // Сетка для сборки 3×2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(6) { position ->
                        val piece = userPieces[position]

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (piece != null) FairyGreen.copy(alpha = 0.3f)
                                    else Color.Gray.copy(alpha = 0.3f)
                                )
                                .border(2.dp, FairyGold, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (piece != null) {
                                Text(
                                    text = "🧩${piece.id + 1}",
                                    fontSize = 24.sp,
                                    color = FairyPurple
                                )
                            } else {
                                Text(
                                    text = "?",
                                    fontSize = 32.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Доступные кусочки для перетаскивания
                Text(
                    text = "Доступные кусочки (нажми на кусочек, затем на ячейку):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FairyPurple
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    pieces.filter { it !in userPieces.values }.forEach { piece ->
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FairyGold.copy(alpha = 0.3f))
                                .border(2.dp, FairyGold, RoundedCornerShape(12.dp))
                                .clickable {
                                    // Помещаем кусочек в первую свободную ячейку
                                    val freePosition = (0..5).firstOrNull { pos ->
                                        !userPieces.containsKey(pos)
                                    }
                                    if (freePosition != null) {
                                        userPieces = userPieces.toMutableMap().apply {
                                            put(freePosition, piece)
                                        }
                                        AudioPlayer.playSFX(R.raw.sfx_click)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🧩${piece.id + 1}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
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
                    Button(
                        onClick = {
                            viewCount++
                            showImage = true
                            userPieces = mutableMapOf()
                            // Показываем картинку ещё раз на 3 секунды
                            kotlinx.coroutines.MainScope().launch {
                                delay(3000)
                                showImage = false
                            }
                        },
                        modifier = Modifier.height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairyBlue)
                    ) {
                        Text("👁️ Посмотреть ещё раз", fontSize = 16.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            pieces = pieces.shuffled()
                            userPieces = mutableMapOf()
                            AudioPlayer.playSFX(R.raw.sfx_reset)
                        },
                        modifier = Modifier.height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FairyPink)
                    ) {
                        Text("🔄 Перемешать", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }

        if (showLevelComplete) {
            LevelComplete(
                stars = stars,
                message = "Картинка собрана!",
                onNext = {
                    if (stage < 5) onNextStage() else onGameComplete()
                }
            )
        }
    }
}

// Вспомогательная функция для корутин
private fun kotlinx.coroutines.MainScope() = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
private fun kotlinx.coroutines.CoroutineScope.launch(block: suspend () -> Unit) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) { block() }
}
