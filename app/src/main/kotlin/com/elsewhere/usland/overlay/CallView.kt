package com.elsewhere.usland.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.elsewhere.usland.state.CallData
import com.elsewhere.usland.state.CallState
import com.elsewhere.usland.utils.UslandDesign
import kotlinx.coroutines.delay

@Composable
fun CallView(data: CallData) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        isVisible = false
        delay(50)
        isVisible = true
    }

    val stateColor = when (data.state) {
        CallState.INCOMING -> UslandDesign.periwinkle
        CallState.ACTIVE -> UslandDesign.success
        CallState.ENDING -> UslandDesign.error
    }

    val stateLabel = when (data.state) {
        CallState.INCOMING -> "Incoming call"
        CallState.ACTIVE -> data.durationFormatted ?: "On call"
        CallState.ENDING -> "Call ended"
    }

    val iconRes = when (data.state) {
        CallState.INCOMING -> R.drawable.ic_play   // reuse as incoming arrow
        CallState.ACTIVE -> R.drawable.ic_check
        CallState.ENDING -> R.drawable.ic_back
    }

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
            // Call state indicator dot
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(stateColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = stateColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Caller name
                Text(
                    text = data.callerName,
                    color = UslandDesign.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // State label (e.g. "Incoming call", "0:32", "Call ended")
                Text(
                    text = stateLabel,
                    color = stateColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            // Phone number if available and different from name
            data.phoneNumber?.let { number ->
                if (number != data.callerName) {
                    Text(
                        text = number,
                        color = UslandDesign.textSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 8.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
