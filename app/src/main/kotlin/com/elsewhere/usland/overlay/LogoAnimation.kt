package com.elsewhere.usland.overlay

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elsewhere.usland.R
import com.elsewhere.usland.utils.UslandDesign

@Composable
fun LogoAnimation(
    animationEnabled: Boolean,
    glowEnabled: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoTransition")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = UslandDesign.logoRotationDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = UslandDesign.glowPulseDuration,
                easing = FastOutLinearInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = UslandDesign.glowPulseDuration,
                easing = FastOutLinearInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowAlpha"
    )

    Box(contentAlignment = Alignment.Center) {
        // Glow effect
        if (glowEnabled) {
            Box(
                modifier = Modifier
                    .size((UslandDesign.logoSize * glowScale).dp)
                    .background(
                        color = UslandDesign.periwinkle.copy(alpha = glowAlpha),
                        shape = CircleShape
                    )
            )
        }

        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Usland logo",
            modifier = Modifier
                .size(UslandDesign.logoSize.dp)
                .rotate(if (animationEnabled) rotation else 0f)
        )
    }
}
