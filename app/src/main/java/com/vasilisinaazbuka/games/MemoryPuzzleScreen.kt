package com.vasilisinaazbuka.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Игра «Собери картинку по памяти» — 5 уровней с пазлами
 * 
 * Картинка автоматически нарезается на кусочки уникальной формы (как настоящий пазл).
 * Сетка 3×2 = 6 кусочков.
 * Кусочки имеют случайные пазловые выступы и выемки по краям.
 * Drag-and-drop для перемещения кусочков.
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

    // Состояния
    var showImage by remember { mutableStateOf(true) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }
    var viewCount by remember { mutableIntStateOf(0) }

    // Генерация кусочков пазла
    data class PuzzlePiece(
        val id: Int,
        val correctPosition: Int,      // Правильная позиция (0-5)
        val topTab: Float,             // Выступ/выемка сверху (-1 до 1, 0 = прямой)
        val bottomTab: Float,          // Выступ/выемка снизу
        val leftTab: Float,            // Выступ/выемка слева
        val rightTab: Float,           // Выступ/выемка справа
        val shapeSeed: Int             // Сид для генерации формы
    )

    // Генерируем кусочки с уникальными формами
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

    // Перемешанные кусочки для выбора
    var availablePieces by remember { mutableStateOf(allPieces.shuffled()) }
    
    // Размещённые кусочки (позиция -> кусочек)
    var placedPieces by remember { mutableStateOf(mutableMapOf<Int, PuzzlePiece>()) }
    
    // Выбранный кусочек для перетаскивания
    var selectedPieceId by remember { mutableIntStateOf(-1) }
    
    // Drag-and-drop состояние
    var draggedPieceId by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dropTargetPosition by remember { mutableIntStateOf(-1) }

    // Показ картинки на 3 секунды
    LaunchedEffect(stage) {
        showImage = true
        availablePieces = allPieces.shuffled()
        placedPieces = mutableMapOf()
        selectedPieceId = -1
        draggedPieceId = -1
        viewCount = 0
        delay(3000)
        showImage = false
    }

    // Проверка правильности сборки
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

    // Основной layout для ландшафтной ориентации
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
            // Левая панель — информация и управление
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

                    StageProgressIndicator(
                        currentStage = stage,
                        maxStages = 5,
                        compact = true
                    )
                }

                CharacterView(
                    character = "vasilisa",
                    emotion = if (showImage) "teacher" else "thinking",
                    message = if (showImage) "Запомни картинку!" 
                              else "Перетащи кусочки\nна правильные места!",
                    modifier = Modifier.fillMaxWidth()
                )

                // Кнопки управления
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!showImage) {
                        Button(
                            onClick = {
                                viewCount++
                                showImage = true
                                // Показываем картинку ещё раз
                                kotlinx.coroutines.MainScope().launch {
                                    delay(3000)
                                    showImage = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FairyBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("👁️ Посмотреть\nещё раз", fontSize = 13.sp, color = Color.White, textAlign = TextAlign.Center)
                        }

                        Button(
                            onClick = {
                                availablePieces = allPieces.shuffled()
                                placedPieces = mutableMapOf()
                                selectedPieceId = -1
                                AudioPlayer.playSFX(R.raw.sfx_reset)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FairyPink),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🔄 Перемешать", fontSize = 14.sp, color = Color.White)
                        }

                        if (selectedPieceId >= 0) {
                            Button(
                                onClick = { selectedPieceId = -1 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
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

            // Правая часть — область пазла
            if (showImage) {
                // Показ полной картинки
                Card(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
                        .aspectRatio(1.5f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(currentImage),
                            contentDescription = "Запомни картинку",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Оверлей с таймером
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
                // Сборка пазла
                Column(
                    modifier = Modifier.weight(0.7f)
                ) {
                    // Сетка для сборки 3×2
                    PuzzleGrid(
                        pieces = placedPieces,
                        allPieces = allPieces,
                        currentImage = currentImage,
                        selectedPieceId = selectedPieceId,
                        dropTargetPosition = dropTargetPosition,
                        onCellClick = { position ->
                            // Если есть выбранный кусочек — помещаем в эту ячейку
                            if (selectedPieceId >= 0 && !placedPieces.containsKey(position)) {
                                val piece = allPieces.find { it.id == selectedPieceId }
                                if (piece != null) {
                                    placedPieces = placedPieces.toMutableMap().apply {
                                        put(position, piece)
                                    }
                                    availablePieces = availablePieces.filter { it.id != selectedPieceId }
                                    selectedPieceId = -1
                                    AudioPlayer.playSFX(R.raw.sfx_drop)
                                }
                            }
                            // Если ячейка занята — убираем кусочек обратно
                            else if (placedPieces.containsKey(position) && selectedPieceId < 0) {
                                val piece = placedPieces[position]!!
                                placedPieces = placedPieces.toMutableMap().apply {
                                    remove(position)
                                }
                                availablePieces = availablePieces + piece
                                AudioPlayer.playSFX(R.raw.sfx_click)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Доступные кусочки
                    Text(
                        text = "Доступные кусочки (выбери и нажми на ячейку):",
                        style = MaterialTheme.typography.bodySmall,
                        color = FairyPurple,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    AvailablePiecesRow(
                        pieces = availablePieces,
                        allPieces = allPieces,
                        currentImage = currentImage,
                        selectedPieceId = selectedPieceId,
                        onPieceClick = { pieceId ->
                            selectedPieceId = if (selectedPieceId == pieceId) -1 else pieceId
                            AudioPlayer.playSFX(R.raw.sfx_click)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                }
            }
        }

        // Окно завершения уровня
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

/**
 * Сетка пазла 3×2 с кусочками
 */
@Composable
private fun PuzzleGrid(
    pieces: Map<Int, PuzzlePiece>,
    allPieces: List<PuzzlePiece>,
    currentImage: Int,
    selectedPieceId: Int,
    dropTargetPosition: Int,
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
        itemsIndexed(List(rows * cols) { it }) { index, position ->
            val piece = pieces[position]
            val isTarget = dropTargetPosition == position
            val isCorrect = piece != null && piece.correctPosition == position

            val bgColor by animateColorAsState(
                targetValue = when {
                    isTarget && selectedPieceId >= 0 -> FairyGold.copy(alpha = 0.4f)
                    piece != null && isCorrect -> FairyGreen.copy(alpha = 0.2f)
                    piece != null && !isCorrect -> FairyPink.copy(alpha = 0.15f)
                    else -> Color.Gray.copy(alpha = 0.2f)
                },
                animationSpec = spring(),
                label = "cellBg"
            )

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(bgColor)
                    .border(
                        width = if (isTarget) 3.dp else 2.dp,
                        color = when {
                            isTarget -> FairyGold
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
                    // Отображаем кусочек картинки с пазловой формой
                    PuzzlePieceView(
                        piece = piece,
                        allPieces = allPieces,
                        imageRes = currentImage,
                        rows = 2,
                        cols = 3
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

/**
 * Отображение одного кусочка пазла с уникальной формой
 */
@Composable
private fun PuzzlePieceView(
    piece: PuzzlePiece,
    allPieces: List<PuzzlePiece>,
    imageRes: Int,
    rows: Int,
    cols: Int
) {
    val row = piece.correctPosition / cols
    val col = piece.correctPosition % cols

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(PuzzlePieceShape(piece))
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = "Кусочек пазла ${piece.id + 1}",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Сдвигаем изображение, чтобы показать нужный фрагмент
                    translationX = -size.width * col
                    translationY = -size.height * row
                    
                    // Масштабируем, чтобы показать всю картинку в одном кусочке
                    scaleX = cols.toFloat()
                    scaleY = rows.toFloat()
                }
                .clipToBounds(),
            contentScale = ContentScale.FillBounds
        )
        
        // Обводка кусочка
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val path = PuzzlePieceShape(piece).createOutline(
                size = Size(size.width, size.height),
                layoutDirection = LayoutDirection.Ltr,
                density = Density(1f)
            ).let { outline ->
                when (outline) {
                    is Outline.Generic -> outline.path
                    else -> Path()
                }
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.3f),
                style = Stroke(width = 2f)
            )
        }
    }
}

/**
 * Форма кусочка пазла с выступами и выемками
 */
private class PuzzlePieceShape(
    private val piece: PuzzlePiece
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val w = size.width
        val h = size.height
        val tabSize = min(w, h) * 0.25f

        // Начинаем с левого верхнего угла
        path.moveTo(0f, 0f)

        // Верхняя сторона
        path.lineTo(w / 2 - tabSize, 0f)
        addTab(path, w / 2, 0f, w / 2, -tabSize * piece.topTab, tabSize)
        path.lineTo(w, 0f)

        // Правая сторона
        path.lineTo(w, h / 2 - tabSize)
        addTab(path, w, h / 2, w + tabSize * piece.rightTab, h / 2, tabSize)
        path.lineTo(w, h)

        // Нижняя сторона
        path.lineTo(w / 2 + tabSize, h)
        addTab(path, w / 2, h, w / 2, h + tabSize * piece.bottomTab, tabSize)
        path.lineTo(0f, h)

        // Левая сторона
        path.lineTo(0f, h / 2 + tabSize)
        addTab(path, 0f, h / 2, -tabSize * piece.leftTab, h / 2, tabSize)
        path.lineTo(0f, 0f)

        path.close()
        return Outline.Generic(path)
    }

    /**
     * Добавляет выступ или выемку на сторону кусочка
     */
    private fun addTab(
        path: Path,
        startX: Float, startY: Float,
        controlX: Float, controlY: Float,
        tabSize: Float
    ) {
        val sign = if (controlY < startY || controlX > startX) 1f else -1f
        
        path.cubicTo(
            startX + (controlX - startX) * 0.5f, startY,
            controlX, startY + (controlY - startY) * 0.3f * sign,
            controlX, controlY
        )
        path.cubicTo(
            controlX, startY + (controlY - startY) * 0.7f * sign,
            startX + (controlX - startX) * 0.5f, startY + (controlY - startY) * sign,
            startX + (controlX - startX), startY + (controlY - startY)
        )
    }
}

/**
 * Ряд доступных кусочков для выбора
 */
@Composable
private fun AvailablePiecesRow(
    pieces: List<PuzzlePiece>,
    allPieces: List<PuzzlePiece>,
    currentImage: Int,
    selectedPieceId: Int,
    onPieceClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pieces.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Все кусочки размещены!",
                style = MaterialTheme.typography.bodyMedium,
                color = FairyGreen,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(min(pieces.size, 6)),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(pieces) { index, piece ->
            val isSelected = selectedPieceId == piece.id
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1f,
                animationSpec = spring(),
                label = "pieceScale"
            )

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) FairyGold.copy(alpha = 0.4f)
                        else Color.White
                    )
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) FairyGold else Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onPieceClick(piece.id) }
                    .then(
                        if (isSelected) Modifier.zIndex(1f) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Миниатюра кусочка
                val row = piece.correctPosition / 3
                val col = piece.correctPosition % 3

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

                // Номер кусочка
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

// Вспомогательная функция для корутин
private fun kotlinx.coroutines.MainScope() = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
private fun kotlinx.coroutines.CoroutineScope.launch(block: suspend () -> Unit) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) { block() }
}
