// Top-level build file: build.gradle.kts (Project level)
plugins {
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("kotlin-kapt") apply false
}

// Репозитории для всех модулей
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
