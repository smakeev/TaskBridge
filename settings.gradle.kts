rootProject.name = "TaskBridge"
include(":Core")
include(":Platform_Handlers")
include(":Android")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.software/kotlin/public")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
