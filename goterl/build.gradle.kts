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
}

dependencies {
    // Lazysodium уже включает JNA. Ничего больше добавлять не нужно.
    implementation("com.goterl:lazysodium-android:5.1.0")
}
