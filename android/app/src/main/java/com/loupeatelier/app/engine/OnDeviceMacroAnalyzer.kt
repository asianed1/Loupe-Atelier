package com.loupeatelier.app.engine

import android.graphics.Bitmap
import com.loupeatelier.app.model.ClientOpticalMetrics
import com.loupeatelier.app.model.FinalScorecard
import com.loupeatelier.app.model.MarkerAssessment
import com.loupeatelier.app.model.MarkerSpec
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Self-contained Option A on-device macro CV & expert-rule scoring engine.
 * Runs locally on the 4:3 macro ROI Bitmap using primitive arrays (low GC churn).
 */
object OnDeviceMacroAnalyzer {

    fun cropToBoundingBoxRoi(source: Bitmap): Bitmap {
        val targetAspect = 4f / 3f
        val srcW = source.width
        val srcH = source.height
        if (srcW <= 0 || srcH <= 0) return source

        // Fit 4:3 frame then crop to the 88% calibrated central macro window
        val boxW: Int
        val boxH: Int
        if (srcW.toFloat() / srcH > targetAspect) {
            boxH = (srcH * 0.88f).roundToInt().coerceAtLeast(1)
            boxW = (boxH * targetAspect).roundToInt().coerceAtMost(srcW)
        } else {
            boxW = (srcW * 0.88f).roundToInt().coerceAtLeast(1)
            boxH = (boxW / targetAspect).roundToInt().coerceAtMost(srcH)
        }

        val left = ((srcW - boxW) / 2).coerceAtLeast(0)
        val top = ((srcH - boxH) / 2).coerceAtLeast(0)
        return Bitmap.createBitmap(source, left, top, boxW, boxH)
    }

