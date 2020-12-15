# Installation

## Gradle
Add the dependency from jCenter to your app module's (not top-level) `build.gradle` file:

```groovy
repositories {
    jcenter()
}

dependencies {
    implementation 'dev.hotwire:turbo:7.0.0-alpha03'
}
```

## Required `minSdkVersion`
Android SDK 24 (or greater) is required as the `minSdkVersion` in your app module's `build.gradle` file:
```groovy
defaultConfig {
    minSdkVersion 24
    ...
}
```

## Internet Permission
In order for a [WebView](https://developer.android.com/reference/android/webkit/WebView.html) to access the Internet and load web pages, your app must have the `INTERNET` permission. Make sure you include this permission in your app's `AndroidManifest.xml` file:
```xml
<uses-permission android:name="android.permission.INTERNET"/>
```
