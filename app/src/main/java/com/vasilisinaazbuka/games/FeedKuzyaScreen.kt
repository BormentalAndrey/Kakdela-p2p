package com.vasilisinaazbuka.games

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

// ==================== Модели данных тамагочи ====================

data class KuzyaState(
    val hunger: Float = 100f, val happiness: Float = 100f, val energy: Float = 100f,
    val health: Float = 100f, val cleanliness: Float = 100f, val age: Int = 0,
    val level: Int = 1, val experience: Int = 0, val coins: Int = 100,
    val mood: KuzyaMood = KuzyaMood.HAPPY, val isSleeping: Boolean = false,
    val isSick: Boolean = false, val lastFedTime: Long = System.currentTimeMillis(),
    val lastPlayedTime: Long = System.currentTimeMillis(), val lastCleanedTime: Long = System.currentTimeMillis(),
    val lastSleptTime: Long = System.currentTimeMillis(), val achievements: List<String> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(), val stats: KuzyaStats = KuzyaStats()
)

data class KuzyaStats(
    val totalFoodEaten: Int = 0, val totalGamesPlayed: Int = 0,
    val totalBathsTaken: Int = 0, val totalSleeps: Int = 0,
    val longestStreak: Int = 0, val currentStreak: Int = 0
)

enum class KuzyaMood { ECSTATIC, HAPPY, NEUTRAL, HUNGRY, SAD, SICK, SLEEPY, DIRTY, ANGRY }

data class InventoryItem(
    val id: String, val name: String, val imageRes: Int,
    val type: ItemType, val quantity: Int = 1, val effect: Int = 10
)

enum class ItemType { FOOD, SNACK, TOY, MEDICINE, SOAP, DECORATION, SPECIAL }

// ==================== Каталог предметов ====================

object ItemCatalog {
    val allItems = listOf(
        InventoryItem("apple", "Яблоко", R.drawable.item_apple, ItemType.FOOD, effect = 15),
        InventoryItem("pizza", "Пицца", R.drawable.item_pizza, ItemType.FOOD, effect = 25),
        InventoryItem("banana", "Банан", R.drawable.item_banana, ItemType.FOOD, effect = 10),
        InventoryItem("fish", "Рыбка", R.drawable.item_fish, ItemType.FOOD, effect = 30),
        InventoryItem("milk", "Молоко", R.drawable.item_milk, ItemType.FOOD, effect = 12),
        InventoryItem("bread", "Хлеб", R.drawable.item_apple, ItemType.FOOD, effect = 10),
        InventoryItem("cheese", "Сыр", R.drawable.item_cheese, ItemType.FOOD, effect = 20),
        InventoryItem("soup", "Суп", R.drawable.item_apple, ItemType.FOOD, effect = 22),
        InventoryItem("porridge", "Каша", R.drawable.item_apple, ItemType.FOOD, effect = 18),
        InventoryItem("cutlet", "Котлета", R.drawable.item_apple, ItemType.FOOD, effect = 28),
        InventoryItem("cookie", "Печенье", R.drawable.item_cookie, ItemType.SNACK, effect = 8),
        InventoryItem("candy", "Конфета", R.drawable.item_apple, ItemType.SNACK, effect = 5),
        InventoryItem("icecream", "Мороженое", R.drawable.item_apple, ItemType.SNACK, effect = 7),
        InventoryItem("cake", "Тортик", R.drawable.item_apple, ItemType.SNACK, effect = 10),
        InventoryItem("ball", "Мячик", R.drawable.item_apple, ItemType.TOY, effect = 20),
        InventoryItem("mouse", "Мышка", R.drawable.item_apple, ItemType.TOY, effect = 25),
        InventoryItem("feather", "Пёрышко", R.drawable.item_apple, ItemType.TOY, effect = 15),
        InventoryItem("laser", "Лазер", R.drawable.item_apple, ItemType.TOY, effect = 30),
        InventoryItem("puzzle", "Пазл", R.drawable.item_apple, ItemType.TOY, effect = 18),
        InventoryItem("bandage", "Бинт", R.drawable.item_apple, ItemType.MEDICINE, effect = 30),
        InventoryItem("pill", "Таблетка", R.drawable.item_apple, ItemType.MEDICINE, effect = 40),
        InventoryItem("syrup", "Сироп", R.drawable.item_apple, ItemType.MEDICINE, effect = 25),
        InventoryItem("soap", "Мыло", R.drawable.item_apple, ItemType.SOAP, effect = 25),
        InventoryItem("shampoo", "Шампунь", R.drawable.item_apple, ItemType.SOAP, effect = 20),
        InventoryItem("towel", "Полотенце", R.drawable.item_apple, ItemType.SOAP, effect = 15),
        InventoryItem("bow", "Бантик", R.drawable.item_apple, ItemType.DECORATION),
        InventoryItem("hat", "Шляпа", R.drawable.item_apple, ItemType.DECORATION),
        InventoryItem("scarf", "Шарфик", R.drawable.item_apple, ItemType.DECORATION),
        InventoryItem("star_treat", "Звёздный корм", R.drawable.item_apple, ItemType.SPECIAL, effect = 50),
        InventoryItem("magic_potion", "Волшебное зелье", R.drawable.item_apple, ItemType.SPECIAL, effect = 75)
    )

