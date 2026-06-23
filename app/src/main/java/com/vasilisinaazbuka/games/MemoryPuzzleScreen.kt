package com.vasilisinaazbuka.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.vasilisinaazbuka.R
import com.vasilisinaazbuka.audio.AudioPlayer
import com.vasilisinaazbuka.data.GameState
import com.vasilisinaazbuka.ui.CharacterView
import com.vasilisinaazbuka.ui.LevelComplete
import com.vasilisinaazbuka.ui.StageProgressIndicator
import com.vasilisinaazbuka.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Карты кусочков для каждого уровня (1-6)
private val puzzlePiecesMap = mapOf(
    "kremlin" to listOf(R.drawable.puzzle_kremlin1, R.drawable.puzzle_kremlin2, R.drawable.puzzle_kremlin3,
                         R.drawable.puzzle_kremlin4, R.drawable.puzzle_kremlin5, R.drawable.puzzle_kremlin6),
    "baikal" to listOf(R.drawable.puzzle_baikal1, R.drawable.puzzle_baikal2, R.drawable.puzzle_baikal3,
                        R.drawable.puzzle_baikal4, R.drawable.puzzle_baikal5, R.drawable.puzzle_baikal6),
    "matryoshka" to listOf(R.drawable.puzzle_matryoshka1, R.drawable.puzzle_matryoshka2, R.drawable.puzzle_matryoshka3,
                            R.drawable.puzzle_matryoshka4, R.drawable.puzzle_matryoshka5, R.drawable.puzzle_matryoshka6),
    "birch" to listOf(R.drawable.puzzle_birch1, R.drawable.puzzle_birch2, R.drawable.puzzle_birch3,
                       R.drawable.puzzle_birch4, R.drawable.puzzle_birch5, R.drawable.puzzle_birch6),
    "samovar" to listOf(R.drawable.puzzle_samovar1, R.drawable.puzzle_samovar2, R.drawable.puzzle_samovar3,
                         R.drawable.puzzle_samovar4, R.drawable.puzzle_samovar5, R.drawable.puzzle_samovar6)
)

private val levelKeys = listOf("kremlin", "baikal", "matryoshka", "birch", "samovar")
private val levelFullImages = listOf(R.drawable.puzzle_kremlin, R.drawable.puzzle_baikal, R.drawable.puzzle_matryoshka, R.drawable.puzzle_birch, R.drawable.puzzle_samovar)

