pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.4.0"
        id("org.jetbrains.kotlin.android") version "1.9.22"
        id("org.jetbrains.kotlin.plugin.compose") version "1.9.22"
        id("com.google.devtools.ksp") version "1.9.22-1.0.11"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Kakdela-p2p"
include(":app")
