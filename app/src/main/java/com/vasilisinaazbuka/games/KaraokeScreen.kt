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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.vasilisinaazbuka.R
import com.vasilisinaazbuka.audio.AudioPlayer
import com.vasilisinaazbuka.data.GameState
import com.vasilisinaazbuka.ui.CharacterView
import com.vasilisinaazbuka.ui.LevelComplete
import com.vasilisinaazbuka.ui.StageProgressIndicator
import com.vasilisinaazbuka.ui.StarDisplay
import com.vasilisinaazbuka.ui.theme.*
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

// ==================== Модели данных для .car файлов ====================

/**
 * Модель данных караоке-файла (.car)
 * Формат основан на предположительной структуре файлов караоке:
 * - Метаданные песни
 * - Массив слогов с временными метками
 * - Подсказки и изображения
 */
data class CarFile(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("metadata") val metadata: CarMetadata,
    @SerializedName("syllables") val syllables: List<CarSyllable>,
    @SerializedName("words") val words: List<CarWord>,
    @SerializedName("music_notes") val musicNotes: List<CarMusicNote>? = null,
    @SerializedName("background") val background: String? = null,
    @SerializedName("character_emotion") val characterEmotion: String = "neutral"
)

data class CarMetadata(
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String = "Народная сказка",
    @SerializedName("difficulty") val difficulty: Int = 1, // 1-5
    @SerializedName("tempo_bpm") val tempoBpm: Int = 120,
    @SerializedName("description") val description: String = "",
    @SerializedName("theme") val theme: String = "general", // nature, city, fairy_tale
    @SerializedName("image_resource") val imageResource: String? = null
)

data class CarSyllable(
    @SerializedName("id") val id: Int,
    @SerializedName("text") val text: String,       // Текст слога: "МА", "МА"
    @SerializedName("start_time_ms") val startTimeMs: Long,
    @SerializedName("end_time_ms") val endTimeMs: Long,
    @SerializedName("color") val color: String? = null, // HEX цвет для подсветки
    @SerializedName("emphasis") val emphasis: Boolean = false, // Ударный слог
    @SerializedName("animation") val animation: String? = null // bounce, pulse, none
)

data class CarWord(
    @SerializedName("id") val id: Int,
    @SerializedName("text") val text: String,       // Полное слово: "МАМА"
    @SerializedName("syllable_ids") val syllableIds: List<Int>,
    @SerializedName("hint_image") val hintImage: String? = null,
    @SerializedName("hint_text") val hintText: String? = null
)

data class CarMusicNote(
    @SerializedName("time_ms") val timeMs: Long,
    @SerializedName("note") val note: String,       // "C4", "D4", "E4"
    @SerializedName("duration_ms") val durationMs: Long,
    @SerializedName("syllable_id") val syllableId: Int? = null
)

// ==================== Загрузчик .car файлов ====================

/**
 * Менеджер загрузки и кеширования .car файлов
 */
object CarFileLoader {
    private val cache = mutableMapOf<String, CarFile>()
    private val gson = Gson()
    
    /**
     * Загружает .car файл из assets/car/
     * @param context Контекст приложения
     * @param fileName Имя файла без расширения (например, "song_01")
     */
    fun loadCarFile(context: Context, fileName: String): CarFile? {
        // Проверяем кеш
        cache[fileName]?.let { return it }
        
        return try {
            val inputStream = context.assets.open("car/$fileName.car")
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val jsonString = reader.readText()
            reader.close()
            
            val carFile = gson.fromJson(jsonString, CarFile::class.java)
            cache[fileName] = carFile
            carFile
        } catch (e: Exception) {
            // Если файл не найден, пытаемся загрузить из zip
            try {
                loadFromZip(context, fileName)
            } catch (e2: Exception) {
                // Возвращаем демо-файл для тестирования
                createDemoCarFile(fileName)
            }
        }
    }
    
