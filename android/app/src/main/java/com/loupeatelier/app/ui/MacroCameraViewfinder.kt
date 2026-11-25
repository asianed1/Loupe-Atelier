package com.loupeatelier.app.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.MotionEvent
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.loupeatelier.app.engine.OnDeviceMacroAnalyzer
import com.loupeatelier.app.model.ClientOpticalMetrics
import com.loupeatelier.app.model.MarkerSpec
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun MacroCameraViewfinder(
    marker: MarkerSpec,
    isAnalyzing: Boolean,
    onCaptured: (roiBitmap: Bitmap, metrics: ClientOpticalMetrics) -> Unit,
    onUploadFromGallery: () -> Unit,
    onOpenDemoSamples: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }

    val isSpecularSensitive = remember(marker.id) {
        marker.boundingBox.reticleType in setOf("hardware-plate", "dial-circle", "typography-band")
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    var zoomRatio by remember { mutableFloatStateOf(1.0f) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var autoLightEnabled by remember(marker.id) { mutableStateOf(!isSpecularSensitive) }
    var torchEnabled by remember { mutableStateOf(false) }
    var glareGuardWarning by remember { mutableStateOf<String?>(null) }
    var evStep by remember { mutableIntStateOf(0) } // -1, 0, +1 bias steps

    var liveBrightness by remember { mutableIntStateOf(128) }
    var liveSharpness by remember { mutableIntStateOf(0) }
    var liveGlareRatio by remember { mutableFloatStateOf(0f) }
    var isSteady by remember { mutableStateOf(true) }
    var telemetryTick by remember { mutableIntStateOf(0) }
    var focusTapOffset by remember { mutableStateOf<Offset?>(null) }
    var captureError by remember { mutableStateOf<String?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    var darkStreak by remember(marker.id) { mutableIntStateOf(0) }
    var glareStreak by remember(marker.id) { mutableIntStateOf(0) }
    var prevBrightness by remember { mutableIntStateOf(-1) }
    var prevSharpness by remember { mutableIntStateOf(-1) }

    DisposableEffect(Unit) {
        onDispose {
            camera?.cameraControl?.enableTorch(false)
            analysisExecutor.shutdown()
        }
    }

    // Center-focus automatically when switching sequential marker steps
    LaunchedEffect(marker.id, camera, previewViewRef) {
        val cam = camera ?: return@LaunchedEffect
        val pv = previewViewRef ?: return@LaunchedEffect
        if (pv.width > 0 && pv.height > 0) {
            val centerPoint = pv.meteringPointFactory.createPoint(pv.width / 2f, pv.height / 2f)
            val action = FocusMeteringAction.Builder(
                centerPoint,
                FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
            ).setAutoCancelDuration(4, TimeUnit.SECONDS).build()
            cam.cameraControl.startFocusAndMetering(action)
        }
        if (isSpecularSensitive && torchEnabled) {
            torchEnabled = false
            cam.cameraControl.enableTorch(false)
        }
        glareGuardWarning = null
    }

    // Smart Auto-Torch with Glare Guard & Hysteresis evaluated per frame tick
    LaunchedEffect(telemetryTick, autoLightEnabled, hasFlashUnit, marker.id) {
        if (telemetryTick == 0) return@LaunchedEffect
        val cam = camera ?: return@LaunchedEffect
        if (!hasFlashUnit || !autoLightEnabled) return@LaunchedEffect

        if (liveGlareRatio > 0.14f) {
            glareStreak++
            darkStreak = 0
            if (glareStreak >= 2 && torchEnabled) {
                torchEnabled = false
                cam.cameraControl.enableTorch(false)
                glareGuardWarning = "Glare detected — LED paused; tilt 15° or use diffused daylight."
            }
        } else if (liveBrightness < 56) {
            darkStreak++
            glareStreak = 0
            if (darkStreak >= 3 && !torchEnabled) {
                if (isSpecularSensitive) {
                    glareGuardWarning = "Low light on reflective surface — diffuse light rather than direct flash."
                } else {
                    torchEnabled = true
                    cam.cameraControl.enableTorch(true)
                    glareGuardWarning = null
                }
            }
        } else if (liveBrightness > 180 && torchEnabled) {
            darkStreak = 0
            torchEnabled = false
            cam.cameraControl.enableTorch(false)
        } else {
            darkStreak = 0
            glareStreak = 0
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(Color(0xFF111110))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    previewViewRef = previewView

                    // Tap-to-focus & AE metering directly on the macro target
                    previewView.setOnTouchListener { v, event ->
                        if (event.action == MotionEvent.ACTION_UP) {
                            val cam = camera
                            if (cam != null) {
                                val point = previewView.meteringPointFactory.createPoint(event.x, event.y)
                                val action = FocusMeteringAction.Builder(
                                    point,
                                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                                ).setAutoCancelDuration(4, TimeUnit.SECONDS).build()
                                cam.cameraControl.startFocusAndMetering(action)
                                focusTapOffset = Offset(event.x, event.y)
                            }
                            v.performClick()
                        }
                        true
                    }

                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        val provider = providerFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()
                        val analyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(analysisExecutor) { proxy ->
                                    try {
                                        val stats = computeYPlaneStats(proxy)
                                        mainExecutor.execute {
                                            val lumDelta = if (prevBrightness >= 0) abs(stats.brightness - prevBrightness) else 0
                                            val sharpDelta = if (prevSharpness >= 0) abs(stats.sharpness - prevSharpness) else 0
                                            prevBrightness = stats.brightness
                                            prevSharpness = stats.sharpness
                                            liveBrightness = stats.brightness
                                            liveSharpness = stats.sharpness
                                            liveGlareRatio = stats.glareRatio
                                            isSteady = lumDelta <= 7 && sharpDelta <= 12
                                            telemetryTick++
                                        }
                                    } finally {
                                        proxy.close()
                                    }
                                }
                            }

                        provider.unbindAll()
                        val boundCam = provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture,
                            analyzer
                        )
                        camera = boundCam
                        hasFlashUnit = boundCam.cameraInfo.hasFlashUnit()
                        imageCapture = capture

                        if (previewView.width > 0 && previewView.height > 0) {
                            val centerPoint = previewView.meteringPointFactory.createPoint(
                                previewView.width / 2f,
                                previewView.height / 2f
                            )
                            boundCam.cameraControl.startFocusAndMetering(
                                FocusMeteringAction.Builder(
                                    centerPoint,
                                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                                ).setAutoCancelDuration(4, TimeUnit.SECONDS).build()
                            )
                        }
                    }, mainExecutor)
                    previewView
                }
            )

            // Calibrated 4:3 Bounding-Box Reticle Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val boxW = size.width * 0.84f
                val boxH = boxW * 0.75f
                val left = (size.width - boxW) / 2f
                val top = (size.height - boxH) / 2f
                val brass = Color(0xFFE3B873)

                drawRect(
                    color = Color.White.copy(alpha = 0.48f),
                    topLeft = Offset(left, top),
                    size = Size(boxW, boxH),
                    style = Stroke(width = 2f)
                )

                val bracket = 28f
                drawLine(brass, Offset(left, top), Offset(left + bracket, top), strokeWidth = 5f)
                drawLine(brass, Offset(left, top), Offset(left, top + bracket), strokeWidth = 5f)
                drawLine(brass, Offset(left + boxW, top), Offset(left + boxW - bracket, top), strokeWidth = 5f)
                drawLine(brass, Offset(left + boxW, top), Offset(left + boxW, top + bracket), strokeWidth = 5f)
                drawLine(brass, Offset(left, top + boxH), Offset(left + bracket, top + boxH), strokeWidth = 5f)
                drawLine(brass, Offset(left, top + boxH), Offset(left, top + boxH - bracket), strokeWidth = 5f)
                drawLine(brass, Offset(left + boxW, top + boxH), Offset(left + boxW - bracket, top + boxH), strokeWidth = 5f)
                drawLine(brass, Offset(left + boxW, top + boxH), Offset(left + boxW, top + boxH - bracket), strokeWidth = 5f)

                if (marker.boundingBox.reticleType == "dial-circle") {
                    drawCircle(
                        color = brass.copy(alpha = 0.85f),
                        radius = boxH * 0.44f,
                        center = Offset(size.width / 2f, size.height / 2f),
                        style = Stroke(width = 2.5f)
                    )
                } else if (marker.boundingBox.reticleType == "stitch-line") {
                    drawLine(
                        color = brass.copy(alpha = 0.55f),
                        start = Offset(left + 16f, size.height / 2f),
                        end = Offset(left + boxW - 16f, size.height / 2f),
                        strokeWidth = 1.5f
                    )
                }

                focusTapOffset?.let { tap ->
                    drawCircle(
                        color = brass,
                        radius = 28f,
                        center = tap,
                        style = Stroke(width = 2.5f)
                    )
                }
            }

            Text(
                text = "Tap frame to focus · Fill ${marker.boundingBox.targetFillMinPercent}–${marker.boundingBox.targetFillMaxPercent}%",
                color = Color.White,
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Real-time optical readiness bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFAF8F4))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val lightLabel = when {
                liveGlareRatio > 0.14f -> "Glare Risk"
                liveBrightness < 56 -> "Low Light ($liveBrightness)"
                liveBrightness > 218 -> "Overexposed ($liveBrightness)"
                else -> "Light OK ($liveBrightness)"
            }
            val focusLabel = if (liveSharpness < 24) "Soft Focus ($liveSharpness%)" else "Sharp ($liveSharpness%)"
            val steadyLabel = if (isSteady) "Steady" else "Hold Still"

            Text(lightLabel, fontSize = 11.sp, color = Color(0xFF181715))
            Text(focusLabel, fontSize = 11.sp, color = Color(0xFF181715))
            Text(steadyLabel, fontSize = 11.sp, color = if (isSteady) Color(0xFF1D6A4E) else Color(0xFFB44332))
        }

        glareGuardWarning?.let {
            Text(
                text = it,
                fontSize = 11.sp,
                color = Color(0xFF9A6B2F),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        captureError?.let {
            Text(
                text = it,
                fontSize = 11.sp,
                color = Color(0xFFB44332),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // Row 1: Optical tuning controls (Zoom, EV Bias, Auto/Manual Light)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(1.0f, 2.0f).forEach { z ->
                OutlinedButton(
                    onClick = {
                        zoomRatio = z
                        camera?.cameraControl?.setZoomRatio(z)
                    }
                ) {
                    Text("${z.toInt()}x", fontSize = 11.sp)
                }
            }

            OutlinedButton(
                onClick = {
                    val cam = camera ?: return@OutlinedButton
                    val expState = cam.cameraInfo.exposureState
                    if (!expState.isExposureCompensationSupported) return@OutlinedButton
                    val nextStep = when (evStep) {
                        0 -> -1
                        -1 -> 1
                        else -> 0
                    }
                    evStep = nextStep
                    val range = expState.exposureCompensationRange
                    val targetIdx = (nextStep * 2).coerceIn(range.lower, range.upper)
                    cam.cameraControl.setExposureCompensationIndex(targetIdx)
                }
            ) {
                val label = if (evStep > 0) "+EV" else if (evStep < 0) "-EV" else "0 EV"
                Text(label, fontSize = 11.sp)
            }

            if (hasFlashUnit) {
                OutlinedButton(
                    onClick = {
                        autoLightEnabled = false
                        val next = !torchEnabled
                        torchEnabled = next
                        camera?.cameraControl?.enableTorch(next)
                        glareGuardWarning = null
                    }
                ) {
                    Text(
                        text = if (torchEnabled) "LED On" else if (autoLightEnabled) "Auto LED" else "LED Off",
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Row 2: Photo Gallery & Built-in Demo Sample Gallery
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = !isAnalyzing && !isCapturing,
                onClick = onUploadFromGallery
            ) {
                Text("Gallery Photo", fontSize = 11.sp)
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = !isAnalyzing && !isCapturing,
                onClick = onOpenDemoSamples
            ) {
                Text("Demo Samples", fontSize = 11.sp)
            }
        }

        // Row 2: Primary Shutter Button
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            enabled = !isAnalyzing && !isCapturing && imageCapture != null,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181715)),
            onClick = {
                val cap = imageCapture ?: return@Button
                isCapturing = true
                captureError = null
                val snapBrightness = liveBrightness
                val snapSharpness = liveSharpness
                val snapZoom = zoomRatio
                cap.takePicture(
                    analysisExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            try {
                                val rawBytes = image.planes[0].buffer.let { buf ->
                                    ByteArray(buf.remaining()).also { buf.get(it) }
                                }
                                val rotation = image.imageInfo.rotationDegrees.toFloat()
                                val decoded = decodeSampledBitmap(rawBytes, maxDim = 1600)

                                if (decoded == null) {
                                    mainExecutor.execute {
                                        captureError = "Could not decode captured macro frame."
                                        isCapturing = false
                                    }
                                    return
                                }

                                val oriented = if (rotation != 0f) {
                                    val matrix = Matrix().apply { postRotate(rotation) }
                                    Bitmap.createBitmap(
                                        decoded,
                                        0,
                                        0,
                                        decoded.width,
                                        decoded.height,
                                        matrix,
                                        true
                                    ).also {
                                        if (it !== decoded) decoded.recycle()
                                    }
                                } else {
                                    decoded
                                }

                                val roiBitmap = OnDeviceMacroAnalyzer.cropToBoundingBoxRoi(oriented)
                                if (roiBitmap !== oriented) oriented.recycle()

                                val metrics = ClientOpticalMetrics(
                                    averageBrightness = snapBrightness,
                                    sharpnessIndex = snapSharpness,
                                    width = roiBitmap.width,
                                    height = roiBitmap.height,
                                    zoomLevel = snapZoom
                                )
                                mainExecutor.execute {
                                    isCapturing = false
                                    onCaptured(roiBitmap, metrics)
                                }
                            } catch (t: Throwable) {
                                mainExecutor.execute {
                                    captureError = t.message ?: "Frame processing failed."
                                    isCapturing = false
                                }
                            } finally {
                                image.close()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            mainExecutor.execute {
                                captureError = exception.message ?: "Camera capture failed."
                                isCapturing = false
                            }
                        }
                    }
                )
            }
        ) {
            Text(
                text = when {
                    isCapturing -> "Locking Macro Frame..."
                    isAnalyzing -> "Analyzing Macro Markers..."
                    else -> "Capture & Score Bounding Box"
                }
            )
        }
    }
}

