package com.vasilisinaazbuka.games

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

// ==================== Модели данных для .kar файлов ====================

data class KarFile(
    val metadata: KarMetadata,
    val lyrics: List<KarLyricEvent>,
    val tempoEvents: List<KarTempoEvent>,
    val timeSignatureEvents: List<KarTimeSignatureEvent>,
    val totalDurationMs: Long
)

data class KarMetadata(
    val title: String = "Неизвестная песня",
    val author: String = "Народная",
    val copyright: String = "",
    val sequenceName: String = "",
    val trackNames: List<String> = emptyList(),
    val instruments: List<String> = emptyList()
)

data class KarLyricEvent(
    val timeMs: Long,
    val text: String,
    val trackIndex: Int = 0,
    val isSyllable: Boolean = true
)

data class KarTempoEvent(
    val timeMs: Long,
    val tempoBpm: Int
)

data class KarTimeSignatureEvent(
    val timeMs: Long,
    val numerator: Int,
    val denominator: Int
)

// ==================== Парсер .kar файлов ====================

object KarParser {

    private const val HEADER_CHUNK_ID = "MThd"
    private const val TRACK_CHUNK_ID = "MTrk"

    fun parse(inputStream: InputStream): KarFile {
        val data = inputStream.readBytes()
        return parse(data)
    }

    fun parse(data: ByteArray): KarFile {
        var offset = 0
        val headerId = String(data, offset, 4)
        offset += 4
        if (headerId != HEADER_CHUNK_ID) {
            throw IllegalArgumentException("Неверный формат файла: $headerId")
        }
        val headerLength = readInt32(data, offset)
        offset += 4
        val format = readInt16(data, offset)
        offset += 2
        val trackCount = readInt16(data, offset)
        offset += 2
        val ticksPerQuarter = readInt16(data, offset)
        offset += 2
        if (headerLength != 6) offset += (headerLength - 6)

        val lyrics = mutableListOf<KarLyricEvent>()
        val tempoEvents = mutableListOf<KarTempoEvent>()
        val timeSignatureEvents = mutableListOf<KarTimeSignatureEvent>()
        val trackNames = mutableListOf<String>()
        val instruments = mutableListOf<String>()
        var title = "Неизвестная песня"
        var author = "Народная"
        var copyright = ""
        var microsecondsPerQuarter = 500000

        for (trackIndex in 0 until trackCount) {
            val trackId = String(data, offset, 4)
            offset += 4
            if (trackId != TRACK_CHUNK_ID) throw IllegalArgumentException("Ожидается MTrk")
            val trackLength = readInt32(data, offset)
            offset += 4
            val trackEnd = offset + trackLength
            var absoluteTimeMs = 0L
            var runningStatus: Byte = 0

            while (offset < trackEnd) {
                val deltaTime = readVariableLength(data, offset)
                offset += deltaTime.first
                val deltaTicks = deltaTime.second
                val deltaMs = ticksToMilliseconds(deltaTicks, ticksPerQuarter, microsecondsPerQuarter)
                absoluteTimeMs += deltaMs

                var eventByte = data[offset].toInt() and 0xFF
                if (eventByte < 0x80) {
                    eventByte = runningStatus.toInt() and 0xFF
                } else {
                    runningStatus = eventByte.toByte()
                    offset++
                }

                when {
                    eventByte == 0xFF -> {
                        val metaType = data[offset].toInt() and 0xFF
                        offset++
                        val metaLength = readVariableLength(data, offset)
                        offset += metaLength.first
                        val length = metaLength.second
                        when (metaType) {
                            0x01 -> {
                                val text = String(data, offset, length, Charsets.UTF_8)
                                if (text.startsWith("@T")) title = text.substring(2).trim()
                                else if (text.startsWith("@A")) author = text.substring(2).trim()
                            }
                            0x02 -> copyright = String(data, offset, length, Charsets.UTF_8)
                            0x03 -> trackNames.add(String(data, offset, length, Charsets.UTF_8))
                            0x04 -> instruments.add(String(data, offset, length, Charsets.UTF_8))
                            0x05 -> {
                                val text = String(data, offset, length, Charsets.UTF_8)
                                if (text.isNotBlank()) {
                                    val syllables = splitIntoSyllables(text)
                                    var syllableTime = absoluteTimeMs
                                    val syllableDuration = deltaMs / maxOf(syllables.size, 1)
                                    syllables.forEach { syllable ->
                                        lyrics.add(KarLyricEvent(syllableTime, syllable, trackIndex, true))
                                        syllableTime += syllableDuration
                                    }
                                }
                            }
                            0x51 -> {
                                microsecondsPerQuarter = ((data[offset].toInt() and 0xFF) shl 16) or
                                        ((data[offset + 1].toInt() and 0xFF) shl 8) or
                                        (data[offset + 2].toInt() and 0xFF)
                                val tempoBpm = (60_000_000 / microsecondsPerQuarter)
                                tempoEvents.add(KarTempoEvent(absoluteTimeMs, tempoBpm))
                            }
                            0x58 -> {
                                val num = data[offset].toInt() and 0xFF
                                val den = 1 shl (data[offset + 1].toInt() and 0xFF)
                                timeSignatureEvents.add(KarTimeSignatureEvent(absoluteTimeMs, num, den))
                            }
                            0x2F -> offset = trackEnd
                        }
                        offset += length
                    }
                    eventByte == 0xF0 || eventByte == 0xF7 -> {
                        val sl = readVariableLength(data, offset)
                        offset += sl.first + sl.second
                    }
                    eventByte in 0x80..0xEF -> {
                        when (eventByte and 0xF0) {
                            0x80, 0x90 -> offset += 2
                            0xA0, 0xB0, 0xE0 -> offset += 2
                            0xC0, 0xD0 -> offset += 1
                        }
                    }
                }
            }
        }

        val totalDurationMs = if (lyrics.isNotEmpty()) lyrics.last().timeMs + 2000 else 30000L
        if (title == "Неизвестная песня" && trackNames.isNotEmpty()) title = trackNames.first()

        return KarFile(
            metadata = KarMetadata(title, author, copyright, trackNames.firstOrNull() ?: "", trackNames, instruments),
            lyrics = lyrics.sortedBy { it.timeMs },
            tempoEvents = tempoEvents,
            timeSignatureEvents = timeSignatureEvents,
            totalDurationMs = totalDurationMs
        )
    }

