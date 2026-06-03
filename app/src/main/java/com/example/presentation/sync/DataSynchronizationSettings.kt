package com.example.presentation.sync

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSynchronizationSettings(
    viewModel: DataSyncViewModel,
    onBack: () -> Unit
) {
    val wifiOnlyEnabled by viewModel.wifiOnlyEnabled.collectAsState()
    val syncFrequencyIndex by viewModel.syncFrequencyIndex.collectAsState()
    val cacheExpiryThreshold by viewModel.cacheExpiryThreshold.collectAsState()

    val primaryBlue = Color(0xFF3B82F6)
    val textSlateColor = Color(0xFFF8FAFC)
    val textSlateMuted = Color(0xFF94A3B8)
    val cardBackgroundColor = Color(0xFF172033)
    val dividerColor = Color(0xFF1E293D)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sync_settings_container")
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Data Synchronization",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("sync_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textSlateColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardBackgroundColor,
                    titleContentColor = textSlateColor
                ),
                modifier = Modifier.border(1.dp, dividerColor)
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Header Banner message
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Information Block",
                        tint = primaryBlue
                    )
                    Text(
                        text = "Tune WorkManager execution criteria, schedule background database sweeps, and optimize active cellular data consumption rates.",
                        fontSize = 12.sp,
                        color = Color(0xFF93C5FD),
                        lineHeight = 16.sp
                    )
                }
            }

            // --- CATEGORY-WISE CLOUD SYNC TERMINAL ---
            val isSyncing by viewModel.isSyncingToCloud.collectAsState()
            val syncLogs by viewModel.syncStatusLogs.collectAsState()

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFEFF6FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("☁️", fontSize = 18.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Category-Wise Cloud Sync Center",
                                fontWeight = FontWeight.Bold,
                                color = textSlateColor,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Syncs and archives separate, filtered categories to Firestore: debtors, call_logs, commitments, agents & compliance policies.",
                                fontSize = 11.sp,
                                color = textSlateMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Synchronous Active Trigger Action
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.triggerCategoryWisePushToCloud() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isSyncing,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("btn_trigger_sync_now")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            } else {
                                Text("☁️ SYNC NOW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Button(
                            onClick = { viewModel.wipeAllLocalAndCloudDataDirect() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isSyncing,
                            modifier = Modifier
                                .weight(1.1f)
                                .height(46.dp)
                                .testTag("btn_wipe_all_data")
                        ) {
                            Text("🗑️ RESET DB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        if (syncLogs.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { viewModel.clearLogHistory() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = textSlateMuted),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(46.dp)
                                    .testTag("btn_clear_sync_logs")
                            ) {
                                Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Developer Diagnostics Console Panel for category status
                    if (syncLogs.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp, min = 120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "DATA SYNC CONSOLE",
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                                Text(
                                    if (isSyncing) "● EXECUTING..." else "● IDLE / COMPLETED",
                                    color = if (isSyncing) Color(0xFFF59E0B) else Color(0xFF10B981),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(modifier = Modifier.fillMaxSize()) {
                                val scrollStateLogs = rememberScrollState()
                                LaunchedEffect(syncLogs.size) {
                                    scrollStateLogs.animateScrollTo(scrollStateLogs.maxValue)
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollStateLogs),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    syncLogs.forEach { log ->
                                        val isSuccess = log.startsWith("SUCCESS") || log.contains("Successful")
                                        val isFailure = log.startsWith("FAILURE") || log.startsWith("ERROR")
                                        val colorText = when {
                                            isSuccess -> Color(0xFF10B981)
                                            isFailure -> Color(0xFFEF4444)
                                            else -> Color(0xFF94A3B8)
                                        }
                                        Text(
                                            text = (if (isSuccess) "✓ " else if (isFailure) "✗ " else "  ") + log,
                                            color = colorText,
                                            fontSize = 11.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- SECTION 1: NETWORK CONSTRAINT SWITCH ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom wifi decoration shape
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFEFF6FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "📶",
                                color = primaryBlue,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Upload Voice Audio Over Wi-Fi Only",
                                fontWeight = FontWeight.Bold,
                                color = textSlateColor,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Defer heavy call recordings (.mp3/.amr) until the agent device connects to an authorized office Wi-Fi network to preserve local plan limits.",
                                color = textSlateMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = wifiOnlyEnabled,
                            onCheckedChange = { viewModel.updateWifiOnlyEnabled(it) },
                            modifier = Modifier.testTag("wifi_only_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = primaryBlue,
                                checkedTrackColor = Color(0xFFBFDBFE)
                            )
                        )
                    }
                }
            }

            // --- SECTION 2: FIRESTORE SYNC FREQUENCY SELECTOR ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp))
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
                            text = "Firestore Sync Frequency Selector",
                            fontWeight = FontWeight.Bold,
                            color = textSlateColor,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "Determine the underlying interval rate for real-time contact modifications and status polling from the database backend.",
                        fontSize = 12.sp,
                        color = textSlateMuted
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Slider mapping to intervals
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(
                            value = syncFrequencyIndex.toFloat(),
                            onValueChange = { viewModel.updateSyncFrequencyIndex(it.toInt()) },
                            valueRange = 0f..3f,
                            steps = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sync_frequency_slider"),
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
                            viewModel.frequencyOptions.forEachIndexed { index, option ->
                                val isSelected = syncFrequencyIndex == index
                                Text(
                                    text = option,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) primaryBlue else textSlateMuted,
                                    modifier = Modifier.widthIn(max = 75.dp),
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 3: LOCAL CACHE EXPIRY THRESHOLD ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Local Cache Expiry Threshold",
                        fontWeight = FontWeight.Bold,
                        color = textSlateColor,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "Configure how long settled client files and historic contact records remain stored inside local storage before automatic sweep routines purge them.",
                        fontSize = 12.sp,
                        color = textSlateMuted
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // A modern option row layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.cacheThresholdOptions.forEachIndexed { index, option ->
                            val isSelected = cacheExpiryThreshold == option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.15f) else cardBackgroundColor)
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) primaryBlue else dividerColor
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.updateCacheExpiryThreshold(option) }
                                    .padding(vertical = 12.dp)
                                    .testTag("cache_expiry_option_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = option,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) primaryBlue else textSlateColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Duration",
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.8f) else textSlateMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- SECTION 4: STUDENT DATABASE BULK UPLOAD ---
            val context = LocalContext.current
            var csvInputText by remember { mutableStateOf("") }
            var uploadFeedbackMessage by remember { mutableStateOf<String?>(null) }
            var isUploadSuccess by remember { mutableStateOf(true) }

            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                uri?.let {
                    try {
                        val inputStream = context.contentResolver.openInputStream(it)
                        val reader = java.io.BufferedReader(java.io.InputStreamReader(inputStream))
                        val sb = java.lang.StringBuilder()
                        var line: String? = reader.readLine()
                        while (line != null) {
                            sb.append(line).append("\n")
                            line = reader.readLine()
                        }
                        reader.close()
                        csvInputText = sb.toString()
                        uploadFeedbackMessage = "CSV file loaded successfully! Tap 'VALIDATE & IMPORT' below to process."
                        isUploadSuccess = true
                    } catch (e: Exception) {
                        uploadFeedbackMessage = "Failed to read CSV file: ${e.localizedMessage}"
                        isUploadSuccess = false
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFEF3C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📤", fontSize = 18.sp)
                        }
                        Column {
                            Text(
                                text = "Student Database Bulk Import",
                                fontWeight = FontWeight.Bold,
                                color = textSlateColor,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Upload, prefill description, or paste comma-separated CSV values for debtors.",
                                fontSize = 11.sp,
                                color = textSlateMuted
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Instruction checklist
                    Text(
                        text = "CSV columns must be:\nroll_no, student_name, phone, alt_phone, dues, base_fee, bucket, college, address, remarks",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryBlue,
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )

                    // Text Field for Direct CSV edit
                    OutlinedTextField(
                        value = csvInputText,
                        onValueChange = { csvInputText = it },
                        placeholder = {
                            Text(
                                text = "Paste CSV data or Tap 'Prefill Demo' below...\n\nExample row:\n1005A,Arman Singh,9876543210,,5400.0,7500.0,31-60,IIT Delhi,Delhi,Late dues enrollment",
                                fontSize = 12.sp,
                                color = textSlateMuted
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = textSlateColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("csv_input_text_field"),
                        maxLines = 10,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    // Error or Success Feedback Banner
                    uploadFeedbackMessage?.let { msg ->
                        val bannerColor = if (isUploadSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                        val textColor = if (isUploadSuccess) Color(0xFF15803D) else Color(0xFFB91C1C)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bannerColor, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isUploadSuccess) "✓" else "⚠️", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = msg, fontSize = 11.sp, color = textColor, modifier = Modifier.weight(1f), lineHeight = 14.sp)
                        }
                    }

                    // Multi button flow in row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Prefill button
                        OutlinedButton(
                            onClick = {
                                csvInputText = """roll_no,student_name,phone,alt_phone,dues,base_fee,bucket,college,address,remarks
1001A,Abhishek Kumar,9876543210,,4500.0,6000.0,60-90,Science and Tech,Patna Bihar,Pending sem 4 fee dues
1002B,Rohan Sharma,9555123456,9111222333,2800.0,2800.0,1-30,Business Management,Sector 15 Noida,Installment promise
1003C,Priya Patel,8888777666,,1200.0,1500.0,31-60,Architecture Department,S.V. Road Mumbai,Pending caution deposits
1004D,Kunal Verma,7777666555,7777666556,500.0,5000.0,1-30,Polytechnic Diploma,Civil Lines Kanpur,Partial fee pending
1005E,Sneha Reddy,9000800070,,6500.0,8000.0,90+,Medical Sciences,Gachibowli Hyderabad,Final term arrears"""
                                uploadFeedbackMessage = "Demo CSV data prefilled! Perfect to test parsing."
                                isUploadSuccess = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textSlateColor),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_prefill_demo_csv")
                        ) {
                            Text("📝 PREFILL DEMO", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Open system picker button
                        OutlinedButton(
                            onClick = { filePickerLauncher.launch("text/*") },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textSlateColor),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_pick_csv_file")
                        ) {
                            Text("📁 CHOOSE CSV", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = {
                            if (csvInputText.trim().isEmpty()) {
                                uploadFeedbackMessage = "Please paste or load CSV text first."
                                isUploadSuccess = false
                            } else {
                                val result = viewModel.bulkUploadStudents(csvInputText)
                                result.fold(
                                    onSuccess = { count ->
                                        uploadFeedbackMessage = "SUCCESS: Parsed & bulk-uploaded $count student records to internal SQLite database!"
                                        isUploadSuccess = true
                                        csvInputText = "" // clear on export success
                                    },
                                    onFailure = { err ->
                                        uploadFeedbackMessage = "IMPORT ERROR: ${err.localizedMessage}"
                                        isUploadSuccess = false
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_validate_and_upload")
                    ) {
                        Text("📤 VALIDATE & IMPORT DATA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // --- SECTION 5: TYPE-WISE EXTRAS DATA EXPORT ---
            val debtorsCount by viewModel.allDebtorsList.collectAsState()
            val callLogsCount by viewModel.allCallLogsList.collectAsState()
            val promisesCount by viewModel.allPromisesList.collectAsState()

            fun shareExportFile(type: String) {
                val fileUri = viewModel.exportTypeToCsvFile(context, type)
                if (fileUri != null) {
                    val label = when (type) {
                        "STUDENTS" -> "Student_Database_Excel"
                        "CALL_LOGS" -> "Call_Logs_Excel"
                        else -> "Payment_Commitments_Excel"
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        setDataAndType(fileUri, "text/csv")
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        putExtra(Intent.EXTRA_SUBJECT, "$label Export")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Download / Share $label"))
                } else {
                    android.widget.Toast.makeText(context, "Export error: Could not build CSV file", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFEFF6FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📥", fontSize = 18.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Type-Wise Excel Document Export",
                                fontWeight = FontWeight.Bold,
                                color = textSlateColor,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Download/Export precise, separate categories formatted with MS Excel UTF-8 BOM encoding.",
                                fontSize = 11.sp,
                                color = textSlateMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Type export list 1: STUDENTS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Student Database / Debtors", fontWeight = FontWeight.Bold, color = textSlateColor, fontSize = 13.sp)
                            Text("${debtorsCount.size} active table rows", fontSize = 11.sp, color = textSlateMuted)
                        }
                        Button(
                            onClick = { shareExportFile("STUDENTS") },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp).testTag("btn_export_students")
                        ) {
                            Text("📥 EXPORT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Type export list 2: CALL LOGS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Communication History / Call Logs", fontWeight = FontWeight.Bold, color = textSlateColor, fontSize = 13.sp)
                            Text("${callLogsCount.size} activity logs", fontSize = 11.sp, color = textSlateMuted)
                        }
                        Button(
                            onClick = { shareExportFile("CALL_LOGS") },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp).testTag("btn_export_call_logs")
                        ) {
                            Text("📥 EXPORT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Type export list 3: PTPs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Payment Commitment Agreements (PTP)", fontWeight = FontWeight.Bold, color = textSlateColor, fontSize = 13.sp)
                            Text("${promisesCount.size} commitment rows", fontSize = 11.sp, color = textSlateMuted)
                        }
                        Button(
                            onClick = { shareExportFile("PAYMENTS") },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp).testTag("btn_export_ptps")
                        ) {
                            Text("📥 EXPORT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
