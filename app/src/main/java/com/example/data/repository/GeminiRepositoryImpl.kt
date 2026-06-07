package com.example.data.repository

import com.example.domain.repository.GeminiRepository
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepositoryImpl @Inject constructor() : GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private suspend fun callGeminiApi(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key is unconfigured. Please configure GEMINI_API_KEY in the secrets panel."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }
            put("contents", contentArray)
        }

        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error: Gemini server returned response code ${response.code}\n${response.body?.string() ?: ""}"
                }
                val responseStr = response.body?.string() ?: return@withContext "Error: Direct empty response body from Gemini."
                val jsonObject = JSONObject(responseStr)
                val candidates = jsonObject.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val contentObj = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No text in Gemini candidate.")
                    }
                }
                "Error: Unexpected JSON response structure from Gemini: $responseStr"
            }
        } catch (e: Exception) {
            "Error: Call failed with exception: ${e.localizedMessage}"
        }
    }

    override suspend fun generateNegotiationScript(
        studentName: String,
        amount: Double,
        college: String,
        segment: String,
        previousNotes: String
    ): String {
        val prompt = """
            You are an expert recovery counselor and communications trainer for Telecaller Pro, a student fees recovery system.
            Generate a personalized, polite, cooperative, but highly persuasive phone negotiation script for a collection agent.
            
            Debtor Profile:
            - Student Name: $studentName
            - Outstanding Fee Amount: ₹$amount
            - College/Department: $college
            - Customer/Segment Category: $segment
            - Previous Communication / Audit Notes: ${if (previousNotes.isBlank()) "No prior interactions recorded yet." else previousNotes}
            
            Guidelines:
            1. Maintain an extremely polite, dignified, empathetic but professional tone. Do not intimidate, coerce, or harass.
            2. Present structural options like split installment payments, digital pay links, parent-teacher-management meetings, or scholarship review.
            3. Anticipate common reasons for student loan or academic fee defaults (temporary banking issues, awaiting sponsors/family, scholarship delay).
            4. Keep the script concise (under 250 words) with clear speaking cues: opening greeting, presenting options, resolving pushback, and a firm confirmation call to action.
            
            Structure the script with bold Markdown headers. Use simple bullet points for speaking options.
        """.trimIndent()

        return callGeminiApi(prompt)
    }

    override suspend fun optimizeCallNotes(rawNotes: String): String {
        if (rawNotes.isBlank()) return "No raw notes provided to optimize."
        
        val prompt = """
            You are an advanced Call Audit AI for a recoveries portal.
            Take the following raw, short, messy notes typed by a busy telecaller agent, and optimize them into a highly professional recovery report:
            
            Raw notes from agent:
            "$rawNotes"
            
            Requirements:
            1. Clean up typing errors, abbreviations, slang, and grammar.
            2. Extract and format key facts:
               - Commitment Level / Sentiment (Cooperative / Avoidant / Disputing / Confused)
               - Agreed Date (if standard promise is found)
               - Root cause of delay (academic dispute, financial bottleneck, parent discussion, etc.)
            3. Suggest a concise next-step action plan for the team.
            
            Provide the output in a neat, well-structured, bulleted layout. Under 150 words.
        """.trimIndent()

        return callGeminiApi(prompt)
    }

    override suspend fun generatePortfolioExecutiveAnalysis(
        debtorsCount: Int,
        totalOutstanding: Double,
        activePtpCount: Int,
        recentLogsSummary: String
    ): String {
        val prompt = """
            You are a Principal AI Analytics Auditor looking at a Telecaller Agency recovery dashboard.
            Perform a strategic assessment and formulate an executive portfolio report based on the following real-time telemetry metrics:
            
            Portfolio Metrics:
            - Active Debtor Students: $debtorsCount
            - Total Overdue Fee Portfolio: ₹$totalOutstanding
            - Active Promises to Pay (PTPs): $activePtpCount commitments awaiting fulfillment
            - Real-time Log Dynamics Overview: $recentLogsSummary
            
            Your Report MUST include:
            1. Executive summary of portfolio health (Healthy / Critical Risk / Stagnant).
            2. Underlying trends shown by the logs (e.g. main friction points, standard student reasons for non-payment).
            3. Direct operational tactical advice for the telecaller team to optimize daily recovery target yield (e.g., dial timings, segment priorities, script focus).
            
            Format clearly in clean Markdown. Keep it engaging, direct, and actionable!
        """.trimIndent()

        return callGeminiApi(prompt)
    }
}
