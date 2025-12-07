// Top-level build file
plugins {
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    // kapt не нужен на уровне root
}

// В Gradle 8.7 с FAIL_ON_PROJECT_REPOS блок repositories не нужен и его убираем
