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
        mavenCentral() // <--- PASTI ADA INI! MapLibre tinggal di sini.
    }
}

rootProject.name = "ForestGuard"
include(":app")