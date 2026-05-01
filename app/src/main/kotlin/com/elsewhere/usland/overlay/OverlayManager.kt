package com.elsewhere.usland.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.elsewhere.usland.state.OverlayState
import com.elsewhere.usland.utils.NotchDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun show() {
        if (composeView != null) return

        val initialY = NotchDetector.getOptimalPillY(context)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = initialY
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        }

        val lifecycleOwner = OverlayLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setContent {
                PillWidget()
            }
        }

        composeView = view
        windowManager.addView(view, params)

        // Listen for vertical offset changes
        scope.launch {
            OverlayState.verticalOffset.collectLatest { offset ->
                updatePosition(initialY + offset)
            }
        }
    }

    fun hide() {
        composeView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                // View might already be removed
            }
        }
        composeView = null
    }

    fun updatePosition(yOffset: Int) {
        composeView?.let { view ->
            val params = view.layoutParams as? WindowManager.LayoutParams ?: return
            params.y = yOffset
            try {
                windowManager.updateViewLayout(view, params)
            } catch (e: Exception) {
                // View might not be attached
            }
        }
    }

    private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }

        fun performRestore(savedState: Bundle?) {
            savedStateRegistryController.performRestore(savedState)
        }
    }
}
