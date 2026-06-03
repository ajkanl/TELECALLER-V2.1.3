package com.example.presentation.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Debtor
import com.example.presentation.home.HomeViewModel

@Composable
fun DebtorProfileScreen(viewModel: HomeViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val debtor by viewModel.selectedDebtor.collectAsState()

    if (debtor == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No debtor selected", color = Color(0xFF64748B))
        }
        return
    }

    val currentDebtor = debtor!!
    val callLogs by viewModel.getCallLogsForDebtor(currentDebtor.id).collectAsState(initial = emptyList())

    var isEditing by remember { mutableStateOf(false) }

    // Form states for profile editing
    var editName by remember(currentDebtor) { mutableStateOf(currentDebtor.name) }
    var editPhone by remember(currentDebtor) { mutableStateOf(currentDebtor.phoneNumber) }
    var editAddress by remember(currentDebtor) { mutableStateOf(currentDebtor.address) }
    var editAmount by remember(currentDebtor) { mutableStateOf(currentDebtor.outstandingAmount.toString()) }
    var editLastContact by remember(currentDebtor) { mutableStateOf(currentDebtor.lastContactDate) }

    // Form states for adding new call logs/notes
    var inputOutcome by remember { mutableStateOf("Not Picked Up") }
    var inputNotes by remember { mutableStateOf("") }

    val primaryBlue = Color(0xFF3B82F6)
    val textSlateColor = Color(0xFFF8FAFC)
    val textSlateMuted = Color(0xFF94A3B8)
    val dividerColor = Color(0xFF1E293D)
    val cardBackgroundColor = Color(0xFF172033)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090F1C))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- PROFILE HEADER BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF172033))
                .border(1.dp, Color(0xFF1E293D))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back",
                    tint = textSlateColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentDebtor.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSlateColor
                )
                Text(
                    text = "ID: #${currentDebtor.id} • ${currentDebtor.customerSegment}",
                    fontSize = 11.sp,
                    color = textSlateMuted
                )
            }

            // Edit toggle mode
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isEditing) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                    .clickable { isEditing = !isEditing }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = if (isEditing) primaryBlue else textSlateColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEditing) "Viewing" else "Modify Profile",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEditing) primaryBlue else textSlateColor
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- PROFILE DETAIL / CARD WORKSPACE ---
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dividerColor, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "DEBTOR ACCOUNT METADATA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSlateMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isEditing) {
                            // VIEWING MODE
                            ProfileDetailField(label = "Outstanding Amount", value = "₹${"%,.2f".format(currentDebtor.outstandingAmount)}", highlight = true)
                            Spacer(modifier = Modifier.height(10.dp))
                            ProfileDetailField(label = "Contact Number", value = viewModel.maskPhoneNumber(currentDebtor.phoneNumber))
                            Spacer(modifier = Modifier.height(10.dp))
                            ProfileDetailField(label = "Current Address", value = currentDebtor.address)
                            Spacer(modifier = Modifier.height(10.dp))
                            ProfileDetailField(label = "Last Contact Date", value = currentDebtor.lastContactDate)
                        } else {
                            // EDITING MODE FORM
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("edit_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryBlue)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = { Text("Contact Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("edit_phone_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryBlue)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editAddress,
                                onValueChange = { editAddress = it },
                                label = { Text("Address") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_address_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryBlue)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editAmount,
                                onValueChange = { editAmount = it },
                                label = { Text("Outstanding Amount (₹)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("edit_amount_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryBlue)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editLastContact,
                                onValueChange = { editLastContact = it },
                                label = { Text("Last Contact Date") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("edit_last_contact_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryBlue)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val amt = editAmount.toDoubleOrNull() ?: currentDebtor.outstandingAmount
                                    val updated = currentDebtor.copy(
                                        name = editName,
                                        phoneNumber = editPhone,
                                        address = editAddress,
                                        outstandingAmount = amt,
                                        lastContactDate = editLastContact
                                    )
                                    viewModel.updateDebtorProfile(updated)
                                    isEditing = false
                                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_profile_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // --- RECORD NEW CALL OUTCOME ---
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dividerColor, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "RECORD NEW CALL OUTCOME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSlateMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("PTP Promised", "Not Picked Up").forEach { status ->
                                val isSelected = inputOutcome == status
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF1F5F9))
                                        .border(1.dp, if (isSelected) primaryBlue else Color.Transparent, RoundedCornerShape(12.dp))
                                        .clickable { inputOutcome = status }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = status,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) primaryBlue else textSlateColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputNotes,
                            onValueChange = { inputNotes = it },
                            placeholder = { Text("Enter detailed call notes / PTP commitments...", fontSize = 12.sp, color = textSlateMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("call_notes_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryBlue)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (inputNotes.isBlank()) {
                                    Toast.makeText(context, "Please enter notes before saving log", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.addCallLogEntry(
                                    debtorId = currentDebtor.id,
                                    debtorName = currentDebtor.name,
                                    outcome = inputOutcome,
                                    notes = inputNotes
                                )
                                inputNotes = ""
                                Toast.makeText(context, "Call log recorded!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("record_outcome_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Record Outcome & Notes", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- HISTORICAL CALL LOGS CHRONOLOGICAL DISPLAY ---
            item {
                Text(
                    text = "Contact History & Notes Logs",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSlateColor,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (callLogs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                        modifier = Modifier.fillMaxWidth().border(1.dp, dividerColor, RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            text = "No contact attempts registered for this account.",
                            fontSize = 12.sp,
                            color = textSlateMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        )
                    }
                }
            } else {
                items(callLogs) { log ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, dividerColor, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (log.status == "PTP Promised") Color(0xFFD1FAE5) else Color(0xFFFFEDD5))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = log.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (log.status == "PTP Promised") Color(0xFF047857) else Color(0xFFD97706)
                                    )
                                }

                                Text(
                                    text = "${log.date} @ ${log.time}",
                                    fontSize = 11.sp,
                                    color = textSlateMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = dividerColor)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Notes:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSlateMuted
                            )
                            Text(
                                text = log.notes,
                                fontSize = 12.sp,
                                color = textSlateColor,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailField(label: String, value: String, highlight: Boolean = false) {
    Column {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = if (highlight) 18.sp else 13.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) Color(0xFF2563EB) else Color(0xFF1E293B),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