    fun getItem(id: String) = allItems.find { it.id == id }
    fun getItemsByType(type: ItemType) = allItems.filter { it.type == type }
}

// ==================== Сохранение состояния ====================

object KuzyaSaveManager {
    private const val PREFS_NAME = "kuzya_tamagotchi"
    fun saveState(context: Context, state: KuzyaState) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("state", com.google.gson.Gson().toJson(state)).apply()
    }
    fun loadState(context: Context): KuzyaState? = try {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("state", null)
        json?.let { com.google.gson.Gson().fromJson(it, KuzyaState::class.java) }
    } catch (e: Exception) { null }
}

// ==================== Основной экран игры ====================

@Composable
fun FeedKuzyaScreen(stage: Int = 1, onNextStage: () -> Unit = {}, onGameComplete: () -> Unit = {}, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var kuzyaState by remember { mutableStateOf(KuzyaSaveManager.loadState(context) ?: KuzyaState()) }
    var currentTab by remember { mutableIntStateOf(0) }
    var showMiniGame by remember { mutableStateOf(false) }
    var miniGameType by remember { mutableStateOf(MiniGameType.FEEDING) }
    var showShop by remember { mutableStateOf(false) }
    var showLevelComplete by remember { mutableStateOf(false) }
    var showAchievement by remember { mutableStateOf<String?>(null) }

    val petScale by animateFloatAsState(
        targetValue = when (kuzyaState.mood) { KuzyaMood.ECSTATIC -> 1.15f; KuzyaMood.HAPPY -> 1.1f; KuzyaMood.SAD, KuzyaMood.SICK -> 0.9f; else -> 1f },
        animationSpec = spring(dampingRatio = 0.5f), label = "petScale"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            kuzyaState = kuzyaState.copy(
                hunger = max(0f, kuzyaState.hunger - 0.5f), happiness = max(0f, kuzyaState.happiness - 0.3f),
                energy = if (kuzyaState.isSleeping) min(100f, kuzyaState.energy + 1f) else max(0f, kuzyaState.energy - 0.2f),
                cleanliness = max(0f, kuzyaState.cleanliness - 0.1f),
                health = if (kuzyaState.hunger < 10 || kuzyaState.cleanliness < 10) max(0f, kuzyaState.health - 0.5f) else min(100f, kuzyaState.health + 0.1f),
                mood = calculateMood(kuzyaState)
            )
            KuzyaSaveManager.saveState(context, kuzyaState)
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Фоновое изображение
        Image(painterResource(R.drawable.bg_level_feed), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)
        
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            KuzyaHeader(kuzyaState, stage, kuzyaState.coins)
            Spacer(Modifier.height(12.dp))

            Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp))
                .background(when (kuzyaState.mood) { KuzyaMood.ECSTATIC -> FairyGold.copy(alpha = 0.3f); KuzyaMood.HAPPY -> FairyGreen.copy(alpha = 0.2f); KuzyaMood.SAD, KuzyaMood.SICK -> Color.Gray.copy(alpha = 0.2f); KuzyaMood.HUNGRY -> FairyPink.copy(alpha = 0.2f); KuzyaMood.DIRTY -> Color(0xFF8B4513).copy(alpha = 0.2f); else -> FairyBlue.copy(alpha = 0.1f) })
                .border(3.dp, FairyGold, RoundedCornerShape(24.dp))
                .pointerInput(Unit) { detectTapGestures(
                    onTap = { kuzyaState = kuzyaState.copy(happiness = min(100f, kuzyaState.happiness + 5f), mood = if (kuzyaState.mood == KuzyaMood.SAD) KuzyaMood.NEUTRAL else kuzyaState.mood); AudioPlayer.playSFX(R.raw.sfx_purr) },
                    onDoubleTap = { showMiniGame = true; miniGameType = MiniGameType.FEEDING },
                    onLongPress = { kuzyaState = kuzyaState.copy(isSleeping = !kuzyaState.isSleeping); AudioPlayer.playSFX(R.raw.sfx_sleep) }
                ) },
                contentAlignment = Alignment.Center
            ) {
                AnimatedKuzya(kuzyaState, petScale)
                if (kuzyaState.isSleeping) SleepingAnimation()
            }

            Spacer(Modifier.height(12.dp)); StatusBars(kuzyaState); Spacer(Modifier.height(12.dp))

            TabRow(selectedTabIndex = currentTab, containerColor = Color.White, contentColor = FairyPurple) {
                listOf("🍽️ Еда", "🎮 Игры", "🛁 Уход", "🛒 Магазин").forEachIndexed { i, t ->
                    Tab(selected = currentTab == i, onClick = { currentTab = i; if (i == 3) showShop = true }, text = { Text(t, fontSize = 14.sp) })
                }
            }

            Spacer(Modifier.height(8.dp))

            when (currentTab) {
                0 -> FoodTab({ item -> kuzyaState = feedKuzya(kuzyaState, item); AudioPlayer.playSFX(R.raw.sfx_eat) }, { showMiniGame = true; miniGameType = MiniGameType.FEEDING })
                1 -> GamesTab({ gameType -> showMiniGame = true; miniGameType = gameType }, kuzyaState)
                2 -> CareTab({
                    kuzyaState = kuzyaState.copy(cleanliness = 100f, happiness = min(100f, kuzyaState.happiness + 10f), stats = kuzyaState.stats.copy(totalBathsTaken = kuzyaState.stats.totalBathsTaken + 1)); AudioPlayer.playSFX(R.raw.sfx_water)
                }, {
                    if (kuzyaState.coins >= 20) { kuzyaState = kuzyaState.copy(health = 100f, isSick = false, coins = kuzyaState.coins - 20); AudioPlayer.playSFX(R.raw.sfx_heal) }
                }, kuzyaState)
            }
        }

        if (showMiniGame) MiniGameDialog(miniGameType, kuzyaState, { reward ->
            kuzyaState = when (miniGameType) {
                MiniGameType.FEEDING -> kuzyaState.copy(hunger = min(100f, kuzyaState.hunger + reward), happiness = min(100f, kuzyaState.happiness + 5f), stats = kuzyaState.stats.copy(totalFoodEaten = kuzyaState.stats.totalFoodEaten + 1), coins = kuzyaState.coins + Random.nextInt(5, 15))
                MiniGameType.FISHING -> kuzyaState.copy(happiness = min(100f, kuzyaState.happiness + 15f), coins = kuzyaState.coins + reward.toInt())
                MiniGameType.CATCH_MOUSE -> kuzyaState.copy(happiness = min(100f, kuzyaState.happiness + 20f), stats = kuzyaState.stats.copy(totalGamesPlayed = kuzyaState.stats.totalGamesPlayed + 1), coins = kuzyaState.coins + reward.toInt())
                MiniGameType.PUZZLE -> kuzyaState.copy(happiness = min(100f, kuzyaState.happiness + 10f), experience = kuzyaState.experience + 25)
            }
            showMiniGame = false; KuzyaSaveManager.saveState(context, kuzyaState)
        }, { showMiniGame = false })

        if (showShop) ShopDialog(kuzyaState, { item ->
            if (kuzyaState.coins >= itemPrice(item)) { kuzyaState = kuzyaState.copy(coins = kuzyaState.coins - itemPrice(item), inventory = addToInventory(kuzyaState.inventory, item)); AudioPlayer.playSFX(R.raw.sfx_coin) }
        }, { showShop = false })

        AnimatedVisibility(visible = showAchievement != null, enter = slideInVertically() + fadeIn(), exit = slideOutVertically() + fadeOut(), modifier = Modifier.align(Alignment.TopCenter)) {
            AchievementNotification(showAchievement ?: "", onDismiss = { showAchievement = null })
        }

        if (showLevelComplete) LevelComplete(stars = calculateStars(kuzyaState), message = "Кнопа счастлив и здоров!\nУровень: ${kuzyaState.level}", onNext = { if (stage < 5) onNextStage() else onGameComplete() })
    }
}

