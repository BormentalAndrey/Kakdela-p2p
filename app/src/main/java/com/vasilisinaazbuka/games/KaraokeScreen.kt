package com.vasilisinaazbuka.games

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

/**
 * Представляет спарсенный .kar (MIDI Karaoke) файл
 * 
 * Формат .kar основан на Standard MIDI File (SMF) с дополнительной текстовой дорожкой.
 * Текст песни хранится в Meta-событиях типа Lyrics (FF 05) и Text (FF 01).
 * Каждый слог/слово привязан к временной метке в миллисекундах.
 */
data class KarFile(
    val metadata: KarMetadata,
    val lyrics: List<KarLyricEvent>,
    val tempoEvents: List<KarTempoEvent>,
    val timeSignatureEvents: List<KarTimeSignatureEvent>,
    val totalDurationMs: Long
)

/**
 * Метаданные караоке-файла
 */
data class KarMetadata(
    val title: String = "Неизвестная песня",
    val author: String = "Народная",
    val copyright: String = "",
    val sequenceName: String = "",
    val trackNames: List<String> = emptyList(),
    val instruments: List<String> = emptyList()
)

/**
 * Событие текста песни (Lyrics Meta Event)
 */
data class KarLyricEvent(
    val timeMs: Long,           // Время от начала песни в миллисекундах
    val text: String,           // Текст слога или слова
    val trackIndex: Int = 0,    // Индекс дорожки
    val isSyllable: Boolean = true  // Это слог (true) или целое слово (false)
)

/**
 * Событие изменения темпа
 */
data class KarTempoEvent(
    val timeMs: Long,
    val tempoBpm: Int
)

/**
 * Событие изменения размера
 */
data class KarTimeSignatureEvent(
    val timeMs: Long,
    val numerator: Int,
    val denominator: Int
)

// ==================== Парсер .kar файлов ====================

/**
 * Парсер .kar (MIDI Karaoke) файлов
 * 
 * Структура MIDI-файла:
 * - Заголовок: "MThd" + длина (6 байт) + формат + количество дорожек + тики на четверть
 * - Дорожки: "MTrk" + длина + события
 * 
 * Meta-события:
 * - FF 01 len text — Text Event (название, автор)
 * - FF 02 len text — Copyright
 * - FF 03 len text — Sequence/Track Name
 * - FF 05 len text — Lyrics (текст песни)
 * - FF 51 03 tt tt tt — Set Tempo
 * - FF 58 04 nn dd cc bb — Time Signature
 */
object KarParser {

    // Константы MIDI
    private const val HEADER_CHUNK_ID = "MThd"
    private const val TRACK_CHUNK_ID = "MTrk"
    
    /**
     * Парсит .kar файл из InputStream
     */
    fun parse(inputStream: InputStream): KarFile {
        val data = inputStream.readBytes()
        return parse(data)
    }
    
