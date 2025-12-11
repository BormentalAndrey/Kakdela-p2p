plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp") // Для Room и других KSP-плагинов
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
        kotlinCompilerExtensionVersion = "2.2.21"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Новый DSL для Kotlin компилятора
    compilerOptions {
        jvmTarget.set(JavaVersion.VERSION_17.toString())
        freeCompilerArgs.addAll(listOf("-opt-in=kotlin.RequiresOptIn"))
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.13.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.3")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")

    // Coil 3
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // WebRTC
    implementation("io.getstream:stream-webrtc-android:1.3.10")
    implementation("io.getstream:stream-webrtc-android-ui:1.3.10")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // WebSocket
    implementation("org.java-websocket:Java-WebSocket:1.5.6")

    // Crypto
    implementation("com.goterl:lazysodium-android:5.1.0")

    // JNA
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
}

// Для KSP
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Для дебага Compose preview
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}
