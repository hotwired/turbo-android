# Path Configuration
A JSON configuration file specifies the set of rules Turbo will follow to navigate to Fragment destinations and configure options. It has two top-level objects: 

1. Application-level `"settings"`
1. Url path-specific `"rules"`

At minimum, you will need a bundled [`src/main/assets/json/configuration.json`](../demo/src/main/assets/json/configuration.json) file in your app that Turbo can read. We also recommend hosting a [remote configuration file on your server](#remote-path-configuration), so you can update the app's configuration at any time without needing an app update.

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

Refer to demo [`configuration.json`](../demo/src/main/assets/json/configuration.json) as an example.

## Remote Path Configuration

Remote configuration files are fetched (and cached) on every app startup, so the app always has the latest configuration available. The location of these configuration files needs to be set in your [`TurboSessionNavHostFragment.pathConfigurationLocation`](QUICK-START.md/#create-a-navhostfragment). 

```kotlin
class MainSessionNavHostFragment : TurboSessionNavHostFragment() {
    // ...
    override val pathConfigurationLocation: TurboPathConfiguration.Location
        get() = TurboPathConfiguration.Location(
            assetFilePath = "json/configuration.json",
            remoteFileUrl = "https://turbo.hotwired.dev/demo/configurations/android-v1.json"
        )
}
```

Here's some tips for managing path configurations:

- Use different path configuration files, with different URLs, for Android and [iOS](https://github.com/hotwired/turbo-ios).
- Include a version in your path configuration URL (`v1` in the above example). This way if you need to make fundamental changes to your architecture you can be confident you won't break the app for people who haven't updated.
- Try to keep your local and remote path configuration files in sync. When your app starts, Turbo will load your local configuration file, then make a request for your remote file which will override your local file. If the files are different and your server doesn't respond quickly, it's possible to get difficult to debug behaviour. If you're making other changes to your app that will require a new native deployment, that's a good time to update your local file to match the current state of your server.

## Settings
The `settings` object is a place to configure app-level settings. This is useful when you have a remote configuration file, since you can add your own custom settings and use them as remote feature-flags. Available settings are:
* `screenshots_enabled` — Whether or not transitional web screenshots should be used during navigation. This gives the appearance of a more smooth experience since the session WebView is swapped between web destination Fragments, but does require more performance overhead. 
	* Optional.
	* Possible values: `true`, `false`. Defaults to `true`.
* Any custom app settings that you'd like to configure here

## Rules
The `"rules"` array defines a list of rules that are processed in order and cascade downward, similar to CSS. The top-most declaration should establish the default behavior for all url path patterns, while each subsequent rule can override for specific behavior.

### Patterns

The `patterns` array defines Regex patterns that will be used to match url paths (and as a result, which `properties` should be applied for a particular path).

### Properties

The `properties` object contains a handful of key/value pairs that Turbo Android supports out of the box. You are free to add more properties as your app needs, but these are the ones the framework is aware of and will handle automatically.

* `uri` — The target destination URI to navigate to. Must map to an Activity or Fragment that has implemented the [`TurboNavGraphDestination`](../turbo/src/main/kotlin/dev/hotwire/turbo/nav/TurboNavGraphDestination.kt) annotation with a matching `uri` value.
	* **Required**. 
	* No explicit value options. No default value.
* `context` — Specifies the presentation context in which the view should be displayed. Turbo will determine what the navigation behavior should be based on this value + the `presentation` value. Unless you are specifically showing a modal-style view (e.g., a form, wizard, navigation, etc.), `default` is usually sufficient. 
	* Optional. 
	* Possible values: `default` or `modal`. Defaults to `default`. 
* `presentation` — Specifies what style to use when presenting the given `uri` destination. Turbo will determine what the navigation behavior should be based on this value + the `context` value. In most cases `default` should be sufficient, but you may find cases where your app needs specific behavior. 
	* Optional. 
	* [Possible values](../turbo/src/main/kotlin/dev/hotwire/turbo/nav/TurboNavPresentation.kt): `default`, `push`, `pop`, `replace`, `replace_root`, `clear_all`, `refresh`, `none`. Defaults to `default`.
* `fallback_uri` — Provides a fallback URI in case a destination cannot be found that maps to the `uri`. Can be useful in cases when pointing to a new `uri` that may not be available yet in older versions of the app.
	* Optional.
	* No explicit value options. No default value.
* `title` —  Specifies a default title that will be displayed in the toolbar for the destination. This is most useful for native destinations, since web destinations will render their title from the `WebView` page's `<title>` tag.
    * Optional.
    * No explicit value options. No default value.
* `pull_to_refresh_enabled` — Whether or not pull-to-refresh should be enabled for the given path.
	* Optional.
	* Possible values: `true`, `false`. Defaults to `false`.
  