    /**
     * Парсит .kar файл из массива байт
     */
    fun parse(data: ByteArray): KarFile {
        var offset = 0
        
        // Читаем заголовок
        val headerId = String(data, offset, 4)
        offset += 4
        
        if (headerId != HEADER_CHUNK_ID) {
            throw IllegalArgumentException("Неверный формат файла. Ожидается MThd, получено: $headerId")
        }
        
        // Длина заголовка (всегда 6)
        val headerLength = readInt32(data, offset)
        offset += 4
        
        // Формат MIDI (0, 1 или 2)
        val format = readInt16(data, offset)
        offset += 2
        
        // Количество дорожек
        val trackCount = readInt16(data, offset)
        offset += 2
        
        // Тики на четверть (разрешение времени)
        val ticksPerQuarter = readInt16(data, offset)
        offset += 2
        
        // Проверяем, что длина заголовка 6
        if (headerLength != 6) {
            offset += (headerLength - 6) // Пропускаем дополнительные байты
        }
        
        // Парсим дорожки
        val lyrics = mutableListOf<KarLyricEvent>()
        val tempoEvents = mutableListOf<KarTempoEvent>()
        val timeSignatureEvents = mutableListOf<KarTimeSignatureEvent>()
        val trackNames = mutableListOf<String>()
        val instruments = mutableListOf<String>()
        var title = "Неизвестная песня"
        var author = "Народная"
        var copyright = ""
        
        var currentTempo = 120 // BPM по умолчанию (500000 микросекунд на четверть)
        var microsecondsPerQuarter = 500000
        
        for (trackIndex in 0 until trackCount) {
            // Читаем заголовок дорожки
            val trackId = String(data, offset, 4)
            offset += 4
            
            if (trackId != TRACK_CHUNK_ID) {
                throw IllegalArgumentException("Ожидается MTrk на дорожке $trackIndex")
            }
            
            val trackLength = readInt32(data, offset)
            offset += 4
            
            val trackEnd = offset + trackLength
            var absoluteTimeMs = 0L
            var runningStatus: Byte = 0
            
            // Парсим события дорожки
            while (offset < trackEnd) {
                // Читаем дельта-время
                val deltaTime = readVariableLength(data, offset)
                offset += deltaTime.first
                val deltaTicks = deltaTime.second
                
                // Переводим тики в миллисекунды
                val deltaMs = ticksToMilliseconds(deltaTicks, ticksPerQuarter, microsecondsPerQuarter)
                absoluteTimeMs += deltaMs
                
                // Читаем событие
                var eventByte = data[offset].toInt() and 0xFF
                
                // Running Status
                if (eventByte < 0x80) {
                    eventByte = runningStatus.toInt() and 0xFF
                } else {
                    runningStatus = eventByte.toByte()
                    offset++
                }
                
                when {
                    // Meta Event
                    eventByte == 0xFF -> {
                        val metaType = data[offset].toInt() and 0xFF
                        offset++
                        val metaLength = readVariableLength(data, offset)
                        offset += metaLength.first
                        val length = metaLength.second
                        
                        when (metaType) {
                            // Text Event
                            0x01 -> {
                                val text = String(data, offset, length, Charsets.UTF_8)
                                // Может содержать название песни или автора
                                if (text.startsWith("@T")) {
                                    title = text.substring(2).trim()
                                } else if (text.startsWith("@A")) {
                                    author = text.substring(2).trim()
                                }
                            }
                            // Copyright
                            0x02 -> {
                                copyright = String(data, offset, length, Charsets.UTF_8)
                            }
                            // Sequence/Track Name
                            0x03 -> {
                                val name = String(data, offset, length, Charsets.UTF_8)
                                trackNames.add(name)
                            }
                            // Instrument Name
                            0x04 -> {
                                val instrument = String(data, offset, length, Charsets.UTF_8)
                                instruments.add(instrument)
                            }
                            // Lyrics
                            0x05 -> {
                                val text = String(data, offset, length, Charsets.UTF_8)
                                if (text.isNotBlank()) {
                                    // Разбиваем текст на слоги
                                    val syllables = splitIntoSyllables(text)
                                    var syllableTime = absoluteTimeMs
                                    val syllableDuration = deltaMs / maxOf(syllables.size, 1)
                                    
                                    syllables.forEach { syllable ->
                                        lyrics.add(
                                            KarLyricEvent(
                                                timeMs = syllableTime,
                                                text = syllable,
                                                trackIndex = trackIndex,
                                                isSyllable = true
                                            )
                                        )
                                        syllableTime += syllableDuration
                                    }
                                }
                            }
                            // Set Tempo
                            0x51 -> {
                                microsecondsPerQuarter = ((data[offset].toInt() and 0xFF) shl 16) or
                                        ((data[offset + 1].toInt() and 0xFF) shl 8) or
                                        (data[offset + 2].toInt() and 0xFF)
                                currentTempo = (60_000_000 / microsecondsPerQuarter)
                                tempoEvents.add(
                                    KarTempoEvent(
                                        timeMs = absoluteTimeMs,
                                        tempoBpm = currentTempo
                                    )
                                )
                            }
                            // Time Signature
                            0x58 -> {
                                val numerator = data[offset].toInt() and 0xFF
                                val denominator = 1 shl (data[offset + 1].toInt() and 0xFF)
                                timeSignatureEvents.add(
                                    KarTimeSignatureEvent(
                                        timeMs = absoluteTimeMs,
                                        numerator = numerator,
                                        denominator = denominator
                                    )
                                )
                            }
                            // End of Track
                            0x2F -> {
                                offset = trackEnd // Завершаем обработку дорожки
                            }
                        }
                        
                        offset += length
                    }
                    // System Exclusive
                    eventByte == 0xF0 || eventByte == 0xF7 -> {
                        val sysExLength = readVariableLength(data, offset)
                        offset += sysExLength.first + sysExLength.second
                    }
                    // MIDI Event
                    eventByte in 0x80..0xEF -> {
                        val channel = eventByte and 0x0F
                        val eventType = eventByte and 0xF0
                        
                        when (eventType) {
                            // Note Off или Note On
                            0x80.toInt(), 0x90.toInt() -> {
                                offset += 2 // Пропускаем note и velocity
                            }
                            // Polyphonic Aftertouch
                            0xA0.toInt() -> {
                                offset += 2
                            }
                            // Control Change
                            0xB0.toInt() -> {
                                offset += 2
                            }
                            // Program Change
                            0xC0.toInt() -> {
                                offset += 1
                            }
                            // Channel Aftertouch
                            0xD0.toInt() -> {
                                offset += 1
                            }
                            // Pitch Bend
                            0xE0.toInt() -> {
                                offset += 2
                            }
                        }
                    }
                }
            }
        }
        
        // Вычисляем общую длительность
        val totalDurationMs = if (lyrics.isNotEmpty()) {
            lyrics.last().timeMs + 2000 // Добавляем 2 секунды после последнего слога
        } else {
            30000L // 30 секунд по умолчанию
        }
        
        // Если название не найдено в метаданных, пытаемся извлечь из имени дорожки
        if (title == "Неизвестная песня" && trackNames.isNotEmpty()) {
            title = trackNames.first()
        }
        
        return KarFile(
            metadata = KarMetadata(
                title = title,
                author = author,
                copyright = copyright,
                sequenceName = trackNames.firstOrNull() ?: "",
                trackNames = trackNames,
                instruments = instruments
            ),
            lyrics = lyrics.sortedBy { it.timeMs },
            tempoEvents = tempoEvents,
            timeSignatureEvents = timeSignatureEvents,
            totalDurationMs = totalDurationMs
        )
    }
    
