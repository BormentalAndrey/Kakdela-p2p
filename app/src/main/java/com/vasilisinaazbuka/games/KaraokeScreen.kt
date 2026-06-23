package com.vasilisinaazbuka.games

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.vasilisinaazbuka.R
import com.vasilisinaazbuka.data.GameState
import com.vasilisinaazbuka.ui.CharacterView
import com.vasilisinaazbuka.ui.LevelComplete
import com.vasilisinaazbuka.ui.StageProgressIndicator
import com.vasilisinaazbuka.ui.theme.*

@Composable
fun KaraokeScreen(songIndex: Int = 1, stage: Int = 1, onNextStage: () -> Unit = {}, onNextSong: () -> Unit = {}, onGameComplete: () -> Unit = {}, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/${R.raw.kar}")
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
                if (state == Player.STATE_ENDED) { playbackCompleted = true; isPlaying = false }
            }
        })
    }

    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_level_karaoke), "Фон", Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)

        Row(Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(0.33f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🎤 Караоке", style = MaterialTheme.typography.titleLarge, color = FairyGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                StageProgressIndicator(currentStage = stage, maxStages = 1, compact = true)
                Spacer(Modifier.height(12.dp))
                CharacterView("vasilisa", if (isPlaying) "happy" else if (playbackCompleted) "proud" else "teacher",
                    if (isPlaying) "Смотри и подпевай!" else if (playbackCompleted) "Молодец! Спой ещё раз!" else "Смотри караоке\nи подпевай!", Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))

                Button(onClick = { if (playbackCompleted) { exoPlayer.seekTo(0); playbackCompleted = false }; exoPlayer.play(); isPlaying = true }, Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyGreen), shape = RoundedCornerShape(16.dp), enabled = !isPlaying) {
                    Text(if (playbackCompleted) "🔄 Повторить" else "▶ Смотреть", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { exoPlayer.pause(); isPlaying = false }, Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPink), shape = RoundedCornerShape(16.dp), enabled = isPlaying) { Text("⏸ Пауза", fontSize = 16.sp, color = Color.White) }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { exoPlayer.seekTo(0); exoPlayer.play(); playbackCompleted = false; isPlaying = true }, Modifier.fillMaxWidth().height(44.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyBlue), shape = RoundedCornerShape(12.dp)) { Text("🔄 Заново", fontSize = 14.sp, color = Color.White) }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showLevelComplete = true; stars = 3; GameState.completeLevel("karaoke", songIndex, 3); exoPlayer.release() }, Modifier.fillMaxWidth().height(44.dp)) { Text("✅ Завершить", fontSize = 14.sp, color = FairyGreen) }
            }

            Spacer(Modifier.width(12.dp))

            Card(Modifier.weight(0.67f).fillMaxHeight(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Black), elevation = CardDefaults.cardElevation(8.dp)) {
                AndroidView(factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = false; resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT } }, modifier = Modifier.fillMaxSize())
            }
        }

        // Кнопка «Назад» на переднем плане
        Box(Modifier.fillMaxSize().wrapContentSize(Alignment.TopEnd).padding(8.dp).zIndex(100f)) {
            Button(onClick = onBack, Modifier.size(48.dp).zIndex(100f), colors = ButtonDefaults.buttonColors(containerColor = FairyBlue.copy(alpha = 0.85f)), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(0.dp), elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)) { Text("↩", fontSize = 20.sp, color = Color.White) }
        }

        if (showLevelComplete) LevelComplete(stars = stars, message = "Караоке просмотрено!\nМолодец!", character = "vasilisa", onNext = { if (songIndex < 1) onNextSong() else onGameComplete() })
    }
}
