// Top-level build file
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    // kapt больше не нужен — ты ничего не обрабатываешь аннотациями
    // если вдруг потом понадобится — добавим KSP
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
