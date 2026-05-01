package com.elsewhere.usland.utils

import androidx.compose.ui.graphics.Color

object UslandDesign {
    // Colors
    val background = Color(0xFF0D0D0D)
    val periwinkle = Color(0xFFCCCCFF)
    val ultraviolet = Color(0xFF7B00FF)
    val textPrimary = Color.White
    val textSecondary = Color(0xFF999999)
    val surface = Color(0xFF1A1A1A)
    val surfaceVariant = Color(0xFF2A2A2A)
    val success = Color(0xFF4CAF50)
    val warning = Color(0xFFFF9800)
    val error = Color(0xFFF44336)

    // Dimensions
    const val pillCollapsedWidth = 120f
    const val pillCollapsedHeight = 34f
    const val pillExpandedWidth = 280f
    const val pillExpandedHeight = 100f
    const val pillMediaWidth = 300f
    const val pillMediaHeight = 80f
    const val pillTimerWidth = 200f
    const val pillTimerHeight = 60f
    const val pillCallWidth = 260f
    const val pillCallHeight = 70f
    const val pillCornerRadius = 50f
    const val logoSize = 28f

    // Animation durations
    const val expandDuration = 300
    const val collapseDuration = 250
    const val logoRotationDuration = 6000
    const val glowPulseDuration = 3000

    // Timing
    const val defaultCollapseDelay = 4000L
    const val mediaUpdateInterval = 1000L
}
