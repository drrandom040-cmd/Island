package com.elsewhere.usland.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsewhere.usland.R
import com.elsewhere.usland.state.OverlayState
import com.elsewhere.usland.utils.UslandDesign

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = UslandDesign.background
                ) {
                    SettingsScreen(
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val logoAnimEnabled by OverlayState.logoAnimEnabled.collectAsState()
    val glowEnabled by OverlayState.glowEnabled.collectAsState()
    val hapticEnabled by OverlayState.hapticEnabled.collectAsState()
    val autoCollapse by OverlayState.autoCollapse.collectAsState()
    val collapseDelay by OverlayState.collapseDelayMs.collectAsState()
    val pillScale by OverlayState.pillScale.collectAsState()
    val verticalOffset by OverlayState.verticalOffset.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(UslandDesign.surfaceVariant)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = UslandDesign.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Settings",
                color = UslandDesign.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Appearance Section
        SectionHeader(title = "Appearance")

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard {
            ToggleSetting(
                title = "Logo Animation",
                description = "Rotate logo when idle",
                isChecked = logoAnimEnabled,
                onCheckedChange = { OverlayState.setLogoAnim(it) }
            )

            SettingsDivider()

            ToggleSetting(
                title = "Glow Effect",
                description = "Pulsing glow around logo",
                isChecked = glowEnabled,
                onCheckedChange = { OverlayState.setGlow(it) }
            )

            SettingsDivider()

            SliderSetting(
                title = "Pill Size",
                value = pillScale,
                valueRange = 0.8f..1.5f,
                valueLabel = "${(pillScale * 100).toInt()}%",
                onValueChange = { OverlayState.setPillScale(it) }
            )

            SettingsDivider()

            SliderSetting(
                title = "Vertical Position",
                value = verticalOffset.toFloat(),
                valueRange = -50f..100f,
                valueLabel = "${verticalOffset}px",
                onValueChange = { OverlayState.setVerticalOffset(it.toInt()) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Behavior Section
        SectionHeader(title = "Behavior")

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard {
            ToggleSetting(
                title = "Haptic Feedback",
                description = "Vibrate on expand/collapse",
                isChecked = hapticEnabled,
                onCheckedChange = { OverlayState.setHaptic(it) }
            )

            SettingsDivider()

            ToggleSetting(
                title = "Auto Collapse",
                description = "Collapse notifications automatically",
                isChecked = autoCollapse,
                onCheckedChange = { OverlayState.setAutoCollapse(it) }
            )

            if (autoCollapse) {
                SettingsDivider()

                SliderSetting(
                    title = "Collapse Delay",
                    value = collapseDelay.toFloat(),
                    valueRange = 2000f..10000f,
                    valueLabel = "${(collapseDelay / 1000f).toInt()}s",
                    onValueChange = { OverlayState.setCollapseDelay(it.toLong()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Section
        SectionHeader(title = "About")

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard {
            AboutItem(
                title = "Version",
                value = "1.0.0"
            )

            SettingsDivider()

            AboutItem(
                title = "Developer",
                value = "Elsewhere Studios"
            )

            SettingsDivider()

            AboutItem(
                title = "Package",
                value = "com.elsewhere.usland"
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = UslandDesign.periwinkle,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = UslandDesign.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(1.dp)
            .background(UslandDesign.surfaceVariant)
    )
}

@Composable
fun ToggleSetting(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = UslandDesign.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = UslandDesign.textSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = UslandDesign.textPrimary,
                checkedTrackColor = UslandDesign.ultraviolet,
                uncheckedThumbColor = UslandDesign.textSecondary,
                uncheckedTrackColor = UslandDesign.surfaceVariant
            )
        )
    }
}

@Composable
fun SliderSetting(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = UslandDesign.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueLabel,
                color = UslandDesign.periwinkle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = UslandDesign.ultraviolet,
                activeTrackColor = UslandDesign.ultraviolet,
                inactiveTrackColor = UslandDesign.surfaceVariant
            )
        )
    }
}

@Composable
fun AboutItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = UslandDesign.textPrimary,
            fontSize = 15.sp
        )
        Text(
            text = value,
            color = UslandDesign.textSecondary,
            fontSize = 14.sp
        )
    }
}
