// Top-level build file (корневой build.gradle.kts)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false   // ← KSP 2.2.20-2.0.4 из libs.versions.toml
}

// УДАЛИЛИ ВЕСЬ БЛОК allprojects { repositories { … } } — это и вызывало ошибку!
// Репозитории теперь только в settings.gradle.kts

// Исправлено устаревшее rootProject.buildDir → layout.buildDirectory
tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
