package com.vasilisinaazbuka.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vasilisinaazbuka.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Компонент для отображения завершения уровня с анимацией звёзд
 *
 * @param stars Количество звёзд (0-3)
 * @param message Поздравительное сообщение
 * @param onNext Действие при нажатии на кнопку «Далее»
 */
@Composable
fun LevelComplete(
    stars: Int = 3,
    message: String = "Уровень пройден!",
    onNext: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Заголовок
                    Text(
                        text = "🎉 Поздравляем! 🎉",
                        style = MaterialTheme.typography.headlineSmall,
                        color = FairyGold,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Сообщение
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        color = FairyPurple,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Звёзды
                    StarDisplay(stars = stars)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Кнопка «Далее»
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FairyGreen
                        )
                    ) {
                        Text(
                            text = "➡️ Далее",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
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

            LaunchedEffect(Unit) {
                delay(index * 300L) // Задержка для последовательного появления
                starVisible = true
            }

            AnimatedVisibility(
                visible = starVisible,
                enter = scaleIn() + fadeIn()
            ) {
                Text(
                    text = if (index < stars) "⭐" else "☆",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
