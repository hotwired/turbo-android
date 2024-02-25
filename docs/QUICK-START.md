# Quick Start Guide

## Contents

1. [Create a NavHostFragment](#create-a-navhostfragment)
1. [Create an Activity](#create-an-activity)
1. [Create a Web Fragment](#create-a-web-fragment)
1. [Create a Path Configuration](#create-a-path-configuration)

## Create a NavHostFragment

A [`NavHostFragment`](https://developer.android.com/reference/androidx/navigation/fragment/NavHostFragment) is a component available in [Android Jetpack](https://developer.android.com/jetpack) and is primarily responsible for providing "an area in your layout for self-contained navigation to occur."

The Turbo extension of this class, `TurboSessionNavHostFragment`, along with being responsible for self-contained `TurboFragment` navigation, also manages a `TurboSesssion` and a `TurboWebView` instance. You will need to implement a few things for this abstract class:

- The name of the `TurboSession` (this is arbitrary, but must be unique in your app)
- The url of a starting location when your app starts up. Note: if you're running your app locally without HTTPS, you'll need to adjust your `android:usesCleartextTraffic` settings in the AndroidManifest.xml (or use an Android Network security configuration), and target [`10.0.2.2` instead of `localhost`](https://developer.android.com/studio/run/emulator-networking).
- A list of registered activities that Turbo will be able to navigate to (optional)
- A list of registered fragments that Turbo will be able to navigate to
- The location of your `TurboPathConfiguration` JSON file(s) to configure navigation rules

In its simplest form, the implementation of your `TurboSessionNavHostFragment` will look like:

**`MainSessionNavHostFragment`:**
```kotlin
import dev.hotwire.turbo.session.TurboSessionNavHostFragment

class MainSessionNavHostFragment : TurboSessionNavHostFragment() {
    override val sessionName = "main"

    override val startLocation = "https://turbo-native-demo.glitch.me/"

    override val registeredActivities: List<KClass<out AppCompatActivity>>
        get() = listOf(
            // Leave empty unless you have more
            // than one TurboActivity in your app
        )

    override val registeredFragments: List<KClass<out Fragment>>
        get() = listOf(
            WebFragment::class
            // And any other TurboFragments in your app
        )

    override val pathConfigurationLocation: TurboPathConfiguration.Location
        get() = TurboPathConfiguration.Location(
            assetFilePath = "json/configuration.json",
            remoteFileUrl = "https://turbo.hotwired.dev/demo/configurations/android-v1.json"
        )
}
```

See the [Fragment section](#create-a-web-fragment) below to create a `TurboFragment` that you'll register here. See the [Path Configuration documentation](PATH-CONFIGURATION.md) to create your path configuration file(s).

Refer to the demo [`MainSessionNavHostFragment`](../demo/src/main/kotlin/dev/hotwire/turbo/demo/main/MainSessionNavHostFragment.kt) for an example.

## Create an Activity

It's strongly recommended to use a single-Activity architecture in your app. Generally, you'll have one `TurboActivity` and many `TurboFragments`.

### Create the TurboActivity layout resource

You need to create a layout resource file that your `TurboActivity` will use to host the `TurboSessionNavHostFragment` that you created above.

Android Jetpack provides a [`FragmentContainerView`](https://developer.android.com/reference/androidx/fragment/app/FragmentContainerView) to contain `NavHostFragment` navigation. In its simplest form, your Activity layout file will look like:

**`res/layout/activity_main.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/main_nav_host"
        android:name="dev.hotwire.turbo.demo.main.MainSessionNavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:defaultNavHost="false" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Refer to the demo [`activity_main.xml`](../demo/src/main/res/layout/activity_main.xml) for an example.

### Create the TurboActivity class

A Turbo Activity is straightforward and needs to implement the [`TurboActivity`](../turbo/src/main/kotlin/dev/hotwire/turbo/activities/TurboActivity.kt) interface in order to provide a [`TurboActivityDelegate`](../turbo/src/main/kotlin/dev/hotwire/turbo/delegates/TurboActivityDelegate.kt).

Your Activity should extend Android Jetpack's [`AppCompatActivity`](https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity). In its simplest form, your Activity will look like:

**`MainActivity.kt`:**

```kotlin
class MainActivity : AppCompatActivity(), TurboActivity {
    override lateinit var delegate: TurboActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        delegate = TurboActivityDelegate(this, R.id.main_nav_host)
    }
}
```

_Note that `R.layout.activity_main` refers to the Activity layout file that you already created. `R.id.main_nav_host` refers to the `MainSessionNavHostFragment` that you created, hosted in the layout file._

Refer to the demo [`MainActivity`](../demo/src/main/kotlin/dev/hotwire/turbo/demo/main/MainActivity.kt) as an example. (Don't forget to add your Activity to your app's [`AndroidManifest.xml`](../demo/src/main/AndroidManifest.xml) file.)

## Create a Web Fragment

### Create the TurboWebFragment class

You'll need at least one web Fragment that will serve as a destination for urls that display web content in your app.

A web Fragment is straightforward and needs to implement the [`TurboWebFragment`](../turbo/src/main/kotlin/dev/hotwire/turbo/fragments/TurboWebFragment.kt) abstract class. This abstract class implements the [`TurboWebFragmentCallback`](../turbo/src/main/kotlin/dev/hotwire/turbo/fragments/TurboWebFragmentCallback.kt) interface, which provides a number of functions available to customize your Fragment.

You'll need to annotate each Fragment in your app with a `@TurboNavGraphDestination` annotation with a URI of your own scheme. This URI is used by the library to build an internal navigation graph and map url path patterns to the destination Fragment with the corresponding URI. See the [Path Configuration documentation](PATH-CONFIGURATION.md) to learn how to map url paths to destination Fragments.

In its simplest form, your web Fragment will look like:

**`WebFragment.kt`:**
```kotlin
@TurboNavGraphDestination(uri = "turbo://fragment/web")
class WebFragment : TurboWebFragment()
```

The library automatically inflates a default `R.layout.turbo_fragment_web` layout to host a `TurboView`. If you'd like to create your own custom layout for your web Fragment, you can override the `onCreateView()` function and inflate your own layout. See the demo [`WebHomeFragment`](../demo/src/main/kotlin/dev/hotwire/turbo/demo/features/web/WebHomeFragment.kt) as an example of of providing your own layout.

You can also provide your own custom progress view or error view by overriding the `createProgressView()` and `createErrorView()` functions in your web Fragment.

Refer to demo [`WebFragment`](../demo/src/main/kotlin/dev/hotwire/turbo/demo/features/web/WebFragment.kt) as an example.

## Create a Path Configuration

See the documentation to learn about setting up your [path configuration](PATH-CONFIGURATION.md)

## Navigation

See the documenation to learn about [navigating between destinations](NAVIGATION.md).

## Advanced Options

See the documentation to [learn about the advanced options available](ADVANCED-OPTIONS.md).
