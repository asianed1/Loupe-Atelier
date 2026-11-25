package com.loupeatelier.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.loupeatelier.app.engine.CategoryProtocol
import com.loupeatelier.app.engine.DemoSampleGallery
import com.loupeatelier.app.engine.DemoSampleKind
import com.loupeatelier.app.engine.DemoSampleSpec
import com.loupeatelier.app.engine.ExpertKnowledgeBase
import com.loupeatelier.app.engine.InspectionStore
import com.loupeatelier.app.engine.OnDeviceMacroAnalyzer
import com.loupeatelier.app.engine.SavedInspectionEntry
import com.loupeatelier.app.model.ClientOpticalMetrics
import com.loupeatelier.app.model.FinalScorecard
import com.loupeatelier.app.model.MarkerAssessment
import com.loupeatelier.app.ui.MacroCameraViewfinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LoupeAndroidApp()
            }
        }
    }
}

private data class DemoBatchResult(
    val assessments: Map<String, MarkerAssessment>,
    val thumbnails: Map<String, Bitmap>,
    val labels: Map<String, String>,
    val scorecard: FinalScorecard
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoupeAndroidApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val builtInProtocols = remember { ExpertKnowledgeBase.categories }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var selectedProtocol by remember { mutableStateOf<CategoryProtocol>(builtInProtocols.first()) }
    var itemName by remember { mutableStateOf(builtInProtocols.first().defaultItemName) }

    var inSetup by remember { mutableStateOf(true) }
    var showArchive by remember { mutableStateOf(false) }
    var showDemoGallery by remember { mutableStateOf(false) }
    var activeStepIndex by remember { mutableIntStateOf(0) }

    val assessments = remember { mutableStateMapOf<String, MarkerAssessment>() }
    val thumbnails = remember { mutableStateMapOf<String, Bitmap>() }
    val demoSampleUsed = remember { mutableStateMapOf<String, String>() }
    val demoPreviews = remember { mutableStateMapOf<String, Bitmap>() }

    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var scorecard by remember { mutableStateOf<FinalScorecard?>(null) }
    var isSaved by remember { mutableStateOf(false) }
    var savedEntries by remember { mutableStateOf<List<SavedInspectionEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        savedEntries = withContext(Dispatchers.IO) { InspectionStore.loadAll(context) }
    }

    LaunchedEffect(showDemoGallery) {
        if (showDemoGallery && demoPreviews.isEmpty()) {
            val generated = withContext(Dispatchers.Default) {
                DemoSampleGallery.samples.associate { sample ->
                    sample.id to DemoSampleGallery.renderSampleBitmap(sample.id, width = 240, height = 180)
                }
            }
            demoPreviews.putAll(generated)
        }
    }

    val markers = selectedProtocol.markers
    val currentMarker = markers[activeStepIndex.coerceIn(0, markers.lastIndex)]
    val currentAssessment = assessments[currentMarker.id]
    val currentThumb = thumbnails[currentMarker.id]
    val matchedHouse = remember(itemName) { ExpertKnowledgeBase.findHouseReference(itemName) }

    fun resetSessionState() {
        assessments.clear()
        thumbnails.clear()
        demoSampleUsed.clear()
        activeStepIndex = 0
        scorecard = null
        isSaved = false
        errorMessage = null
        statusMessage = null
    }

    fun runLocalAssessment(
        roiBitmap: Bitmap,
        metrics: ClientOpticalMetrics,
        demoLabel: String? = null
    ) {
        val targetMarker = currentMarker
        scope.launch {
            isAnalyzing = true
            errorMessage = null
            statusMessage = null
            try {
                val (result, thumb) = withContext(Dispatchers.Default) {
                    val assessed = OnDeviceMacroAnalyzer.analyzeMarker(roiBitmap, targetMarker, metrics)
                    val scaledThumb = Bitmap.createScaledBitmap(roiBitmap, 320, 240, true)
                    if (scaledThumb !== roiBitmap) roiBitmap.recycle()
                    assessed to scaledThumb
                }
                thumbnails[targetMarker.id] = thumb
                assessments[targetMarker.id] = result
                if (demoLabel != null) {
                    demoSampleUsed[targetMarker.id] = demoLabel
                } else {
                    demoSampleUsed.remove(targetMarker.id)
                }
                isSaved = false
            } catch (e: Exception) {
                errorMessage = e.message ?: "On-device macro inspection failed."
            } finally {
                isAnalyzing = false
            }
        }
    }

    fun runDemoSample(sample: DemoSampleSpec) {
        val wasInSetup = inSetup
        val targetProtocol = if (wasInSetup) builtInProtocols.first() else selectedProtocol
        val targetIdx = if (wasInSetup) {
            targetProtocol.markers.indexOfFirst { it.boundingBox.reticleType == sample.targetReticleType }
                .takeIf { it >= 0 } ?: 0
        } else {
            activeStepIndex.coerceIn(0, targetProtocol.markers.lastIndex)
        }
        val targetMarker = targetProtocol.markers[targetIdx]

        if (wasInSetup) {
            selectedProtocol = targetProtocol
            itemName = "[DEMO] ${targetProtocol.defaultItemName}"
            resetSessionState()
            activeStepIndex = targetIdx
            inSetup = false
        }
        scorecard = null
        showDemoGallery = false

        scope.launch {
            isAnalyzing = true
            errorMessage = null
            statusMessage = null
            try {
                val (result, thumb) = withContext(Dispatchers.Default) {
                    val bmp = DemoSampleGallery.renderSampleBitmap(sample.id, width = 960, height = 720)
                    val metrics = ClientOpticalMetrics(
                        averageBrightness = 128,
                        sharpnessIndex = -1,
                        width = bmp.width,
                        height = bmp.height,
                        zoomLevel = 1.0f
                    )
                    val assessed = OnDeviceMacroAnalyzer.analyzeMarker(bmp, targetMarker, metrics)
                    val scaledThumb = Bitmap.createScaledBitmap(bmp, 320, 240, true)
                    if (scaledThumb !== bmp) bmp.recycle()
                    assessed to scaledThumb
                }
                thumbnails[targetMarker.id] = thumb
                assessments[targetMarker.id] = result
                demoSampleUsed[targetMarker.id] = sample.title
                isSaved = false
            } catch (e: Exception) {
                errorMessage = e.message ?: "Demo sample inspection failed."
            } finally {
                isAnalyzing = false
            }
        }
    }

    fun runFullDemoWalkthrough(preferDefect: Boolean) {
        val demoProtocol = builtInProtocols.first()
        val demoItemName = if (preferDefect) {
            "[DEMO] Structured Calfskin Flap (Counterfeit Defect Set)"
        } else {
            "[DEMO] Structured Calfskin Flap (Clean Benchmark Set)"
        }
        selectedProtocol = demoProtocol
        itemName = demoItemName
        resetSessionState()
        inSetup = false
        showDemoGallery = false

        scope.launch {
            isAnalyzing = true
            errorMessage = null
            try {
                val batch = withContext(Dispatchers.Default) {
                    val outAssess = mutableMapOf<String, MarkerAssessment>()
                    val outThumbs = mutableMapOf<String, Bitmap>()
                    val outLabels = mutableMapOf<String, String>()
                    demoProtocol.markers.forEach { marker ->
                        val sample = DemoSampleGallery.recommendedSampleFor(
                            marker.boundingBox.reticleType,
                            preferDefect = preferDefect
                        )
                        val bmp = DemoSampleGallery.renderSampleBitmap(sample.id, width = 960, height = 720)
                        val metrics = ClientOpticalMetrics(
                            averageBrightness = 128,
                            sharpnessIndex = -1,
                            width = bmp.width,
                            height = bmp.height,
                            zoomLevel = 1.0f
                        )
                        outAssess[marker.id] = OnDeviceMacroAnalyzer.analyzeMarker(bmp, marker, metrics)
                        val scaledThumb = Bitmap.createScaledBitmap(bmp, 320, 240, true)
                        if (scaledThumb !== bmp) bmp.recycle()
                        outThumbs[marker.id] = scaledThumb
                        outLabels[marker.id] = sample.title
                    }
                    val finalCard = OnDeviceMacroAnalyzer.synthesizeScorecard(
                        protocol = demoProtocol,
                        itemName = demoItemName,
                        assessments = demoProtocol.markers.mapNotNull { outAssess[it.id] }
                    )
                    DemoBatchResult(outAssess, outThumbs, outLabels, finalCard)
                }
                assessments.putAll(batch.assessments)
                thumbnails.putAll(batch.thumbnails)
                demoSampleUsed.putAll(batch.labels)
                scorecard = batch.scorecard
            } catch (e: Exception) {
                errorMessage = e.message ?: "Full demo walkthrough failed."
            } finally {
                isAnalyzing = false
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isAnalyzing = true
            errorMessage = null
            try {
                val (roi, metrics) = withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            val maxSide = maxOf(info.size.width, info.size.height)
                            if (maxSide > 1600) {
                                decoder.setTargetSampleSize((maxSide / 1600).coerceAtLeast(1))
                            }
                        }
                    } else {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    val cropped = OnDeviceMacroAnalyzer.cropToBoundingBoxRoi(bmp)
                    if (cropped !== bmp) bmp.recycle()
                    val m = ClientOpticalMetrics(
                        averageBrightness = 128,
                        sharpnessIndex = -1,
                        width = cropped.width,
                        height = cropped.height,
                        zoomLevel = 1.0f
                    )
                    cropped to m
                }
                runLocalAssessment(roi, metrics)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unable to read selected photo."
                isAnalyzing = false
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF7F5F0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Loupe Atelier · Edge AI",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    OutlinedButton(
                        onClick = {
                            showDemoGallery = false
                            showArchive = !showArchive
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(if (showArchive) "Close" else "Saved (${savedEntries.size})", fontSize = 12.sp)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (showArchive) {
                Text("Saved Offline Inspections", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                if (savedEntries.isEmpty()) {
                    Text("No saved scorecards yet.", fontSize = 13.sp, color = Color(0xFF5C5850))
                } else {
                    savedEntries.forEach { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFE2DDD2), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(entry.itemName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(
                                "${entry.categoryName} · Prob ${entry.probabilityScore}/100 · Conf ${entry.confidenceScore}/100",
                                fontSize = 12.sp,
                                color = Color(0xFF5C5850)
                            )
                            Text(entry.verdictBand, fontSize = 12.sp, color = Color(0xFF9A6B2F))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Loupe Atelier Scorecard: ${entry.itemName}")
                                            putExtra(Intent.EXTRA_TEXT, entry.reportText)
                                        }
                                        context.startActivity(Intent.createChooser(share, "Share Scorecard"))
                                    }
                                ) {
                                    Text("Share", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            savedEntries = withContext(Dispatchers.IO) {
                                                InspectionStore.deleteEntry(context, entry.id)
                                            }
                                        }
                                    }
                                ) {
                                    Text("Delete", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else if (showDemoGallery) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Demo Mode · Sample Gallery", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    OutlinedButton(onClick = { showDemoGallery = false }) {
                        Text("Back", fontSize = 12.sp)
                    }
                }
                Text(
                    "Demonstrate Loupe Atelier without a physical luxury item. Every sample feeds a real 960×720 macro image into the on-device computer-vision analyzer (no faked scores).",
                    fontSize = 13.sp,
                    color = Color(0xFF5C5850)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB44332)),
                        onClick = { runFullDemoWalkthrough(preferDefect = true) }
                    ) {
                        Text("4-Step Defect Demo", fontSize = 12.sp)
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D6A4E)),
                        onClick = { runFullDemoWalkthrough(preferDefect = false) }
                    ) {
                        Text("4-Step Clean Demo", fontSize = 12.sp)
                    }
                }

                HorizontalDivider()

                val orderedSamples = remember(inSetup, currentMarker.id) {
                    if (inSetup) {
                        DemoSampleGallery.samples
                    } else {
                        DemoSampleGallery.samples.sortedByDescending {
                            it.targetReticleType == currentMarker.boundingBox.reticleType
                        }
                    }
                }

                orderedSamples.forEach { sample ->
                    val previewBmp = demoPreviews[sample.id]
                    val matchesStep = !inSetup && sample.targetReticleType == currentMarker.boundingBox.reticleType
                    val badgeColor = when (sample.kind) {
                        DemoSampleKind.DEFECT -> Color(0xFFB44332)
                        DemoSampleKind.BENCHMARK -> Color(0xFF1D6A4E)
                        DemoSampleKind.OPTICAL_FAIL -> Color(0xFF9A6B2F)
                    }
                    val badgeLabel = when (sample.kind) {
                        DemoSampleKind.DEFECT -> "DEFECT SAMPLE"
                        DemoSampleKind.BENCHMARK -> "CLEAN BENCHMARK"
                        DemoSampleKind.OPTICAL_FAIL -> "RETAKE GATE DEMO"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(
                                width = if (matchesStep) 2.dp else 1.dp,
                                color = if (matchesStep) Color(0xFFE3B873) else Color(0xFFE2DDD2),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (previewBmp != null) {
                                Image(
                                    bitmap = previewBmp.asImageBitmap(),
                                    contentDescription = sample.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(96.dp)
                                        .aspectRatio(4f / 3f)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = if (matchesStep) "$badgeLabel · MATCHES STEP 0${currentMarker.stepNumber}" else badgeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = badgeColor
                                )
                                Text(sample.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(sample.subtitle, fontSize = 12.sp, color = Color(0xFF5C5850))
                            }
                        }
                        Text(sample.expectedNote, fontSize = 11.sp, color = Color(0xFF5C5850))
                        OutlinedButton(
                            enabled = !isAnalyzing,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { runDemoSample(sample) }
                        ) {
                            Text(
                                text = if (inSetup) {
                                    "Inspect This Sample Now"
                                } else {
                                    "Test on Step 0${currentMarker.stepNumber} (${currentMarker.title.take(20)}…)"
                                },
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else if (inSetup) {
                Text(
                    "Self-Contained On-Device Pre-Screening",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF181715)
                )
                Text(
                    "100% private & offline. Includes 12 built-in luxury category protocols, 16 house reference archives, and an adaptive checklist generator for any unlisted item—zero servers or accounts.",
                    fontSize = 13.sp,
                    color = Color(0xFF5C5850)
                )

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDemoGallery = true }
                ) {
                    Text("Demo Mode · Sample Defect & Benchmark Gallery", fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Brand & Model Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (matchedHouse != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFAF6EE), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE3B873), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Offline Archive Match · ${matchedHouse.houseName}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF9A6B2F)
                        )
                        Text(matchedHouse.signatureMacroCues, fontSize = 12.sp, color = Color(0xFF181715))
                        Text(matchedHouse.eraAndSerialTip, fontSize = 12.sp, color = Color(0xFF5C5850))
                    }
                }

                OutlinedButton(
                    enabled = itemName.trim().length >= 3,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val adaptive = ExpertKnowledgeBase.buildAdaptiveOfflineProtocol(itemName)
                        selectedProtocol = adaptive
                        resetSessionState()
                        statusMessage = "Tailored ${adaptive.markers.size}-step offline protocol built for \"${adaptive.defaultItemName}\"."
                    }
                ) {
                    Text("Auto-Match & Tailor Checklist for Typed Item", fontSize = 12.sp)
                }

                statusMessage?.let {
                    Text(it, fontSize = 12.sp, color = Color(0xFF1D6A4E), fontWeight = FontWeight.Medium)
                }
                errorMessage?.let {
                    Text(it, fontSize = 12.sp, color = Color(0xFFB44332))
                }

                if (selectedProtocol.isAdaptiveCustom) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(2.dp, Color(0xFF1D6A4E), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(selectedProtocol.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1D6A4E))
                        Text(
                            "${selectedProtocol.markers.size} Tailored Markers · Ready 100% Offline",
                            fontSize = 12.sp,
                            color = Color(0xFF5C5850)
                        )
                    }
                }

                builtInProtocols.forEach { proto ->
                    val isSelected = proto.id == selectedProtocol.id
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF181715) else Color(0xFFE2DDD2),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedProtocol = proto
                                itemName = proto.defaultItemName
                                resetSessionState()
                            }
                            .padding(14.dp)
                    ) {
                        Text(proto.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            "${proto.markers.size} Sequential Markers · ${proto.defaultItemName}",
                            fontSize = 12.sp,
                            color = Color(0xFF5C5850)
                        )
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181715)),
                    onClick = {
                        inSetup = false
                        activeStepIndex = 0
                        scorecard = null
                    }
                ) {
                    Text("Begin Macro Sequence")
                }
            } else if (scorecard != null) {
                val final = scorecard!!
                val cleanName = itemName.ifBlank { selectedProtocol.defaultItemName }
                Text("Final Pre-Screening Scorecard", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text(cleanName, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(
                    "Authenticity Probability: ${final.overallProbabilityScore}/100 · Visual Confidence: ${final.overallConfidenceScore}/100",
                    fontWeight = FontWeight.SemiBold
                )
                Text(final.verdictBand, color = Color(0xFF9A6B2F), fontWeight = FontWeight.SemiBold)
                Text(final.executiveSummary, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        enabled = !isSaved,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) Color(0xFF1D6A4E) else Color(0xFF181715)
                        ),
                        onClick = {
                            val report = InspectionStore.formatShareReport(
                                protocol = selectedProtocol,
                                itemName = cleanName,
                                markers = markers,
                                assessments = assessments,
                                scorecard = final
                            )
                            val entry = SavedInspectionEntry(
                                id = "insp-${System.currentTimeMillis()}",
                                categoryName = selectedProtocol.name,
                                itemName = cleanName,
                                probabilityScore = final.overallProbabilityScore,
                                confidenceScore = final.overallConfidenceScore,
                                verdictBand = final.verdictBand,
                                savedAtLabel = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date()),
                                reportText = report
                            )
                            scope.launch {
                                savedEntries = withContext(Dispatchers.IO) {
                                    InspectionStore.saveEntry(context, entry)
                                }
                                isSaved = true
                            }
                        }
                    ) {
                        Text(if (isSaved) "Saved Offline" else "Save Scorecard", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val report = InspectionStore.formatShareReport(
                                protocol = selectedProtocol,
                                itemName = cleanName,
                                markers = markers,
                                assessments = assessments,
                                scorecard = final
                            )
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Loupe Atelier Scorecard: $cleanName")
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(share, "Export Scorecard"))
                        }
                    ) {
                        Text("Share Report", fontSize = 12.sp)
                    }
                }

                HorizontalDivider()

                markers.forEachIndexed { idx, marker ->
                    val a = assessments[marker.id]
                    val thumb = thumbnails[marker.id]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2DDD2), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (thumb != null) {
                                Image(
                                    bitmap = thumb.asImageBitmap(),
                                    contentDescription = marker.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(88.dp)
                                        .aspectRatio(4f / 3f)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("0${marker.stepNumber}. ${marker.title}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                demoSampleUsed[marker.id]?.let { label ->
                                    Text(
                                        "DEMO SAMPLE · $label",
                                        fontSize = 11.sp,
                                        color = Color(0xFF9A6B2F),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (a != null) {
                                    Text(
                                        "Signal ${a.authenticityScore}/100 · Conf ${a.confidenceScore}/100",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "Box ${a.framingScore}% · Light ${a.lightingScore}% · Focus ${a.sharpnessScore}%",
                                        fontSize = 11.sp,
                                        color = Color(0xFF5C5850)
                                    )
                                }
                            }
                        }

                        if (a != null) {
                            a.confirmations.forEach { Text("· $it", fontSize = 12.sp, color = Color(0xFF1D6A4E)) }
                            a.anomalies.forEach { Text("· $it", fontSize = 12.sp, color = Color(0xFFB44332)) }
                            if (!a.qualityPassed && a.retakeAngleInstruction != null) {
                                Text(
                                    "Retake Guidance: ${a.retakeAngleInstruction}",
                                    color = Color(0xFFB44332),
                                    fontSize = 12.sp
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    scorecard = null
                                    activeStepIndex = idx
                                }
                            ) {
                                Text("Retake This Marker", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Text("Human Augmentation Checks", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                final.prioritizedHumanChecks.forEachIndexed { i, check ->
                    Text("0${i + 1}. $check", fontSize = 13.sp)
                }

                Text(final.disclaimer, fontSize = 12.sp, color = Color(0xFF5C5850))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        resetSessionState()
                        inSetup = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181715))
                ) {
                    Text("Start New Inspection")
                }
            } else {
                Text(
                    "Step 0${currentMarker.stepNumber} of 0${markers.size} · ${currentMarker.title}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(currentMarker.lightingRequirement, fontSize = 13.sp, color = Color(0xFF5C5850))
                Text(currentMarker.angleAndDistance, fontSize = 13.sp, color = Color(0xFF5C5850))

                if (!hasCameraPermission) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                        ) {
                            Text("Grant Camera Permission", fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { galleryLauncher.launch("image/*") }
                            ) {
                                Text("Gallery Photo", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { showDemoGallery = true }
                            ) {
                                Text("Demo Samples", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    MacroCameraViewfinder(
                        marker = currentMarker,
                        isAnalyzing = isAnalyzing,
                        onCaptured = { roiBitmap, metrics ->
                            runLocalAssessment(roiBitmap, metrics)
                        },
                        onUploadFromGallery = {
                            galleryLauncher.launch("image/*")
                        },
                        onOpenDemoSamples = {
                            showDemoGallery = true
                        }
                    )
                }

                errorMessage?.let {
                    Text(it, color = Color(0xFFB44332), fontSize = 13.sp)
                }

                currentAssessment?.let { assess ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2DDD2), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        demoSampleUsed[currentMarker.id]?.let { label ->
                            Text(
                                "DEMO SAMPLE · $label",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9A6B2F)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (currentThumb != null) {
                                Image(
                                    bitmap = currentThumb.asImageBitmap(),
                                    contentDescription = currentMarker.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(84.dp)
                                        .aspectRatio(4f / 3f)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Signal: ${assess.authenticityScore}/100 · Conf: ${assess.confidenceScore}/100",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Box ${assess.framingScore}% · Light ${assess.lightingScore}% · Focus ${assess.sharpnessScore}%",
                                    fontSize = 12.sp,
                                    color = Color(0xFF5C5850)
                                )
                            }
                        }

                        if (!assess.qualityPassed) {
                            Text(
                                "Retake Recommended: ${assess.retakeAngleInstruction ?: assess.qualityPenaltyReason}",
                                color = Color(0xFFB44332),
                                fontSize = 13.sp
                            )
                        }
                        assess.confirmations.forEach { Text("· $it", fontSize = 12.sp, color = Color(0xFF1D6A4E)) }
                        assess.anomalies.forEach { Text("· $it", fontSize = 12.sp, color = Color(0xFFB44332)) }
                        Text("Human Check: ${assess.humanJudgementNote}", fontSize = 12.sp, color = Color(0xFF5C5850))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        enabled = !isAnalyzing,
                        onClick = {
                            if (activeStepIndex > 0) activeStepIndex-- else inSetup = true
                        }
                    ) {
                        Text(if (activeStepIndex > 0) "Previous" else "Categories")
                    }
                    if (activeStepIndex < markers.lastIndex) {
                        Button(
                            enabled = !isAnalyzing,
                            onClick = { activeStepIndex++ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181715))
                        ) {
                            Text("Next Marker")
                        }
                    } else {
                        Button(
                            enabled = assessments.isNotEmpty() && !isAnalyzing,
                            onClick = {
                                scorecard = OnDeviceMacroAnalyzer.synthesizeScorecard(
                                    protocol = selectedProtocol,
                                    itemName = itemName.ifBlank { selectedProtocol.defaultItemName },
                                    assessments = markers.mapNotNull { assessments[it.id] }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D6A4E))
                        ) {
                            Text("Issue Final Scorecard")
                        }
                    }
                }
            }
        }
    }
}
