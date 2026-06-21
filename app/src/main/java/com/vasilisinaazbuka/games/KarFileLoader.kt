package com.vasilisinaazbuka.games

import android.content.Context
import android.util.Log

/**
 * Менеджер загрузки и кеширования .kar файлов
 * Используется в KaraokeScreen для загрузки караоке-песен
 */
object KarFileLoader {

    private val cache = mutableMapOf<String, KarFile>()

    /**
     * Загружает .kar файл из assets/kar/
     * @param context Контекст приложения
     * @param fileName Имя файла без расширения (например, "song_01")
     */
    fun loadKarFile(context: Context, fileName: String): KarFile? {
        // Проверяем кеш
        cache[fileName]?.let { return it }

        return try {
            val inputStream = context.assets.open("kar/$fileName.kar")
            val bytes = inputStream.readBytes()
            inputStream.close()
            
            // Парсим .kar файл
            val karFile = KarParser.parse(bytes)
            cache[fileName] = karFile
            karFile
        } catch (e: Exception) {
            Log.e("KarFileLoader", "Ошибка загрузки $fileName: ${e.message}")
            null
        }
    }

    /**
     * Загружает .kar файл по индексу песни (1-20)
     */
    fun loadKarFile(context: Context, songIndex: Int): KarFile? {
        val fileName = "song_${songIndex.toString().padStart(2, '0')}"
        return loadKarFile(context, fileName)
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
     * Очищает кеш загруженных файлов
     */
    fun clearCache() {
        cache.clear()
        Log.d("KarFileLoader", "Кеш караоке очищен")
    }

    /**
     * Проверяет, загружен ли файл в кеш
     */
    fun isCached(fileName: String): Boolean {
        return cache.containsKey(fileName)
    }

    /**
     * Удаляет конкретный файл из кеша
     */
    fun removeFromCache(fileName: String) {
        cache.remove(fileName)
    }
}
