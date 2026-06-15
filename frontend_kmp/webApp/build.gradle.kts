plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport { enabled.set(true) }
                outputFileName = "pontolivre.js"
            }
            runTask {
                devServer = devServer?.copy(port = 3000)
            }
        }
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":frontend_kmp:shared"))
                implementation(compose.html.core)
                implementation(compose.runtime)
                implementation("io.ktor:ktor-client-js:2.3.10")
            }
        }
    }
}