    fun analyzeMarker(
        roiBitmap: Bitmap,
        marker: MarkerSpec,
        captureMetrics: ClientOpticalMetrics
    ): MarkerAssessment {
        // High-precision 480x360 grayscale analysis buffer for macro detail
        val w = 480
        val h = 360
        val scaled = if (roiBitmap.width == w && roiBitmap.height == h) {
            roiBitmap
        } else {
            Bitmap.createScaledBitmap(roiBitmap, w, h, true)
        }
        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        if (scaled !== roiBitmap) scaled.recycle()

        val gray = FloatArray(w * h)
        var lumSum = 0.0
        var shadowClip = 0
        var highlightClip = 0

        for (i in pixels.indices) {
            val c = pixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val y = 0.299f * r + 0.587f * g + 0.114f * b
            gray[i] = y
            lumSum += y
            if (y < 24f) shadowClip++
            if (y > 245f) highlightClip++
        }

        val meanLum = lumSum / pixels.size
        val shadowRatio = shadowClip.toDouble() / pixels.size
        val highlightRatio = highlightClip.toDouble() / pixels.size

        // 1. Lighting & Glare Gate (0-100)
        val lumPenalty = when {
            meanLum < 54.0 -> (54.0 - meanLum) * 1.45
            meanLum > 216.0 -> (meanLum - 216.0) * 1.55
            else -> 0.0
        }
        val glarePenalty = highlightRatio * 155.0
        val darkPenalty = shadowRatio * 82.0
        val lightingScore = (97.0 - lumPenalty - glarePenalty - darkPenalty)
            .roundToInt()
            .coerceIn(0, 100)

        // 2. Macro Focus Sharpness (0-100)
        val lapSharpness = computeCentralLaplacianScore(gray, w, h)
        val sharpnessScore = if (captureMetrics.sharpnessIndex >= 0) {
            ((lapSharpness * 0.85) + (captureMetrics.sharpnessIndex * 0.15))
                .roundToInt()
                .coerceIn(0, 100)
        } else {
            lapSharpness
        }

        // 3. Bounding-Box Proportion & Subject Presence Fit (0-100)
        val isSmoothSubstrate = marker.boundingBox.reticleType in setOf(
            "hardware-plate", "dial-circle", "typography-band", "serial-tag"
        )
        val framingScore = computeBoundingBoxFitScore(gray, w, h, isSmoothSubstrate)

        val qualityPassed = framingScore >= 55 && lightingScore >= 50 && sharpnessScore >= 55

        // 4. Feature-specific macro craftsmanship evaluation
        val featureEval = when (marker.boundingBox.reticleType) {
            "stitch-line" -> evaluateStitchMacro(gray, w, h)
            "typography-band" -> evaluateTypographyMacro(gray, w, h)
            "serial-tag" -> evaluateSerialAndEdgeMacro(gray, w, h)
            "hardware-plate", "dial-circle" -> evaluateHardwareOrDialMacro(gray, w, h)
            else -> evaluateGeneralMacro(gray, w, h)
        }

        val rawConfidence = (
            lightingScore * 0.28 +
                sharpnessScore * 0.42 +
                framingScore * 0.30
            ).roundToInt().coerceIn(10, 96)

        val finalConfidence = if (qualityPassed) {
            rawConfidence
        } else {
            minOf(rawConfidence, 46)
        }

        val retakeReason = buildQualityPenaltyReason(
            lightingScore,
            sharpnessScore,
            framingScore,
            highlightRatio,
            meanLum
        )
        val retakeInstruction = if (!qualityPassed) {
            buildRetakeInstruction(
                marker,
                lightingScore,
                sharpnessScore,
                framingScore,
                highlightRatio,
                meanLum
            )
        } else {
            null
        }

        val adjustedAuthScore = if (qualityPassed) {
            featureEval.score
        } else {
            ((featureEval.score * 0.30) + (50 * 0.70)).roundToInt().coerceIn(25, 72)
        }

        val summary = if (qualityPassed) {
            "On-device macro screening scored $adjustedAuthScore/100 visual consistency ($finalConfidence% optical confidence) for ${marker.title}."
        } else {
            "Optical quality or bounding-box fill fell below macro threshold; confidence lowered to $finalConfidence%. Retake recommended."
        }

        return MarkerAssessment(
            markerId = marker.id,
            authenticityScore = adjustedAuthScore,
            confidenceScore = finalConfidence,
            framingScore = framingScore,
            lightingScore = lightingScore,
            sharpnessScore = sharpnessScore,
            qualityPassed = qualityPassed,
            retakeRecommended = !qualityPassed,
            retakeAngleInstruction = retakeInstruction,
            qualityPenaltyReason = retakeReason,
            confirmations = featureEval.confirmations,
            anomalies = featureEval.anomalies,
            humanJudgementNote = marker.targetConfirmations.firstOrNull()?.let {
                "Physically verify: $it under a 10x loupe alongside tactile inspection."
            } ?: "Verify material weight and physical finishing alongside visual score.",
            summary = summary
        )
    }

