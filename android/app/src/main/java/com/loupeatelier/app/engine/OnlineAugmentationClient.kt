package com.loupeatelier.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import com.loupeatelier.app.model.BoundingBoxSpec
import com.loupeatelier.app.model.ClientOpticalMetrics
import com.loupeatelier.app.model.MarkerAssessment
import com.loupeatelier.app.model.MarkerSpec
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lightweight zero-dependency HTTPS augmentation client using built-in HttpURLConnection.
 * Only invoked on explicit user tap when an item is outside the offline databank or
 * when a second-opinion cloud macro check is requested.
 */
object OnlineAugmentationClient {

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun fetchCustomProtocol(baseUrl: String, itemDescription: String): CategoryProtocol {
        val cleanBase = baseUrl.trim().trimEnd('/')
        val payload = JSONObject().apply {
            put("itemDescription", itemDescription)
        }
        val response = postJson("$cleanBase/api/protocol/plan", payload, timeoutMs = 25000)
        val itemTitle = response.optString("itemTitle").ifBlank { itemDescription }
        val rawMarkers = response.optJSONArray("markers")
            ?: throw IllegalStateException("Server returned no marker steps for \"$itemDescription\".")

        val markers = buildList {
            for (i in 0 until rawMarkers.length()) {
                val m = rawMarkers.optJSONObject(i) ?: continue
                val bb = m.optJSONObject("boundingBox")
                add(
                    MarkerSpec(
                        id = m.optString("id").ifBlank { "custom-step-${i + 1}" },
                        stepNumber = m.optInt("stepNumber", i + 1),
                        title = m.optString("title"),
                        focusFeature = m.optString("focusFeature"),
                        weight = m.optDouble("weight", 0.25),
                        lightingRequirement = m.optString("lightingRequirement"),
                        angleAndDistance = m.optString("angleAndDistance"),
                        targetConfirmations = m.optJSONArray("targetConfirmations").toList(),
                        targetAnomalies = m.optJSONArray("targetAnomalies").toList(),
                        boundingBox = BoundingBoxSpec(
                            aspectRatioLabel = bb?.optString("aspectRatioLabel") ?: "4:3 Calibrated Macro",
                            aspectRatioValue = (bb?.optDouble("aspectRatioValue", 4.0 / 3.0) ?: (4.0 / 3.0)).toFloat(),
                            targetFillMinPercent = bb?.optInt("targetFillMinPercent", 60) ?: 60,
                            targetFillMaxPercent = bb?.optInt("targetFillMaxPercent", 85) ?: 85,
                            alignmentRule = bb?.optString("alignmentRule") ?: "Center target inside 4:3 macro guide.",
                            reticleType = bb?.optString("reticleType") ?: "general-macro"
                        )
                    )
                )
            }
        }
        if (markers.isEmpty()) {
            throw IllegalStateException("Unable to generate custom protocol for \"$itemDescription\".")
        }

        val slug = itemTitle.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').take(28)
        return CategoryProtocol(
            id = "custom-$slug-${System.currentTimeMillis() % 10000}",
            name = "Custom Cached · $itemTitle",
            defaultItemName = itemTitle,
            markers = markers,
            humanChecks = listOf(
                "Cross-check material weight, surface smell/tactile finish, and serial documentation against house archives.",
                "Compare high-magnification macro captures of typography and hardware under a 10x loupe."
            ),
            isCustomCached = true
        )
    }

