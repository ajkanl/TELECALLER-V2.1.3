package com.example

import com.example.data.util.DataImportHandler
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DataImportHandlerTest {

    private val importHandler = DataImportHandler(debtorDao = null)

    @Test
    fun csvLineParser_correctlyHandlesCommasInQuotes() {
        val line = "10243,\"Verma, Aarav\",9876543210,9111222333,45000.0,2026-04-15"
        val parsed = importHandler.parseCsvLine(line)

        assertEquals(6, parsed.size)
        assertEquals("10243", parsed[0])
        assertEquals("Verma, Aarav", parsed[1]) // Outer quotes should be removed and inner comma preserved
        assertEquals("9876543210", parsed[2])
        assertEquals("9111222333", parsed[3])
        assertEquals("45000.0", parsed[4])
        assertEquals("2026-04-15", parsed[5])
    }

    @Test
    fun phoneNumberNormalization_cleansAllWhitespacesAndCountryPrefixes() {
        // Indian country code with + and spaces
        assertEquals("9876543210", importHandler.normalizePhoneNumber("+91 98765 43210"))
        // Indian country code with dash
        assertEquals("9876543210", importHandler.normalizePhoneNumber("91-98765-43210"))
        // Standard leading zero prefix
        assertEquals("9876543210", importHandler.normalizePhoneNumber("09876543210"))
        // Already clean 10-digits
        assertEquals("9876543210", importHandler.normalizePhoneNumber("9876543210"))
        // Short/invalid returns null
        assertNull(importHandler.normalizePhoneNumber("12345"))
        // String letters returns null
        assertNull(importHandler.normalizePhoneNumber("abcde12345"))
    }

    @Test
    fun dpdCalculation_bucketsByDatePerfect() {
        val current = LocalDate.now()
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // 15 days ago -> 1-30 DPD
        val date15 = current.minusDays(15).format(format)
        val result15 = importHandler.calculateDpdBucketAndDays(date15)
        assertNotNull(result15)
        assertEquals("1-30 DPD", result15!!.first)
        assertEquals(15L, result15.second)

        // 45 days ago -> 31-60 DPD
        val date45 = current.minusDays(45).format(format)
        val result45 = importHandler.calculateDpdBucketAndDays(date45)
        assertNotNull(result45)
        assertEquals("31-60 DPD", result45!!.first)
        assertEquals(45L, result45.second)

        // 75 days ago -> 60-90 DPD
        val date75 = current.minusDays(75).format(format)
        val result75 = importHandler.calculateDpdBucketAndDays(date75)
        assertNotNull(result75)
        assertEquals("60-90 DPD", result75!!.first)
        assertEquals(75L, result75.second)

        // 120 days ago -> 90+ DPD
        val date120 = current.minusDays(120).format(format)
        val result120 = importHandler.calculateDpdBucketAndDays(date120)
        assertNotNull(result120)
        assertEquals("90+ DPD", result120!!.first)
        assertEquals(120L, result120.second)

        // Future/current date -> 0 DPD
        val dateFuture = current.format(format)
        val resultFuture = importHandler.calculateDpdBucketAndDays(dateFuture)
        assertNotNull(resultFuture)
        assertEquals("0 DPD", resultFuture!!.first)
        assertEquals(0L, resultFuture.second)
    }

    @Test
    fun importCsvText_successfullyParsesValidRowsAndLogsErrorsForInvalids() {
        val current = LocalDate.now()
        val date35 = current.minusDays(35).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val date95 = current.minusDays(95).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val csvContent = """
            account_number,student_name,primary_phone,alternative_phone,total_due_amount,original_due_date
            ACCT101,Rohan Sharma,+91 9988776655,09911223344,15000.00,$date35
            ACCT102,"Kapoor, Aditi",9876543210,,8500.50,$date95
            ACCT103,Broken Phone,12345,,12000,$date35
            ,No Account,9876543210,,1500,$date35
        """.trimIndent()

        val result = importHandler.parseAndValidateCsvText(csvContent)

        // Expected results:
        // ACCT101 -> Valid. DPD: 35 days, bucket "31-60 DPD"
        // ACCT102 -> Valid. DPD: 95 days, bucket "90+ DPD"
        // ACCT103 -> Invalid phone 12345 (Dropped, logged with error)
        // Row 4 -> Missing account (Dropped, logged with error)

        assertEquals(4, result.totalRowsProcessed)
        assertEquals(2, result.successfullyImported.size)
        assertEquals(2, result.errorLogs.size)

        val record1 = result.successfullyImported[0]
        assertEquals("ACCT101", record1.accountNumber)
        assertEquals("Rohan Sharma", record1.studentName)
        assertEquals("9988776655", record1.primaryPhone)
        assertEquals("9911223344", record1.alternativePhone)
        assertEquals(15000.00, record1.totalDueAmount, 0.001)
        assertEquals("31-60 DPD", record1.computedDpdBucket)

        val record2 = result.successfullyImported[1]
        assertEquals("ACCT102", record2.accountNumber)
        assertEquals("Kapoor, Aditi", record2.studentName)
        assertEquals("9876543210", record2.primaryPhone)
        assertNull(record2.alternativePhone)
        assertEquals(8500.50, record2.totalDueAmount, 0.001)
        assertEquals("90+ DPD", record2.computedDpdBucket)

        assertTrue(result.errorLogs.any { it.contains("invalid primary phone") })
        assertTrue(result.errorLogs.any { it.contains("Account number field sits empty") })
    }

    @Test
    fun importCsvText_withShuffledHeaders_resolvesProperly() {
        val current = LocalDate.now()
        val date10 = current.minusDays(10).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        val csvContent = """
            student_name,original_due_date,total_due_amount,primary_phone,account_number
            "Patel, Dev",$date10,3200,9988776655,ACCT999
        """.trimIndent()

        val result = importHandler.parseAndValidateCsvText(csvContent)

        assertEquals(1, result.totalRowsProcessed)
        assertEquals(1, result.successfullyImported.size)
        assertTrue(result.errorLogs.isEmpty())

        val record = result.successfullyImported[0]
        assertEquals("ACCT999", record.accountNumber)
        assertEquals("Patel, Dev", record.studentName)
        assertEquals("9988776655", record.primaryPhone)
        assertEquals(3200.0, record.totalDueAmount, 0.001)
        assertEquals("1-30 DPD", record.computedDpdBucket)
    }
}
