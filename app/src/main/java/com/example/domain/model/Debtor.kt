package com.example.domain.model

data class Debtor(
    val id: String,
    val name: String,
    val overdueDays: Int,
    val outstandingAmount: Double,
    val customerSegment: String, // e.g., "High Value", "Standard"
    val phoneNumber: String, // contact number
    val address: String,
    val lastContactDate: String,
    val college: String = "Default College",
    val remarks: String = "",
    val father: String = "",
    val dob: String = "",
    val course: String = "",
    val courseSession: String = "",
    val guardianNumber: String = ""
)

data class CallRecord(
    val id: String,
    val debtorId: String = "",
    val debtorName: String,
    val status: String, // e.g., "Not Picked Up", "PTP Promised", "Completed"
    val time: String,
    val simCard: String, // "SIM 1", "SIM 2"
    val date: String = "",
    val notes: String = "",
    val callType: String = "OUTBOUND", // "OUTBOUND" or "INBOUND"
    val category: String = "Business"  // "Business" or "Personal"
)
