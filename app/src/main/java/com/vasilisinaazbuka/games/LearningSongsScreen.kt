package com.vasilisinaazbuka.games

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.media.MediaPlayer
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
import com.vasilisinaazbuka.ui.theme.*

// ==================== Данные песен ====================
data class LearningSong(
    val id: Int,
    val title: String,
    val audioRes: Int,
    val coverRes: Int,
    val question: String,
    val answers: List<String>,
    val correctAnswer: Int,
    val moral: String
)

val learningSongs = listOf(
    LearningSong(1, "Василиса и потерянная игрушка", R.raw.song_01, R.drawable.character_vasilisa_happy,
        "О чём эта песня?",
        listOf("О том, как Кнопа сломал игрушку", "О том, что нужно искать вместе, а не кричать", "О том, что зайка убежал в лес"), 1,
        "Если что-то потерял — не сердись, лучше вместе поискать!"),
    LearningSong(2, "Волшебное слово", R.raw.song_02, R.drawable.character_kuzya_happy,
        "Какое слово нужно говорить?",
        listOf("Сейчас", "Пожалуйста", "Быстро"), 1,
        "Добрые слова «пожалуйста» и «спасибо» открывают все сердца!"),
    LearningSong(3, "Кнопа и открытое окно", R.raw.song_03, R.drawable.character_kuzya_neutral,
        "О чём эта песня?",
        listOf("О том, что окна — это весело", "О том, что надо мыть окна", "О том, что нужно быть осторожным у окна"), 2,
        "Безопасность дома — это важно! Будь осторожен у окна."),
    LearningSong(4, "Честная находка", R.raw.song_04, R.drawable.character_vasilisa_proud,
        "Что нужно сделать с найденной вещью?",
        listOf("Спрятать в карман", "Отдать хозяину", "Выбросить"), 1,
        "Если нашёл чужую вещь — верни хозяину. Честность делает мир добрее!"),
    LearningSong(5, "Цветок для бабушки", R.raw.song_05, R.drawable.character_vasilisa_happy,
        "О чём эта песня?",
        listOf("О том, что цветы нужно рвать", "О том, что природу нужно беречь", "О том, что бабушка любит цветы из магазина"), 1,
        "Береги природу: цветы, деревья, животных. Мир станет красивее!"),
    LearningSong(6, "Кнопа не любит беспорядок", R.raw.song_06, R.drawable.character_kuzya_angry,
        "Что нужно делать после игры?",
        listOf("Убирать игрушки", "Бежать гулять", "Смотреть мультики"), 0,
        "После игры всегда убирай игрушки. Чистота — это комфорт!"),
    LearningSong(7, "Последняя конфета", R.raw.song_07, R.drawable.character_kuzya_happy,
        "Что приносит радость?",
        listOf("Жадность", "Делиться с друзьями", "Есть одному"), 1,
        "Когда делишься с друзьями — счастья становится больше!"),
    LearningSong(8, "Дождливый день", R.raw.song_08, R.drawable.character_kuzya_sleeping,
        "О чём эта песня?",
        listOf("О том, что дождь — это плохо", "О том, что радость можно найти даже в дождь", "О том, что нужно сидеть дома"), 1,
        "Даже в дождливый день можно найти радость. Улыбнись!"),
    LearningSong(9, "Смелость признаться", R.raw.song_09, R.drawable.character_vasilisa_thinking,
        "Что сделала Василиса?",
        listOf("Спрятала испачканный альбом", "Честно рассказала маме", "Обвинила Кнопу"), 1,
        "Если ошибся — признайся честно. Это смелость, а не слабость!"),
    LearningSong(10, "Кнопа спасает птенца", R.raw.song_10, R.drawable.character_kuzya_ecstatic,
        "Кого нужно позвать, если случилась беда?",
        listOf("Друга", "Взрослых", "Никого"), 1,
        "Помогай тем, кто в беде, но сначала позови взрослых!")
)

