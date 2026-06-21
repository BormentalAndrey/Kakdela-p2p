package com.vasilisinaazbuka.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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

// Точка линии рисования
private data class DrawPoint(val x: Float, val y: Float)

@Composable
fun ColoringScreen(stage: Int = 1, onNextStage: () -> Unit = {}, onGameComplete: () -> Unit = {}, onBack: () -> Unit = {}) {
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var brushSize by remember { mutableFloatStateOf(25f) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }

    // История линий: каждая линия = список точек + цвет + размер кисти
    var lines by remember { mutableStateOf(listOf<Triple<List<Offset>, Color, Float>>()) }
    var currentLine by remember { mutableStateOf(mutableListOf<Offset>()) }
    var isDrawing by remember { mutableStateOf(false) }

    val colorPalette = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan, Color(0xFFFFA500), Color(0xFF8B4513))
    val levelImages = listOf(R.drawable.coloring_matryoshka, R.drawable.coloring_kremlin, R.drawable.coloring_samovar, R.drawable.coloring_birch, R.drawable.coloring_balalaika)

    LaunchedEffect(stage) { lines = emptyList(); currentLine.clear(); selectedColor = Color.Red; brushSize = 25f; showLevelComplete = false }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_level_coloring), "Фон", Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)

        Row(Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Левая панель (1/3)
            Column(Modifier.weight(0.33f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Раскраска", style = MaterialTheme.typography.titleLarge, color = FairyGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                StageProgressIndicator(currentStage = stage, maxStages = 5, compact = true)
                Spacer(Modifier.height(12.dp))

                CharacterView("vasilisa", "happy", "Рисуй на картинке!\nВыбери цвет и рисуй\nпальчиком.", Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))

                // Палитра
                Text("Цвет:", style = MaterialTheme.typography.bodySmall, color = FairyPurple)
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    colorPalette.take(4).forEach { color ->
                        Box(Modifier.size(38.dp).clip(CircleShape).background(color).border(if (selectedColor == color) 3.dp else 1.dp, if (selectedColor == color) Color.Black else Color.Gray, CircleShape).clickable { selectedColor = color; AudioPlayer.playSFX(R.raw.sfx_click) })
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    colorPalette.drop(4).forEach { color ->
                        Box(Modifier.size(38.dp).clip(CircleShape).background(color).border(if (selectedColor == color) 3.dp else 1.dp, if (selectedColor == color) Color.Black else Color.Gray, CircleShape).clickable { selectedColor = color; AudioPlayer.playSFX(R.raw.sfx_click) })
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Размер кисти
                Text("Размер кисти: ${brushSize.toInt()}", style = MaterialTheme.typography.bodySmall, color = FairyPurple)
                Slider(value = brushSize, onValueChange = { brushSize = it }, valueRange = 5f..80f, modifier = Modifier.fillMaxWidth(), colors = SliderDefaults.colors(thumbColor = selectedColor, activeTrackColor = selectedColor))

                Spacer(Modifier.height(12.dp))

                // Кнопки
                Button(onClick = { selectedColor = Color.White; AudioPlayer.playSFX(R.raw.sfx_click) }, Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) { Text("🧹 Ластик", fontSize = 14.sp) }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = { lines = emptyList(); AudioPlayer.playSFX(R.raw.sfx_reset) }, Modifier.fillMaxWidth().height(40.dp)) { Text("🔄 Заново", fontSize = 14.sp, color = FairyPink) }
                Spacer(Modifier.height(6.dp))
                Button(onClick = { showLevelComplete = true; stars = 3; GameState.completeLevel("coloring", stage); AudioPlayer.playSFX(R.raw.sfx_success) }, Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyGreen)) { Text("✅ Готово!", fontSize = 14.sp, color = Color.White) }
            }

            Spacer(Modifier.width(12.dp))

            // Правая часть — холст (2/3)
            Card(Modifier.weight(0.67f).fillMaxHeight().aspectRatio(1f), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                Box(Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color.White).border(2.dp, FairyGold, RoundedCornerShape(16.dp)).clipToBounds()) {
                    // Картинка-основа
                    Image(painterResource(levelImages.getOrElse(stage - 1) { R.drawable.coloring_matryoshka }), "Раскраска", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)

                    // Слой рисования
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        isDrawing = true
                                        currentLine.clear()
                                        currentLine.add(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentLine.add(change.position)
                                    },
                                    onDragEnd = {
                                        if (currentLine.isNotEmpty()) {
                                            lines = lines + Triple(currentLine.toList(), selectedColor, brushSize)
                                        }
                                        currentLine.clear()
                                        isDrawing = false
                                    },
                                    onDragCancel = {
                                        currentLine.clear()
                                        isDrawing = false
                                    }
                                )
                            }
                    ) {
                        // Рисуем сохранённые линии
                        lines.forEach { (points, color, size) ->
                            if (points.size >= 2) {
                                val path = Path().apply {
                                    moveTo(points[0].x, points[0].y)
                                    for (i in 1 until points.size) {
                                        lineTo(points[i].x, points[i].y)
                                    }
                                }
                                drawPath(path = path, color = color, style = Stroke(width = size, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            } else if (points.size == 1) {
                                drawCircle(color = color, radius = size / 2, center = points[0])
                            }
                        }

                        // Рисуем текущую линию
                        if (currentLine.size >= 2) {
                            val path = Path().apply {
                                moveTo(currentLine[0].x, currentLine[0].y)
                                for (i in 1 until currentLine.size) {
                                    lineTo(currentLine[i].x, currentLine[i].y)
                                }
                            }
                            drawPath(path = path, color = selectedColor, style = Stroke(width = brushSize, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }
                }
            }
        }

        if (showLevelComplete) LevelComplete(stars, "Отлично! Картинка раскрашена!", onNext = { if (stage < 5) onNextStage() else onGameComplete() })
    }
}
