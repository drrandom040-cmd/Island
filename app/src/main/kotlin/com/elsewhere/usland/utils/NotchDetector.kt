package com.elsewhere.usland.utils

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.DisplayCutout
import android.view.WindowInsets
import android.view.WindowManager

data class NotchInfo(
    val hasNotch: Boolean,
    val notchRect: Rect?,
    val notchPosition: NotchPosition,
    val safeInsetTop: Int,
    val safeInsetLeft: Int,
    val safeInsetRight: Int
)

enum class NotchPosition {
    NONE,
    CENTER,
    LEFT,
    RIGHT
}

object NotchDetector {

    fun detect(context: Context): NotchInfo {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return NotchInfo(
                hasNotch = false,
                notchRect = null,
                notchPosition = NotchPosition.NONE,
                safeInsetTop = 0,
                safeInsetLeft = 0,
                safeInsetRight = 0
            )
        }

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            detectWithWindowMetrics(windowManager)
        } else {
            detectLegacy(windowManager)
        }
    }

    @Suppress("DEPRECATION")
    private fun detectLegacy(windowManager: WindowManager): NotchInfo {
        val display = windowManager.defaultDisplay
        val cutout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            display.cutout
        } else {
            null
        }

        return parseDisplayCutout(cutout)
    }

    private fun detectWithWindowMetrics(windowManager: WindowManager): NotchInfo {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return NotchInfo(
                hasNotch = false,
                notchRect = null,
                notchPosition = NotchPosition.NONE,
                safeInsetTop = 0,
                safeInsetLeft = 0,
                safeInsetRight = 0
            )
        }

        val windowMetrics = windowManager.currentWindowMetrics
        val windowInsets = windowMetrics.windowInsets
        val cutout = windowInsets.displayCutout

        return parseDisplayCutout(cutout)
    }

    private fun parseDisplayCutout(cutout: DisplayCutout?): NotchInfo {
        if (cutout == null) {
            return NotchInfo(
                hasNotch = false,
                notchRect = null,
                notchPosition = NotchPosition.NONE,
                safeInsetTop = 0,
                safeInsetLeft = 0,
                safeInsetRight = 0
            )
        }

        val boundingRects = cutout.boundingRects
        val topCutout = boundingRects.firstOrNull()

        val position = when {
            topCutout == null -> NotchPosition.NONE
            topCutout.centerX() < topCutout.width() -> NotchPosition.LEFT
            topCutout.centerX() > topCutout.width() * 2 -> NotchPosition.RIGHT
            else -> NotchPosition.CENTER
        }

        return NotchInfo(
            hasNotch = boundingRects.isNotEmpty(),
            notchRect = topCutout,
            notchPosition = position,
            safeInsetTop = cutout.safeInsetTop,
            safeInsetLeft = cutout.safeInsetLeft,
            safeInsetRight = cutout.safeInsetRight
        )
    }

    fun getOptimalPillY(context: Context): Int {
        val notchInfo = detect(context)
        
        return if (notchInfo.hasNotch && notchInfo.notchRect != null) {
            // Position below the notch
            notchInfo.notchRect.bottom + 8
        } else {
            // Use safe inset or default status bar height
            val statusBarHeight = getStatusBarHeight(context)
            statusBarHeight + 8
        }
    }

    private fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier(
            "status_bar_height", 
            "dimen", 
            "android"
        )
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            // Default fallback
            (24 * context.resources.displayMetrics.density).toInt()
        }
    }
}
