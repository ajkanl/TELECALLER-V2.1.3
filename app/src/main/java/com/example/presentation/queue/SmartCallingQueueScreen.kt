package com.example.presentation.queue

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.DebtorEntity
import com.example.data.local.entity.PromiseToPayEntity
import com.example.domain.model.Debtor
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCallingQueueScreen(
    viewModel: SmartCallingQueueViewModel,
    onBack: () -> Unit,
    onCallDebtor: (Debtor) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    val eligibleCalling by viewModel.eligibleCallingQueue.collectAsState()
    val eligibleFollowUp by viewModel.eligibleFollowUpQueue.collectAsState()
    val allDebtors by viewModel.allDebtors.collectAsState()
    val allCallLogs by viewModel.allCallLogs.collectAsState()
    val allPromises by viewModel.allPromises.collectAsState()

    // Dialog state for explaining lockouts
    var showLockDialog by remember { mutableStateOf(false) }
    var lockDialogTitle by remember { mutableStateOf("") }
    var lockDialogMessage by remember { mutableStateOf("") }

    // Colors
    val primaryBlue = Color(0xFF2563EB)
    val backgroundGray = Color(0xFFF8FAFC)
    val cardBg = Color.Transparent
    val textPrimary = Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)
    val greenStatus = Color(0xFF10B981)
    val orangeStatus = Color(0xFFF59E0B)
    val redStatus = Color(0xFFEF4444)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Calling Queue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = textPrimary
                        )
                        Text(
                            text = "Automated Compliance Guardrails",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("smart_queue_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = textPrimary
                )
            )
        },
        containerColor = backgroundGray
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TabRow Setup
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = primaryBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = primaryBlue
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Dues Reminder",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(
                                containerColor = if (selectedTab == 0) primaryBlue else Color(0xFFE2E8F0),
                                contentColor = if (selectedTab == 0) Color.White else textSecondary
                            ) {
                                Text("${eligibleCalling.size}")
                            }
                        }
                    },
                    modifier = Modifier.testTag("dues_reminder_tab")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Follow-up Window",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(
                                containerColor = if (selectedTab == 1) orangeStatus else Color(0xFFE2E8F0),
                                contentColor = if (selectedTab == 1) Color.White else textSecondary
                            ) {
                                Text("${eligibleFollowUp.size}")
                            }
                        }
                    },
                    modifier = Modifier.testTag("follow_up_tab")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main List based on Selected Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                val currentTimestamp = System.currentTimeMillis()

                if (selectedTab == 0) {
                    // TAB 1: DUES REMINDER QUEUE
                    // Split into Eligible (Unlocked) and Restricted (In Cooldown)
                    val (unlockedList, lockedList) = allDebtors.partition { debtor ->
                        eligibleCalling.any { it.id == debtor.id }
                    }

                    if (unlockedList.isNotEmpty()) {
                        item {
                            QueueSectionHeader(title = "Eligible for Reminder Dialing", count = unlockedList.size, color = greenStatus)
                        }
                        items(unlockedList) { debtor ->
                            DebtorQueueCard(
                                debtor = debtor,
                                tabIndex = 0,
                                isLocked = false,
                                lastCall = allCallLogs.filter { it.debtorId == debtor.id }.maxByOrNull { it.callTimestamp },
                                ptp = allPromises.firstOrNull { it.debtorId == debtor.id && it.ptpStatus == "ACTIVE" },
                                currentTimestamp = currentTimestamp,
                                onCallClick = {
                                    val mapped = debtor.toDomain()
                                    onCallDebtor(mapped)
                                },
                                onLockedClick = {}
                            )
                        }
                    }

                    if (lockedList.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            QueueSectionHeader(title = "Restricted (15-day Standard Cooldown / Future PTP)", count = lockedList.size, color = textSecondary)
                        }
                        items(lockedList) { debtor ->
                            val lastCallLog = allCallLogs.filter { it.debtorId == debtor.id }.maxByOrNull { it.callTimestamp }
                            val ptpLog = allPromises.firstOrNull { it.debtorId == debtor.id && it.ptpStatus == "ACTIVE" }

                            DebtorQueueCard(
                                debtor = debtor,
                                tabIndex = 0,
                                isLocked = true,
                                lastCall = lastCallLog,
                                ptp = ptpLog,
                                currentTimestamp = currentTimestamp,
                                onCallClick = {},
                                onLockedClick = {
                                    // Generate descriptive lockout details
                                    lockDialogTitle = "Compliance Call Restricted"
                                    lockDialogMessage = when {
                                        lastCallLog != null && (currentTimestamp - lastCallLog.callTimestamp) < 15L * 24 * 3600 * 1000 -> {
                                            val daysPassed = (currentTimestamp - lastCallLog.callTimestamp) / (24 * 3600 * 1000)
                                            val remainingDays = 15 - daysPassed
                                            "Regulatory Guideline Guardrail: Debtor was contacted $daysPassed days ago (${formatTimestamp(lastCallLog.callTimestamp)}). Under compliance policy, a standard 15-day cooldown applies to prevent customer harassment. Reopens in $remainingDays days."
                                        }
                                        ptpLog != null && ptpLog.promisedPaymentDate > currentTimestamp -> {
                                            "Active Commitment Guardrail: This customer has an active Promise-to-Pay (PTP) scheduled for ${formatTimestamp(ptpLog.promisedPaymentDate)}. You are prohibited from calling before this agreed commitment date."
                                        }
                                        else -> {
                                            "General Policy Guardrail: This record is locked as it does not meet the necessary temporal or status parameters for a general dues reminder."
                                        }
                                    }
                                    showLockDialog = true
                                    Toast.makeText(context, "Contact Locked: Regulatory Cooldown active", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    if (unlockedList.isEmpty() && lockedList.isEmpty()) {
                        item {
                            NoRecordsView("No records found in the Dues Reminder Queue.")
                        }
                    }

                } else {
                    // TAB 2: ACTIVE FOLLOW-UPS
                    // Partition all debtors who have an active PTP into Eligible (Past due within 15 days of current month) and Restricted (Future commitments or expired)
                    val debtorsWithPromises = allDebtors.filter { debtor ->
                        allPromises.any { it.debtorId == debtor.id && it.ptpStatus == "ACTIVE" }
                    }

                    val (unlockedList, lockedList) = debtorsWithPromises.partition { debtor ->
                        eligibleFollowUp.any { it.id == debtor.id }
                    }

                    if (unlockedList.isNotEmpty()) {
                        item {
                            QueueSectionHeader(title = "High-priority Follow-up Tasks", count = unlockedList.size, color = orangeStatus)
                        }
                        items(unlockedList) { debtor ->
                            DebtorQueueCard(
                                debtor = debtor,
                                tabIndex = 1,
                                isLocked = false,
                                lastCall = allCallLogs.filter { it.debtorId == debtor.id }.maxByOrNull { it.callTimestamp },
                                ptp = allPromises.firstOrNull { it.debtorId == debtor.id && it.ptpStatus == "ACTIVE" },
                                currentTimestamp = currentTimestamp,
                                onCallClick = {
                                    val mapped = debtor.toDomain()
                                    onCallDebtor(mapped)
                                },
                                onLockedClick = {}
                            )
                        }
                    }

                    if (lockedList.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            QueueSectionHeader(title = "Restricted Follow-ups (Expired / Future)", count = lockedList.size, color = textSecondary)
                        }
                        items(lockedList) { debtor ->
                            val lastCallLog = allCallLogs.filter { it.debtorId == debtor.id }.maxByOrNull { it.callTimestamp }
                            val ptpLog = allPromises.firstOrNull { it.debtorId == debtor.id && it.ptpStatus == "ACTIVE" }

                            DebtorQueueCard(
                                debtor = debtor,
                                tabIndex = 1,
                                isLocked = true,
                                lastCall = lastCallLog,
                                ptp = ptpLog,
                                currentTimestamp = currentTimestamp,
                                onCallClick = {},
                                onLockedClick = {
                                    lockDialogTitle = "Commitment Follow-up Restricted"
                                    lockDialogMessage = when {
                                        ptpLog != null && ptpLog.promisedPaymentDate > currentTimestamp -> {
                                            "Scheduled Commit: This payment timeline is scheduled for ${formatTimestamp(ptpLog.promisedPaymentDate)}. Early contact is prohibited until the target date is reached."
                                        }
                                        ptpLog != null && (currentTimestamp - ptpLog.promisedPaymentDate) > 15L * 24 * 3600 * 1000 -> {
                                            "Grace Period Expired: The agreed payment date was ${formatTimestamp(ptpLog.promisedPaymentDate)}, which is more than 15 days ago. This record has dropped out of the automated follow-up window and must be escalated to formal collections."
                                        }
                                        ptpLog != null && !isSameMonth(ptpLog.promisedPaymentDate, currentTimestamp) -> {
                                            "Calendar Month Restriction: The payment timeline (${formatTimestamp(ptpLog.promisedPaymentDate)}) belongs to a previous month. Automated follow-ups cannot cross calendar-month boundaries. File marked for manual supervisor allocation."
                                        }
                                        lastCallLog != null && (currentTimestamp - lastCallLog.callTimestamp) < 15L * 24 * 3600 * 1000 -> {
                                            "Cooldown Active: You have already contacted this customer ${ (currentTimestamp - lastCallLog.callTimestamp) / (24 * 3600 * 1000) } days ago. Under compliance guidelines, we must respect the 15-day communication spacing rules."
                                        }
                                        else -> {
                                            "Escalated Record: This commitment follow-up is currently locked as it sits outside permissible collection grace parameters."
                                        }
                                    }
                                    showLockDialog = true
                                    Toast.makeText(context, "Follow-up Locked: Grace Expired / Target Future", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    if (unlockedList.isEmpty() && lockedList.isEmpty()) {
                        item {
                            NoRecordsView("No active payment commitments found in the database. Add promises to populate.")
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue explaining Guardrail policies
    if (showLockDialog) {
        AlertDialog(
            onDismissRequest = { showLockDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = orangeStatus,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = lockDialogTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Text(
                    text = lockDialogMessage,
                    fontSize = 14.sp,
                    color = textPrimary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showLockDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Understood", color = Color.White)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFFF8FAFC)
        )
    }
}

@Composable
fun QueueSectionHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count Accounts",
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DebtorQueueCard(
    debtor: DebtorEntity,
    tabIndex: Int,
    isLocked: Boolean,
    lastCall: CallLogEntity?,
    ptp: PromiseToPayEntity?,
    currentTimestamp: Long,
    onCallClick: () -> Unit,
    onLockedClick: () -> Unit
) {
    val progressBorderColor = if (isLocked) Color(0xFFE2E8F0) else Color(0xFFE2E8F0)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("debtor_queue_card_${debtor.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color(0xFFF1F5F9).copy(alpha = 0.6f) else Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLocked) 0.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Debtor text information
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = debtor.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isLocked) Color(0xFF94A3B8) else Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Segment Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isLocked) Color(0xFFCBD5E1).copy(alpha = 0.3f)
                                else if (debtor.customerSegment == "High Value") Color(0xFFFDF2F8) else Color(0xFFEFF6FF)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = debtor.customerSegment,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLocked) Color(0xFF94A3B8) else if (debtor.customerSegment == "High Value") Color(0xFFDB2777) else Color(0xFF2563EB)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Overdue amount
                Text(
                    text = "${formatCurrency(debtor.totalOverdueAmount)} Overdue  •  Bucket ${debtor.dpdBucket} DPD",
                    fontSize = 13.sp,
                    color = if (isLocked) Color(0xFF94A3B8) else Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Indicator Chips based on compliance state
                if (tabIndex == 0) {
                    // Dues Reminder Badge
                    val lastCallText = if (lastCall != null) {
                        val days = (currentTimestamp - lastCall.callTimestamp) / (1000 * 3600 * 24)
                        if (isLocked) "Last Called: $days days ago (Cooldown)" else "Last Called: $days days ago"
                    } else {
                        "Last Called: Never Called"
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isLocked) Color(0xFFE2E8F0) else Color(0xFFECFDF5)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isLocked) Color(0xFF64748B) else Color(0xFF10B981),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = lastCallText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked) Color(0xFF64748B) else Color(0xFF047857)
                        )
                    }
                } else {
                    // Active Follow-Ups Badge
                    val followUpText = if (ptp != null) {
                        if (ptp.promisedPaymentDate > currentTimestamp) {
                            "Future Promise: Due on ${formatTimestamp(ptp.promisedPaymentDate)}"
                        } else {
                            val daysPassed = (currentTimestamp - ptp.promisedPaymentDate) / (1000 * 3600 * 24)
                            val remainingGrace = 15 - daysPassed
                            val daysLeftInMonth = getDaysLeftInCurrentMonth()
                            val limit = minOf(remainingGrace, daysLeftInMonth.toLong())

                            if (limit <= 0) {
                                "Timeline Broken: 0 Days Remaining (Expired)"
                            } else if (daysPassed <= 0) {
                                "Due Today"
                            } else {
                                "Timeline Broken: $limit Days Remaining This Month"
                            }
                        }
                    } else {
                        "No Payment Timeline Configured"
                    }

                    val badgeBg = if (isLocked) Color(0xFFE2E8F0) else if (followUpText == "Due Today") Color(0xFFFEF3C7) else Color(0xFFFFF7ED)
                    val badgeTint = if (isLocked) Color(0xFF64748B) else if (followUpText == "Due Today") Color(0xFFD97706) else Color(0xFFEA580C)

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Info,
                            contentDescription = null,
                            tint = badgeTint,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = followUpText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTint
                        )
                    }
                }
            }

            // Dial Trigger (Phone Button)
            val buttonColor = if (isLocked) Color(0xFFCBD5E1) else if (tabIndex == 1) Color(0xFFEA580C) else Color(0xFF10B981)
            IconButton(
                onClick = { if (isLocked) onLockedClick() else onCallClick() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(buttonColor)
                    .testTag("dial_button_${debtor.id}"),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Call,
                    contentDescription = if (isLocked) "Call Locked" else "Call Customer",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun NoRecordsView(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Mapper extension
private fun DebtorEntity.toDomain(): Debtor {
    return Debtor(
        id = id,
        name = name,
        overdueDays = when (dpdBucket) {
            "1-30" -> 15
            "31-60" -> 45
            "61-90" -> 75
            else -> 105
        },
        outstandingAmount = totalOverdueAmount,
        customerSegment = customerSegment,
        phoneNumber = phoneNumber,
        address = address,
        lastContactDate = lastContactDate,
        college = college,
        remarks = remarks
    )
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return format.format(amount)
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
}

private fun isSameMonth(timestamp1: Long, timestamp2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}

private fun getDaysLeftInCurrentMonth(): Int {
    val calendar = Calendar.getInstance()
    val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    return lastDay - today
}
