package com.loupeatelier.app.model

data class BoundingBoxSpec(
    val aspectRatioLabel: String = "4:3 Calibrated Macro",
    val aspectRatioValue: Float = 4f / 3f,
    val targetFillMinPercent: Int = 65,
    val targetFillMaxPercent: Int = 85,
    val alignmentRule: String,
    val reticleType: String
)

data class MarkerSpec(
    val id: String,
    val stepNumber: Int,
    val title: String,
    val focusFeature: String,
    val weight: Double,
    val lightingRequirement: String,
    val angleAndDistance: String,
    val targetConfirmations: List<String>,
    val targetAnomalies: List<String>,
    val boundingBox: BoundingBoxSpec
)

data class ClientOpticalMetrics(
    val averageBrightness: Int,
    val sharpnessIndex: Int,
    val width: Int,
    val height: Int,
    val zoomLevel: Float
)

data class MarkerAssessment(
    val markerId: String,
    val authenticityScore: Int,
    val confidenceScore: Int,
    val framingScore: Int,
    val lightingScore: Int,
    val sharpnessScore: Int,
    val qualityPassed: Boolean,
    val retakeRecommended: Boolean,
    val retakeAngleInstruction: String?,
    val qualityPenaltyReason: String?,
    val confirmations: List<String>,
    val anomalies: List<String>,
    val humanJudgementNote: String,
    val summary: String
)

data class FinalScorecard(
    val overallProbabilityScore: Int,
    val overallConfidenceScore: Int,
    val verdictBand: String,
    val executiveSummary: String,
    val lowConfidenceMarkers: List<String>,
    val prioritizedHumanChecks: List<String>,
    val disclaimer: String
)