    fun synthesizeScorecard(
        protocol: CategoryProtocol,
        itemName: String,
        assessments: List<MarkerAssessment>
    ): FinalScorecard {
        var weightedProb = 0.0
        var effWeightSum = 0.0
        var weightedConf = 0.0
        var baseWeightSum = 0.0
        val lowConfidence = mutableListOf<String>()

        for (marker in protocol.markers) {
            val assess = assessments.find { it.markerId == marker.id } ?: continue
            val w = if (marker.weight > 0) marker.weight else 1.0 / protocol.markers.size
            val confFactor = (assess.confidenceScore / 100.0).coerceAtLeast(0.20)
            weightedProb += assess.authenticityScore * w * confFactor
            effWeightSum += w * confFactor
            weightedConf += assess.confidenceScore * w
            baseWeightSum += w

            if (!assess.qualityPassed || assess.confidenceScore < 60) {
                lowConfidence.add(marker.title)
            }
        }

        val overallProb = if (effWeightSum > 0) (weightedProb / effWeightSum).roundToInt().coerceIn(0, 100) else 0
        val overallConf = if (baseWeightSum > 0) (weightedConf / baseWeightSum).roundToInt().coerceIn(0, 100) else 0

        val verdict = when {
            overallConf < 55 || lowConfidence.size >= (protocol.markers.size + 1) / 2 ->
                "Inconclusive — Retake or Human Audit Needed"
            overallProb >= 78 -> "High Visual Consistency"
            overallProb >= 58 -> "Moderate Visual Consistency"
            else -> "Elevated Anomaly Signal"
        }

        val houseTip = ExpertKnowledgeBase.findHouseReference(itemName)
        val summary = buildString {
            append("On-device edge screening evaluated ${assessments.size} of ${protocol.markers.size} macro markers ")
            append("for $itemName, yielding a $overallProb/100 visual authenticity probability ")
            append("with $overallConf/100 optical confidence. ")
            if (lowConfidence.isNotEmpty()) {
                append("Confidence penalized on: ${lowConfidence.joinToString(", ")}. ")
            }
            if (houseTip != null) {
                append("House cue (${houseTip.houseName}): ${houseTip.eraAndSerialTip}")
            }
        }

        val combinedHumanChecks = buildList {
            if (houseTip != null) {
                add("${houseTip.houseName} Signature Check: ${houseTip.signatureMacroCues}")
                add("${houseTip.houseName} Serial/Era Check: ${houseTip.eraAndSerialTip}")
            }
            addAll(protocol.humanChecks)
        }

        return FinalScorecard(
            overallProbabilityScore = overallProb,
            overallConfidenceScore = overallConf,
            verdictBand = verdict,
            executiveSummary = summary,
            lowConfidenceMarkers = lowConfidence,
            prioritizedHumanChecks = combinedHumanChecks,
            disclaimer = "Pre-Screening Notice: Scores reflect probabilistic visual macro signals (stitch slant & pitch regularity, adaptive typography kerning/bleed, metal grain & porosity, and optical framing). They do not constitute a 100% authentication guarantee and must be paired with qualified human judgement."
        )
    }

    private fun computeCentralLaplacianScore(gray: FloatArray, w: Int, h: Int): Int {
        var sum = 0.0
        var sqSum = 0.0
        var count = 0
        for (y in (h / 6) until (5 * h / 6) step 2) {
            val row = y * w
            val rowUp = (y - 1) * w
            val rowDown = (y + 1) * w
            for (x in (w / 6) until (5 * w / 6) step 2) {
                val c = gray[row + x]
                val lap = gray[rowUp + x] +
                    gray[rowDown + x] +
                    gray[row + x - 1] +
                    gray[row + x + 1] - 4f * c
                sum += lap
                sqSum += lap * lap
                count++
            }
        }
        val mean = if (count > 0) sum / count else 0.0
        val variance = if (count > 0) (sqSum / count - mean * mean).coerceAtLeast(0.0) else 0.0
        return ((sqrt(variance) / 26.0) * 100.0).roundToInt().coerceIn(0, 100)
    }

