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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    // Lazysodium должен быть доступен для app, используем api
    api("com.goterl:lazysodium-android:5.1.0")

    // JNA для работы с нативными библиотеками
    implementation("net.java.dev.jna:jna:5.14.0@aar")
}
