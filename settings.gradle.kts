pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add JitPack for MPAndroidChart
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "DataSovereignty"
include(":app")
