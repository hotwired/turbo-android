# Advanced Configuration

## Create a Native Fragment
// TODO

## Display Bottom Sheet Dialogs
// TODO

## Fragment Transition Animations
// TODO

## Using Multiple Activities
You may encounter situations where a truly single-`Activity` app may not be feasible. For example, you may need an `Activity` for logged-out state and a separate `Activity` for logged-in state.

In such cases, you simply need to create an additional `Activity` that also implements the `TurboActivity` interface. You will need to be sure to register each `Activity` by calling [`TurboSessionNavHostFragment.registeredActivities()`](../turbolinks/src/main/kotlin/com/basecamp/turbolinks/session/TurbolinksSessionNavHostFragment.kt) so that you can navigate between them.