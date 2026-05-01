package com.elsewhere.usland.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.elsewhere.usland.MainActivity
import com.elsewhere.usland.R
import com.elsewhere.usland.overlay.OverlayManager
import com.elsewhere.usland.receiver.ScreenStateReceiver

class OverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "usland_overlay_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.elsewhere.usland.ACTION_STOP"
        
        private var instance: OverlayService? = null
        
        fun isRunning(): Boolean = instance != null
    }

    private lateinit var overlayManager: OverlayManager
    private var screenStateReceiver: ScreenStateReceiver? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        createNotificationChannel()
        startForegroundCompat()
        
        overlayManager = OverlayManager(applicationContext)
        overlayManager.show()
        
        registerScreenStateReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        instance = null
        overlayManager.hide()
        unregisterScreenStateReceiver()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
        }
        
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun startForegroundCompat() {
        val notification = buildNotification()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openPendingIntent)
            .addAction(0, getString(R.string.btn_stop_service), stopPendingIntent)
            .build()
    }

    private fun registerScreenStateReceiver() {
        screenStateReceiver = ScreenStateReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    private fun unregisterScreenStateReceiver() {
        screenStateReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // Already unregistered
            }
        }
        screenStateReceiver = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