    private fun computeBoundingBoxFitScore(
        gray: FloatArray,
        w: Int,
        h: Int,
        isSmoothSubstrate: Boolean
    ): Int {
        var innerEnergy = 0.0
        var innerCount = 0
        var borderEnergy = 0.0
        var borderCount = 0
        var activeCells = 0
        var crispFeatureCells = 0
        var totalCells = 0

        val xMin = (w * 0.14).toInt()
        val xMax = (w * 0.86).toInt()
        val yMin = (h * 0.14).toInt()
        val yMax = (h * 0.86).toInt()

        for (y in 4 until h - 4 step 2) {
            val row = y * w
            val rowUp = (y - 1) * w
            val rowDown = (y + 1) * w
            for (x in 4 until w - 4 step 2) {
                val gx = abs(gray[row + x + 1] - gray[row + x - 1])
                val gy = abs(gray[rowDown + x] - gray[rowUp + x])
                val mag = gx + gy
                if (x in xMin..xMax && y in yMin..yMax) {
                    innerEnergy += mag
                    innerCount++
                    if (mag > 12f) activeCells++
                    if (mag > 32f) crispFeatureCells++
                    totalCells++
                } else {
                    borderEnergy += mag
                    borderCount++
                }
            }
        }

        val innerMean = if (innerCount > 0) innerEnergy / innerCount else 0.0
        val borderMean = if (borderCount > 0) borderEnergy / borderCount else 0.0
        val fillCoverage = if (totalCells > 0) activeCells.toDouble() / totalCells else 0.0
        val crispRatio = if (totalCells > 0) crispFeatureCells.toDouble() / totalCells else 0.0

        // Reject blank/covered lens or featureless shots
        if (innerMean < 4.5 || (fillCoverage < 0.05 && crispRatio < 0.015)) return 26

        val concentrationRatio = innerMean / (borderMean + 3.5)
        val raw = if (isSmoothSubstrate) {
            // Smooth metal plates, dials, and stamped leather have crisp focal engravings over calmer backgrounds
            (innerMean * 1.9 + concentrationRatio * 28.0 + fillCoverage * 26.0 + crispRatio * 120.0).roundToInt()
        } else {
            (innerMean * 2.1 + concentrationRatio * 24.0 + fillCoverage * 34.0).roundToInt()
        }
        return raw.coerceIn(22, 98)
    }

    private data class FeatureEval(
        val score: Int,
        val confirmations: List<String>,
        val anomalies: List<String>
    )

    private fun evaluateStitchMacro(gray: FloatArray, w: Int, h: Int): FeatureEval {
        val angleBins = IntArray(18)
        var strongEdges = 0
        var obliqueEdges = 0
        val yStart = (h * 0.28).toInt()
        val yEnd = (h * 0.72).toInt()
        val xStart = (w * 0.10).toInt()
        val xEnd = (w * 0.90).toInt()

        val colEnergy = DoubleArray(xEnd - xStart)

        for (y in yStart until yEnd) {
            val rowUp = (y - 1) * w
            val row = y * w
            val rowDown = (y + 1) * w
            for (x in xStart until xEnd) {
                val gx = -gray[rowUp + x - 1] + gray[rowUp + x + 1] -
                    2f * gray[row + x - 1] + 2f * gray[row + x + 1] -
                    gray[rowDown + x - 1] + gray[rowDown + x + 1]
                val gy = -gray[rowUp + x - 1] - 2f * gray[rowUp + x] - gray[rowUp + x + 1] +
                    gray[rowDown + x - 1] + 2f * gray[rowDown + x] + gray[rowDown + x + 1]
                val mag = hypot(gx.toDouble(), gy.toDouble())
                colEnergy[x - xStart] += mag
                if (mag > 44.0) {
                    var deg = atan2(gy.toDouble(), gx.toDouble()) * 180.0 / PI
                    if (deg < 0) deg += 180.0
                    val bin = (deg / 10.0).toInt().coerceIn(0, 17)
                    angleBins[bin]++
                    strongEdges++
                    // Gradient angles perpendicular to slanted saddle stitches (~15-35 deg off axis),
                    // excluding purely horizontal/vertical lock-stitch or straight edge lines (bins 0, 8, 9, 17)
                    if (bin in 1..3 || bin in 6..7 || bin in 10..11 || bin in 14..16) {
                        obliqueEdges++
                    }
                }
            }
        }

        if (strongEdges < 110) {
            return FeatureEval(
                score = 45,
                confirmations = emptyList(),
                anomalies = listOf("Insufficient stitch contour density detected inside stitch band")
            )
        }

        // Zero-mean autocorrelation for periodic stitch spacing along seam
        var colSum = 0.0
        for (v in colEnergy) colSum += v
        val meanCol = colSum / colEnergy.size
        val centered = DoubleArray(colEnergy.size)
        var zeroLag = 0.0
        for (i in colEnergy.indices) {
            val d = colEnergy[i] - meanCol
            centered[i] = d
            zeroLag += d * d
        }
        var bestPeriodCorr = 0.0
        for (lag in 14..56) {
            if (lag >= centered.size) break
            var corr = 0.0
            for (i in 0 until centered.size - lag) {
                corr += centered[i] * centered[i + lag]
            }
            val norm = if (zeroLag > 0) corr / zeroLag else 0.0
            if (norm > bestPeriodCorr) bestPeriodCorr = norm
        }

        var maxBin = 0
        for (b in angleBins) if (b > maxBin) maxBin = b
        val dominantShare = maxBin.toDouble() / strongEdges
        val obliqueShare = obliqueEdges.toDouble() / strongEdges

        val confirmations = mutableListOf<String>()
        val anomalies = mutableListOf<String>()

        val score = (42.0 + dominantShare * 84.0 + obliqueShare * 26.0 + bestPeriodCorr.coerceAtLeast(0.0) * 38.0)
            .roundToInt()
            .coerceIn(35, 94)

        if (dominantShare >= 0.18 && obliqueShare >= 0.22) {
            confirmations.add("Consistent directional stitch slant measured (${(dominantShare * 100).roundToInt()}% orientation lock)")
        } else {
            anomalies.add("Weak or flat stitch orientation across seam band (check for straight machine lock-stitch)")
        }

        if (bestPeriodCorr >= 0.23) {
            confirmations.add("Uniform periodic stitch pitch spacing confirmed along seam")
        } else {
            anomalies.add("Irregular stitch spacing periodicity detected along horizontal track")
        }

        return FeatureEval(score, confirmations, anomalies)
    }

