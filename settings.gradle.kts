rootProject.name = "ponto_livre"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/public/p/skiko/dev")
    }
}

include(":backend")
include(":frontend_kmp:shared")
include(":frontend_kmp:androidApp")
include(":frontend_kmp:webApp")
include(":infra")
include(":esp32_firmware")
