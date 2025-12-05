// build.gradle.kts (Project level — корень проекта)
plugins {
    id("com.android.application") version "8.4.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("kotlin-kapt") apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
