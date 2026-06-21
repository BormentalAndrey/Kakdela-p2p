package com.vasilisinaazbuka.games

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.vasilisinaazbuka.ui.StageProgressIndicator
import com.vasilisinaazbuka.ui.theme.*
import kotlinx.coroutines.delay
import java.io.InputStream

// ==================== Модели данных ====================
data class KarFile(val metadata: KarMetadata, val lyrics: List<KarLyricEvent>, val tempoEvents: List<KarTempoEvent>, val timeSignatureEvents: List<KarTimeSignatureEvent>, val totalDurationMs: Long)
data class KarMetadata(val title: String = "Неизвестная песня", val author: String = "Народная", val copyright: String = "", val sequenceName: String = "", val trackNames: List<String> = emptyList(), val instruments: List<String> = emptyList())
data class KarLyricEvent(val timeMs: Long, val text: String, val trackIndex: Int = 0, val isSyllable: Boolean = true)
data class KarTempoEvent(val timeMs: Long, val tempoBpm: Int)
data class KarTimeSignatureEvent(val timeMs: Long, val numerator: Int, val denominator: Int)

// ==================== Парсер .kar ====================
object KarParser {
    private const val HEADER_CHUNK_ID = "MThd"; private const val TRACK_CHUNK_ID = "MTrk"
    fun parse(inputStream: InputStream): KarFile = parse(inputStream.readBytes())
    fun parse(data: ByteArray): KarFile {
        var offset = 0; val headerId = String(data, offset, 4); offset += 4
        if (headerId != HEADER_CHUNK_ID) throw IllegalArgumentException("Неверный формат: $headerId")
        val headerLength = readInt32(data, offset); offset += 4; val format = readInt16(data, offset); offset += 2
        val trackCount = readInt16(data, offset); offset += 2; val ticksPerQuarter = readInt16(data, offset); offset += 2
        if (headerLength != 6) offset += (headerLength - 6)
        val lyrics = mutableListOf<KarLyricEvent>(); val tempoEvents = mutableListOf<KarTempoEvent>()
        val timeSignatureEvents = mutableListOf<KarTimeSignatureEvent>(); val trackNames = mutableListOf<String>()
        val instruments = mutableListOf<String>(); var title = "Неизвестная песня"; var author = "Народная"; var copyright = ""
        var microsecondsPerQuarter = 500000
        for (trackIndex in 0 until trackCount) {
            val trackId = String(data, offset, 4); offset += 4
            if (trackId != TRACK_CHUNK_ID) throw IllegalArgumentException("Ожидается MTrk")
            val trackLength = readInt32(data, offset); offset += 4; val trackEnd = offset + trackLength
            var absoluteTimeMs = 0L; var runningStatus: Byte = 0
            while (offset < trackEnd) {
                val deltaTime = readVariableLength(data, offset); offset += deltaTime.first
                absoluteTimeMs += ticksToMilliseconds(deltaTime.second, ticksPerQuarter, microsecondsPerQuarter)
                var eventByte = data[offset].toInt() and 0xFF
                if (eventByte < 0x80) eventByte = runningStatus.toInt() and 0xFF else { runningStatus = eventByte.toByte(); offset++ }
                when {
                    eventByte == 0xFF -> {
                        val metaType = data[offset].toInt() and 0xFF; offset++
                        val metaLength = readVariableLength(data, offset); offset += metaLength.first
                        when (metaType) {
                            0x01 -> { val t = String(data, offset, metaLength.second, Charsets.UTF_8); if (t.startsWith("@T")) title = t.substring(2).trim() else if (t.startsWith("@A")) author = t.substring(2).trim() }
                            0x02 -> copyright = String(data, offset, metaLength.second, Charsets.UTF_8)
                            0x03 -> trackNames.add(String(data, offset, metaLength.second, Charsets.UTF_8))
                            0x05 -> { val t = String(data, offset, metaLength.second, Charsets.UTF_8); if (t.isNotBlank()) { val syls = splitIntoSyllables(t); var st = absoluteTimeMs; val sd = (deltaTime.second / maxOf(syls.size, 1)).toLong(); syls.forEach { lyrics.add(KarLyricEvent(st, it, trackIndex, true)); st += sd } } }
                            0x51 -> { microsecondsPerQuarter = ((data[offset].toInt() and 0xFF) shl 16) or ((data[offset+1].toInt() and 0xFF) shl 8) or (data[offset+2].toInt() and 0xFF); tempoEvents.add(KarTempoEvent(absoluteTimeMs, 60_000_000 / microsecondsPerQuarter)) }
                            0x2F -> offset = trackEnd
                        }
                        offset += metaLength.second
                    }
                    eventByte in 0x80..0xEF -> { when (eventByte and 0xF0) { 0x80, 0x90, 0xA0, 0xB0, 0xE0 -> offset += 2; 0xC0, 0xD0 -> offset += 1 } }
                }
            }
        }
        val totalMs = if (lyrics.isNotEmpty()) lyrics.last().timeMs + 2000 else 30000L
        if (title == "Неизвестная песня" && trackNames.isNotEmpty()) title = trackNames.first()
        return KarFile(KarMetadata(title, author, copyright, trackNames.firstOrNull() ?: "", trackNames, instruments), lyrics.sortedBy { it.timeMs }, tempoEvents, timeSignatureEvents, totalMs)
    }
    private fun readInt16(d: ByteArray, o: Int) = ((d[o].toInt() and 0xFF) shl 8) or (d[o+1].toInt() and 0xFF)
    private fun readInt32(d: ByteArray, o: Int) = ((d[o].toInt() and 0xFF) shl 24) or ((d[o+1].toInt() and 0xFF) shl 16) or ((d[o+2].toInt() and 0xFF) shl 8) or (d[o+3].toInt() and 0xFF)
    private fun readVariableLength(d: ByteArray, o: Int): Pair<Int, Int> { var v = 0; var b = 0; while (true) { val byte = d[o+b].toInt() and 0xFF; b++; v = (v shl 7) or (byte and 0x7F); if (byte and 0x80 == 0) break }; return Pair(b, v) }
    private fun ticksToMilliseconds(t: Int, tpq: Int, mpq: Int): Long = if (tpq == 0) 0 else (t.toLong() * mpq) / (tpq * 1000)
    private fun splitIntoSyllables(text: String): List<String> {
        if (text.length <= 2) return listOf(text); val s = mutableListOf<String>(); var c = StringBuilder()
        val v = setOf('А','Е','Ё','И','О','У','Ы','Э','Ю','Я','а','е','ё','и','о','у','ы','э','ю','я')
        for (ch in text) { c.append(ch); if (ch in v && c.length > 1) { s.add(c.toString().uppercase()); c = StringBuilder() } }
        if (c.isNotEmpty()) s.add(c.toString().uppercase()); return if (s.isEmpty()) listOf(text.uppercase()) else s
    }
}

