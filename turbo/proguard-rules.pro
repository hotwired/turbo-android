# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/dankim/android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
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