    private fun evaluateTypographyMacro(gray: FloatArray, w: Int, h: Int): FeatureEval {
        val yStart = (h * 0.24).toInt()
        val yEnd = (h * 0.76).toInt()
        val xStart = (w * 0.12).toInt()
        val xEnd = (w * 0.88).toInt()

        val rowCount = yEnd - yStart
        val colCount = xEnd - xStart
        val mags = FloatArray(rowCount * colCount)
        var maxMag = 0f
        var magSum = 0.0

        var idx = 0
        for (y in yStart until yEnd) {
            val row = y * w
            val rowUp = (y - 1) * w
            val rowDown = (y + 1) * w
            for (x in xStart until xEnd) {
                val gx = abs(gray[row + x + 1] - gray[row + x - 1])
                val gy = abs(gray[rowDown + x] - gray[rowUp + x])
                val mag = gx + gy
                mags[idx++] = mag
                magSum += mag
                if (mag > maxMag) maxMag = mag
            }
        }

        val meanMag = magSum / maxOf(1, mags.size)
        if (meanMag < 5.5 || maxMag < 24f) {
            return FeatureEval(
                score = 45,
                confirmations = emptyList(),
                anomalies = listOf("Minimal stamped letterform contrast detected inside typography frame")
            )
        }

        // Adaptive threshold calibrated to lighting contrast so raking-light debossing works cleanly
        val crispThreshold = (meanMag * 2.8).toFloat().coerceIn(32f, 68f)
        val haloLow = crispThreshold * 0.36f
        val haloHigh = crispThreshold * 0.70f

        var highGradCount = 0
        var midHaloCount = 0
        val colHighEdges = IntArray(colCount)

        idx = 0
        for (ry in 0 until rowCount) {
            for (cx in 0 until colCount) {
                val mag = mags[idx++]
                if (mag >= crispThreshold) {
                    highGradCount++
                    colHighEdges[cx]++
                } else if (mag in haloLow..haloHigh) {
                    midHaloCount++
                }
            }
        }

        val density = highGradCount.toDouble() / maxOf(1, mags.size)
        if (density < 0.012) {
            return FeatureEval(
                score = 46,
                confirmations = emptyList(),
                anomalies = listOf("Insufficient letterform edge density inside typography band")
            )
        }

        // Primitive gap statistics without list boxing
        var gapCount = 0
        var gapSum = 0.0
        var gapSqSum = 0.0
        var runGap = 0
        var insideGap = false
        for (count in colHighEdges) {
            if (count <= 2) {
                runGap++
                insideGap = true
            } else if (insideGap) {
                if (runGap in 2..28) {
                    gapCount++
                    gapSum += runGap
                    gapSqSum += runGap * runGap
                }
                runGap = 0
                insideGap = false
            }
        }

        val gapConsistency = if (gapCount >= 3) {
            val meanGap = gapSum / gapCount
            val variance = (gapSqSum / gapCount - meanGap * meanGap).coerceAtLeast(0.0)
            (1.0 - (sqrt(variance) / (meanGap + 1.0))).coerceIn(0.0, 1.0)
        } else {
            0.55
        }

        val crispRatio = highGradCount.toDouble() / (highGradCount + midHaloCount + 1)
        val score = (42.0 + crispRatio * 58.0 + gapConsistency * 24.0)
            .roundToInt()
            .coerceIn(38, 94)

        val confirmations = mutableListOf<String>()
        val anomalies = mutableListOf<String>()

        if (crispRatio >= 0.40) {
            confirmations.add("Sharp letterform edge transitions with clean foil/ink containment")
        } else {
            anomalies.add("Soft edge haloing around character boundaries; inspect serif kerning and foil bleed")
        }
        if (gapConsistency >= 0.60) {
            confirmations.add("Balanced inter-glyph spacing rhythm across stamp width")
        } else {
            anomalies.add("Variable spacing gaps detected between character clusters")
        }

        return FeatureEval(score, confirmations, anomalies)
    }

