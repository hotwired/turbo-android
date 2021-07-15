# Turbo Native for Android

**Note:** The Hotwire frameworks are presented in beta form. We're using them all in production with HEY, but expect that significant changes might be made in response to early feedback. ✌️❤️

---------

**Build high-fidelity hybrid apps with native navigation and a single shared web view**. Turbo Native for Android provides the tooling to wrap your [Turbo 7](https://turbo.hotwired.dev/)-enabled web app in a native Android shell. It manages a single WebView instance across multiple Fragment destinations, giving you native navigation UI with all the client-side performance benefits of Turbo.

## Features
- **Deliver fast, efficient hybrid apps.** Avoid reloading JavaScript and CSS. Save memory by sharing one WebView.
- **Reuse mobile web views across platforms.** Create your views once, on the server, in HTML. Deploy them to [iOS](https://github.com/hotwired/turbo-ios), Android, and mobile browsers simultaneously. Ship new features without waiting on Play Store approval.
- **Enhance web views with native UI.** Navigate web views using native patterns. Augment web UI with native controls.
- **Produce large apps with small teams.** Achieve baseline HTML coverage for free. Upgrade to native views as needed.

## Requirements

1. Android SDK 24+ is required as the `minSdkVersion` in your build.gradle.
1. This library is written entirely in [Kotlin](https://kotlinlang.org/), and your app should use Kotlin as well. Compatibility with Java is not provided or supported.
1. This library supports web apps using either Turbo 7 or Turbolinks 5.
1. `Turbo` (or `Turbolinks`) is exposed on the `window` object on the WebView page being loaded.

**Note:** You should understand how Turbo works with web applications in the browser before attempting to use Turbo Android. See the [Turbo 7 documentation](https://turbo.hotwired.dev) for details.

## Getting Started
The best way to get started with Turbo Android is to try out the demo app first to get familiar with the framework. The demo app walks you through all the basic Turbo flows as well as some advanced features. To run the demo, clone this repo, open the directory in Android Studio, and build the `demo` module to your Android device. See [demo/README.md](demo/README.md) for more details about the demo. When you’re ready to start your own application, read through the rest of the documentation.

See the [instructions to build the project yourself](docs/BUILD-PROJECT.md).

## Documentation

1. [Installation](docs/INSTALLATION.md)
1. [Overview](docs/OVERVIEW.md)
1. [Quick Start](docs/QUICK-START.md)
1. [Path Configuration](docs/PATH-CONFIGURATION.md)
1. [Navigation](docs/NAVIGATION.md)
1. [Advanced Options](docs/ADVANCED-OPTIONS.md)

## Contributing

Turbo Android is open-source software, freely distributable under the terms of an [MIT-style license](LICENSE). The [source code is hosted on GitHub](https://github.com/hotwired/turbo-android). Development is sponsored by [Basecamp](https://basecamp.com/).

We welcome contributions in the form of bug reports, pull requests, or thoughtful discussions in the [GitHub issue tracker](https://github.com/hotwired/turbo-android/issues).

Please note that this project is released with a [Contributor Code of Conduct](docs/CONDUCT.md). By participating in this project you agree to abide by its terms.

---------

© 2020 Basecamp, LLC
