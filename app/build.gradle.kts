plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.kakdela.p2p"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kakdela.p2p"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1")
        }
    }
}

dependencies {
    // Compose + Material3 (стабильные версии 2025)
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Основные AndroidX (обновлённые версии)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")  // ← Исправлено: 1.13.0 (стабильная)
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")  // ← Исправлено: 2.2.1 (стабильная)

    // Криптография (lazysodium)
    implementation("com.goterl:lazysodium-android:5.1.0")
    implementation("net.java.dev.jna:jna:5.14.0@aar")

    // WebRTC
    implementation("com.infobip:google-webrtc:1.0.45036")

    // Навигация
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Resolution strategy для фикса конфликтов (duplicate classes)
configurations.all {
    resolutionStrategy {
        // Форсируем androidx.core (убирает дубли с support)
        force("androidx.core:core:1.13.1")
        // Форсируем JNA (убирает дубли из lazysodium)
        force("net.java.dev.jna:jna:5.14.0")
        // Форсируем annotations (убирает дубли из JetBrains)
        force("org.jetbrains:annotations:24.1.0")
    }
}
