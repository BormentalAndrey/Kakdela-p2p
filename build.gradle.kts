// Top of file: plugins block (update versions as needed)
plugins {
    id("com.android.application") version "8.5.0" apply false  // Use latest AGP compatible with Gradle 8.7
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false  // Match Kotlin for KSP
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false  // NEW: Compose Compiler Plugin
}

// Fix deprecation: Replace any `buildDir` usage, e.g.:
// Instead of: val customDir = buildDir.resolve("custom")
// Use: val customDir = layout.buildDirectory.dir("custom").get().asFile
