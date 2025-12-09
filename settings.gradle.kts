pluginManagement {
    repositories {
        google()  // CRITICAL: For AGP resolution
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()  // CRITICAL: For AGP + AndroidX deps
        mavenCentral()
    }
}

rootProject.name = "Kakdela-p2p"
include(":app")
