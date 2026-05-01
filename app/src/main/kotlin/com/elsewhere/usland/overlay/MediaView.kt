package com.elsewhere.usland.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsewhere.usland.R
import com.elsewhere.usland.state.MediaData
import com.elsewhere.usland.utils.UslandDesign
import kotlinx.coroutines.delay

@Composable
fun MediaView(data: MediaData) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        isVisible = false
        delay(50)
        isVisible = true
    }

    val progress by animateFloatAsState(
        targetValue = data.progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInHorizontally { -it / 3 }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art / Music icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(UslandDesign.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = UslandDesign.periwinkle
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Track name
                Text(
                    text = data.trackName,
                    color = UslandDesign.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Artist name
                Text(
                    text = data.artistName,
                    color = UslandDesign.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

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
                            .fillMaxWidth(progress)
                            .background(
                                UslandDesign.periwinkle,
                                RoundedCornerShape(1.5.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Play/Pause indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(UslandDesign.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (data.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    ),
                    contentDescription = if (data.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(16.dp),
                    tint = UslandDesign.textPrimary
                )
            }
        }
    }
}
