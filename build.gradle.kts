// Корневой build.gradle.kts (Project-level)
plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24" apply false
    id("kotlin-kapt") apply false
}

// Обязательно нужны репозитории для плагинов
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Репозитории для зависимостей
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Отключаем лишние предупреждения и ускоряем сборку
tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
    options.isFork = true
    options.forkOptions.jvmArgs?.addAll(listOf("-Xmx4g", "-Dfile.encoding=UTF-8"))
}

subprojects {
    afterEvaluate {
        configurations.all {
            resolutionStrategy.cacheChangingModulesFor(0, "seconds")
            resolutionStrategy.cacheDynamicVersionsFor(0, "seconds")
        }
    }
}