    /**
     * Читает 16-битное целое (big-endian)
     */
    private fun readInt16(data: ByteArray, offset: Int): Int {
        return ((data[offset].toInt() and 0xFF) shl 8) or
                (data[offset + 1].toInt() and 0xFF)
    }
    
    /**
     * Читает 32-битное целое (big-endian)
     */
    private fun readInt32(data: ByteArray, offset: Int): Int {
        return ((data[offset].toInt() and 0xFF) shl 24) or
                ((data[offset + 1].toInt() and 0xFF) shl 16) or
                ((data[offset + 2].toInt() and 0xFF) shl 8) or
                (data[offset + 3].toInt() and 0xFF)
    }
    
    /**
     * Читает значение переменной длины (Variable Length Value)
     * Возвращает пару (количество прочитанных байт, значение)
     */
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
    
    /**
     * Переводит тики в миллисекунды
     */
    private fun ticksToMilliseconds(ticks: Int, ticksPerQuarter: Int, microsecondsPerQuarter: Int): Long {
        if (ticksPerQuarter == 0) return 0
        return (ticks.toLong() * microsecondsPerQuarter) / (ticksPerQuarter * 1000)
    }
    
    /**
     * Разбивает текст на слоги (простая эвристика для русского языка)
     */
    private fun splitIntoSyllables(text: String): List<String> {
        if (text.length <= 2) return listOf(text)
        
        val syllables = mutableListOf<String>()
        var currentSyllable = StringBuilder()
        
        val vowels = setOf('А', 'Е', 'Ё', 'И', 'О', 'У', 'Ы', 'Э', 'Ю', 'Я',
                           'а', 'е', 'ё', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я')
        
        for (char in text) {
            currentSyllable.append(char)
            if (char in vowels && currentSyllable.length > 1) {
                syllables.add(currentSyllable.toString().uppercase())
                currentSyllable = StringBuilder()
            }
        }
        
        if (currentSyllable.isNotEmpty()) {
            syllables.add(currentSyllable.toString().uppercase())
        }
        
        return if (syllables.isEmpty()) listOf(text.uppercase()) else syllables
    }
}

// ==================== Загрузчик .kar файлов ====================

/**
 * Менеджер загрузки и кеширования .kar файлов
 */
object KarFileLoader {
    