// ==================== Основной экран ====================
@Composable
fun KaraokeScreen(songIndex: Int = 1, stage: Int = 1, onNextStage: () -> Unit = {}, onNextSong: () -> Unit = {}, onGameComplete: () -> Unit = {}, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val karFile = remember(songIndex) { KarFileLoader.loadKarFile(context, songIndex) }

    if (karFile == null) {
        Box(Modifier.fillMaxSize().background(FairyBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("🎵", fontSize = 64.sp); Spacer(Modifier.height(16.dp)); Text("Загрузка песни...", style = MaterialTheme.typography.titleLarge, color = FairyPurple); Text("Песня $songIndex из 20", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
        }
        return
    }

    var gameMode by remember { mutableStateOf(GameMode.LISTEN) }; var currentLyricIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }; var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }; var mistakes by remember { mutableIntStateOf(0) }; var score by remember { mutableIntStateOf(0) }
    var constructedWord by remember { mutableStateOf("") }; var currentWordIndex by remember { mutableIntStateOf(0) }
    val words = remember(karFile) { groupSyllablesIntoWords(karFile.lyrics) }; val currentWord = words.getOrNull(currentWordIndex)

    LaunchedEffect(songIndex) { gameMode = GameMode.LISTEN; currentLyricIndex = 0; currentWordIndex = 0; constructedWord = ""; score = 0; mistakes = 0; showLevelComplete = false; isPlaying = false }
    LaunchedEffect(gameMode, isPlaying) { if (gameMode == GameMode.LISTEN && isPlaying && karFile.lyrics.isNotEmpty()) { for (i in karFile.lyrics.indices) { currentLyricIndex = i; val cur = karFile.lyrics[i].timeMs; val nxt = if (i+1 < karFile.lyrics.size) karFile.lyrics[i+1].timeMs else cur+500; delay((nxt-cur).coerceAtLeast(100)) }; delay(1000); isPlaying = false; currentLyricIndex = karFile.lyrics.size-1 } }
    LaunchedEffect(constructedWord) {
        if (currentWord != null && constructedWord == currentWord.text.replace(" ", "")) { score++; AudioPlayer.playSFX(R.raw.sfx_success); delay(800); if (currentWordIndex < words.size-1) { currentWordIndex++; constructedWord = "" } else { stars = when { mistakes == 0 -> 3; mistakes <= 2 -> 2; else -> 1 }; showLevelComplete = true; GameState.completeLevel("karaoke", songIndex, stars) } }
        else if (constructedWord.length >= (currentWord?.text?.replace(" ", "")?.length ?: 0)) { mistakes++; AudioPlayer.playSFX(R.raw.sfx_error); delay(500); constructedWord = "" }
    }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_level_karaoke), "Фон", Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)

        Row(Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Левая панель (1/3)
            Column(Modifier.weight(0.33f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                SongHeader(karFile, songIndex)
                Spacer(Modifier.height(8.dp))
                SongProgressBar(songIndex, 20)
                Spacer(Modifier.height(12.dp))
                CharacterView("vasilisa", when { showLevelComplete -> "proud"; mistakes > 3 -> "sad"; score > words.size/2 -> "happy"; isPlaying -> "teacher"; else -> "neutral" },
                    when (gameMode) { GameMode.LISTEN -> if (isPlaying) "Слушай и следи\nза слогами!" else "Нажми ▶ чтобы\nпослушать"; GameMode.READ -> "Составь слово\nиз букв!"; GameMode.SING -> "Пой вместе\nсо мной!" }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                GameModeSelector(gameMode) { mode -> gameMode = mode; if (mode == GameMode.LISTEN) currentLyricIndex = 0 }
            }

            Spacer(Modifier.width(12.dp))

            // Правая часть — караоке (2/3)
            Card(Modifier.weight(0.67f).fillMaxHeight(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
                when (gameMode) {
                    GameMode.LISTEN -> ListenMode(karFile, currentLyricIndex, isPlaying, { isPlaying = !isPlaying }, { currentLyricIndex = 0; isPlaying = false })
                    GameMode.READ -> ReadMode(karFile, currentWord, constructedWord,
                        { letter -> if (constructedWord.length < (currentWord?.text?.replace(" ","")?.length ?: 0)) { constructedWord += letter.uppercase(); AudioPlayer.playSFX(R.raw.sfx_type) } },
                        { if (constructedWord.isNotEmpty()) { constructedWord = constructedWord.dropLast(1); AudioPlayer.playSFX(R.raw.sfx_click) } },
                        { constructedWord = ""; AudioPlayer.playSFX(R.raw.sfx_reset) }, { gameMode = GameMode.LISTEN; currentLyricIndex = 0; isPlaying = true })
                    GameMode.SING -> SingMode(karFile, currentLyricIndex) { val resId = context.resources.getIdentifier("melody_song_${songIndex.toString().padStart(2,'0')}", "raw", context.packageName); if (resId != 0) AudioPlayer.playSFX(resId) }
                }
            }
        }
        if (showLevelComplete) LevelComplete(stars, "Песня «${karFile.metadata.title}» спета!\nСлов правильно: $score из ${words.size}", onNext = { if (songIndex < 20) onNextSong() else onGameComplete() })
    }
}

// ==================== Компоненты ====================
@Composable
private fun SongHeader(karFile: KarFile, songIndex: Int) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = FairyGold.copy(alpha = 0.2f)), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(karFile.metadata.title, style = MaterialTheme.typography.titleMedium, color = FairyPurple, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(karFile.metadata.author, style = MaterialTheme.typography.bodySmall, color = FairyBlue)
        }
    }
}

