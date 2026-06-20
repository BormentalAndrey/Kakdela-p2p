# Основные правила ProGuard для "В гостях у Василисы"

# Сохраняем Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.vasilisinaazbuka.**$$serializer { *; }
-keepclassmembers class com.vasilisinaazbuka.** {
    *** Companion;
}
-keepclasseswithmembers class com.vasilisinaazbuka.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Сохраняем Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.vasilisinaazbuka.data.** { *; }

# Сохраняем модели данных
-keep class com.vasilisinaazbuka.games.KarFile { *; }
-keep class com.vasilisinaazbuka.games.KarMetadata { *; }
-keep class com.vasilisinaazbuka.games.KarLyricEvent { *; }

# Сохраняем enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Сохраняем Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Сохраняем R-классы
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Оптимизация Compose
-allowaccessmodification
-repackageclasses
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Сохраняем MIDI-парсер
-keep class com.vasilisinaazbuka.games.KarParser { *; }

# Удаляем логирование в релизе
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
