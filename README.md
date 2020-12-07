# Turbo Android

Turbo Android is a native adapter for any [Turbo 7](https://github.com/turbolinks/turbolinks#readme) enabled web app. It enables you to build hybrid (native + web) apps that give you the flexibility to display native screens, `WebView` screens, or a blend of both. It's built entirely using standard Android tools and conventions.

This library has been in use and tested in the wild since June 2020 in the all-new [HEY Android](https://play.google.com/store/apps/details?id=com.basecamp.hey&hl=en_US) app.

## Contents

1. [Introduction](#introduction)
1. [Prerequisites](#prerequisites)
1. [Installation](#installation)
1. [Getting Started](#getting-started)
1. [Additional Configuration](#additional-configuration)
1. [Running the Demo App](#running-the-demo-app)
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

### Create a Nav Host Fragment
A nav host fragment is ultimately an extension of the Android Navigation component's [NavHostFragment](https://developer.android.com/reference/androidx/navigation/fragment/NavHostFragment), and as such is primarily responsible for providing "an area in your layout for self-contained navigation to occurr." 

The Turbo extension of this class, `TurbolinksSessionNavHostFragment` is bound to a single Turbo session, and you will need to implement just a few things.

* The name of the Turbolinks session
* The url of a starting location
* A list of activities that Turbolinks will be able to navigate to (optional)
* A list of fragments that Turbolinks will be able to navigate to
* A path configuration to provide navigation configuration

In its simplest form, your `TurbolinksSessionNavHostFragment` will look like:

`MainSessionNavHostFragment`:
```kotlin
class MainSessionNavHostFragment : TurbolinksSessionNavHostFragment() {
    override val sessionName = "main"

    override val startLocation
        get() = "https://hotwire.dev/turbo/demo"

    override val registeredActivities: List<KClass<out Activity>>
        get() = listOf()

    override val registeredFragments: List<KClass<out Fragment>>
        get() = listOf(
            MyWebFragment::class,
            MyNativeImageViewerFragment::class
        )

    override val pathConfigurationLocation: TurbolinksPathConfiguration.Location
        get() = TurbolinksPathConfiguration.Location(
            assetFilePath = "json/configuration.json"
            remoteFileUrl = "https://hotwire.dev/turbo/demo/config/android-v1.json"
        )
}
```

See the Fragment section (TODO) below to create a `Fragment` that you'll register. See the Configuration secton (TODO) below to create your path configuration file(s).

Refer to [MainSessionNavHostFragment](demoapp_simple/src/main/kotlin/com/basecamp/turbolinks/demosimple/main/MainSessionNavHostFragment.kt) for an example.

### Create an Activity layout
You need to create a layout file that your `Activity` will use to host the nav host fragment that you created above.

AndroidX provides a [`FragmentContainerView`](https://developer.android.com/reference/androidx/fragment/app/FragmentContainerView) to contain `NavHostFragment` navigation. In its simplest form, your layout file will look like:

`res/layout/activity_main.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/main_nav_host"
        android:name="com.basecamp.turbolinks.demosimple.main.MainSessionNavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:defaultNavHost="false" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### Create an Activity
It's strongly recommended to use a single-`Activity` architecture in your app. Generally, you'll have one `Activity` and many `Fragments`. Navigating between `Fragments` will take place within the `TurbolinksSessionNavHostFragment` instance hosted in the `Activity`'s layout file.

A Turbolinks `Activity` is straightforward and simply need to implement the [TurbolinksActivity](turbolinks/src/main/kotlin/com/basecamp/turbolinks/activities/TurbolinksActivity.kt) interface in order to provide a [TurbolinksActivityDelegate](turbolinks/src/main/kotlin/com/basecamp/turbolinks/delegates/TurbolinksActivityDelegate.kt).

Your `Activity` should extend AndroidX's [`AppCompatActivity`](https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity). In its simplest form, your `Activity` will look like:

`MainActivity.kt`:
```kotlin
class MainActivity : AppCompatActivity(), TurbolinksActivity {
    override lateinit var delegate: TurbolinksActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        delegate = TurbolinksActivityDelegate(this, R.id.main_nav_host)
    }
}
```

Note that `R.layout.activity_main` refers to the layout file that you already created. `R.id.main_nav_host` refers to the `MainSessionNavHostFragment` hosted in the layout file.

Refer to [MainActivity](demoapp_simple/src/main/kotlin/com/basecamp/turbolinks/demosimple/main/MainActivity.kt) and feel free to copy that as a starting point. (Don't forget to add your `Activity` to your app's `AndroidManifest.xml` file.)

### Create a Configuration
A configuration file specifies the set of rules Turbolinks will follow to navigate and present views. It has two sections: 

1. Application-level settings
1. Path-specific rules 

Typically path-specific rules will do most of the work in determining how and when a particular URI will be navigated to, but additional application-level settings can also be applied to customize navigation logic (see [Create a destination interface](#create-a-destination-interface)).

At minimum you will need a [src/main/assets/json/configuration.json](/demoapp_simple/src/main/assets/json/configuration.json) file that Turbolinks can read, with at least one path configuration. Note that the configuration file is processed in order and cascades downward, similar to CSS. The top most declaration should establish the default behavior for all path patterns, while each subsequent rule can override for specific behavior.

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

The `pattern` array defines a Regex pattern that will be used to determine if a provided URI matches (and as a result, which `properties` should be applied).

#### Properties

The `properties` object contains a handful of key/value pairs that Turbolinks supports out of the box. You are free to add more properties as your app needs, but these are the ones the framework is aware of and will handle automatically.

* `uri` — The target destination to navigate to. Must map to an Activity or Fragment that has implemented the [TurbolinksNavGraphDestination](/turbolinks/src/main/kotlin/com/basecamp/turbolinks/nav/TurbolinksNavGraphDestination.kt) annotation with a matching `uri` value.
	* **Required**. 
	* No explicit value options. No default value.
* `context` — Specifies the presentation context in which the view should be displayed. Turbolinks will determine what the navigation behavior should be based on this value + the `presentation` value. Unless you are specifically showing a modal-style view (e.g., a form, wizard, navigation, etc.), `default` is usually sufficient. 
	* Optional. 
	* Possible values: `default` or `modal`. Defaults to `default`. 
* `presentation` — Specifies what "action" to use to present the given `uri`. Turbolinks will determine what the navigation behavior should be based on this value + the `context` value. In most cases `default` should be sufficient, but you may find cases where your app needs specific beahvior. 
	* Optional. 
	* Possible values: `default`, `push`, `pop`, `replace`, `replace_root`, `clear_all`, `refresh`, `none`. Defaults to `default`.
* `fallback_uri` — Provides a fallback URI in case a destination cannot be found that maps to the `uri`. Can be useful in cases when pointing to a new `uri` that may not be available yet.
	* Optional.
	* No explicit value options. No default value. 
* `pull_to_refresh_enabled` — Whether or not pull to refresh should be enabled for a given path.
	* Optional.
	* Possible values: `true`, `false`. Defaults to `false`.
* `screenshots_enabled` — Whether or not transitional screenshots (when returning to a previous screen) should be used. This gives the appearance of a much faster experience going back, but does require more performance overhead. 
	* Optional.
	* Possible values: `true`, `false`. Defaults to `true`.

### Create a Fragment Layout
You'll need a basic layout for your fragment to inflate. Refer to [fragment_web.xml](/demoapp_simple/src/main/res/layout/fragment_web.xml) and feel free to copy that as a starting point.

The most important thing is that your layout `include` a reference to the [turbolinks_default](turbolinks/src/main/res/layout/turbolinks_default.xml) resource. This is a view provided by the library which automatically add the necessary view hierarchy that Turbolinks expects for attaching a WebView, progress view, and error view.

```xml
<include
    layout="@layout/turbolinks_default"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintTop_toBottomOf="@+id/app_bar" />
```

### Create a Fragment
You'll need at least one fragment to represent the main body of your view. 

A [WebFragment](demoapp_simple/src/main/kotlin/com/basecamp/turbolinks/demosimple/features/web/WebFragment.kt) would be a good option to handle all standard WebViews in your app. This fragment:

* Should implement your custom destination interface as mentioned above, or, if you haven't created one, simply implement [TurbolinksNavDestination](turbolinks/src/main/kotlin/com/basecamp/turbolinks/nav/TurbolinksNavDestination.kt).
* Must extend one of the base fragments provided by Turbolinks. In this case, as a web fragment, you should extend [TurbolinksWebFragment](turbolinks/src/main/kotlin/com/basecamp/turbolinks/fragments/TurbolinksWebFragment.kt).

Refer to [WebFragment](demoapp_simple/src/main/kotlin/com/basecamp/turbolinks/demosimple/features/web/WebFragment.kt) as an example and feel free to copy it as a starting point. It outlines the very basics of what every fragment will need to implement — a nav graph annotation, inflating a view, setting up progress and error views, and setting up a toolbar.

🎉 **Congratulations, you're using Turbolinks on Android!** 🎉

## Additional Configuration

### Advanced use with Multiple Activities
You may encounter situations where a truly single-`Activity` app may not be feasible. For example, you may need an `Activity` for logged-out state and a separate `Activity` for logged-in state.

In such cases, you simply need to create an additional `Activity` that also implements the `TurbolinksActivity` interface. You will need to be sure to register each `Activity` by calling [TurbolinksSessionNavHostFragment.registeredActivities()](turbolinks/src/main/kotlin/com/basecamp/turbolinks/session/TurbolinksSessionNavHostFragment.kt) so that you can navigate between them.

## Running the Demo App

The demo apps bundled with the library work best with the [Turbolinks Demo App](https://github.com/basecamp/turbolinks-demo). You can follow the instructions in that repo to start up the server.

### Start the Demo Android App

- Ensure the demo server and Android device are on the same network.
- Start up the demo server per its instructions.
- Go to [Contants.kt](demoapp/src/main/kotlin/com/basecamp/turbolinks/demo/util/Constants.kt).
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
