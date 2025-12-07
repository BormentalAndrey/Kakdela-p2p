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

    // !!! ВЫРАВНИВАЕМ JAVA И KOTLIN !!!
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Lazysodium already includes JNA — DO NOT ADD JNA MANUALLY
    implementation("com.goterl:lazysodium-android:5.1.0")
}
