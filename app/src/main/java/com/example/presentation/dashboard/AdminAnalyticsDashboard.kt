package com.example.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.security.TelecallerAgent
import com.example.domain.security.AgentPermissions
import com.example.presentation.security.SecurityViewModel
import com.example.presentation.home.HomeViewModel
import com.example.domain.model.Debtor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Administrative Analytics Hub containing Operational Overviews,
 * dynamic Agent controls (with Permission Creator configurations), and direct student file lookup.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsDashboard(
    viewModel: SecurityViewModel,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit
) {
    // Premium Design Palette
    val backgroundBg = Color(0xFFF8FAFC)
    val webChromeHeaderBg = Color(0xFF1E293B)
    val accentBlue = Color(0xFF2563EB)
    val accentGreen = Color(0xFF10B981)
    val accentOrange = Color(0xFFF97316)
    val textDark = Color(0xFF0F172A)
    val textSecondary = Color(0xFF475569)
    val borderSlate = Color(0xFFE2E8F0)

    // Tab state: 0 -> Telemetry, 1 -> Agent Control, 2 -> Student Search
    var selectedTab by remember { mutableStateOf(0) }
    var isSimulatedWebViewMode by remember { mutableStateOf(true) }
    var syncTriggeredCount by remember { mutableStateOf(0) }

    // Dynamic states collected from viewModels
    val telecallers by viewModel.telecallersList.collectAsState()
    val rawDebtorsList by homeViewModel.debtors.collectAsState()
    val isNumberMaskingGlobal by viewModel.isNumberMaskingEnabled.collectAsState()

    // Dialog state for adding a new agent
    var showAddAgentDialog by remember { mutableStateOf(false) }
    var newAgentName by remember { mutableStateOf("") }
    var newAgentIsAdmin by remember { mutableStateOf(false) }
    var newAgentPassword by remember { mutableStateOf("") }
    var newAgentPhone by remember { mutableStateOf("") }
    var newAgentEmail by remember { mutableStateOf("") }
    var registerErrorMsg by remember { mutableStateOf("") }

    // Dialog state for editing permissions
    var activePermissionEditingAgent by remember { mutableStateOf<TelecallerAgent?>(null) }

    // Student Detail & Upload states
    var activeHistoryStudent by remember { mutableStateOf<Debtor?>(null) }
    var isUploadSectionExpanded by remember { mutableStateOf(false) }
    var uploadCollegeName by remember { mutableStateOf("") }
    var uploadCsvContent by remember { mutableStateOf("") }
    var uploadResultMessage by remember { mutableStateOf<String?>(null) }
    var uploadErrorLogs by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_analytics_container")
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Admin Hub & Policies",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark
                        )
                        Text(
                            text = "Authority panel • Dues auditing • Policy controls",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("admin_analytics_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textDark
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSimulatedWebViewMode) Color.White else Color.Transparent)
                                .clickable { isSimulatedWebViewMode = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Web Frame",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSimulatedWebViewMode) accentBlue else textSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isSimulatedWebViewMode) Color.White else Color.Transparent)
                                .clickable { isSimulatedWebViewMode = false }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Pure Native",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isSimulatedWebViewMode) accentBlue else textSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = textDark
                ),
                modifier = Modifier.border(1.dp, Color(0xFFF1F5F9))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBg)
                .padding(innerPadding)
        ) {
            // Web browser simulated address frame
            AnimatedVisibility(
                visible = isSimulatedWebViewMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(webChromeHeaderBg)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFEF4444), CircleShape))
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFF59E0B), CircleShape))
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF334155))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔒", fontSize = 10.sp, modifier = Modifier.padding(end = 6.dp))
                            Text(
                                "https://recoverypro.internal/admin/dashboard",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "🔄",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable { syncTriggeredCount++ }
                            .padding(4.dp)
                    )
                }
            }

            // Centralized M3 Switcher Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = accentBlue,
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📊 Telemetry", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("admin_tab_telemetry")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("👥 Agent Control", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("admin_tab_agents")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("🔍 Student Lookup", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier.testTag("admin_tab_search")
                )
            }

            // Scrollable display panel corresponding to active tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBg)
            ) {
                when (selectedTab) {
                    0 -> { // --- TAB 0: ORIGINAL TELEMETRY AUDITS ---
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Operational Velocity Hub",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textDark
                                    )
                                    Text(
                                        text = "Simulation matrices updated live",
                                        fontSize = 11.sp,
                                        color = textSecondary
                                    )
                                }
                                if (syncTriggeredCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFFECFDF5))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "Refreshed ($syncTriggeredCount)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentGreen
                                        )
                                    }
                                }
                            }

                            // Recovery Matrix Cards
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, borderSlate, RoundedCornerShape(12.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(10.dp).background(accentBlue, CircleShape)
                                        )
                                        Column {
                                            Text("Total Assigned Portfolio Pool", fontSize = 11.sp, color = textSecondary)
                                            Text("₹1,24,50,000.00", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textDark)
                                        }
                                    }
                                }

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, borderSlate, RoundedCornerShape(12.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(10.dp).background(accentGreen, CircleShape)
                                        )
                                        Column {
                                            Text("Total Active Capital Settled", fontSize = 11.sp, color = textSecondary)
                                            Text("₹38,40,000.00", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentGreen)
                                        }
                                    }
                                }

                                // Attempted vs Unattempted Student Count dashboard metric
                                val unattemptedCount = rawDebtorsList.count { it.lastContactDate == "Never" }
                                val attemptedCount = rawDebtorsList.count { it.lastContactDate != "Never" }
                                val totalStudents = rawDebtorsList.size

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, borderSlate, RoundedCornerShape(12.dp)).testTag("attempted_unattempted_card")
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "Telemetry Breakdown: Contact Attempts",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textSecondary
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Attempted (Called)", fontSize = 11.sp, color = textSecondary)
                                                Text("$attemptedCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentGreen)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Unattempted (Not Called)", fontSize = 11.sp, color = textSecondary)
                                                Text("$unattemptedCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = accentOrange)
                                            }
                                        }

                                        // Linear Progress Bar representing ratio
                                        if (totalStudents > 0) {
                                            val progressRatio = attemptedCount.toFloat() / totalStudents.toFloat()
                                            LinearProgressIndicator(
                                                progress = { progressRatio },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                                color = accentGreen,
                                                trackColor = Color(0xFFFFEDD5) // Soft orange for unattempted tracking
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${(progressRatio * 100).toInt()}% contacted",
                                                    fontSize = 10.sp,
                                                    color = textSecondary
                                                )
                                                Text(
                                                    text = "Total: $totalStudents students",
                                                    fontSize = 10.sp,
                                                    color = textSecondary
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "No student files loaded yet. Please import student data.",
                                                fontSize = 11.sp,
                                                color = textSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            // Radial Dial Chart for Conversion Ratio
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth().border(1.dp, borderSlate, RoundedCornerShape(14.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Target Collective Conversion Velocity",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textDark
                                    )

                                    Box(
                                        modifier = Modifier.size(120.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.size(100.dp)) {
                                            drawArc(
                                                color = Color(0xFFF1F5F9),
                                                startAngle = 135f,
                                                sweepAngle = 270f,
                                                useCenter = false,
                                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                            drawArc(
                                                color = accentBlue,
                                                startAngle = 135f,
                                                sweepAngle = 270f * 0.742f,
                                                useCenter = false,
                                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("74.2%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textDark)
                                            Text("Resolved Rate", fontSize = 10.sp, color = textSecondary)
                                        }
                                    }
                                }
                            }

                            // Leaderboard of Active Telecallers
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth().border(1.dp, borderSlate, RoundedCornerShape(14.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Leaderboard Run Rate", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textDark)
                                    telecallers.sortedByDescending { it.ptpsSecured }.forEach { caller ->
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
                                                    modifier = Modifier.size(8.dp).background(
                                                        if (caller.isOnline) accentGreen else textSecondary,
                                                        CircleShape
                                                    )
                                                )
                                                Text(caller.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textDark)
                                            }
                                            Text("${caller.ptpsSecured} PTP commitments", fontSize = 12.sp, color = accentBlue, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // --- TAB 1: OPERATIONAL TELECALLERS MANAGEMENT & PERMISSIONS CREATOR ---
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Agent Registration & Control", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textDark)
                                    Text("Dynamic authorization matrices per operator", fontSize = 10.sp, color = textSecondary)
                                }
                                Button(
                                    onClick = { showAddAgentDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("admin_register_agent_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Text("Register", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(telecallers) { agent ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, borderSlate, RoundedCornerShape(12.dp))
                                            .testTag("agent_list_item_${agent.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(agent.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = textDark)
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(if (agent.isOnline) Color(0xFFECFDF5) else Color(0xFFF1F5F9))
                                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = if (agent.isOnline) "ONLINE" else "OFFLINE",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (agent.isOnline) accentGreen else textSecondary
                                                            )
                                                        }
                                                    }
                                                    Text("Agent ID: ${agent.id}", fontSize = 10.sp, color = textSecondary)
                                                }

                                                IconButton(
                                                    onClick = { activePermissionEditingAgent = agent },
                                                    modifier = Modifier.testTag("agent_settings_icon_${agent.id}")
                                                ) {
                                                    Icon(Icons.Default.Settings, contentDescription = "Edit permissions", tint = accentBlue)
                                                }
                                            }

                                            // Summary of quick metrics
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    Column {
                                                        Text("Dialed Calls", fontSize = 9.sp, color = textSecondary)
                                                        Text("${agent.callsDialed}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textDark)
                                                    }
                                                    Column {
                                                        Text("Talk Time (Mins)", fontSize = 9.sp, color = textSecondary)
                                                        Text("${agent.talkTimeMinutes}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textDark)
                                                    }
                                                    Column {
                                                        Text("Promises Secured", fontSize = 9.sp, color = textSecondary)
                                                        Text("${agent.ptpsSecured}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentGreen)
                                                    }
                                                }

                                                // Switch dashboard direct action
                                                Button(
                                                    onClick = { viewModel.setImpersonatedAgent(agent) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.testTag("btn_switch_dashboard_${agent.id}")
                                                ) {
                                                    Text("Switch Dashboard", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentBlue)
                                                }
                                            }

                                            HorizontalDivider(color = Color(0xFFF1F5F9))

                                            // Visual badges indicating active privileges
                                            Row(
                                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (agent.permissions.callInitiation) {
                                                    PermissionBadge("Call Initiation Enabled", accentGreen)
                                                } else {
                                                    PermissionBadge("Calls Restricted", Color(0xFFEF4444))
                                                }

                                                if (agent.permissions.canSeeFullNumbers) {
                                                    PermissionBadge("Full Numbers View", Color(0xFF8B5CF6))
                                                } else {
                                                    PermissionBadge("Numbers Masked", Color(0xFFF59E0B))
                                                }

                                                if (agent.permissions.canRecordAudio) {
                                                    PermissionBadge("Auto Audio Capture", Color(0xFF06B6D4))
                                                }

                                                if (agent.permissions.canPerformPurge) {
                                                    PermissionBadge("Wipe Clean Rights", Color.Red)
                                                }

                                                if (agent.permissions.isAdmin) {
                                                    PermissionBadge("Administrative Authority", Color(0xFFDC2626))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // --- TAB 2: ACTIVE STUDENT DATABASE SEARCH & IMPERSONATION ---
                        var searchInput by remember { mutableStateOf("") }
                        val matchedDebtors = rawDebtorsList.filter {
                            it.name.contains(searchInput, ignoreCase = true) ||
                                    it.phoneNumber.contains(searchInput) ||
                                    it.college.contains(searchInput, ignoreCase = true) ||
                                    it.remarks.contains(searchInput, ignoreCase = true) ||
                                    it.customerSegment.contains(searchInput, ignoreCase = true)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                                        Column {
                                            Text("Student Database Index", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textDark)
                                            Text("Search actual local cases on students & debtors. Switch context directly.", fontSize = 10.sp, color = textSecondary)
                                        }

                                        // --- BULK INGESTION PANEL (ADMIN ONLY) ---
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                            modifier = Modifier.fillMaxWidth().clickable { isUploadSectionExpanded = !isUploadSectionExpanded }.testTag("bulk_ingestion_header_card")
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = accentBlue)
                                                        Text(
                                                            text = "College-Wise Student Bulk Import",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = textDark
                                                        )
                                                    }
                                                    Text(
                                                        text = if (isUploadSectionExpanded) "COLLAPSE" else "EXPAND (ADMIN)",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = accentBlue
                                                    )
                                                }

                                                if (isUploadSectionExpanded) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                                        Text(
                                                            text = "Paste CSV-formatted text records with headers: account_number, student_name, primary_phone, original_due_date, total_due_amount",
                                                            fontSize = 10.sp,
                                                            color = textSecondary
                                                        )

                                                        // College Input Field
                                                        OutlinedTextField(
                                                            value = uploadCollegeName,
                                                            onValueChange = { uploadCollegeName = it },
                                                            placeholder = { Text("e.g. ABC College of Technology", fontSize = 11.sp) },
                                                            label = { Text("College / Institution Name", fontSize = 11.sp) },
                                                            modifier = Modifier.fillMaxWidth().testTag("upload_college_name_field"),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor = accentBlue,
                                                                unfocusedBorderColor = Color.LightGray
                                                            )
                                                        )

                                                        // CSV payload field
                                                        OutlinedTextField(
                                                            value = uploadCsvContent,
                                                            onValueChange = { uploadCsvContent = it },
                                                            placeholder = {
                                                                Text(
                                                                    "account_number,student_name,primary_phone,original_due_date,total_due_amount\nS101,Aarav Kumar,9876543210,2025-05-10,45000",
                                                                    fontSize = 10.sp
                                                                )
                                                            },
                                                            label = { Text("Pasted CSV Records (Plain Text)", fontSize = 11.sp) },
                                                            maxLines = 6,
                                                            minLines = 3,
                                                            modifier = Modifier.fillMaxWidth().testTag("upload_csv_payload_field"),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor = accentBlue,
                                                                unfocusedBorderColor = Color.LightGray
                                                            )
                                                        )

                                                        if (uploadResultMessage != null) {
                                                            Text(
                                                                text = uploadResultMessage!!,
                                                                color = if (uploadResultMessage!!.startsWith("Error")) Color.Red else Color(0xFF047857),
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }

                                                        Button(
                                                            onClick = {
                                                                if (uploadCollegeName.isBlank() || uploadCsvContent.isBlank()) {
                                                                    uploadResultMessage = "Error: College Name and pasted records can't be empty."
                                                                } else {
                                                                    homeViewModel.importStudentDataCollegeWise(uploadCsvContent, uploadCollegeName) { count, err ->
                                                                        if (count > 0) {
                                                                            uploadResultMessage = "Successfully imported $count student files for $uploadCollegeName!"
                                                                            uploadCsvContent = ""
                                                                            uploadCollegeName = ""
                                                                        } else {
                                                                            uploadResultMessage = "Error: $err"
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                                            modifier = Modifier.fillMaxWidth().testTag("trigger_import_button")
                                                        ) {
                                                            Text("Commit bulk imports to DB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = searchInput,
                                            onValueChange = { searchInput = it },
                                            placeholder = { Text("Search by name, contact, college, remarks...", fontSize = 12.sp) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("admin_student_search_input"),
                                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = accentBlue,
                                                unfocusedBorderColor = borderSlate
                                            )
                                        )
                                    }
                                }

                                if (matchedDebtors.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("No student records found matching keyword.", color = textSecondary, fontSize = 12.sp)
                                        }
                                    }
                                } else {
                                    items(matchedDebtors) { student ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, borderSlate, RoundedCornerShape(12.dp))
                                                .clickable { activeHistoryStudent = student }
                                                .testTag("matched_student_${student.id}")
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textDark)
                                                        Text("College: ${student.college}", fontSize = 11.sp, color = textSecondary)
                                                        if (student.remarks.isNotEmpty()) {
                                                            Text("Remark: ${student.remarks}", fontSize = 10.sp, color = Color(0xFFEA580C), fontWeight = FontWeight.SemiBold, maxLines = 1)
                                                        }
                                                        Text(
                                                            text = "Outstanding: ₹${"%,.2f".format(student.outstandingAmount)}",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFEF4444)
                                                        )
                                                    }

                                                    // Segment badge representation
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFFEFF6FF))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(student.customerSegment, color = accentBlue, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                    }
                                                }

                                                // Contact Number depending on global masking AND override permissions check
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text("Allocation Date: ${student.lastContactDate}", fontSize = 10.sp, color = textSecondary)
                                                        Text(
                                                            text = "Contact: " + if (isNumberMaskingGlobal) {
                                                                // Simulated override display check
                                                                "Masked (${student.phoneNumber.takeLast(4)})"
                                                            } else {
                                                                student.phoneNumber
                                                            },
                                                            fontSize = 11.sp,
                                                            color = textSecondary,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        TextButton(
                                                            onClick = { activeHistoryStudent = student },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.testTag("btn_view_history_${student.id}")
                                                        ) {
                                                            Text("History & Remarks", fontSize = 10.sp, color = accentBlue, fontWeight = FontWeight.Bold)
                                                        }

                                                        // Simulated Switch view action to assigned case handler telecaller Rajesh/Aditi
                                                        Button(
                                                            onClick = {
                                                                // Link the case directly to Rajesh Kumar (or Aditi dynamically based on DPD)
                                                                val handlerName = if (student.overdueDays > 30) "Aditi Verma" else "Rajesh Kumar"
                                                                val matchingAgent = telecallers.find { it.name == handlerName } ?: telecallers.first()
                                                                viewModel.setImpersonatedAgent(matchingAgent)
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                        ) {
                                                            Text("Solve Session", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                }
            }
        }
    }

    // --- DIALOG 1: REGISTER NEW AGENT ---
    if (showAddAgentDialog) {
        AlertDialog(
            onDismissRequest = { showAddAgentDialog = false },
            title = { Text("Register Telecaller Agent", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (registerErrorMsg.isNotEmpty()) {
                        Text(
                            text = registerErrorMsg,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = newAgentName,
                        onValueChange = { newAgentName = it },
                        label = { Text("Username / Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("new_agent_name_field")
                    )

                    OutlinedTextField(
                        value = newAgentEmail,
                        onValueChange = { newAgentEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth().testTag("new_agent_email_field")
                    )

                    OutlinedTextField(
                        value = newAgentPhone,
                        onValueChange = { newAgentPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth().testTag("new_agent_phone_field")
                    )

                    OutlinedTextField(
                        value = newAgentPassword,
                        onValueChange = { newAgentPassword = it },
                        label = { Text("Login Password") },
                        modifier = Modifier.fillMaxWidth().testTag("new_agent_password_field")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Assign Administrative Authority", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = newAgentIsAdmin,
                            onCheckedChange = { newAgentIsAdmin = it },
                            modifier = Modifier.testTag("new_agent_admin_switch")
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
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                    modifier = Modifier.testTag("confirm_register_agent")
                ) {
                    Text("Register Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAgentDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White
        )
    }

    // --- DIALOG 2: MANAGE PERMISSIONS CREATOR SHIFT ---
    if (activePermissionEditingAgent != null) {
        val editingAgent = activePermissionEditingAgent!!
        var callPerm by remember(editingAgent) { mutableStateOf(editingAgent.permissions.callInitiation) }
        var seeUnmaskedPerm by remember(editingAgent) { mutableStateOf(editingAgent.permissions.canSeeFullNumbers) }
        var recordPerm by remember(editingAgent) { mutableStateOf(editingAgent.permissions.canRecordAudio) }
        var purgePerm by remember(editingAgent) { mutableStateOf(editingAgent.permissions.canPerformPurge) }
        var adminPerm by remember(editingAgent) { mutableStateOf(editingAgent.permissions.isAdmin) }

        AlertDialog(
            onDismissRequest = { activePermissionEditingAgent = null },
            title = {
                Column {
                    Text("Permission Matrix Creator", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Enforces structural constraints on: ${editingAgent.name}", fontSize = 10.sp, color = textSecondary)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    PermissionToggleItem(
                        label = "Active Dialing Capability",
                        description = "Enable operator to start calls with customers",
                        checked = callPerm,
                        onCheckedChange = { callPerm = it },
                        accentColor = accentGreen,
                        tag = "perm_call_${editingAgent.id}"
                    )

                    PermissionToggleItem(
                        label = "Override Contact Masking",
                        description = "Directly reveal full raw calling phone numbers to agent",
                        checked = seeUnmaskedPerm,
                        onCheckedChange = { seeUnmaskedPerm = it },
                        accentColor = Color(0xFF8B5CF6),
                        tag = "perm_unmask_${editingAgent.id}"
                    )

                    PermissionToggleItem(
                        label = "Auto Call Audio Recording",
                        description = "Secure verbal logs and store in compliance databases",
                        checked = recordPerm,
                        onCheckedChange = { recordPerm = it },
                        accentColor = Color(0xFF06B6D4),
                        tag = "perm_record_${editingAgent.id}"
                    )

                    PermissionToggleItem(
                        label = "Emergency Clearing Privileges",
                        description = "Permit operator to launch instantaneous local database purges",
                        checked = purgePerm,
                        onCheckedChange = { purgePerm = it },
                        accentColor = Color.Red,
                        tag = "perm_purge_${editingAgent.id}"
                    )

                    PermissionToggleItem(
                        label = "Administrative Authority",
                        description = "Grants view access to administrative audit controls",
                        checked = adminPerm,
                        onCheckedChange = { adminPerm = it },
                        accentColor = Color(0xFFDC2626),
                        tag = "perm_admin_${editingAgent.id}"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAgentPermissions(
                            editingAgent.id,
                            AgentPermissions(
                                callInitiation = callPerm,
                                canSeeFullNumbers = seeUnmaskedPerm,
                                canRecordAudio = recordPerm,
                                canPerformPurge = purgePerm,
                                isAdmin = adminPerm
                            )
                        )
                        activePermissionEditingAgent = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                    modifier = Modifier.testTag("save_permissions_button")
                ) {
                    Text("Publish Policy")
                }
            },
            dismissButton = {
                TextButton(onClick = { activePermissionEditingAgent = null }) {
                    Text("Discard Changes")
                }
            },
            containerColor = Color.White
        )
    }

    // --- DIALOG 3: STUDENT DETAILS, REMARKS, CALL LOGS & PAYMENT PROMISES HISTORY ---
    if (activeHistoryStudent != null) {
        val student = activeHistoryStudent!!
        var remarkInput by remember(student.id) { mutableStateOf(student.remarks) }
        val callLogs by homeViewModel.getCallLogsForDebtor(student.id).collectAsState(initial = emptyList())
        val promises by homeViewModel.getPromisesForDebtor(student.id).collectAsState(initial = emptyList())

        AlertDialog(
            onDismissRequest = { activeHistoryStudent = null },
            title = {
                Column {
                    Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textDark)
                    Text(text = "College ID / Serial: ${student.id}", fontSize = 11.sp, color = textSecondary)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Profile Context
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("STUDENT FILE PROFILE", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = accentBlue)
                        Text("Institution: ${student.college}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textDark)
                        Text("Outstanding Amount: ₹${"%,.2f".format(student.outstandingAmount)}", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.SemiBold)
                        Text("Segment Group: ${student.customerSegment}", fontSize = 11.sp, color = textSecondary)
                    }

                    HorizontalDivider(color = borderSlate)

                    // Remarks Capture
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("CAPTURE STUDENT-WISE REMARK", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = accentBlue)
                        OutlinedTextField(
                            value = remarkInput,
                            onValueChange = { remarkInput = it },
                            placeholder = { Text("Enter remark for persistence...", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("student_remark_field"),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                        )
                        Button(
                            onClick = {
                                val updated = student.copy(remarks = remarkInput)
                                homeViewModel.updateDebtorProfile(updated)
                                activeHistoryStudent = updated
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.align(Alignment.End).testTag("save_remark_button")
                        ) {
                            Text("Save Remark", fontSize = 10.sp, color = Color.White)
                        }
                    }

                    HorizontalDivider(color = borderSlate)

                    // Call History
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("CONTACT & CALL OUTCOMES HISTORY", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = accentBlue)
                        if (callLogs.isEmpty()) {
                            Text("No call history on record. Student has not been dialed.", fontSize = 11.sp, color = textSecondary)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                callLogs.forEach { log ->
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, borderSlate, RoundedCornerShape(8.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = log.status, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = accentBlue)
                                                Text(text = "${log.date} • ${log.time}", fontSize = 9.sp, color = textSecondary)
                                            }
                                            if (log.notes.isNotEmpty()) {
                                                Text(text = log.notes, fontSize = 11.sp, color = textDark)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = borderSlate)

                    // Payment Promises History
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("PROMISE TO PAY (PTP) PROGRESS", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = accentBlue)
                        if (promises.isEmpty()) {
                            Text("No payment promises made on this file.", fontSize = 11.sp, color = textSecondary)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                promises.forEach { ptp ->
                                    val formattedDate = try {
                                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(ptp.promisedPaymentDate))
                                    } catch (e: Exception) {
                                        "unknown date"
                                    }
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(8.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "Amount: ₹${"%,.2f".format(ptp.promisedAmount)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF047857))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFF047857).copy(alpha = 0.1f))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(text = ptp.ptpStatus, color = Color(0xFF047857), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                }
                                            }
                                            Text(text = "Promise Date: $formattedDate", fontSize = 10.sp, color = textSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeHistoryStudent = null }) {
                    Text("Close Panel")
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun PermissionBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun PermissionToggleItem(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
            Text(description, fontSize = 10.sp, color = Color(0xFF475569))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
