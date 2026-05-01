package com.elsewhere.usland.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elsewhere.usland.state.OverlayState

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                OverlayState.setScreenOn(false)
                // Collapse any expanded state when screen turns off
                OverlayState.collapse()
                // Clear notification queue to prevent buildup
                OverlayState.clearQueue()
            }
            Intent.ACTION_SCREEN_ON -> {
                OverlayState.setScreenOn(true)
            }
        }
    }
}
