package com.elsewhere.usland.overlay

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.elsewhere.usland.state.OverlayState
import com.elsewhere.usland.state.PillState
import com.elsewhere.usland.utils.UslandDesign
import kotlinx.coroutines.delay

@Composable
fun PillWidget() {
    val context = LocalContext.current
    val pillState by OverlayState.pillState.collectAsState()
    val notification by OverlayState.currentNotification.collectAsState()
    val media by OverlayState.currentMedia.collectAsState()
    val timer by OverlayState.currentTimer.collectAsState()
    val call by OverlayState.currentCall.collectAsState()
    val logoAnimEnabled by OverlayState.logoAnimEnabled.collectAsState()
    val glowEnabled by OverlayState.glowEnabled.collectAsState()
    val hapticEnabled by OverlayState.hapticEnabled.collectAsState()
    val autoCollapse by OverlayState.autoCollapse.collectAsState()
    val collapseDelay by OverlayState.collapseDelayMs.collectAsState()
    val pillScale by OverlayState.pillScale.collectAsState()

    var previousState by remember { mutableStateOf(PillState.IDLE) }

    // Haptic feedback on state change
    LaunchedEffect(pillState) {
        if (hapticEnabled && pillState != previousState) {
            vibrate(context)
        }
        previousState = pillState
    }

    // Auto-collapse for notifications
    LaunchedEffect(pillState, autoCollapse) {
        if (pillState == PillState.NOTIFICATION && autoCollapse) {
            delay(collapseDelay)
            if (OverlayState.pillState.value == PillState.NOTIFICATION) {
                OverlayState.collapse()
            }
        }
    }

    val targetWidth = when (pillState) {
        PillState.IDLE         -> UslandDesign.pillCollapsedWidth
        PillState.NOTIFICATION -> UslandDesign.pillExpandedWidth
        PillState.MEDIA        -> UslandDesign.pillMediaWidth
        PillState.TIMER        -> UslandDesign.pillTimerWidth
        PillState.CALL         -> UslandDesign.pillCallWidth
    } * pillScale

    val targetHeight = when (pillState) {
        PillState.IDLE         -> UslandDesign.pillCollapsedHeight
        PillState.NOTIFICATION -> UslandDesign.pillExpandedHeight
        PillState.MEDIA        -> UslandDesign.pillMediaHeight
        PillState.TIMER        -> UslandDesign.pillTimerHeight
        PillState.CALL         -> UslandDesign.pillCallHeight
    } * pillScale

    val animWidth by animateFloatAsState(
        targetValue = targetWidth,
        animationSpec = tween(
            durationMillis = if (pillState == PillState.IDLE)
                UslandDesign.collapseDuration
            else
                UslandDesign.expandDuration,
            easing = FastOutSlowInEasing
        ),
        label = "pillWidth"
    )

    val animHeight by animateFloatAsState(
        targetValue = targetHeight,
        animationSpec = tween(
            durationMillis = if (pillState == PillState.IDLE)
                UslandDesign.collapseDuration
            else
                UslandDesign.expandDuration,
            easing = FastOutSlowInEasing
        ),
        label = "pillHeight"
    )

    Box(
        modifier = Modifier
            .width(animWidth.dp)
            .height(animHeight.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(UslandDesign.pillCornerRadius.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(UslandDesign.pillCornerRadius.dp))
            .background(
                color = UslandDesign.background,
                shape = RoundedCornerShape(UslandDesign.pillCornerRadius.dp)
            )
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -30f && pillState != PillState.IDLE) {
                        OverlayState.collapse()
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (pillState != PillState.IDLE) {
                            OverlayState.collapse()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        when (pillState) {
            PillState.IDLE -> LogoAnimation(
                animationEnabled = logoAnimEnabled,
                glowEnabled = glowEnabled
            )
            PillState.NOTIFICATION -> notification?.let {
                NotificationView(data = it)
            }
            PillState.MEDIA -> media?.let {
                MediaView(data = it)
            }
            PillState.TIMER -> timer?.let {
                TimerView(data = it)
            }
            PillState.CALL -> call?.let {
                CallView(data = it)
            }
        }
    }
}

private fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
            as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(20)
    }
}
