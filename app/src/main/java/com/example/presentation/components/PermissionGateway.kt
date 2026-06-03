package com.example.presentation.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

/**
 * Returns the list of runtime permissions needed by the application.
 */
fun getRequiredRuntimePermissions(): List<String> {
    val list = mutableListOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.RECORD_AUDIO
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        list.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    return list
}

/**
 * Utility function to check if all required permissions are granted.
 */
fun hasRequiredPermissions(context: Context): Boolean {
    return getRequiredRuntimePermissions().all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * Utility function to verify if SYSTEM_ALERT_WINDOW (Overlay) permission is granted.
 * If not, redirects the user to the native system settings page to toggle it on.
 * Returns true if the permission is already granted, false if redirect action was taken.
 */
fun checkAndRequestOverlayPermission(context: Context): Boolean {
    val hasOverlayPermission = Settings.canDrawOverlays(context)
    if (!hasOverlayPermission) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
    return hasOverlayPermission
}

@Composable
fun PermissionGateway(
    onGranted: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(hasRequiredPermissions(context)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultMap ->
        val allGranted = resultMap.values.all { it }
        permissionsGranted = allGranted
        if (allGranted) {
            onGranted()
        }
    }

    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            onGranted()
        }
    }

    if (permissionsGranted) {
        content()
    } else {
        // High-fidelity branded explanation Screen matching the RecoveryPro Bento Slate Theme
        val backgroundColor = Color(0xFFF7F9FB)
        val primaryBlue = Color(0xFF2563EB)
        val textSlateColor = Color(0xFF1E293B)
        val textSlateMuted = Color(0xFF64748B)
        val cardBorderColor = Color(0xFFE2E8F0)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header brand card
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Branded Lock Icon
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Shield Guard",
                                    tint = primaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Compliance & Device Access",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSlateColor,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "RecoveryPro maps and audits physical telephone line sequences for financial recovery team operations.",
                                fontSize = 12.sp,
                                color = textSlateMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Description of specific permissions needed
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "REQUIRED OPERATION PERMISSIONS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSlateMuted,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PermissionExplanationItem(
                                label = "Phone Status & SIM Mapping",
                                description = "Used to detect phone calls, physical carrier lines (SIM 1 / SIM 2), and cellular hook actions.",
                                statusLabel = "READ_PHONE_STATE",
                                primaryBlue = primaryBlue,
                                textSlateColor = textSlateColor,
                                textSlateMuted = textSlateMuted
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PermissionExplanationItem(
                                label = "Call Logging & Queue Verification",
                                description = "Allows reading the system logs to automatically match called numbers with local debtor accounts.",
                                statusLabel = "READ_CALL_LOG",
                                primaryBlue = primaryBlue,
                                textSlateColor = textSlateColor,
                                textSlateMuted = textSlateMuted
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PermissionExplanationItem(
                                label = "Automated Direct Calling",
                                description = "Enables instant connection placement from your curated collection queue straight to your physical SIM launcher.",
                                statusLabel = "CALL_PHONE",
                                primaryBlue = primaryBlue,
                                textSlateColor = textSlateColor,
                                textSlateMuted = textSlateMuted
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PermissionExplanationItem(
                                label = "Audio Conversation Recording",
                                description = "Legally records client dialogues to compile audit-trail compliance files and confirm active PTP records.",
                                statusLabel = "RECORD_AUDIO",
                                primaryBlue = primaryBlue,
                                textSlateColor = textSlateColor,
                                textSlateMuted = textSlateMuted
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PermissionExplanationItem(
                                label = "Foreground Service & Notifications",
                                description = "Retains session hooks and keeps local data synchronized securely with backend logs in background states.",
                                statusLabel = "POST_NOTIFICATIONS",
                                primaryBlue = primaryBlue,
                                textSlateColor = textSlateColor,
                                textSlateMuted = textSlateMuted
                            )
                        }
                    }
                }

                // Action Call-to-action bar
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                launcher.launch(getRequiredRuntimePermissions().toTypedArray())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("grant_permissions_button")
                        ) {
                            Text(
                                text = "Authorize Operation Suite",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Legal note",
                                tint = textSlateMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "All recordings are managed under institutional compliance rules.",
                                fontSize = 10.sp,
                                color = textSlateMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionExplanationItem(
    label: String,
    description: String,
    statusLabel: String,
    primaryBlue: Color,
    textSlateColor: Color,
    textSlateMuted: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "•",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryBlue
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textSlateColor
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = textSlateMuted,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFEFF6FF))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryBlue,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
