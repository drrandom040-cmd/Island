package com.elsewhere.usland.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsewhere.usland.R
import com.elsewhere.usland.state.TimerData
import com.elsewhere.usland.utils.UslandDesign
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun TimerView(data: TimerData) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        isVisible = false
        delay(50)
        isVisible = true
    }

    val progress = if (data.totalMillis > 0) {
        1f - (data.remainingMillis.toFloat() / data.totalMillis.toFloat())
    } else {
        0f
    }.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "timerProgress"
    )

    // Blinking effect when time is low
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    val isLowTime = data.remainingMillis < 10000 // Less than 10 seconds
    val textAlpha = if (isLowTime) blinkAlpha else 1f

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInHorizontally { -it / 3 }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Timer icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLowTime) UslandDesign.warning.copy(alpha = 0.2f)
                        else UslandDesign.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_timer),
                    contentDescription = "Timer",
                    modifier = Modifier.size(18.dp),
                    tint = if (isLowTime) UslandDesign.warning else UslandDesign.periwinkle
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Time display
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formatTime(data.remainingMillis),
                    color = if (isLowTime) UslandDesign.warning else UslandDesign.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.alpha(textAlpha)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(UslandDesign.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .background(
                                if (isLowTime) UslandDesign.warning
                                else UslandDesign.periwinkle,
                                RoundedCornerShape(1.5.dp)
                            )
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
