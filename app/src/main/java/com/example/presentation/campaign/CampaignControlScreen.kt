package com.example.presentation.campaign

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignControlScreen(
    viewModel: CampaignViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val metrics by viewModel.campaignMetrics.collectAsState()
    val isFreezeEnabled by viewModel.isDialerQueueFrozen.collectAsState()
    val selectedSourcePool by viewModel.selectedSourcePool.collectAsState()
    val selectedTargetAgent by viewModel.selectedTargetAgent.collectAsState()
    val isReallocating by viewModel.isReallocating.collectAsState()
    val resultMsg by viewModel.reallocationResultMsg.collectAsState()

    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    val primaryBlue = Color(0xFF2563EB)
    val textSlateColor = Color(0xFF1E293B)
    val textSlateMuted = Color(0xFF64748B)
    val accentRed = Color(0xFFEF4444)
    val successGreen = Color(0xFF10B981)

    // Notify user of re-allocation results
    LaunchedEffect(resultMsg) {
        resultMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearResultMsg()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("campaign_screen_container")
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Campaign & Routing",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("campaign_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textSlateColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // --- SECTION 1: ACTIVE CAMPAIGNS HEADER & HORIZONTAL CARDS ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Campaign Summary",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSlateColor
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFreezeEnabled) Color(0xFFFEE2E2) else Color(0xFFECFDF5))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isFreezeEnabled) "DIALERS PAUSED" else "DIALERS ACTIVE",
                            fontWeight = FontWeight.Bold,
                            color = if (isFreezeEnabled) accentRed else successGreen,
                            fontSize = 10.sp
                        )
                    }
                }

                // Horizontal metrics LazyRow
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(metrics) { metric ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .width(220.dp)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(metric.colorHex).copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = null,
                                            tint = Color(metric.colorHex),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    Text(
                                        text = "${metric.recoveryRate}% Rec",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (metric.recoveryRate > 30) successGreen else textSlateColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = metric.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textSlateColor
                                )

                                Text(
                                    text = metric.description,
                                    fontSize = 11.sp,
                                    color = textSlateMuted
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LinearProgressIndicator(
                                        progress = { metric.recoveryRate / 100f },
                                        color = Color(metric.colorHex),
                                        trackColor = Color(0xFFF1F5F9),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${metric.totalAccounts} Accts",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSlateColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- SECTION 2: BULK FREEZE SAFETY QUEUE SWITCH ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFreezeEnabled) Color(0xFFFFF1F2) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, if (isFreezeEnabled) Color(0xFFFECDD3) else Color(0xFFE2E8F0)),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom play/pause layout indicator
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isFreezeEnabled) Color(0xFFFECDD3) else Color(0xFFEFF6FF),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFreezeEnabled) {
                            // Custom Pause symbol drawing via Row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.size(14.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxHeight().width(4.dp).background(accentRed, RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.fillMaxHeight().width(4.dp).background(accentRed, RoundedCornerShape(1.dp)))
                            }
                        } else {
                            // Custom Play Arrow symbol
                            Text(
                                "▶",
                                color = primaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Freeze All Outbound Dialer Queues",
                            fontWeight = FontWeight.Bold,
                            color = textSlateColor,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "A master emergency override that halts all automated dialers and freezes contact logging across callers' handsets instantly.",
                            color = textSlateMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = isFreezeEnabled,
                        onCheckedChange = { viewModel.toggleDialerQueueFrozen(it) },
                        modifier = Modifier.testTag("dialer_freeze_safety_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = accentRed,
                            checkedTrackColor = Color(0xFFFCA5A5)
                        )
                    )
                }
            }

            // --- SECTION 3: DYNAMIC LEAD RE-ALLOCATION WORKFLOW ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = primaryBlue
                        )
                        Text(
                            text = "Delinquent Lead Re-Allocation Tool",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textSlateColor
                        )
                    }

                    Text(
                        text = "Migrate active collections or route unallocated debt pipelines to customized recovery tiers instantly.",
                        fontSize = 12.sp,
                        color = textSlateMuted
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Dropdown A: Select Source DPD Pool
                    Text(
                        text = "Select Source DPD Pool",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSlateColor
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = sourceExpanded,
                        onExpandedChange = { sourceExpanded = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("source_pool_dropdown_container")
                    ) {
                        OutlinedTextField(
                            value = selectedSourcePool,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("source_pool_dropdown_trigger"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = sourceExpanded,
                            onDismissRequest = { sourceExpanded = false }
                        ) {
                            viewModel.dpdPools.forEach { selection ->
                                DropdownMenuItem(
                                    text = { Text(selection) },
                                    onClick = {
                                        viewModel.setSelectedSourcePool(selection)
                                        sourceExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Dropdown B: Select Target Collection Agent Team
                    Text(
                        text = "Select Target Collection Agent Team",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSlateColor
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = targetExpanded,
                        onExpandedChange = { targetExpanded = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("target_agent_dropdown_container")
                    ) {
                        OutlinedTextField(
                            value = selectedTargetAgent,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("target_agent_dropdown_trigger"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = targetExpanded,
                            onDismissRequest = { targetExpanded = false }
                        ) {
                            viewModel.agentTeams.forEach { team ->
                                DropdownMenuItem(
                                    text = { Text(team) },
                                    onClick = {
                                        viewModel.setSelectedTargetAgent(team)
                                        targetExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Execution Action Button
                    Button(
                        onClick = { viewModel.triggerReAllocationWorkflow() },
                        enabled = !isReallocating,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("trigger_reallocation_action_button")
                    ) {
                        if (isReallocating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Text(
                                    text = "Trigger Re-Allocation Workflow",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
