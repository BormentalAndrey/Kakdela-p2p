package com.vasilisinaazbuka.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vasilisinaazbuka.ui.theme.*

/**
 * Компонент для отображения персонажа (Василиса или Кузя) с эмоцией и сообщением
 *
 * @param character Имя персонажа: "vasilisa" или "kuzya"
 * @param emotion Эмоция персонажа: happy, sad, neutral, proud, teacher, hungry, thinking
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
    // Определяем эмодзи и цвет в зависимости от персонажа и эмоции
    val (emoji, bgColor, borderColor) = when {
        character == "vasilisa" && emotion == "happy" -> Triple("👧✨", FairyGold.copy(alpha = 0.2f), FairyGold)
        character == "vasilisa" && emotion == "proud" -> Triple("👧🌟", FairyPurple.copy(alpha = 0.2f), FairyPurple)
        character == "vasilisa" && emotion == "teacher" -> Triple("👧📚", FairyBlue.copy(alpha = 0.2f), FairyBlue)
        character == "vasilisa" && emotion == "thinking" -> Triple("👧🤔", FairyGreen.copy(alpha = 0.2f), FairyGreen)
        character == "vasilisa" -> Triple("👧", FairyBlue.copy(alpha = 0.2f), FairyBlue)

        character == "kuzya" && emotion == "happy" -> Triple("🐱✨", FairyGold.copy(alpha = 0.2f), FairyGold)
        character == "kuzya" && emotion == "hungry" -> Triple("🐱🍽️", FairyPink.copy(alpha = 0.2f), FairyPink)
        character == "kuzya" && emotion == "neutral" -> Triple("🐱", FairyGreen.copy(alpha = 0.2f), FairyGreen)
        character == "kuzya" && emotion == "sad" -> Triple("🐱😿", Color.Gray.copy(alpha = 0.2f), Color.Gray)
        character == "kuzya" -> Triple("🐱", FairyGreen.copy(alpha = 0.2f), FairyGreen)

        else -> Triple("❓", Color.Gray.copy(alpha = 0.2f), Color.Gray)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар персонажа
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .border(3.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Сообщение персонажа
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = FairyPurple,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