    private fun loadFromZip(context: Context, fileName: String): CarFile? {
        val inputStream = context.assets.open("car/songs.zip")
        val zipStream = ZipInputStream(inputStream)
        
        var entry = zipStream.nextEntry
        while (entry != null) {
            if (entry.name == "$fileName.car") {
                val reader = BufferedReader(InputStreamReader(zipStream, "UTF-8"))
                val jsonString = reader.readText()
                val carFile = gson.fromJson(jsonString, CarFile::class.java)
                cache[fileName] = carFile
                return carFile
            }
            entry = zipStream.nextEntry
        }
        return null
    }
    
    /**
     * Создаёт демо-файл для тестирования
     */
    private fun createDemoCarFile(fileName: String): CarFile {
        return CarFile(
            metadata = CarMetadata(
                title = "Песня ${fileName.replace("song_", "")}",
                author = "Василиса Премудрая",
                difficulty = 2,
                tempoBpm = 120,
                description = "Демо-песня для тестирования"
            ),
            syllables = listOf(
                CarSyllable(1, "МА", 0, 500, emphasis = true),
                CarSyllable(2, "МА", 500, 1000),
                CarSyllable(3, "МО", 1500, 2000, emphasis = true),
                CarSyllable(4, "СКВА", 2000, 3000)
            ),
            words = listOf(
                CarWord(1, "МАМА", listOf(1, 2)),
                CarWord(2, "МОСКВА", listOf(3, 4))
            )
        )
    }
    
    fun getAvailableSongs(context: Context): List<String> {
        return try {
            context.assets.list("car/")
                ?.filter { it.endsWith(".car") }
                ?.map { it.removeSuffix(".car") }
                ?.sorted()
                ?: (1..20).map { "song_${it.toString().padStart(2, '0')}" }
        } catch (e: Exception) {
            (1..20).map { "song_${it.toString().padStart(2, '0')}" }
        }
    }
    
    fun clearCache() {
        cache.clear()
    }
}

// ==================== Основной экран игры ====================