    private val cache = mutableMapOf<String, KarFile>()
    
    /**
     * Загружает .kar файл из assets/kar/
     * @param context Контекст приложения
     * @param songIndex Индекс песни (1-20)
     */
    fun loadKarFile(context: Context, songIndex: Int): KarFile? {
        val fileName = "song_${songIndex.toString().padStart(2, '0')}"
        
        // Проверяем кеш
        cache[fileName]?.let { return it }
        
        return try {
            val inputStream = context.assets.open("kar/$fileName.kar")
            val karFile = KarParser.parse(inputStream)
            inputStream.close()
            cache[fileName] = karFile
            karFile
        } catch (e: Exception) {
            // Если файл не найден, создаём демо-файл
            createDemoKarFile(fileName)
        }
    }
    
    /**
     * Загружает .kar файл по имени
     */
    fun loadKarFileByName(context: Context, fileName: String): KarFile? {
        cache[fileName]?.let { return it }
        
        return try {
            val inputStream = context.assets.open("kar/$fileName.kar")
            val karFile = KarParser.parse(inputStream)
            inputStream.close()
            cache[fileName] = karFile
            karFile
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Получает список доступных песен
     */
    fun getAvailableSongs(context: Context): List<String> {
        return try {
            context.assets.list("kar/")
                ?.filter { it.endsWith(".kar") }
                ?.map { it.removeSuffix(".kar") }
                ?.sorted()
                ?: (1..20).map { "song_${it.toString().padStart(2, '0')}" }
        } catch (e: Exception) {
            (1..20).map { "song_${it.toString().padStart(2, '0')}" }
        }
    }
    
    /**
     * Очищает кеш
     */
    fun clearCache() {
        cache.clear()
    }
    
    /**
     * Создаёт демо-файл для тестирования
     */
    private fun createDemoKarFile(fileName: String): KarFile {
        val lyrics = when (fileName) {
            "song_01" -> listOf(
                KarLyricEvent(0, "МА"),
                KarLyricEvent(500, "МА"),
                KarLyricEvent(1000, "МО"),
                KarLyricEvent(1500, "Я"),
                KarLyricEvent(2000, "РО"),
                KarLyricEvent(2500, "ДИ"),
                KarLyricEvent(3000, "НА"),
                KarLyricEvent(3500, "МО"),
                KarLyricEvent(4000, "Я")
            )
            "song_02" -> listOf(
                KarLyricEvent(0, "МОС"),
                KarLyricEvent(500, "КВА"),
                KarLyricEvent(1000, "СТО"),
                KarLyricEvent(1500, "ЛИ"),
                KarLyricEvent(2000, "ЦА"),
                KarLyricEvent(2500, "РОС"),
                KarLyricEvent(3000, "СИ"),
                KarLyricEvent(3500, "И")
            )
            "song_03" -> listOf(
                KarLyricEvent(0, "РО"),
                KarLyricEvent(500, "ДИ"),
                KarLyricEvent(1000, "НА"),
                KarLyricEvent(1500, "РОС"),
                KarLyricEvent(2000, "СИ"),
                KarLyricEvent(2500, "Я"),
                KarLyricEvent(3000, "БЕ"),
                KarLyricEvent(3500, "РЁ"),
                KarLyricEvent(4000, "ЗА"),
                KarLyricEvent(4500, "МО"),
                KarLyricEvent(5000, "Я")
            )
            else -> listOf(
                KarLyricEvent(0, "ВА"),
                KarLyricEvent(500, "СИ"),
                KarLyricEvent(1000, "ЛИ"),
                KarLyricEvent(1500, "СА"),
                KarLyricEvent(2000, "ПРЕ"),
                KarLyricEvent(2500, "КРАС"),
                KarLyricEvent(3000, "НА"),
                KarLyricEvent(3500, "Я")
            )
        }
        
        return KarFile(
            metadata = KarMetadata(
                title = when (fileName) {
                    "song_01" -> "Мама"
                    "song_02" -> "Москва"
                    "song_03" -> "Родина"
                    else -> "Василиса"
                },
                author = "Василиса Премудрая"
            ),
            lyrics = lyrics,
            tempoEvents = listOf(KarTempoEvent(0, 120)),
            timeSignatureEvents = listOf(KarTimeSignatureEvent(0, 4, 4)),
            totalDurationMs = (lyrics.lastOrNull()?.timeMs ?: 0) + 2000
        )
    }
}

// ==================== Основной экран игры ====================

/**
 * Игра «Караоке-читалка» — полноценное караоке с 20 песнями из .kar файлов
 * 
 * @param songIndex Индекс песни (1-20)
 * @param stage Этап внутри песни (для разбивки сложных песен)
 * @param onNextStage Переход к следующему этапу
 * @param onNextSong Переход к следующей песне
 * @param onGameComplete Завершение игры
 * @param onBack Навигация назад
 */
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
    
    // Загрузка .kar файла
    val karFile = remember(songIndex) {
        KarFileLoader.loadKarFile(context, songIndex)
    }
    
    if (karFile == null) {
        // Ошибка загрузки
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FairyBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎵",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Загрузка песни...",
                    style = MaterialTheme.typography.titleLarge,
                    color = FairyPurple
                )
                Text(
                    text = "Песня $songIndex из 20",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        return
    }
    
    // Состояния игры
    var gameMode by remember { mutableStateOf(GameMode.LISTEN) }
    var currentLyricIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    
    // Состояния для режима чтения
    var constructedWord by remember { mutableStateOf("") }
    var currentWordIndex by remember { mutableIntStateOf(0) }
    
    // Группировка слогов в слова
    val words = remember(karFile) {
        groupSyllablesIntoWords(karFile.lyrics)
    }
    
    // Текущее слово
    val currentWord = words.getOrNull(currentWordIndex)
    
    // Сброс состояния при смене песни
    LaunchedEffect(songIndex) {
        gameMode = GameMode.LISTEN
        currentLyricIndex = 0
        currentWordIndex = 0
        constructedWord = ""
        score = 0
        mistakes = 0
        showLevelComplete = false
        isPlaying = false
    }
    
    // Автоматическое воспроизведение в режиме LISTEN
    LaunchedEffect(gameMode, isPlaying) {
        if (gameMode == GameMode.LISTEN && isPlaying && karFile.lyrics.isNotEmpty()) {
            for (i in karFile.lyrics.indices) {
                currentLyricIndex = i
                
                // Вычисляем задержку до следующего слога
                val currentTime = karFile.lyrics[i].timeMs
                val nextTime = if (i + 1 < karFile.lyrics.size) {
                    karFile.lyrics[i + 1].timeMs
                } else {
                    currentTime + 500
                }
                val delayMs = (nextTime - currentTime).coerceAtLeast(100)
                
                delay(delayMs)
            }
            
            // Завершение воспроизведения
            delay(1000)
            isPlaying = false
            currentLyricIndex = karFile.lyrics.size - 1
        }
    }
    
    // Проверка правильности составленного слова
    LaunchedEffect(constructedWord) {
        if (currentWord != null && constructedWord == currentWord.text.replace(" ", "")) {
            score++
            AudioPlayer.playSFX(R.raw.sfx_success)
            
            delay(800)
            
            if (currentWordIndex < words.size - 1) {
                currentWordIndex++
                constructedWord = ""
            } else {
                // Все слова пройдены
                stars = when {
                    mistakes == 0 -> 3
                    mistakes <= 2 -> 2
                    else -> 1
                }
                showLevelComplete = true
                GameState.completeLevel("karaoke", songIndex, stars)
            }
        } else if (constructedWord.length >= (currentWord?.text?.replace(" ", "")?.length ?: 0)) {
            mistakes++
            AudioPlayer.playSFX(R.raw.sfx_error)
            delay(500)
            constructedWord = ""
        }
    }
    
    // Основной интерфейс
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FairyBlue.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок песни
            SongHeader(karFile = karFile, songIndex = songIndex)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Прогресс по песням (1 из 20)
            SongProgressBar(currentSong = songIndex, totalSongs = 20)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Персонаж Василиса
            CharacterView(
                character = "vasilisa",
                emotion = when {
                    showLevelComplete -> "proud"
                    mistakes > 3 -> "sad"
                    score > words.size / 2 -> "happy"
                    isPlaying -> "teacher"
                    else -> "neutral"
                },
                message = when (gameMode) {
                    GameMode.LISTEN -> if (isPlaying) "Слушай и следи за слогами!" else "Нажми ▶ чтобы послушать"
                    GameMode.READ -> "Составь слово из букв!"
                    GameMode.SING -> "Пой вместе со мной!"
                },
                modifier = Modifier.height(80.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Селектор режимов
            GameModeSelector(
                currentMode = gameMode,
                onModeSelected = { mode ->
                    gameMode = mode
                    if (mode == GameMode.LISTEN) {
                        currentLyricIndex = 0
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Основная область
            when (gameMode) {
                GameMode.LISTEN -> ListenMode(
                    karFile = karFile,
                    currentLyricIndex = currentLyricIndex,
                    isPlaying = isPlaying,
                    onPlayPause = { isPlaying = !isPlaying },
                    onReset = {
                        currentLyricIndex = 0
                        isPlaying = false
                    }
                )
                
                GameMode.READ -> ReadMode(
                    karFile = karFile,
                    currentWord = currentWord,
                    constructedWord = constructedWord,
                    onLetterPress = { letter ->
                        val targetLength = currentWord?.text?.replace(" ", "")?.length ?: 0
                        if (constructedWord.length < targetLength) {
                            constructedWord += letter.uppercase()
                            AudioPlayer.playSFX(R.raw.sfx_type)
                        }
                    },
                    onDelete = {
                        if (constructedWord.isNotEmpty()) {
                            constructedWord = constructedWord.dropLast(1)
                            AudioPlayer.playSFX(R.raw.sfx_click)
                        }
                    },
                    onClear = {
                        constructedWord = ""
                        AudioPlayer.playSFX(R.raw.sfx_reset)
                    },
                    onListen = {
                        gameMode = GameMode.LISTEN
                        currentLyricIndex = 0
                        isPlaying = true
                    }
                )
                
                GameMode.SING -> SingMode(
                    karFile = karFile,
                    currentLyricIndex = currentLyricIndex,
                    onPlayMelody = {
                        // Попытка воспроизвести MIDI
                        val resId = context.resources.getIdentifier(
                            "melody_song_${songIndex.toString().padStart(2, '0')}",
                            "raw",
                            context.packageName
                        )
                        if (resId != 0) {
                            AudioPlayer.playSFX(resId)
                        }
                    }
                )
            }
        }
        
        // Окно завершения песни
        if (showLevelComplete) {
            LevelComplete(
                stars = stars,
                message = "Песня «${karFile.metadata.title}» спета!\nСлов правильно: $score из ${words.size}",
                onNext = {
                    if (songIndex < 20) {
                        onNextSong()
                    } else {
                        onGameComplete()
                    }
                }
            )
        }
    }
}

// ==================== Компоненты ====================

/**
 * Заголовок песни
 */
@Composable
private fun SongHeader(karFile: KarFile, songIndex: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = FairyGold.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = karFile.metadata.title,
                style = MaterialTheme.typography.headlineSmall,
                color = FairyPurple,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = karFile.metadata.author,
                style = MaterialTheme.typography.bodyMedium,
                color = FairyBlue
            )
            if (karFile.metadata.copyright.isNotEmpty()) {
                Text(
                    text = karFile.metadata.copyright,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = "Песня $songIndex из 20 • ${karFile.metadata.trackNames.size} дорожек",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

/**
 * Прогресс-бар песен
 */
@Composable
private fun SongProgressBar(currentSong: Int, totalSongs: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Песня $currentSong из $totalSongs",
            style = MaterialTheme.typography.bodySmall,
            color = FairyPurple,
            modifier = Modifier.weight(1f)
        )
        
        LinearProgressIndicator(
            progress = { currentSong.toFloat() / totalSongs.toFloat() },
            modifier = Modifier
                .weight(2f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = FairyGold,
            trackColor = FairyGold.copy(alpha = 0.2f)
        )
    }
}

/**
 * Селектор режимов игры
 */
@Composable
private fun GameModeSelector(
    currentMode: GameMode,
    onModeSelected: (GameMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(
            GameMode.LISTEN to "👂 Слушаю",
            GameMode.READ to "📖 Читаю",
            GameMode.SING to "🎤 Пою"
        ).forEach { (mode, label) ->
            FilterChip(
                selected = currentMode == mode,
                onClick = { onModeSelected(mode) },
                label = {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (currentMode == mode) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (mode) {
                        GameMode.LISTEN -> FairyGold
                        GameMode.READ -> FairyGreen
                        GameMode.SING -> FairyPurple
                    },
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

/**
 * Режим СЛУШАЮ — отображение слогов с подсветкой
 */
@Composable
private fun ListenMode(
    karFile: KarFile,
    currentLyricIndex: Int,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Слушай и следи за слогами:",
                style = MaterialTheme.typography.titleMedium,
                color = FairyGold,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Отображение слогов с подсветкой
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Группируем слоги по строкам (примерно по 6-8 слогов в строке)
                val lines = karFile.lyrics.chunked(8)
                
                items(lines) { lineLyrics ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        lineLyrics.forEach { lyric ->
                            val isCurrent = karFile.lyrics.indexOf(lyric) == currentLyricIndex
                            val isPast = karFile.lyrics.indexOf(lyric) < currentLyricIndex
                            
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isCurrent -> FairyGold.copy(alpha = 0.8f)
                                            isPast -> FairyGreen.copy(alpha = 0.3f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isCurrent) FairyGold else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .then(
                                        if (isCurrent) Modifier.scale(1.15f) else Modifier
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = lyric.text,
                                    fontSize = if (isCurrent) 28.sp else 22.sp,
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = when {
                                        isCurrent -> Color.White
                                        isPast -> FairyGreen
                                        else -> FairyBlue
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопки управления воспроизведением
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка сброса
                IconButton(onClick = onReset) {
                    Text(text = "🔄", fontSize = 24.sp)
                }
                
                // Кнопка воспроизведения/паузы
                FloatingActionButton(
                    onClick = onPlayPause,
                    containerColor = if (isPlaying) FairyPink else FairyGreen
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        fontSize = 28.sp,
                        color = Color.White
                    )
                }
                
                // Индикатор прогресса
                if (karFile.lyrics.isNotEmpty()) {
                    Text(
                        text = "${currentLyricIndex + 1}/${karFile.lyrics.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            
            // Прогресс-бар воспроизведения
            if (karFile.lyrics.isNotEmpty() && karFile.totalDurationMs > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                
                val currentTime = if (currentLyricIndex < karFile.lyrics.size) {
                    karFile.lyrics[currentLyricIndex].timeMs
                } else {
                    karFile.totalDurationMs
                }
                
                LinearProgressIndicator(
                    progress = { 
                        (currentTime.toFloat() / karFile.totalDurationMs).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = FairyGold,
                    trackColor = FairyGold.copy(alpha = 0.2f)
                )
            }
        }
    }
}

/**
 * Режим ЧИТАЮ — составление слов из букв
 */
@Composable
private fun ReadMode(
    karFile: KarFile,
    currentWord: KarWord?,
    constructedWord: String,
    onLetterPress: (Char) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onListen: () -> Unit
) {
    if (currentWord == null) {
        Text(
            text = "Все слова пройдены!",
            color = FairyGreen,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        return
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Подсказка — слоги слова
            Text(
                text = "Составь слово:",
                style = MaterialTheme.typography.titleMedium,
                color = FairyPurple,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Показываем слоги как подсказку
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                currentWord.syllables.forEach { syllable ->
                    Text(
                        text = syllable,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = FairyGold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Поле для составленного слова
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (constructedWord == currentWord.text.replace(" ", ""))
                            FairyGreen.copy(alpha = 0.2f)
                        else
                            FairyBlue.copy(alpha = 0.1f)
                    )
                    .border(
                        2.dp,
                        if (constructedWord.isNotEmpty()) FairyGold else Color.Gray,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (constructedWord.isEmpty()) {
                        "_ ".repeat(currentWord.text.replace(" ", "").length).trim()
                    } else {
                        constructedWord.toCharArray().joinToString(" ")
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (constructedWord == currentWord.text.replace(" ", ""))
                        FairyGreen else FairyBlue,
                    letterSpacing = 4.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Клавиатура с буквами
            val availableLetters = remember(currentWord) {
                val letters = currentWord.text.replace(" ", "").uppercase().toCharArray().toMutableList()
                // Добавляем дополнительные буквы для сложности
                val extraLetters = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".toCharArray()
                extraLetters.filter { it !in letters }.shuffled().take(4).forEach { letters.add(it) }
                letters.shuffled()
            }
            
            Text(
                text = "Выбери буквы:",
                style = MaterialTheme.typography.bodyMedium,
                color = FairyPurple
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(120.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(availableLetters) { letter ->
                    val isUsed = constructedWord.count { it == letter } >=
                            currentWord.text.count { it.uppercaseChar() == letter }
                    
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                if (isUsed) Color.Gray.copy(alpha = 0.3f)
                                else FairyGold.copy(alpha = 0.2f)
                            )
                            .border(2.dp, if (isUsed) Color.Gray else FairyGold, CircleShape)
                            .clickable(enabled = !isUsed) {
                                onLetterPress(letter)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUsed) Color.Gray else Color.Black
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Кнопки управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onListen,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("👂 Послушать", fontSize = 14.sp)
                }
                
                Button(
                    onClick = onDelete,
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FairyPink)
                ) {
                    Text("⌫", fontSize = 18.sp)
                }
                
                Button(
                    onClick = onClear,
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("🔄", fontSize = 18.sp)
                }
            }
        }
    }
}

/**
 * Режим ПОЮ — свободное пение с текстом
 */
@Composable
private fun SingMode(
    karFile: KarFile,
    currentLyricIndex: Int,
    onPlayMelody: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎤 Пой вместе с Василисой!",
                style = MaterialTheme.typography.titleLarge,
                color = FairyPurple,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Текст песни крупными слогами
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val lines = karFile.lyrics.chunked(6)
                
                itemsIndexed(lines) { lineIndex, lineLyrics ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        lineLyrics.forEach { lyric ->
                            val absoluteIndex = karFile.lyrics.indexOf(lyric)
                            val isCurrent = absoluteIndex == currentLyricIndex
                            
                            Text(
                                text = lyric.text,
                                fontSize = if (isCurrent) 32.sp else 26.sp,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                                color = when {
                                    isCurrent -> FairyPink
                                    absoluteIndex < currentLyricIndex -> FairyGreen
                                    else -> FairyBlue
                                },
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .then(
                                        if (isCurrent) Modifier.scale(1.15f) else Modifier
                                    )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка воспроизведения мелодии
            Button(
                onClick = onPlayMelody,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FairyGold),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎵", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Прослушать мелодию",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== Вспомогательные функции ====================

/**
 * Модель слова из слогов
 */
data class KarWord(
    val text: String,
    val syllables: List<String>,
    val startIndex: Int,
    val endIndex: Int
)

/**
 * Группирует слоги в слова на основе текста
 */
private fun groupSyllablesIntoWords(lyrics: List<KarLyricEvent>): List<KarWord> {
    if (lyrics.isEmpty()) return emptyList()
    
    val words = mutableListOf<KarWord>()
    var currentWordText = StringBuilder()
    var currentSyllables = mutableListOf<String>()
    var startIndex = 0
    
    lyrics.forEachIndexed { index, lyric ->
        currentWordText.append(lyric.text)
        currentSyllables.add(lyric.text)
        
        // Проверяем, завершено ли слово (по длине или пробелу в следующем слоге)
        val isLast = index == lyrics.size - 1
        val nextLyric = if (!isLast) lyrics[index + 1] else null
        val nextStartsWithSpace = nextLyric?.text?.startsWith(" ") == true
        val currentEndsWithSpace = lyric.text.endsWith(" ")
        
        if (isLast || nextStartsWithSpace || currentWordText.length >= 6) {
            words.add(
                KarWord(
                    text = currentWordText.toString().trim().uppercase(),
                    syllables = currentSyllables.toList(),
                    startIndex = startIndex,
                    endIndex = index
                )
            )
            currentWordText = StringBuilder()
            currentSyllables = mutableListOf()
            startIndex = index + 1
        }
    }
    
    return words
}

/**
 * Режимы игры
 */
enum class GameMode {
    LISTEN,  // Слушаем и следим за слогами
    READ,    // Составляем слова из букв
    SING     // Поём вместе
}
