plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.kakdela.goterl"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
}

dependencies {
    implementation("com.goterl:lazysodium-android:5.1.0")
    implementation("net.java.dev.jna:jna:5.14.0@aar")
}
