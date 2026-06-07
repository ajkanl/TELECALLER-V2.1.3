package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface GeminiRepository {
    /**
     * Generates a persuasive negotiation and recovery script tailored to a specific debtor's profile.
     */
    suspend fun generateNegotiationScript(
        studentName: String,
        amount: Double,
        college: String,
        segment: String,
        previousNotes: String
    ): String

    /**
     * Format rough notes into clean, professional call notes and determine sentiment.
     */
    suspend fun optimizeCallNotes(rawNotes: String): String

    /**
     * Perform portfolio-wide analysis to identify trend metrics, reasons for defaults, and recommendations.
     */
    suspend fun generatePortfolioExecutiveAnalysis(
        debtorsCount: Int,
        totalOutstanding: Double,
        activePtpCount: Int,
        recentLogsSummary: String
    ): String
}
