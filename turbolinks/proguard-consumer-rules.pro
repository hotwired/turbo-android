# The Android Gradle plugin allows to define ProGuard rules which get embedded in the AAR.
# These ProGuard rules are automatically applied when a consumer app sets minifyEnabled to true.
# The custom rule file must be defined using the 'consumerProguardFiles' property in your
# build.gradle file.

-keepclassmembers class com.basecamp.turbolinks.TurbolinksSession {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
