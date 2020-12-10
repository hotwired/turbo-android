# Getting Started

## Contents

1. [Create a NavHostFragment](#create-a-navhostfragment)
1. [Create an Activity](#create-an-activity)
1. [Create a Web Fragment](#create-a-web-fragment)
1. [Create a Path Configuration](#create-a-path-configuration)

## Create a NavHostFragment
A [NavHostFragment](https://developer.android.com/reference/androidx/navigation/fragment/NavHostFragment) is a component available in [Android Jetpack](https://developer.android.com/jetpack) and is primarily responsible for providing "an area in your layout for self-contained navigation to occurr."

The Turbo extension of this class, `TurboSessionNavHostFragment`, along with being responsible for self-contained `TurboFragment` navigation, also manages a `TurboSesssion` and a `TurboWebView` instance. You will need to implement a few things for this abstract class:

* The name of the `TurboSession` (this is abitrary, but must be unique in your app)
* The url of a starting location when your app starts up
* A list of registered activities that Turbo will be able to navigate to (optional)
* A list of registered fragments that Turbo will be able to navigate to
* The location of your `TurboPathConfiguration` JSON file(s) to configure navigation rules

In its simplest form, the implementation of your `TurboSessionNavHostFragment` will look like:

**`MainSessionNavHostFragment`:**
```kotlin
class MainSessionNavHostFragment : TurboSessionNavHostFragment() {
    override val sessionName = "main"

    override val startLocation
        get() = "https://hotwire.dev/turbo/demo"

    override val registeredActivities: List<KClass<out Activity>>
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
            assetFilePath = "json/configuration.json"
            remoteFileUrl = "https://hotwire.dev/turbo/demo/config/android-v1.json"
        )
}
```

See the [Fragment section](#create-a-web-fragment) below to create a `TurboFragment` that you'll register here. See the [Path Configuration section](#create-a-path-configuration) below to create your path configuration file(s).

Refer to the demo [MainSessionNavHostFragment](../demoapp_simple/src/main/kotlin/com/basecamp/turbolinks/demosimple/main/MainSessionNavHostFragment.kt) for an example.

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
        android:name="dev.hotwire.turbo.demosimple.main.MainSessionNavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:defaultNavHost="false" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Refer to the demo [`activity_main.xml`](../demoapp_simple/src/main/res/layout/activity_main.xml) for an example.

### Create the TurboActivity class

A Turbo Activity is straightforward and simply needs to implement the [TurboActivity](../turbolinks/src/main/kotlin/com/basecamp/turbolinks/activities/TurbolinksActivity.kt) interface in order to provide a [TurboActivityDelegate](../turbolinks/src/main/kotlin/com/basecamp/turbolinks/delegates/TurbolinksActivityDelegate.kt).

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

*Note that `R.layout.activity_main` refers to the Activity layout file that you already created. `R.id.main_nav_host` refers to the `MainSessionNavHostFragment` that you created, hosted in the layout file.*

Refer to the demo [MainActivity](../demoapp_simple/src/main/kotlin/com/basecamp/turbolinks/demosimple/main/MainActivity.kt) as an example. (Don't forget to add your Activity to your app's [`AndroidManifest.xml`](../demoapp_simple/src/main/AndroidManifest.xml) file.)

## Create a Web Fragment

### Create the TurboWebFragment layout resource
You need to create a layout resource file that your `TurboWebFragment` will use to inflate a `TurboView` that the library provides.

The easiest way to include a `TurboView` in your layout resource is to `<include ... />` a reference to the library's [`turbo_default.xml`](../turbolinks/src/main/res/layout/turbolinks_default.xml) resource. This is a view provided by the library which automatically adds the necessary view hierarchy that Turbo expects for attaching a WebView, progress view, and error view.

In its simplest form, your web Fragment layout file will look like:

**`res/layout/fragment_web.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <include
        layout="@layout/turbo_default"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Refer to demo [`fragment_web.xml`](../demoapp_simple/src/main/res/layout/fragment_web.xml) for an example.

### Create the TurboWebFragment class
You'll need at least one web Fragment that will serve as a destination for urls that display web content in your app. 

A web Fragment is straightforward and simply needs to implement the [TurboWebFragment](../turbolinks/src/main/kotlin/com/basecamp/turbolinks/fragments/TurbolinksWebFragment.kt) abstract class. This abstract class implements the [`TurboWebFragmentCallback`](../turbolinks/src/main/kotlin/com/basecamp/turbolinks/fragments/TurbolinksWebFragmentCallback.kt) interface, which provides a number of functions available to customize your Fragment.

You'll also need to annotate each Fragment in your app with a `@TurboNavGraphDestination` annotation with a URI of your own scheme. This URI is used by the library to build an internal navigation graph and map url path patterns to the destination Fragment with the corresponding URI. See the [Path Configuration section](#create-a-path-configuration) below to learn how to map url paths to destination Fragments.

In its simplest form, your web Fragment will look like:

**`WebFragment.kt`:**
```kotlin
@TurboNavGraphDestination(uri = "turbo://fragment/web")
class WebFragment : TurboWebFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_web, container, false)
    }

    override fun createProgressView(location: String): View {
        return layoutInflater.inflate(R.layout.progress, null)
    }

    override fun createErrorView(statusCode: Int): View {
        return layoutInflater.inflate(R.layout.error, null)
    }

    override fun toolbarForNavigation(): Toolbar? {
        return view?.findViewById(R.id.toolbar)
    }
}

// TODO: Let's try to provide default library views error/progress views that can be overridden with 
// custom views. Let's also see if it makes sense to provide a `turbo_*.xml` layout resource file 
// that contains an AppBarLayout with a Toolbar so it's easier to get an app up and running.
```

*Note that `R.layout.fragment_web` refers to the Fragment layout file that you already created.*

Refer to demo [WebFragment](../demoapp_simple/src/main/kotlin/com/basecamp/turbolinks/demosimple/features/web/WebFragment.kt) as an example.

## Create a Path Configuration
A JSON configuration file specifies the set of rules Turbo will follow to navigate to Fragment destinations and configure options. It has two top-level objects: 

1. Application-level `"settings"`
1. Url path-specific `"rules"`

At minimum, you will need a bundled [`src/main/assets/json/configuration.json`](../demoapp_simple/src/main/assets/json/configuration.json) file in your app that Turbo can read. We also recommend hosting a remote configuration file on your server, so you can update the app's configuration at any time without needing an app update. Remote configuration files are fetched (and cached) on every app startup, so the app always has the latest configuration available. The location of these configuration files needs to be set in your [`TurboSessionNavHostFragment.pathConfigurationLocation`](#create-a-navhostfragment). 

In its simplest form, your JSON configuration will look like:

**`assets/json/configuration.json`:**
```json
{
  "settings": {
    "screenshots_enabled": true
  },
  "rules": [
    {
      "patterns": [
        ".*"
      ],
      "properties": {
        "context": "default",
        "uri": "turbo://fragment/web",
        "pull_to_refresh_enabled": true
      }
    }
  ]
}
```

### Settings
The `settings` object is a place to configure app-level settings. This is extremely useful when you have a remote configuration file, since you can add your own custom settings and use them as remote feature-flags. Available settings are:
* `screenshots_enabled` — Whether or not transitional web screenshots should be used during navigation. This gives the appearance of a more smooth experience since the session WebView is swapped between web destination Fragments, but does require more performance overhead. 
	* Optional.
	* Possible values: `true`, `false`. Defaults to `true`.
* Any custom app settings that you'd like to configure here

### Rules
The `"rules"` array defines a list of rules that are processed in order and cascade downward, similar to CSS. The top-most declaration should establish the default behavior for all url path patterns, while each subsequent rule can override for specific behavior.

#### Patterns

The `patterns` array defines Regex patterns that will be used to match url paths (and as a result, which `properties` should be applied for a particular path).

#### Properties

The `properties` object contains a handful of key/value pairs that Turbolinks supports out of the box. You are free to add more properties as your app needs, but these are the ones the framework is aware of and will handle automatically.

* `uri` — The target destination URI to navigate to. Must map to an Activity or Fragment that has implemented the [TurboNavGraphDestination](../turbolinks/src/main/kotlin/com/basecamp/turbolinks/nav/TurbolinksNavGraphDestination.kt) annotation with a matching `uri` value.
	* **Required**. 
	* No explicit value options. No default value.
* `context` — Specifies the presentation context in which the view should be displayed. Turbo will determine what the navigation behavior should be based on this value + the `presentation` value. Unless you are specifically showing a modal-style view (e.g., a form, wizard, navigation, etc.), `default` is usually sufficient. 
	* Optional. 
	* Possible values: `default` or `modal`. Defaults to `default`. 
* `presentation` — Specifies what style to use when presenting the given `uri` destination. Turbo will determine what the navigation behavior should be based on this value + the `context` value. In most cases `default` should be sufficient, but you may find cases where your app needs specific beahvior. 
	* Optional. 
	* Possible values: `default`, `push`, `pop`, `replace`, `replace_root`, `clear_all`, `refresh`, `none`. Defaults to `default`.
* `fallback_uri` — Provides a fallback URI in case a destination cannot be found that maps to the `uri`. Can be useful in cases when pointing to a new `uri` that may not be available yet in older versions of the app.
	* Optional.
	* No explicit value options. No default value. 
* `pull_to_refresh_enabled` — Whether or not pull-to-refresh should be enabled for the given path.
	* Optional.
	* Possible values: `true`, `false`. Defaults to `false`.

Refer to demo [`configuration.json`](../demoapp_simple/src/main/assets/json/configuration.json) as an example.

## Navigate to Destinations
See the documenation to learn about [navigating between destinations](NAVIGATION.md).

## Advanced Configuration
See the documentation to [learn about the advanced configuration options available](ADVANCED-CONFIGURATION.md).

## 🎉 Congratulations, you're using Turbo on Android! 🎉