    private fun evaluateSerialAndEdgeMacro(gray: FloatArray, w: Int, h: Int): FeatureEval {
        val typoEval = evaluateTypographyMacro(gray, w, h)
        // Inspect outer border strip for thick uneven edge glazing bleed
        var edgeSpikeCount = 0
        var edgeSamples = 0
        for (y in (h * 0.16).toInt() until (h * 0.84).toInt() step 2) {
            val row = y * w
            for (x in (w * 0.80).toInt() until (w * 0.92).toInt() step 2) {
                val gx = abs(gray[row + x + 1] - gray[row + x - 1])
                if (gx > 42f) edgeSpikeCount++
                edgeSamples++
            }
        }
        val edgeVarianceRatio = if (edgeSamples > 0) edgeSpikeCount.toDouble() / edgeSamples else 0.0
        val adjusted = (typoEval.score + if (edgeVarianceRatio in 0.02..0.18) 3 else -3).coerceIn(38, 93)
        val confs = typoEval.confirmations.toMutableList()
        val anoms = typoEval.anomalies.toMutableList()
        if (edgeVarianceRatio <= 0.20) {
            confs.add("Adjacent border finish transitions cleanly without heavy paint pooling")
        } else {
            anoms.add("Abrupt edge transition variance; verify multi-coat edge glazing smoothness")
        }
        return FeatureEval(adjusted, confs.take(3), anoms.take(3))
    }

