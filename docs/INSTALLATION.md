# Installation

## Gradle
Add the dependency from jCenter to your app module's (not top-level) `build.gradle` file:

```groovy
repositories {
    jcenter()
}

dependencies {
    implementation 'dev.hotwire:turbo:7.0.0-beta01'
}
```

See the [latest version](https://bintray.com/hotwire/maven/turbo-android) available on Bintray/JCenter.

## Required `minSdkVersion`
Android SDK 24 (or greater) is required as the `minSdkVersion` in your app module's `build.gradle` file:
```groovy
defaultConfig {
    minSdkVersion 24
    // ...
}
```

## Internet Permission
In order for a [WebView](https://developer.android.com/reference/android/webkit/WebView.html) to access the Internet and load web pages, your app must have the `INTERNET` permission. Make sure you include this permission in your app's `AndroidManifest.xml` file:
```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

# Pre-release Builds
Pre-release builds will be published to [GitHub Packages](https://github.com/features/packages).

## Personal Access Token
If you'd like to use a pre-release version, you'll need to create a [Personal Access Token](https://docs.github.com/en/free-pro-team@latest/packages/learn-github-packages/about-github-packages#authenticating-to-github-packages) in your GitHub account and give it the `read:packages` permission.

Copy your access token to your `.bash_profile` (or another accessible place that's outside of source control):

```bash
export GITHUB_USER='<your username>'
export GITHUB_ACCESS_TOKEN='<your personal access token>'
```

##  Gradle
Add the GitHub Packages maven repository and the dependency to your app module's `build.gradle` file:

```groovy
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/hotwired/turbo-android")

        credentials {
            username = System.getenv('GITHUB_USER')
            password = System.getenv('GITHUB_ACCESS_TOKEN')
        }
    }
}

dependencies {
    implementation 'dev.hotwire:turbo:7.0.1-alpha01'
}
```
