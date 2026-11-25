package com.loupeatelier.app.engine

import android.content.Context
import com.loupeatelier.app.model.FinalScorecard
import com.loupeatelier.app.model.MarkerAssessment
import com.loupeatelier.app.model.MarkerSpec
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavedInspectionEntry(
    val id: String,
    val categoryName: String,
    val itemName: String,
    val probabilityScore: Int,
    val confidenceScore: Int,
    val verdictBand: String,
    val savedAtLabel: String,
    val reportText: String
)

object InspectionStore {
    private const val FILE_NAME = "loupe_atelier_inspections.json"

    fun loadAll(context: Context): List<SavedInspectionEntry> {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return emptyList()
            val arr = JSONArray(file.readText())
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    add(
                        SavedInspectionEntry(
                            id = o.optString("id"),
                            categoryName = o.optString("categoryName"),
                            itemName = o.optString("itemName"),
                            probabilityScore = o.optInt("probabilityScore"),
                            confidenceScore = o.optInt("confidenceScore"),
                            verdictBand = o.optString("verdictBand"),
                            savedAtLabel = o.optString("savedAtLabel"),
                            reportText = o.optString("reportText")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveEntry(context: Context, entry: SavedInspectionEntry): List<SavedInspectionEntry> {
        val current = loadAll(context).filterNot { it.id == entry.id }
        val updated = (listOf(entry) + current).take(25)
        writeEntries(context, updated)
        return updated
    }

    fun deleteEntry(context: Context, id: String): List<SavedInspectionEntry> {
        val updated = loadAll(context).filterNot { it.id == id }
        writeEntries(context, updated)
        return updated
    }

    private fun writeEntries(context: Context, entries: List<SavedInspectionEntry>) {
        try {
            val arr = JSONArray()
            entries.forEach { e ->
                arr.put(JSONObject().apply {
                    put("id", e.id)
                    put("categoryName", e.categoryName)
                    put("itemName", e.itemName)
                    put("probabilityScore", e.probabilityScore)
                    put("confidenceScore", e.confidenceScore)
                    put("verdictBand", e.verdictBand)
                    put("savedAtLabel", e.savedAtLabel)
                    put("reportText", e.reportText)
                })
            }
            File(context.filesDir, FILE_NAME).writeText(arr.toString())
        } catch (_: Exception) {
            // Ignore write errors
        }
    }

    fun formatShareReport(
        protocol: CategoryProtocol,
        itemName: String,
        markers: List<MarkerSpec>,
        assessments: Map<String, MarkerAssessment>,
        scorecard: FinalScorecard
    ): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        return buildString {
            appendLine("LoupeAtelier — ITEM PRE-SCREENING RESULT")
            appendLine("Item: $itemName (${protocol.name})")
            appendLine("Checked: $timestamp")
            appendLine("------------------------------------------------")
            appendLine("Visual Score:    ${scorecard.overallProbabilityScore}/100")
            appendLine("Photo Clarity:   ${scorecard.overallConfidenceScore}/100")
            appendLine("Summary Rating:  ${scorecard.verdictBand}")
            appendLine()
            appendLine(scorecard.executiveSummary)
            appendLine()
            appendLine("PHOTO-BY-PHOTO BREAKDOWN:")
            markers.forEach { m ->
                val a = assessments[m.id]
                if (a != null) {
                    appendLine(
                        "Photo 0${m.stepNumber}. ${m.title} -> " +
                            "Visual ${a.authenticityScore}/100 | Clarity ${a.confidenceScore}/100 | " +
                            "Framing ${a.framingScore}% / Light ${a.lightingScore}% / Focus ${a.sharpnessScore}%"
                    )
                    a.confirmations.forEach { appendLine("   + Looks Good: $it") }
                    a.anomalies.forEach { appendLine("   - Watch Out: $it") }
                    if (!a.qualityPassed && a.retakeAngleInstruction != null) {
                        appendLine("   ! Retake Tip: ${a.retakeAngleInstruction}")
                    }
                }
            }
            appendLine()
            appendLine("HANDS-ON CHECKS:")
            scorecard.prioritizedHumanChecks.forEachIndexed { idx, c ->
                appendLine("${idx + 1}. $c")
            }
            appendLine()
            appendLine(scorecard.disclaimer)
        }
    }
}
