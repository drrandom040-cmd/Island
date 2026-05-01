package com.elsewhere.usland.state

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class PillState { IDLE, NOTIFICATION, MEDIA, CALL, TIMER }

data class NotificationData(
    val appName: String,
    val title: String,
    val body: String? = null,
    val packageName: String,
    val iconResId: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class MediaData(
    val trackName: String,
    val artistName: String,
    val albumArt: Any? = null,
    val isPlaying: Boolean,
    val progress: Float = 0f,
    val duration: Long = 0L
)

data class TimerData(
    val remainingMillis: Long,
    val totalMillis: Long,
    val label: String = "Timer"
)

enum class CallState { INCOMING, ACTIVE, ENDING }

data class CallData(
    val callerName: String,
    val phoneNumber: String? = null,
    val state: CallState = CallState.INCOMING,
    val durationFormatted: String? = null
)

object OverlayState {
    private val _pillState = MutableStateFlow(PillState.IDLE)
    val pillState: StateFlow<PillState> = _pillState

    private val _currentNotification = MutableStateFlow<NotificationData?>(null)
    val currentNotification: StateFlow<NotificationData?> = _currentNotification

    private val _currentMedia = MutableStateFlow<MediaData?>(null)
    val currentMedia: StateFlow<MediaData?> = _currentMedia

    private val _currentTimer = MutableStateFlow<TimerData?>(null)
    val currentTimer: StateFlow<TimerData?> = _currentTimer

    private val _currentCall = MutableStateFlow<CallData?>(null)
    val currentCall: StateFlow<CallData?> = _currentCall

    private val _isScreenOn = MutableStateFlow(true)
    val isScreenOn: StateFlow<Boolean> = _isScreenOn

    private val _logoAnimEnabled = MutableStateFlow(true)
    val logoAnimEnabled: StateFlow<Boolean> = _logoAnimEnabled

    private val _glowEnabled = MutableStateFlow(true)
    val glowEnabled: StateFlow<Boolean> = _glowEnabled

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled

    private val _autoCollapse = MutableStateFlow(true)
    val autoCollapse: StateFlow<Boolean> = _autoCollapse

    private val _collapseDelayMs = MutableStateFlow(4000L)
    val collapseDelayMs: StateFlow<Long> = _collapseDelayMs

    private val _pillScale = MutableStateFlow(1.0f)
    val pillScale: StateFlow<Float> = _pillScale

    private val _verticalOffset = MutableStateFlow(0)
    val verticalOffset: StateFlow<Int> = _verticalOffset

    private val queue = ArrayDeque<NotificationData>(  )
    private val handler = Handler(Looper.getMainLooper())

    fun pushNotification(data: NotificationData) {
        // Higher priority for new notifications
        _currentNotification.value = data
        _pillState.value = PillState.NOTIFICATION
        
        // Clear any auto-collapse timer if we have one (handled in UI)
    }

    fun collapse() {
        if (queue.isNotEmpty()) {
            val next = queue.removeFirst()
            _currentNotification.value = next
            _pillState.value = PillState.NOTIFICATION
            return
        }

        _pillState.value = PillState.IDLE
        _currentNotification.value = null
        _currentTimer.value = null
        _currentCall.value = null
    }

    fun setMedia(data: MediaData?) {
        val oldMedia = _currentMedia.value
        _currentMedia.value = data
        
        // Only switch to MEDIA state if music started playing
        if (data != null && data.isPlaying && (oldMedia == null || !oldMedia.isPlaying)) {
            if (_pillState.value == PillState.IDLE) {
                _pillState.value = PillState.MEDIA
            }
        } else if (data == null || !data.isPlaying) {
            if (_pillState.value == PillState.MEDIA) {
                _pillState.value = PillState.IDLE
            }
        }
    }

    fun setTimer(data: TimerData?) {
        _currentTimer.value = data
        if (data != null) {
            _pillState.value = PillState.TIMER
        } else if (_pillState.value == PillState.TIMER) {
            _pillState.value = PillState.IDLE
        }
    }

    fun setCall(data: CallData?) {
        _currentCall.value = data
        if (data != null) {
            _pillState.value = PillState.CALL
        } else if (_pillState.value == PillState.CALL) {
            _pillState.value = PillState.IDLE
        }
    }

    fun setScreenOn(on: Boolean) {
        _isScreenOn.value = on
    }

    fun setLogoAnim(enabled: Boolean) {
        _logoAnimEnabled.value = enabled
    }

    fun setGlow(enabled: Boolean) {
        _glowEnabled.value = enabled
    }

    fun setHaptic(enabled: Boolean) {
        _hapticEnabled.value = enabled
    }

    fun setAutoCollapse(enabled: Boolean) {
        _autoCollapse.value = enabled
    }

    fun setCollapseDelay(delayMs: Long) {
        _collapseDelayMs.value = delayMs
    }

    fun setPillScale(scale: Float) {
        _pillScale.value = scale.coerceIn(0.8f, 1.5f)
    }

    fun onPillTapped() {
        if (_pillState.value == PillState.IDLE) {
            // Try to show media if available
            if (_currentMedia.value != null) {
                _pillState.value = PillState.MEDIA
            }
        } else {
            // If already expanded, maybe do nothing or collapse on double tap (already handled)
        }
    }

    fun setVerticalOffset(offset: Int) {
        _verticalOffset.value = offset.coerceIn(-50, 100)
    }

    fun clearQueue() {
        queue.clear()
    }
}
