package com.loupeatelier.app.engine

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class DemoSampleKind {
    DEFECT,
    BENCHMARK,
    OPTICAL_FAIL
}

data class DemoSampleSpec(
    val id: String,
    val title: String,
    val subtitle: String,
    val targetReticleType: String,
    val kind: DemoSampleKind,
    val expectedNote: String
)

object DemoSampleGallery {

    val samples: List<DemoSampleSpec> = listOf(
        DemoSampleSpec(
            id = "demo-stitch-defect",
            title = "Irregular Straight Lock-Stitch",
            subtitle = "Flat 0° machine stitch with erratic spacing & stray punctures",
            targetReticleType = "stitch-line",
            kind = DemoSampleKind.DEFECT,
            expectedNote = "Triggers flat stitch orientation & irregular pitch spacing alerts"
        ),
        DemoSampleSpec(
            id = "demo-stitch-clean",
            title = "19° Hand Saddle-Stitch (Clean)",
            subtitle = "Consistent 19° waxed-linen slant & uniform awl pitch",
            targetReticleType = "stitch-line",
            kind = DemoSampleKind.BENCHMARK,
            expectedNote = "Passes optical gates with high stitch slant & spacing lock"
        ),
        DemoSampleSpec(
            id = "demo-typo-defect",
            title = "Foil-Bleed & Uneven Kerning Stamp",
            subtitle = "Soft haloing gold foil spill & irregular character gaps",
            targetReticleType = "typography-band",
            kind = DemoSampleKind.DEFECT,
            expectedNote = "Triggers letterform edge haloing & variable kerning alerts"
        ),
        DemoSampleSpec(
            id = "demo-typo-clean",
            title = "Crisp Gold-Foil House Stamp (Clean)",
            subtitle = "Sharp debossed serif edges & balanced spacing rhythm",
            targetReticleType = "typography-band",
            kind = DemoSampleKind.BENCHMARK,
            expectedNote = "Confirms clean foil containment & balanced inter-glyph gaps"
        ),
        DemoSampleSpec(
            id = "demo-hardware-defect",
            title = "Porous Cast Plating & Rough Screws",
            subtitle = "Isotropic surface grain, casting pinholes & skewed rivets",
            targetReticleType = "hardware-plate",
            kind = DemoSampleKind.DEFECT,
            expectedNote = "Triggers isotropic surface noise & casting porosity alerts"
        ),
        DemoSampleSpec(
            id = "demo-hardware-clean",
            title = "Brushed Brass Plaque & Pearling (Clean)",
            subtitle = "Directional satin brushing, symmetric rivets & zero pitting",
            targetReticleType = "hardware-plate",
            kind = DemoSampleKind.BENCHMARK,
            expectedNote = "Confirms directional brushing & clean non-porous surface"
        ),
        DemoSampleSpec(
            id = "demo-serial-defect",
            title = "Paint-Spill Edge & Soft Date Code",
            subtitle = "Uneven edge resin blobs spilling onto strap & blurred stamp",
            targetReticleType = "serial-tag",
            kind = DemoSampleKind.DEFECT,
            expectedNote = "Triggers abrupt edge-glazing variance & haloing alerts"
        ),
        DemoSampleSpec(
            id = "demo-serial-clean",
            title = "Clean Blindstamp & Satin Edge (Clean)",
            subtitle = "Crisp debossed year code with smooth multi-coat edge resin",
            targetReticleType = "serial-tag",
            kind = DemoSampleKind.BENCHMARK,
            expectedNote = "Confirms clean edge transition & crisp blindstamp spacing"
        ),
        DemoSampleSpec(
            id = "demo-optical-fail",
            title = "Blurry & Flash-Glare Shot (Retake Demo)",
            subtitle = "Out-of-focus leather with direct LED glare blowout",
            targetReticleType = "general",
            kind = DemoSampleKind.OPTICAL_FAIL,
            expectedNote = "Demonstrates quality gate blocking false high confidence"
        )
    )

    fun recommendedSampleFor(reticleType: String, preferDefect: Boolean): DemoSampleSpec {
        val desiredKind = if (preferDefect) DemoSampleKind.DEFECT else DemoSampleKind.BENCHMARK
        return samples.firstOrNull { it.targetReticleType == reticleType && it.kind == desiredKind }
            ?: samples.firstOrNull { it.kind == desiredKind }
            ?: samples.first()
    }

