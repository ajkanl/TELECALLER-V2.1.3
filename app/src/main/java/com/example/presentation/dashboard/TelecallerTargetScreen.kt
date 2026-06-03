package com.example.presentation.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    
    var searchQuery by remember { mutableStateOf("") }
    var editingAgentId by remember { mutableStateOf<String?>(null) }
    var targetInputVal by remember { mutableStateOf("") }

    val primaryBlue = Color(0xFF3B82F6)
    val cardBorderColor = Color(0xFF1E293D)
    val textSlateColor = Color(0xFFF8FAFC)
    val textSlateMuted = Color(0xFF94A3B8)

    val filteredTelecallers = remember(telecallers, searchQuery) {
        if (searchQuery.isBlank()) {
            telecallers
        } else {
            telecallers.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery, ignoreCase = true)
            }
        }
    }

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
                .background(Color(0xFF090F1C))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Screen Header description
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                    .padding(bottom = 16.dp)
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
                        text = "Establish and adjust the baseline recovery goals for active agents. Changes are applied dynamically to the agent dashboard gauges and telemetry logs.",
                        fontSize = 11.sp,
                        color = textSlateMuted,
                        lineHeight = 16.sp
                    )
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
                    .padding(bottom = 16.dp)
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
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTelecallers) { agent ->
                        val isEditing = editingAgentId == agent.id
                        val formattedTarget = remember(agent.targetAmount) {
                            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                            formatter.format(agent.targetAmount)
                        }

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
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                .testTag("agent_target_card_${agent.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar circle
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(Color(0xFF1D2D44), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryBlue,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = agent.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = textSlateColor
                                        )
                                        Text(
                                            text = "ID: ${agent.id} • Calls: ${agent.callsDialed} • PTPs: ${agent.ptpsSecured}",
                                            fontSize = 11.sp,
                                            color = textSlateMuted
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Target Goal",
                                            fontSize = 10.sp,
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
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (isEditing) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
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
                                                    viewModel.updateAgentTarget(agent.id, amtVal)
                                                    editingAgentId = null
                                                    Toast.makeText(context, "Target goals synchronized successfully for ${agent.name}!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                                            modifier = Modifier.height(44.dp).testTag("target_save_btn_${agent.id}")
                                        ) {
                                            Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { editingAgentId = null },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            modifier = Modifier.height(44.dp).testTag("target_cancel_btn_${agent.id}")
                                        ) {
                                            Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            editingAgentId = agent.id
                                            targetInputVal = agent.targetAmount.toLong().toString()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
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
                                                text = "Adjust recovery target",
                                                color = primaryBlue,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
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
