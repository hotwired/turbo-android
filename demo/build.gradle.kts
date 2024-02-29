plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.10")
    }
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.hotwire.turbo.demo"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))
        }

        getByName("debug") {
            isDebuggable = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        named("main")  { java { srcDirs("src/main/kotlin") } }
        named("test")  { java { srcDirs("src/test/kotlin") } }
        named("debug") { java { srcDirs("src/debug/kotlin") } }
    }

    lint {
        lintConfig = file("android-lint.xml")
    }

    namespace = "dev.hotwire.turbo.demo"
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.browser:browser:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("dev.hotwire:strada:1.0.0-beta3")

    implementation(project(":turbo"))
}

repositories {
    google()
    mavenCentral()
}
