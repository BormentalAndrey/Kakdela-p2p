package com.vasilisinaazbuka.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

// ==================== Модели данных тамагочи ====================

/**
 * Состояние питомца Кузи
 */
data class KuzyaState(
    val hunger: Float = 100f,        // Сытость (0-100)
    val happiness: Float = 100f,     // Счастье (0-100)
    val energy: Float = 100f,        // Энергия (0-100)
    val health: Float = 100f,        // Здоровье (0-100)
    val cleanliness: Float = 100f,   // Чистота (0-100)
    val age: Int = 0,                // Возраст в днях
    val level: Int = 1,              // Уровень питомца
    val experience: Int = 0,         // Опыт до следующего уровня
    val coins: Int = 100,            // Монеты
    val mood: KuzyaMood = KuzyaMood.HAPPY,
    val isSleeping: Boolean = false,
    val isSick: Boolean = false,
    val lastFedTime: Long = System.currentTimeMillis(),
    val lastPlayedTime: Long = System.currentTimeMillis(),
    val lastCleanedTime: Long = System.currentTimeMillis(),
    val lastSleptTime: Long = System.currentTimeMillis(),
    val achievements: List<String> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val stats: KuzyaStats = KuzyaStats()
)

data class KuzyaStats(
    val totalFoodEaten: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalBathsTaken: Int = 0,
    val totalSleeps: Int = 0,
    val longestStreak: Int = 0,
    val currentStreak: Int = 0
)

enum class KuzyaMood {
    ECSTATIC,   // 😸 В восторге
    HAPPY,      // 😺 Счастлив
    NEUTRAL,    // 😼 Нормально
    HUNGRY,     // 😿 Голоден
    SAD,        // 😾 Грустный
    SICK,       // 🤒 Болеет
    SLEEPY,     // 😴 Сонный
    DIRTY,      // 💩 Грязный
    ANGRY       // 🙀 Злой
}

/**
 * Предметы инвентаря
 */
data class InventoryItem(
    val id: String,
    val name: String,
    val emoji: String,
    val type: ItemType,
    val quantity: Int = 1,
    val effect: Int = 10  // Насколько сильно влияет на характеристику
)

enum class ItemType {
    FOOD,       // Еда
    SNACK,      // Лакомство
    TOY,        // Игрушка
    MEDICINE,   // Лекарство
    SOAP,       // Мыло/шампунь
    DECORATION, // Украшение
    SPECIAL     // Особый предмет
}

// ==================== Игровые предметы ====================

/**
 * Каталог всех доступных предметов в игре
 */
object ItemCatalog {
    val allItems = listOf(
        // Еда
        InventoryItem("apple", "Яблоко", "🍎", ItemType.FOOD, effect = 15),
        InventoryItem("pizza", "Пицца", "🍕", ItemType.FOOD, effect = 25),
        InventoryItem("banana", "Банан", "🍌", ItemType.FOOD, effect = 10),
        InventoryItem("fish", "Рыбка", "🐟", ItemType.FOOD, effect = 30),
        InventoryItem("milk", "Молоко", "🥛", ItemType.FOOD, effect = 12),
        InventoryItem("bread", "Хлеб", "🍞", ItemType.FOOD, effect = 10),
        InventoryItem("cheese", "Сыр", "🧀", ItemType.FOOD, effect = 20),
        InventoryItem("soup", "Суп", "🍜", ItemType.FOOD, effect = 22),
        InventoryItem("porridge", "Каша", "🥣", ItemType.FOOD, effect = 18),
        InventoryItem("cutlet", "Котлета", "🍖", ItemType.FOOD, effect = 28),
        
        // Лакомства
        InventoryItem("cookie", "Печенье", "🍪", ItemType.SNACK, effect = 8),
        InventoryItem("candy", "Конфета", "🍬", ItemType.SNACK, effect = 5),
        InventoryItem("icecream", "Мороженое", "🍦", ItemType.SNACK, effect = 7),
        InventoryItem("cake", "Тортик", "🎂", ItemType.SNACK, effect = 10),
        
        // Игрушки
        InventoryItem("ball", "Мячик", "⚽", ItemType.TOY, effect = 20),
        InventoryItem("mouse", "Мышка", "🐭", ItemType.TOY, effect = 25),
        InventoryItem("feather", "Пёрышко", "🪶", ItemType.TOY, effect = 15),
        InventoryItem("laser", "Лазер", "🔴", ItemType.TOY, effect = 30),
        InventoryItem("puzzle", "Пазл", "🧩", ItemType.TOY, effect = 18),
        
        // Лекарства
        InventoryItem("bandage", "Бинт", "🩹", ItemType.MEDICINE, effect = 30),
        InventoryItem("pill", "Таблетка", "💊", ItemType.MEDICINE, effect = 40),
        InventoryItem("syrup", "Сироп", "🥄", ItemType.MEDICINE, effect = 25),
        
        // Гигиена
        InventoryItem("soap", "Мыло", "🧼", ItemType.SOAP, effect = 25),
        InventoryItem("shampoo", "Шампунь", "🧴", ItemType.SOAP, effect = 20),
        InventoryItem("towel", "Полотенце", "🪥", ItemType.SOAP, effect = 15),
        
        // Украшения
        InventoryItem("bow", "Бантик", "🎀", ItemType.DECORATION),
        InventoryItem("hat", "Шляпа", "🎩", ItemType.DECORATION),
        InventoryItem("scarf", "Шарфик", "🧣", ItemType.DECORATION),
        
        // Особые предметы
        InventoryItem("star_treat", "Звёздный корм", "⭐", ItemType.SPECIAL, effect = 50),
        InventoryItem("magic_potion", "Волшебное зелье", "🧪", ItemType.SPECIAL, effect = 75)
    )
    
