package com.vasilisinaazbuka.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vasilisinaazbuka.R
import com.vasilisinaazbuka.ui.theme.*

@Composable
fun CharacterView(character: String, emotion: String = "neutral", message: String = "", modifier: Modifier = Modifier) {
    val pulseScale by rememberInfiniteTransition().animateFloat(1f, 1.1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "pulse")
    
    val (avatarRes, bgColor, borderColor, shouldPulse) = when {
        // ==================== ВАСИЛИСА ====================
        character == "vasilisa" && emotion == "happy" -> 
            Quad(R.drawable.character_vasilisa_happy, FairyGold.copy(alpha = 0.2f), FairyGold, true)
        character == "vasilisa" && emotion == "proud" -> 
            Quad(R.drawable.character_vasilisa_proud, FairyPurple.copy(alpha = 0.2f), FairyPurple, false)
        character == "vasilisa" && emotion == "teacher" -> 
            Quad(R.drawable.character_vasilisa_teacher, FairyBlue.copy(alpha = 0.2f), FairyBlue, false)
        character == "vasilisa" && emotion == "thinking" -> 
            Quad(R.drawable.character_vasilisa_thinking, FairyGreen.copy(alpha = 0.2f), FairyGreen, false)
        character == "vasilisa" && emotion == "sad" -> 
            Quad(R.drawable.character_vasilisa_sad, Color.Gray.copy(alpha = 0.2f), Color.Gray, false)
        character == "vasilisa" && emotion == "surprised" -> 
            Quad(R.drawable.character_vasilisa_surprised, FairyPink.copy(alpha = 0.2f), FairyPink, false)
        character == "vasilisa" -> 
            Quad(R.drawable.character_vasilisa_happy, FairyBlue.copy(alpha = 0.2f), FairyBlue, false)

        // ==================== КНОПА ====================
        character == "knopa" && emotion == "happy" -> 
            Quad(R.drawable.character_kuzya_happy, FairyGold.copy(alpha = 0.2f), FairyGold, true)
        character == "knopa" && emotion == "ecstatic" -> 
            Quad(R.drawable.character_kuzya_ecstatic, FairyGold.copy(alpha = 0.3f), FairyGold, true)
        character == "knopa" && emotion == "hungry" -> 
            Quad(R.drawable.character_kuzya_hungry, FairyPink.copy(alpha = 0.2f), FairyPink, false)
        character == "knopa" && emotion == "neutral" -> 
            Quad(R.drawable.character_kuzya_neutral, FairyGreen.copy(alpha = 0.2f), FairyGreen, false)
        character == "knopa" && emotion == "sad" -> 
            Quad(R.drawable.character_kuzya_sad, Color.Gray.copy(alpha = 0.2f), Color.Gray, false)
        character == "knopa" && emotion == "sleeping" -> 
            Quad(R.drawable.character_kuzya_sleeping, FairyBlue.copy(alpha = 0.2f), FairyBlue, false)
        character == "knopa" && emotion == "sick" -> 
            Quad(R.drawable.character_kuzya_sick, Color.Red.copy(alpha = 0.2f), Color.Red, false)
        character == "knopa" && emotion == "dirty" -> 
            Quad(R.drawable.character_kuzya_dirty, Color(0xFF8B7355).copy(alpha = 0.2f), Color(0xFF8B7355), false)
        character == "knopa" && emotion == "angry" -> 
            Quad(R.drawable.character_kuzya_angry, Color.Red.copy(alpha = 0.3f), Color.Red, false)
        character == "knopa" && emotion == "sleepy" -> 
            Quad(R.drawable.character_kuzya_sleeping, FairyPurple.copy(alpha = 0.2f), FairyPurple, false)
        character == "knopa" && emotion == "playing" -> 
            Quad(R.drawable.character_kuzya_playing, FairyGreen.copy(alpha = 0.3f), FairyGreen, true)
        character == "knopa" && emotion == "loved" -> 
            Quad(R.drawable.character_kuzya_happy, FairyPink.copy(alpha = 0.3f), FairyPink, true)
        character == "knopa" -> 
            Quad(R.drawable.character_kuzya_neutral, FairyGreen.copy(alpha = 0.2f), FairyGreen, false)

        // ==================== ОБРАТНАЯ СОВМЕСТИМОСТЬ (kuzya) ====================
        character == "kuzya" && emotion == "happy" -> 
            Quad(R.drawable.character_kuzya_happy, FairyGold.copy(alpha = 0.2f), FairyGold, true)
        character == "kuzya" && emotion == "hungry" -> 
            Quad(R.drawable.character_kuzya_hungry, FairyPink.copy(alpha = 0.2f), FairyPink, false)
        character == "kuzya" && emotion == "neutral" -> 
            Quad(R.drawable.character_kuzya_neutral, FairyGreen.copy(alpha = 0.2f), FairyGreen, false)
        character == "kuzya" && emotion == "sad" -> 
            Quad(R.drawable.character_kuzya_sad, Color.Gray.copy(alpha = 0.2f), Color.Gray, false)
        character == "kuzya" && emotion == "sleeping" -> 
            Quad(R.drawable.character_kuzya_sleeping, FairyBlue.copy(alpha = 0.2f), FairyBlue, false)
        character == "kuzya" -> 
            Quad(R.drawable.character_kuzya_neutral, FairyGreen.copy(alpha = 0.2f), FairyGreen, false)

        else -> Quad(R.drawable.character_vasilisa_happy, Color.Gray.copy(alpha = 0.2f), Color.Gray, false)
    }

    Card(modifier.animateContentSize(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
        Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(bgColor).border(2.dp, borderColor, CircleShape).then(if (shouldPulse) Modifier.scale(pulseScale) else Modifier), contentAlignment = Alignment.Center) {
                Image(painterResource(avatarRes), "Персонаж", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Spacer(Modifier.width(8.dp))
            if (message.isNotEmpty()) {
                Column(Modifier.weight(1f)) {
                    Text(when { character == "vasilisa" -> "Василиса"; character == "knopa" || character == "kuzya" -> "Кнопа"; else -> "" }, style = MaterialTheme.typography.labelSmall, color = borderColor, fontWeight = FontWeight.Bold)
                    Text(message, style = MaterialTheme.typography.bodySmall, color = FairyPurple, fontWeight = FontWeight.Medium, textAlign = TextAlign.Start, maxLines = 2)
                }
            } else {
                Text(when { character == "vasilisa" -> "Василиса"; character == "knopa" || character == "kuzya" -> "Кнопа"; else -> "" }, style = MaterialTheme.typography.bodySmall, color = borderColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
