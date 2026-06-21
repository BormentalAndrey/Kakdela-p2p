package com.vasilisinaazbuka.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vasilisinaazbuka.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Индикатор прогресса по этапам игры
 * Оптимизирован для ландшафтной ориентации
 *
 * @param currentStage Текущий этап (начиная с 1)
 * @param maxStages Общее количество этапов
 * @param label Текст метки (по умолчанию "Этап")
 * @param showNumbers Показывать номера этапов на точках
 * @param compact Компактный режим (без текста)
 * @param modifier Модификатор композабла
 */
@Composable
fun StageProgressIndicator(
    currentStage: Int,
    maxStages: Int,
    label: String = "Этап",
    showNumbers: Boolean = false,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val targetProgress = if (maxStages > 0) {
        currentStage.toFloat() / maxStages.toFloat()
    } else 0f

    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 100f),
        label = "stageProgress"
    )

    var dotsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(maxStages) {
        dotsVisible = false
        delay(100)
        dotsVisible = true
    }

    if (compact) {
        CompactProgressIndicator(currentStage, maxStages, progress, showNumbers, dotsVisible, modifier)
    } else {
        FullProgressIndicator(currentStage, maxStages, label, progress, showNumbers, dotsVisible, modifier)
    }
}

// ==================== FullProgressIndicator ====================

@Composable
private fun FullProgressIndicator(
    currentStage: Int,
    maxStages: Int,
    label: String,
    progress: Float,
    showNumbers: Boolean,
    dotsVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("$label $currentStage из $maxStages", style = MaterialTheme.typography.bodySmall, color = FairyPurple, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)),
            color = FairyGold, trackColor = FairyGold.copy(alpha = 0.2f)
        )
        Spacer(Modifier.width(12.dp))
        StageDots(currentStage, maxStages, showNumbers, dotsVisible, 12.dp)
    }
}

// ==================== CompactProgressIndicator ====================

@Composable
private fun CompactProgressIndicator(
    currentStage: Int,
    maxStages: Int,
    progress: Float,
    showNumbers: Boolean,
    dotsVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = FairyGold, trackColor = FairyGold.copy(alpha = 0.2f)
        )
        Spacer(Modifier.width(8.dp))
        StageDots(currentStage, maxStages, showNumbers, dotsVisible, 8.dp)
        Spacer(Modifier.width(4.dp))
        Text("$currentStage/$maxStages", style = MaterialTheme.typography.labelSmall, color = FairyPurple, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
    }
}

// ==================== StageDots ====================

@Composable
private fun StageDots(
    currentStage: Int,
    maxStages: Int,
    showNumbers: Boolean,
    visible: Boolean,
    dotSize: Dp = 12.dp
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(maxStages) { index ->
            val stageNumber = index + 1
            val isCompleted = stageNumber < currentStage
            val isCurrent = stageNumber == currentStage

            var dotVisible by remember { mutableStateOf(false) }

            LaunchedEffect(visible) {
                if (visible) { delay(index * 80L); dotVisible = true }
            }

            val dotColor by animateColorAsState(
                targetValue = when { isCompleted -> FairyGreen; isCurrent -> FairyGold; else -> Color.Gray.copy(alpha = 0.3f) },
                animationSpec = tween(300), label = "dotColor_$index"
            )

            val dotScale by animateFloatAsState(
                targetValue = if (isCurrent && dotVisible) 1.4f else 1f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 200f), label = "dotScale_$index"
            )

            if (dotVisible) {
                Box(
                    Modifier.size(dotSize).scale(dotScale).clip(CircleShape).background(dotColor)
                        .then(if (isCurrent) Modifier.border(2.dp, FairyGold.copy(alpha = 0.5f), CircleShape) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    if (showNumbers && dotSize >= 12.dp) {
                        Text("$stageNumber", fontSize = (dotSize.value / 2).sp, color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                    }
                    if (isCompleted && dotSize >= 12.dp) {
                        Text("✓", fontSize = (dotSize.value / 2).sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== VerticalStageProgress ====================

@Composable
fun VerticalStageProgress(currentStage: Int, maxStages: Int, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in maxStages downTo 1) {
            val isCompleted = i < currentStage
            val isCurrent = i == currentStage

            val color by animateColorAsState(
                targetValue = when { isCompleted -> FairyGreen; isCurrent -> FairyGold; else -> Color.Gray.copy(alpha = 0.3f) },
                animationSpec = tween(300), label = "vColor_$i"
            )

            val dotScale by animateFloatAsState(
                targetValue = if (isCurrent) 1.3f else 1f,
                animationSpec = spring(dampingRatio = 0.4f), label = "vScale_$i"
            )

            Box(
                Modifier.size(14.dp).scale(dotScale).clip(CircleShape).background(color)
                    .then(if (isCurrent) Modifier.border(2.dp, FairyGold, CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) Text("✓", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                else if (isCurrent) Text("$i", fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (i > 1) {
                Box(Modifier.width(2.dp).height(16.dp).background(if (i <= currentStage) FairyGreen.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f)))
            }
        }
    }
}

// ==================== ThemedStageProgress ====================

@Composable
fun ThemedStageProgress(currentStage: Int, maxStages: Int, stageIcons: List<String> = emptyList(), modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Этап $currentStage из $maxStages", style = MaterialTheme.typography.bodySmall, color = FairyPurple, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = if (maxStages > 0) currentStage.toFloat() / maxStages.toFloat() else 0f,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = FairyGold, trackColor = FairyGold.copy(alpha = 0.2f)
        )
        Spacer(Modifier.height(6.dp))
        if (stageIcons.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                stageIcons.forEachIndexed { index, icon ->
                    val isCurrent = (index + 1) == currentStage
                    Text(icon, fontSize = if (isCurrent) 24.sp else 18.sp, modifier = Modifier.then(if (isCurrent) Modifier.scale(1.2f) else Modifier))
                }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                repeat(maxStages) { index ->
                    val stageNumber = index + 1
                    Box(Modifier.size(10.dp).clip(CircleShape).background(when {
                        stageNumber < currentStage -> FairyGreen; stageNumber == currentStage -> FairyGold; else -> Color.Gray.copy(alpha = 0.3f)
                    }))
                }
            }
        }
    }
}