private data class YPlaneStats(
    val brightness: Int,
    val sharpness: Int,
    val glareRatio: Float
)

private fun computeYPlaneStats(proxy: ImageProxy): YPlaneStats {
    val plane = proxy.planes.firstOrNull() ?: return YPlaneStats(128, 50, 0f)
    val buffer = plane.buffer
    val limit = buffer.limit()
    val w = proxy.width
    val h = proxy.height
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride

    var sum = 0L
    var glareCount = 0
    var count = 0
    for (y in (h / 8) until (7 * h / 8) step 4) {
        val rowOffset = y * rowStride
        for (x in (w / 8) until (7 * w / 8) step 4) {
            val idx = rowOffset + x * pixelStride
            if (idx in 0 until limit) {
                val yVal = buffer.get(idx).toInt() and 0xFF
                sum += yVal
                if (yVal > 244) glareCount++
                count++
            }
        }
    }
    val brightness = if (count > 0) (sum / count).toInt() else 128
    val glareRatio = if (count > 0) glareCount.toFloat() / count else 0f

    var lapSum = 0.0
    var lapSqSum = 0.0
    var lapCount = 0
    for (y in (h / 5) until (4 * h / 5) step 4) {
        val rowOffset = y * rowStride
        val upRow = (y - 1) * rowStride
        val downRow = (y + 1) * rowStride
        for (x in (w / 5) until (4 * w / 5) step 4) {
            val colOffset = x * pixelStride
            val idx = rowOffset + colOffset
            val up = upRow + colOffset
            val down = downRow + colOffset
            val left = rowOffset + (x - 1) * pixelStride
            val right = rowOffset + (x + 1) * pixelStride
            if (up >= 0 && down < limit && left >= 0 && right < limit) {
                val c = buffer.get(idx).toInt() and 0xFF
                val lap = (buffer.get(up).toInt() and 0xFF) +
                    (buffer.get(down).toInt() and 0xFF) +
                    (buffer.get(left).toInt() and 0xFF) +
                    (buffer.get(right).toInt() and 0xFF) - 4 * c
                lapSum += lap
                lapSqSum += lap * lap
                lapCount++
            }
        }
    }
    val mean = if (lapCount > 0) lapSum / lapCount else 0.0
    val variance = if (lapCount > 0) (lapSqSum / lapCount - mean * mean).coerceAtLeast(0.0) else 0.0
    val sharpness = ((sqrt(variance) / 30.0) * 100.0).roundToInt().coerceIn(0, 100)
    return YPlaneStats(brightness, sharpness, glareRatio)
}

private fun decodeSampledBitmap(bytes: ByteArray, maxDim: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    var inSampleSize = 1
    while (bounds.outWidth / inSampleSize > maxDim || bounds.outHeight / inSampleSize > maxDim) {
        inSampleSize *= 2
    }
    val opts = BitmapFactory.Options().apply {
        this.inSampleSize = inSampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
}
