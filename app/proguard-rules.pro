# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- AndroidX ---
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>(...);
}

# --- Retrofit / OkHttp ---
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# --- GSON ---
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.internal.UnsafeAllocator { *; }
-dontwarn com.google.gson.internal.$Gson$Types

# --- Kotlin Serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.annotations.**
-keep,allowoptimization class kotlinx.serialization.** {
    <methods>;
}

# --- Mapbox ---
-keep class com.mapbox.** { *; }

# --- Hilt / Dagger ---
-keep class dagger.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class dagger.hilt.internal.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent

# --- Firebase ---
-keep class com.google.firebase.** { *; }

# --- Domain / API Models (Keep data classes intact for serialization) ---
-keep class com.piperrideshare.driver.api.models.** { *; }
