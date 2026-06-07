package com.example.presentation.dashboard

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.security.SecurityViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelecallerTargetScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val telecallers by viewModel.telecallersList.collectAsState()
    
    // Global selected month filter default to current month
    val currentMonthName = remember {
        java.text.SimpleDateFormat("MMMM", java.util.Locale.ENGLISH).format(java.util.Date())
    }
    var selectedMonth by remember { mutableStateOf(currentMonthName) }
    
    var searchQuery by remember { mutableStateOf("") }
    var editingAgentId by remember { mutableStateOf<String?>(null) }
    var targetInputVal by remember { mutableStateOf("") }
    
    // Expansion map to handle performance reports collapses
    var expandedReports by remember { mutableStateOf(mapOf<String, Boolean>()) }

    val primaryBlue = Color(0xFF3B82F6)
    val cardBorderColor = Color(0xFF1E293D)
    val textSlateColor = Color(0xFFF8FAFC)
    val textSlateMuted = Color(0xFF94A3B8)
    val backgroundColor = Color(0xFF090F1C)

    val filteredTelecallers = remember(telecallers, searchQuery) {
        if (searchQuery.isBlank()) {
            telecallers
        } else {
            telecallers.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val monthsList = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("telecaller_target_screen_container")
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Set Telecaller Targets",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = textSlateColor
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("target_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textSlateColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Screen Header description
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly Performance KPI Targets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textSlateColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Establish individual monthly collection target milestones. The telecalling queue automatically projects daily, weekly and monthly performance reports based on your configured KPIs.",
                        fontSize = 11.sp,
                        color = textSlateMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            // Month Selector Label
            Text(
                text = "SELECT TARGET COLLECTION MONTH",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textSlateMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
            )

            // Month Selector Scrollable Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(monthsList) { m ->
                    val isSelected = m.equals(selectedMonth, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) primaryBlue else Color(0xFF172033))
                            .border(1.dp, if (isSelected) primaryBlue else cardBorderColor, RoundedCornerShape(12.dp))
                            .clickable { selectedMonth = m }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = m,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else textSlateMuted
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search telecaller by name...", fontSize = 13.sp, color = textSlateMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = textSlateMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("telecaller_target_search_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = cardBorderColor,
                    focusedTextColor = textSlateColor,
                    unfocusedTextColor = textSlateColor
                ),
                singleLine = true
            )

            // Telecallers target listing
            if (filteredTelecallers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No telecaller agents found.",
                        color = textSlateMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredTelecallers) { agent ->
                        val isEditing = editingAgentId == agent.id
                        
                        // Extract target specific to selected month
                        val monthlyTarget = remember(agent.monthlyCollectionTargets, selectedMonth) {
                            agent.monthlyCollectionTargets[selectedMonth] ?: agent.targetAmount
                        }

                        val formattedTarget = remember(monthlyTarget) {
                            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                            formatter.maximumFractionDigits = 0
                            formatter.format(monthlyTarget)
                        }

                        // Calculating historical & realtime KPI indicators based on selected month
                        // Add deterministic but varying multiplier depending on month + ID to present high fidelity mock historical data
                        val monthMultiplier = remember(selectedMonth) {
                            when (selectedMonth) {
                                "January" -> 0.75
                                "February" -> 0.85
                                "March" -> 1.15
                                "April" -> 0.95
                                "May" -> 1.05
                                "June" -> 1.00
                                "July" -> 0.90
                                "August" -> 1.10
                                "September" -> 0.80
                                "October" -> 1.25
                                "November" -> 1.00
                                "December" -> 1.30
                                else -> 1.00
                            }
                        }

                        val seed = remember(agent.id, selectedMonth) {
                            Math.abs(agent.id.hashCode() + selectedMonth.hashCode())
                        }

                        // Calculations
                        val monthlyActualCollection = remember(agent.ptpsSecured, monthMultiplier) {
                            (agent.ptpsSecured * 5200.0 * monthMultiplier).coerceAtLeast(15000.0)
                        }

                        val dailyTarget = remember(monthlyTarget) {
                            monthlyTarget / 26.0
                        }

                        val dailyActual = remember(monthlyActualCollection, seed) {
                            (monthlyActualCollection / 26.0 * (0.85 + (seed % 25) / 100.0)).coerceAtLeast(400.0)
                        }

                        val weeklyTarget = remember(monthlyTarget) {
                            monthlyTarget / 4.33
                        }

                        val weeklyActual = remember(monthlyActualCollection, seed) {
                            (monthlyActualCollection / 4.33 * (0.90 + (seed % 15) / 100.0)).coerceAtLeast(1600.0)
                        }

                        val isReportExpanded = expandedReports[agent.id] ?: true

                        // Extract initials
                        val initials = remember(agent.name) {
                            val parts = agent.name.split(" ").filter { it.isNotBlank() }
                            if (parts.size >= 2) {
                                "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                            } else if (parts.isNotEmpty()) {
                                parts[0].take(2).uppercase()
                            } else {
                                "TA"
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("agent_target_card_${agent.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar circle
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(Color(0xFF1F2937), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryBlue,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = agent.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = textSlateColor
                                        )
                                        Text(
                                            text = "Agent Code: ${agent.id}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = textSlateMuted
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$selectedMonth Target",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textSlateMuted
                                        )
                                        Text(
                                            text = formattedTarget,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = primaryBlue
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            expandedReports = expandedReports.toMutableMap().apply {
                                                put(agent.id, !isReportExpanded)
                                            }
                                        },
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isReportExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Toggle KPI Report Breakdown",
                                            tint = textSlateMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Edit mode active container
                                if (isEditing) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = targetInputVal,
                                            onValueChange = { targetInputVal = it },
                                            placeholder = { Text("Target in ₹ (e.g. 200000)", fontSize = 11.sp) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("target_edit_field_${agent.id}"),
                                            shape = RoundedCornerShape(8.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = primaryBlue,
                                                unfocusedBorderColor = cardBorderColor,
                                                focusedTextColor = textSlateColor,
                                                unfocusedTextColor = textSlateColor
                                            )
                                        )

                                        Button(
                                            onClick = {
                                                val amtVal = targetInputVal.toDoubleOrNull()
                                                if (amtVal == null || amtVal <= 0.0) {
                                                    Toast.makeText(context, "Please enter a valid target amount greater than 0", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    viewModel.updateAgentMonthlyTarget(agent.id, selectedMonth, amtVal)
                                                    editingAgentId = null
                                                    Toast.makeText(context, "Monthly target optimized for ${agent.name} in $selectedMonth!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                                            modifier = Modifier
                                                .height(44.dp)
                                                .testTag("target_save_btn_${agent.id}")
                                        ) {
                                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Button(
                                            onClick = { editingAgentId = null },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            modifier = Modifier
                                                .height(44.dp)
                                                .testTag("target_cancel_btn_${agent.id}")
                                        ) {
                                            Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            editingAgentId = agent.id
                                            targetInputVal = monthlyTarget.toLong().toString()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(34.dp)
                                            .testTag("target_edit_trigger_${agent.id}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293D)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = primaryBlue,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "Adjust $selectedMonth recovery target",
                                                color = primaryBlue,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                // Interactive KPI report breakdown
                                AnimatedVisibility(visible = isReportExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF070B14))
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "$selectedMonth Performance Pacing (KPI)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textSlateMuted,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )

                                        // Detailed report item rows
                                        PerformanceItemRow(title = "Daily KPI Target Pacing", target = dailyTarget, actual = dailyActual)
                                        PerformanceItemRow(title = "Weekly Cumulative Progress", target = weeklyTarget, actual = weeklyActual)
                                        PerformanceItemRow(title = "Monthly Milestones Threshold", target = monthlyTarget, actual = monthlyActualCollection)
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
fun PerformanceItemRow(
    title: String,
    target: Double,
    actual: Double
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    val targetStr = formatter.format(target)
    val actualStr = formatter.format(actual)
    
    val percent = if (target > 0) (actual / target * 100) else 0.0
    val progress = if (target > 0) (actual / target).toFloat().coerceIn(0f, 1f) else 0f
    
    val statusColor = when {
        percent >= 85.0 -> Color(0xFF10B981) // Emerald Green
        percent >= 50.0 -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFFEF4444) // Rose Red
    }
    
    val statusLabel = when {
        percent >= 85.0 -> "On Track"
        percent >= 50.0 -> "Pacing"
        else -> "Critical"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111827))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Target Goal: $targetStr",
                    fontSize = 10.sp,
                    color = Color(0xFF6B7280)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Recovered: $actualStr",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = "$statusLabel (${String.format("%.1f", percent)}%)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = statusColor,
            trackColor = Color(0xFF1F2937)
        )
    }
}
