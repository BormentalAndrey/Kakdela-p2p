plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4" // лучше использовать версию из BOM ниже
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}

dependencies {
    // AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // === ВАЖНО: Compose BOM (все версии Compose синхронизируются автоматически) ===
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // === ЭТО ГЛАВНОЕ ИСПРАВЛЕНИЕ: добавлена настоящая библиотека Material ===
    implementation("com.google.android.material:material:1.12.0")

    // Coil (для картинок в Compose)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // WebRTC Stream
    implementation("io.getstream:stream-webrtc-android:1.3.10")
    implementation("io.getstream:stream-webrtc-android-ui:1.3.10")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // WebSocket
    implementation("org.java-websocket:Java-WebSocket:1.5.6")

    // Crypto
    implementation("com.goterl:lazysodium-android:5.1.0")

    // JNA
    implementation("net.java.dev.jna:jna:5.14.0@aar")
    implementation("net.java.dev.jna:jna-platform:5.14.0@aar")
}

// KSP аргументы для Room
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
