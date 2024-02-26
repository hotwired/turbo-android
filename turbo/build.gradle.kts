plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("maven-publish")
    id("signing")
}

val libVersionName by extra(version as String)
val libraryName by extra("Turbo Native for Android")
val libraryDescription by extra("Android framework for making Turbo native apps")

val publishedGroupId by extra("dev.hotwire")
val publishedArtifactId by extra("turbo")

val siteUrl by extra("https://github.com/hotwired/turbo-android")
val gitUrl by extra("https://github.com/hotwired/turbo-android.git")

val licenseType by extra("MIT License")
val licenseUrl by extra("https://github.com/hotwired/turbo-android/blob/main/LICENSE")

val developerId by extra("basecamp")
val developerEmail by extra("androidteam@basecamp.com")

val isSonatypeRelease by extra(project.hasProperty("sonatype"))

repositories {
    google()
    mavenCentral()
}

android {
    compileSdk = 34
    testOptions.unitTests.isIncludeAndroidResources = true

    defaultConfig {
        minSdk = 26
        targetSdk = 34

        // Define ProGuard rules for this android library project. These rules will be applied when
        // a consumer of this library sets 'minifyEnabled true'.
        consumerProguardFiles("proguard-consumer-rules.pro")
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"))
        }
    }

    sourceSets {
        named("main")  { java { srcDirs("src/main/kotlin") } }
        named("test")  { java { srcDirs("src/test/kotlin") } }
        named("debug") { java { srcDirs("src/debug/kotlin") } }
    }

    namespace = "dev.hotwire.turbo"

    buildFeatures {
        buildConfig = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")
    implementation("com.google.android.material:material:1.11.0")

    // AndroidX
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Networking/API
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Exported AndroidX dependencies
    api("androidx.appcompat:appcompat:1.6.1")
    api("androidx.core:core-ktx:1.12.0")
    api("androidx.webkit:webkit:1.8.0")
    api("androidx.activity:activity-ktx:1.8.1")
    api("androidx.fragment:fragment-ktx:1.6.2")
    api("androidx.navigation:navigation-fragment-ktx:2.7.5")
    api("androidx.navigation:navigation-ui-ktx:2.7.5")

    // Tests
    testImplementation("androidx.test:core:1.5.0") // Robolectric
    testImplementation("androidx.navigation:navigation-testing:2.7.5")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.robolectric:robolectric:4.9.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("com.nhaarman:mockito-kotlin:1.6.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
    testImplementation("junit:junit:4.13.2")
}

tasks {
    // Only sign Sonatype release artifacts
    withType<Sign>().configureEach {
        onlyIf { isSonatypeRelease }
    }
}

// Sign Sonatype published release artifacts
if (isSonatypeRelease) {
    signing {
        val keyId = System.getenv("GPG_KEY_ID")
        val secretKey = System.getenv("GPG_SECRET_KEY")
        val password = System.getenv("GPG_PASSWORD")

        useInMemoryPgpKeys(keyId, secretKey, password)

        setRequired({ gradle.taskGraph.hasTask("publish") })
        sign(publishing.publications)
    }
}

// Publish to GitHub Packages via:
//   ./gradlew -Pversion=<version> clean build publish
//   https://github.com/orgs/hotwired/packages?repo_name=turbo-android
// Publish to Maven Central via:
//   ./gradlew -Psonatype -Pversion=<version> clean build publish
//   https://search.maven.org/artifact/dev.hotwire/turbo
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = publishedGroupId
            artifactId = publishedArtifactId
            version = libVersionName

            pom {
                name.set(libraryName)
                description.set(libraryDescription)
                url.set(siteUrl)

                licenses {
                    license {
                        name.set(licenseType)
                        url.set(licenseUrl)
                    }
                }
                developers {
                    developer {
                        id.set(developerId)
                        name.set(developerId)
                        email.set(developerEmail)
                    }
                }
                scm {
                    url.set(gitUrl)
                }
            }

            // Applies the component for the release build variant
            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        if (isSonatypeRelease) {
            maven {
                url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")

                credentials {
                    username = System.getenv("SONATYPE_USER")
                    password = System.getenv("SONATYPE_PASSWORD")
                }
            }
        } else {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/hotwired/turbo-android")

                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}
