plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "dev.hotwire.turbo.demo"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
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
        jvmTarget = "1.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        named("main")  {java { srcDirs("src/main/kotlin") } }
        named("test")  {java { srcDirs("src/test/kotlin") } }
        named("debug") {java { srcDirs("src/debug/kotlin") } }
    }

    lint {
        lintConfig = file("android-lint.xml")
    }

    namespace = "dev.hotwire.turbo.demo"
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.browser:browser:1.5.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")

    implementation(project(":turbo"))
}

repositories {
    google()
    mavenCentral()
}
