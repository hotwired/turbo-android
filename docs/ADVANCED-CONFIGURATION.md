## Additional Configuration

### Advanced use with Multiple Activities
You may encounter situations where a truly single-`Activity` app may not be feasible. For example, you may need an `Activity` for logged-out state and a separate `Activity` for logged-in state.

In such cases, you simply need to create an additional `Activity` that also implements the `TurbolinksActivity` interface. You will need to be sure to register each `Activity` by calling [TurbolinksSessionNavHostFragment.registeredActivities()](turbolinks/src/main/kotlin/com/basecamp/turbolinks/session/TurbolinksSessionNavHostFragment.kt) so that you can navigate between them.