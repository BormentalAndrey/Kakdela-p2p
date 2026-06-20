package com.vasilisinaazbuka.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vasilisinaazbuka.ui.theme.*

/**
 * Компонент для отображения персонажа (Василиса или Кнопа) с эмоцией и сообщением
 * Оптимизирован для ландшафтной ориентации
 *
 * @param character Имя персонажа: "vasilisa" или "knopa"
 * @param emotion Эмоция персонажа: happy, sad, neutral, proud, teacher, hungry, thinking, sleeping, sick
 * @param message Текст сообщения от персонажа
 * @param modifier Модификатор композабла
 */
@Composable
fun CharacterView(
    character: String,
    emotion: String = "neutral",
    message: String = "",
    modifier: Modifier = Modifier
) {
    // Анимация пульсации для счастливого персонажа
    val pulseScale by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Определяем эмодзи и цвет в зависимости от персонажа и эмоции
    val (emoji, bgColor, borderColor, shouldPulse) = when {
        // Василиса
        character == "vasilisa" && emotion == "happy" -> 
            Quad("👧✨", FairyGold.copy(alpha = 0.2f), FairyGold, true)
        character == "vasilisa" && emotion == "proud" -> 
            Quad("👧🌟", FairyPurple.copy(alpha = 0.2f), FairyPurple, false)
        character == "vasilisa" && emotion == "teacher" -> 
            Quad("👧📚", FairyBlue.copy(alpha = 0.2f), FairyBlue, false)
        character == "vasilisa" && emotion == "thinking" -> 
            Quad("👧🤔", FairyGreen.copy(alpha = 0.2f), FairyGreen, false)
        character == "vasilisa" && emotion == "sad" -> 
            Quad("👧😢", Color.Gray.copy(alpha = 0.2f), Color.Gray, false)
        character == "vasilisa" && emotion == "surprised" -> 
            Quad("👧😮", FairyPink.copy(alpha = 0.2f), FairyPink, false)
        character == "vasilisa" -> 
            Quad("👧", FairyBlue.copy(alpha = 0.2f), FairyBlue, false)

        // Кнопа (кот)
        character == "knopa" && emotion == "happy" -> 
            Quad("🐱✨", FairyGold.copy(alpha = 0.2f), FairyGold, true)
        character == "knopa" && emotion == "ecstatic" -> 
            Quad("😸💫", FairyGold.copy(alpha = 0.3f), FairyGold, true)
        character == "knopa" && emotion == "hungry" -> 
            Quad("😿🍽️", FairyPink.copy(alpha = 0.2f), FairyPink, false)
        character == "knopa" && emotion == "neutral" -> 
            Quad("🐱", FairyGreen.copy(alpha = 0.2f), FairyGreen, false)
        character == "knopa" && emotion == "sad" -> 
            Quad("😾💧", Color.Gray.copy(alpha = 0.2f), Color.Gray, false)
        character == "knopa" && emotion == "sleeping" -> 
            Quad("😴💤", FairyBlue.copy(alpha = 0.2f), FairyBlue, false)
        character == "knopa" && emotion == "sick" -> 
            Quad("🤒🤕", Color.Red.copy(alpha = 0.2f), Color.Red, false)
        character == "knopa" && emotion == "dirty" -> 
            Quad("😼💨", Color(0xFF8B7355).copy(alpha = 0.2f), Color(0xFF8B7355), false)
        character == "knopa" && emotion == "angry" -> 
            Quad("🙀💢", Color.Red.copy(alpha = 0.3f), Color.Red, false)
        character == "knopa" && emotion == "sleepy" -> 
            Quad("😴", FairyPurple.copy(alpha = 0.2f), FairyPurple, false)
        character == "knopa" && emotion == "playing" -> 
            Quad("🐱🎾", FairyGreen.copy(alpha = 0.3f), FairyGreen, true)
        character == "knopa" && emotion == "loved" -> 
            Quad("😻❤️", FairyPink.copy(alpha = 0.3f), FairyPink, true)
        character == "knopa" -> 
            Quad("🐱", FairyGreen.copy(alpha = 0.2f), FairyGreen, false)

        // Для обратной совместимости (старое имя "kuzya")
        character == "kuzya" && emotion == "happy" -> 
            Quad("🐱✨", FairyGold.copy(alpha = 0.2f), FairyGold, true)
        character == "kuzya" && emotion == "hungry" -> 
            Quad("😿🍽️", FairyPink.copy(alpha = 0.2f), FairyPink, false)
        character == "kuzya" && emotion == "neutral" -> 
            Quad("🐱", FairyGreen.copy(alpha = 0.2f), FairyGreen, false)
        character == "kuzya" && emotion == "sad" -> 
            Quad("😾💧", Color.Gray.copy(alpha = 0.2f), Color.Gray, false)
        character == "kuzya" && emotion == "sleeping" -> 
            Quad("😴💤", FairyBlue.copy(alpha = 0.2f), FairyBlue, false)
        character == "kuzya" -> 
            Quad("🐱", FairyGreen.copy(alpha = 0.2f), FairyGreen, false)

        else -> Quad("❓", Color.Gray.copy(alpha = 0.2f), Color.Gray, false)
    }

    Card(
        modifier = modifier
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар персонажа с анимацией
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .border(2.dp, borderColor, CircleShape)
                    .then(
                        if (shouldPulse) Modifier.scale(pulseScale) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Имя персонажа и сообщение
            if (message.isNotEmpty()) {
                Column(modifier = Modifier.weight(1f)) {
                    // Имя персонажа
                    Text(
                        text = when {
                            character == "vasilisa" -> "Василиса"
                            character == "knopa" || character == "kuzya" -> "Кнопа"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = borderColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Сообщение
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = FairyPurple,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start,
                        maxLines = 2
                    )
                }
            } else {
                // Только имя если нет сообщения
                Text(
                    text = when {
                        character == "vasilisa" -> "Василиса"
                        character == "knopa" || character == "kuzya" -> "Кнопа"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = borderColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Вспомогательный класс для хранения 4 значений
 */
private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