    fun getItem(id: String): InventoryItem? = allItems.find { it.id == id }
    
    fun getItemsByType(type: ItemType): List<InventoryItem> = allItems.filter { it.type == type }
}

// ==================== Сохранение состояния тамагочи ====================

/**
 * Менеджер сохранения состояния Кузи
 */
object KuzyaSaveManager {
    private const val PREFS_NAME = "kuzya_tamagotchi"
    private const val KEY_KUZYA_STATE = "kuzya_state"
    
    fun saveState(context: Context, state: KuzyaState) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = com.google.gson.Gson().toJson(state)
        prefs.edit().putString(KEY_KUZYA_STATE, json).apply()
    }
    
    fun loadState(context: Context): KuzyaState? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_KUZYA_STATE, null) ?: return null
        return try {
            com.google.gson.Gson().fromJson(json, KuzyaState::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    fun resetState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

// ==================== Основной экран игры ====================

/**
 * Игра «Накорми Кузю» — полноценный тамагочи
 * 
 * @param stage Уровень сложности/этап (1-5)
 * @param onNextStage Переход к следующему этапу
 * @param onGameComplete Завершение игры
 * @param onBack Навигация назад
 */
@Composable
fun FeedKuzyaScreen(
    stage: Int = 1,
    onNextStage: () -> Unit = {},
    onGameComplete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Загружаем или создаём состояние Кузи
    var kuzyaState by remember {
        mutableStateOf(
            KuzyaSaveManager.loadState(context) ?: KuzyaState()
        )
    }
    
    // Текущая вкладка интерфейса
    var currentTab by remember { mutableIntStateOf(0) }
    
    // Состояния для мини-игр
    var showMiniGame by remember { mutableStateOf(false) }
    var miniGameType by remember { mutableStateOf(MiniGameType.FEEDING) }
    var showShop by remember { mutableStateOf(false) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var showAchievement by remember { mutableStateOf<String?>(null) }
    
    // Анимация питомца
    val petScale by animateFloatAsState(
        targetValue = when (kuzyaState.mood) {
            KuzyaMood.ECSTATIC -> 1.15f
            KuzyaMood.HAPPY -> 1.1f
            KuzyaMood.SAD, KuzyaMood.SICK -> 0.9f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.5f),
        label = "petScale"
    )
    
    val petRotation by animateFloatAsState(
        targetValue = when (kuzyaState.mood) {
            KuzyaMood.ECSTATIC -> 5f
            KuzyaMood.HAPPY -> 3f
            KuzyaMood.ANGRY -> -5f
            else -> 0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "petRotation"
    )
    
    // Обновление состояния питомца со временем
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Обновляем каждые 5 секунд
            
            kuzyaState = kuzyaState.copy(
                hunger = max(0f, kuzyaState.hunger - 0.5f),
                happiness = max(0f, kuzyaState.happiness - 0.3f),
                energy = if (kuzyaState.isSleeping) 
                    min(100f, kuzyaState.energy + 1f) 
                else 
                    max(0f, kuzyaState.energy - 0.2f),
                cleanliness = max(0f, kuzyaState.cleanliness - 0.1f),
                health = if (kuzyaState.hunger < 10 || kuzyaState.cleanliness < 10) 
                    max(0f, kuzyaState.health - 0.5f) 
                else 
                    min(100f, kuzyaState.health + 0.1f),
                mood = calculateMood(kuzyaState)
            )
            
            // Автосохранение
            KuzyaSaveManager.saveState(context, kuzyaState)
        }
    }
    
    // Проверка достижений
    LaunchedEffect(kuzyaState.stats.totalFoodEaten) {
        when {
            kuzyaState.stats.totalFoodEaten >= 100 && 
                "gourmet" !in kuzyaState.achievements -> {
                showAchievement = "Гурман"
                kuzyaState = kuzyaState.copy(
                    achievements = kuzyaState.achievements + "gourmet",
                    coins = kuzyaState.coins + 50
                )
            }
            kuzyaState.stats.totalGamesPlayed >= 50 && 
                "player" !in kuzyaState.achievements -> {
                showAchievement = "Игрок"
                kuzyaState = kuzyaState.copy(
                    achievements = kuzyaState.achievements + "player",
                    coins = kuzyaState.coins + 50
                )
            }
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
            // Заголовок с информацией
            KuzyaHeader(
                kuzyaState = kuzyaState,
                stage = stage,
                coins = kuzyaState.coins
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Питомец (кликабельный)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        when (kuzyaState.mood) {
                            KuzyaMood.ECSTATIC -> FairyGold.copy(alpha = 0.3f)
                            KuzyaMood.HAPPY -> FairyGreen.copy(alpha = 0.2f)
                            KuzyaMood.SAD, KuzyaMood.SICK -> Color.Gray.copy(alpha = 0.2f)
                            KuzyaMood.HUNGRY -> FairyPink.copy(alpha = 0.2f)
                            KuzyaMood.DIRTY -> Color(0xFF8B4513).copy(alpha = 0.2f)
                            else -> FairyBlue.copy(alpha = 0.1f)
                        }
                    )
                    .border(3.dp, FairyGold, RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                // Погладить Кузю
                                kuzyaState = kuzyaState.copy(
                                    happiness = min(100f, kuzyaState.happiness + 5f),
                                    mood = if (kuzyaState.mood == KuzyaMood.SAD) 
                                        KuzyaMood.NEUTRAL else kuzyaState.mood
                                )
                                AudioPlayer.playSFX(R.raw.sfx_purr)
                            },
                            onDoubleTap = {
                                // Покормить Кузю
                                showMiniGame = true
                                miniGameType = MiniGameType.FEEDING
                            },
                            onLongPress = {
                                // Уложить спать / разбудить
                                kuzyaState = kuzyaState.copy(
                                    isSleeping = !kuzyaState.isSleeping
                                )
                                AudioPlayer.playSFX(R.raw.sfx_sleep)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Анимированный Кузя
                AnimatedKuzya(
                    state = kuzyaState,
                    scale = petScale,
                    rotation = petRotation
                )
                
                // Zzz анимация когда спит
                if (kuzyaState.isSleeping) {
                    SleepingAnimation()
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Статус-бары
            StatusBars(kuzyaState = kuzyaState)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Вкладки действий
            TabRow(
                selectedTabIndex = currentTab,
                containerColor = Color.White,
                contentColor = FairyPurple
            ) {
                listOf("🍽️ Еда", "🎮 Игры", "🛁 Уход", "🛒 Магазин").forEachIndexed { index, title ->
                    Tab(
                        selected = currentTab == index,
                        onClick = { 
                            currentTab = index
                            if (index == 3) showShop = true
                        },
                        text = { Text(title, fontSize = 14.sp) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Содержимое вкладки
            when (currentTab) {
                0 -> FoodTab(
                    onFeedItem = { item ->
                        kuzyaState = feedKuzya(kuzyaState, item)
                        AudioPlayer.playSFX(R.raw.sfx_eat)
                    },
                    onStartMiniGame = {
                        showMiniGame = true
                        miniGameType = MiniGameType.FEEDING
                    }
                )
                1 -> GamesTab(
                    onPlayGame = { gameType ->
                        showMiniGame = true
                        miniGameType = gameType
                    },
                    kuzyaState = kuzyaState
                )
                2 -> CareTab(
                    onBath = {
                        kuzyaState = kuzyaState.copy(
                            cleanliness = 100f,
                            happiness = min(100f, kuzyaState.happiness + 10f),
                            stats = kuzyaState.stats.copy(
                                totalBathsTaken = kuzyaState.stats.totalBathsTaken + 1
                            )
                        )
                        AudioPlayer.playSFX(R.raw.sfx_water)
                    },
                    onHeal = {
                        if (kuzyaState.coins >= 20) {
                            kuzyaState = kuzyaState.copy(
                                health = 100f,
                                isSick = false,
                                coins = kuzyaState.coins - 20
                            )
                            AudioPlayer.playSFX(R.raw.sfx_heal)
                        }
                    },
                    kuzyaState = kuzyaState
                )
            }
        }
        
        // Мини-игры
        if (showMiniGame) {
            MiniGameDialog(
                gameType = miniGameType,
                kuzyaState = kuzyaState,
                onComplete = { reward ->
                    kuzyaState = when (miniGameType) {
                        MiniGameType.FEEDING -> kuzyaState.copy(
                            hunger = min(100f, kuzyaState.hunger + reward),
                            happiness = min(100f, kuzyaState.happiness + 5f),
                            stats = kuzyaState.stats.copy(
                                totalFoodEaten = kuzyaState.stats.totalFoodEaten + 1
                            ),
                            coins = kuzyaState.coins + Random.nextInt(5, 15)
                        )
                        MiniGameType.FISHING -> kuzyaState.copy(
                            happiness = min(100f, kuzyaState.happiness + 15f),
                            coins = kuzyaState.coins + reward.toInt()
                        )
                        MiniGameType.CATCH_MOUSE -> kuzyaState.copy(
                            happiness = min(100f, kuzyaState.happiness + 20f),
                            stats = kuzyaState.stats.copy(
                                totalGamesPlayed = kuzyaState.stats.totalGamesPlayed + 1
                            ),
                            coins = kuzyaState.coins + reward.toInt()
                        )
                        MiniGameType.PUZZLE -> kuzyaState.copy(
                            happiness = min(100f, kuzyaState.happiness + 10f),
                            experience = kuzyaState.experience + 25
                        )
                    }
                    showMiniGame = false
                    KuzyaSaveManager.saveState(context, kuzyaState)
                },
                onDismiss = { showMiniGame = false }
            )
        }
        
        // Магазин
        if (showShop) {
            ShopDialog(
                kuzyaState = kuzyaState,
                onBuy = { item ->
                    if (kuzyaState.coins >= itemPrice(item)) {
                        kuzyaState = kuzyaState.copy(
                            coins = kuzyaState.coins - itemPrice(item),
                            inventory = addToInventory(kuzyaState.inventory, item)
                        )
                        AudioPlayer.playSFX(R.raw.sfx_coin)
                    }
                },
                onDismiss = { showShop = false }
            )
        }
        
        // Уведомление о достижении
        AnimatedVisibility(
            visible = showAchievement != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            AchievementNotification(
                title = showAchievement ?: "",
                onDismiss = { showAchievement = null }
            )
        }
        
        // Завершение этапа
        if (showLevelComplete) {
            LevelComplete(
                stars = calculateStars(kuzyaState),
                message = "Кузя счастлив и здоров!\nУровень: ${kuzyaState.level}",
                onNext = {
                    if (stage < 5) {
                        onNextStage()
                    } else {
                        onGameComplete()
                    }
                }
            )
        }
    }
}

// ==================== Компоненты интерфейса ====================

@Composable
private fun KuzyaHeader(kuzyaState: KuzyaState, stage: Int, coins: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Кузя ❤️",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FairyPink
                )
                Text(
                    text = "Уровень ${kuzyaState.level} • День ${kuzyaState.age}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⭐", fontSize = 20.sp)
                Text(
                    text = "$coins",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FairyGold
                )
            }
        }
    }
}

@Composable
private fun AnimatedKuzya(state: KuzyaState, scale: Float, rotation: Float) {
    val emoji = when {
        state.isSleeping -> "😴"
        state.mood == KuzyaMood.ECSTATIC -> "😸"
        state.mood == KuzyaMood.HAPPY -> "😺"
        state.mood == KuzyaMood.HUNGRY -> "😿"
        state.mood == KuzyaMood.SAD -> "😾"
        state.mood == KuzyaMood.SICK -> "🤒"
        state.mood == KuzyaMood.SLEEPY -> "😴"
        state.mood == KuzyaMood.DIRTY -> "😼"
        state.mood == KuzyaMood.ANGRY -> "🙀"
        else -> "😼"
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 100.sp,
            modifier = Modifier
                .scale(scale)
                .rotate(rotation)
        )
        
        Text(
            text = when (state.mood) {
                KuzyaMood.ECSTATIC -> "Мур-мур-мур! 😻"
                KuzyaMood.HAPPY -> "Мяу! Я счастлив!"
                KuzyaMood.HUNGRY -> "Мяу... Я голоден..."
                KuzyaMood.SAD -> "Мяу... Мне грустно..."
                KuzyaMood.SICK -> "Мяу... Я болею..."
                KuzyaMood.SLEEPY -> "Хочу спать..."
                KuzyaMood.DIRTY -> "Мяу! Я грязный!"
                KuzyaMood.ANGRY -> "МЯУ! Я злой!"
                else -> "Мяу!"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = FairyPurple,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SleepingAnimation() {
    var offset by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            animateFloatAsState(
                targetValue = -20f,
                animationSpec = tween(2000)
            )
            delay(2000)
        }
    }
    
    Text(
        text = "Z",
        fontSize = 24.sp,
        color = FairyBlue,
        modifier = Modifier.offset(y = offset.dp)
    )
}

@Composable
private fun StatusBars(kuzyaState: KuzyaState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            StatusBar("🍽️ Сытость", kuzyaState.hunger, if (kuzyaState.hunger < 30) FairyPink else FairyGreen)
            StatusBar("😊 Счастье", kuzyaState.happiness, FairyGold)
            StatusBar("⚡ Энергия", kuzyaState.energy, FairyBlue)
            StatusBar("❤️ Здоровье", kuzyaState.health, if (kuzyaState.health < 30) Color.Red else FairyGreen)
            StatusBar("🧼 Чистота", kuzyaState.cleanliness, FairyPurple)
        }
    }
}

@Composable
private fun StatusBar(label: String, value: Float, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(100.dp)
        )
        
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
        
        Text(
            text = "${value.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}

// ==================== Вкладки ====================

@Composable
private fun FoodTab(
    onFeedItem: (InventoryItem) -> Unit,
    onStartMiniGame: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Покорми Кузю",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FairyPurple
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Выбери еду или сыграй в мини-игру",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(180.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ItemCatalog.getItemsByType(ItemType.FOOD).take(8)) { item ->
                    FoodItemButton(item = item, onClick = { onFeedItem(item) })
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onStartMiniGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FairyGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🎮 Мини-игра: Накорми Кузю", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun FoodItemButton(item: InventoryItem, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(FairyGold.copy(alpha = 0.2f))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(text = item.emoji, fontSize = 36.sp)
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GamesTab(
    onPlayGame: (MiniGameType) -> Unit,
    kuzyaState: KuzyaState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Игры с Кузей",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FairyPurple
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            listOf(
                Triple(MiniGameType.FISHING, "🎣 Рыбалка", "Поймай рыбку для Кузи"),
                Triple(MiniGameType.CATCH_MOUSE, "🐭 Поймай мышку", "Помоги Кузе поймать мышей"),
                Triple(MiniGameType.PUZZLE, "🧩 Пазл", "Собери картинку с Кузей")
            ).forEach { (type, title, desc) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onPlayGame(type) },
                    colors = CardDefaults.cardColors(
                        containerColor = FairyBlue.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, fontSize = 20.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun CareTab(
    onBath: () -> Unit,
    onHeal: () -> Unit,
    kuzyaState: KuzyaState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Уход за Кузей",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FairyPurple
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Кнопка купания
                Button(
                    onClick = onBath,
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FairyBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🛁", fontSize = 28.sp)
                        Text(text = "Купать", fontSize = 14.sp, color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Кнопка лечения
                Button(
                    onClick = onHeal,
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FairyPink),
                    shape = RoundedCornerShape(16.dp),
                    enabled = kuzyaState.health < 100 && kuzyaState.coins >= 20
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "💊", fontSize = 28.sp)
                        Text(text = "Лечить (20⭐)", fontSize = 12.sp, color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Кнопка сна
                Button(
                    onClick = {
                        // Уложить спать
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FairyPurple),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "😴", fontSize = 28.sp)
                        Text(text = "Спать", fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// ==================== Мини-игры ====================

enum class MiniGameType {
    FEEDING,        // Накормить Кузю
    FISHING,        // Рыбалка
    CATCH_MOUSE,    // Поймать мышку
    PUZZLE          // Пазл
}

@Composable
private fun MiniGameDialog(
    gameType: MiniGameType,
    kuzyaState: KuzyaState,
    onComplete: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (gameType) {
                    MiniGameType.FEEDING -> "Накорми Кузю"
                    MiniGameType.FISHING -> "Рыбалка"
                    MiniGameType.CATCH_MOUSE -> "Поймай мышку"
                    MiniGameType.PUZZLE -> "Пазл"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            when (gameType) {
                MiniGameType.FEEDING -> FeedingMiniGame(
                    kuzyaState = kuzyaState,
                    onComplete = { onComplete(it) }
                )
                MiniGameType.FISHING -> FishingMiniGame(
                    onComplete = { onComplete(it) }
                )
                MiniGameType.CATCH_MOUSE -> CatchMouseMiniGame(
                    onComplete = { onComplete(it) }
                )
                MiniGameType.PUZZLE -> PuzzleMiniGame(
                    onComplete = { onComplete(it) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun FeedingMiniGame(
    kuzyaState: KuzyaState,
    onComplete: (Float) -> Unit
) {
    var foodCount by remember { mutableIntStateOf(0) }
    val targetFood = remember { Random.nextInt(3, 8) }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Дай Кузе $targetFood кусочков еды!",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Собрано: $foodCount из $targetFood",
            style = MaterialTheme.typography.bodyLarge,
            color = if (foodCount >= targetFood) FairyGreen else FairyGold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Еда на экране
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(200.dp)
        ) {
            items(List(maxOf(targetFood, foodCount + 2)) { it }) { index ->
                if (index < foodCount) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FairyGreen.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✅", fontSize = 32.sp)
                    }
                } else {
                    val foods = listOf("🍎", "🍕", "🍌", "🐟", "🥛", "🧀")
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FairyGold.copy(alpha = 0.3f))
                            .clickable {
                                foodCount++
                                AudioPlayer.playSFX(R.raw.sfx_eat)
                                if (foodCount >= targetFood) {
                                    onComplete(30f)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = foods[index % foods.size],
                            fontSize = 40.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FishingMiniGame(onComplete: (Float) -> Unit) {
    var fishPosition by remember { mutableFloatStateOf(Random.nextFloat()) }
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(15) }
    var isGameOver by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        while (timeLeft > 0 && !isGameOver) {
            delay(1000)
            timeLeft--
            fishPosition = Random.nextFloat()
        }
        if (!isGameOver) {
            isGameOver = true
            onComplete(score.toFloat() * 5f)
        }
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Поймай рыбку! Время: $timeLeft сек")
        Text(text = "Счёт: $score", color = FairyGold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(FairyBlue.copy(alpha = 0.2f))
                .clickable {
                    if (fishPosition in 0.4f..0.6f) {
                        score++
                        fishPosition = Random.nextFloat()
                        AudioPlayer.playSFX(R.raw.sfx_splash)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🐟",
                fontSize = 48.sp,
                modifier = Modifier.offset(
                    x = ((fishPosition - 0.5f) * 200).dp
                )
            )
        }
    }
}

@Composable
private fun CatchMouseMiniGame(onComplete: (Float) -> Unit) {
    var mouseVisible by remember { mutableStateOf(true) }
    var mousePosition by remember { mutableStateOf(Pair(Random.nextFloat(), Random.nextFloat())) }
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(20) }
    
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
            if (Random.nextFloat() > 0.5f) {
                mouseVisible = true
                mousePosition = Pair(Random.nextFloat(), Random.nextFloat())
            } else {
                mouseVisible = false
            }
        }
        onComplete(score.toFloat() * 3f)
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Поймай мышек! Время: $timeLeft сек")
        Text(text = "Поймано: $score", color = FairyGold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Gray.copy(alpha = 0.1f))
        ) {
            if (mouseVisible) {
                Text(
                    text = "🐭",
                    fontSize = 40.sp,
                    modifier = Modifier
                        .offset(
                            x = (mousePosition.first * 250).dp,
                            y = (mousePosition.second * 150).dp
                        )
                        .clickable {
                            score++
                            mouseVisible = false
                            AudioPlayer.playSFX(R.raw.sfx_squeak)
                        }
                )
            }
            
            // Кузя-охотник
            Text(
                text = "🐱",
                fontSize = 50.sp,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun PuzzleMiniGame(onComplete: (Float) -> Unit) {
    var pieces by remember {
        mutableStateOf(List(4) { it }.shuffled())
    }
    var placedPieces by remember { mutableIntStateOf(0) }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Собери пазл с Кузей!")
        Text(text = "Собрано: $placedPieces из 4")
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            items(4) { index ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (pieces[index] == index) FairyGreen.copy(alpha = 0.5f)
                            else FairyGold.copy(alpha = 0.3f)
                        )
                        .clickable {
                            if (pieces[index] == index) {
                                placedPieces++
                                if (placedPieces >= 4) {
                                    onComplete(20f)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (pieces[index]) {
                            0 -> "😺"
                            1 -> "🐟"
                            2 -> "🎀"
                            else -> "⭐"
                        },
                        fontSize = 40.sp
                    )
                }
            }
        }
    }
}

// ==================== Магазин ====================

@Composable
private fun ShopDialog(
    kuzyaState: KuzyaState,
    onBuy: (InventoryItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🛒 Магазин", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text("⭐${kuzyaState.coins}", color = FairyGold)
            }
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ItemCatalog.allItems.take(15)) { item ->
                    ShopItemCard(
                        item = item,
                        canBuy = kuzyaState.coins >= itemPrice(item),
                        onBuy = { onBuy(item) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun ShopItemCard(
    item: InventoryItem,
    canBuy: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clickable(enabled = canBuy, onClick = onBuy),
        colors = CardDefaults.cardColors(
            containerColor = if (canBuy) Color.White else Color.Gray.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = item.emoji, fontSize = 32.sp)
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "⭐${itemPrice(item)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (canBuy) FairyGold else Color.Gray
            )
        }
    }
}

@Composable
private fun AchievementNotification(
    title: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = FairyGold),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🏆", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Достижение!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            IconButton(onClick = onDismiss) {
                Text(text = "✕", color = Color.White)
            }
        }
    }
}

// ==================== Вспомогательные функции ====================

private fun calculateMood(state: KuzyaState): KuzyaMood {
    return when {
        state.isSleeping -> KuzyaMood.SLEEPY
        state.isSick || state.health < 20 -> KuzyaMood.SICK
        state.hunger < 10 -> KuzyaMood.HUNGRY
        state.cleanliness < 15 -> KuzyaMood.DIRTY
        state.happiness < 10 -> KuzyaMood.SAD
        state.energy < 10 -> KuzyaMood.SLEEPY
        state.happiness > 80 && state.hunger > 80 -> KuzyaMood.ECSTATIC
        state.happiness > 60 && state.hunger > 50 -> KuzyaMood.HAPPY
        else -> KuzyaMood.NEUTRAL
    }
}

private fun feedKuzya(state: KuzyaState, item: InventoryItem): KuzyaState {
    return state.copy(
        hunger = min(100f, state.hunger + item.effect),
        happiness = min(100f, state.happiness + item.effect / 2),
        stats = state.stats.copy(totalFoodEaten = state.stats.totalFoodEaten + 1),
        mood = calculateMood(state.copy(hunger = min(100f, state.hunger + item.effect)))
    )
}

private fun addToInventory(inventory: List<InventoryItem>, item: InventoryItem): List<InventoryItem> {
    val existing = inventory.find { it.id == item.id }
    return if (existing != null) {
        inventory.map {
            if (it.id == item.id) it.copy(quantity = it.quantity + 1)
            else it
        }
    } else {
        inventory + item.copy(quantity = 1)
    }
}

private fun itemPrice(item: InventoryItem): Int {
    return when (item.type) {
        ItemType.FOOD -> 10
        ItemType.SNACK -> 15
        ItemType.TOY -> 25
        ItemType.MEDICINE -> 30
        ItemType.SOAP -> 20
        ItemType.DECORATION -> 35
        ItemType.SPECIAL -> 50
    }
}

private fun calculateStars(state: KuzyaState): Int {
    val avg = (state.hunger + state.happiness + state.health + state.cleanliness + state.energy) / 5
    return when {
        avg > 80 -> 3
        avg > 50 -> 2
        else -> 1
    }
}
