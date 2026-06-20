package com.vasilisinaazbuka.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
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
import kotlin.math.min
import kotlin.random.Random

// Модель кусочка пазла вынесена из функции
private data class PuzzlePiece(
    val id: Int,
    val correctPosition: Int,
    val topTab: Float,
    val bottomTab: Float,
    val leftTab: Float,
    val rightTab: Float,
    val shapeSeed: Int
)

/**
 * Игра «Собери картинку по памяти» — 5 уровней с пазлами
 * Картинка автоматически нарезается на кусочки уникальной формы.
 * Сетка 3×2 = 6 кусочков.
 */
@Composable
fun MemoryPuzzleScreen(
    stage: Int = 1,
    onNextStage: () -> Unit = {},
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val levelImages = listOf(
        R.drawable.puzzle_kremlin,
        R.drawable.puzzle_baikal,
        R.drawable.puzzle_matryoshka,
        R.drawable.puzzle_birch,
        R.drawable.puzzle_samovar
    )

    val currentImage = levelImages.getOrElse(stage - 1) { R.drawable.puzzle_kremlin }

    var showImage by remember { mutableStateOf(true) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }
    var viewCount by remember { mutableIntStateOf(0) }

    val allPieces = remember {
        val pieces = mutableListOf<PuzzlePiece>()
        val rows = 2
        val cols = 3
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val id = row * cols + col
                pieces.add(
                    PuzzlePiece(
                        id = id,
                        correctPosition = id,
                        topTab = if (row == 0) 0f else Random.nextFloat() * 2f - 1f,
                        bottomTab = if (row == rows - 1) 0f else Random.nextFloat() * 2f - 1f,
                        leftTab = if (col == 0) 0f else Random.nextFloat() * 2f - 1f,
                        rightTab = if (col == cols - 1) 0f else Random.nextFloat() * 2f - 1f,
                        shapeSeed = Random.nextInt()
                    )
                )
            }
        }
        pieces
    }

    var availablePieces by remember { mutableStateOf(allPieces.toList()) }
    var placedPieces by remember { mutableStateOf<Map<Int, PuzzlePiece>>(emptyMap()) }
    var selectedPieceId by remember { mutableIntStateOf(-1) }

    LaunchedEffect(stage) {
        showImage = true
        availablePieces = allPieces.shuffled()
        placedPieces = emptyMap()
        selectedPieceId = -1
        viewCount = 0
        delay(3000)
        showImage = false
    }

    val isComplete = placedPieces.size == 6 && placedPieces.all { (pos, piece) ->
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
            GameState.completeLevel("memorypuzzle", stage, stars)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FairyBlue.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая панель
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Собери картинку",
                        style = MaterialTheme.typography.titleLarge,
                        color = FairyGold,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StageProgressIndicator(currentStage = stage, maxStages = 5, compact = true)
                }

                CharacterView(
                    character = "vasilisa",
                    emotion = if (showImage) "teacher" else "thinking",
                    message = if (showImage) "Запомни картинку!"
                    else "Перетащи кусочки\nна правильные места!",
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!showImage) {
                        Button(
                            onClick = {
                                viewCount++
                                showImage = true
                                GlobalScope.launch(Dispatchers.Main) {
                                    delay(3000)
                                    showImage = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FairyBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("👁️ Посмотреть\nещё раз", fontSize = 13.sp, color = Color.White, textAlign = TextAlign.Center)
                        }

                        Button(
                            onClick = {
                                availablePieces = allPieces.shuffled()
                                placedPieces = emptyMap()
                                selectedPieceId = -1
                                AudioPlayer.playSFX(R.raw.sfx_reset)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FairyPink),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🔄 Перемешать", fontSize = 14.sp, color = Color.White)
                        }

                        if (selectedPieceId >= 0) {
                            Button(
                                onClick = { selectedPieceId = -1 },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("✕ Отменить выбор", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Правая часть
            if (showImage) {
                Card(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
                        .aspectRatio(1.5f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(currentImage),
                            contentDescription = "Запомни картинку",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Запоминай! 3 сек...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.weight(0.7f)) {
                    PuzzleGrid(
                        pieces = placedPieces,
                        currentImage = currentImage,
                        selectedPieceId = selectedPieceId,
                        onCellClick = { position ->
                            if (selectedPieceId >= 0 && !placedPieces.containsKey(position)) {
                                val piece = allPieces.find { it.id == selectedPieceId }
                                if (piece != null) {
                                    placedPieces = placedPieces + (position to piece)
                                    availablePieces = availablePieces.filter { it.id != selectedPieceId }
                                    selectedPieceId = -1
                                    AudioPlayer.playSFX(R.raw.sfx_drop)
                                }
                            } else if (placedPieces.containsKey(position) && selectedPieceId < 0) {
                                val piece = placedPieces[position]!!
                                placedPieces = placedPieces - position
                                availablePieces = availablePieces + piece
                                AudioPlayer.playSFX(R.raw.sfx_click)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.5f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Доступные кусочки (выбери и нажми на ячейку):",
                        style = MaterialTheme.typography.bodySmall,
                        color = FairyPurple,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    AvailablePiecesRow(
                        pieces = availablePieces,
                        currentImage = currentImage,
                        selectedPieceId = selectedPieceId,
                        onPieceClick = { pieceId ->
                            selectedPieceId = if (selectedPieceId == pieceId) -1 else pieceId
                            AudioPlayer.playSFX(R.raw.sfx_click)
                        },
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )
                }
            }
        }

        if (showLevelComplete) {
            LevelComplete(
                stars = stars,
                message = "Картинка собрана!\nПодсматриваний: $viewCount",
                character = "vasilisa",
                onNext = {
                    if (stage < 5) onNextStage() else onGameComplete()
                }
            )
        }
    }
}

// ==================== PuzzleGrid ====================

@Composable
private fun PuzzleGrid(
    pieces: Map<Int, PuzzlePiece>,
    currentImage: Int,
    selectedPieceId: Int,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = 2
    val cols = 3

    LazyVerticalGrid(
        columns = GridCells.Fixed(cols),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(
            items = List(rows * cols) { it },
            key = { _, position -> position }
        ) { index, position ->
            val piece = pieces[position]
            val isCorrect = piece != null && piece.correctPosition == position

            val bgColor by animateColorAsState(
                targetValue = when {
                    piece != null && isCorrect -> FairyGreen.copy(alpha = 0.2f)
                    piece != null && !isCorrect -> FairyPink.copy(alpha = 0.15f)
                    else -> Color.Gray.copy(alpha = 0.2f)
                },
                animationSpec = spring(),
                label = "cellBg_$position"
            )

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(bgColor)
                    .border(
                        width = 2.dp,
                        color = when {
                            piece != null && isCorrect -> FairyGreen
                            piece != null -> FairyPink
                            else -> FairyGold.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onCellClick(position) },
                contentAlignment = Alignment.Center
            ) {
                if (piece != null) {
                    PuzzlePieceView(
                        piece = piece,
                        imageRes = currentImage
                    )
                } else {
                    Text(
                        text = "${position + 1}",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== PuzzlePieceView ====================

@Composable
private fun PuzzlePieceView(
    piece: PuzzlePiece,
    imageRes: Int
) {
    val cols = 3
    val rows = 2
    val col = piece.correctPosition % cols
    val row = piece.correctPosition / cols

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(PuzzlePieceShape(piece))
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = "Кусочек ${piece.id + 1}",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = -size.width * col
                    translationY = -size.height * row
                    scaleX = cols.toFloat()
                    scaleY = rows.toFloat()
                }
                .clipToBounds(),
            contentScale = ContentScale.FillBounds
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val shape = PuzzlePieceShape(piece)
            val outline = shape.createOutline(
                size = this.size,
                layoutDirection = LayoutDirection.Ltr,
                density = Density(1f)
            )
            if (outline is Outline.Generic) {
                drawPath(
                    path = outline.path,
                    color = Color.White.copy(alpha = 0.3f),
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

// ==================== PuzzlePieceShape ====================

private class PuzzlePieceShape(
    private val piece: PuzzlePiece
) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val w = size.width
        val h = size.height
        val tabSize = min(w, h) * 0.25f

        path.moveTo(0f, 0f)

        // Верх
        path.lineTo(w / 2f - tabSize, 0f)
        addTab(path, w / 2f, 0f, w / 2f, -tabSize * piece.topTab, tabSize)
        path.lineTo(w, 0f)

        // Право
        path.lineTo(w, h / 2f - tabSize)
        addTab(path, w, h / 2f, w + tabSize * piece.rightTab, h / 2f, tabSize)
        path.lineTo(w, h)

        // Низ
        path.lineTo(w / 2f + tabSize, h)
        addTab(path, w / 2f, h, w / 2f, h + tabSize * piece.bottomTab, tabSize)
        path.lineTo(0f, h)

        // Лево
        path.lineTo(0f, h / 2f + tabSize)
        addTab(path, 0f, h / 2f, -tabSize * piece.leftTab, h / 2f, tabSize)
        path.lineTo(0f, 0f)

        path.close()
        return Outline.Generic(path)
    }

    private fun addTab(
        path: Path,
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        tabSize: Float
    ) {
        val dx = x2 - x1
        val dy = y2 - y1
        val cx1 = x1 + dx * 0.5f
        val cy1 = y1
        val cx2 = x2
        val cy2 = y1 + dy * 0.5f
        val cx3 = x1 + dx * 0.5f
        val cy3 = y2
        val cx4 = x1
        val cy4 = y1 + dy * 0.5f

        path.cubicTo(cx1, cy1, cx2, cy2, x2, y2)
        path.cubicTo(cx3, cy3, cx4, cy4, x1 + dx, y1 + dy)
    }
}

// ==================== AvailablePiecesRow ====================

@Composable
private fun AvailablePiecesRow(
    pieces: List<PuzzlePiece>,
    currentImage: Int,
    selectedPieceId: Int,
    onPieceClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pieces.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "Все кусочки размещены!",
                style = MaterialTheme.typography.bodyMedium,
                color = FairyGreen,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    val count = min(pieces.size, 6)

    LazyVerticalGrid(
        columns = GridCells.Fixed(count),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(
            items = pieces,
            key = { _, piece -> piece.id }
        ) { index, piece ->
            val isSelected = selectedPieceId == piece.id
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1f,
                animationSpec = spring(),
                label = "scale_${piece.id}"
            )

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) FairyGold.copy(alpha = 0.4f) else Color.White)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) FairyGold else Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onPieceClick(piece.id) }
                    .zIndex(if (isSelected) 1f else 0f),
                contentAlignment = Alignment.Center
            ) {
                val col = piece.correctPosition % 3
                val row = piece.correctPosition / 3

                Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
                    Image(
                        painter = painterResource(currentImage),
                        contentDescription = "Кусочек ${piece.id + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = -size.width * col
                                translationY = -size.height * row
                                scaleX = 3f
                                scaleY = 2f
                            },
                        contentScale = ContentScale.FillBounds
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${piece.id + 1}",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
