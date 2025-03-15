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
        flatDir {
            dirs("libs")  // This tells Gradle where to find AAR files
        }
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
            name = "TarsosDSP repository"
            url = uri("https://mvn.0110.be/releases")
        }

    }
}

rootProject.name = "MusicToVibrationApp"
include(":app")
 