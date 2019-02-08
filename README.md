# Turbolinks Android
[![Build Status on Travis:](https://travis-ci.org/turbolinks/turbolinks-android.svg?branch=master)](https://travis-ci.org/turbolinks/turbolinks-android) [ ![Download](https://api.bintray.com/packages/basecamp/maven/turbolinks-android/images/download.svg) ](https://bintray.com/basecamp/maven/turbolinks-android/_latestVersion)

Turbolinks Android is a native adapter for any [Turbolinks 5](https://github.com/turbolinks/turbolinks#readme) enabled web app. It's built entirely using standard Android tools and conventions.

This library has been in use and tested in the wild since November 2015 in the all-new [Basecamp 3 for Android](https://play.google.com/store/apps/details?id=com.basecamp.bc3).

Our goal for this library was that it'd be easy on our fellow programmers:

- **Easy to start**: one jCenter dependency, one custom view, one adapter interface to implement. No other requirements.
- **Easy to use**: full access to the TurbolinksSession, along with a convenience default instance.
- **Easy to understand**: tidy code backed by solid documentation via [Javadocs](http://turbolinks.github.io/turbolinks-android/) and this README.

## Contents

1. [Installation](#installation-one-step)
1. [Getting Started](#getting-started-three-steps)
1. [Advanced Configuration](#advanced-configuration)
1. [Running the Demo App](#running-the-demo-app)
1. [Contributing](#contributing)

## Installation (One Step)
Add the dependency from jCenter to your app's (not project) `build.gradle` file.

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.basecamp:turbolinks:1.0.6'
}
```

## Getting Started (Three Steps)

### Prerequisites

1. We recommend using Turbolinks from an activity or an extension of your activity, like a custom controller. This library hasn't been tested with Android Fragments (we don't use them). We'd recommend avoiding Fragments with this library, as they might produce unintended results.
2. Android API 19+ is required as the `minSdkVersion` in your build.gradle.
3. In order for a  [WebView](https://developer.android.com/reference/android/webkit/WebView.html) to access the Internet and load web pages, your application must have the `INTERNET` permission. Make sure you have `<uses-permission android:name="android.permission.INTERNET" />` in your Android manifest.

### 1. Add TurbolinksView to a Layout

In your activity's layout, insert the `TurbolinksView` custom view.

`TurbolinksView` extends `FrameLayout`, so all of its standard attributes are available to you.

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.basecamp.turbolinks.TurbolinksView
        android:id="@+id/turbolinks_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</LinearLayout>
```

### 2. Implement the TurbolinksAdapter Interface

Implement the `TurbolinksAdapter` interface in your activity, which will require implementing a handful of callback methods. These callbacks are [outlined in greater detail below](#handling-adapter-callbacks).

Right off the bat, you don't need to worry about handling every callback, especially if you're starting off with a simple app. Most can be left as empty methods for now.

**But at the very minimum, you must handle the [visitProposedToLocationWithAction](#visitproposedtolocationwithaction)**. Otherwise your app won't know what to do when a link is clicked inside a WebView.

Beyond this README, you can get a good feel for the callbacks from the [Javadoc](http://turbolinks.github.io/turbolinks-android/) and the [demo app](/demoapp).

### 3. Get the Default TurbolinksSession and Visit a Location

From your activity that implements the `TurbolinksAdapter` interface, here's how you tell Turbolinks to visit a location:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    // Standard activity boilerplate here...

    // Assumes an instance variable is defined. Find the view you added to your
    // layout in step 1.
    turbolinksView = (TurbolinksView) findViewById(R.id.turbolinks_view);

    TurbolinksSession.getDefault(this)
                     .activity(this)
                     .adapter(this)
                     .view(turbolinksView)
                     .visit("https://basecamp.com");
}
```

🎉**Congratulations, you're using Turbolinks on Android!** 👏

## Advanced Configuration

### Handling Adapter Callbacks

The `TurbolinksAdapter` class provides callback events directly from the WebView and Turbolinks itself. This gives you the opportunity to intercept those events and inject your own native actions -- things like routing logic, displaying UI elements, and error handling.

As mentioned earlier, you must implement `visitProposedToLocationWithAction`, or your app won't know what to do when a link is clicked inside a WebView.

You can of course choose to leave the rest of the adapter callbacks blank, but we'd recommend implementing the two error handling callbacks (`onReceivedError` and `requestFailedWithStatusCode`) for when things go wrong.

#### visitProposedToLocationWithAction

This is a callback from Turbolinks telling you that a visit has been proposed and is about to begin. **This is the most important callback that you must implement.**

This callback provides your app the opportunity to figure out what it should do and where it should go. At the very minimum, you can create an `Intent` to open another `Activity` that fires another Turbolinks call with the provided location, like so:

```java
Intent intent = new Intent(this, MainActivity.class);
intent.putExtra(INTENT_URL, location);
this.startActivity(intent);
```

In more complex apps, you'll most likely want to do some routing logic here. Should you open another WebView Activity? Should you open a native Activity in certain cases? This is the place to do that logic.

#### onPageFinished

This is a callback that's executed at the end of the standard [WebViewClient's onPageFinished](http://developer.android.com/reference/android/webkit/WebViewClient.html#onPageFinished(android.webkit.WebView, java.lang.String)) method.

This callback will only be fired once upon cold booting. If there is any action you need to take after the first full page load is complete, just once, this is the place to do it.

The reason this is only called once is because the first location that Turbolinks loads after initialization is always a "cold boot" -- a full page load of all resources that's executed through a normal `WebView.loadUrl(url)`. Every subsequent location visit (with the exception of an error condition or page invalidation) will fire through Turbolinks, without a full page load.

#### visitCompleted

This is a callback from Turbolinks telling you it considers the visit completed. The request has been fulfilled successfully and the page fully rendered.

It's similar conceptually to onPageFinished, except this callback will be called for every Turbolinks visit. This is a good time to take actions that you need on every page, such as reading data-attributes (or other metadata) from the loaded page.

#### onReceivedError

This is a callback that's executed at the end of the standard [WebViewClient's onReceivedError](http://developer.android.com/reference/android/webkit/WebViewClient.html#onReceivedError(android.webkit.WebView, int, java.lang.String, java.lang.String)) method.

**We recommend you implement this method.** Otherwise, your user will see an endless progress view/spinner without something that handles the error. You can handle the error however you like -- send the user to a different page, show a native error screen, etc.

#### requestFailedWithStatusCode

This is a callback from Turbolinks telling you that an XHR request has failed.

**We recommend you implement this method.** Otherwise, your user will see an endless progress view/spinner without something that handles the error. You can handle the error however you like -- send the user to a different page, show a native error screen, etc.

#### pageInvalidated

This is a callback from Turbolinks telling you that a change has been detected in a resource/asset in the `<HEAD>`, and as a result the Turbolinks state has been invalidated. Most likely the web app has been updated while the app was using it.

The library will automatically fall back to cold booting the location (which it must do since resources have been changed) and then will notify you via this callback that the page was invalidated. This is an opportunity for you to clean up any UI state that you might have lingering around that may no longer be valid (title data, etc.)

### Overriding Default TurbolinksSession Settings
There are some optional features in TurbolinksSession that are enabled by default.

#### Bitmap Screenshots
When the Turbolinks `WebView` is detached from an activity, a bitmap screenshot of the view is automatically created and later displayed when the activity is resumed - until a copy of the page is restored from cache in the `WebView`. This gives the illusion that the `WebView` was never detached and no visual flicker is seen. To disable this behavior, simply call:
```java
turbolinksSession.setScreenshotsEnabled(false);
```

#### Pull To Refresh
Refreshes the TurbolinksView when a user swipes down from the top of the view.
To disable simply call:
```java
turbolinksSession.setPullToRefreshEnabled(false);
```

### Custom Instance(s) of TurbolinksSession

We provide a single, reusable instance of TurbolinksSession that you can access through this convenience method:

```java
TurbolinksSession turbolinksSession = TurbolinksSession.getDefault(context);
```

If you need greater control, you can always create your own instance(s) with:

```java
TurbolinksSession myTurbolinksSession = TurbolinksSession.getNew(context);
```

Some things to keep in mind if you create your own instance of TurbolinksSession:

- You'll be responsible for managing the lifecycle of the TurbolinksSession instance you create and ensure it's being reused for each subsequent Turbolinks call. In other words, if you call `.getNew(context)` accidentally on every visit, you'll be getting a new session instance and cold-booting every time, thereby defeating the purpose of Turbolinks.
- The best places to manage the lifecycle of your instance is most likely in one of two places:
  - A singleton helper class that instantiates once/returns always
  - A custom object that extends `Application`

You'll need to weigh the benefits and complexities of those options, but the bottom line is that you'll want to carefully manage the lifecycle of your Turbolinks instance(s).

### Custom Progress View

By default the library will provide you with a progress view with a progress bar -- a simple `FrameLayout` that covers the `WebView` while it's loading, and shows a spinner after 500ms.

If that doesn't meet your needs, you can also pass in your own custom progress view like so:

```java
TurbolinksSession.getDefault(this)
                 .activity(this)
                 .adapter(this)
                 .progressView(progressView, resourceIdOfProgressBar, progressBarDelay)
                 .view(turbolinksView)
                 .visit("https://basecamp.com");
```

Some notes about using a custom progress view:

- It doesn't matter what kind of layout view you use, but you'll want to do something that covers the entire `WebView` and uses `match_parent` for the height and width.
- We ask you to provide the resource ID of the progress bar *inside your progress view* so that we can internally handle when to display it. The library has a mechanism that can delay showing the progress bar to improve perceived loading times (a slight delay in showing the progress bar makes apps feel faster), so we need a handle to that view.
- In conjunction with the progress bar resource ID, you can also specify the delay in milliseconds before it's displayed. The default progress bar shows after 500 ms.

### Custom WebView WebSettings

By default the library sets some minimally intrusive WebSettings on the shared WebView. Some are required while others serve as reasonable defaults for modern web applications. They are:

- `setJavaScriptEnabled(true)` (required)
- `setDomStorageEnabled(true)`
- `setDatabaseEnabled(true)`

If however these are not to your liking, you can always override them. The `WebView` is always available to you via `getWebView()`, and you can update the `WebSettings` to your liking:

```java
WebSettings settings = TurbolinksSession.getDefault().getWebView().getSettings();
settings.setDomStorageEnabled(false);
settings.setAllowFileAccess(false);
```

**If you do update the WebView settings, be sure not to override `setJavaScriptEnabled(false)`. Doing so would break Turbolinks, which relies heavily on JavaScript.**

### Custom JavascriptInterfaces

If you have custom JavaScript on your pages that you want to access as JavascriptInterfaces, you can add them like so:

```java
TurbolinksSession.getDefault().addJavascriptInterface(this, "MyCustomJavascriptInterface");
```

The Java object being passed in can be anything, as long as it has at least one method annotated with `@android.webkit.JavascriptInterface`. Names of interfaces must be unique, or they will be overwritten in the library's map.

## Running the Demo App

A demo app is bundled with the library, and works in two parts:

1. A tiny Turbolinks-enabled Sinatra web app that you can run locally.
1. A tiny Android app that connects to the Sinatra web app.

### Prerequisites

- [Ruby installed](https://www.ruby-lang.org/en/downloads/)
- [RubyGems installed](https://rubygems.org/pages/download)
- [Bundler installed](http://bundler.io/)
- [Sinatra installed](http://www.sinatrarb.com/)
- [Rack installed](https://github.com/rack/rack#installing-with-rubygems)
- Your IP address

### Start the Demo Sinatra Web App

- From the command line, change directories to `<project-root>/demoapp/server`.
- Run `bundle`
- Run `rackup --host 0.0.0.0`

You should see a message saying what port the demo web app is running on. It usually looks like:

`Listening on 0.0.0.0:9292`

### Start the Demo Android App

- Ensure your web app and Android device are on the same network.
- Go to [`MainActivity`](/demoapp/src/main/java/com/basecamp/turbolinks/demo/MainActivity.java).
- Find the `BASE_URL` String at the top of the class. Change the IP and port of that string to match your IP and the port the Sinatra web app is running on.
- Build/run the app to your device.

## Contributing

Turbolinks Android is open-source software, freely distributable under the terms of an [MIT-style license](LICENSE). The [source code is hosted on GitHub](https://github.com/turbolinks/turbolinks-android).

We welcome contributions in the form of bug reports, pull requests, or thoughtful discussions in the [GitHub issue tracker](https://github.com/turbolinks/turbolinks-android/issues). Please see the [Code of Conduct](CONDUCT.md) for our pledge to contributors.

Turbolinks Android was created by [Dan Kim](https://twitter.com/dankim) and [Jay Ohms](https://twitter.com/jayohms), with guidance and help from [Sam Stephenson](https://twitter.com/sstephenson) and [Jeffrey Hardy](https://twitter.com/packagethief). Development is sponsored by [Basecamp](https://basecamp.com/).

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