    private fun readInt16(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)

    private fun readInt32(data: ByteArray, offset: Int): Int =
        ((data[offset].toInt() and 0xFF) shl 24) or
        ((data[offset + 1].toInt() and 0xFF) shl 16) or
        ((data[offset + 2].toInt() and 0xFF) shl 8) or
        (data[offset + 3].toInt() and 0xFF)

    private fun readVariableLength(data: ByteArray, offset: Int): Pair<Int, Int> {
        var value = 0
        var bytesRead = 0
        while (true) {
            val byte = data[offset + bytesRead].toInt() and 0xFF
            bytesRead++
            value = (value shl 7) or (byte and 0x7F)
            if (byte and 0x80 == 0) break
        }
        return Pair(bytesRead, value)
    }

    private fun ticksToMilliseconds(ticks: Int, ticksPerQuarter: Int, microsecondsPerQuarter: Int): Long {
        if (ticksPerQuarter == 0) return 0
        return (ticks.toLong() * microsecondsPerQuarter) / (ticksPerQuarter * 1000)
    }

    private fun splitIntoSyllables(text: String): List<String> {
        if (text.length <= 2) return listOf(text)
        val syllables = mutableListOf<String>()
        var cur = StringBuilder()
        val vowels = setOf('А', 'Е', 'Ё', 'И', 'О', 'У', 'Ы', 'Э', 'Ю', 'Я', 'а', 'е', 'ё', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я')
        for (ch in text) {
            cur.append(ch)
            if (ch in vowels && cur.length > 1) { syllables.add(cur.toString().uppercase()); cur = StringBuilder() }
        }
        if (cur.isNotEmpty()) syllables.add(cur.toString().uppercase())
        return if (syllables.isEmpty()) listOf(text.uppercase()) else syllables
    }
}

// ==================== Основной экран игры ====================

@Composable
fun KaraokeScreen(
    songIndex: Int = 1,
    stage: Int = 1,
    onNextStage: () -> Unit = {},
    onNextSong: () -> Unit = {},
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    val karFile = remember(songIndex) {
        KarFileLoader.loadKarFile(context, songIndex)
    }

    if (karFile == null) {
        Box(Modifier.fillMaxSize().background(FairyBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎵", fontSize = 64.sp)
                Spacer(Modifier.height(16.dp))
                Text("Загрузка песни...", style = MaterialTheme.typography.titleLarge, color = FairyPurple)
                Text("Песня $songIndex из 20", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
        return
    }

    var gameMode by remember { mutableStateOf(GameMode.LISTEN) }
    var currentLyricIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var constructedWord by remember { mutableStateOf("") }
    var currentWordIndex by remember { mutableIntStateOf(0) }

    val words = remember(karFile) { groupSyllablesIntoWords(karFile.lyrics) }
    val currentWord = words.getOrNull(currentWordIndex)

    LaunchedEffect(songIndex) {
        gameMode = GameMode.LISTEN; currentLyricIndex = 0; currentWordIndex = 0
        constructedWord = ""; score = 0; mistakes = 0; showLevelComplete = false; isPlaying = false
    }

    LaunchedEffect(gameMode, isPlaying) {
        if (gameMode == GameMode.LISTEN && isPlaying && karFile.lyrics.isNotEmpty()) {
            for (i in karFile.lyrics.indices) {
                currentLyricIndex = i
                val cur = karFile.lyrics[i].timeMs
                val nxt = if (i + 1 < karFile.lyrics.size) karFile.lyrics[i + 1].timeMs else cur + 500
                delay((nxt - cur).coerceAtLeast(100))
            }
            delay(1000); isPlaying = false; currentLyricIndex = karFile.lyrics.size - 1
        }
    }

    LaunchedEffect(constructedWord) {
        if (currentWord != null && constructedWord == currentWord.text.replace(" ", "")) {
            score++; AudioPlayer.playSFX(R.raw.sfx_success); delay(800)
            if (currentWordIndex < words.size - 1) { currentWordIndex++; constructedWord = "" }
            else {
                stars = when { mistakes == 0 -> 3; mistakes <= 2 -> 2; else -> 1 }
                showLevelComplete = true; GameState.completeLevel("karaoke", songIndex, stars)
            }
        } else if (constructedWord.length >= (currentWord?.text?.replace(" ", "")?.length ?: 0)) {
            mistakes++; AudioPlayer.playSFX(R.raw.sfx_error); delay(500); constructedWord = ""
        }
    }

    Box(Modifier.fillMaxSize().background(FairyBlue.copy(alpha = 0.1f))) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            SongHeader(karFile, songIndex)
            Spacer(Modifier.height(8.dp))
            SongProgressBar(songIndex, 20)
            Spacer(Modifier.height(8.dp))
            CharacterView("vasilisa", when { showLevelComplete -> "proud"; mistakes > 3 -> "sad"; score > words.size / 2 -> "happy"; isPlaying -> "teacher"; else -> "neutral" },
                when (gameMode) { GameMode.LISTEN -> if (isPlaying) "Слушай и следи за слогами!" else "Нажми ▶ чтобы послушать"; GameMode.READ -> "Составь слово из букв!"; GameMode.SING -> "Пой вместе со мной!" },
                Modifier.height(80.dp))
            Spacer(Modifier.height(12.dp))
            GameModeSelector(gameMode) { mode -> gameMode = mode; if (mode == GameMode.LISTEN) currentLyricIndex = 0 }
            Spacer(Modifier.height(12.dp))
            when (gameMode) {
                GameMode.LISTEN -> ListenMode(karFile, currentLyricIndex, isPlaying, { isPlaying = !isPlaying }, { currentLyricIndex = 0; isPlaying = false })
                GameMode.READ -> ReadMode(karFile, currentWord, constructedWord,
                    { letter -> if (constructedWord.length < (currentWord?.text?.replace(" ", "")?.length ?: 0)) { constructedWord += letter.uppercase(); AudioPlayer.playSFX(R.raw.sfx_type) } },
                    { if (constructedWord.isNotEmpty()) { constructedWord = constructedWord.dropLast(1); AudioPlayer.playSFX(R.raw.sfx_click) } },
                    { constructedWord = ""; AudioPlayer.playSFX(R.raw.sfx_reset) },
                    { gameMode = GameMode.LISTEN; currentLyricIndex = 0; isPlaying = true })
                GameMode.SING -> SingMode(karFile, currentLyricIndex) {
                    val resId = context.resources.getIdentifier("melody_song_${songIndex.toString().padStart(2, '0')}", "raw", context.packageName)
                    if (resId != 0) AudioPlayer.playSFX(resId)
                }
            }
        }
        if (showLevelComplete) LevelComplete(stars, "Песня «${karFile.metadata.title}» спета!\nСлов правильно: $score из ${words.size}", onNext = { if (songIndex < 20) onNextSong() else onGameComplete() })
    }
}

// ==================== Компоненты ====================

@Composable
private fun SongHeader(karFile: KarFile, songIndex: Int) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = FairyGold.copy(alpha = 0.2f)), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(karFile.metadata.title, style = MaterialTheme.typography.headlineSmall, color = FairyPurple, fontWeight = FontWeight.Bold)
            Text(karFile.metadata.author, style = MaterialTheme.typography.bodyMedium, color = FairyBlue)
            if (karFile.metadata.copyright.isNotEmpty()) Text(karFile.metadata.copyright, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("Песня $songIndex из 20 • ${karFile.metadata.trackNames.size} дорожек", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
private fun SongProgressBar(currentSong: Int, totalSongs: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Песня $currentSong из $totalSongs", style = MaterialTheme.typography.bodySmall, color = FairyPurple, modifier = Modifier.weight(1f))
        LinearProgressIndicator(
            progress = currentSong.toFloat() / totalSongs.toFloat(),
            modifier = Modifier.weight(2f).height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = FairyGold, trackColor = FairyGold.copy(alpha = 0.2f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameModeSelector(currentMode: GameMode, onModeSelected: (GameMode) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf(GameMode.LISTEN to "👂 Слушаю", GameMode.READ to "📖 Читаю", GameMode.SING to "🎤 Пою").forEach { (mode, label) ->
            FilterChip(selected = currentMode == mode, onClick = { onModeSelected(mode) },
                label = { Text(label, fontSize = 14.sp, fontWeight = if (currentMode == mode) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (mode) { GameMode.LISTEN -> FairyGold; GameMode.READ -> FairyGreen; GameMode.SING -> FairyPurple },
                    selectedLabelColor = Color.White))
        }
    }
}

@Composable
private fun ListenMode(karFile: KarFile, currentLyricIndex: Int, isPlaying: Boolean, onPlayPause: () -> Unit, onReset: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Слушай и следи за слогами:", style = MaterialTheme.typography.titleMedium, color = FairyGold, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyColumn(Modifier.fillMaxWidth().height(300.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                items(karFile.lyrics.chunked(8)) { line ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center) {
                        line.forEach { lyric ->
                            val isCur = karFile.lyrics.indexOf(lyric) == currentLyricIndex
                            val isPast = karFile.lyrics.indexOf(lyric) < currentLyricIndex
                            Box(Modifier.padding(2.dp).clip(RoundedCornerShape(8.dp)).background(when { isCur -> FairyGold.copy(alpha = 0.8f); isPast -> FairyGreen.copy(alpha = 0.3f); else -> Color.Transparent })
                                .border(1.dp, if (isCur) FairyGold else Color.Transparent, RoundedCornerShape(8.dp)).then(if (isCur) Modifier.scale(1.15f) else Modifier).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(lyric.text, fontSize = if (isCur) 28.sp else 22.sp, fontWeight = if (isCur) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = when { isCur -> Color.White; isPast -> FairyGreen; else -> FairyBlue })
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onReset) { Text("🔄", fontSize = 24.sp) }
                FloatingActionButton(onClick = onPlayPause, containerColor = if (isPlaying) FairyPink else FairyGreen) {
                    Text(if (isPlaying) "⏸" else "▶", fontSize = 28.sp, color = Color.White)
                }
                if (karFile.lyrics.isNotEmpty()) Text("${currentLyricIndex + 1}/${karFile.lyrics.size}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            if (karFile.lyrics.isNotEmpty() && karFile.totalDurationMs > 0) {
                Spacer(Modifier.height(8.dp))
                val ct = if (currentLyricIndex < karFile.lyrics.size) karFile.lyrics[currentLyricIndex].timeMs else karFile.totalDurationMs
                LinearProgressIndicator(
                    progress = (ct.toFloat() / karFile.totalDurationMs).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = FairyGold, trackColor = FairyGold.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun ReadMode(karFile: KarFile, currentWord: KarWord?, constructedWord: String, onLetterPress: (Char) -> Unit, onDelete: () -> Unit, onClear: () -> Unit, onListen: () -> Unit) {
    if (currentWord == null) { Text("Все слова пройдены!", color = FairyGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold); return }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Составь слово:", style = MaterialTheme.typography.titleMedium, color = FairyPurple, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.Center) { currentWord.syllables.forEach { Text(it, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = FairyGold, modifier = Modifier.padding(horizontal = 4.dp)) } }
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp)).background(if (constructedWord == currentWord.text.replace(" ", "")) FairyGreen.copy(alpha = 0.2f) else FairyBlue.copy(alpha = 0.1f))
                .border(2.dp, if (constructedWord.isNotEmpty()) FairyGold else Color.Gray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(if (constructedWord.isEmpty()) "_ ".repeat(currentWord.text.replace(" ", "").length).trim() else constructedWord.toCharArray().joinToString(" "),
                    fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if (constructedWord == currentWord.text.replace(" ", "")) FairyGreen else FairyBlue, letterSpacing = 4.sp)
            }
            Spacer(Modifier.height(16.dp))
            val availableLetters = remember(currentWord) {
                val letters = currentWord.text.replace(" ", "").uppercase().toCharArray().toMutableList()
                val extra = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".toCharArray()
                extra.filter { it !in letters }.shuffled().take(4).forEach { letters.add(it) }
                letters.shuffled()
            }
            Text("Выбери буквы:", style = MaterialTheme.typography.bodyMedium, color = FairyPurple)
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(6), modifier = Modifier.height(120.dp), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(availableLetters) { letter ->
                    val isUsed = constructedWord.count { it == letter } >= currentWord.text.count { it.uppercaseChar() == letter }
                    Box(Modifier.aspectRatio(1f).clip(CircleShape).background(if (isUsed) Color.Gray.copy(alpha = 0.3f) else FairyGold.copy(alpha = 0.2f))
                        .border(2.dp, if (isUsed) Color.Gray else FairyGold, CircleShape).clickable(enabled = !isUsed) { onLetterPress(letter) }, contentAlignment = Alignment.Center) {
                        Text(letter.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isUsed) Color.Gray else Color.Black)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedButton(onClick = onListen, Modifier.height(48.dp)) { Text("👂 Послушать", fontSize = 14.sp) }
                Button(onClick = onDelete, Modifier.height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPink)) { Text("⌫", fontSize = 18.sp) }
                Button(onClick = onClear, Modifier.height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("🔄", fontSize = 18.sp) }
            }
        }
    }
}

@Composable
private fun SingMode(karFile: KarFile, currentLyricIndex: Int, onPlayMelody: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(8.dp), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎤 Пой вместе с Василисой!", style = MaterialTheme.typography.titleLarge, color = FairyPurple, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyColumn(Modifier.fillMaxWidth().height(350.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                itemsIndexed(karFile.lyrics.chunked(6)) { _, line ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center) {
                        line.forEach { lyric ->
                            val absIdx = karFile.lyrics.indexOf(lyric)
                            val isCur = absIdx == currentLyricIndex
                            Text(lyric.text, fontSize = if (isCur) 32.sp else 26.sp, fontWeight = if (isCur) FontWeight.ExtraBold else FontWeight.Bold,
                                color = when { isCur -> FairyPink; absIdx < currentLyricIndex -> FairyGreen; else -> FairyBlue },
                                modifier = Modifier.padding(horizontal = 4.dp).then(if (isCur) Modifier.scale(1.15f) else Modifier))
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onPlayMelody, Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyGold), shape = RoundedCornerShape(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text("🎵", fontSize = 24.sp); Spacer(Modifier.width(8.dp)); Text("Прослушать мелодию", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

// ==================== Вспомогательные функции ====================

data class KarWord(val text: String, val syllables: List<String>, val startIndex: Int, val endIndex: Int)

private fun groupSyllablesIntoWords(lyrics: List<KarLyricEvent>): List<KarWord> {
    if (lyrics.isEmpty()) return emptyList()
    val words = mutableListOf<KarWord>()
    var curText = StringBuilder()
    var curSyl = mutableListOf<String>()
    var start = 0
    lyrics.forEachIndexed { i, lyric ->
        curText.append(lyric.text); curSyl.add(lyric.text)
        val isLast = i == lyrics.size - 1
        val nextSpace = if (!isLast) lyrics[i + 1].text.startsWith(" ") else false
        if (isLast || nextSpace || curText.length >= 6) {
            words.add(KarWord(curText.toString().trim().uppercase(), curSyl.toList(), start, i))
            curText = StringBuilder(); curSyl = mutableListOf(); start = i + 1
        }
    }
    return words
}

enum class GameMode { LISTEN, READ, SING }
