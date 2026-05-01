package com.elsewhere.usland

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elsewhere.usland.service.OverlayService
import com.elsewhere.usland.settings.SettingsActivity
import com.elsewhere.usland.utils.PermissionHelper
import com.elsewhere.usland.utils.UslandDesign
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = UslandDesign.background
                ) {
                    MainScreen(
                        onRequestOverlay = { requestOverlayPermission() },
                        onRequestNotification = { requestNotificationPermission() },
                        onRequestNotificationListener = { requestNotificationListenerPermission() },
                        onStartService = { startOverlayService() },
                        onStopService = { stopOverlayService() },
                        onOpenSettings = { openSettings() },
                        checkPermissions = { PermissionHelper.checkAllPermissions(this) },
                        isServiceRunning = { OverlayService.isRunning() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Permissions might have changed while we were in the background
    }

    private fun requestOverlayPermission() {
        PermissionHelper.requestOverlayPermission(this)
    }

    private fun requestNotificationPermission() {
        PermissionHelper.requestNotificationPermission(this)
    }

    private fun requestNotificationListenerPermission() {
        PermissionHelper.requestNotificationListenerPermission(this)
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun MainScreen(
    onRequestOverlay: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestNotificationListener: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onOpenSettings: () -> Unit,
    checkPermissions: () -> PermissionHelper.PermissionStatus,
    isServiceRunning: () -> Boolean
) {
    var permissionStatus by remember { mutableStateOf(checkPermissions()) }
    var serviceRunning by remember { mutableStateOf(isServiceRunning()) }

    // Refresh permission status periodically
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            permissionStatus = checkPermissions()
            serviceRunning = isServiceRunning()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Usland",
                    color = UslandDesign.periwinkle,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dynamic Island for Android",
                    color = UslandDesign.textSecondary,
                    fontSize = 14.sp
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(UslandDesign.surfaceVariant)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    tint = UslandDesign.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = UslandDesign.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (serviceRunning) "Running" else "Stopped",
                    color = if (serviceRunning) UslandDesign.success else UslandDesign.textSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (serviceRunning) 
                        "Usland is active and displaying the Dynamic Island" 
                    else 
                        "Tap Start to begin",
                    color = UslandDesign.textSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Permissions Section
        Text(
            text = "Permissions",
            color = UslandDesign.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        PermissionItem(
            title = "Overlay Permission",
            description = "Display over other apps",
            isGranted = permissionStatus.overlay,
            onRequest = onRequestOverlay
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionItem(
            title = "Notification Access",
            description = "Read incoming notifications",
            isGranted = permissionStatus.notificationListener,
            onRequest = onRequestNotificationListener
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(modifier = Modifier.height(8.dp))
            
            PermissionItem(
                title = "Post Notifications",
                description = "Show service notification",
                isGranted = permissionStatus.notification,
                onRequest = onRequestNotification
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        if (serviceRunning) {
            OutlinedButton(
                onClick = onStopService,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = UslandDesign.error
                )
            ) {
                Text(
                    text = "Stop Usland",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Button(
                onClick = onStartService,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = permissionStatus.overlay,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UslandDesign.ultraviolet,
                    disabledContainerColor = UslandDesign.surfaceVariant
                )
            ) {
                Text(
                    text = "Start Usland",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (!permissionStatus.overlay) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Overlay permission is required to start",
                color = UslandDesign.warning,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Footer
        Text(
            text = "Version 1.0.0",
            color = UslandDesign.textSecondary,
            fontSize = 12.sp
        )
        Text(
            text = "Elsewhere Studios",
            color = UslandDesign.textSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = UslandDesign.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = UslandDesign.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    color = UslandDesign.textSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isGranted) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Granted",
                    tint = UslandDesign.success,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UslandDesign.periwinkle
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Grant",
                        color = UslandDesign.background,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