// ==================== Компоненты интерфейса ====================

@Composable
private fun KuzyaHeader(kuzyaState: KuzyaState, stage: Int, coins: Int) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text("Кнопа ❤️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FairyPink); Text("Уровень ${kuzyaState.level} • День ${kuzyaState.age}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
            Row(verticalAlignment = Alignment.CenterVertically) { Text("⭐", fontSize = 20.sp); Text("$coins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FairyGold) }
        }
    }
}

@Composable
private fun AnimatedKuzya(state: KuzyaState, scale: Float) {
    val imgRes = when {
        state.isSleeping -> R.drawable.character_kuzya_sleeping; state.mood == KuzyaMood.ECSTATIC || state.mood == KuzyaMood.HAPPY -> R.drawable.character_kuzya_happy
        state.mood == KuzyaMood.HUNGRY -> R.drawable.character_kuzya_hungry; state.mood == KuzyaMood.SAD -> R.drawable.character_kuzya_sad
        state.mood == KuzyaMood.SICK -> R.drawable.character_kuzya_sad; state.mood == KuzyaMood.SLEEPY -> R.drawable.character_kuzya_sleeping
        state.mood == KuzyaMood.DIRTY -> R.drawable.character_kuzya_neutral; state.mood == KuzyaMood.ANGRY -> R.drawable.character_kuzya_hungry
        else -> R.drawable.character_kuzya_neutral
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(imgRes), contentDescription = "Кнопа", modifier = Modifier.size(120.dp).scale(scale), contentScale = ContentScale.Fit)
        Text(when (state.mood) { KuzyaMood.ECSTATIC -> "Мур-мур-мур!"; KuzyaMood.HAPPY -> "Мяу! Я счастлив!"; KuzyaMood.HUNGRY -> "Мяу... Я голоден..."; KuzyaMood.SAD -> "Мяу... Мне грустно..."; KuzyaMood.SICK -> "Мяу... Я болею..."; KuzyaMood.SLEEPY -> "Хочу спать..."; KuzyaMood.DIRTY -> "Мяу! Я грязный!"; KuzyaMood.ANGRY -> "МЯУ! Я злой!"; else -> "Мяу!" }, style = MaterialTheme.typography.bodyMedium, color = FairyPurple, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SleepingAnimation() { var o by remember { mutableFloatStateOf(0f) }; LaunchedEffect(Unit) { while (true) { delay(2000); o = -20f } }; Text("Z", fontSize = 24.sp, color = FairyBlue, modifier = Modifier.offset(y = o.dp)) }

@Composable
private fun StatusBars(kuzyaState: KuzyaState) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            StatusBar("🍽️ Сытость", kuzyaState.hunger, if (kuzyaState.hunger < 30) FairyPink else FairyGreen)
            StatusBar("😊 Счастье", kuzyaState.happiness, FairyGold); StatusBar("⚡ Энергия", kuzyaState.energy, FairyBlue)
            StatusBar("❤️ Здоровье", kuzyaState.health, if (kuzyaState.health < 30) Color.Red else FairyGreen)
            StatusBar("🧼 Чистота", kuzyaState.cleanliness, FairyPurple)
        }
    }
}