/**
 * Игра «Караоке-читалка» — полноценное караоке с 20 песнями из .car файлов
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
    
    // Загрузка .car файла
    val carFile = remember(songIndex) {
        val fileName = "song_${songIndex.toString().padStart(2, '0')}"
        CarFileLoader.loadCarFile(context, fileName)
    }
    
    if (carFile == null) {
        // Ошибка загрузки
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Ошибка загрузки песни", color = FairyPink, fontSize = 24.sp)
        }
        return
    }
    
    // Состояния игры
    var gameMode by remember { mutableStateOf(GameMode.PREVIEW) }
    var currentSyllableIndex by remember { mutableIntStateOf(0) }
    var constructedWord by remember { mutableStateOf("") }
    var currentWordIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var stars by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    
    // Анимации
    val bounceAnimation by animateFloatAsState(
        targetValue = if (currentSyllableIndex >= 0) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.3f, stiffness = 100f),
        label = "bounce"
    )
    
    // Текущее слово и слоги
    val currentWord = carFile.words.getOrNull(currentWordIndex)
    val currentSyllables = currentWord?.syllableIds?.mapNotNull { id ->
        carFile.syllables.find { it.id == id }
    } ?: emptyList()
    
    // Клавиатура с буквами
    val alphabet = remember {
        val letters = mutableListOf<Char>()
        currentSyllables.forEach { syllable ->
            syllable.text.forEach { char ->
                if (char.isLetter() && char !in letters) {
                    letters.add(char)
                }
            }
        }
        // Добавляем дополнительные буквы для сложности
        "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".forEach { char ->
            if (char !in letters) letters.add(char)
        }
        letters.shuffled().take(12) // Ограничиваем до 12 букв для детской клавиатуры
    }
    
    // Сброс состояния при смене песни
    LaunchedEffect(songIndex) {
        gameMode = GameMode.PREVIEW
        currentSyllableIndex = 0
        currentWordIndex = 0
        constructedWord = ""
        score = 0
        mistakes = 0
        showLevelComplete = false
        isPlaying = false
    }
    
    // Автоматическое воспроизведение в режиме LISTEN
    LaunchedEffect(gameMode, isPlaying) {
        if (gameMode == GameMode.LISTEN && isPlaying) {
            for (syllable in carFile.syllables) {
                val delayTime = if (syllable.startTimeMs > 0) {
                    syllable.startTimeMs - (carFile.syllables.getOrNull(syllable.id - 2)?.endTimeMs ?: 0)
                } else {
                    500L
                }
                delay(delayTime.coerceAtLeast(100))
                currentSyllableIndex = syllable.id - 1
                // Воспроизводим звук слога если есть
                val soundResId = context.resources.getIdentifier(
                    "syllable_${carFile.metadata.title.lowercase()}_${syllable.id}",
                    "raw",
                    context.packageName
                )
                if (soundResId != 0) {
                    AudioPlayer.playSFX(soundResId)
                }
            }
            isPlaying = false
            gameMode = GameMode.READ
        }
    }
    
    // Проверка правильности составленного слова
    LaunchedEffect(constructedWord) {
        if (constructedWord == currentWord?.text) {
            score++
            AudioPlayer.playSFX(R.raw.sfx_success)
            
            // Переход к следующему слову или песне
            if (currentWordIndex < carFile.words.size - 1) {
                delay(1000)
                currentWordIndex++
                constructedWord = ""
                currentSyllableIndex = 0
            } else {
                // Песня завершена
                stars = when {
                    mistakes == 0 -> 3
                    mistakes <= 2 -> 2
                    else -> 1
                }
                showLevelComplete = true
                GameState.completeLevel("karaoke", songIndex, stars)
            }
        } else if (constructedWord.length >= (currentWord?.text?.length ?: 0)) {
            // Неправильное слово
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = FairyGold.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = carFile.metadata.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = FairyPurple,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = carFile.metadata.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FairyBlue
                    )
                    Text(
                        text = "Песня $songIndex из 20",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Прогресс по словам в песне
            StageProgressIndicator(
                currentStage = currentWordIndex + 1,
                maxStages = carFile.words.size
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Персонаж Василиса
            CharacterView(
                character = "vasilisa",
                emotion = when {
                    showLevelComplete -> "proud"
                    mistakes > 2 -> "sad"
                    score > carFile.words.size / 2 -> "happy"
                    else -> "teacher"
                },
                message = when (gameMode) {
                    GameMode.PREVIEW -> "Посмотри на слоги: ${carFile.words.joinToString(" ") { it.text }}"
                    GameMode.LISTEN -> "Слушай и смотри на слоги!"
                    GameMode.READ -> "Составь слово: ${currentWord?.text?.length ?: 0} букв"
                    GameMode.SING -> "Спой вместе с Василисой!"
                },
                modifier = Modifier.height(80.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Режимы игры
            GameModeSelector(
                currentMode = gameMode,
                onModeSelected = { mode ->
                    gameMode = mode
                    if (mode == GameMode.LISTEN) {
                        isPlaying = true
                        currentSyllableIndex = 0
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Основная область в зависимости от режима
            when (gameMode) {
                GameMode.PREVIEW -> PreviewMode(carFile)
                GameMode.LISTEN -> ListenMode(
                    carFile = carFile,
                    currentSyllableIndex = currentSyllableIndex,
                    isPlaying = isPlaying,
                    onPlayPause = { isPlaying = !isPlaying }
                )
                GameMode.READ -> ReadMode(
                    carFile = carFile,
                    currentWord = currentWord,
                    constructedWord = constructedWord,
                    currentSyllables = currentSyllables,
                    alphabet = alphabet,
                    onLetterPress = { letter ->
                        if (constructedWord.length < (currentWord?.text?.length ?: 0)) {
                            constructedWord += letter
                            AudioPlayer.playSFX(R.raw.sfx_type)
                        }
                    },
                    onDelete = {
                        if (constructedWord.isNotEmpty()) {
                            constructedWord = constructedWord.dropLast(1)
                        }
                    },
                    onClear = { constructedWord = "" }
                )
                GameMode.SING -> SingMode(
                    carFile = carFile,
                    currentSyllableIndex = currentSyllableIndex
                )
            }
        }
        
        // Окно завершения
        if (showLevelComplete) {
            LevelComplete(
                stars = stars,
                message = "Песня «${carFile.metadata.title}» пройдена!",
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

// ==================== Режимы игры ====================

enum class GameMode {
    PREVIEW,  // Предпросмотр песни
    LISTEN,   // Слушаем и следим за слогами
    READ,     // Составляем слова из букв
    SING      // Поём вместе (свободный режим)
}

@Composable
fun GameModeSelector(
    currentMode: GameMode,
    onModeSelected: (GameMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(
            GameMode.PREVIEW to "👁️ Смотрю",
            GameMode.LISTEN to "👂 Слушаю",
            GameMode.READ to "📖 Читаю",
            GameMode.SING to "🎤 Пою"
        ).forEach { (mode, label) ->
            FilterChip(
                selected = currentMode == mode,
                onClick = { onModeSelected(mode) },
                label = { Text(label, fontSize = 14.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (mode) {
                        GameMode.PREVIEW -> FairyBlue
                        GameMode.LISTEN -> FairyGold
                        GameMode.READ -> FairyGreen
                        GameMode.SING -> FairyPurple
                    }
                )
            )
        }
    }
}

// ==================== Режим ПРЕДПРОСМОТР ====================

@Composable
fun PreviewMode(carFile: CarFile) {
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
                text = "Слова песни:",
                style = MaterialTheme.typography.titleMedium,
                color = FairyPurple,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Отображение всех слов песни
            WrapRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                carFile.words.forEach { word ->
                    Card(
                        modifier = Modifier.padding(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FairyGold.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = word.text,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FairyBlue
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Разбивка по слогам
            Text(
                text = "По слогам:",
                style = MaterialTheme.typography.titleSmall,
                color = FairyGreen
            )
            
            WrapRow {
                carFile.syllables.forEach { syllable ->
                    Text(
                        text = syllable.text,
                        fontSize = 24.sp,
                        fontWeight = if (syllable.emphasis) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (syllable.emphasis) FairyPink else FairyBlue,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

// Вспомогательный компонент для переноса строк
@Composable
fun WrapRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}

// ==================== Режим СЛУШАЮ ====================

@Composable
fun ListenMode(
    carFile: CarFile,
    currentSyllableIndex: Int,
    isPlaying: Boolean,
    onPlayPause: () -> Unit
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
            // Визуализация слогов с анимацией
            Text(
                text = "Слушай и повторяй:",
                style = MaterialTheme.typography.titleMedium,
                color = FairyGold,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Слоги с подсветкой текущего
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                itemsIndexed(carFile.syllables) { index, syllable ->
                    val isCurrent = index == currentSyllableIndex
                    val isPast = index < currentSyllableIndex
                    
                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .then(
                                if (isCurrent) Modifier.scale(1.2f) else Modifier
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isCurrent -> FairyGold.copy(alpha = 0.8f)
                                isPast -> FairyGreen.copy(alpha = 0.5f)
                                else -> Color.Gray.copy(alpha = 0.2f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = syllable.text,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 28.sp,
                            fontWeight = if (syllable.emphasis || isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
                            color = when {
                                isCurrent -> Color.White
                                isPast -> FairyGreen
                                else -> Color.Gray
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка воспроизведения
            Button(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) FairyPink else FairyGreen
                )
            ) {
                Text(
                    text = if (isPlaying) "⏸️" else "▶️",
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isPlaying) "Идёт воспроизведение..." else "Нажми, чтобы прослушать",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

// ==================== Режим ЧИТАЮ ====================

@Composable
fun ReadMode(
    carFile: CarFile,
    currentWord: CarWord?,
    constructedWord: String,
    currentSyllables: List<CarSyllable>,
    alphabet: List<Char>,
    onLetterPress: (Char) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    if (currentWord == null) {
        Text("Нет слов для чтения", color = FairyPink)
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
            // Подсказка
            Text(
                text = "Составь слово:",
                style = MaterialTheme.typography.titleMedium,
                color = FairyPurple,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Слоги текущего слова
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                currentSyllables.forEach { syllable ->
                    Text(
                        text = syllable.text,
                        fontSize = 28.sp,
                        fontWeight = if (syllable.emphasis) FontWeight.ExtraBold else FontWeight.Normal,
                        color = FairyGold,
                        modifier = Modifier.padding(4.dp)
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
                        if (constructedWord == currentWord.text) FairyGreen.copy(alpha = 0.2f)
                        else FairyBlue.copy(alpha = 0.1f)
                    )
                    .border(
                        2.dp,
                        if (constructedWord.isNotEmpty()) FairyGold else Color.Gray,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = constructedWord.ifEmpty {
                        "_ ".repeat(currentWord.text.length).trim()
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (constructedWord == currentWord.text) FairyGreen else FairyBlue,
                    letterSpacing = 4.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Подсказка с изображением
            if (currentWord.hintImage != null || currentWord.hintText != null) {
                Text(
                    text = "Подсказка: ${currentWord.hintText ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Клавиатура с буквами
            Text(
                text = "Выбери буквы:",
                style = MaterialTheme.typography.bodyMedium,
                color = FairyPurple
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alphabet) { letter ->
                    val isUsed = constructedWord.count { it == letter } >= 
                        currentWord.text.count { it == letter }
                    
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                if (isUsed) Color.Gray.copy(alpha = 0.3f)
                                else FairyGold.copy(alpha = 0.2f)
                            )
                            .border(2.dp, FairyGold, CircleShape)
                            .clickable(enabled = !isUsed) {
                                onLetterPress(letter)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter.toString(),
                            fontSize = 24.sp,
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
                Button(
                    onClick = onDelete,
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FairyPink)
                ) {
                    Text("⌫ Удалить", fontSize = 14.sp)
                }
                
                Button(
                    onClick = onClear,
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("🔄 Очистить", fontSize = 14.sp)
                }
            }
        }
    }
}

// ==================== Режим ПОЮ ====================

@Composable
fun SingMode(
    carFile: CarFile,
    currentSyllableIndex: Int
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
            
            // Текст песни с крупными слогами
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Группируем слоги по словам
                val wordGroups = carFile.words.map { word ->
                    word to word.syllableIds.mapNotNull { id ->
                        carFile.syllables.find { it.id == id }
                    }
                }
                
                items(wordGroups) { (word, syllables) ->
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        syllables.forEach { syllable ->
                            val isCurrent = syllable.id - 1 == currentSyllableIndex
                            
                            Text(
                                text = syllable.text,
                                fontSize = if (isCurrent) 36.sp else 28.sp,
                                fontWeight = if (syllable.emphasis) FontWeight.ExtraBold 
                                    else FontWeight.Bold,
                                color = when {
                                    isCurrent -> FairyPink
                                    syllable.emphasis -> FairyGold
                                    else -> FairyBlue
                                },
                                modifier = Modifier
                                    .padding(2.dp)
                                    .then(
                                        if (isCurrent) Modifier.scale(1.1f) else Modifier
                                    )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Кнопка «Прослушать мелодию»
            Button(
                onClick = {
                    // Воспроизведение MIDI или аудио
                    val soundResId = LocalContext.current.resources.getIdentifier(
                        "melody_${carFile.metadata.title.lowercase().replace(" ", "_")}",
                        "raw",
                        LocalContext.current.packageName
                    )
                    if (soundResId != 0) {
                        AudioPlayer.playSFX(soundResId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FairyGold)
            ) {
                Text("🎵 Прослушать мелодию", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

// ==================== Вспомогательные компоненты ====================

/**
 * Отображение прогресса по песням (1-20)
 */
@Composable
fun SongProgressBar(
    currentSong: Int,
    totalSongs: Int = 20
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Песня $currentSong из $totalSongs",
            style = MaterialTheme.typography.titleSmall,
            color = FairyPurple
        )
        
        LinearProgressIndicator(
            progress = { currentSong.toFloat() / totalSongs.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = FairyGold,
            trackColor = FairyGold.copy(alpha = 0.2f)
        )
    }
}
