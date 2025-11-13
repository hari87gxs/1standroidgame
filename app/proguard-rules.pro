# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep application main class
-keep class com.athreya.mathworkout.** { *; }

# Keep Jetpack Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Room database classes
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep ViewModel classes
-keep class androidx.lifecycle.** { *; }

# Keep Navigation classes
-keep class androidx.navigation.** { *; }

# Keep DataStore classes
-keep class androidx.datastore.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Material Design classes
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep attributes for debugging
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile