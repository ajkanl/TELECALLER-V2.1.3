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
    val isFormValid = remember(selectedOutcome, ptpDate, ptpAmount) {
        if (selectedOutcome.isEmpty()) {
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
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp)),
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
                                unfocusedBorderColor = Color(0xFFE2E8F0)
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
                                unfocusedBorderColor = Color(0xFFE2E8F0)
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
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Dynamic Informational Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Sync status: Logging after call drop automatically connects to central debt registry once saved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF92400E)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 5. ACTION BAR SUBMISSIONS ---
            Button(
                onClick = {
                    if (isFormValid) {
                        val finalNotes = if (selectedOutcome == "Promise to Pay (PTP)") {
                            "Promised PTP Date: $ptpDate, Amount: ₹$ptpAmount\n$notesText"
                        } else {
                            notesText
                        }

                        // Save the Call outcome using the existing ViewModel function
                        viewModel.addCallLogEntry(
                            debtorId = debtor.id,
                            debtorName = debtor.name,
                            outcome = selectedOutcome,
                            notes = finalNotes
                        )

                        Toast.makeText(context, "Log synced successfully!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) Color(0xFF10B981) else Color(0xFF94A3B8),
                    disabledContainerColor = Color(0xFFCBD5E1)
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
