plugins {
    id("org.jetbrains.kotlin.android")
    id("com.android.library")
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
}

dependencies {
    implementation("com.goterl:lazysodium-android:5.1.0")
    implementation("net.java.dev.jna:jna:5.14.0@aar")
}
