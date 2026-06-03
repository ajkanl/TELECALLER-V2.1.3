package com.example.presentation.security

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityAndComplianceSettingsScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val isNumberMaskingEnabled by viewModel.isNumberMaskingEnabled.collectAsState()
    val isHardwareBindingEnabled by viewModel.isHardwareBindingEnabled.collectAsState()
    val isScreenshotBlockEnabled by viewModel.isScreenshotBlockEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showPurgeDialog by remember { mutableStateOf(false) }
    var purgeConfirmationText by remember { mutableStateOf("") }

    val primaryBlue = Color(0xFF2563EB)
    val textSlateColor = Color(0xFF1E293B)
    val textSlateMuted = Color(0xFF64748B)
    val accentRed = Color(0xFFEF4444)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("security_settings_screen_container")
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Security & Compliance Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("security_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Back",
                            tint = textSlateColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = textSlateColor
                ),
                modifier = Modifier.border(1.dp, Color(0xFFF1F5F9))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER INFO PANEL ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = primaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Security Control Suite",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textSlateColor
                        )
                        Text(
                            text = "Configure strict rules to prevent unauthorized financial record logging and debtor directory data leaks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSlateMuted
                        )
                    }
                }
            }

            Text(
                text = "Operational Leak Protection",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textSlateColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            // --- 1. Debtor Number Masking Toggle ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            "Mask Debtor Contact Numbers",
                            fontWeight = FontWeight.Bold,
                            color = textSlateColor
                        )
                    },
                    supportingContent = {
                        Text(
                            "Replaces intermediate digits (e.g. +91 ******1234) on dialing pages to protect contact directories.",
                            color = textSlateMuted
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = isNumberMaskingEnabled,
                            onCheckedChange = { viewModel.toggleNumberMasking(it) },
                            modifier = Modifier.testTag("debtor_masking_toggle")
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            // --- 2. Device Hardware Binding Switch ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            "Direct Hardware Device Binding",
                            fontWeight = FontWeight.Bold,
                            color = textSlateColor
                        )
                    },
                    supportingContent = {
                        Text(
                            "Authenticates telecallers ONLY through specific phone IMEI/Hardware ID parameters for uncompromised lines.",
                            color = textSlateMuted
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = isHardwareBindingEnabled,
                            onCheckedChange = { viewModel.toggleHardwareBinding(it) },
                            modifier = Modifier.testTag("hardware_binding_toggle")
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            // --- 3. Screen Capture & Screenshot Blocker ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            "Prevent Screen Captures & Screenshots",
                            fontWeight = FontWeight.Bold,
                            color = textSlateColor
                        )
                    },
                    supportingContent = {
                        Text(
                            "Blocks image cap inputs & restricts phone display logs using Activity FLAG_SECURE parameters.",
                            color = textSlateMuted
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = isScreenshotBlockEnabled,
                            onCheckedChange = { viewModel.toggleScreenshotBlock(it) },
                            modifier = Modifier.testTag("screenshot_block_toggle")
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            // --- THEME SELECTION BLOCK ---
            Text(
                text = "Application Branding Theme",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textSlateColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .testTag("theme_selection_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Customize the interface layout and coloration accent profiles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSlateMuted
                    )

                    val themes = listOf(
                        "system" to "🖥️ System",
                        "light" to "☀️ Light",
                        "dark" to "🌙 Dark",
                        "bento_slate" to "🌌 Bento Slate",
                        "crimson_warning" to "🚨 Crimson Warning",
                        "neon_emerald" to "💚 Neon Cyber"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themes.forEach { (mode, label) ->
                            val isSelected = themeMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("theme_chip_$mode"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. Remote Emergency Data Wipe Button ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = accentRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Dangerous Operations Unit",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    }

                    Text(
                        text = "Triggering a purge instantly wipes all debtor allocations, pending agreements, cached logs, and active session logins from this hand unit offline storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7F1D1D)
                    )

                    OutlinedButton(
                        onClick = { showPurgeDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = accentRed,
                            containerColor = Color.Transparent
                        ),
                        border = BorderStroke(1.dp, accentRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("emergency_purge_button")
                    ) {
                        Text(
                            text = "Emergency Data Purge",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG CONFIRMATION BOX ---
    if (showPurgeDialog) {
        AlertDialog(
            onDismissRequest = {
                showPurgeDialog = false
                purgeConfirmationText = ""
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = accentRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Confirm Emergency Purge",
                    fontWeight = FontWeight.Bold,
                    color = textSlateColor
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "This action is completely IRREVERSIBLE. To clear all local Room SQLite databases instantly and force terminate this session, type the confirmation keyword PURGE below:",
                        color = textSlateColor,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = purgeConfirmationText,
                        onValueChange = { purgeConfirmationText = it },
                        placeholder = { Text("Type PURGE to proceed") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purge_confirmation_text_field"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentRed,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (purgeConfirmationText == "PURGE") {
                            viewModel.executeEmergencyPurge {
                                showPurgeDialog = false
                                purgeConfirmationText = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentRed),
                    enabled = purgeConfirmationText == "PURGE",
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_purge_button")
                ) {
                    Text("PURGE ALL DATA", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPurgeDialog = false
                        purgeConfirmationText = ""
                    },
                    modifier = Modifier.testTag("dismiss_purge_button")
                ) {
                    Text("Cancel", color = textSlateColor)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFFF8FAFC)
        )
    }
}
