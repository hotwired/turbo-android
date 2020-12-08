# Getting Started

## Contents

1. [Create a NavHostFragment](#create-a-navhostfragment)
1. [Create an Activity](#create-an-activity)
1. [Create a Web Fragment](#create-a-web-fragment)
1. [Create a Path Configuration](#create-a-path-configuration)

## Create a NavHostFragment
A [NavHostFragment](https://developer.android.com/reference/androidx/navigation/fragment/NavHostFragment) is a component available in AndroidX and is primarily responsible for providing "an area in your layout for self-contained navigation to occurr."

The Turbo extension of this class, `TurboSessionNavHostFragment`, along with being responsible for self-contained `TurboFragment` navigation, also manages a `TurboSesssion` and a `TurboWebView` instance. You will need to implement just a few things for this abstract class.

* The name of the `TurboSession` (this is abitrary, but must be unique)
* The url of a starting location when your app starts up
* A list of registered activities that Turbo will be able to navigate to (optional)
* A list of registered fragments that Turbo will be able to navigate to
* The location of your `TurboPathConfiguration` JSON file(s) to configure navigation

In its simplest form, the implementation of your `TurbolinksSessionNavHostFragment` will look like:

**`MainSessionNavHostFragment`:**
```kotlin
class MainSessionNavHostFragment : TurbolinksSessionNavHostFragment() {
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
It's strongly recommended to use a single-`Activity` architecture in your app. Generally, you'll have one `TurboActivity` and many `TurboFragments`.

### Create the TurboActivity layout resource
You need to create a layout resource file that your `TurboActivity` will use to host the `TurboSessionNavHostFragment` that you created above.

AndroidX provides a [`FragmentContainerView`](https://developer.android.com/reference/androidx/fragment/app/FragmentContainerView) to contain `NavHostFragment` navigation. In its simplest form, your layout file will look like:

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
        android:name="com.basecamp.turbolinks.demosimple.main.MainSessionNavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:defaultNavHost="false" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Refer to the demo [`activity_main.xml`](../demoapp_simple/src/main/res/layout/activity_main.xml) for an example.

### Create the TurboActivity class

A Turbolinks `Activity` is straightforward and simply need to implement the [TurbolinksActivity](turbolinks/src/main/kotlin/com/basecamp/turbolinks/activities/TurbolinksActivity.kt) interface in order to provide a [TurbolinksActivityDelegate](turbolinks/src/main/kotlin/com/basecamp/turbolinks/delegates/TurbolinksActivityDelegate.kt).

Your `Activity` should extend AndroidX's [`AppCompatActivity`](https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity). In its simplest form, your `Activity` will look like:

**`MainActivity.kt`:**
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

Refer to [MainActivity](../demoapp_simple/src/main/kotlin/com/basecamp/turbolinks/demosimple/main/MainActivity.kt) and feel free to copy that as a starting point. (Don't forget to add your `Activity` to your app's `AndroidManifest.xml` file.)

## Create a Web Fragment

### Create the TurboWebFragment layout
You'll need a basic layout for your fragment to inflate. Refer to [fragment_web.xml](../demoapp_simple/src/main/res/layout/fragment_web.xml) and feel free to copy that as a starting point.

The most important thing is that your layout `include` a reference to the [turbolinks_default](turbolinks/src/main/res/layout/turbolinks_default.xml) resource. This is a view provided by the library which automatically add the necessary view hierarchy that Turbolinks expects for attaching a WebView, progress view, and error view.

```xml
<include
    layout="@layout/turbolinks_default"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintTop_toBottomOf="@+id/app_bar" />
```

### Create the TurboWebFragment class
You'll need at least one fragment to represent the main body of your view. 

A [WebFragment](../demoapp_simple/src/main/kotlin/com/basecamp/turbolinks/demosimple/features/web/WebFragment.kt) would be a good option to handle all standard WebViews in your app. This fragment:

* Should implement your custom destination interface as mentioned above, or, if you haven't created one, simply implement [TurbolinksNavDestination](turbolinks/src/main/kotlin/com/basecamp/turbolinks/nav/TurbolinksNavDestination.kt).
* Must extend one of the base fragments provided by Turbolinks. In this case, as a web fragment, you should extend [TurbolinksWebFragment](turbolinks/src/main/kotlin/com/basecamp/turbolinks/fragments/TurbolinksWebFragment.kt).

Refer to [WebFragment](../demoapp_simple/src/main/kotlin/com/basecamp/turbolinks/demosimple/features/web/WebFragment.kt) as an example and feel free to copy it as a starting point. It outlines the very basics of what every fragment will need to implement — a nav graph annotation, inflating a view, setting up progress and error views, and setting up a toolbar.

## Create a Path Configuration
A configuration file specifies the set of rules Turbolinks will follow to navigate and present views. It has two sections: 

1. Application-level settings
1. Path-specific rules 

Typically path-specific rules will do most of the work in determining how and when a particular URI will be navigated to, but additional application-level settings can also be applied to customize navigation logic (see [Create a destination interface](#create-a-destination-interface)).

At minimum you will need a [src/main/assets/json/configuration.json](../demoapp_simple/src/main/assets/json/configuration.json) file that Turbolinks can read, with at least one path configuration. Note that the configuration file is processed in order and cascades downward, similar to CSS. The top most declaration should establish the default behavior for all path patterns, while each subsequent rule can override for specific behavior.

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

### Patterns

The `pattern` array defines a Regex pattern that will be used to determine if a provided URI matches (and as a result, which `properties` should be applied).

### Properties

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

## 🎉 Congratulations, you're using Turbolinks on Android! 🎉