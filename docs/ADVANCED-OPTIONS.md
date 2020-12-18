# Advanced Options

## Create a Native Fragment
You don't need to rely on your web app for every screen in your app. Elevating destinations that would benefit from a higher fidelity experience to fully-native is often a great idea. For example, here's how you would create a fully native image viewer fragment:

### Create the TurboFragment layout resource
You need to create a layout resource file that your native `TurboFragment` will use as its content view.

In its simplest form, your Fragment layout file will look like:

**`res/layout/fragment_image_viewer.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Your layout views here -->

</androidx.constraintlayout.widget.ConstraintLayout>
```

Refer to the demo [`fragment_image_viewer.xml`](../demo/src/main/res/layout/fragment_image_viewer.xml) for an example.

### Create the TurboFragment class
A native Fragment is straightforward and needs to implement the [`TurboFragment`](../turbo/src/main/kotlin/dev/hotwire/turbo/fragments/TurboFragment.kt) abstract class.

You'll need to annotate each Fragment in your app with a `@TurboNavGraphDestination` annotation with a URI of your own scheme. This URI is used by the library to build an internal navigation graph and map url path patterns to the destination Fragment with the corresponding URI. See the [Path Configuration guide](PATH-CONFIGURATION.md) to learn how to map url paths to destination Fragments.

In its simplest form, your native Fragment will look like:

**`ImageViewerFragment.kt`:**
```kotlin
@TurboNavGraphDestination(uri = "turbo://fragment/image_viewer")
class ImageViewerFragment : TurboFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadImage(view)
    }

    private fun loadImage(view: View) {
        // Load the image into the view using the `location`
    }
}
```

Don't forget to register the `ImageViewerFragment` class in your app's `TurboSessionNavHostFragment` as a Fragment destination.

Refer to demo [`ImageViewerFragment`](../demo/src/main/kotlin/dev/hotwire/turbo/demo/features/imageviewer/ImageViewerFragment.kt) as an example.

## Display Bottom Sheet Dialogs
// TODO

## Fragment Transition Animations
// TODO

## Using Multiple Activities
You may encounter situations where a truly single-`Activity` app may not be feasible. For example, you may need an `Activity` for logged-out state and a separate `Activity` for logged-in state.

In such cases, you simply need to create an additional `Activity` that also implements the `TurboActivity` interface. You will need to be sure to register each `Activity` by calling [`TurboSessionNavHostFragment.registeredActivities()`](../turbo/src/main/kotlin/dev/hotwire/turbo/session/TurboSessionNavHostFragment.kt) so that you can navigate between them.