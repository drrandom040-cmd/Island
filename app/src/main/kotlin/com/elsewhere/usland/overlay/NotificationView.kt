package com.elsewhere.usland.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsewhere.usland.R
import com.elsewhere.usland.state.NotificationData
import com.elsewhere.usland.utils.UslandDesign
import kotlinx.coroutines.delay

@Composable
fun NotificationView(data: NotificationData) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        isVisible = false
        delay(50)
        isVisible = true
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
            // App Icon placeholder
            Icon(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                tint = UslandDesign.periwinkle
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // App name
                Text(
                    text = data.appName,
                    color = UslandDesign.periwinkle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Title
                Text(
                    text = data.title,
                    color = UslandDesign.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Body (if present)
                data.body?.let { body ->
                    if (body.isNotBlank()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = body,
                            color = UslandDesign.textSecondary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Time indicator
            Text(
                text = "now",
                color = UslandDesign.textSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
