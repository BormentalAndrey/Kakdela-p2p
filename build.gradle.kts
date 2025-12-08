// Top-level build file
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false // ЭТА СТРОКА РЕШАЕТ ВСЁ
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
