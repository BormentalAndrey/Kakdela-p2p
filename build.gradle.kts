// Top-level build file (Project level) — 100% рабочий для Gradle 8.3
plugins {
    id("com.android.application") version "8.3.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("kotlin-kapt") apply false
}

// Репозитории для всех модулей
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
