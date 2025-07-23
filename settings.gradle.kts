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
        maven { url = uri("https://briarproject.org/maven") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Talita"
include(":app")