    fun renderSampleBitmap(sampleId: String, width: Int = 960, height: Int = 720): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        when (sampleId) {
            "demo-stitch-clean" -> drawStitchSample(canvas, width, height, isDefect = false)
            "demo-stitch-defect" -> drawStitchSample(canvas, width, height, isDefect = true)
            "demo-typo-clean" -> drawTypographySample(canvas, width, height, isDefect = false)
            "demo-typo-defect" -> drawTypographySample(canvas, width, height, isDefect = true)
            "demo-hardware-clean" -> drawHardwareSample(canvas, width, height, isDefect = false)
            "demo-hardware-defect" -> drawHardwareSample(canvas, width, height, isDefect = true)
            "demo-serial-clean" -> drawSerialSample(canvas, width, height, isDefect = false)
            "demo-serial-defect" -> drawSerialSample(canvas, width, height, isDefect = true)
            else -> drawOpticalFailSample(canvas, width, height)
        }
        return bmp
    }

    private fun drawCalfskinBackdrop(
        canvas: Canvas,
        w: Int,
        h: Int,
        baseColor: Int,
        grainAlpha: Int = 12,
        seed: Int = 42
    ) {
        canvas.drawColor(baseColor)
        val grad = Paint().apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                Color.argb(28, 255, 248, 232),
                Color.argb(32, 0, 0, 0),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), grad)

        val rng = Random(seed)
        val lightDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(grainAlpha, 255, 245, 225)
        }
        val darkDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(grainAlpha, 20, 14, 10)
        }
        val step = 10
        var y = 6
        while (y < h) {
            var x = 6
            while (x < w) {
                val jx = x + rng.nextInt(-3, 4)
                val jy = y + rng.nextInt(-3, 4)
                val r = rng.nextFloat() * 3.2f + 1.8f
                canvas.drawCircle(
                    jx.toFloat(),
                    jy.toFloat(),
                    r,
                    if (rng.nextBoolean()) lightDot else darkDot
                )
                x += step
            }
            y += step
        }
    }

    private fun drawStitchSample(canvas: Canvas, w: Int, h: Int, isDefect: Boolean) {
        drawCalfskinBackdrop(
            canvas, w, h,
            baseColor = Color.rgb(166, 112, 70),
            grainAlpha = 10,
            seed = if (isDefect) 109 else 101
        )

        val seamY = h * 0.50f
        // Subtle seam shadow crease
        val groovePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(28, 45, 24, 12)
            strokeWidth = 10f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(w * 0.08f, seamY, w * 0.92f, seamY, groovePaint)

        // Subtle panel edge stitching contrast along bottom edge of flap
        val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(122, 78, 46)
            strokeWidth = 5f
        }
        canvas.drawLine(w * 0.06f, h * 0.72f, w * 0.94f, h * 0.72f, edgePaint)

        val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(46, 26, 14)
        }
        val threadShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(145, 58, 36, 18)
            strokeWidth = 9.5f
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        val threadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDefect) Color.rgb(218, 208, 190) else Color.rgb(242, 232, 206)
            strokeWidth = if (isDefect) 6.5f else 7.8f
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(130, 255, 252, 240)
            strokeWidth = 2.4f
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }

        if (!isDefect) {
            // Crisp uniform 21° hand-saddle stitches across the seam
            val pitch = 54f
            var x = w * 0.12f
            while (x <= w * 0.85f) {
                val x1 = x
                val y1 = seamY + 9f
                val x2 = x + 38f
                val y2 = seamY - 7f
                canvas.drawCircle(x1, y1, 4.5f, holePaint)
                canvas.drawCircle(x2, y2, 4.5f, holePaint)
                canvas.drawLine(x1 + 1.5f, y1 + 3.0f, x2 + 1.5f, y2 + 3.0f, threadShadow)
                canvas.drawLine(x1, y1, x2, y2, threadPaint)
                canvas.drawLine(x1 + 3f, y1 - 1.5f, x2 - 3f, y2 - 1.5f, highlightPaint)
                x += pitch
            }
        } else {
            // Flat 0° straight lock-stitch with erratic gap spacing and slight vertical wander
            val irregularSteps = listOf(38f, 72f, 34f, 80f, 32f, 64f, 76f, 36f, 68f, 40f, 74f, 35f, 66f)
            val yOffsets = listOf(0f, -4f, 3f, -2f, 5f, -3f, 2f, -5f, 4f, 0f, -3f, 3f, -1f)
            var x = w * 0.11f
            var idx = 0
            while (x <= w * 0.86f) {
                val step = irregularSteps[idx % irregularSteps.size]
                val stitchLen = (step * 0.62f).coerceIn(20f, 46f)
                val dy = yOffsets[idx % yOffsets.size]
                val x1 = x
                val x2 = (x + stitchLen).coerceAtMost(w * 0.88f)
                val y = seamY + dy
                canvas.drawCircle(x1, y, 5.2f, holePaint)
                canvas.drawCircle(x2, y, 5.2f, holePaint)
                canvas.drawLine(x1, y + 2.5f, x2, y + 2.5f, threadShadow)
                canvas.drawLine(x1, y, x2, y, threadPaint)
                // Occasional stray double-puncture defect
                if (idx % 4 == 2) {
                    canvas.drawCircle(x1 + 9f, y + 8f, 3.8f, holePaint)
                }
                x += step
                idx++
            }
        }
    }

    private fun drawTypographySample(canvas: Canvas, w: Int, h: Int, isDefect: Boolean) {
        drawCalfskinBackdrop(
            canvas, w, h,
            baseColor = Color.rgb(86, 76, 68),
            grainAlpha = if (isDefect) 26 else 10,
            seed = if (isDefect) 209 else 201
        )

        // Subtle crisp leather pores in mid-band so overall optical sharpness stays real
        if (isDefect) {
            val poreRng = Random(277)
            val porePaint = Paint().apply { color = Color.rgb(52, 44, 38) }
            for (i in 0 until 450) {
                val px = poreRng.nextInt((w * 0.14).toInt(), (w * 0.86).toInt()).toFloat()
                val py = poreRng.nextInt((h * 0.18).toInt(), (h * 0.82).toInt()).toFloat()
                if (py < h * 0.34f || py > h * 0.66f) {
                    canvas.drawRect(px, py, px + 2.5f, py + 2.5f, porePaint)
                }
            }
        }

        if (!isDefect) {
            val debossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(28, 22, 18)
                textSize = 56f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                letterSpacing = 0.14f
                textAlign = Paint.Align.CENTER
            }
            val foilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(232, 196, 102)
                textSize = 56f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                letterSpacing = 0.14f
                textAlign = Paint.Align.CENTER
            }
            val subDeboss = Paint(debossPaint).apply { textSize = 32f; letterSpacing = 0.22f }
            val subFoil = Paint(foilPaint).apply { textSize = 32f; letterSpacing = 0.22f }

            canvas.drawText("LOUPE ATELIER", w / 2f, h * 0.45f + 2.5f, debossPaint)
            canvas.drawText("LOUPE ATELIER", w / 2f, h * 0.45f, foilPaint)
            canvas.drawText("PARIS · MADE IN FRANCE", w / 2f, h * 0.57f + 2f, subDeboss)
            canvas.drawText("PARIS · MADE IN FRANCE", w / 2f, h * 0.57f, subFoil)
        } else {
            // Sloppy over-heated gold foil bleed + uneven character kerning & baseline wobble
            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(190, 196, 152, 68)
                textSize = 58f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                maskFilter = BlurMaskFilter(9.5f, BlurMaskFilter.Blur.NORMAL)
            }
            val smearedFoil = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(228, 190, 92)
                textSize = 56f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                maskFilter = BlurMaskFilter(3.2f, BlurMaskFilter.Blur.NORMAL)
            }

            val glyphs = listOf("L", "O", "U", "P", "E", " ", "A", "T", "E", "L", "I", "E", "R")
            val xOffsets = listOf(190f, 232f, 294f, 344f, 384f, 428f, 456f, 514f, 558f, 616f, 660f, 694f, 742f)
            val yWobble = listOf(0f, -4f, 3f, -2f, 4f, 0f, -3f, 5f, -2f, 3f, -4f, 2f, 0f)
            for (i in glyphs.indices) {
                val ch = glyphs[i]
                val x = xOffsets[i]
                val y = h * 0.46f + yWobble[i]
                canvas.drawText(ch, x, y, haloPaint)
                canvas.drawText(ch, x, y, smearedFoil)
            }

            val subHalo = Paint(haloPaint).apply { textSize = 33f }
            val subSmear = Paint(smearedFoil).apply { textSize = 32f }
            canvas.drawText("PA RIS   · MADE  IN FRAN CE", w * 0.19f, h * 0.59f, subHalo)
            canvas.drawText("PA RIS   · MADE  IN FRAN CE", w * 0.19f, h * 0.59f, subSmear)
        }
    }

    private fun drawHardwareSample(canvas: Canvas, w: Int, h: Int, isDefect: Boolean) {
        // Dark matte backdrop around the centered plaque
        canvas.drawColor(Color.rgb(52, 48, 44))

        val plaqueRect = RectF(w * 0.16f, h * 0.24f, w * 0.84f, h * 0.76f)
        val rng = Random(if (isDefect) 309 else 301)

        if (!isDefect) {
            // Smooth brushed brass gradient + directional satin lines
            val brassGrad = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    plaqueRect.left, plaqueRect.top, plaqueRect.left, plaqueRect.bottom,
                    intArrayOf(Color.rgb(218, 186, 108), Color.rgb(196, 158, 78), Color.rgb(224, 196, 124)),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(plaqueRect, 20f, 20f, brassGrad)

            // Fine horizontal directional satin brushing lines
            val brushPaint = Paint().apply { strokeWidth = 1.8f }
            var y = plaqueRect.top.toInt() + 6
            while (y < plaqueRect.bottom.toInt() - 6) {
                val v = if (rng.nextBoolean()) Color.argb(26, 255, 248, 218) else Color.argb(24, 112, 82, 26)
                brushPaint.color = v
                canvas.drawLine(plaqueRect.left + 8f, y.toFloat(), plaqueRect.right - 8f, y.toFloat(), brushPaint)
                y += 3
            }

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(112, 84, 34)
                style = Paint.Style.STROKE
                strokeWidth = 5f
            }
            canvas.drawRoundRect(plaqueRect, 20f, 20f, borderPaint)

            // Symmetric domed pearling rivets
            listOf(w * 0.24f, w * 0.76f).forEach { cx ->
                val cy = h * 0.50f
                val rivetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        cx - 5f, cy - 5f, 28f,
                        Color.rgb(245, 224, 160),
                        Color.rgb(142, 108, 46),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(cx, cy, 24f, rivetPaint)
                canvas.drawCircle(cx, cy, 24f, borderPaint.apply { strokeWidth = 2.5f })
            }

            // Crisp centered engraving
            val engrave = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(74, 54, 18)
                textSize = 44f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                letterSpacing = 0.16f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("ATELIER", w / 2f, h * 0.52f, engrave)
        } else {
            // Rough cast zinc-alloy plate with isotropic granular noise + casting pit holes
            val castPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(190, 166, 114)
            }
            canvas.drawRoundRect(plaqueRect, 20f, 20f, castPaint)

            // Isotropic mottled casting noise (no directional brushing)
            val noisePaint = Paint()
            for (i in 0 until 2400) {
                val nx = rng.nextInt(plaqueRect.left.toInt() + 6, plaqueRect.right.toInt() - 6).toFloat()
                val ny = rng.nextInt(plaqueRect.top.toInt() + 6, plaqueRect.bottom.toInt() - 6).toFloat()
                noisePaint.color = if (rng.nextBoolean()) {
                    Color.argb(42, 240, 226, 185)
                } else {
                    Color.argb(45, 95, 76, 38)
                }
                canvas.drawRect(nx, ny, nx + 5f, ny + 5f, noisePaint)
            }

            // Isolated sharp 2x2px casting porosity pit pinholes that trigger microPittingCount
            val pitPaint = Paint().apply { color = Color.rgb(54, 42, 22) }
            for (i in 0 until 1500) {
                val px = rng.nextInt(plaqueRect.left.toInt() + 10, plaqueRect.right.toInt() - 10).toFloat()
                val py = rng.nextInt(plaqueRect.top.toInt() + 10, plaqueRect.bottom.toInt() - 10).toFloat()
                canvas.drawRect(px, py, px + 2.2f, py + 2.2f, pitPaint)
            }

            // Asymmetric slightly crooked hollow-looking screw heads
            val screwCenters = listOf(Pair(w * 0.23f, h * 0.48f), Pair(w * 0.77f, h * 0.53f))
            screwCenters.forEachIndexed { idx, (cx, cy) ->
                val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(120, 96, 48) }
                canvas.drawCircle(cx, cy, 24f, ring)
                val slot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(42, 34, 18)
                    strokeWidth = 6f
                }
                val angle = if (idx == 0) 0.22 else -0.45
                val dx = (cos(angle) * 20).toFloat()
                val dy = (sin(angle) * 20).toFloat()
                canvas.drawLine(cx - dx, cy - dy, cx + dx, cy + dy, slot)
            }

            // Soft wavy etching
            val wavyEngrave = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(88, 68, 30)
                textSize = 42f
                typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                letterSpacing = 0.08f
                textAlign = Paint.Align.CENTER
                maskFilter = BlurMaskFilter(2.5f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawText("ATE LIER", w * 0.515f, h * 0.51f, wavyEngrave)
        }
    }

    private fun drawSerialSample(canvas: Canvas, w: Int, h: Int, isDefect: Boolean) {
        drawCalfskinBackdrop(
            canvas, w, h,
            baseColor = Color.rgb(110, 90, 74),
            grainAlpha = 14,
            seed = if (isDefect) 409 else 401
        )

        if (!isDefect) {
            // Crisp smooth dark-chocolate resin edge coat on right edge band (x = 0.83..0.90)
            val resinEdge = Paint().apply { color = Color.rgb(54, 42, 34) }
            canvas.drawRect(w * 0.83f, h * 0.12f, w * 0.90f, h * 0.88f, resinEdge)

            val blindDeboss = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(42, 30, 22)
                textSize = 46f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.18f
                textAlign = Paint.Align.CENTER
            }
            val blindHighlight = Paint(blindDeboss).apply {
                color = Color.rgb(142, 118, 98)
            }
            canvas.drawText("U · 2024", w * 0.46f, h * 0.49f + 2f, blindHighlight)
            canvas.drawText("U · 2024", w * 0.46f, h * 0.49f, blindDeboss)
        } else {
            // Thick uneven wavy black-brown edge paint spilling into x = 0.80..0.92 band
            val rng = Random(444)
            val spillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(24, 18, 14) }
            val dripPath = Path().apply {
                moveTo(w * 0.92f, h * 0.12f)
                var y = h * 0.12f
                while (y <= h * 0.88f) {
                    val xSpill = w * (0.79f + rng.nextFloat() * 0.08f)
                    lineTo(xSpill, y)
                    y += 14f
                }
                lineTo(w * 0.92f, h * 0.88f)
                close()
            }
            canvas.drawPath(dripPath, spillPaint)

            // Extra sharp paint globs along the right strip
            val globLight = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(185, 165, 145) }
            var gy = h * 0.18f
            while (gy < h * 0.82f) {
                val gx = w * (0.81f + rng.nextFloat() * 0.07f)
                canvas.drawCircle(gx, gy, 7f, spillPaint)
                canvas.drawCircle(gx + 6f, gy, 3f, globLight)
                gy += 24f
            }

            // Soft smudged serial code with uneven spacing
            val smear = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(36, 26, 20)
                textSize = 46f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                maskFilter = BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawText("U   20  24", w * 0.23f, h * 0.50f, smear)
        }
    }

    private fun drawOpticalFailSample(canvas: Canvas, w: Int, h: Int) {
        canvas.drawColor(Color.rgb(154, 108, 72))
        // Intentionally defocused blurry threads
        val blurryStitch = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(225, 212, 188)
            strokeWidth = 12f
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(22f, BlurMaskFilter.Blur.NORMAL)
        }
        var x = w * 0.15f
        while (x < w * 0.82f) {
            canvas.drawLine(x, h * 0.52f, x + 36f, h * 0.48f, blurryStitch)
            x += 58f
        }
        // Harsh blown-out LED flash glare blob covering >22% of central box
        val glare = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w * 0.50f, h * 0.48f, w * 0.34f,
                intArrayOf(Color.rgb(255, 255, 255), Color.rgb(254, 254, 252), Color.argb(0, 255, 255, 255)),
                floatArrayOf(0f, 0.58f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(w * 0.50f, h * 0.48f, w * 0.34f, glare)
    }
}
