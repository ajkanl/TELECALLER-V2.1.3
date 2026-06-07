package com.example.presentation.dashboard

import android.widget.Toast
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Send
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DebtorEntity
import com.example.domain.model.Debtor
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDashboardScreen(
    viewModel: AgentDashboardViewModel,
    onBack: () -> Unit,
    onCallDebtor: (Debtor) -> Unit
) {
    val context = LocalContext.current
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val syncResultMessage by viewModel.syncResultMessage.collectAsState()

    val allDebtors by viewModel.allDebtorsList.collectAsState()
    val amountRecovered by viewModel.amountRecoveredToday.collectAsState()
    val targetAssigned by viewModel.targetAssignedToday.collectAsState()
    val totalCalls by viewModel.totalCallsMade.collectAsState()
    val pendingCount by viewModel.pendingLeads.collectAsState()
    val buckets by viewModel.bucketCounts.collectAsState()
    val promises by viewModel.promisesDueToday.collectAsState()
    val activeImpersonatedAgent by viewModel.activeImpersonatedAgent.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var selectedFilterBucket by remember { mutableStateOf<String?>(null) }
    var isSheetOpen by remember { mutableStateOf(false) }

    var selectedDashboardSection by remember { mutableStateOf("Leaderboard") } // "Leaderboard" or "Outgoing Logs"
    var selectedDatePeriod by remember { mutableStateOf("TODAY") } // "TODAY", "YESTERDAY", "THIS WEEK", "THIS MONTH", "THIS YEAR", "DATE RANGE"
    var activeLogFilter by remember { mutableStateOf<String?>(null) } // null, "INBOUND", "OUTBOUND", "Personal", "Business"

    val sdfFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }
    val initialDateStr = remember { sdfFormat.format(java.util.Date()) }
    var inputFromDate by remember { mutableStateOf(initialDateStr) }
    var inputToDate by remember { mutableStateOf(initialDateStr) }

    val recentCalls by viewModel.recentCalls.collectAsState()
    val telecallersList by viewModel.telecallersList.collectAsState()
    val cardBorderColor = Color(0xFF334155)

    val isDateInPeriod = remember {
        { dateStr: String, period: String, fDateStr: String, tDateStr: String ->
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val recordDate = try { sdf.parse(dateStr) } catch (e: Exception) { null }
            if (recordDate == null) {
                false
            } else {
                val recordCal = java.util.Calendar.getInstance().apply {
                    time = recordDate
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                // Base "today" is "2026-06-03" (metadata context date)
                val baseDate = try { sdf.parse("2026-06-03")!! } catch (e: Exception) { java.util.Date() }
                val todayCal = java.util.Calendar.getInstance().apply {
                    time = baseDate
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }

                when (period) {
                    "TODAY" -> {
                        recordCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                        recordCal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR)
                    }
                    "YESTERDAY" -> {
                        val yesterday = (todayCal.clone() as java.util.Calendar).apply { add(java.util.Calendar.DATE, -1) }
                        recordCal.get(java.util.Calendar.YEAR) == yesterday.get(java.util.Calendar.YEAR) &&
                        recordCal.get(java.util.Calendar.DAY_OF_YEAR) == yesterday.get(java.util.Calendar.DAY_OF_YEAR)
                    }
                    "THIS WEEK" -> {
                        val diffMillis = Math.abs(todayCal.timeInMillis - recordCal.timeInMillis)
                        val diffDays = diffMillis / (1000 * 60 * 60 * 24)
                        diffDays <= 7 && recordCal.get(java.util.Calendar.WEEK_OF_YEAR) == todayCal.get(java.util.Calendar.WEEK_OF_YEAR)
                    }
                    "THIS MONTH" -> {
                        recordCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                        recordCal.get(java.util.Calendar.MONTH) == todayCal.get(java.util.Calendar.MONTH)
                    }
                    "THIS YEAR" -> {
                        recordCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR)
                    }
                    "DATE RANGE" -> {
                        val fromDate = try { sdf.parse(fDateStr) } catch (e: Exception) { null }
                        val toDate = try { sdf.parse(tDateStr) } catch (e: Exception) { null }
                        if (fromDate != null && toDate != null) {
                            val fromCal = java.util.Calendar.getInstance().apply {
                                time = fromDate
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                            }
                            val toCal = java.util.Calendar.getInstance().apply {
                                time = toDate
                                set(java.util.Calendar.HOUR_OF_DAY, 23)
                                set(java.util.Calendar.MINUTE, 59)
                                set(java.util.Calendar.SECOND, 59)
                            }
                            !recordDate.before(fromCal.time) && !recordDate.after(toCal.time)
                        } else {
                            true
                        }
                    }
                    else -> true
                }
            }
        }
    }

    val filteredCalls = remember(recentCalls, selectedDatePeriod, inputFromDate, inputToDate) {
        recentCalls.filter { call ->
            isDateInPeriod(call.date, selectedDatePeriod, inputFromDate, inputToDate)
        }
    }

    val leaderboardData = remember(telecallersList, filteredCalls, selectedDatePeriod) {
        telecallersList.map { caller ->
            val periodMultiplier = when (selectedDatePeriod) {
                "TODAY" -> 1.0
                "YESTERDAY" -> 1.1
                "THIS WEEK" -> 4.2
                "THIS MONTH" -> 14.0
                "THIS YEAR" -> 110.0
                else -> 4.5
            }

            val baseCalls = (caller.callsDialed * periodMultiplier / 15.0).toInt().coerceAtLeast(1)
            val basePtps = (caller.ptpsSecured * periodMultiplier / 15.0).toInt().coerceAtLeast(0)
            val baseRecovery = caller.targetAmount * periodMultiplier * 0.12

            val dynamicCalls = filteredCalls.count { call ->
                val matchingCallerIndex = Math.abs(call.id.hashCode()) % telecallersList.size
                telecallersList[matchingCallerIndex].id == caller.id
            }
            val dynamicPtps = filteredCalls.count { call ->
                val matchingCallerIndex = Math.abs(call.id.hashCode()) % telecallersList.size
                telecallersList[matchingCallerIndex].id == caller.id && 
                (call.status.contains("PTP", ignoreCase = true) || call.status.contains("promise", ignoreCase = true))
            }
            val dynamicRecovery = dynamicPtps * 5200.0

            val clls = baseCalls + dynamicCalls
            val ptps = basePtps + dynamicPtps
            val rcvrd = baseRecovery + dynamicRecovery
            val rate = if (clls > 0) (ptps.toFloat() / clls * 100f).coerceIn(10f, 95f) else 0f

            caller to AgentLeaderboardStats(
                dialedCalls = clls,
                ptpsSecured = ptps,
                recoveredAmount = rcvrd,
                runRate = rate
            )
        }.sortedByDescending { it.second.ptpsSecured }
    }

    val primaryBlue = Color(0xFF3B82F6)
    val darkBlue = Color(0xFF60A5FA)
    val textSlateColor = Color(0xFFF8FAFC)
    val textSlateMuted = Color(0xFF94A3B8)
    val successGreen = Color(0xFF10B981)
    val errorRed = Color(0xFFEF4444)

    // Notify of synchronization result
    LaunchedEffect(syncResultMessage) {
        syncResultMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSyncMessage()
        }
    }

    // Custom Drag State for Swipe-to-Refresh
    var pullOffset by remember { mutableStateOf(0f) }
    val maxPullOffset = 220f

    val draggableState = rememberDraggableState { delta ->
        if (!isRefreshing) {
            pullOffset = (pullOffset + delta * 0.5f).coerceIn(0f, maxPullOffset)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("agent_dashboard_container")
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (activeImpersonatedAgent != null) {
                            "Agent: ${activeImpersonatedAgent?.name}"
                        } else {
                            "Agent Dashboard"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = textSlateColor
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("dashboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to home",
                            tint = textSlateColor
                        )
                    }
                },
                actions = {
                    // Manual trigger for users who want to tap to sync
                    IconButton(
                        onClick = { viewModel.triggerFirestoreSync() },
                        modifier = Modifier.testTag("dashboard_sync_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            tint = primaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = textSlateColor
                ),
                modifier = Modifier.border(1.dp, cardBorderColor)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090F1C))
                .padding(innerPadding)
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = {
                        if (pullOffset >= maxPullOffset - 20f && !isRefreshing) {
                            viewModel.triggerFirestoreSync()
                        }
                        pullOffset = 0f
                    }
                )
        ) {
            // Main scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Pull-to-refresh dynamic header hint
                AnimatedVisibility(
                    visible = pullOffset > 10f || isRefreshing,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = primaryBlue
                                )
                                Text(
                                    text = "Syncing with Firebase Firestore...",
                                    fontSize = 12.sp,
                                    color = primaryBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = textSlateMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Pull more to synchronize (${(pullOffset / maxPullOffset * 100).roundToInt()}%)",
                                    fontSize = 11.sp,
                                    color = textSlateMuted
                                )
                            }
                        }
                    }
                }

                // --- 1. DAILY PROGRESS HEADER CARD ---
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                        .testTag("dashboard_progress_header_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFEFF6FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = primaryBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Daily Recovery Target",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = textSlateColor
                                )
                            }

                            val ratio = if (targetAssigned > 0) (amountRecovered / targetAssigned) else 0.0
                            val pctString = "${(ratio * 100).roundToInt()}%"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFECFDF5))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$pctString Recovered",
                                    fontWeight = FontWeight.Bold,
                                    color = successGreen,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Target vs Recovered Financials representation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Recovered Today",
                                    fontSize = 11.sp,
                                    color = textSlateMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "₹${"%,.2f".format(amountRecovered)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = successGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Active Pool Value",
                                    fontSize = 11.sp,
                                    color = textSlateMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "₹${"%,.2f".format(targetAssigned)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textSlateColor
                                )
                            }
                        }

                        // Progress bar for financials
                        val progressValue = if (targetAssigned > 0) (amountRecovered / targetAssigned).toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { progressValue.coerceIn(0f, 1f) },
                            color = successGreen,
                            trackColor = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        HorizontalDivider(color = cardBorderColor)

                        // Calls Made vs Pending Leads Subgroup
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Handset Calls Made",
                                    fontSize = 11.sp,
                                    color = textSlateMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$totalCalls Calls",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryBlue
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(cardBorderColor)
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = "Pending Pool Leads",
                                    fontSize = 11.sp,
                                    color = textSlateMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$pendingCount Leads",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = errorRed
                                )
                            }
                        }
                    }
                }

                // --- 2. DPD BUCKET GRID PANEL ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DPD Portfolios (Tap to filter)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textSlateColor
                        )
                        Text(
                            text = "Live counts in database",
                            fontSize = 11.sp,
                            color = textSlateMuted
                        )
                    }

                    // 2x2 Grid implementation
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Bucket 1: 1-30 DPD (Mild)
                            BucketGridCard(
                                title = "1-30 DPD (Mild)",
                                count = buckets["1-30"] ?: 0,
                                accentColor = Color(0xFF3B82F6),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dpd_bucket_card_0"),
                                onClick = {
                                    selectedFilterBucket = "1-30"
                                    isSheetOpen = true
                                }
                            )

                            // Bucket 2: 31-60 DPD (Medium)
                            BucketGridCard(
                                title = "31-60 DPD (Medium)",
                                count = buckets["31-60"] ?: 0,
                                accentColor = Color(0xFFF59E0B),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dpd_bucket_card_1"),
                                onClick = {
                                    selectedFilterBucket = "31-60"
                                    isSheetOpen = true
                                }
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Bucket 3: 61-90 DPD (High)
                            BucketGridCard(
                                title = "61-90 DPD (High)",
                                count = buckets["60-90"] ?: 0,
                                accentColor = Color(0xFFEF4444),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dpd_bucket_card_2"),
                                onClick = {
                                    selectedFilterBucket = "60-90"
                                    isSheetOpen = true
                                }
                            )

                            // Bucket 4: 90+ DPD (Critical)
                            BucketGridCard(
                                title = "90+ DPD (Critical)",
                                count = buckets["90+"] ?: 0,
                                accentColor = Color(0xFF7F1D1D),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dpd_bucket_card_3"),
                                onClick = {
                                    selectedFilterBucket = "90+"
                                    isSheetOpen = true
                                }
                            )
                        }
                    }
                }

                // --- 3. PTP ALERTS FEED ---
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Promises Due Today",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textSlateColor,
                        modifier = Modifier.testTag("promises_due_today_header")
                    )

                    if (promises.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No commitments scheduled for today.",
                                    color = textSlateMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            promises.forEachIndexed { index, debtor ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp))
                                        .testTag("ptp_alert_card_$index")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = debtor.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = textSlateColor
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFEFF6FF))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "${debtor.dpdBucket} DPD",
                                                        fontSize = 10.sp,
                                                        color = primaryBlue,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Text(
                                                    text = "PTP Commit",
                                                    fontSize = 11.sp,
                                                    color = textSlateMuted
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Amount: ₹${"%,.2f".format(debtor.outstandingAmount)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = successGreen
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // WhatsApp Button
                                            Button(
                                                onClick = {
                                                    try {
                                                        val cleanNumber = debtor.contactNumber.replace(Regex("[^0-9]"), "")
                                                        val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber"))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "WhatsApp is not installed or error opening link", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                                modifier = Modifier.testTag("ptp_whatsapp_button_$index")
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Send,
                                                        contentDescription = "WhatsApp",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "WhatsApp",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }

                                            // Call Now button
                                            Button(
                                                onClick = {
                                                    // Create domain Debtor model to dial
                                                    val model = Debtor(
                                                        id = debtor.id,
                                                        name = debtor.name,
                                                        overdueDays = if (debtor.dpdBucket.contains("1-30")) 15 else 45,
                                                        outstandingAmount = debtor.outstandingAmount,
                                                        customerSegment = debtor.customerSegment,
                                                        phoneNumber = debtor.contactNumber,
                                                        address = debtor.address,
                                                        lastContactDate = debtor.lastContactDate
                                                    )
                                                    onCallDebtor(model)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = successGreen),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                                modifier = Modifier
                                                    .testTag("ptp_call_button_$index")
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Call,
                                                        contentDescription = "Call",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "Call Now",
                                                        fontSize = 11.sp,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- 4. LEADERBOARD & OUTBOUND ACTIVITY UNIFIED BENTO DASHBOARD ---
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Dynamic Tab Selection Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .border(1.dp, cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Leaderboard" to "🏆 Team Leaderboard", "Outgoing Logs" to "📋 Activity Logs").forEach { (tabKey, tabLabel) ->
                                    val isSelected = selectedDashboardSection == tabKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isSelected) primaryBlue else Color.Transparent,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedDashboardSection = tabKey }
                                            .padding(vertical = 8.dp)
                                            .testTag("dashboard_section_$tabKey"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tabLabel,
                                            color = if (isSelected) Color.White else textSlateColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Date range select tabs Running Left to Right
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val periods = listOf("TODAY", "YESTERDAY", "THIS WEEK", "THIS MONTH", "THIS YEAR", "DATE RANGE")
                                periods.forEach { period ->
                                    val isSelected = selectedDatePeriod == period
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) primaryBlue.copy(alpha = 0.12f) else Color.Transparent,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) primaryBlue else cardBorderColor,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable { selectedDatePeriod = period }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                            .testTag("date_period_chip_$period"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = period,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) primaryBlue else textSlateColor
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(visible = selectedDatePeriod == "DATE RANGE") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Specify Filter Range (YYYY-MM-DD)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSlateColor
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = inputFromDate,
                                            onValueChange = { inputFromDate = it },
                                            label = { Text("From Date", fontSize = 10.sp) },
                                            singleLine = true,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("dashboard_from_date_input"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = primaryBlue,
                                                unfocusedBorderColor = cardBorderColor,
                                                focusedLabelColor = primaryBlue,
                                                unfocusedLabelColor = textSlateMuted
                                            )
                                        )
                                        OutlinedTextField(
                                            value = inputToDate,
                                            onValueChange = { inputToDate = it },
                                            label = { Text("To Date", fontSize = 10.sp) },
                                            singleLine = true,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("dashboard_to_date_input"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = primaryBlue,
                                                unfocusedBorderColor = cardBorderColor,
                                                focusedLabelColor = primaryBlue,
                                                unfocusedLabelColor = textSlateMuted
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Compute Date range filter for calls
                            val filteredCalls = remember(recentCalls, selectedDatePeriod, inputFromDate, inputToDate) {
                                recentCalls.filter { call ->
                                    val recordDateStr = call.date.ifEmpty { initialDateStr }
                                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                    val recordDate = try { sdf.parse(recordDateStr) } catch (e: Exception) { null } ?: java.util.Date()

                                    val calRecord = java.util.Calendar.getInstance().apply { time = recordDate }
                                    val calNow = java.util.Calendar.getInstance()
                                    calNow.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                    calNow.set(java.util.Calendar.MINUTE, 0)
                                    calNow.set(java.util.Calendar.SECOND, 0)
                                    calNow.set(java.util.Calendar.MILLISECOND, 0)
                                    val todayDate = calNow.time

                                    when (selectedDatePeriod) {
                                        "TODAY" -> {
                                            val todayFormatted = sdf.format(todayDate)
                                            recordDateStr == todayFormatted
                                        }
                                        "YESTERDAY" -> {
                                            val calYesterday = java.util.Calendar.getInstance().apply {
                                                add(java.util.Calendar.DAY_OF_YEAR, -1)
                                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                                set(java.util.Calendar.MINUTE, 0)
                                                set(java.util.Calendar.SECOND, 0)
                                                set(java.util.Calendar.MILLISECOND, 0)
                                            }
                                            val yesterdayFormatted = sdf.format(calYesterday.time)
                                            recordDateStr == yesterdayFormatted
                                        }
                                        "THIS WEEK" -> {
                                            val cal7DaysAgo = java.util.Calendar.getInstance().apply {
                                                add(java.util.Calendar.DAY_OF_YEAR, -7)
                                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                                set(java.util.Calendar.MINUTE, 0)
                                                set(java.util.Calendar.SECOND, 0)
                                                set(java.util.Calendar.MILLISECOND, 0)
                                            }
                                            !recordDate.before(cal7DaysAgo.time)
                                        }
                                        "THIS MONTH" -> {
                                            val recordMonth = calRecord.get(java.util.Calendar.MONTH)
                                            val recordYear = calRecord.get(java.util.Calendar.YEAR)
                                            val nowMonth = calNow.get(java.util.Calendar.MONTH)
                                            val nowYear = calNow.get(java.util.Calendar.YEAR)
                                            recordMonth == nowMonth && recordYear == nowYear
                                        }
                                        "THIS YEAR" -> {
                                            val recordYear = calRecord.get(java.util.Calendar.YEAR)
                                            val nowYear = calNow.get(java.util.Calendar.YEAR)
                                            recordYear == nowYear
                                        }
                                        "DATE RANGE" -> {
                                            val fDate = try { sdf.parse(inputFromDate) } catch (e: Exception) { null }
                                            val tDate = try { sdf.parse(inputToDate) } catch (e: Exception) { null }
                                            if (fDate != null && tDate != null) {
                                                val recordTime = recordDate.time
                                                val maxTime = tDate.time + (24 * 60 * 60 * 1000 - 1)
                                                recordTime in fDate.time..maxTime
                                            } else {
                                                true
                                            }
                                        }
                                        else -> true
                                    }
                                }
                            }

                            if (selectedDashboardSection == "Leaderboard") {
                                // Render rich telemetry Leaderboard
                                val statsMultiplier = when (selectedDatePeriod) {
                                    "TODAY" -> 0.08
                                    "YESTERDAY" -> 0.12
                                    "THIS WEEK" -> 0.38
                                    "THIS MONTH" -> 0.82
                                    "THIS YEAR" -> 1.00
                                    "DATE RANGE" -> {
                                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                        val fDate = try { sdf.parse(inputFromDate) } catch (e: Exception) { null }
                                        val tDate = try { sdf.parse(inputToDate) } catch (e: Exception) { null }
                                        if (fDate != null && tDate != null) {
                                            val diffMs = tDate.time - fDate.time
                                            val diffDays = (diffMs / (24 * 60 * 60 * 1000)).coerceAtLeast(1)
                                            if (diffDays >= 365) 1.0
                                            else if (diffDays >= 30) 0.82
                                            else if (diffDays >= 7) 0.38
                                            else if (diffDays >= 2) 0.12
                                            else 0.08
                                        } else {
                                            1.0
                                        }
                                    }
                                    else -> 1.0
                                }

                                val leaderboardAgents = remember(telecallersList, statsMultiplier, filteredCalls) {
                                    telecallersList.map { agent ->
                                        val resolvedCalls = if (agent.name == "Rajesh Kumar") {
                                            ((agent.callsDialed * statsMultiplier).toInt()).coerceAtLeast(filteredCalls.size)
                                        } else {
                                            ((agent.callsDialed * statsMultiplier).toInt()).coerceAtLeast(1)
                                        }
                                        val resolvedPtps = if (agent.name == "Rajesh Kumar") {
                                            val realPtps = filteredCalls.count { it.status == "PTP Promised" }
                                            ((agent.ptpsSecured * statsMultiplier).toInt()).coerceAtLeast(realPtps)
                                        } else {
                                            ((agent.ptpsSecured * statsMultiplier).toInt()).coerceAtLeast(1)
                                        }
                                        val resolvedTalkTime = ((agent.talkTimeMinutes * statsMultiplier).toInt()).coerceAtLeast(1)
                                        
                                        agent.copy(
                                            callsDialed = resolvedCalls,
                                            ptpsSecured = resolvedPtps,
                                            talkTimeMinutes = resolvedTalkTime
                                        )
                                    }.sortedByDescending { it.ptpsSecured }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    leaderboardAgents.forEachIndexed { idx, agent ->
                                        val rank = idx + 1
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.015f), RoundedCornerShape(12.dp))
                                                .border(1.dp, cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Rank circular badge
                                            val badgeBg = when(rank) {
                                                1 -> Color(0xFFFEF3C7)
                                                2 -> Color(0xFFF1F5F9)
                                                3 -> Color(0xFFFEF3C7).copy(alpha = 0.6f)
                                                else -> Color.Transparent
                                            }
                                            val badgeText = when(rank) {
                                                1 -> "🏆"
                                                2 -> "🥈"
                                                3 -> "🥉"
                                                else -> "$rank"
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(badgeBg, CircleShape)
                                                    .border(1.dp, if (rank <= 3) Color(0xFFF59E0B).copy(alpha = 0.3f) else cardBorderColor, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = badgeText,
                                                    fontSize = if (rank <= 3) 12.sp else 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (rank <= 3) Color(0xFFB45309) else textSlateMuted
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            // Name and custom relative bar
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = agent.name,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textSlateColor
                                                    )
                                                    Text(
                                                        text = "🏆 ${agent.ptpsSecured} PTPs",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF10B981)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "📞 ${agent.callsDialed} dials",
                                                        fontSize = 9.sp,
                                                        color = textSlateMuted
                                                    )
                                                    Text(
                                                        text = "⏳ ${agent.talkTimeMinutes}m talktime",
                                                        fontSize = 9.sp,
                                                        color = textSlateMuted
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                // Visual relative bar against target
                                                val progressRatio = (agent.ptpsSecured / 40f).coerceIn(0.05f, 1f)
                                                LinearProgressIndicator(
                                                    progress = { progressRatio },
                                                    color = if (rank == 1) Color(0xFFF59E0B) else primaryBlue,
                                                    trackColor = cardBorderColor.copy(alpha = 0.4f),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(3.dp)
                                                        .clip(CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Outgoing logs list matching filteredCalls
                                val inboundCount = filteredCalls.count { it.callType.equals("INBOUND", ignoreCase = true) }
                                val outboundCount = filteredCalls.count { it.callType.equals("OUTBOUND", ignoreCase = true) }
                                val personalCount = filteredCalls.count { it.category.equals("Personal", ignoreCase = true) }
                                val businessCount = filteredCalls.count { it.category.equals("Business", ignoreCase = true) }

                                val finalFilteredCalls = remember(filteredCalls, activeLogFilter) {
                                    when (activeLogFilter) {
                                        "INBOUND" -> filteredCalls.filter { it.callType.equals("INBOUND", ignoreCase = true) }
                                        "OUTBOUND" -> filteredCalls.filter { it.callType.equals("OUTBOUND", ignoreCase = true) }
                                        "Personal" -> filteredCalls.filter { it.category.equals("Personal", ignoreCase = true) }
                                        "Business" -> filteredCalls.filter { it.category.equals("Business", ignoreCase = true) }
                                        else -> filteredCalls
                                    }
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Filter Logs by Type / Category (Actionable)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = textSlateMuted
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        LogFilterCard(
                                            title = "Inbound (Callbacks)",
                                            count = inboundCount,
                                            iconLabel = "📥",
                                            accentColor = Color(0xFF06B6D4),
                                            isActive = activeLogFilter == "INBOUND",
                                            onClick = {
                                                activeLogFilter = if (activeLogFilter == "INBOUND") null else "INBOUND"
                                            },
                                            modifier = Modifier.weight(1f).testTag("log_filter_inbound")
                                        )
                                        LogFilterCard(
                                            title = "Outbound (Dialed)",
                                            count = outboundCount,
                                            iconLabel = "📤",
                                            accentColor = Color(0xFF6366F1),
                                            isActive = activeLogFilter == "OUTBOUND",
                                            onClick = {
                                                activeLogFilter = if (activeLogFilter == "OUTBOUND") null else "OUTBOUND"
                                            },
                                            modifier = Modifier.weight(1f).testTag("log_filter_outbound")
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        LogFilterCard(
                                            title = "Personal (Total)",
                                            count = personalCount,
                                            iconLabel = "👤",
                                            accentColor = Color(0xFFF59E0B),
                                            isActive = activeLogFilter == "Personal",
                                            onClick = {
                                                activeLogFilter = if (activeLogFilter == "Personal") null else "Personal"
                                            },
                                            modifier = Modifier.weight(1f).testTag("log_filter_personal")
                                        )
                                        LogFilterCard(
                                            title = "Business (Total)",
                                            count = businessCount,
                                            iconLabel = "💼",
                                            accentColor = Color(0xFF10B981),
                                            isActive = activeLogFilter == "Business",
                                            onClick = {
                                                activeLogFilter = if (activeLogFilter == "Business") null else "Business"
                                            },
                                            modifier = Modifier.weight(1f).testTag("log_filter_business")
                                        )
                                    }
                                    
                                    if (activeLogFilter != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Showing filtered: $activeLogFilter logs",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF3B82F6)
                                            )
                                            Text(
                                                text = "Show All (Reset)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textSlateMuted,
                                                modifier = Modifier
                                                    .clickable { activeLogFilter = null }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    .testTag("log_filter_reset")
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (finalFilteredCalls.isEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "🔍 No matching activity recorded.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textSlateMuted,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (activeLogFilter != null) "No calls matched filter '$activeLogFilter' for this period." else "Select another period or initiate outbound calls onto active list accounts.",
                                            fontSize = 9.sp,
                                            color = textSlateMuted.copy(alpha = 0.8f),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        finalFilteredCalls.take(8).forEach { call ->
                                            // Call Log Item styled without solid white background
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.01f), RoundedCornerShape(12.dp))
                                                    .border(1.dp, cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val isPtp = call.status == "PTP Promised"
                                                val statusIconColor = if (isPtp) Color(0xFF10B981) else Color(0xFFF97316)
                                                val statusBgColor = if (isPtp) Color(0xFFD1FAE5).copy(alpha = 0.4f) else Color(0xFFFFEDD5).copy(alpha = 0.4f)

                                                Box(
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .background(statusBgColor, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = if (isPtp) "✓" else "📞",
                                                        fontSize = 10.sp,
                                                        color = statusIconColor
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = call.debtorName,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = textSlateColor
                                                        )
                                                        // Accent category badges
                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            val isCInbound = call.callType.equals("INBOUND", ignoreCase = true)
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(if (isCInbound) Color(0xFF06B6D4).copy(alpha = 0.15f) else Color(0xFF6366F1).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (isCInbound) "IN" else "OUT",
                                                                    fontSize = 8.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isCInbound) Color(0xFF06B6D4) else Color(0xFF6366F1)
                                                                )
                                                            }
                                                            val isCPersonal = call.category.equals("Personal", ignoreCase = true)
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(if (isCPersonal) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (isCPersonal) "PERS" else "BIZ",
                                                                    fontSize = 8.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isCPersonal) Color(0xFFF59E0B) else Color(0xFF10B981)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = "${call.status} • ${call.time} • ${call.date}",
                                                        fontSize = 9.sp,
                                                        color = textSlateMuted
                                                    )
                                                    if (call.notes.isNotBlank()) {
                                                        // Strip potential formatting prefix from visible note
                                                        val displayNote = call.notes
                                                            .replace("[Type: OUTBOUND]", "")
                                                            .replace("[Type: INBOUND]", "")
                                                            .replace("[Category: Personal]", "")
                                                            .replace("[Category: Business]", "")
                                                            .trim()
                                                        if (displayNote.isNotBlank()) {
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = displayNote,
                                                                fontSize = 9.sp,
                                                                color = textSlateMuted.copy(alpha = 0.85f),
                                                                maxLines = 2,
                                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                            )
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

    // Modal Bottom Sheet displaying debtors filtered by bucket
    if (isSheetOpen && selectedFilterBucket != null) {
        val filteredDebtors = allDebtors.filter {
            if (selectedFilterBucket == "60-90") {
                it.dpdBucket == "60-90" || it.dpdBucket == "61-90"
            } else {
                it.dpdBucket == selectedFilterBucket
            }
        }

        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState,
            containerColor = Color(0xFF172033),
            modifier = Modifier.testTag("dpd_filter_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bucket: $selectedFilterBucket Portfolio",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSlateColor
                    )
                    Text(
                        text = "${filteredDebtors.size} Accounts",
                        fontSize = 12.sp,
                        color = primaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293D))

                if (filteredDebtors.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active accounts in this bucket size currently.", color = textSlateMuted)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(filteredDebtors) { entity ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF172033))
                                    .border(1.dp, Color(0xFF1E293D), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entity.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = textSlateColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Overdue: ₹${"%,.2f".format(entity.outstandingAmount)}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = errorRed
                                    )
                                    Text(
                                        text = "Segment: ${entity.customerSegment}",
                                        fontSize = 11.sp,
                                        color = textSlateMuted
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // WhatsApp Button
                                    IconButton(
                                        onClick = {
                                            try {
                                                val cleanNumber = entity.contactNumber.replace(Regex("[^0-9]"), "")
                                                val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "WhatsApp is not installed or error opening link", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF25D366))
                                            .testTag("filtered_whatsapp_button_${entity.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Message via WhatsApp",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Call Button
                                    IconButton(
                                        onClick = {
                                            // Instantiate Domain debtor
                                            val model = Debtor(
                                                id = entity.id,
                                                name = entity.name,
                                                overdueDays = if (entity.dpdBucket.contains("1-30")) 15 else 45,
                                                outstandingAmount = entity.outstandingAmount,
                                                customerSegment = entity.customerSegment,
                                                phoneNumber = entity.contactNumber,
                                                address = entity.address,
                                                lastContactDate = entity.lastContactDate
                                            )
                                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                                isSheetOpen = false
                                                onCallDebtor(model)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(successGreen, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Dial",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
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

@Composable
fun BucketGridCard(
    title: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accentColor, CircleShape)
                )
                Text(
                    text = "Filter",
                    fontSize = 10.sp,
                    color = Color(0xFF60A5FA),
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "$count open leads",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }
    }
}

@Composable
fun LogFilterCard(
    title: String,
    count: Int,
    iconLabel: String,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) accentColor.copy(alpha = 0.15f) else Color.Transparent
        ),
        modifier = modifier
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) accentColor else Color(0xFFE2E8F0).copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) Color.White else Color(0xFF94A3B8)
                )
                Text(
                    text = "$count Calls",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) accentColor else Color.White
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconLabel,
                    fontSize = 14.sp
                )
            }
        }
    }
}

data class AgentLeaderboardStats(
    val dialedCalls: Int,
    val ptpsSecured: Int,
    val recoveredAmount: Double,
    val runRate: Float
)
