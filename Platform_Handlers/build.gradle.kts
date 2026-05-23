plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "PlatformHandlers"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Put common dependencies here
        }
    }
}

android {
    namespace = "com.taskbridge.platform"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}