@Composable
fun LearningSongsScreen(
    songIndex: Int = 1,
    onNextSong: () -> Unit = {},
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val currentSong = learningSongs.getOrElse(songIndex - 1) { learningSongs[0] }

    var phase by remember { mutableStateOf(SongPhase.LISTEN) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var isCorrect by remember { mutableStateOf(false) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    // Анимация для обложки
    val coverScale by animateFloatAsState(targetValue = if (isPlaying) 1.05f else 1f, animationSpec = spring(dampingRatio = 0.3f), label = "cover")

    Box(Modifier.fillMaxSize().background(FairyBlue.copy(alpha = 0.05f))) {
        Image(painterResource(R.drawable.bg_level_karaoke), "Фон", Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.2f)

        // Кнопка «Назад»
        Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.TopEnd) {
            Button(onClick = onBack, Modifier.size(44.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyBlue.copy(alpha = 0.7f)), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(0.dp)) { Text("↩", fontSize = 18.sp, color = Color.White) }
        }

        when (phase) {
            SongPhase.LISTEN -> ListenPhase(
                currentSong = currentSong, songIndex = songIndex,
                isPlaying = isPlaying, coverScale = coverScale,
                onPlay = { AudioPlayer.playSFX(currentSong.audioRes); isPlaying = true },
                onPause = { AudioPlayer.stopMusic(); isPlaying = false },
                onContinue = { phase = SongPhase.QUESTION }
            )
            SongPhase.QUESTION -> QuestionPhase(
                currentSong = currentSong, selectedAnswer = selectedAnswer,
                onAnswerSelect = { index ->
                    selectedAnswer = index; isCorrect = index == currentSong.correctAnswer
                    stars = if (isCorrect) 3 else 1; phase = SongPhase.RESULT
                    GameState.completeLevel("learningsongs", songIndex, if (isCorrect) 3 else 1)
                    AudioPlayer.playSFX(if (isCorrect) R.raw.sfx_success else R.raw.sfx_error)
                }
            )
            SongPhase.RESULT -> ResultPhase(
                currentSong = currentSong, isCorrect = isCorrect, stars = stars,
                onNext = { if (songIndex < 10) onNextSong() else { showLevelComplete = true } },
                onReplay = { selectedAnswer = -1; phase = SongPhase.LISTEN; isPlaying = false; AudioPlayer.stopMusic() }
            )
        }

        if (showLevelComplete) LevelComplete(stars = 3, message = "Все 10 песен прослушаны!\nТы молодец!", character = "vasilisa", onNext = { onGameComplete() })
    }
}

enum class SongPhase { LISTEN, QUESTION, RESULT }

@Composable
private fun ListenPhase(currentSong: LearningSong, songIndex: Int, isPlaying: Boolean, coverScale: Float, onPlay: () -> Unit, onPause: () -> Unit, onContinue: () -> Unit) {
    Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(0.4f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("🎵 Песня $songIndex из 10", style = MaterialTheme.typography.titleLarge, color = FairyGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            CharacterView("vasilisa", if (isPlaying) "happy" else "teacher", if (isPlaying) "Слушай внимательно!" else "Послушай песенку\nи ответь на вопрос!", Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Text(currentSong.title, style = MaterialTheme.typography.titleMedium, color = FairyPurple, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))

            Button(onClick = if (isPlaying) onPause else onPlay, Modifier.size(100.dp).clip(CircleShape), colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) FairyPink else FairyGreen)) {
                Text(if (isPlaying) "⏸" else "▶", fontSize = 40.sp, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Text(if (isPlaying) "Играет..." else "Нажми чтобы слушать", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(Modifier.height(24.dp))
            Button(onClick = onContinue, Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyGold), shape = RoundedCornerShape(16.dp)) {
                Text("❓ Ответить на вопрос", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.width(24.dp))

        // Обложка песни
        Card(Modifier.weight(0.6f).fillMaxHeight(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(painterResource(currentSong.coverRes), "Обложка", Modifier.fillMaxSize(0.7f).clip(RoundedCornerShape(16.dp)).scale(coverScale), contentScale = ContentScale.Fit)
            }
        }
    }
}

@Composable
private fun QuestionPhase(currentSong: LearningSong, selectedAnswer: Int, onAnswerSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("❓ Вопрос", style = MaterialTheme.typography.headlineMedium, color = FairyGold, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Text(currentSong.question, style = MaterialTheme.typography.titleLarge, color = FairyPurple, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        currentSong.answers.forEachIndexed { index, answer ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onAnswerSelect(index) }, shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (selectedAnswer == index) FairyGold.copy(alpha = 0.3f) else Color.White),
                elevation = CardDefaults.cardElevation(if (selectedAnswer == index) 8.dp else 4.dp)) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(listOf("🅰", "🅱", "©")[index], fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(answer, style = MaterialTheme.typography.titleMedium, color = if (selectedAnswer == index) FairyGold else FairyBlue)
                }
            }
        }
    }
}

@Composable
private fun ResultPhase(currentSong: LearningSong, isCorrect: Boolean, stars: Int, onNext: () -> Unit, onReplay: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(if (isCorrect) "🎉 Правильно!" else "😊 Почти!", style = MaterialTheme.typography.headlineMedium, color = if (isCorrect) FairyGreen else FairyGold, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = FairyGold.copy(alpha = 0.1f))) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📚 Мораль:", style = MaterialTheme.typography.titleSmall, color = FairyPurple, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(currentSong.moral, style = MaterialTheme.typography.bodyLarge, color = FairyBlue, textAlign = TextAlign.Center)
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onReplay, Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPink), shape = RoundedCornerShape(16.dp)) { Text("🔄 Послушать ещё раз", fontSize = 14.sp, color = Color.White) }
            Button(onClick = onNext, Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyGreen), shape = RoundedCornerShape(16.dp)) { Text("➡️ Далее", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}