@Composable
private fun StatusBar(label: String, value: Float, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(100.dp))
        LinearProgressIndicator(progress = value / 100f, modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)), color = color, trackColor = color.copy(alpha = 0.2f))
        Text("${value.toInt()}%", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun FoodTab(onFeedItem: (InventoryItem) -> Unit, onStartMiniGame: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Покорми Кнопу", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FairyPurple); Spacer(Modifier.height(8.dp))
            Text("Выбери еду или сыграй в мини-игру", style = MaterialTheme.typography.bodySmall, color = Color.Gray); Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(180.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ItemCatalog.getItemsByType(ItemType.FOOD).take(8)) { item -> FoodItemButton(item) { onFeedItem(item) } }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onStartMiniGame, Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyGreen), shape = RoundedCornerShape(16.dp)) { Text("🎮 Мини-игра: Накорми Кнопу", fontSize = 16.sp, color = Color.White) }
        }
    }
}

@Composable
private fun FoodItemButton(item: InventoryItem, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(FairyGold.copy(alpha = 0.2f)).clickable(onClick = onClick).padding(8.dp)) {
        Image(painterResource(item.imageRes), contentDescription = item.name, modifier = Modifier.size(48.dp), contentScale = ContentScale.Fit)
        Text(item.name, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
    }
}

