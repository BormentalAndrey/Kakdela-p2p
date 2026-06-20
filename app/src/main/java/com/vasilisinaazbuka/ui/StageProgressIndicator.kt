package com.vasilisinaazbuka.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vasilisinaazbuka.ui.theme.*

/**
 * Индикатор прогресса по этапам игры
 *
 * @param currentStage Текущий этап (начиная с 1)
 * @param maxStages Общее количество этапов
 * @param modifier Модификатор композабла
 */
@Composable
fun StageProgressIndicator(
    currentStage: Int,
    maxStages: Int,
    modifier: Modifier = Modifier
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val targetProgress = currentStage.toFloat() / maxStages.toFloat()

    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500),
        label = "stageProgress"
    )

    LaunchedEffect(currentStage) {
        animatedProgress = targetProgress
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Текст с номером этапа
        Text(
            text = "Этап $currentStage из $maxStages",
            style = MaterialTheme.typography.titleSmall,
            color = FairyPurple,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Прогресс-бар
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = FairyGold,
            trackColor = FairyGold.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Точки-индикаторы этапов
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(maxStages) { index ->
                val stageNumber = index + 1
                val isCompleted = stageNumber < currentStage
                val isCurrent = stageNumber == currentStage

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> FairyGreen
                                isCurrent -> FairyGold
                                else -> Color.Gray.copy(alpha = 0.3f)
                            }
                        )
                )
            }
        }
    }
}
