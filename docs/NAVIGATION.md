# Navigate to Destinations

## From Web Links
Tapping a web link in a `TurboWebFragment` will automatically navigate to the url's corresponding Fragment destination, based on the app's [Path Configuration](PATH-CONFIGURATION.md).

Sometimes, you may want to override this default behavior. For example, if your web app can surface external domain urls, you should open those urls in the device's default browser. The `TurboWebFragment` abstract class implements the `TurbolNavDestination` interface, which provides a `shouldNavigateTo(newLocation: String)` function that can be overridden.

In your web Fragment, this would look like:

**`WebFragment.kt`:**
```kotlin
@TurboNavGraphDestination(uri = "turbo://fragment/web")
class WebFragment : TurboWebFragment() {

    // ...

    override fun shouldNavigateTo(newLocation: String): Boolean {
        return when (newLocation.startsWith(MY_DOMAIN)) {
            true -> {
                // Allow Turbo to follow the navigation to `newLocation`
                true
            }
            else -> {
                // Open `newLocation` in the device's external browser
                launchBrowser(newLocation)
                false
            }
        }
    }

    private fun launchBrowser(location: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(location))
        context?.startActivity(intent)
    }
}
```

Refer to demo [`NavDestination`](../demo/src/main/kotlin/dev/hotwire/turbo/demo/base/NavDestination.kt) interface as a more advanced example.

# From a TurboFragment
If you'd like to navigate to a new destination in response to native UI/features, it's easy from any `TurboFragment`. The following navigation APIs are available:

- `navigate(location: String)`
- `navigateUp()`
- `navigateBack()`
- `clearBackStack()`
- `refresh(displayProgress: Boolean)`

Refer to the [`TurboNavDestination`](../turbo/src/main/kotlin/dev/hotwire/turbo/nav/TurboNavDestination.kt) interface for further documentation.

In your Fragment, this would look like:

**`WebFragment.kt`:**
```kotlin
@TurboNavGraphDestination(uri = "turbo://fragment/web")
class WebFragment : TurboWebFragment() {

    // ...

    private fun respondToNativeFeature() {
        // Navigate to a new destination
        navigate(MY_NEW_LOCATION)

        // Navigate up to the previous destination (Toolbar arrow behavior)
        navigateUp()

        // Navigate back to the previous destination (OS back button behavior)
        navigateBack()

        // Clears all Fragment destinations off the backstack, excluding
        // the starting destination of your TurboSessionNavHostFragment
        clearBackStack()

        // Refresh the current destination
        refresh()

        // Refresh the current destination without displaying progress
        refresh(displayProgress = false)
    }
}
```
# From a TurboActivity
If you'd like to navigate to a new destination in response to native UI/features, it's easy from any `TurboActivity`. The following navigation APIs are available from the Activity's `delegate`:

- `delegate.navigate(location: String)`
- `delegate.navigateUp()`
- `delegate.navigateBack()`
- `delegate.clearBackStack()`
- `delegate.refresh(displayProgress: Boolean)`

Refer to the [`TurboActivityDelegate`](../turbo/src/main/kotlin/dev/hotwire/turbo/delegates/TurboActivityDelegate.kt) class for further documentation.

In your Activity, this would look like:

**`MainActivity.kt`:**
```kotlin
class MainActivity : AppCompatActivity(), TurboActivity {

    // ...

    private fun respondToNativeFeature() {
        // Navigate to a new destination
        delegate.navigate(MY_NEW_LOCATION)

        // Navigate up to the previous destination (Toolbar arrow behavior)
        delegate.navigateUp()

        // Navigate back to the previous destination (OS back button behavior)
        delegate.navigateBack()

        // Clears all Fragment destinations off the backstack, excluding
        // the starting destination of your TurboSessionNavHostFragment
        delegate.clearBackStack()

        // Refresh the current destination
        delegate.refresh()

        // Refresh the current destination without displaying progress
        delegate.refresh(displayProgress = false)
    }
}
```
