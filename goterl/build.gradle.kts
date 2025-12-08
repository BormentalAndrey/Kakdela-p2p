plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.kakdela.goterl"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    // --- ВАЖНО --- одинаковая JVM для Kotlin и Java
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // JVM Toolchain — рекомендует сам Gradle
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    // Lazysodium уже содержит JNA — добавлять JNA НЕЛЬЗЯ
    implementation("com.goterl:lazysodium-android:5.1.0")
}
