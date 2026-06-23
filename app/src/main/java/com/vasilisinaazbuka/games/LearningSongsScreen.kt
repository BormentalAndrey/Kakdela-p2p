package com.vasilisinaazbuka.games

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
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
    val videoRes: Int,       // R.raw.song_01 и т.д.
    val question: String,
    val answers: List<String>,
    val correctAnswer: Int   // индекс правильного ответа (0-2)
)

val learningSongs = listOf(
    LearningSong(1, "Василиса и потерянная игрушка", R.raw.song_01, "О чём эта песня?",
        listOf("О том, как Кнопа сломал игрушку", "О том, что нужно искать вместе, а не кричать", "О том, что зайка убежал в лес"), 1),
    LearningSong(2, "Волшебное слово", R.raw.song_02, "Какое слово нужно говорить?",
        listOf("Сейчас", "Пожалуйста", "Быстро"), 1),
    LearningSong(3, "Кнопа и открытое окно", R.raw.song_03, "О чём эта песня?",
        listOf("О том, что окна — это весело", "О том, что надо мыть окна", "О том, что нужно быть осторожным у окна"), 2),
    LearningSong(4, "Честная находка", R.raw.song_04, "Что нужно сделать с найденной вещью?",
        listOf("Спрятать в карман", "Отдать хозяину", "Выбросить"), 1),
    LearningSong(5, "Цветок для бабушки", R.raw.song_05, "О чём эта песня?",
        listOf("О том, что цветы нужно рвать", "О том, что природу нужно беречь", "О том, что бабушка любит цветы из магазина"), 1),
    LearningSong(6, "Кнопа не любит беспорядок", R.raw.song_06, "Что нужно делать после игры?",
        listOf("Убирать игрушки", "Бежать гулять", "Смотреть мультики"), 0),
    LearningSong(7, "Последняя конфета", R.raw.song_07, "Что приносит радость?",
        listOf("Жадность", "Делиться с друзьями", "Есть одному"), 1),
    LearningSong(8, "Дождливый день", R.raw.song_08, "О чём эта песня?",
        listOf("О том, что дождь — это плохо", "О том, что радость можно найти даже в дождь", "О том, что нужно сидеть дома"), 1),
    LearningSong(9, "Смелость признаться", R.raw.song_09, "Что сделала Василиса?",
        listOf("Спрятала испачканный альбом", "Честно рассказала маме", "Обвинила Кнопу"), 1),
    LearningSong(10, "Кнопа спасает птенца", R.raw.song_10, "Кого нужно позвать, если случилась беда?",
        listOf("Друга", "Взрослых", "Никого"), 1)
)

@Composable
fun LearningSongsScreen(
    songIndex: Int = 1,
    onNextSong: () -> Unit = {},
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentSong = learningSongs.getOrElse(songIndex - 1) { learningSongs[0] }

    var phase by remember { mutableStateOf(SongPhase.VIDEO) }  // VIDEO, QUESTION, RESULT
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var isCorrect by remember { mutableStateOf(false) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }

    // Видеоплеер
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/${currentSong.videoRes}")
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = false
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var playbackCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isPlaying = state == Player.STATE_READY && exoPlayer.playWhenReady
                if (state == Player.STATE_ENDED) {
                    playbackCompleted = true
                    isPlaying = false
                    phase = SongPhase.QUESTION
                }
            }
        })
    }

    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_level_karaoke), "Фон", Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)

        // Кнопка «Назад» справа вверху
        Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.TopEnd) {
            Button(onClick = onBack, Modifier.size(44.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyBlue.copy(alpha = 0.7f)), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(0.dp)) { Text("↩", fontSize = 18.sp, color = Color.White) }
        }

        when (phase) {
            SongPhase.VIDEO -> VideoPhase(
                currentSong = currentSong,
                songIndex = songIndex,
                exoPlayer = exoPlayer,
                isPlaying = isPlaying,
                playbackCompleted = playbackCompleted,
                onPlay = { exoPlayer.play(); isPlaying = true },
                onPause = { exoPlayer.pause(); isPlaying = false },
                onReplay = { exoPlayer.seekTo(0); exoPlayer.play(); playbackCompleted = false; isPlaying = true },
                onSkipToQuestion = { phase = SongPhase.QUESTION }
            )

            SongPhase.QUESTION -> QuestionPhase(
                currentSong = currentSong,
                selectedAnswer = selectedAnswer,
                onAnswerSelect = { index ->
                    selectedAnswer = index
                    isCorrect = index == currentSong.correctAnswer
                    stars = if (isCorrect) 3 else 1
                    phase = SongPhase.RESULT
                    GameState.completeLevel("learningsongs", songIndex, if (isCorrect) 3 else 1)
                    AudioPlayer.playSFX(if (isCorrect) R.raw.sfx_success else R.raw.sfx_error)
                }
            )

            SongPhase.RESULT -> ResultPhase(
                currentSong = currentSong,
                isCorrect = isCorrect,
                stars = stars,
                onNext = {
                    if (songIndex < 10) {
                        onNextSong()
                    } else {
                        showLevelComplete = true
                    }
                },
                onReplay = {
                    selectedAnswer = -1
                    phase = SongPhase.VIDEO
                    playbackCompleted = false
                }
            )
        }

        if (showLevelComplete) {
            LevelComplete(stars = 3, message = "Все 10 песен прослушаны!\nТы молодец!", character = "vasilisa", onNext = { onGameComplete() })
        }
    }
}

