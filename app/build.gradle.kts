plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.github.auag0.imnotadeveloper"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "io.github.auag0.imnotadeveloper"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isJniDebuggable = true
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/kotlin/**"
            excludes += "/kotlin-tooling-metadata.json"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
    externalNativeBuild {
        ndkBuild {
            path = file("src/main/jni/Android.mk")
        }
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
}