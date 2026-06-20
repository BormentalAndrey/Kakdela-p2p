package com.vasilisinaazbuka.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vasilisinaazbuka.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Компонент для отображения завершения уровня с анимацией звёзд
 * Оптимизирован для ландшафтной ориентации
 *
 * @param stars Количество звёзд (0-3)
 * @param message Поздравительное сообщение
 * @param character Персонаж для отображения ("vasilisa" или "knopa")
 * @param coins Количество полученных монет (для тамагочи)
 * @param onNext Действие при нажатии на кнопку «Далее»
 * @param onReplay Действие при нажатии на кнопку «Заново» (опционально)
 */
@Composable
fun LevelComplete(
    stars: Int = 3,
    message: String = "Уровень пройден!",
    character: String = "vasilisa",
    coins: Int = 0,
    onNext: () -> Unit = {},
    onReplay: (() -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }
    var showStars by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(300)
        showStars = true
        delay(1000)
        showButtons = true
    }

    // Анимация конфетти
    val confettiColors = listOf(FairyGold, FairyPink, FairyGreen, FairyPurple, FairyBlue)
    val confettiCount = 20

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + 
                scaleIn(initialScale = 0.8f, animationSpec = tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            // Конфетти
            repeat(confettiCount) { index ->
                ConfettiPiece(
                    color = confettiColors[index % confettiColors.size],
                    delay = index * 50L,
                    index = index
                )
            }

            // Основная карточка
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .fillMaxHeight(0.75f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Левая часть — персонаж и звёзды
                    Column(
                        modifier = Modifier.weight(0.45f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Персонаж
                        CharacterCelebration(
                            character = character,
                            stars = stars
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Звёзды
                        AnimatedVisibility(
                            visible = showStars,
                            enter = fadeIn() + scaleIn()
                        ) {
                            StarDisplay(stars = stars)
                        }
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Правая часть — сообщение и кнопки
                    Column(
                        modifier = Modifier.weight(0.55f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Заголовок
                        Text(
                            text = when (stars) {
                                3 -> "🎉 Великолепно! 🎉"
                                2 -> "👏 Хорошо! 👏"
                                1 -> "👍 Неплохо! 👍"
                                else -> "💪 Попробуй ещё! 💪"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = FairyGold,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Сообщение
                        Text(
                            text = message,
                            style = MaterialTheme.typography.titleMedium,
                            color = FairyPurple,
                            textAlign = TextAlign.Center,
                            maxLines = 4
                        )

                        // Монеты (если есть)
                        if (coins > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🪙", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+$coins монет",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FairyGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Кнопки
                        AnimatedVisibility(
                            visible = showButtons,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 }
                            ) + fadeIn()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Кнопка «Далее»
                                Button(
                                    onClick = onNext,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FairyGreen
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp
                                    )
                                ) {
                                    Text(
                                        text = "➡️ Далее",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                // Кнопка «Заново» (если нужна)
                                if (onReplay != null && stars < 3) {
                                    OutlinedButton(
                                        onClick = onReplay,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = FairyBlue
                                        )
                                    ) {
                                        Text(
                                            text = "🔄 Попробовать ещё раз",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Анимированный персонаж в окне завершения
 */
@Composable
private fun CharacterCelebration(
    character: String,
    stars: Int
) {
    val bounce by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val rotation by rememberInfiniteTransition().animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val emoji = when {
        character == "knopa" && stars == 3 -> "😸"
        character == "knopa" && stars == 2 -> "😺"
        character == "knopa" && stars == 1 -> "😼"
        character == "knopa" -> "😿"
        character == "vasilisa" && stars == 3 -> "👧🌟"
        character == "vasilisa" && stars == 2 -> "👧😊"
        character == "vasilisa" && stars == 1 -> "👧"
        else -> "👧"
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        FairyGold.copy(alpha = 0.3f),
                        FairyGold.copy(alpha = 0.1f)
                    )
                )
            )
            .border(3.dp, FairyGold, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 50.sp,
            modifier = Modifier
                .offset(y = bounce.dp)
                .rotate(rotation)
        )
    }
}

/**
 * Частица конфетти
 */
@Composable
private fun ConfettiPiece(
    color: Color,
    delay: Long,
    index: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay)
        visible = true
    }

    val randomX = remember { kotlin.random.Random.nextFloat() }
    val randomY = remember { kotlin.random.Random.nextFloat() }
    val randomRotation = remember { kotlin.random.Random.nextFloat() * 360f }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.TopStart)
        ) {
            Text(
                text = listOf("⭐", "✨", "🌟", "💫", "🎉")[index % 5],
                fontSize = (12 + index % 8).sp,
                color = color,
                modifier = Modifier
                    .offset(
                        x = (randomX * 600).dp,
                        y = (randomY * 300).dp
                    )
                    .rotate(randomRotation)
            )
        }
    }
}

/**
 * Отображение звёзд с анимацией появления
 */
@Composable
fun StarDisplay(stars: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            var starVisible by remember { mutableStateOf(false) }
            var starBounced by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(index * 300L) // Задержка для последовательного появления
                starVisible = true
                delay(200)
                starBounced = true
            }

            val starScale by animateFloatAsState(
                targetValue = if (starBounced && index < stars) 1.2f else 1f,
                animationSpec = spring(dampingRatio = 0.3f, stiffness = 100f),
                label = "starScale"
            )

            AnimatedVisibility(
                visible = starVisible,
                enter = scaleIn(
                    animationSpec = spring(dampingRatio = 0.5f)
                ) + fadeIn()
            ) {
                Text(
                    text = if (index < stars) "⭐" else "☆",
                    fontSize = 44.sp,
                    color = if (index < stars) FairyGold else Color.Gray.copy(alpha = 0.4f),
                    modifier = Modifier
                        .padding(8.dp)
                        .scale(starScale)
                )
            }
        }
    }
}

/**
 * Упрощённая версия для использования в мини-играх
 */
@Composable
fun MiniLevelComplete(
    message: String,
    onNext: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FairyGreen.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "✅ $message",
                    style = MaterialTheme.typography.titleSmall,
                    color = FairyGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = FairyGreen)
                ) {
                    Text("Далее", color = Color.White)
                }
            }
        }
    }
}