@Composable
private fun GamesTab(onPlayGame: (MiniGameType) -> Unit, kuzyaState: KuzyaState) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Игры с Кнопой", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FairyPurple); Spacer(Modifier.height(8.dp))
            listOf(Triple(MiniGameType.FISHING, "🎣 Рыбалка", "Поймай рыбку"), Triple(MiniGameType.CATCH_MOUSE, "🐭 Поймай мышку", "Помоги Кнопе"), Triple(MiniGameType.PUZZLE, "🧩 Пазл", "Собери картинку")).forEach { (t, title, desc) ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onPlayGame(t) }, colors = CardDefaults.cardColors(containerColor = FairyBlue.copy(alpha = 0.1f))) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(title, fontSize = 20.sp); Spacer(Modifier.weight(1f)); Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                }
            }
        }
    }
}

@Composable
private fun CareTab(onBath: () -> Unit, onHeal: () -> Unit, kuzyaState: KuzyaState) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Уход за Кнопой", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FairyPurple); Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = onBath, Modifier.weight(1f).height(80.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyBlue), shape = RoundedCornerShape(16.dp)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("🛁", fontSize = 28.sp); Text("Купать", fontSize = 14.sp, color = Color.White) } }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onHeal, Modifier.weight(1f).height(80.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPink), shape = RoundedCornerShape(16.dp), enabled = kuzyaState.health < 100 && kuzyaState.coins >= 20) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("💊", fontSize = 28.sp); Text("Лечить (20⭐)", fontSize = 12.sp, color = Color.White) } }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {}, Modifier.weight(1f).height(80.dp), colors = ButtonDefaults.buttonColors(containerColor = FairyPurple), shape = RoundedCornerShape(16.dp)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("😴", fontSize = 28.sp); Text("Спать", fontSize = 14.sp, color = Color.White) } }
            }
        }
    }
}

// ==================== Мини-игры ====================

enum class MiniGameType { FEEDING, FISHING, CATCH_MOUSE, PUZZLE }

@Composable
private fun MiniGameDialog(gameType: MiniGameType, kuzyaState: KuzyaState, onComplete: (Float) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(when (gameType) { MiniGameType.FEEDING -> "Накорми Кнопу"; MiniGameType.FISHING -> "Рыбалка"; MiniGameType.CATCH_MOUSE -> "Поймай мышку"; MiniGameType.PUZZLE -> "Пазл" }, fontWeight = FontWeight.Bold) },
        text = { when (gameType) { MiniGameType.FEEDING -> FeedingMiniGame(kuzyaState, onComplete); MiniGameType.FISHING -> FishingMiniGame(onComplete); MiniGameType.CATCH_MOUSE -> CatchMouseMiniGame(onComplete); MiniGameType.PUZZLE -> PuzzleMiniGame(onComplete) } },
        confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } })
}

