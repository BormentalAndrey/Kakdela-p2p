package com.vasilisinaazbuka.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.vasilisinaazbuka.R
import com.vasilisinaazbuka.ui.CharacterView
import com.vasilisinaazbuka.ui.theme.*

data class ColoringImage(
    val id: Int,
    val name: String,
    val imageRes: Int
)

val coloringImages = listOf(
    ColoringImage(1, "Матрёшка", R.drawable.coloring_matryoshka),
    ColoringImage(2, "Кремль", R.drawable.coloring_kremlin),
    ColoringImage(3, "Самовар", R.drawable.coloring_samovar),
    ColoringImage(4, "Берёзка", R.drawable.coloring_birch),
    ColoringImage(5, "Балалайка", R.drawable.coloring_balalaika)
)

@Composable
fun ColoringSelectScreen(
    onStageSelected: (Int) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Box(Modifier.fillMaxSize().background(FairyBlue.copy(alpha = 0.05f))) {
        Image(painterResource(R.drawable.bg_level_coloring), "Фон", Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.2f)

        Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Левая панель
            Column(Modifier.weight(0.35f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🎨 Раскраска", style = MaterialTheme.typography.headlineMedium, color = FairyGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                CharacterView("vasilisa", "happy", "Выбери картинку\nкоторую хочешь\nраскрасить!", Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Text("Нажми на картинку\nчтобы начать рисовать!", style = MaterialTheme.typography.bodyMedium, color = FairyPurple, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.width(20.dp))

            // Сетка выбора раскрасок
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(0.65f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(coloringImages) { index, image ->
                    Card(
                        modifier = Modifier
                            .aspectRatio(0.85f)
                            .clickable { onStageSelected(index + 1) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            Modifier.fillMaxSize().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(image.imageRes),
                                contentDescription = image.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(2.dp, FairyGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = image.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = FairyBlue,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Рисовать 🖌️",
                                style = MaterialTheme.typography.bodySmall,
                                color = FairyPink,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Кнопка «Назад» на переднем плане
        Box(Modifier.fillMaxSize().wrapContentSize(Alignment.TopEnd).padding(8.dp).zIndex(100f)) {
            Button(onClick = onBack, Modifier.size(48.dp).zIndex(100f), colors = ButtonDefaults.buttonColors(containerColor = FairyBlue.copy(alpha = 0.85f)), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(0.dp), elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)) { Text("↩", fontSize = 20.sp, color = Color.White) }
        }
    }
}
