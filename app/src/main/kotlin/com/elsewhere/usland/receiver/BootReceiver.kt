package com.elsewhere.usland.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.elsewhere.usland.service.OverlayService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) {
            return
        }

        // Check if we have overlay permission before starting
        if (!Settings.canDrawOverlays(context)) {
            return
        }

        // Check if start on boot is enabled (could read from DataStore here)
        // For now, always start on boot if permission is granted
        startOverlayService(context)
    }

    private fun startOverlayService(context: Context) {
        val serviceIntent = Intent(context, OverlayService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