@Composable
private fun FeedingMiniGame(kuzyaState: KuzyaState, onComplete: (Float) -> Unit) {
    var foodCount by remember { mutableIntStateOf(0) }; val targetFood = remember { Random.nextInt(3, 8) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Дай Кнопе $targetFood кусочков еды!", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp))
        Text("Собрано: $foodCount из $targetFood", style = MaterialTheme.typography.bodyLarge, color = if (foodCount >= targetFood) FairyGreen else FairyGold); Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(200.dp)) {
            items(List(maxOf(targetFood, foodCount + 2)) { it }) { index ->
                if (index < foodCount) Box(Modifier.padding(4.dp).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(FairyGreen.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) { Text("✅", fontSize = 32.sp) }
                else {
                    val foods = listOf(R.drawable.item_apple, R.drawable.item_pizza, R.drawable.item_banana, R.drawable.item_fish, R.drawable.item_milk, R.drawable.item_cheese)
                    Box(Modifier.padding(4.dp).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(FairyGold.copy(alpha = 0.3f)).clickable { foodCount++; AudioPlayer.playSFX(R.raw.sfx_eat); if (foodCount >= targetFood) onComplete(30f) }, contentAlignment = Alignment.Center) {
                        Image(painterResource(foods[index % foods.size]), contentDescription = null, modifier = Modifier.size(40.dp), contentScale = ContentScale.Fit)
                    }
                }
            }
        }
    }
}

@Composable
private fun FishingMiniGame(onComplete: (Float) -> Unit) {
    var fishPosition by remember { mutableFloatStateOf(Random.nextFloat()) }; var score by remember { mutableIntStateOf(0) }; var timeLeft by remember { mutableIntStateOf(15) }; var isGameOver by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (timeLeft > 0 && !isGameOver) { delay(1000); timeLeft--; fishPosition = Random.nextFloat() }; if (!isGameOver) { isGameOver = true; onComplete(score.toFloat() * 5f) } }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Поймай рыбку! Время: $timeLeft сек"); Text("Счёт: $score", color = FairyGold); Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(150.dp).background(FairyBlue.copy(alpha = 0.2f)).clickable { if (fishPosition in 0.4f..0.6f) { score++; fishPosition = Random.nextFloat(); AudioPlayer.playSFX(R.raw.sfx_splash) } }, contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.item_fish), contentDescription = null, modifier = Modifier.size(48.dp).offset(x = ((fishPosition - 0.5f) * 200).dp), contentScale = ContentScale.Fit)
        }
    }
}

@Composable
private fun CatchMouseMiniGame(onComplete: (Float) -> Unit) {
    var mouseVisible by remember { mutableStateOf(true) }; var mousePosition by remember { mutableStateOf(Pair(Random.nextFloat(), Random.nextFloat())) }; var score by remember { mutableIntStateOf(0) }; var timeLeft by remember { mutableIntStateOf(20) }
    LaunchedEffect(Unit) { while (timeLeft > 0) { delay(1000); timeLeft--; if (Random.nextFloat() > 0.5f) { mouseVisible = true; mousePosition = Pair(Random.nextFloat(), Random.nextFloat()) } else mouseVisible = false }; onComplete(score.toFloat() * 3f) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Поймай мышек! Время: $timeLeft сек"); Text("Поймано: $score", color = FairyGold); Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(200.dp).background(Color.Gray.copy(alpha = 0.1f))) {
            if (mouseVisible) Text("🐭", fontSize = 40.sp, modifier = Modifier.offset(x = (mousePosition.first * 250).dp, y = (mousePosition.second * 150).dp).clickable { score++; mouseVisible = false; AudioPlayer.playSFX(R.raw.sfx_squeak) })
            Image(painterResource(R.drawable.character_kuzya_happy), contentDescription = null, modifier = Modifier.size(60.dp).align(Alignment.BottomCenter), contentScale = ContentScale.Fit)
        }
    }
}

