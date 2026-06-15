plugins {
    kotlin("jvm") version "1.9.23" apply false
    kotlin("multiplatform") version "1.9.23" apply false
    kotlin("android") version "1.9.23" apply false
    kotlin("plugin.spring") version "1.9.23" apply false
    kotlin("plugin.jpa") version "1.9.23" apply false
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.compose") version "1.6.2" apply false
    kotlin("plugin.serialization") version "1.9.23" apply false
}

allprojects {
    group = "com.pontolivre"
    version = "1.0.0"

    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
