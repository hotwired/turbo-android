# The Android Gradle plugin allows to define ProGuard rules which get embedded in the AAR.
# These ProGuard rules are automatically applied when a consumer app sets minifyEnabled to true.
# The custom rule file must be defined using the 'consumerProguardFiles' property in your
# build.gradle file.

-keepclassmembers class dev.hotwire.turbo.session.TurboSession {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface

# Gson
-keep class com.google.** { *; }
-keep class org.apache.** { *; }
-keep class javax.** { *; }
-keep class sun.misc.Unsafe { *; }

-keep class dev.hotwire.turbo.** { *; }