    fun requestSecondOpinion(
        baseUrl: String,
        itemName: String,
        categoryName: String,
        marker: MarkerSpec,
        roiBitmap: Bitmap,
        metrics: ClientOpticalMetrics
    ): MarkerAssessment {
        // Compress ROI cleanly (max 800px, JPEG 84%) -> ~55-110 KB upload
        val uploadBmp = if (roiBitmap.width > 800 || roiBitmap.height > 600) {
            Bitmap.createScaledBitmap(roiBitmap, 800, 600, true)
        } else {
            roiBitmap
        }
        val out = ByteArrayOutputStream()
        uploadBmp.compress(Bitmap.CompressFormat.JPEG, 84, out)
        if (uploadBmp !== roiBitmap) uploadBmp.recycle()

        val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        val dataUrl = "data:image/jpeg;base64,$b64"

        val payload = JSONObject().apply {
            put("imageDataUrl", dataUrl)
            put("categoryName", categoryName)
            put("itemName", itemName)
            put("markerSpec", JSONObject().apply {
                put("id", marker.id)
                put("stepNumber", marker.stepNumber)
                put("title", marker.title)
                put("focusFeature", marker.focusFeature)
                put("weight", marker.weight)
                put("lightingRequirement", marker.lightingRequirement)
                put("angleAndDistance", marker.angleAndDistance)
                put("targetConfirmations", JSONArray(marker.targetConfirmations))
                put("targetAnomalies", JSONArray(marker.targetAnomalies))
                put("boundingBox", JSONObject().apply {
                    put("aspectRatioLabel", marker.boundingBox.aspectRatioLabel)
                    put("aspectRatioValue", marker.boundingBox.aspectRatioValue.toDouble())
                    put("targetFillMinPercent", marker.boundingBox.targetFillMinPercent)
                    put("targetFillMaxPercent", marker.boundingBox.targetFillMaxPercent)
                    put("alignmentRule", marker.boundingBox.alignmentRule)
                    put("reticleType", marker.boundingBox.reticleType)
                })
            })
            put("clientMetrics", JSONObject().apply {
                put("averageBrightness", metrics.averageBrightness)
                put("sharpnessIndex", metrics.sharpnessIndex)
                put("width", metrics.width)
                put("height", metrics.height)
                put("zoomLevel", metrics.zoomLevel.toDouble())
            })
        }

        val cleanBase = baseUrl.trim().trimEnd('/')
        val res = postJson("$cleanBase/api/inspect/marker", payload, timeoutMs = 35000)
        return MarkerAssessment(
            markerId = res.optString("markerId").ifBlank { marker.id },
            authenticityScore = res.optInt("authenticityScore", 50),
            confidenceScore = res.optInt("confidenceScore", 50),
            framingScore = res.optInt("framingScore", 50),
            lightingScore = res.optInt("lightingScore", 50),
            sharpnessScore = res.optInt("sharpnessScore", 50),
            qualityPassed = res.optBoolean("qualityPassed", false),
            retakeRecommended = res.optBoolean("retakeRecommended", true),
            retakeAngleInstruction = res.optString("retakeAngleInstruction").takeIf { it.isNotBlank() && it != "null" },
            qualityPenaltyReason = res.optString("qualityPenaltyReason").takeIf { it.isNotBlank() && it != "null" },
            confirmations = res.optJSONArray("confirmations").toList(),
            anomalies = res.optJSONArray("anomalies").toList(),
            humanJudgementNote = res.optString("humanJudgementNote").ifBlank {
                "Verify physical material weight and finishing alongside cloud visual signal."
            },
            summary = res.optString("summary").ifBlank { "Online macro specialist inspection completed." },
            sourceLabel = "Online Second Opinion"
        )
    }

    private fun postJson(endpointUrl: String, body: JSONObject, timeoutMs: Int): JSONObject {
        val conn = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 12000
            readTimeout = timeoutMs
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
        }
        return try {
            conn.outputStream.use { os ->
                os.write(body.toString().toByteArray(Charsets.UTF_8))
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "{}"
            val json = JSONObject(text)
            if (code !in 200..299) {
                throw IllegalStateException(json.optString("error").ifBlank { "HTTP $code from augmentation service" })
            }
            json
        } finally {
            conn.disconnect()
        }
    }

    private fun JSONArray?.toList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                val s = optString(i)
                if (s.isNotBlank()) add(s)
            }
        }
    }
}
