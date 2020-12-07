# Turbo Android

Turbo Android is a native adapter for any [Turbo 7](https://github.com/turbolinks/turbolinks#readme) enabled web app. It enables you to build hybrid (native + web) apps that give you the flexibility to display native screens, `WebView` screens, or a blend of both. It's built entirely using standard Android tools and conventions.

This library has been in use and tested in the wild since June 2020 in the all-new [HEY Android](https://play.google.com/store/apps/details?id=com.basecamp.hey&hl=en_US) app.

## Contents

1. [Introduction](#introduction)
1. [Prerequisites](#prerequisites)
1. [Installation](#installation)
1. [Getting Started](#getting-started)
1. [Advanced Configuration](#advanced-configuration)
1. [Running the Demo App](#try-the-demo-app)
1. [Contributing](#contributing)

## Introduction
Turbo Android uses Google's [Navigation component library](https://developer.android.com/guide/navigation) under the hood to navigate between destinations. It leverages a single-`Activity` architecture and each navigation destination is a `Fragment` that you'll implement in your app. To take advantage of speed improvements that [Turbo](https://github.com/turbolinks/turbolinks) enables for web applications, a single `WebView` instance is swapped between each `TurbolinksWebFragment` destination, so the `WebView` instance and resources don't need to be recreated for each destination.

![Structure of a Turbo App](docs/assets/turbo-app-diagram.png)

## Prerequisites

1. Android API 24+ is required as the `minSdkVersion` in your build.gradle.
1. In order for a [WebView](https://developer.android.com/reference/android/webkit/WebView.html) to access the Internet and load web pages, your application must have the `INTERNET` permission. Make sure you have `<uses-permission android:name="android.permission.INTERNET" />` in your app's `AndroidManifest.xml`.
1. This library is written entirely in [Kotlin](https://kotlinlang.org/), and your app should use Kotlin as well. Explicit compatibility with Java is not provided.

## Installation
Add the dependency from jCenter to your app module's (not top-level) `build.gradle` file.

```groovy
repositories {
    jcenter()
}

dependencies {
    implementation 'com.basecamp:turbolinks:2.0.0'
}
```

## Getting Started
See the instructions to [get started with your app](docs/GETTING-STARTED.md).

## Advanced Configuration
See the instructions to [learn about the advanced configuration options available](docs/ADVANCED-CONFIGURATION.md).

## Try the Demo App
See the instructions to [try out the demo app](docs/DEMO-APP.md).

## Contributing

Turbolinks Android is open-source software, freely distributable under the terms of an [MIT-style license](docs/LICENSE). The [source code is hosted on GitHub](https://github.com/turbolinks/turbolinks-android).

We welcome contributions in the form of bug reports, pull requests, or thoughtful discussions in the [GitHub issue tracker](https://github.com/turbolinks/turbolinks-android/issues). Please see the [Code of Conduct](docs/CONDUCT.md) for our pledge to contributors.

Turbolinks Android's development is sponsored by [Basecamp](https://basecamp.com/).

See the [instructions to build the project yourself](docs/BUILD-PROJECT.md).