@Composable
private fun SongProgressBar(currentSong: Int, totalSongs: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("$currentSong/$totalSongs", style = MaterialTheme.typography.labelSmall, color = FairyPurple, modifier = Modifier.width(36.dp))
        LinearProgressIndicator(progress = currentSong.toFloat() / totalSongs, modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)), color = FairyGold, trackColor = FairyGold.copy(alpha = 0.2f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameModeSelector(currentMode: GameMode, onModeSelected: (GameMode) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(GameMode.LISTEN to "👂 Слушаю", GameMode.READ to "📖 Читаю", GameMode.SING to "🎤 Пою").forEach { (mode, label) ->
            FilterChip(selected = currentMode == mode, onClick = { onModeSelected(mode) }, label = { Text(label, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = when (mode) { GameMode.LISTEN -> FairyGold; GameMode.READ -> FairyGreen; GameMode.SING -> FairyPurple }, selectedLabelColor = Color.White),
                modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ListenMode(karFile: KarFile, currentLyricIndex: Int, isPlaying: Boolean, onPlayPause: () -> Unit, onReset: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Слушай и следи за слогами:", style = MaterialTheme.typography.titleSmall, color = FairyGold, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            items(karFile.lyrics.chunked(6)) { line ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.Center) {
                    line.forEach { lyric ->
                        val isCur = karFile.lyrics.indexOf(lyric) == currentLyricIndex; val isPast = karFile.lyrics.indexOf(lyric) < currentLyricIndex
                        Box(Modifier.padding(1.dp).clip(RoundedCornerShape(6.dp)).background(when { isCur -> FairyGold.copy(alpha = 0.8f); isPast -> FairyGreen.copy(alpha = 0.3f); else -> Color.Transparent }).border(1.dp, if (isCur) FairyGold else Color.Transparent, RoundedCornerShape(6.dp)).then(if (isCur) Modifier.scale(1.1f) else Modifier).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(lyric.text, fontSize = if (isCur) 24.sp else 18.sp, fontWeight = if (isCur) FontWeight.ExtraBold else FontWeight.Normal, color = when { isCur -> Color.White; isPast -> FairyGreen; else -> FairyBlue })
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onReset) { Text("🔄", fontSize = 20.sp) }
            FloatingActionButton(onClick = onPlayPause, containerColor = if (isPlaying) FairyPink else FairyGreen, modifier = Modifier.size(48.dp)) { Text(if (isPlaying) "⏸" else "▶", fontSize = 22.sp, color = Color.White) }
            if (karFile.lyrics.isNotEmpty()) Text("${currentLyricIndex+1}/${karFile.lyrics.size}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
private fun ReadMode(karFile: KarFile, currentWord: KarWord?, constructedWord: String, onLetterPress: (Char) -> Unit, onDelete: () -> Unit, onClear: () -> Unit, onListen: () -> Unit) {
    if (currentWord == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Все слова пройдены!", color = FairyGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold) }; return }
    Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Составь слово:", style = MaterialTheme.typography.titleSmall, color = FairyPurple, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.Center) { currentWord.syllables.forEach { Text(it, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FairyGold, modifier = Modifier.padding(horizontal = 3.dp)) } }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(10.dp)).background(if (constructedWord == currentWord.text.replace(" ","")) FairyGreen.copy(alpha = 0.2f) else FairyBlue.copy(alpha = 0.1f)).border(2.dp, if (constructedWord.isNotEmpty()) FairyGold else Color.Gray, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Text(if (constructedWord.isEmpty()) "_ ".repeat(currentWord.text.replace(" ","").length).trim() else constructedWord.toCharArray().joinToString(" "), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (constructedWord == currentWord.text.replace(" ","")) FairyGreen else FairyBlue)
        }
        Spacer(Modifier.height(12.dp))
        val letters = remember(currentWord) { val l = currentWord.text.replace(" ","").uppercase().toCharArray().toMutableList(); val ex = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".toCharArray(); ex.filter { it !in l }.shuffled().take(4).forEach { l.add(it) }; l.shuffled() }
        LazyVerticalGrid(columns = GridCells.Fixed(6), modifier = Modifier.height(100.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(letters) { letter ->
                val used = constructedWord.count { it == letter } >= currentWord.text.count { it.uppercaseChar() == letter }
                Box(Modifier.aspectRatio(1f).clip(CircleShape).background(if (used) Color.Gray.copy(alpha = 0.3f) else FairyGold.copy(alpha = 0.2f)).border(2.dp, if (used) Color.Gray else FairyGold, CircleShape).clickable(enabled = !used) { onLetterPress(letter) }, contentAlignment = Alignment.Center) { Text(letter.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (used) Color.Gray else Color.Black) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            OutlinedButton(onClick = onListen, Modifier.height(40.dp)) { Text("👂", fontSize = 16.sp) }
            Button(onClick = onDelete, Modifier.height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPink)) { Text("⌫", fontSize = 16.sp) }
            Button(onClick = onClear, Modifier.height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("🔄", fontSize = 16.sp) }
        }
    }
}

@Composable
private fun SingMode(karFile: KarFile, currentLyricIndex: Int, onPlayMelody: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎤 Пой вместе с Василисой!", style = MaterialTheme.typography.titleSmall, color = FairyPurple, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            itemsIndexed(karFile.lyrics.chunked(5)) { _, line ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center) {
                    line.forEach { lyric ->
                        val absIdx = karFile.lyrics.indexOf(lyric); val isCur = absIdx == currentLyricIndex
                        Text(lyric.text, fontSize = if (isCur) 28.sp else 22.sp, fontWeight = if (isCur) FontWeight.ExtraBold else FontWeight.Bold, color = when { isCur -> FairyPink; absIdx < currentLyricIndex -> FairyGreen; else -> FairyBlue }, modifier = Modifier.padding(horizontal = 3.dp).then(if (isCur) Modifier.scale(1.1f) else Modifier))
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onPlayMelody, Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyGold), shape = RoundedCornerShape(12.dp)) { Text("🎵 Прослушать мелодию", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

// ==================== Вспомогательные функции ====================
data class KarWord(val text: String, val syllables: List<String>, val startIndex: Int, val endIndex: Int)

private fun groupSyllablesIntoWords(lyrics: List<KarLyricEvent>): List<KarWord> {
    if (lyrics.isEmpty()) return emptyList(); val words = mutableListOf<KarWord>(); var ct = StringBuilder(); var cs = mutableListOf<String>(); var s = 0
    lyrics.forEachIndexed { i, lyric -> ct.append(lyric.text); cs.add(lyric.text); val last = i == lyrics.size-1; val ns = if (!last) lyrics[i+1].text.startsWith(" ") else false; if (last || ns || ct.length >= 6) { words.add(KarWord(ct.toString().trim().uppercase(), cs.toList(), s, i)); ct = StringBuilder(); cs = mutableListOf(); s = i+1 } }
    return words
}

enum class GameMode { LISTEN, READ, SING }
