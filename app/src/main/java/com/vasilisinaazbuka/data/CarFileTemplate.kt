package com.vasilisinaazbuka.data

/**
 * Документация формата .car файлов
 * 
 * Каждый .car файл — это JSON с следующей структурой:
 * 
 * {
 *   "version": 1,
 *   "metadata": {
 *     "title": "Название песни",
 *     "author": "Автор",
 *     "difficulty": 2,
 *     "tempo_bpm": 120,
 *     "description": "Описание песни",
 *     "theme": "fairy_tale",
 *     "image_resource": "bg_karaoke_song_01"
 *   },
 *   "syllables": [
 *     {
 *       "id": 1,
 *       "text": "МА",
 *       "start_time_ms": 0,
 *       "end_time_ms": 500,
 *       "emphasis": true,
 *       "color": "#FFB74D",
 *       "animation": "bounce"
 *     }
 *   ],
 *   "words": [
 *     {
 *       "id": 1,
 *       "text": "МАМА",
 *       "syllable_ids": [1, 2],
 *       "hint_text": "Родной человек",
 *       "hint_image": "hint_mother"
 *     }
 *   ],
 *   "music_notes": [
 *     {
 *       "time_ms": 0,
 *       "note": "C4",
 *       "duration_ms": 500,
 *       "syllable_id": 1
 *     }
 *   ],
 *   "background": "bg_karaoke_song_01",
 *   "character_emotion": "happy"
 * }
 */
object CarFileTemplate {
    // Пустой объект для документации
}
