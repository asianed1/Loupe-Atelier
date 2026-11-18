package com.loupeatelier.app.engine

import android.content.Context
import com.loupeatelier.app.model.BoundingBoxSpec
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
    private const val CUSTOM_PROTOCOLS_FILE = "loupe_custom_protocols.json"
    private const val PREFS_NAME = "loupe_atelier_prefs"
    private const val KEY_SERVER_URL = "augmentation_server_url"
    const val DEFAULT_SERVER_URL = "https://ais-pre-5foxma5n2sc2kuorqd22uj-174556293157.asia-east1.run.app"

    fun getServerUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL)?.trim()?.ifBlank { DEFAULT_SERVER_URL }
            ?: DEFAULT_SERVER_URL
    }

    fun saveServerUrl(context: Context, url: String) {
        val clean = url.trim().trimEnd('/')
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SERVER_URL, clean.ifBlank { DEFAULT_SERVER_URL })
            .apply()
    }

    fun loadCustomProtocols(context: Context): List<CategoryProtocol> {
        return try {
            val file = File(context.filesDir, CUSTOM_PROTOCOLS_FILE)
            if (!file.exists()) return emptyList()
            val arr = JSONArray(file.readText())
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val markersArr = o.optJSONArray("markers") ?: continue
                    val markers = buildList {
                        for (j in 0 until markersArr.length()) {
                            val m = markersArr.optJSONObject(j) ?: continue
                            val bb = m.optJSONObject("boundingBox")
                            add(
                                MarkerSpec(
                                    id = m.optString("id"),
                                    stepNumber = m.optInt("stepNumber", j + 1),
                                    title = m.optString("title"),
                                    focusFeature = m.optString("focusFeature"),
                                    weight = m.optDouble("weight", 0.25),
                                    lightingRequirement = m.optString("lightingRequirement"),
                                    angleAndDistance = m.optString("angleAndDistance"),
                                    targetConfirmations = m.optJSONArray("targetConfirmations").toStringList(),
                                    targetAnomalies = m.optJSONArray("targetAnomalies").toStringList(),
                                    boundingBox = BoundingBoxSpec(
                                        aspectRatioLabel = bb?.optString("aspectRatioLabel") ?: "4:3 Calibrated Macro",
                                        aspectRatioValue = (bb?.optDouble("aspectRatioValue", 4.0 / 3.0) ?: (4.0 / 3.0)).toFloat(),
                                        targetFillMinPercent = bb?.optInt("targetFillMinPercent", 60) ?: 60,
                                        targetFillMaxPercent = bb?.optInt("targetFillMaxPercent", 85) ?: 85,
                                        alignmentRule = bb?.optString("alignmentRule") ?: "Center target inside 4:3 guide.",
                                        reticleType = bb?.optString("reticleType") ?: "general-macro"
                                    )
                                )
                            )
                        }
                    }
                    if (markers.isNotEmpty()) {
                        add(
                            CategoryProtocol(
                                id = o.optString("id"),
                                name = o.optString("name"),
                                defaultItemName = o.optString("defaultItemName"),
                                markers = markers,
                                humanChecks = o.optJSONArray("humanChecks").toStringList(),
                                isCustomCached = true
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCustomProtocol(context: Context, protocol: CategoryProtocol): List<CategoryProtocol> {
        val current = loadCustomProtocols(context).filterNot { it.id == protocol.id || it.defaultItemName.equals(protocol.defaultItemName, ignoreCase = true) }
        val updated = (listOf(protocol.copy(isCustomCached = true)) + current).take(20)
        try {
            val arr = JSONArray()
            updated.forEach { p ->
                arr.put(JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("defaultItemName", p.defaultItemName)
                    put("humanChecks", JSONArray(p.humanChecks))
                    put("markers", JSONArray().apply {
                        p.markers.forEach { m ->
                            put(JSONObject().apply {
                                put("id", m.id)
                                put("stepNumber", m.stepNumber)
                                put("title", m.title)
                                put("focusFeature", m.focusFeature)
                                put("weight", m.weight)
                                put("lightingRequirement", m.lightingRequirement)
                                put("angleAndDistance", m.angleAndDistance)
                                put("targetConfirmations", JSONArray(m.targetConfirmations))
                                put("targetAnomalies", JSONArray(m.targetAnomalies))
                                put("boundingBox", JSONObject().apply {
                                    put("aspectRatioLabel", m.boundingBox.aspectRatioLabel)
                                    put("aspectRatioValue", m.boundingBox.aspectRatioValue.toDouble())
                                    put("targetFillMinPercent", m.boundingBox.targetFillMinPercent)
                                    put("targetFillMaxPercent", m.boundingBox.targetFillMaxPercent)
                                    put("alignmentRule", m.boundingBox.alignmentRule)
                                    put("reticleType", m.boundingBox.reticleType)
                                })
                            })
                        }
                    })
                })
            }
            File(context.filesDir, CUSTOM_PROTOCOLS_FILE).writeText(arr.toString())
        } catch (_: Exception) {
            // Best-effort offline cache
        }
        return updated
    }

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
            appendLine("LOUPE ATELIER — MACRO PRE-SCREENING SCORECARD")
            appendLine("Item: $itemName (${protocol.name})")
            appendLine("Assessed: $timestamp")
            appendLine("------------------------------------------------")
            appendLine("Authenticity Probability: ${scorecard.overallProbabilityScore}/100")
            appendLine("Visual Confidence:        ${scorecard.overallConfidenceScore}/100")
            appendLine("Verdict Band:             ${scorecard.verdictBand}")
            appendLine()
            appendLine(scorecard.executiveSummary)
            appendLine()
            appendLine("MARKER BREAKDOWN:")
            markers.forEach { m ->
                val a = assessments[m.id]
                if (a != null) {
                    appendLine(
                        "0${m.stepNumber}. ${m.title} (Wt ${(m.weight * 100).toInt()}%) [${a.sourceLabel}] -> " +
                            "Signal ${a.authenticityScore}/100 | Conf ${a.confidenceScore}/100 | " +
                            "Box ${a.framingScore}% / Light ${a.lightingScore}% / Focus ${a.sharpnessScore}%"
                    )
                    a.confirmations.forEach { appendLine("   + $it") }
                    a.anomalies.forEach { appendLine("   - $it") }
                    if (!a.qualityPassed && a.retakeAngleInstruction != null) {
                        appendLine("   ! Retake: ${a.retakeAngleInstruction}")
                    }
                }
            }
            appendLine()
            appendLine("HUMAN AUGMENTATION CHECKLIST:")
            scorecard.prioritizedHumanChecks.forEachIndexed { idx, c ->
                appendLine("${idx + 1}. $c")
            }
            appendLine()
            appendLine(scorecard.disclaimer)
        }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                val item = optString(i)
                if (item.isNotBlank()) add(item)
            }
        }
    }
}
