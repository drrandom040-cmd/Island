package com.elsewhere.usland.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.elsewhere.usland.notification.UslandNotificationListener

object PermissionHelper {

    data class PermissionStatus(
        val overlay: Boolean,
        val notification: Boolean,
        val notificationListener: Boolean,
        val allGranted: Boolean
    )

    fun checkAllPermissions(context: Context): PermissionStatus {
        val overlay = canDrawOverlays(context)
        val notification = hasNotificationPermission(context)
        val notificationListener = isNotificationListenerEnabled(context)

        return PermissionStatus(
            overlay = overlay,
            notification = notification,
            notificationListener = notificationListener,
            allGranted = overlay && notification && notificationListener
        )
    }

    // Overlay Permission
    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun requestOverlayPermission(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    // Notification Permission (Android 13+)
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestNotificationPermission(activity: Activity, requestCode: Int = 1001) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                requestCode
            )
        }
    }

    // Notification Listener Permission
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val componentName = ComponentName(context, UslandNotificationListener::class.java)
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(componentName.flattenToString()) == true
    }

    fun requestNotificationListenerPermission(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    // DND Access (optional, for call detection)
    fun hasDoNotDisturbAccess(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun requestDoNotDisturbAccess(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    // App Settings
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    // Battery Optimization (for reliable background operation)
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) 
            as android.os.PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
