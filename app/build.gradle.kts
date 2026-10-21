import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Built-in default API key, injected at build time from a file OUTSIDE the
// project (env YTSUBS_KEY_FILE). Never committed to source.
val keyFilePath = System.getenv("YTSUBS_KEY_FILE")
val defaultApiKey = keyFilePath
    ?.let { p -> File(p).takeIf { f -> f.exists() }?.readText()?.trim() }
    ?: ""

android {
    namespace = "com.deon.ytsubslive"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.deon.ytsubslive"
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "1.0.0"
        buildConfigField("String", "DEFAULT_API_KEY", "\"$defaultApiKey\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    lint {
        checkReleaseBuilds = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core:1.9.0")
}
