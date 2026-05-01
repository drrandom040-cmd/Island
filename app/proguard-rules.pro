# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Keep notification listener
-keep class com.elsewhere.usland.notification.UslandNotificationListener { *; }

# Keep overlay service
-keep class com.elsewhere.usland.service.OverlayService { *; }

# Keep receivers
-keep class com.elsewhere.usland.receiver.** { *; }
