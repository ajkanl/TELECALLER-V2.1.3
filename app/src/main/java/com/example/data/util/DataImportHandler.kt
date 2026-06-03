package com.example.data.util

import android.util.Log
import com.example.data.local.dao.DebtorDao
import com.example.data.local.entity.DebtorEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.tasks.await
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Data classification structures representing import results and logs.
 */
data class DebtorImportRecord(
    val accountNumber: String,
    val studentName: String,
    val primaryPhone: String,
    val alternativePhone: String?,
    val totalDueAmount: Double,
    val originalDueDate: String,
    val computedDpdBucket: String,
    val daysPastDue: Long
)

data class ImportResult(
    val totalRowsProcessed: Int,
    val successfullyImported: List<DebtorImportRecord>,
    val errorLogs: List<String>
)

data class FirestoreBatchResult(
    val totalBatchesCreated: Int,
    val totalRecordsWritten: Int,
    val didSucceed: Boolean,
    val exceptionMessage: String? = null
)

/**
 * Robust handler logic to parse, validate, bucket-classify, and upload debtor outstanding accounts in batches.
 */
class DataImportHandler(
    private val debtorDao: DebtorDao? = null
) {
    private val tag = "DataImportHandler"

    // Supported headers aliases for dynamic index configuration checking
    private val accountNumberAliases = listOf("account_number", "account number", "accountno", "account_no", "id", "loan_account")
    private val studentNameAliases = listOf("student_name", "student name", "name", "debtor_name", "student")
    private val primaryPhoneAliases = listOf("primary_phone", "primary phone", "phone", "contact", "primary_contact", "mobile")
    private val alternativePhoneAliases = listOf("alternative_phone", "alternative phone", "alt_phone", "alternative_contact", "alt_contact")
    private val totalDueAmountAliases = listOf("total_due_amount", "total due amount", "amount_due", "total_due", "overdue_amount", "balance")
    private val originalDueDateAliases = listOf("original_due_date", "original due date", "due_date", "original_due", "date_due")

    /**
     * Parses an InputStream containing raw CSV records.
     */
    fun parseAndValidateCsv(inputStream: InputStream): ImportResult {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val rawLines = mutableListOf<String>()
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                line?.let { rawLines.add(it) }
            }
        } catch (e: Exception) {
            return ImportResult(0, emptyList(), listOf("Failed to read CSV stream: ${e.message}"))
        } finally {
            inputStream.close()
        }
        return parseAndValidateLines(rawLines)
    }

    /**
     * Parses a raw CSV String multi-line payload.
     */
    fun parseAndValidateCsvText(csvContent: String): ImportResult {
        val rawLines = csvContent.split("\r?\n".toRegex()).filter { it.isNotBlank() }
        return parseAndValidateLines(rawLines)
    }

    /**
     * Core line parsing, header matching, and data validation runner.
     */
    private fun parseAndValidateLines(lines: List<String>): ImportResult {
        if (lines.isEmpty()) {
            return ImportResult(0, emptyList(), listOf("The provided CSV file contains no content."))
        }

        val headerLine = lines[0]
        val headers = parseCsvLine(headerLine)

        // Find Dynamic Indexes for flexible layout compatibility
        val accountNumIdx = headers.indexOfFirst { h -> accountNumberAliases.any { it.equals(h, ignoreCase = true) } }
        val studentNameIdx = headers.indexOfFirst { h -> studentNameAliases.any { it.equals(h, ignoreCase = true) } }
        val primaryPhoneIdx = headers.indexOfFirst { h -> primaryPhoneAliases.any { it.equals(h, ignoreCase = true) } }
        val altPhoneIdx = headers.indexOfFirst { h -> alternativePhoneAliases.any { it.equals(h, ignoreCase = true) } }
        val totalDueIdx = headers.indexOfFirst { h -> totalDueAmountAliases.any { it.equals(h, ignoreCase = true) } }
        val originalDueIdx = headers.indexOfFirst { h -> originalDueDateAliases.any { it.equals(h, ignoreCase = true) } }

        // Sanity check mandatory headers
        val missingHeaders = mutableListOf<String>()
        if (accountNumIdx == -1) missingHeaders.add("account_number")
        if (studentNameIdx == -1) missingHeaders.add("student_name")
        if (primaryPhoneIdx == -1) missingHeaders.add("primary_phone")
        if (totalDueIdx == -1) missingHeaders.add("total_due_amount")
        if (originalDueIdx == -1) missingHeaders.add("original_due_date")

        if (missingHeaders.isNotEmpty()) {
            return ImportResult(
                totalRowsProcessed = 0,
                successfullyImported = emptyList(),
                errorLogs = listOf("Error: Missing critical mapping header(s): ${missingHeaders.joinToString(", ")}. Current headers found: $headers")
            )
        }

        val successRecords = mutableListOf<DebtorImportRecord>()
        val errorsList = mutableListOf<String>()

        for (i in 1 until lines.size) {
            val rawRowLine = lines[i]
            if (rawRowLine.isBlank()) continue

            val row = parseCsvLine(rawRowLine)
            val lineNum = i + 1

            // Boundary safeguard check
            if (row.size <= maxOf(accountNumIdx, studentNameIdx, primaryPhoneIdx, totalDueIdx, originalDueIdx)) {
                errorsList.add("Row $lineNum: Contains incomplete column fields. Values: $row")
                continue
            }

            val rawAccountNumber = row[accountNumIdx].trim()
            val rawStudentName = row[studentNameIdx].trim()
            val rawPrimaryPhone = row[primaryPhoneIdx].trim()
            val rawAltPhone = if (altPhoneIdx != -1 && altPhoneIdx < row.size) row[altPhoneIdx].trim() else null
            val rawTotalDue = row[totalDueIdx].trim()
            val rawOriginalDueDate = row[originalDueIdx].trim()

            // 1. Mandatory Fields Null or Blank validation
            if (rawAccountNumber.isEmpty()) {
                errorsList.add("Row $lineNum: Account number field sits empty.")
                continue
            }
            if (rawStudentName.isEmpty()) {
                errorsList.add("Row $lineNum: Account '$rawAccountNumber' is missing a student name.")
                continue
            }

            // 2. Normalize and check Primary Phone (Require 10-digit validity)
            val normalizedPrimaryPhone = normalizePhoneNumber(rawPrimaryPhone)
            if (normalizedPrimaryPhone == null) {
                errorsList.add("Row $lineNum: Account '$rawAccountNumber' features invalid primary phone '$rawPrimaryPhone'. Must parse to clean 10-digit number.")
                continue
            }

            // Safe normalize alternative phone (optional field)
            val normalizedAltPhone = if (!rawAltPhone.isNullOrBlank()) normalizePhoneNumber(rawAltPhone) else null

            // 3. Float Parsing Total Due Amount
            val parsedTotalDue = rawTotalDue.replace("[$,]".toRegex(), "").toDoubleOrNull()
            if (parsedTotalDue == null || parsedTotalDue < 0) {
                errorsList.add("Row $lineNum: Account '$rawAccountNumber' contains invalid due amount '$rawTotalDue'.")
                continue
            }

            // 4. Calculate Days Past Due and configure corresponding bucket
            val dpdResult = calculateDpdBucketAndDays(rawOriginalDueDate)
            if (dpdResult == null) {
                errorsList.add("Row $lineNum: Account '$rawAccountNumber' includes unrecognized date format '$rawOriginalDueDate'. Supported formats (yyyy-MM-dd, dd-MM-yyyy, dd/MM/yyyy).")
                continue
            }

            val (computedBucket, daysPastDue) = dpdResult

            successRecords.add(
                DebtorImportRecord(
                    accountNumber = rawAccountNumber,
                    studentName = rawStudentName,
                    primaryPhone = normalizedPrimaryPhone,
                    alternativePhone = normalizedAltPhone,
                    totalDueAmount = parsedTotalDue,
                    originalDueDate = rawOriginalDueDate,
                    computedDpdBucket = computedBucket,
                    daysPastDue = daysPastDue
                )
            )
        }

        return ImportResult(
            totalRowsProcessed = lines.size - 1,
            successfullyImported = successRecords,
            errorLogs = errorsList
        )
    }

    /**
     * Parse single line respecting quotes and embedded commas.
     */
    fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var curVal = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (inQuotes) {
                if (ch == '\"') {
                    if (i + 1 < line.length && line[i + 1] == '\"') {
                        curVal.append('\"') // Escaped quote representation
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    curVal.append(ch)
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true
                } else if (ch == ',') {
                    result.add(curVal.toString().trim())
                    curVal = StringBuilder()
                } else {
                    curVal.append(ch)
                }
            }
            i++
        }
        result.add(curVal.toString().trim())
        return result
    }

    /**
     * Cleans up country-code prefixes (+91, 91, 0), spaces, and symbols, verifying 10 numeric digits.
     */
    fun normalizePhoneNumber(rawPhone: String): String? {
        // Strip out common formatting non-digits
        var cleaned = rawPhone.replace("[^0-9]".toRegex(), "")

        // Normalize leading prefixes to get exactly 10 digits
        if (cleaned.length == 12 && cleaned.startsWith("91")) {
            cleaned = cleaned.substring(2)
        } else if (cleaned.length == 11 && cleaned.startsWith("0")) {
            cleaned = cleaned.substring(1)
        }

        // Return validated 10-digit format
        return if (cleaned.length == 10) cleaned else null
    }

    /**
     * Computes Days Past Due (DPD) by comparing System Date with Original Due Date.
     */
    fun calculateDpdBucketAndDays(dueDateStr: String): Pair<String, Long>? {
        val supportedFormatPatterns = listOf(
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "yyyy-MM-dd HH:mm:ss"
        )
        var parsedDate: Date? = null

        for (pattern in supportedFormatPatterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.isLenient = false
                parsedDate = sdf.parse(dueDateStr.trim())
                if (parsedDate != null) {
                    break
                }
            } catch (e: Exception) {
                // Try next pattern matching structures
            }
        }

        if (parsedDate == null) return null

        val currentSystemTime = System.currentTimeMillis()
        val dueTime = parsedDate.time
        val diffMillis = currentSystemTime - dueTime
        val daysBetween = diffMillis / (24 * 60 * 60 * 1000L)

        // Classify into exact application-standard DPD buckets plus descriptive text
        val bucketStr = when {
            daysBetween <= 0 -> "0 DPD"
            daysBetween in 1..30 -> "1-30 DPD"
            daysBetween in 31..60 -> "31-60 DPD"
            daysBetween in 61..90 -> "60-90 DPD"
            else -> "90+ DPD"
        }

        return Pair(bucketStr, daysBetween)
    }

    /**
     * Firebase Firestore batch write integration saving up to 500 documents per transaction block.
     */
    suspend fun uploadToFirestoreBatched(records: List<DebtorImportRecord>): FirestoreBatchResult {
        if (records.isEmpty()) {
            return FirestoreBatchResult(0, 0, true)
        }

        try {
            val db = FirebaseFirestore.getInstance()
            val collectionRef = db.collection("debtors")
            val batchChunks = records.chunked(500)
            var batchCount = 0
            var recordsWritten = 0

            for (chunk in batchChunks) {
                val writeBatch = db.batch()
                for (record in chunk) {
                    val docRef = collectionRef.document(record.accountNumber)
                    val dataMap = mapOf(
                        "id" to record.accountNumber,
                        "name" to record.studentName,
                        "phoneNumber" to record.primaryPhone,
                        "alternativeNumber" to record.alternativePhone,
                        "totalOverdueAmount" to record.totalDueAmount,
                        "originalDueDate" to record.originalDueDate,
                        "dpdBucket" to record.computedDpdBucket.removeSuffix(" DPD"), // standardizing internal DB storage
                        "daysPastDue" to record.daysPastDue,
                        "currentStatus" to "PENDING",
                        "allocationDate" to System.currentTimeMillis()
                    )
                    writeBatch.set(docRef, dataMap)
                }

                writeBatch.commit().await()
                batchCount++
                recordsWritten += chunk.size
                Log.d(tag, "Successfully wrote Firestore batch progress count #$batchCount with ${chunk.size} docs.")
            }

            return FirestoreBatchResult(
                totalBatchesCreated = batchCount,
                totalRecordsWritten = recordsWritten,
                didSucceed = true
            )

        } catch (e: Exception) {
            Log.e(tag, "Failure executing Firestore batched uploads: ${e.message}", e)
            return FirestoreBatchResult(
                totalBatchesCreated = 0,
                totalRecordsWritten = 0,
                didSucceed = false,
                exceptionMessage = e.message
            )
        }
    }

    /**
     * Sync import records locally to Room database for immediate offline usage.
     */
    suspend fun saveToLocalDatabase(records: List<DebtorImportRecord>, collegeName: String = "Default College"): Int {
        if (debtorDao == null || records.isEmpty()) return 0

        val mappedEntities = records.map {
            DebtorEntity(
                id = it.accountNumber,
                name = it.studentName,
                phoneNumber = "+91" + it.primaryPhone, // Prefixing format
                alternativeNumber = it.alternativePhone?.let { alt -> "+91" + alt },
                totalOverdueAmount = it.totalDueAmount,
                principalAmount = it.totalDueAmount * 0.9, // reasonable default principal estimation
                dpdBucket = it.computedDpdBucket.removeSuffix(" DPD"),
                allocationDate = System.currentTimeMillis(),
                currentStatus = "PENDING",
                contactNumber = "+91" + it.primaryPhone,
                address = "India",
                outstandingAmount = it.totalDueAmount,
                lastContactDate = "Never",
                customerSegment = if (it.totalDueAmount >= 100000.0) "High Value" else "Standard",
                college = collegeName
            )
        }

        debtorDao.insertDebtors(mappedEntities)
        return mappedEntities.size
    }
}
