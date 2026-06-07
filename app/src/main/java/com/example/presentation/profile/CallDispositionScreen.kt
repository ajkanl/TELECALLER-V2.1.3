package com.example.presentation.profile

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Debtor
import com.example.presentation.home.HomeViewModel
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallDispositionScreen(
    debtor: Debtor,
    viewModel: HomeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 1. UI states
    var selectedOutcome by remember { mutableStateOf("") }
    var ptpDate by remember { mutableStateOf("") }
    var ptpAmount by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var selectedCallType by remember { mutableStateOf("Outbound") } // "Outbound", "Inbound"
    var selectedCategory by remember { mutableStateOf("Business") } // "Business", "Personal"

    // Student detail states (Editable at end of call)
    var studentName by remember { mutableStateOf(debtor.name) }
    var studentMobile by remember { mutableStateOf(debtor.phoneNumber) }
    var studentGuardian by remember { mutableStateOf(debtor.guardianNumber) }
    var studentDues by remember { mutableStateOf(if (debtor.outstandingAmount == 0.0) "" else debtor.outstandingAmount.toString()) }

    // Constants for color harmony
    val primaryBlue = Color(0xFF3B82F6)
    val textSlateColor = Color(0xFFF8FAFC)
    val textSlateMuted = Color(0xFF94A3B8)
    val cardBackgroundColor = Color(0xFF172033)
    val dividerColor = Color(0xFF1E293D)

    // Calendar Picker launcher
    fun launchDatePicker(currentText: String, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedMonth = String.format("%02d", selectedMonth + 1)
            val formattedDay = String.format("%02d", selectedDay)
            onDateSelected("$selectedYear-$formattedMonth-$formattedDay")
        }, year, month, day).show()
    }

    // Validation checks
    val isPtp = selectedOutcome == "Promise to Pay (PTP)"
    val isFormValid = remember(selectedOutcome, ptpDate, ptpAmount, studentName, studentMobile) {
        if (studentName.isBlank() || studentMobile.isBlank()) {
            false
        } else if (selectedOutcome.isEmpty()) {
            false
        } else if (isPtp) {
            val amountParsed = ptpAmount.toDoubleOrNull()
            ptpDate.isNotBlank() && amountParsed != null && amountParsed > 0.0
        } else {
            true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("disposition_screen_container")
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Log Call Disposition",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("disposition_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel disposition logging",
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. HEADER ACCOUNT BLOCK ---
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
                        text = "DEBTOR ACCOUNT OVERVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textSlateMuted,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF1D2D44), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = debtor.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textSlateColor
                                )
                                Text(
                                    text = "Account: #${debtor.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSlateMuted,
                                    modifier = Modifier.testTag("debtor_account_number_header")
                                )
                            }
                        }

                        // Outstanding Dues badge
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "OVERDUE BALANCE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textSlateMuted
                            )

                            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                            val formattedAmount = try {
                                currencyFormat.format(debtor.outstandingAmount)
                            } catch (e: Exception) {
                                "₹ %,.2f".format(locale = Locale.US, debtor.outstandingAmount)
                            }

                            Text(
                                text = formattedAmount,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.testTag("pending_dues_header")
                            )
                        }
                    }
                }
            }

            // --- EDITABLE STUDENT PROFILE & DUES CARD ---
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
                        text = "EDIT PROFILE & STUDENT DUES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryBlue,
                        letterSpacing = 0.5.sp
                    )

                    // Student Name Input
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Student/Contact Name") },
                        placeholder = { Text("Enter student name") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_student_name_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFF1E293D),
                            focusedTextColor = textSlateColor,
                            unfocusedTextColor = textSlateColor
                        )
                    )

                    // Mobile Number Input
                    OutlinedTextField(
                        value = studentMobile,
                        onValueChange = { studentMobile = it },
                        label = { Text("Mobile Number") },
                        placeholder = { Text("Enter mobile number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("edit_student_mobile_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFF1E293D),
                            focusedTextColor = textSlateColor,
                            unfocusedTextColor = textSlateColor
                        )
                    )

                    // Guardian Number Input
                    OutlinedTextField(
                        value = studentGuardian,
                        onValueChange = { studentGuardian = it },
                        label = { Text("Guardian / Parent Number") },
                        placeholder = { Text("Enter guardian number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("edit_student_guardian_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFF1E293D),
                            focusedTextColor = textSlateColor,
                            unfocusedTextColor = textSlateColor
                        )
                    )

                    // Student Dues (Outstanding Amount) numerical field
                    OutlinedTextField(
                        value = studentDues,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                                studentDues = it
                            }
                        },
                        label = { Text("Student Dues (₹)") },
                        placeholder = { Text("Enter student outstanding dues") },
                        leadingIcon = {
                            Text(
                                text = "₹",
                                fontWeight = FontWeight.Bold,
                                color = textSlateMuted,
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("edit_student_dues_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color(0xFF1E293D),
                            focusedTextColor = textSlateColor,
                            unfocusedTextColor = textSlateColor
                        )
                    )
                }
            }

            // --- NEW: CALL TYPE & CATEGORY SELECTION ---
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
                        text = "CALL METRICS DIRECTION & CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryBlue,
                        letterSpacing = 0.5.sp
                    )

                    // Call Direction (Type)
                    Text(
                        text = "Call Direction",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSlateMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .border(1.dp, dividerColor, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Outbound" to "📤 Outbound (Dialed)", "Inbound" to "📥 Inbound (Callback)").forEach { (key, label) ->
                            val isSelected = selectedCallType == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) primaryBlue else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCallType = key }
                                    .padding(vertical = 10.dp)
                                    .testTag("call_type_option_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else textSlateColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Call Category (Purpose)
                    Text(
                        text = "Call Type Classification",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSlateMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .border(1.dp, dividerColor, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Business" to "💼 Business Recoveries", "Personal" to "👤 Personal Follow-up").forEach { (key, label) ->
                            val isSelected = selectedCategory == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) primaryBlue else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCategory = key }
                                    .padding(vertical = 10.dp)
                                    .testTag("call_category_option_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else textSlateColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. OUTCOME SELECTION GRID ---
            Text(
                text = "Select Disposition Outcome",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textSlateColor
            )

            val dispositions = listOf(
                Pair("Promise to Pay (PTP)", "outcome_ptp"),
                Pair("Refused to Pay", "outcome_refused"),
                Pair("Wrong Number", "outcome_wrong_number"),
                Pair("No Answer / Busy", "outcome_no_answer")
            )

            // Grid of 2x2 selection layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until dispositions.size step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (j in i..i + 1) {
                            if (j < dispositions.size) {
                                val item = dispositions[j]
                                val isSelected = selectedOutcome == item.first
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.15f) else cardBackgroundColor)
                                        .border(
                                            1.dp,
                                            if (isSelected) primaryBlue else dividerColor,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedOutcome = item.first }
                                        .padding(horizontal = 12.dp, vertical = 16.dp)
                                        .testTag(item.second),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.first,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) primaryBlue else textSlateColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. CONDITIONAL PTP INPUT FIELDS ---
            AnimatedVisibility(
                visible = isPtp,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
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
                            text = "COMMITTED PTP SETTLEMENT DETAILS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryBlue,
                            letterSpacing = 0.5.sp
                        )

                        // Promised Payment Date select trigger
                        OutlinedTextField(
                            value = ptpDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Promised Payment Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Pick date trigger",
                                    tint = primaryBlue,
                                    modifier = Modifier.clickable {
                                        launchDatePicker(ptpDate) { ptpDate = it }
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ptp_date_field")
                                .clickable {
                                    launchDatePicker(ptpDate) { ptpDate = it }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = Color(0xFF1E293D),
                                focusedTextColor = textSlateColor,
                                unfocusedTextColor = textSlateColor
                            )
                        )

                        // Promised Payment Amount numerical entry
                        OutlinedTextField(
                            value = ptpAmount,
                            onValueChange = {
                                if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) {
                                    ptpAmount = it
                                }
                            },
                            label = { Text("Promised Amount (₹)") },
                            placeholder = { Text("Enter payment amount") },
                            leadingIcon = {
                                Text(
                                    text = "₹",
                                    fontWeight = FontWeight.Bold,
                                    color = textSlateMuted,
                                    modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ptp_amount_field"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = Color(0xFF1E293D),
                                focusedTextColor = textSlateColor,
                                unfocusedTextColor = textSlateColor
                            )
                        )
                    }
                }
            }

            // --- 4. AGENT NOTES INPUT ---
            Text(
                text = "Add Call Notes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textSlateColor
            )

            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                placeholder = {
                    Text(
                        text = "Enter summary of call notes or dispute details...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSlateMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Notes element icon",
                        tint = textSlateMuted,
                        modifier = Modifier.padding(bottom = 36.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("agent_notes_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = Color(0xFF1E293D),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = textSlateColor,
                    unfocusedTextColor = textSlateColor
                )
            )

            // Dynamic Informational Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFFB923C),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Sync status: Logging after call drop automatically connects to central debt registry once saved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFD180)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 5. ACTION BAR SUBMISSIONS ---
            Button(
                onClick = {
                    if (isFormValid) {
                        val typeTag = if (selectedCallType == "Inbound") "[Type: INBOUND]" else "[Type: OUTBOUND]"
                        val categoryTag = if (selectedCategory == "Personal") "[Category: Personal]" else "[Category: Business]"
                        val tagsPrefix = "$typeTag$categoryTag\n"

                        val finalNotes = if (selectedOutcome == "Promise to Pay (PTP)") {
                            "${tagsPrefix}Promised PTP Date: $ptpDate, Amount: ₹$ptpAmount\n$notesText"
                        } else {
                            "$tagsPrefix$notesText"
                        }

                        // Generate/Resolve unique clean ID for this student if currently blank
                        val resolvedId = if (debtor.id.isBlank()) "STU-${System.currentTimeMillis()}" else debtor.id

                        val updatedDebtor = debtor.copy(
                            id = resolvedId,
                            name = studentName,
                            phoneNumber = studentMobile,
                            guardianNumber = studentGuardian,
                            outstandingAmount = studentDues.toDoubleOrNull() ?: 0.0
                        )

                        // 1. Save and Upsert Student Profile (syncs to both databases)
                        viewModel.updateDebtorProfile(updatedDebtor)

                        // 2. Add Call Log connected with patient/student record
                        viewModel.addCallLogEntry(
                            debtorId = resolvedId,
                            debtorName = studentName,
                            outcome = selectedOutcome,
                            notes = finalNotes
                        )

                        Toast.makeText(context, "Log and profile updated successfully!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) Color(0xFF10B981) else Color(0xFF1E293D),
                    disabledContainerColor = Color(0xFF101B2E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_disposition_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Save Outcome & Sync Log",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