enum class SongPhase { VIDEO, QUESTION, RESULT }

@Composable
private fun VideoPhase(
    currentSong: LearningSong,
    songIndex: Int,
    exoPlayer: ExoPlayer,
    isPlaying: Boolean,
    playbackCompleted: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onReplay: () -> Unit,
    onSkipToQuestion: () -> Unit
) {
    Row(Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(0.33f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("🎵 Песня $songIndex из 10", style = MaterialTheme.typography.titleLarge, color = FairyGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            CharacterView("vasilisa", if (isPlaying) "happy" else "teacher", if (isPlaying) "Смотри и слушай!" else "Послушай песенку!", Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))

            Text(currentSong.title, style = MaterialTheme.typography.titleSmall, color = FairyPurple, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))

            Button(onClick = onPlay, Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyGreen), shape = RoundedCornerShape(16.dp), enabled = !isPlaying) {
                Text(if (playbackCompleted) "🔄 Повторить" else "▶ Смотреть", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onPause, Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPink), shape = RoundedCornerShape(16.dp), enabled = isPlaying) { Text("⏸ Пауза", fontSize = 16.sp, color = Color.White) }
            Spacer(Modifier.height(8.dp))

            if (playbackCompleted) {
                Button(onClick = onSkipToQuestion, Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyGold), shape = RoundedCornerShape(16.dp)) { Text("❓ Ответить на вопрос", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }

        Spacer(Modifier.width(12.dp))

        Card(Modifier.weight(0.67f).fillMaxHeight(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Black), elevation = CardDefaults.cardElevation(8.dp)) {
            AndroidView(factory = { ctx ->
                PlayerView(ctx).apply { player = exoPlayer; useController = false; resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT }
            }, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun QuestionPhase(
    currentSong: LearningSong,
    selectedAnswer: Int,
    onAnswerSelect: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("❓ Вопрос", style = MaterialTheme.typography.headlineMedium, color = FairyGold, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Text(currentSong.question, style = MaterialTheme.typography.titleLarge, color = FairyPurple, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        currentSong.answers.forEachIndexed { index, answer ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onAnswerSelect(index) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (selectedAnswer == index) FairyGold.copy(alpha = 0.3f) else Color.White),
                elevation = CardDefaults.cardElevation(if (selectedAnswer == index) 8.dp else 4.dp)
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selectedAnswer == index) "✅" else listOf("🅰", "🅱", "©")[index], fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(answer, style = MaterialTheme.typography.titleMedium, color = if (selectedAnswer == index) FairyGold else FairyBlue)
                }
            }
        }
    }
}

@Composable
private fun ResultPhase(
    currentSong: LearningSong,
    isCorrect: Boolean,
    stars: Int,
    onNext: () -> Unit,
    onReplay: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(if (isCorrect) "🎉 Правильно!" else "😊 Почти!", style = MaterialTheme.typography.headlineMedium, color = if (isCorrect) FairyGreen else FairyGold, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // Мораль песни
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = FairyGold.copy(alpha = 0.1f))) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📚 Мораль:", style = MaterialTheme.typography.titleSmall, color = FairyPurple, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    when (currentSong.id) {
                        1 -> "Если что-то потерял — не сердись, лучше вместе поискать!"
                        2 -> "Добрые слова «пожалуйста» и «спасибо» открывают все сердца!"
                        3 -> "Безопасность дома — это важно! Будь осторожен у окна."
                        4 -> "Если нашёл чужую вещь — верни хозяину. Честность делает мир добрее!"
                        5 -> "Береги природу: цветы, деревья, животных. Мир станет красивее!"
                        6 -> "После игры всегда убирай игрушки. Чистота — это комфорт!"
                        7 -> "Когда делишься с друзьями — счастья становится больше!"
                        8 -> "Даже в дождливый день можно найти радость. Улыбнись!"
                        9 -> "Если ошибся — признайся честно. Это смелость, а не слабость!"
                        10 -> "Помогай тем, кто в беде, но сначала позови взрослых!"
                        else -> "Доброта и честность — лучшие друзья!"
                    },
                    style = MaterialTheme.typography.bodyLarge, color = FairyBlue, textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onReplay, Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPink), shape = RoundedCornerShape(16.dp)) { Text("🔄 Послушать ещё раз", fontSize = 14.sp, color = Color.White) }
            Button(onClick = onNext, Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyGreen), shape = RoundedCornerShape(16.dp)) { Text("➡️ Далее", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}
