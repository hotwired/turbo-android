# Turbolinks Android

Turbolinks Android is a native adapter for any [Turbolinks 6](https://github.com/turbolinks/turbolinks#readme) enabled web app. It's built entirely using standard Android tools and conventions.

This library has been in use and tested in the wild since June 2020 in the all-new [HEY Android](https://play.google.com/store/apps/details?id=com.basecamp.hey&hl=en_US) app.

## Contents

1. [Prerequisites](#prerequisites)
1. [Installation](#installation)
1. [Getting Started](#getting-started)
1. [Additional Configuration](#additional-configuration)
1. [Running the Demo App](#running-the-demo-app)
1. [Contributing](#contributing)

### Prerequisites

1. Android API 24+ is required as the `minSdkVersion` in your build.gradle.
1. In order for a [WebView](https://developer.android.com/reference/android/webkit/WebView.html) to access the Internet and load web pages, your application must have the `INTERNET` permission. Make sure you have `<uses-permission android:name="android.permission.INTERNET" />` in your Android manifest.

## Installation
Add the dependency from jCenter to your module's (not top-level) `build.gradle` file.

```groovy
repositories {
    jcenter()
}

dependencies {
    implementation 'com.basecamp:turbolinks:2.0.0'
}
```

## Getting Started

### Create a Configuration
A configuration file specifies the set of rules Turbolinks will follow to navigate and present views. It has two sections: 

1. Application-level settings
1. Path-specific rules 

Typically path-specific rules will do most of the work in determining how and when a particular URI will be navigated to, but additional application-level settings can also be applied to customize navigation logic (see [Create a destination interface](#create-a-destination-interface)).

At minimum you will need a `src/main/assets/json/configuration.json` file that Turbolinks can read, with at least one path configuration. Note that the configuration file is processed in order and cascades downward, similar to CSS. The top most declaration should establish the default behavior for all path patterns, while each subsequent rule can override for specific behavior.

Example:

```json
{
  "rules": [
    {
      "patterns": [
        ".*"
      ],
      "properties": {
        "context": "default",
        "uri": "turbolinks://fragment/web",
        "pull_to_refresh_enabled": true,
        "screenshots_enabled": true
      }
    }
  ]
}
```

#### Patterns

The `pattern` attribute defines a Regex pattern that will be used to determine if a provided URI matches (and as a result, which `properties` should be applied).

#### Properties

The `properties` that Turbolinks supports out of the box are:

* `uri`: **Required**. Must map to an Activity or Fragment that has implemented the [TurbolinksNavGraphDestination](/turbolinks/src/main/kotlin/com/basecamp/turbolinks/nav/TurbolinksNavGraphDestination.kt) annotation with a matching `uri` value.
* `context`: Optional. Possible values are `default` or `modal`. Describes the presentation context in which the view should be displayed. Turbolinks will determine what the navigation behavior should be based on this value + `presenentation`. Unless you are specifically showing a modal-style view (e.g., a form), `default` is usually sufficient. Defaults to `default`.
* `presentation`: Optional. Possible values are `default`, `push`, `pop`, `replace`, `replace_root`, `clear_all`, `refresh`, or `none`. Turbolinks will determine what the navigation behavior should be based on this value + `context`. In most cases `default` should be sufficient, but you may find cases where your app needs specific beahvior. Defaults to `default`.
* `fallback_uri`: Optional. Provides a fallback URI in case a destination cannot be found that maps to the `uri`. Can be useful in cases when pointing to a new `uri` that may not be deployed yet.
* `pull_to_refresh_enabled`: Optional. Whether or not pull to refresh should be enabled for a given path. Defaults to `false`.
* `screenshots_enabled`: Optional. Whether or not transitional screenshots (when returning to a previous screen) should be used. This gives the appearance of a much faster experience going back, but does require more performance overhead. Defaults to `true`.

### Create a Layout
You'll need a basic layout for your fragment to inflate. Refer to `fragment_web.xml` and feel free to copy that as a starting point.

The most important thing is to include a reference to the `turbolinks_default` resource. This is a view provided by the library which automatically add the necessary view hierarchy that Turbolinks expects for attaching a WebView, progress view, and error view.

```xml
<include
    layout="@layout/turbolinks_default"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintTop_toBottomOf="@+id/app_bar" />
```

### Create a Destination Interface
This step is optional.

The standard `TurbolinksNavDestination` provides most of what you need, but extending this interface and creating your own can provide some navigational flexibility that many, more complex apps will need.

Refer to `NavDestination` as an example, which shows some common additional logic that might be helpful. Feel free to copy that as a starting point.

### Create a Fragment
You'll need at least one fragment to represent the main body of your view. 

A `WebFragment` would be a good option to handle all standard WebViews in your app. This fragment...

* Should implement your custom destination interface as mentioned above, or, if you haven't created one, simply implement `TurbolinksNavDestination`.
* Must extend one of the base fragments provided by Turbolinks. In this case, as a web fragment, you should extend `TurbolinksWebFragment`.

Refer to `WebFragment` as an example and feel free to copy it as a starting point. It outlines the very basics of what every fragment will need to implement — a nav graph annotation, inflating a view, setting up progress and error views, and setting up a toolbar.

### Create an Activity
Turbolinks assumes a single-activity per Turbolinks session architecture. Generally you'll have one activity and many fragments, which will swap into that activity's nav host.

Turbolinks activities are fairly straightforward and simply need to extend `TurbolinksActivity` in order to provide a `TurbolinksDelegate`.

Refer to `MainActivity` and feel free to copy that as a starting point.

### Create a Nav Host Fragment
A nav host fragment is ultimately an extension of the Android Navigation component's `NavHostFragment`, and as such is primarily responsible for providing "an area in your layout for self-contained navigation to occurr." 

The Turbolinks version of this class is bound to a single Turbolinks session, and you will need to implement just a few things.

* The name of the Turbolinks session
* The URI of a starting location
* A list of activities that Turbolinks will be able to navigate to
* A list of fragments that Turbolinks will be able to navigate to
* A path configuration to provide navigation configuration
* Any additional custom setup steps to execute upon creating the session

Refer to `MainSessionNavHostFragment` for an example.

🎉 **Congratulations, you're using Turbolinks on Android!** 🎉

## Additional Configuration

### Multiple instances of TurbolinksSession

There is a 1-1-1 relationship between an activity, its nav host fragment, and its Turbolinks session. The nav host fragment automatically creates a `TurbolinksSession` for you.

You may encounter situations where a truly single activity app may not be feasible — that is, you may need an activity, for example, for logged out state vs. logged in state. Or perhaps it's safer to send inactive users to an entirely different activity to guard access controls.

In such cases you, simply need to create a new activity + nav host fragment, which will in turn create its own Turbolinks session. You will need to be sure to register all these activities as `TurbolinksSessionNavHostFragment().registeredActivities` so that you can navigate between them.

### Custom WebView WebSettings

By default the library sets a few WebSettings on the shared WebView for a given session. Some are required while others serve as reasonable defaults for modern web applications. They are:

- `setJavaScriptEnabled(true)` (required)
- `setDomStorageEnabled(true)`

If however these are not to your liking, you can always override them from your `TurbolinksWebFragment`. The `WebView` is always available to you via `TurbolinksWebFragmentDelegate.webView()`, and you can update the `WebSettings` to your liking.

```kotlin
delegate.webView.settings.domStorageEnabled = false
```

**If you do update the WebView settings, be sure not to override `setJavaScriptEnabled(false)`. Doing so would break Turbolinks, which relies heavily on JavaScript.**

### Custom JavascriptInterfaces

If you have custom JavaScript on your pages that you want to access as JavascriptInterfaces, you can add them like so:

```java
delegate.webView.addJavascriptInterface(this, "MyCustomJavascriptInterface");
```

The Java object being passed in can be anything, as long as it has at least one method annotated with `@android.webkit.JavascriptInterface`. Names of interfaces must be unique, or they will be overwritten in the library's map.

**Do not use the reserved name `TurbolinksSession` for your JavaScriptInterface, as that is used by the library.**

## Running the Demo App

The demo apps bundled with the library work best with the [Turbolinks Demo App](https://github.com/basecamp/turbolinks-demo). You can follow the instructions in that repo to start up the server.

### Start the Demo Android App

- Ensure the demo server and Android device are on the same network.
- Start up the demo server per its instructions.
- Go to `Contants.kt`.
- Find the `BASE_URL` String at the top of the class. Change the IP to your IP.
- Build/run the app to your device.

## Contributing

Turbolinks Android is open-source software, freely distributable under the terms of an [MIT-style license](LICENSE). The [source code is hosted on GitHub](https://github.com/turbolinks/turbolinks-android).

We welcome contributions in the form of bug reports, pull requests, or thoughtful discussions in the [GitHub issue tracker](https://github.com/turbolinks/turbolinks-android/issues). Please see the [Code of Conduct](CONDUCT.md) for our pledge to contributors.

Turbolinks Android's development is sponsored by [Basecamp](https://basecamp.com/).

### Building from Source

#### From Android Studio:

- Open the [project's Gradle file](build.gradle).
- In the menu, choose Build --> Rebuild project.

#### From command line:

- Change directories to the project's root directory.
- Run `./gradlew clean assemble -p turbolinks`.

The .aar's will be built at `<project-root>/turbolinks/build/outputs/aar`.

### Running Tests

**From command line:**

- Change directories to the project's root directory.
- Run `./gradlew clean testRelease -p turbolinks`
