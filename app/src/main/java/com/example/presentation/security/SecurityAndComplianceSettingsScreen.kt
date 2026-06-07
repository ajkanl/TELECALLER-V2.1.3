package com.example.presentation.security

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val telecallers by viewModel.telecallersList.collectAsState()

    var showPurgeDialog by remember { mutableStateOf(false) }
    var purgeConfirmationText by remember { mutableStateOf("") }

    // --- NEW: Agent management states ---
    var showAddAgentDialog by remember { mutableStateOf(false) }
    var newAgentName by remember { mutableStateOf("") }
    var newAgentEmail by remember { mutableStateOf("") }
    var newAgentPhone by remember { mutableStateOf("") }
    var newAgentPassword by remember { mutableStateOf("") }
    var newAgentIsAdmin by remember { mutableStateOf(false) }
    var registerErrorMsg by remember { mutableStateOf("") }
    var activePermissionEditingAgent by remember { mutableStateOf<com.example.domain.security.TelecallerAgent?>(null) }
    var showDeleteConfirmationAgent by remember { mutableStateOf<com.example.domain.security.TelecallerAgent?>(null) }
    val accentBlue = Color(0xFF3B82F6)
    val accentGreen = Color(0xFF10B981)

    val primaryBlue = Color(0xFF3B82F6)
    val textSlateColor = Color(0xFFF8FAFC)
    val textSlateMuted = Color(0xFF94A3B8)
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
                modifier = Modifier.border(1.dp, Color(0xFF1E293D))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090F1C))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293D), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF1D2D44), RoundedCornerShape(12.dp)),
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

            // --- 1. Debtor Contact Number Masking Toggle ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293D), RoundedCornerShape(12.dp))
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
                    .border(1.dp, Color(0xFF1E293D), RoundedCornerShape(12.dp))
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
                    .border(1.dp, Color(0xFF1E293D), RoundedCornerShape(12.dp))
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
                    .border(1.dp, Color(0xFF1E293D), RoundedCornerShape(12.dp))
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

            // --- AGENT ADMINISTRATION & DIRECTORY ---
            Text(
                text = "Agent Registry & Credentials",
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
                    .border(1.dp, Color(0xFF1E293D), RoundedCornerShape(12.dp))
                    .testTag("admin_agent_mgmt_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Manage Operators & Authority",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textSlateColor
                        )

                        // REGISTER NEW AGENT BUTTON
                        Button(
                            onClick = { showAddAgentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("admin_add_agent_button_settings")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("Add Agent", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Text(
                        text = "Register new telecallers or administer existing profiles, compliance authorizations, targets, and operational status.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSlateMuted
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E293D)))

                    // Agent Cards List mapped inside column
                    if (telecallers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No registered agents found.", color = textSlateMuted, fontSize = 12.sp)
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            telecallers.forEach { agent ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF1E293E), RoundedCornerShape(10.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(if (agent.permissions.isAdmin) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF64748B).copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = if (agent.permissions.isAdmin) Color(0xFF60A5FA) else Color(0xFF94A3B8),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                Column {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = agent.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = textSlateColor
                                                        )

                                                        if (agent.permissions.isAdmin) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                                            ) {
                                                                Text("ADMIN", fontSize = 8.sp, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = "ID: ${agent.id} • ${if (agent.isOnline) "🟢 Active" else "⚪ Offline"}",
                                                        fontSize = 10.sp,
                                                        color = textSlateMuted
                                                    )
                                                }
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // EDIT PERMISSIONS BUTTON
                                                IconButton(
                                                    onClick = { activePermissionEditingAgent = agent },
                                                    modifier = Modifier.size(32.dp).testTag("edit_agent_perms_btn_${agent.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Settings,
                                                        contentDescription = "Edit Permissions",
                                                        tint = textSlateMuted,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                // DELETE/REMOVE BUTTON
                                                IconButton(
                                                    onClick = { showDeleteConfirmationAgent = agent },
                                                    modifier = Modifier.size(32.dp).testTag("delete_agent_btn_${agent.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Agent",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Mini info row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(
                                                    text = "Dialed: ${agent.callsDialed}",
                                                    fontSize = 10.sp,
                                                    color = textSlateMuted
                                                )
                                                Text(
                                                    text = "Secured: ${agent.ptpsSecured}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF10B981)
                                                )
                                                Text(
                                                    text = "Target: ₹${agent.targetAmount}",
                                                    fontSize = 10.sp,
                                                    color = textSlateMuted
                                                )
                                            }
                                            
                                            if (agent.isDisabled) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text("DISABLED", fontSize = 8.sp, color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. Remote Emergency Data Wipe Button ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF991B1B).copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
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
                            color = Color(0xFFFCA5A5)
                        )
                    }

                    Text(
                        text = "Triggering a purge instantly wipes all debtor allocations, pending agreements, cached logs, and active session logins from this hand unit offline storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFEE2E2)
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
                            unfocusedBorderColor = Color(0xFF1E293D),
                            focusedTextColor = textSlateColor,
                            unfocusedTextColor = textSlateColor
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
            containerColor = Color(0xFF172033)
        )
    }

    // --- ADD AGENT DIALOG ---
    if (showAddAgentDialog) {
        AlertDialog(
            onDismissRequest = { showAddAgentDialog = false },
            title = {
                Text(
                    text = "Register Telecaller Agent",
                    fontWeight = FontWeight.Bold,
                    color = textSlateColor,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (registerErrorMsg.isNotBlank()) {
                        Text(registerErrorMsg, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = newAgentName,
                        onValueChange = { newAgentName = it },
                        label = { Text("Agent Username") },
                        modifier = Modifier.fillMaxWidth().testTag("new_agent_name_field_settings"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFF1E293D),
                            focusedTextColor = textSlateColor,
                            unfocusedTextColor = textSlateColor
                        )
                    )

                    OutlinedTextField(
                        value = newAgentEmail,
                        onValueChange = { newAgentEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth().testTag("new_agent_email_field_settings"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFF1E293D),
                            focusedTextColor = textSlateColor,
                            unfocusedTextColor = textSlateColor
                        )
                    )

                    OutlinedTextField(
                        value = newAgentPhone,
                        onValueChange = { newAgentPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth().testTag("new_agent_phone_field_settings"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFF1E293D),
                            focusedTextColor = textSlateColor,
                            unfocusedTextColor = textSlateColor
                        )
                    )

                    OutlinedTextField(
                        value = newAgentPassword,
                        onValueChange = { newAgentPassword = it },
                        label = { Text("Login Password") },
                        modifier = Modifier.fillMaxWidth().testTag("new_agent_password_field_settings"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFF1E293D),
                            focusedTextColor = textSlateColor,
                            unfocusedTextColor = textSlateColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grant Administrative Authority", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textSlateColor)
                        Switch(
                            checked = newAgentIsAdmin,
                            onCheckedChange = { newAgentIsAdmin = it },
                            modifier = Modifier.testTag("new_agent_admin_switch_settings")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAgentName.isNotBlank() && newAgentPassword.isNotBlank()) {
                            viewModel.registerTelecaller(
                                name = newAgentName,
                                passwordPlain = newAgentPassword,
                                email = newAgentEmail,
                                phoneNumber = newAgentPhone,
                                isAdmin = newAgentIsAdmin
                            ) { result ->
                                if (result.isSuccess) {
                                    newAgentName = ""
                                    newAgentPassword = ""
                                    newAgentPhone = ""
                                    newAgentEmail = ""
                                    registerErrorMsg = ""
                                    showAddAgentDialog = false
                                } else {
                                    registerErrorMsg = result.exceptionOrNull()?.localizedMessage ?: "Failed to register"
                                }
                            }
                        } else {
                            registerErrorMsg = "Name and Password cannot be empty."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    modifier = Modifier.testTag("confirm_register_agent_settings")
                ) {
                    Text("Register Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAgentDialog = false }) {
                    Text("Cancel", color = textSlateColor)
                }
            },
            containerColor = Color(0xFF172033),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- CONFIRM DELETE AGENT DIALOG ---
    if (showDeleteConfirmationAgent != null) {
        val targetAgent = showDeleteConfirmationAgent!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationAgent = null },
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
                    text = "Purge and Delete Agent",
                    fontWeight = FontWeight.Bold,
                    color = textSlateColor,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Are you absolutely sure you want to remove the agent '${targetAgent.name}'? This wipes their permissions from storage, removes their database profile permanently, and prevents them from logging back in.",
                    color = textSlateColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeTelecaller(targetAgent.name) { result ->
                            showDeleteConfirmationAgent = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete_agent_btn")
                ) {
                    Text("Delete Permanently", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmationAgent = null }
                ) {
                    Text("Cancel", color = textSlateColor)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF172033)
        )
    }

    // --- AUTHORITY MATRIX DIALOG ---
    if (activePermissionEditingAgent != null) {
        val editingAgent = activePermissionEditingAgent!!
        var callPerm by remember(editingAgent) { mutableStateOf(editingAgent.permissions.callInitiation) }
        var seeUnmaskedPerm by remember(editingAgent) { mutableStateOf(editingAgent.permissions.canSeeFullNumbers) }
        var recordPerm by remember(editingAgent) { mutableStateOf(editingAgent.permissions.canRecordAudio) }
        var purgePerm by remember(editingAgent) { mutableStateOf(editingAgent.permissions.canPerformPurge) }
        var adminPerm by remember(editingAgent) { mutableStateOf(editingAgent.permissions.isAdmin) }
        var agentDisabledVal by remember(editingAgent) { mutableStateOf(editingAgent.isDisabled) }
        var targetAmtStr by remember(editingAgent) { mutableStateOf(editingAgent.targetAmount.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { activePermissionEditingAgent = null },
            title = {
                Column {
                    Text("Authority & Rules Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textSlateColor)
                    Text("Configuring operator constraints: ${editingAgent.name}", fontSize = 10.sp, color = textSlateMuted)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Outlined target amount modifier
                    OutlinedTextField(
                        value = targetAmtStr,
                        onValueChange = { targetAmtStr = it },
                        label = { Text("Monthly Collection Target (₹)") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_agent_target_field"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFF1E293D),
                            focusedTextColor = textSlateColor,
                            unfocusedTextColor = textSlateColor
                        )
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E293D)).padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Active Dialing Capability", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                            Text("Enable operator to start calls with customers", fontSize = 9.sp, color = textSlateMuted)
                        }
                        Switch(
                            checked = callPerm,
                            onCheckedChange = { callPerm = it },
                            modifier = Modifier.testTag("toggle_call_perm")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Override Contact Masking", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                            Text("Directly reveal full raw calling phone numbers to agent", fontSize = 9.sp, color = textSlateMuted)
                        }
                        Switch(
                            checked = seeUnmaskedPerm,
                            onCheckedChange = { seeUnmaskedPerm = it },
                            modifier = Modifier.testTag("toggle_unmask_perm")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto Call Audio Recording", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                            Text("Secure verbal logs and store in compliance databases", fontSize = 9.sp, color = textSlateMuted)
                        }
                        Switch(
                            checked = recordPerm,
                            onCheckedChange = { recordPerm = it },
                            modifier = Modifier.testTag("toggle_record_perm")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Emergency Clearing Privileges", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                            Text("Permit operator to launch instantaneous local database purges", fontSize = 9.sp, color = textSlateMuted)
                        }
                        Switch(
                            checked = purgePerm,
                            onCheckedChange = { purgePerm = it },
                            modifier = Modifier.testTag("toggle_purge_perm")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Administrative Authority", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                            Text("Grants view access to administrative audit controls", fontSize = 9.sp, color = textSlateMuted)
                        }
                        Switch(
                            checked = adminPerm,
                            onCheckedChange = { adminPerm = it },
                            modifier = Modifier.testTag("toggle_admin_perm")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Disable Agent Account", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                            Text("Freezes this account and suspends access rights", fontSize = 9.sp, color = textSlateMuted)
                        }
                        Switch(
                            checked = agentDisabledVal,
                            onCheckedChange = { agentDisabledVal = it },
                            modifier = Modifier.testTag("toggle_disable_perm")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetVal = targetAmtStr.toDoubleOrNull() ?: editingAgent.targetAmount
                        viewModel.updateAgentTarget(editingAgent.id, targetVal)
                        viewModel.updateAgentPermissions(
                            agentId = editingAgent.id,
                            permissions = com.example.domain.security.AgentPermissions(
                                callInitiation = callPerm,
                                canSeeFullNumbers = seeUnmaskedPerm,
                                canRecordAudio = recordPerm,
                                canPerformPurge = purgePerm,
                                isAdmin = adminPerm
                            ),
                            isDisabled = agentDisabledVal
                        )
                        activePermissionEditingAgent = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    modifier = Modifier.testTag("save_agent_permissions_settings_btn")
                ) {
                    Text("Apply Policies")
                }
            },
            dismissButton = {
                TextButton(onClick = { activePermissionEditingAgent = null }) {
                    Text("Cancel", color = textSlateColor)
                }
            },
            containerColor = Color(0xFF172033),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
