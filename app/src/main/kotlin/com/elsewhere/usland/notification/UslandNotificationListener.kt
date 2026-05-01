package com.elsewhere.usland.notification

import android.app.Notification
import android.content.pm.PackageManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.elsewhere.usland.state.MediaData
import com.elsewhere.usland.state.NotificationData
import com.elsewhere.usland.state.OverlayState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UslandNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mediaSessionManager: MediaSessionManager? = null
    private var activeMediaController: MediaController? = null

    private val mediaCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateMediaState()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateMediaState()
        }
    }

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateActiveMediaSession(controllers)
    }

    override fun onCreate() {
        super.onCreate()
        setupMediaSessionListener()
    }

    override fun onDestroy() {
        activeMediaController?.unregisterCallback(mediaCallback)
        mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Skip our own notifications
        if (sbn.packageName == packageName) return

        val notification = sbn.notification
        val extras = notification.extras

        // Get title and text
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() // Use text as title if title is missing
            ?: return

        val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        // Skip if it looks like a media control notification that we already handle via MediaSession
        if (notification.category == Notification.CATEGORY_TRANSPORT) return
        if (notification.actions?.any { it.title?.toString()?.contains("pause", ignoreCase = true) == true } == true) return

        // Get app name
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(sbn.packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            sbn.packageName.split(".").last()
        }

        val data = NotificationData(
            appName = appName,
            title = title,
            body = body,
            packageName = sbn.packageName,
            timestamp = sbn.postTime
        )

        scope.launch {
            OverlayState.pushNotification(data)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Could be used to track notification dismissal
    }

    private fun setupMediaSessionListener() {
        try {
            mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
            
            val componentName = android.content.ComponentName(
                this,
                UslandNotificationListener::class.java
            )

            mediaSessionManager?.addOnActiveSessionsChangedListener(
                sessionListener,
                componentName
            )

            // Get initial sessions
            val controllers = mediaSessionManager?.getActiveSessions(componentName)
            updateActiveMediaSession(controllers)
        } catch (e: SecurityException) {
            // Notification listener permission not granted
        }
    }

    private fun updateActiveMediaSession(controllers: List<MediaController>?) {
        // Unregister from previous controller
        activeMediaController?.unregisterCallback(mediaCallback)

        // Find the first playing controller, or the first one available
        val controller = controllers?.firstOrNull { 
            it.playbackState?.state == PlaybackState.STATE_PLAYING 
        } ?: controllers?.firstOrNull()

        activeMediaController = controller
        controller?.registerCallback(mediaCallback)

        updateMediaState()
    }

    private fun updateMediaState() {
        val controller = activeMediaController ?: run {
            OverlayState.setMedia(null)
            return
        }

        val metadata = controller.metadata
        val playbackState = controller.playbackState

        if (metadata == null) {
            OverlayState.setMedia(null)
            return
        }

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

        val trackName = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: "Unknown Track"

        val artistName = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: "Unknown Artist"

        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val position = playbackState?.position ?: 0L

        val progress = if (duration > 0) {
            (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        val mediaData = MediaData(
            trackName = trackName,
            artistName = artistName,
            albumArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART),
            isPlaying = isPlaying,
            progress = progress,
            duration = duration
        )

        OverlayState.setMedia(mediaData)
    }
}
