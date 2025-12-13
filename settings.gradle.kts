pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.android.application") version "8.4.2"
        id("com.android.library") version "8.4.2"
        id("org.jetbrains.kotlin.android") version "1.9.25"
        id("com.google.devtools.ksp") version "1.9.25-1.0.26"
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
