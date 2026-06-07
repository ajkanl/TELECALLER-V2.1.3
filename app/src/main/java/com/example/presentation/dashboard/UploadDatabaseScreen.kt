package com.example.presentation.dashboard

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.home.HomeViewModel

// Helper helper function to resolve filename from Content URI safely
fun getFileName(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val displayNameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIdx >= 0) {
                    result = cursor.getString(displayNameIdx)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDatabaseScreen(
    homeViewModel: HomeViewModel,
    currentUser: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activeAgent by homeViewModel.activeImpersonatedAgent.collectAsState()
    val isCurrentUserAdmin = currentUser?.contains("admin", ignoreCase = true) == true || currentUser == "armankumar.singh24@gmail.com"
    val isAdmin = (activeAgent == null && isCurrentUserAdmin) || activeAgent?.permissions?.isAdmin == true

    val primaryBlue = Color(0xFF3B82F6)
    val cardBorderColor = Color(0xFF1E293D)
    val textSlateColor = Color(0xFFF8FAFC)
    val textSlateMuted = Color(0xFF94A3B8)
    val backgroundColor = Color(0xFF090F1C)

    if (!isAdmin) {
        // Redesigned unauthorized/restricted access boundary
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp)
                    .testTag("unauthorized_admin_barrier")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Access Restricted",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(64.dp)
                    )

                    Text(
                        text = "Access Restricted",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSlateColor
                    )

                    Text(
                        text = "The Database Ingestion and CSV Bulk Import module is strictly reserved for Admin accounts only.\n\nActive User: ${activeAgent?.name ?: currentUser ?: "Unknown"}\nAgent ID: ${activeAgent?.id ?: "N/A"}\nStatus: Unauthorized",
                        fontSize = 13.sp,
                        color = textSlateMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293D)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("barrier_return_button")
                    ) {
                        Text("Return to Safety", fontWeight = FontWeight.Bold, color = textSlateColor)
                    }
                }
            }
        }
    } else {
        var uploadCollegeName by remember { mutableStateOf("") }
        var uploadCsvContent by remember { mutableStateOf("") }
        var uploadResultMessage by remember { mutableStateOf<String?>(null) }
        var isProcessing by remember { mutableStateOf(false) }
        var isStudentSchemaMode by remember { mutableStateOf(false) }
        var importedFileName by remember { mutableStateOf<String?>(null) }

        // CSV File Selector contract
        val csvFilePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    val resolvedName = getFileName(context, uri) ?: "records.csv"
                    // Force CSV extension match
                    if (!resolvedName.endsWith(".csv", ignoreCase = true)) {
                        uploadResultMessage = "Error: Invalid file format ($resolvedName). Only CSV format files (.csv) are permitted by security policy."
                        Toast.makeText(context, "Permitted formats: .csv only", Toast.LENGTH_LONG).show()
                    } else {
                        // Read string contents from Content Resolver safely
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val rawText = inputStream.bufferedReader().use { it.readText() }
                            uploadCsvContent = rawText
                            importedFileName = resolvedName
                            uploadResultMessage = "Success: Loaded CSV file $resolvedName (${rawText.length} bytes)."
                            Toast.makeText(context, "CSV Linked: $resolvedName", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    uploadResultMessage = "Error loading local file: ${e.localizedMessage}"
                    Toast.makeText(context, "Unable to import CSV", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Sample templates
        val sampleRecoveryTemplate = """account_number,student_name,primary_phone,original_due_date,total_due_amount
S301,Vikram Rathod,9876543201,2025-06-05,52000
S302,Priya Nair,9876543202,2025-06-15,48000
S303,Kabir Mehta,9876543203,2025-06-20,74000"""

        val sampleStudentTemplate = """Roll,Name,Phone,Parent/guardian Number,COLLEGE,Course,Course Session,Father,Dob
Roll001,Amit Sharma,9876543201,9876543251,Oxford College of Science,B.Sc Computer Science,2023-2026,Rajesh Sharma,15-08-2002
Roll002,Sunita Patel,9876543202,9876543252,Oxford College of Science,M.Tech AI,2024-2026,Vijay Patel,22-11-2001
Roll003,John Doe,9876543203,9876543253,Stanford Law School,Master of Laws,2025-2026,Robert Doe,05-04-2003"""

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("upload_database_screen_container")
                .statusBarsPadding()
                .navigationBarsPadding(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Upload Data into Database",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = textSlateColor
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("upload_screen_back_button")
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // High level guidance banner
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = primaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isStudentSchemaMode) "Student Database Bulk Import" else "College-Wise Bulk Import System",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF93C5FD)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isStudentSchemaMode) {
                                    "Directly ingest your complete student database. This system maps Name, Father's Name, DOB, College, Course, Course Session, Roll number, Phone, and Parent/guardian contacts into our highly efficient local system state."
                                } else {
                                    "Use this interface to perform batch operations and ingest entire tables worth of student loan and overdue records from standardized CSV payloads directly into the local device database."
                                },
                                fontSize = 11.sp,
                                color = Color(0xFFBFDBFE),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Database format selection field
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF172033)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "A. Choose Database Schema Format",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = textSlateColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                onClick = {
                                    isStudentSchemaMode = false
                                    uploadCsvContent = ""
                                    importedFileName = null
                                    uploadResultMessage = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!isStudentSchemaMode) Color(0xFF1D2D44) else Color(0xFF131B2A)
                                ),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(64.dp)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (!isStudentSchemaMode) primaryBlue else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("schema_recovery_card")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = if (!isStudentSchemaMode) primaryBlue else textSlateMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Dues Recovery",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (!isStudentSchemaMode) primaryBlue else textSlateColor
                                        )
                                    }
                                }
                            }

                            Card(
                                onClick = {
                                    isStudentSchemaMode = true
                                    uploadCsvContent = ""
                                    importedFileName = null
                                    uploadResultMessage = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isStudentSchemaMode) Color(0xFF1D2D44) else Color(0xFF131B2A)
                                ),
                                modifier = Modifier
                                    .weight(2f)
                                    .height(64.dp)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isStudentSchemaMode) primaryBlue else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("schema_student_card")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isStudentSchemaMode) primaryBlue else textSlateMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Student Database",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isStudentSchemaMode) primaryBlue else textSlateColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // CSV specifications and sample templates
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. Plain Text CSV Requirements & Schema",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = textSlateColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isStudentSchemaMode) {
                                "Your student database CSV dataset should contain these column headers on the first line (Roll, Name, Phone are mandatory):"
                            } else {
                                "Your CSV dataset MUST contain these exact case-sensitive column headers on the first line:"
                            },
                            fontSize = 11.sp,
                            color = textSlateMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isStudentSchemaMode) {
                                    "Roll, Name, Phone, Parent/guardian Number, COLLEGE, Course, Course Session, Father, Dob"
                                } else {
                                    "account_number, student_name, primary_phone, original_due_date, total_due_amount"
                                },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFE2E8F0),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Template Assistant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = textSlateColor
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1D2D44))
                                    .clickable {
                                        uploadCsvContent = if (isStudentSchemaMode) sampleStudentTemplate else sampleRecoveryTemplate
                                        importedFileName = "template_example.csv"
                                        Toast.makeText(context, "Loaded sample CSV data into payload field!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Load Sample Template Data",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryBlue
                                )
                            }
                        }
                    }
                }

                // Paste and Upload Form
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "2. Data Ingestion Form",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = textSlateColor
                        )

                        // Real CSV Upload trigger
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    csvFilePickerLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "application/csv"))
                                }
                                .testTag("upload_csv_picker_trigger")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Upload CSV",
                                    tint = primaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (importedFileName != null) "CSV Linked: $importedFileName" else "UPLOAD CSV DOCUMENT DIRECTLY",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (importedFileName != null) primaryBlue else textSlateColor
                                )
                            }
                        }

                        // College Name Field
                        OutlinedTextField(
                            value = uploadCollegeName,
                            onValueChange = { uploadCollegeName = it },
                            placeholder = { Text("e.g. Stanford University School of Law", fontSize = 12.sp) },
                            label = { Text("Fallback University / Institution Name", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_screen_college_name_field"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = cardBorderColor,
                                focusedTextColor = textSlateColor,
                                unfocusedTextColor = textSlateColor
                            ),
                            singleLine = true
                        )

                        // CSV Paste Text Area
                        OutlinedTextField(
                            value = uploadCsvContent,
                            onValueChange = { uploadCsvContent = it },
                            placeholder = {
                                Text(
                                    if (isStudentSchemaMode) {
                                        "Paste Student CSV here:\nRoll001,Amit Sharma,9876543201,,Oxford College,,,"
                                    } else {
                                        "Paste your CSV here. For example:\nS101,Aarav Kumar,9876543210,2025-05-10,45000"
                                    },
                                    fontSize = 11.sp,
                                    color = textSlateMuted
                                )
                            },
                            label = { Text("Pasted CSV Plain Text Payloads", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .testTag("upload_screen_csv_payload_field"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = cardBorderColor,
                                focusedTextColor = textSlateColor,
                                unfocusedTextColor = textSlateColor
                            )
                        )

                        // Result Area if available
                        if (uploadResultMessage != null) {
                            val isError = uploadResultMessage!!.startsWith("Error")
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isError) Color(0xFFDC2626).copy(alpha = 0.15f) else Color(0xFF059669).copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (isError) Color(0xFFDC2626) else Color(0xFF059669), RoundedCornerShape(8.dp))
                                    .testTag("upload_screen_result_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isError) Color(0xFFFCA5A5) else Color(0xFF34D399),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = uploadResultMessage!!,
                                        color = if (isError) Color(0xFFFCA5A5) else Color(0xFF34D399),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Process Data CTA
                        Button(
                            onClick = {
                                val emptyNameOkOnStudentModeWithHeaders = isStudentSchemaMode && uploadCsvContent.isNotBlank()
                                if ((uploadCollegeName.isBlank() && !emptyNameOkOnStudentModeWithHeaders) || uploadCsvContent.isBlank()) {
                                    uploadResultMessage = "Error: Fallback Institution Name and CSV content must not be blank."
                                } else {
                                    isProcessing = true
                                    uploadResultMessage = null
                                    homeViewModel.importStudentDataCollegeWise(
                                        csvContent = uploadCsvContent,
                                        collegeName = uploadCollegeName.ifBlank { "Unassigned" },
                                        isStudentDbSchema = isStudentSchemaMode
                                    ) { count, err ->
                                        isProcessing = false
                                        if (count > 0) {
                                            uploadResultMessage = "Success: Successfully imported $count accounts!"
                                            uploadCsvContent = ""
                                            uploadCollegeName = ""
                                            importedFileName = null
                                            Toast.makeText(context, "Data Synchronization Successful", Toast.LENGTH_SHORT).show()
                                        } else {
                                            uploadResultMessage = "Error: ${err ?: "Parsing payload failed due to invalid dimensions."}"
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("upload_screen_submit_btn"),
                            enabled = !isProcessing
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Process & Ingest Database", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