@Composable
fun MemoryPuzzleScreen(stage: Int = 1, onNextStage: () -> Unit = {}, onGameComplete: () -> Unit = {}, onBack: () -> Unit = {}) {
    val currentKey = levelKeys.getOrElse(stage - 1) { "kremlin" }
    val currentFullImage = levelFullImages.getOrElse(stage - 1) { R.drawable.puzzle_kremlin }
    val pieceImages = puzzlePiecesMap[currentKey] ?: puzzlePiecesMap["kremlin"]!!

    var showImage by remember { mutableStateOf(true) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }
    var viewCount by remember { mutableIntStateOf(0) }
    var selectedPiece by remember { mutableIntStateOf(-1) }

    val shuffledPieces = remember { (0..5).shuffled() }
    var placedPieces by remember { mutableStateOf(mapOf<Int, Int>()) }

    LaunchedEffect(stage) { showImage = true; placedPieces = emptyMap(); selectedPiece = -1; viewCount = 0; delay(3000); showImage = false }
    val isComplete = placedPieces.size == 6 && placedPieces.all { (cell, piece) -> cell == piece }

    LaunchedEffect(isComplete) {
        if (isComplete && !showLevelComplete) {
            stars = when (viewCount) { 0 -> 3; 1 -> 2; else -> 1 }
            showLevelComplete = true; AudioPlayer.playSFX(R.raw.sfx_success); GameState.completeLevel("memorypuzzle", stage, stars)
        }
    }

    val availablePieces = shuffledPieces.filter { it !in placedPieces.values }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_level_memory), "Фон", Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)

        Row(Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(0.33f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Собери картинку", style = MaterialTheme.typography.titleLarge, color = FairyGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp)); StageProgressIndicator(currentStage = stage, maxStages = 5, compact = true); Spacer(Modifier.height(12.dp))
                CharacterView("vasilisa", if (showImage) "teacher" else "thinking", if (showImage) "Запомни картинку!\nСмотри внимательно." else "Собери картинку!\nНажми на кусочек,\nпотом на ячейку.", Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))

                if (!showImage) {
                    Button({ viewCount++; showImage = true; GlobalScope.launch(Dispatchers.Main) { delay(3000); showImage = false } }, Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyBlue), shape = RoundedCornerShape(12.dp)) { Text("👁️ Посмотреть\nещё раз", fontSize = 14.sp, color = Color.White, textAlign = TextAlign.Center) }
                    Spacer(Modifier.height(8.dp))
                    Button({ placedPieces = emptyMap(); selectedPiece = -1; AudioPlayer.playSFX(R.raw.sfx_reset) }, Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPink), shape = RoundedCornerShape(12.dp)) { Text("🔄 Заново", fontSize = 14.sp, color = Color.White) }
                    if (selectedPiece >= 0) { Spacer(Modifier.height(8.dp)); Text("Выбран кусочек №${selectedPiece + 1}\nНажми на ячейку!", style = MaterialTheme.typography.bodySmall, color = FairyGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(0.67f).fillMaxHeight().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                if (showImage) {
                    Card(Modifier.fillMaxWidth().aspectRatio(1.5f), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Image(painterResource(currentFullImage), "Запомни!", Modifier.fillMaxSize().padding(16.dp), contentScale = ContentScale.Fit)
                            Box(Modifier.align(Alignment.TopCenter).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text("Запоминай! 3 сек...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        }
                    }
                } else {
                    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().aspectRatio(1.5f), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), userScrollEnabled = false) {
                        itemsIndexed(List(6) { it }) { _, cellIndex ->
                            val pieceInCell = placedPieces[cellIndex]; val isCorrect = pieceInCell != null && pieceInCell == cellIndex
                            val bgColor by animateColorAsState(targetValue = when { pieceInCell != null && isCorrect -> FairyGreen.copy(alpha = 0.3f); pieceInCell != null && !isCorrect -> FairyPink.copy(alpha = 0.3f); selectedPiece >= 0 -> FairyGold.copy(alpha = 0.2f); else -> Color.Gray.copy(alpha = 0.2f) }, animationSpec = spring(), label = "bg_$cellIndex")
                            Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(bgColor).border(2.dp, when { pieceInCell != null && isCorrect -> FairyGreen; pieceInCell != null -> FairyPink; else -> FairyGold.copy(alpha = 0.5f) }, RoundedCornerShape(8.dp)).clickable {
                                if (selectedPiece >= 0 && !placedPieces.containsKey(cellIndex)) { placedPieces = placedPieces + (cellIndex to selectedPiece); selectedPiece = -1; AudioPlayer.playSFX(R.raw.sfx_drop) }
                                else if (pieceInCell != null && selectedPiece < 0) { placedPieces = placedPieces - cellIndex; AudioPlayer.playSFX(R.raw.sfx_click) }
                            }, contentAlignment = Alignment.Center) {
                                if (pieceInCell != null) Image(painter = painterResource(pieceImages[pieceInCell]), contentDescription = "Кусочек ${pieceInCell + 1}", modifier = Modifier.fillMaxSize().padding(2.dp), contentScale = ContentScale.Fit)
                                else Text("${cellIndex + 1}", fontSize = 24.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp)); Text("Доступные кусочки:", style = MaterialTheme.typography.bodySmall, color = FairyPurple); Spacer(Modifier.height(4.dp))
                    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().height(140.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), userScrollEnabled = false) {
                        itemsIndexed(availablePieces) { _, pieceIndex ->
                            val isSelected = selectedPiece == pieceIndex
                            Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) FairyGold.copy(alpha = 0.5f) else Color.White).border(if (isSelected) 3.dp else 1.dp, if (isSelected) FairyGold else Color.Gray, RoundedCornerShape(8.dp)).clickable { selectedPiece = if (isSelected) -1 else pieceIndex; AudioPlayer.playSFX(R.raw.sfx_click) }, contentAlignment = Alignment.Center) {
                                Image(painter = painterResource(pieceImages[pieceIndex]), contentDescription = "Кусочек ${pieceIndex + 1}", modifier = Modifier.fillMaxSize().padding(4.dp), contentScale = ContentScale.Fit)
                                Box(Modifier.align(Alignment.TopStart).padding(2.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) { Text("${pieceIndex + 1}", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }
        }

        // Кнопка «Назад» на переднем плане
        Box(Modifier.fillMaxSize().wrapContentSize(Alignment.TopEnd).padding(8.dp).zIndex(100f)) {
            Button(onClick = onBack, Modifier.size(48.dp).zIndex(100f), colors = ButtonDefaults.buttonColors(containerColor = FairyBlue.copy(alpha = 0.85f)), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(0.dp), elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)) { Text("↩", fontSize = 20.sp, color = Color.White) }
        }

        if (showLevelComplete) LevelComplete(stars, "Картинка собрана!\nПодсматриваний: $viewCount", character = "vasilisa", onNext = { if (stage < 5) onNextStage() else onGameComplete() })
    }
}