@Composable
private fun PuzzleMiniGame(onComplete: (Float) -> Unit) {
    var pieces by remember { mutableStateOf(List(4) { it }.shuffled()) }; var placedPieces by remember { mutableIntStateOf(0) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Собери пазл с Кнопой!"); Text("Собрано: $placedPieces из 4"); Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxWidth().height(200.dp)) {
            items(4) { index -> Box(Modifier.padding(4.dp).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(if (pieces[index] == index) FairyGreen.copy(alpha = 0.5f) else FairyGold.copy(alpha = 0.3f)).clickable { if (pieces[index] == index) { placedPieces++; if (placedPieces >= 4) onComplete(20f) } }, contentAlignment = Alignment.Center) {
                Image(painterResource(when (pieces[index]) { 0 -> R.drawable.character_kuzya_happy; 1 -> R.drawable.item_fish; 2 -> R.drawable.item_apple; else -> R.drawable.item_apple }), contentDescription = null, modifier = Modifier.size(40.dp), contentScale = ContentScale.Fit)
            } }
        }
    }
}

// ==================== Магазин ====================

@Composable
private fun ShopDialog(kuzyaState: KuzyaState, onBuy: (InventoryItem) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Row(verticalAlignment = Alignment.CenterVertically) { Text("🛒 Магазин", fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("⭐${kuzyaState.coins}", color = FairyGold) } },
        text = { LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(ItemCatalog.allItems.take(15)) { item -> ShopItemCard(item, kuzyaState.coins >= itemPrice(item)) { onBuy(item) } } } },
        confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } })
}

@Composable
private fun ShopItemCard(item: InventoryItem, canBuy: Boolean, onBuy: () -> Unit) {
    Card(Modifier.aspectRatio(0.8f).clickable(enabled = canBuy, onClick = onBuy), colors = CardDefaults.cardColors(containerColor = if (canBuy) Color.White else Color.Gray.copy(alpha = 0.3f)), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(item.imageRes), contentDescription = item.name, modifier = Modifier.size(40.dp), contentScale = ContentScale.Fit)
            Text(item.name, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            Text("⭐${itemPrice(item)}", style = MaterialTheme.typography.bodySmall, color = if (canBuy) FairyGold else Color.Gray)
        }
    }
}

@Composable
private fun AchievementNotification(title: String, onDismiss: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = FairyGold), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text("🏆", fontSize = 32.sp); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Достижение!", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White); Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White) }; IconButton(onClick = onDismiss) { Text("✕", color = Color.White) } }
    }
}

// ==================== Вспомогательные функции ====================

private fun calculateMood(state: KuzyaState) = when { state.isSleeping -> KuzyaMood.SLEEPY; state.isSick || state.health < 20 -> KuzyaMood.SICK; state.hunger < 10 -> KuzyaMood.HUNGRY; state.cleanliness < 15 -> KuzyaMood.DIRTY; state.happiness < 10 -> KuzyaMood.SAD; state.energy < 10 -> KuzyaMood.SLEEPY; state.happiness > 80 && state.hunger > 80 -> KuzyaMood.ECSTATIC; state.happiness > 60 && state.hunger > 50 -> KuzyaMood.HAPPY; else -> KuzyaMood.NEUTRAL }
private fun feedKuzya(state: KuzyaState, item: InventoryItem) = state.copy(hunger = min(100f, state.hunger + item.effect), happiness = min(100f, state.happiness + item.effect / 2), stats = state.stats.copy(totalFoodEaten = state.stats.totalFoodEaten + 1), mood = calculateMood(state.copy(hunger = min(100f, state.hunger + item.effect))))
private fun addToInventory(inventory: List<InventoryItem>, item: InventoryItem) = inventory.find { it.id == item.id }?.let { inventory.map { if (it.id == item.id) it.copy(quantity = it.quantity + 1) else it } } ?: (inventory + item.copy(quantity = 1))
private fun itemPrice(item: InventoryItem) = when (item.type) { ItemType.FOOD -> 10; ItemType.SNACK -> 15; ItemType.TOY -> 25; ItemType.MEDICINE -> 30; ItemType.SOAP -> 20; ItemType.DECORATION -> 35; ItemType.SPECIAL -> 50 }
private fun calculateStars(state: KuzyaState) = when { (state.hunger + state.happiness + state.health + state.cleanliness + state.energy) / 5 > 80 -> 3; (state.hunger + state.happiness + state.health + state.cleanliness + state.energy) / 5 > 50 -> 2; else -> 1 }