    private fun evaluateHardwareOrDialMacro(gray: FloatArray, w: Int, h: Int): FeatureEval {
        val yStart = (h * 0.20).toInt()
        val yEnd = (h * 0.80).toInt()
        val xStart = (w * 0.18).toInt()

        var gxTotal = 0.0
        var gyTotal = 0.0
        var symmetricPairs = 0
        var totalPairs = 0
        var microPittingCount = 0

        for (y in yStart until yEnd) {
            val row = y * w
            val rowUp = (y - 1) * w
            val rowDown = (y + 1) * w
            for (x in xStart until (w / 2)) {
                val c = gray[row + x]
                // Skip specular catchlights so normal metallic highlights don't look like porosity
                if (c > 238f) continue

                val gx = abs(gray[row + x + 1] - gray[row + x - 1])
                val gy = abs(gray[rowDown + x] - gray[rowUp + x])
                gxTotal += gx
                gyTotal += gy

                val avgN = (gray[rowUp + x] + gray[rowDown + x] + gray[row + x - 1] + gray[row + x + 1]) * 0.25f
                if (abs(c - avgN) > 22f && gx < 18f && gy < 18f) {
                    microPittingCount++
                }

                val mirrorX = w - 1 - x
                if (abs(c - gray[row + mirrorX]) < 34f) {
                    symmetricPairs++
                }
                totalPairs++
            }
        }

        val directionalRatio = abs(gxTotal - gyTotal) / (gxTotal + gyTotal + 1.0)
        val symmetryRatio = if (totalPairs > 0) symmetricPairs.toDouble() / totalPairs else 0.5
        val pittingRatio = if (totalPairs > 0) microPittingCount.toDouble() / totalPairs else 0.0

        val rawScore = 50.0 + directionalRatio * 46.0 + symmetryRatio * 34.0 - pittingRatio * 180.0
        val score = rawScore.roundToInt().coerceIn(38, 93)

        val confirmations = mutableListOf<String>()
        val anomalies = mutableListOf<String>()
        if (directionalRatio >= 0.11) {
            confirmations.add("Directional satin brushing or machined facet consistency detected")
        } else {
            anomalies.add("Isotropic surface noise; inspect for cast texture under plating")
        }
        if (pittingRatio > 0.03) {
            anomalies.add("Micro-speckle surface variance detected; check for casting porosity")
        } else {
            confirmations.add("Clean surface continuity without heavy casting porosity")
        }

        return FeatureEval(score, confirmations, anomalies)
    }

    private fun evaluateGeneralMacro(gray: FloatArray, w: Int, h: Int): FeatureEval {
        val sharpness = computeCentralLaplacianScore(gray, w, h)
        val score = (48.0 + sharpness * 0.42).roundToInt().coerceIn(40, 89)
        return FeatureEval(
            score = score,
            confirmations = listOf("Macro edge definition and surface grain captured for inspection"),
            anomalies = if (sharpness < 45) listOf("Fine grain detail slightly soft for conclusive micro-edge check") else emptyList()
        )
    }

    private fun buildQualityPenaltyReason(
        lighting: Int,
        sharpness: Int,
        framing: Int,
        highlightRatio: Double,
        meanLum: Double
    ): String? {
        val reasons = mutableListOf<String>()
        if (framing < 55) reasons.add("target did not fill 60–85% of the bounding box")
        if (sharpness < 55) reasons.add("macro focus sharpness was below threshold ($sharpness%)")
        if (lighting < 50) {
            if (highlightRatio > 0.12) reasons.add("specular glare obscured surface detail")
            else if (meanLum < 60.0) reasons.add("scene illumination was too dim")
            else reasons.add("uneven lighting contrast")
        }
        return if (reasons.isEmpty()) null else "Confidence lowered because ${reasons.joinToString("; ")}."
    }

    private fun buildRetakeInstruction(
        marker: MarkerSpec,
        lighting: Int,
        sharpness: Int,
        framing: Int,
        highlightRatio: Double,
        meanLum: Double
    ): String {
        val steps = mutableListOf<String>()
        if (framing < 55) {
            steps.add("Move closer (6–10 cm) so ${marker.focusFeature} fills ${marker.boundingBox.targetFillMinPercent}–${marker.boundingBox.targetFillMaxPercent}% of the 4:3 box")
        }
        if (sharpness < 55) {
            steps.add("Brace hands, tap the bounding box to lock macro AF, and hold steady")
        }
        if (lighting < 50) {
            if (highlightRatio > 0.12) {
                steps.add("Turn off direct torch or tilt 15° to eliminate specular glare")
            } else if (meanLum < 60.0) {
                steps.add("Move to bright indirect daylight or enable diffused task light")
            }
        }
        return steps.joinToString(". ") + "."
    }
}
