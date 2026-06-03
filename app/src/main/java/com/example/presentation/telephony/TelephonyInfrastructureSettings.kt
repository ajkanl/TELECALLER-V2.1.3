package com.example.presentation.telephony

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelephonyInfrastructureSettings(
    viewModel: TelephonyViewModel,
    onBack: () -> Unit
) {
    val simRule by viewModel.simRule.collectAsState()
    val recordingPolicy by viewModel.recordingPolicy.collectAsState()
    val maxDailyCalls by viewModel.maxDailyCalls.collectAsState()

    val primaryBlue = Color(0xFF2563EB)
    val textSlateColor = Color(0xFF1E293B)
    val textSlateMuted = Color(0xFF64748B)

    val simOptions = listOf(
        "Force SIM 1 Only",
        "Force SIM 2 Only",
        "Allow Agent Choice (Default)"
    )

    val recordingOptions = listOf(
        "Record All Interactions (Mandatory)",
        "Record Connected Outbound Calls Only",
        "Disable Voice Storage"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("telephony_settings_container")
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Telephony Infrastructure",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("telephony_back_button")
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

            // Introduction Info Alert
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = primaryBlue
                    )
                    Text(
                        text = "Manage agent hardware configurations, regulatory storage adherence settings, and handset network cellular constraints from this panel.",
                        fontSize = 12.sp,
                        color = Color(0xFF1D4ED8),
                        lineHeight = 16.sp
                    )
                }
            }

            // --- SECTION 1: DUAL SIM PRIMARY SLOT SELECTOR ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            text = "Dual SIM Primary Slot Selector",
                            fontWeight = FontWeight.Bold,
                            color = textSlateColor,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "Specify which physical cellular slot should be utilized by default during carrier dialing requests.",
                        fontSize = 12.sp,
                        color = textSlateMuted
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Column of Radio item options
                    simOptions.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateSimRule(option) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = (simRule == option),
                                    onClick = { viewModel.updateSimRule(option) },
                                    modifier = Modifier.testTag("sim_rule_option_$index"),
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryBlue)
                                )
                                Text(
                                    text = option,
                                    fontSize = 13.sp,
                                    color = textSlateColor,
                                    fontWeight = if (simRule == option) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 2: CALL RECORDING POLICY GROUP ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            text = "Call Recording Policy Group",
                            fontWeight = FontWeight.Bold,
                            color = textSlateColor,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "Configure legal audio safety flags and recording constraints for storage in the centralized compliance backend.",
                        fontSize = 12.sp,
                        color = textSlateMuted
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    recordingOptions.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateRecordingPolicy(option) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = (recordingPolicy == option),
                                    onClick = { viewModel.updateRecordingPolicy(option) },
                                    modifier = Modifier.testTag("recording_policy_option_$index"),
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryBlue)
                                )
                                Text(
                                    text = option,
                                    fontSize = 13.sp,
                                    color = textSlateColor,
                                    fontWeight = if (recordingPolicy == option) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 3: DAILY CALL THROTTLE COUNTER ---
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
                    Text(
                        text = "Daily Call Throttle Counter",
                        fontWeight = FontWeight.Bold,
                        color = textSlateColor,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "Define the maximum permitted outbound dialing attempts per handset each day to avoid carrier plan suspensions or dialing weariness.",
                        fontSize = 12.sp,
                        color = textSlateMuted
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Handset Limit",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = textSlateColor
                        )

                        // Editable text input field synced with the slider
                        var textValue by remember(maxDailyCalls) { mutableStateOf(maxDailyCalls.toString()) }

                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { newValue ->
                                textValue = newValue
                                newValue.toIntOrNull()?.let {
                                    if (it in 50..500) {
                                        viewModel.updateMaxDailyCalls(it)
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(90.dp)
                                .testTag("daily_throttle_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )
                    }

                    // Slider Component linked to VM maxDailyCalls
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Slider(
                            value = maxDailyCalls.toFloat(),
                            onValueChange = { viewModel.updateMaxDailyCalls(it.toInt()) },
                            valueRange = 50f..500f,
                            steps = 9, // Steps of 50
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("daily_throttle_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = primaryBlue,
                                activeTrackColor = primaryBlue,
                                inactiveTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("50 calls", fontSize = 11.sp, color = textSlateMuted)
                            Text("Current Limit: $maxDailyCalls", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryBlue)
                            Text("500 calls", fontSize = 11.sp, color = textSlateMuted)
                        }
                    }
                }
            }
        }
    }
}
