plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    // ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" // ← ОБЯЗАТЕЛЬНО!
    // ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
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
        kotlinCompilerExtensionVersion = "1.5.17" // Стабильная с Compose ~1.6–1.7
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM (рекомендуется последняя на декабрь 2025)
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))

    // Основные Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation) // ← ВАЖНО: clickable, clip, draggable и т.д.
    implementation(libs.androidx.activity.compose)

    // Material Design (для некоторых компонентов, если используете)
    implementation("com.google.android.material:material:1.12.0")

    // Навигация + сериализация маршрутов
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Иконки Material3 (рекомендуемый современный способ)
    implementation("androidx.compose.material3:material3-icons-filled")
    implementation("androidx.compose.material3:material3-icons-outlined")
    // Если нужны ВСЕ иконки (очень большой размер APK — не рекомендуется):
    // implementation("androidx.compose.material3:material3-icons-extended")

    // Coil 3 для AsyncImage (актуально на декабрь 2025)
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0") // для загрузки по сети

    // QR-код
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Room (если используете)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines + WebSocket + Crypto
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.java.websocket)
    implementation("com.goterl:lazysodium-android:5.1.0")

    // WebRTC — обновлённая версия от Stream (на декабрь 2025)
    implementation("io.getstream:stream-webrtc-android-ui:1.4.2") // ← проверьте актуальную на GitHub/Maven
    implementation("io.getstream:stream-webrtc-android-core:1.4.2")

    // Debug
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation("androidx.compose.ui:ui-tooling-preview")
}
