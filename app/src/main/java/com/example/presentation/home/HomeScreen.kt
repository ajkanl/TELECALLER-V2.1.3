package com.example.presentation.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.net.Uri
import android.content.Intent
import androidx.compose.material.icons.filled.Send
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import com.example.domain.model.CallRecord
import com.example.domain.model.Debtor

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
    onOpenSecuritySettings: () -> Unit,
    onOpenCampaignControl: () -> Unit,
    onOpenTelephonySettings: () -> Unit,
    onOpenSyncSettings: () -> Unit,
    onOpenAgentDashboard: () -> Unit,
    onOpenSmartQueue: () -> Unit,
    onOpenAdminAnalytics: () -> Unit,
    onOpenTelecallerTargetScreen: () -> Unit,
    onOpenUploadDatabaseScreen: () -> Unit
) {
    val context = LocalContext.current
    val debtors by viewModel.debtors.collectAsState()
    val recentCalls by viewModel.recentCalls.collectAsState()
    val dailyProgress by viewModel.dailyCallProgress.collectAsState()
    val recoveredAmount by viewModel.dailyRecoveredAmount.collectAsState()
    val currentSim by viewModel.currentSim.collectAsState()
    val activeCallDebtor by viewModel.activeCallDebtor.collectAsState()
    val isDialing by viewModel.isDialing.collectAsState()
    val isNumberMaskingEnabled by viewModel.isNumberMaskingEnabled.collectAsState()
    val todayReminders by viewModel.todayReminders.collectAsState()

    var activeTab by remember { mutableStateOf("Home") }
    var isEditingProfile by remember { mutableStateOf(false) }
    var queueSearchQuery by remember { mutableStateOf("") }

    // Dashboard dynamic tabs & date filters states (Leaderboard & Outgoing Logs)
    var selectedDashboardSection by remember { mutableStateOf("Leaderboard") } // "Leaderboard" or "Outgoing Logs"
    var selectedDatePeriod by remember { mutableStateOf("TODAY") } // "TODAY", "YESTERDAY", "THIS WEEK", "THIS MONTH", "THIS YEAR", "DATE RANGE"
    
    val sdfFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }
    val initialDateStr = remember { sdfFormat.format(java.util.Date()) }
    var inputFromDate by remember { mutableStateOf(initialDateStr) }
    var inputToDate by remember { mutableStateOf(initialDateStr) }

    val telecallersList by viewModel.telecallersList.collectAsState()

    // Colors matching Premium Dark Bento Theme
    val backgroundColor = Color(0xFF090F1C)
    val cardBorderColor = Color(0xFF1E293D)
    val primaryBlue = Color(0xFF3B82F6)
    val textSlateColor = Color(0xFFF8FAFC)
    val textSlateMuted = Color(0xFF94A3B8)
    val cardBackgroundColor = Color(0xFF172033)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- 1. HEADER SECTION ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackgroundColor)
                .border(1.dp, cardBorderColor)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Text(
                        text = "$currentSim • Online",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSlateMuted,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RecoveryPro ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSlateColor
                    )
                    Text(
                        text = "v1.4",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryBlue
                    )
                }
            }

            // SIM Switch Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFF6FF))
                    .clickable {
                        viewModel.switchSim()
                        Toast
                            .makeText(context, "Switched call line to ${viewModel.currentSim.value}", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Switch SIM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryBlue
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFDBEAFE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RK",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D4ED8),
                    fontSize = 13.sp
                )
            }
        }

        // --- MAIN BODY SECTION ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeTab == "Home") {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- TODAY'S ACTION ALERTS & REMINDERS ---
                    if (todayReminders.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)), // Crimson alert background
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(20.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Alerts Icon",
                                            tint = Color(0xFFFCA5A5),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "TODAY'S ACTION REMINDERS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFCA5A5),
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    todayReminders.forEach { notificationText ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "•",
                                                color = Color(0xFFFCA5A5),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = notificationText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFFFECACA)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 2. PRIORITY FOLLOW-UP BENTO BOX ---
                    item {
                        PriorityBentoBox(
                            debtors = debtors,
                            activeCallDebtor = activeCallDebtor,
                            isDialing = isDialing,
                            onCallInitiated = { viewModel.initiateCall(it) },
                            onCallAction = { status -> viewModel.endCallWithResult(status) },
                            onCancel = { viewModel.cancelCall() },
                            onMaskNumber = { viewModel.maskPhoneNumber(it) }
                        )
                    }

                    // --- 3. STATS IN ROW (BENTO GRID - 2 COLUMNS) ---
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // "Calls Today" Bento Box (Column Left)
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
                                    .padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "CALLS TODAY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSlateMuted
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "${dailyProgress.first}",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textSlateColor
                                        )
                                        Text(
                                            text = " / ${dailyProgress.second}",
                                            fontSize = 14.sp,
                                            color = textSlateMuted,
                                            modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = { dailyProgress.first.toFloat() / dailyProgress.second },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(CircleShape),
                                        color = primaryBlue,
                                        trackColor = Color.White.copy(alpha = 0.15f),
                                    )
                                }
                            }

                            // "Recovered" Bento Box (Column Right)
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.4f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFF047857).copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                                    .padding(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "RECOVERED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "₹${"%.2f".format(recoveredAmount / 100000.0)}L",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "↑ 12% vs yest.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399)
                                    )
                                }
                            }
                        }
                    }

                    // --- 4. LEADERBOARD & OUTBOUND ACTIVITY UNIFIED BENTO DASHBOARD ---
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Dynamic Tab Selection Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
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

                                // Interactive Custom Range Selection Date Pickers
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
                                                    .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
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
                                                    1 -> "🥇"
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
                                    if (filteredCalls.isEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "🔍 No outbound activity recorded.",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = textSlateMuted,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Select another period or initiate outbound calls onto active list accounts.",
                                                fontSize = 9.sp,
                                                color = textSlateMuted.copy(alpha = 0.8f),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            filteredCalls.take(8).forEach { call ->
                                                // Call Log Item styled without solid white background
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
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
                                                        Text(
                                                            text = call.debtorName,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = textSlateColor
                                                        )
                                                        Text(
                                                            text = "${call.status} • ${call.time} • ${call.date}",
                                                            fontSize = 9.sp,
                                                            color = textSlateMuted
                                                        )
                                                        if (call.notes.isNotEmpty()) {
                                                            Text(
                                                                text = "Notes: ${call.notes}",
                                                                fontSize = 9.sp,
                                                                color = textSlateMuted.copy(alpha = 0.8f),
                                                                modifier = Modifier.padding(top = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = call.simCard,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textSlateMuted,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 5. WORKMANAGER STATUS CARD (DARK BENTO BOX) ---
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                              ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Gear",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "WorkManager Status",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Auto-syncing background tasks",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(primaryBlue)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // --- 6. AGENT STATUS CARD (BENTO SECTION) ---
                    item {
                        val nonDisabledAgents = remember(telecallersList) {
                            telecallersList.filter { !it.isDisabled }
                        }
                        
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
                                .testTag("agent_status_bento_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(primaryBlue.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = "Agents",
                                                tint = primaryBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Agent Operational Status",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textSlateColor
                                            )
                                            Text(
                                                text = "${nonDisabledAgents.filter { it.isOnline }.size} active / ${nonDisabledAgents.filter { !it.isOnline }.size} inactive",
                                                fontSize = 10.sp,
                                                color = textSlateMuted
                                            )
                                        }
                                    }
                                }

                                if (nonDisabledAgents.isEmpty()) {
                                    Text(
                                        text = "No agents configured in policy settings.",
                                        fontSize = 11.sp,
                                        color = textSlateMuted,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        nonDisabledAgents.forEach { agent ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                    .border(1.dp, cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Status indicator circle
                                                    Box(
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .background(
                                                                if (agent.isOnline) Color(0xFF10B981) else Color(0xFF64748B),
                                                                CircleShape
                                                            )
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = agent.name,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textSlateColor
                                                    )
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(
                                                            if (agent.isOnline) Color(0xFF047857).copy(alpha = 0.2f) else Color(0xFF334155).copy(alpha = 0.2f)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (agent.isOnline) "Active" else "Inactive",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (agent.isOnline) Color(0xFF34D399) else Color(0xFF94A3B8)
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
            } else if (activeTab == "Queue") {
                val filteredQueueDebtors = remember(debtors, queueSearchQuery) {
                    if (queueSearchQuery.isBlank()) {
                        debtors
                    } else {
                        debtors.filter { debtor ->
                            debtor.name.contains(queueSearchQuery, ignoreCase = true) ||
                            debtor.phoneNumber.contains(queueSearchQuery) ||
                            debtor.customerSegment.contains(queueSearchQuery, ignoreCase = true)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, cardBorderColor, RoundedCornerShape(20.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Debtor Collection Queue",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textSlateColor
                                )
                                Text(
                                    text = "Tap on any debtor account below to edit profile parameters and register historical call notes.",
                                    fontSize = 11.sp,
                                    color = textSlateMuted
                                )
                                OutlinedTextField(
                                    value = queueSearchQuery,
                                    onValueChange = { queueSearchQuery = it },
                                    placeholder = { Text("Search debtors by name, phone...", fontSize = 13.sp, color = textSlateMuted) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = textSlateMuted) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("queue_search_field"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = backgroundColor,
                                        unfocusedContainerColor = backgroundColor,
                                        focusedBorderColor = primaryBlue,
                                        unfocusedBorderColor = cardBorderColor,
                                        focusedTextColor = textSlateColor,
                                        unfocusedTextColor = textSlateColor
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    if (filteredQueueDebtors.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (debtors.isEmpty()) {
                                    CircularProgressIndicator(color = primaryBlue)
                                } else {
                                    Text("No matching debtors found.", color = textSlateMuted, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(filteredQueueDebtors) { debtor ->
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, cardBorderColor, RoundedCornerShape(20.dp))
                                    .clickable { viewModel.selectDebtor(debtor) }
                                    .testTag("debtor_item_${debtor.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = debtor.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textSlateColor
                                        )
                                        Text(
                                            text = "${viewModel.maskPhoneNumber(debtor.phoneNumber)} • ${debtor.customerSegment}",
                                            fontSize = 11.sp,
                                            color = textSlateMuted
                                        )
                                        Text(
                                            text = "Last Contact: ${debtor.lastContactDate}",
                                            fontSize = 10.sp,
                                            color = primaryBlue,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${"%,.0f".format(debtor.outstandingAmount)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textSlateColor
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.selectDebtor(debtor) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "Edit Profile",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primaryBlue
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val cleanNumber = debtor.phoneNumber.replace(Regex("[^0-9]"), "")
                                                        val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber"))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "WhatsApp is not installed or error opening link", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF25D366))
                                                    .testTag("whatsapp_button_student_${debtor.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Send,
                                                    contentDescription = "Message via WhatsApp",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.initiateCall(debtor)
                                                    viewModel.selectDebtor(debtor)
                                                },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981))
                                                    .testTag("dialer_button_student_${debtor.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Call,
                                                    contentDescription = "Call Student",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == "Stats") {
                // We're inside activeTab == "Stats", which we labeled "Dashboard"
                
                // Helper to filter call records by period
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

                        caller to LeaderboardStats(
                            dialedCalls = clls,
                            ptpsSecured = ptps,
                            recoveredAmount = rcvrd,
                            runRate = rate
                        )
                    }.sortedByDescending { it.second.ptpsSecured }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title Header
                    item {
                        Column {
                            Text(
                                text = "Performance Dashboard",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSlateColor
                            )
                            Text(
                                text = "Real-time leaderboard & outgoing activity statistics",
                                fontSize = 11.sp,
                                color = textSlateMuted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // 1. SECTION SELECTION Segmented Switcher (Tabs)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE2E8F0).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            listOf("Leaderboard", "Outgoing Logs").forEach { section ->
                                val isSelected = selectedDashboardSection == section
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) Color.White else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedDashboardSection = section }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = section,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) primaryBlue else textSlateMuted
                                    )
                                }
                            }
                        }
                    }

                    // 2. PERIOD TABS From Left To Right:
                    // TODAY, YESTERDAY, THIS WEEK, THIS MONTH, THIS YEAR, DATE RANGE
                    item {
                        val periodsList = listOf(
                            "TODAY" to "Today",
                            "YESTERDAY" to "Yesterday",
                            "THIS WEEK" to "This Week",
                            "THIS MONTH" to "This Month",
                            "THIS YEAR" to "This Year",
                            "DATE RANGE" to "Date Range"
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(periodsList) { pair ->
                                val key = pair.first
                                val label = pair.second
                                val isSelected = selectedDatePeriod == key
                                Surface(
                                    onClick = { selectedDatePeriod = key },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) primaryBlue else Color.White,
                                    border = BorderStroke(1.dp, if (isSelected) primaryBlue else cardBorderColor),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else textSlateMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. SELECT DATE RANGE INPUTS FROM DATE TO DATE (If DATE RANGE selected)
                    if (selectedDatePeriod == "DATE RANGE") {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Filter by Custom Date Range",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSlateColor
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "From Date (yyyy-MM-dd)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textSlateMuted,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                            OutlinedTextField(
                                                value = inputFromDate,
                                                onValueChange = { inputFromDate = it },
                                                singleLine = true,
                                                textStyle = TextStyle(fontSize = 12.sp, color = textSlateColor),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = primaryBlue,
                                                    unfocusedBorderColor = cardBorderColor,
                                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                                    focusedContainerColor = Color.White
                                                ),
                                                modifier = Modifier.fillMaxWidth().testTag("dashboard_from_date")
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "To Date (yyyy-MM-dd)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textSlateMuted,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                            OutlinedTextField(
                                                value = inputToDate,
                                                onValueChange = { inputToDate = it },
                                                singleLine = true,
                                                textStyle = TextStyle(fontSize = 12.sp, color = textSlateColor),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = primaryBlue,
                                                    unfocusedBorderColor = cardBorderColor,
                                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                                    focusedContainerColor = Color.White
                                                ),
                                                modifier = Modifier.fillMaxWidth().testTag("dashboard_to_date")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. MAIN CONTENT CONTAINER (Leaderboard or Outgoing Logs)
                    if (selectedDashboardSection == "Leaderboard") {
                        // LEADERBOARD LIST
                        if (leaderboardData.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                        .padding(24.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Empty",
                                            tint = textSlateMuted,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No Telecaller Data Available", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                                        Text("Register telecallers or update compliance binding.", fontSize = 11.sp, color = textSlateMuted, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        } else {
                            itemsIndexed(leaderboardData) { index, pair ->
                                val caller = pair.first
                                val stats = pair.second
                                val rank = index + 1
                                val rankColor = when (rank) {
                                    1 -> Color(0xFFF59E0B) // Gold
                                    2 -> Color(0xFF94A3B8) // Silver
                                    3 -> Color(0xFFB45309) // Bronze
                                    else -> Color(0xFF64748B) // Slate
                                }
                                
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                        .testTag("leaderboard_card_rank_$rank")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Rank circular badge
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(rankColor.copy(alpha = 0.15f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "#$rank",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = rankColor
                                                    )
                                                }
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text(caller.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .background(if (caller.isOnline) Color(0xFF10B981) else Color(0xFF94A3B8), CircleShape)
                                                        )
                                                    }
                                                    Text(
                                                        text = "Agent ID: ${caller.id} • ${if (caller.isOnline) "🟢 Active" else "⚫ Offline"}",
                                                        fontSize = 10.sp,
                                                        color = textSlateMuted
                                                    )
                                                }
                                            }
                                            
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "${stats.ptpsSecured} PTPs",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primaryBlue
                                                )
                                                Text(
                                                    text = "Resolved: ₹${String.format("%,.0f", stats.recoveredAmount)}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF10B981),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            color = Color(0xFFF1F5F9)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Dialed Calls", fontSize = 9.sp, color = textSlateMuted)
                                                Text("${stats.dialedCalls} attempts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Conversions", fontSize = 9.sp, color = textSlateMuted)
                                                Text("${String.format("%.1f", stats.runRate)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Assign Target", fontSize = 9.sp, color = textSlateMuted)
                                                Text("₹${String.format("%,.0f", caller.targetAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        // Resolution Progress Bar
                                        LinearProgressIndicator(
                                            progress = { stats.runRate / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = primaryBlue,
                                            trackColor = Color(0xFFF1F5F9)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // OUTGOING LATEST ACTIVITY LOGS
                        if (filteredCalls.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                        .padding(24.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Empty",
                                            tint = textSlateMuted,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No Activity Logs Found", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                                        Text("No outbound calls made or resolved during $selectedDatePeriod.", fontSize = 11.sp, color = textSlateMuted, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        } else {
                            items(filteredCalls.sortedByDescending { it.id }) { log ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                        .testTag("log_card_${log.id}")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(primaryBlue.copy(alpha = 0.1f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = "Contact Log",
                                                        tint = primaryBlue,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(log.debtorName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textSlateColor)
                                                    Text("Line: ${log.simCard} • Date: ${log.date}", fontSize = 11.sp, color = textSlateMuted)
                                                }
                                            }

                                            // Status pill overlay
                                            val statusPillColor = when {
                                                log.status.contains("PTP", ignoreCase = true) || log.status.contains("Promise", ignoreCase = true) -> Color(0xFF10B981) // active green
                                                log.status.contains("Completed", ignoreCase = true) || log.status.contains("Answered", ignoreCase = true) -> Color(0xFF3B82F6) // blue
                                                log.status.contains("Ringing", ignoreCase = true) || log.status.contains("No Answer", ignoreCase = true) -> Color(0xFFF59E0B) // amber
                                                else -> Color(0xFF64748B) // slate
                                            }
                                            Surface(
                                                color = statusPillColor.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.border(0.5.dp, statusPillColor, RoundedCornerShape(6.dp))
                                            ) {
                                                Text(
                                                    text = log.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = statusPillColor,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        if (log.notes.isNotEmpty()) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                color = Color(0xFFF1F5F9)
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Operator Notes", fontSize = 9.sp, color = textSlateMuted, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = log.notes,
                                                        fontSize = 11.sp,
                                                        color = textSlateColor,
                                                        style = TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = log.time,
                                                    fontSize = 11.sp,
                                                    color = textSlateMuted,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == "Account") {
                val activeAgent by viewModel.activeImpersonatedAgent.collectAsState()
                val telecallers by viewModel.telecallersList.collectAsState()
                val currentAgent = activeAgent ?: telecallers.firstOrNull { it.id == "T01" } ?: com.example.domain.security.TelecallerAgent("T01", "Rajesh Kumar", true, 142, 272, 28)

                if (isEditingProfile) {
                    EditProfileSheet(
                        agent = currentAgent,
                        onDismiss = { isEditingProfile = false },
                        onSave = { newName, newPicture ->
                            viewModel.updateAgentProfile(currentAgent.id, newName, newPicture)
                            isEditingProfile = false
                            Toast.makeText(context, "Agent Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                                .testTag("agent_profile_summary_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                            border = BorderStroke(1.dp, cardBorderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.padding(top = 10.dp)
                                ) {
                                    AgentAvatar(agent = currentAgent, size = 88.dp, onClick = { isEditingProfile = true })
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(primaryBlue)
                                            .align(Alignment.BottomEnd)
                                            .border(2.dp, backgroundColor, CircleShape)
                                            .clickable { isEditingProfile = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                                            contentDescription = "Edit Picture Button",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = currentAgent.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textSlateColor,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Recovery Agent Code: ${currentAgent.id}",
                                    fontSize = 12.sp,
                                    color = textSlateMuted,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(backgroundColor)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Calls", color = textSlateMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = "${currentAgent.callsDialed}", color = textSlateColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Talktime (m)", color = textSlateMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = "${currentAgent.talkTimeMinutes}", color = textSlateColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "PTPs Secured", color = textSlateMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = "${currentAgent.ptpsSecured}", color = textSlateColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { isEditingProfile = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .testTag("btn_edit_profile")
                        ) {
                            Text("Edit Contact Profile", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenSecuritySettings,
                            colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .testTag("open_security_settings_button")
                        ) {
                            Text("Security & Compliance", color = textSlateColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenCampaignControl,
                            colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .testTag("open_campaign_control_button")
                        ) {
                            Text("Campaign & Routing", color = textSlateColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenTelephonySettings,
                            colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .testTag("open_telephony_settings_button")
                        ) {
                            Text("Telephony Settings", color = textSlateColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenSyncSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .testTag("open_sync_settings_button")
                        ) {
                            Text("Data & Sync Settings", color = textSlateColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenAgentDashboard,
                            colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .testTag("open_agent_dashboard_button")
                        ) {
                            Text("Agent Dashboard", color = textSlateColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenSmartQueue,
                            colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .testTag("open_smart_queue_button")
                        ) {
                            Text("Smart Calling Queue", color = textSlateColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenAdminAnalytics,
                            colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .testTag("open_admin_analytics_button")
                        ) {
                            Text("Admin Analytics Dashboard", color = textSlateColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenTelecallerTargetScreen,
                            colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .testTag("open_telecaller_target_button")
                        ) {
                            Text("Set Telecaller Target", color = textSlateColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenUploadDatabaseScreen,
                            colors = ButtonDefaults.buttonColors(containerColor = cardBackgroundColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .testTag("open_upload_data_button")
                        ) {
                            Text("Upload Data into Database", color = textSlateColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp)
                                .testTag("logout_button")
                        ) {
                            Text("Sign Out Master Key", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- BOTTOM NAVIGATION BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackgroundColor)
                .border(1.dp, cardBorderColor)
                .padding(bottom = 14.dp, top = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isActive = activeTab == "Home",
                onClick = { activeTab = "Home" },
                primaryColor = primaryBlue,
                mutedColor = textSlateMuted
            )
            NavItem(
                icon = Icons.Default.Search,
                label = "Queue",
                isActive = activeTab == "Queue",
                onClick = { activeTab = "Queue" },
                primaryColor = primaryBlue,
                mutedColor = textSlateMuted
            )
            NavItem(
                icon = Icons.Default.Call,
                label = "Call",
                isActive = false,
                onClick = { onOpenSmartQueue() },
                primaryColor = primaryBlue,
                mutedColor = textSlateMuted
            )
            NavItem(
                icon = Icons.Default.List,
                label = "Dashboard",
                isActive = activeTab == "Stats",
                onClick = { activeTab = "Stats" },
                primaryColor = primaryBlue,
                mutedColor = textSlateMuted
            )
            NavItem(
                icon = Icons.Default.AccountCircle,
                label = "Account",
                isActive = activeTab == "Account",
                onClick = { activeTab = "Account" },
                primaryColor = primaryBlue,
                mutedColor = textSlateMuted
            )
        }
    }
}

@Composable
fun PriorityBentoBox(
    debtors: List<Debtor>,
    activeCallDebtor: Debtor?,
    isDialing: Boolean,
    onCallInitiated: (Debtor) -> Unit,
    onCallAction: (String) -> Unit,
    onCancel: () -> Unit,
    onMaskNumber: (String) -> String
) {
    val highlightColor = Color(0xFF2563EB)
    var selectedTab by remember { mutableStateOf("task") }

    val debtor = activeCallDebtor ?: debtors.firstOrNull() ?: Debtor("0", "Aditya Vardhan", 12, 84250.0, "High Value", "+919876543210", "42, Sector 5, Bangalore", "Never Contacted")

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDialing) Color(0xFF1E1E2F) else highlightColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("priority_follow_up_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Only show navigation tabs if NOT in an active dialing session!
            if (!isDialing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        "task" to "🎯 Active Call",
                        "metrics" to "📈 Portfolio",
                        "sync" to "🛡️ System Sync"
                    )
                    tabs.forEach { (tabId, label) ->
                        val isSelected = selectedTab == tabId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.White.copy(alpha = 0.35f) else Color.Transparent)
                                .clickable { selectedTab = tabId }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            if (isDialing) {
                // --- ACTIVE DIALING SEQUENCE OVERLAY ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = Color(0xFF3B82F6),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DIALING PHYSICAL SIM CARD...",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = debtor.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = onMaskNumber(debtor.phoneNumber),
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDC2626), RoundedCornerShape(10.dp))
                            .clickable { onCancel() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Track Call Outcome:",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onCallAction("Not Picked Up") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("No Picked", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onCallAction("PTP Promised") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(40.dp)
                    ) {
                        Text("PTP Promised", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // --- NOT DIALING -> RENDER THE CURRENT SELECTED TAB ---
                when (selectedTab) {
                    "task" -> {
                        // --- DEFAULT IDLE FOLLOW-UP VIEW ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "PRIORITY FOLLOW-UP CONTACT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDBEAFE),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = debtor.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "#${debtor.id} • Overdue ${debtor.overdueDays} Days",
                                    fontSize = 12.sp,
                                    color = Color(0xFFBFDBFE)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = debtor.customerSegment,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "OUTSTANDING AMOUNT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBFDBFE)
                                )
                                Text(
                                    text = "₹${"%,.2f".format(debtor.outstandingAmount)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                            Button(
                                onClick = { onCallInitiated(debtor) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("call_now_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Call Now",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                    "metrics" -> {
                        // --- LIVE PORTFOLIO DASHBOARD METRICS ---
                        val totalDebtors = debtors.size
                        val totalOutstanding = debtors.sumOf { it.outstandingAmount }
                        val totalHighValueOutstanding = debtors.filter { it.customerSegment.contains("High", ignoreCase = true) }.sumOf { it.outstandingAmount }
                        val averageDaysInArrears = if (debtors.isNotEmpty()) debtors.map { it.overdueDays }.average().toInt() else 0

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "LIVE ALLOCATED PORTFOLIO SUMMARY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDBEAFE),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "TOTAL PORTFOLIO DEBT",
                                        fontSize = 9.sp,
                                        color = Color(0xFFBFDBFE),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "₹${"%,.2f".format(totalOutstanding)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(
                                        text = "HIGH VALUE CONCENTRATION",
                                        fontSize = 9.sp,
                                        color = Color(0xFFBFDBFE),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "₹${"%,.2f".format(totalHighValueOutstanding)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📊", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Allocated Accounts: $totalDebtors Students",
                                        fontSize = 11.sp,
                                        color = Color(0xFFDBEAFE)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⏳", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Avg Delinquency: $averageDaysInArrears Days",
                                        fontSize = 11.sp,
                                        color = Color(0xFFDBEAFE)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Visual Target Progress Bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "TEAM RECOVERY TARGET",
                                        fontSize = 9.sp,
                                        color = Color(0xFFBFDBFE),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "78% Achieved",
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { 0.78f },
                                    color = Color(0xFF34D399),
                                    trackColor = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                    "sync" -> {
                        // --- HIGH FIDELITY SYSTEM INTEGRATION & SYNC STATUS ---
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "SYSTEM SYNC & COMPLIANCE GATEWAY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDBEAFE),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val syncStatusItems = listOf(
                                "📡 Network Cellular SIM" to "SIM 1 (Cellular Gateway Online)",
                                "🔥 Cloud Databases" to "Firewall Protected (Firestore / RTDB)",
                                "💼 Background Processor" to "Android WorkManager Sync (Running)",
                                "🔒 Encrypted Storage" to "SQLite Room Cache Active"
                            )

                            syncStatusItems.forEach { (label, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        color = Color(0xFFBFDBFE),
                                        fontWeight = FontWeight.Normal
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color(0xFF34D399), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = value,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "All inbound communication logs, recording sessions and payment agreements are replicated instantly under telecaller audit rules constraint.",
                                fontSize = 10.sp,
                                color = Color(0xFFDBEAFE).copy(alpha = 0.8f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentCallItem(call: CallRecord, textSlateColor: Color, textSlateMuted: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF172033), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1E293D), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isPtp = call.status == "PTP Promised"
        val statusIconColor = if (isPtp) Color(0xFF34D399) else Color(0xFFFB923C)
        val statusBgColor = if (isPtp) Color(0xFF022C22) else Color(0xFF451A03)

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(statusBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPtp) "✓" else "📞",
                fontSize = 11.sp,
                color = statusIconColor
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.debtorName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textSlateColor
            )
            Text(
                text = "${call.status} • ${call.time}",
                fontSize = 10.sp,
                color = textSlateMuted
            )
        }
        Text(
            text = call.simCard,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textSlateMuted,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    mutedColor: Color
) {
    val tintColor by animateColorAsState(targetValue = if (isActive) primaryColor else mutedColor, label = "tint")

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = tintColor
        )
    }
}

data class LeaderboardStats(
    val dialedCalls: Int,
    val ptpsSecured: Int,
    val recoveredAmount: Double,
    val runRate: Float
)

@Composable
fun AgentAvatar(
    agent: com.example.domain.security.TelecallerAgent,
    size: androidx.compose.ui.unit.Dp = 80.dp,
    onClick: (() -> Unit)? = null
) {
    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .border(2.dp, Color(0xFF1E293D), CircleShape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    val presetBrush = when (agent.profilePicture) {
        "preset:cyber_blue" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
        "preset:gold_shield" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706)))
        "preset:emerald_guardian" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
        "preset:sunset_crimson" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFBE123C)))
        "preset:cosmic_purple" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF5B21B6)))
        else -> null
    }

    if (presetBrush != null) {
        Box(
            modifier = modifier.background(presetBrush),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (agent.profilePicture) {
                "preset:cyber_blue" -> Icons.Default.AccountCircle
                "preset:gold_shield" -> Icons.Default.Settings
                "preset:emerald_guardian" -> Icons.Default.List
                "preset:sunset_crimson" -> Icons.Default.Call
                else -> Icons.Default.Home
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    } else if (agent.profilePicture != null && agent.profilePicture.isNotEmpty()) {
        Box(
            modifier = modifier.background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            val painter = coil.compose.rememberAsyncImagePainter(model = Uri.parse(agent.profilePicture))
            androidx.compose.foundation.Image(
                painter = painter,
                contentDescription = "Agent profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
    } else {
        val initials = if (agent.name.length >= 2) {
            agent.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
        } else if (agent.name.isNotEmpty()) {
            agent.name.take(1).uppercase()
        } else {
            "A"
        }
        val defaultBrush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
        Box(
            modifier = modifier.background(defaultBrush),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = (size.value * 0.32f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun EditProfileSheet(
    agent: com.example.domain.security.TelecallerAgent,
    onDismiss: () -> Unit,
    onSave: (newName: String, newPicture: String?) -> Unit
) {
    var nameInput by remember { mutableStateOf(agent.name) }
    var selectedPicture by remember { mutableStateOf(agent.profilePicture) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPicture = uri.toString()
            Toast.makeText(context, "Verification Photo Linked", Toast.LENGTH_SHORT).show()
        }
    }

    val previewAgent = remember(nameInput, selectedPicture) {
        agent.copy(name = nameInput, profilePicture = selectedPicture)
    }

    val backOverlay = Color(0xFF090F1C)
    val cardBg = Color(0xFF172033)
    val cardBorder = Color(0xFF1E293D)
    val primaryBlue = Color(0xFF3B82F6)
    val textSlate = Color(0xFFF8FAFC)
    val textMuted = Color(0xFF94A3B8)

    Card(
        modifier = Modifier
            .fillMaxSize()
            .testTag("edit_profile_sheet"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Carrier Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSlate
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_profile_edit")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = textSlate)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center) {
                AgentAvatar(agent = previewAgent, size = 110.dp)
                
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Pick Photo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap photo circle to upload custom avatar image",
                fontSize = 11.sp,
                color = textMuted
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Display Name/Alias") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_name_input"),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = textSlate, fontSize = 16.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = cardBorder,
                    focusedLabelColor = primaryBlue,
                    unfocusedLabelColor = textMuted,
                    cursorColor = primaryBlue
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CHOOSE SECURE SYSTEM THEME BADGE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textMuted,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val presets = listOf(
                "preset:cyber_blue" to "Cyber",
                "preset:gold_shield" to "Shield",
                "preset:emerald_guardian" to "Emerald",
                "preset:sunset_crimson" to "Sunset",
                "preset:cosmic_purple" to "Purple"
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(presets) { (presetId, label) ->
                    val isSelected = selectedPicture == presetId
                    val mockPreset = agent.copy(profilePicture = presetId)
                    Card(
                        modifier = Modifier
                            .width(85.dp)
                            .clickable { selectedPicture = presetId }
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) primaryBlue else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = backOverlay),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AgentAvatar(agent = mockPreset, size = 40.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) primaryBlue else textSlate,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { selectedPicture = null },
                colors = ButtonDefaults.buttonColors(containerColor = cardBorder, contentColor = textSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reset to Name Initials", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = cardBorder, contentColor = textMuted),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (nameInput.trim().isEmpty()) {
                            Toast.makeText(context, "Name cannot be empty!", Toast.LENGTH_SHORT).show()
                        } else {
                            onSave(nameInput.trim(), selectedPicture)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

