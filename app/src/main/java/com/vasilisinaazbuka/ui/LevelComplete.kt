package com.vasilisinaazbuka.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vasilisinaazbuka.R
import com.vasilisinaazbuka.ui.theme.*
import kotlinx.coroutines.delay

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

    LaunchedEffect(Unit) { visible = true; delay(300); showStars = true; delay(1000); showButtons = true }

    val confettiColors = listOf(FairyGold, FairyPink, FairyGreen, FairyPurple, FairyBlue)
    val confettiCount = 20

    AnimatedVisibility(visible = visible, enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.8f, animationSpec = tween(500))) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(enabled = false) { }, contentAlignment = Alignment.Center) {
            repeat(confettiCount) { index -> ConfettiPiece(confettiColors[index % confettiColors.size], index * 50L, index) }

            Card(Modifier.fillMaxWidth(0.55f).fillMaxHeight(0.75f).padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(16.dp)) {
                Row(Modifier.fillMaxSize().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(0.45f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CharacterCelebration(character, stars)
                        Spacer(Modifier.height(16.dp))
                        AnimatedVisibility(visible = showStars, enter = fadeIn() + scaleIn()) { StarDisplay(stars) }
                    }
                    Spacer(Modifier.width(24.dp))
                    Column(Modifier.weight(0.55f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(when (stars) { 3 -> "🎉 Великолепно! 🎉"; 2 -> "👏 Хорошо! 👏"; 1 -> "👍 Неплохо! 👍"; else -> "💪 Попробуй ещё! 💪" }, style = MaterialTheme.typography.headlineSmall, color = FairyGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Text(message, style = MaterialTheme.typography.titleMedium, color = FairyPurple, textAlign = TextAlign.Center, maxLines = 4)
                        if (coins > 0) { Spacer(Modifier.height(8.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Text("🪙", fontSize = 24.sp); Spacer(Modifier.width(4.dp)); Text("+$coins монет", style = MaterialTheme.typography.titleMedium, color = FairyGold, fontWeight = FontWeight.Bold) } }
                        Spacer(Modifier.height(20.dp))
                        AnimatedVisibility(visible = showButtons, enter = slideInVertically { it / 2 } + fadeIn()) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onNext, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyGreen), elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)) { Text("➡️ Далее", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                                if (onReplay != null && stars < 3) OutlinedButton(onClick = onReplay, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = FairyBlue)) { Text("🔄 Попробовать ещё раз", fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterCelebration(character: String, stars: Int) {
    val bounce by rememberInfiniteTransition().animateFloat(0f, -10f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "bounce")
    val rotation by rememberInfiniteTransition().animateFloat(-5f, 5f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "rotation")

    val avatarRes = when {
        character == "knopa" && stars == 3 -> R.drawable.character_kuzya_ecstatic
        character == "knopa" && stars == 2 -> R.drawable.character_kuzya_happy
        character == "knopa" && stars == 1 -> R.drawable.character_kuzya_neutral
        character == "knopa" -> R.drawable.character_kuzya_sad
        character == "vasilisa" && stars == 3 -> R.drawable.character_vasilisa_proud
        character == "vasilisa" && stars == 2 -> R.drawable.character_vasilisa_happy
        character == "vasilisa" && stars == 1 -> R.drawable.character_vasilisa_happy
        else -> R.drawable.character_vasilisa_happy
    }

    Box(Modifier.size(100.dp).clip(CircleShape).background(Brush.radialGradient(listOf(FairyGold.copy(alpha = 0.3f), FairyGold.copy(alpha = 0.1f)))).border(3.dp, FairyGold, CircleShape), contentAlignment = Alignment.Center) {
        Image(painterResource(avatarRes), "Персонаж", Modifier.fillMaxSize().padding(12.dp).offset(y = bounce.dp).rotate(rotation), contentScale = ContentScale.Fit)
    }
}

@Composable
private fun ConfettiPiece(color: Color, delay: Long, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delay); visible = true }
    val randomX = remember { kotlin.random.Random.nextFloat() }; val randomY = remember { kotlin.random.Random.nextFloat() }; val randomRotation = remember { kotlin.random.Random.nextFloat() * 360f }
    AnimatedVisibility(visible = visible, enter = fadeIn() + scaleIn()) {
        Box(Modifier.fillMaxSize().wrapContentSize(Alignment.TopStart)) { Text(listOf("⭐", "✨", "🌟", "💫", "🎉")[index % 5], fontSize = (12 + index % 8).sp, color = color, modifier = Modifier.offset(x = (randomX * 600).dp, y = (randomY * 300).dp).rotate(randomRotation)) }
    }
}

@Composable
fun StarDisplay(stars: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            var starVisible by remember { mutableStateOf(false) }; var starBounced by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(index * 300L); starVisible = true; delay(200); starBounced = true }
            val starScale by animateFloatAsState(targetValue = if (starBounced && index < stars) 1.2f else 1f, animationSpec = spring(dampingRatio = 0.3f, stiffness = 100f), label = "starScale")
            AnimatedVisibility(visible = starVisible, enter = scaleIn(spring(dampingRatio = 0.5f)) + fadeIn()) { Text(if (index < stars) "⭐" else "☆", fontSize = 44.sp, color = if (index < stars) FairyGold else Color.Gray.copy(alpha = 0.4f), modifier = Modifier.padding(8.dp).scale(starScale)) }
        }
    }
}

@Composable
fun MiniLevelComplete(message: String, onNext: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(visible = visible, enter = fadeIn() + scaleIn()) {
        Card(Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = FairyGreen.copy(alpha = 0.1f)), elevation = CardDefaults.cardElevation(8.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("✅ $message", style = MaterialTheme.typography.titleSmall, color = FairyGreen, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = FairyGreen)) { Text("Далее", color = Color.White) }
            }
        }
    }
}
