package com.vasilisinaazbuka.games

/**
 * Менеджер загрузки и кеширования .kar файлов
 * Используется в KaraokeScreen для загрузки караоке-песен
 */
object KarFileLoader {

    private val cache = mutableMapOf<String, Any>()

    /**
     * Загружает .kar файл из assets/kar/
     * @param context Контекст приложения
     * @param fileName Имя файла без расширения (например, "song_01")
     */
    fun loadKarFile(context: android.content.Context, fileName: String): Any? {
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
            android.util.Log.e("KarFileLoader", "Ошибка загрузки $fileName: ${e.message}")
            null
        }
    }

    /**
     * Загружает .kar файл по индексу песни (1-20)
     */
    fun loadKarFile(context: android.content.Context, songIndex: Int): Any? {
        val fileName = "song_${songIndex.toString().padStart(2, '0')}"
        return loadKarFile(context, fileName)
    }

    /**
     * Получает список доступных песен
     */
    fun getAvailableSongs(context: android.content.Context): List<String> {
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
        android.util.Log.d("KarFileLoader", "Кеш караоке очищен")
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
