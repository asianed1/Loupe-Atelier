package com.loupeatelier.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loupeatelier.app.model.MarkerSpec

private data class BlueprintTarget(
    val leftFrac: Float,
    val topFrac: Float,
    val widthFrac: Float,
    val heightFrac: Float,
    val aimLabel: String
)

private fun resolveTargetZone(protocolId: String, marker: MarkerSpec): BlueprintTarget {
    return when (marker.id) {
        // Heritage & Quilted Handbags
        "heritage-stitching" -> BlueprintTarget(0.27f, 0.36f, 0.46f, 0.16f, "Top flap stitching seam")
        "heritage-typography" -> BlueprintTarget(0.36f, 0.52f, 0.28f, 0.14f, "Brand stamp below flap")
        "heritage-hardware" -> BlueprintTarget(0.41f, 0.62f, 0.18f, 0.17f, "Front clasp & metal rivets")
        "heritage-serial" -> BlueprintTarget(0.69f, 0.44f, 0.14f, 0.28f, "Inner side tab & edge trim")

        "quilted-stitching" -> BlueprintTarget(0.24f, 0.42f, 0.52f, 0.16f, "Quilted flap seam alignment")
        "quilted-typography" -> BlueprintTarget(0.36f, 0.32f, 0.28f, 0.14f, "Interior flap logo stamp")
        "quilted-hardware" -> BlueprintTarget(0.41f, 0.58f, 0.18f, 0.17f, "Front turnlock clasp")
        "quilted-serial" -> BlueprintTarget(0.64f, 0.72f, 0.17f, 0.15f, "Lower inner corner serial tag")

        // Watches
        "watch-dial", "chrono-dial" -> BlueprintTarget(0.34f, 0.26f, 0.32f, 0.48f, "Dial face, markers & hands")
        "watch-clasp" -> BlueprintTarget(0.68f, 0.66f, 0.24f, 0.24f, "Bracelet clasp engraving")
        "watch-rehaut" -> BlueprintTarget(0.42f, 0.62f, 0.26f, 0.18f, "Inner bezel ring & side crown")
        "chrono-case" -> BlueprintTarget(0.60f, 0.34f, 0.16f, 0.32f, "Case edge, pushers & caseback")
        "chrono-strap" -> BlueprintTarget(0.36f, 0.78f, 0.28f, 0.18f, "Strap buckle & keeper loop")

        // Sneakers
        "footwear-stitching" -> BlueprintTarget(0.34f, 0.58f, 0.38f, 0.18f, "Side stitching & sole seam")
        "footwear-typography" -> BlueprintTarget(0.28f, 0.24f, 0.22f, 0.22f, "Tongue or insole logo label")
        "footwear-hardware" -> BlueprintTarget(0.66f, 0.36f, 0.20f, 0.24f, "Eyelets or back heel tab")

        // Jewelry
        "jewelry-hallmark" -> BlueprintTarget(0.35f, 0.22f, 0.30f, 0.18f, "Inside purity & maker stamp")
        "jewelry-clasp" -> BlueprintTarget(0.66f, 0.40f, 0.18f, 0.24f, "Clasp hinge & screw slots")
        "jewelry-bevels" -> BlueprintTarget(0.20f, 0.42f, 0.20f, 0.24f, "Polished edge & motif bevel")

        // Belts
        "belts-typography" -> BlueprintTarget(0.42f, 0.40f, 0.28f, 0.20f, "Stamped text inside leather strap")
        "belts-hardware" -> BlueprintTarget(0.14f, 0.24f, 0.26f, 0.52f, "Metal buckle front & back")
        "belts-stitch" -> BlueprintTarget(0.68f, 0.38f, 0.22f, 0.24f, "Strap edge stitching & holes")

        // Scarves / Textiles
        "textiles-edge" -> BlueprintTarget(0.18f, 0.72f, 0.36f, 0.16f, "Hand-rolled hem edge")
        "textiles-caretag" -> BlueprintTarget(0.64f, 0.66f, 0.18f, 0.20f, "Sewn corner care label")
        "textiles-print" -> BlueprintTarget(0.24f, 0.22f, 0.40f, 0.34f, "Printed corner pattern & signature")

        // Outerwear
        "outerwear-labels" -> BlueprintTarget(0.41f, 0.16f, 0.18f, 0.18f, "Inside neck label or care tag")
        "outerwear-hardware" -> BlueprintTarget(0.44f, 0.40f, 0.14f, 0.24f, "Main zipper pull or snap button")
        "outerwear-stitch" -> BlueprintTarget(0.20f, 0.36f, 0.16f, 0.22f, "Sleeve crest or placket seam")

        // Eyewear
        "eyewear-typography" -> BlueprintTarget(0.56f, 0.40f, 0.28f, 0.20f, "Printed text inside temple arm")
        "eyewear-hinge" -> BlueprintTarget(0.47f, 0.36f, 0.14f, 0.24f, "Metal hinge & screw joint")
        "eyewear-bridge" -> BlueprintTarget(0.26f, 0.38f, 0.14f, 0.22f, "Center nose bridge & frame edge")

        // Luggage
        "luggage-hardware" -> BlueprintTarget(0.66f, 0.36f, 0.18f, 0.22f, "TSA latch lock & corner rivets")
        "luggage-serial" -> BlueprintTarget(0.22f, 0.52f, 0.18f, 0.22f, "Hinge rail serial plate")
        "luggage-trim" -> BlueprintTarget(0.36f, 0.12f, 0.28f, 0.18f, "Top leather handle grip or wheel")

        // Writing Instruments
        "writing-nib" -> BlueprintTarget(0.10f, 0.36f, 0.22f, 0.28f, "Metal nib top engraving & slit")
        "writing-serial" -> BlueprintTarget(0.54f, 0.34f, 0.16f, 0.32f, "Cap band & clip ring engraving")
        "writing-barrel" -> BlueprintTarget(0.32f, 0.38f, 0.22f, 0.24f, "Barrel thread seam & polish")

        else -> when (marker.boundingBox.reticleType) {
            "stitch-line" -> BlueprintTarget(0.26f, 0.40f, 0.48f, 0.18f, "Close-up of seam stitching")
            "typography-band" -> BlueprintTarget(0.32f, 0.38f, 0.36f, 0.20f, "Stamped or printed brand logo")
            "hardware-plate" -> BlueprintTarget(0.38f, 0.48f, 0.24f, 0.26f, "Metal clasp, buckle, or zipper")
            "serial-tag" -> BlueprintTarget(0.62f, 0.56f, 0.22f, 0.24f, "Serial tag & adjacent edge trim")
            "dial-circle" -> BlueprintTarget(0.34f, 0.26f, 0.32f, 0.48f, "Centered watch dial face")
            else -> BlueprintTarget(0.34f, 0.34f, 0.32f, 0.32f, "Key craftsmanship detail")
        }
    }
}

private fun baseCategoryId(protocolId: String): String {
    return protocolId.removePrefix("adaptive-")
}

@Composable
fun ItemBlueprintLocator(
    protocolId: String,
    marker: MarkerSpec,
    modifier: Modifier = Modifier
) {
    val category = baseCategoryId(protocolId)
    val target = resolveTargetZone(protocolId, marker)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFAF8F4), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2DDD2), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier
                .width(124.dp)
                .height(86.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFEAE5DA), RoundedCornerShape(8.dp))
        ) {
            val w = size.width
            val h = size.height
            val ink = Color(0xFF2B2825)
            val faintInk = Color(0xFF9C968B)

            when (category) {
                "handbag-heritage", "handbag-quilted" -> drawHandbagSilhouette(w, h, ink, faintInk, quilted = category == "handbag-quilted")
                "watch-dive", "watch-chrono" -> drawWatchSilhouette(w, h, ink, faintInk, chrono = category == "watch-chrono")
                "footwear" -> drawSneakerSilhouette(w, h, ink, faintInk)
                "jewelry" -> drawJewelrySilhouette(w, h, ink, faintInk)
                "belts" -> drawBeltSilhouette(w, h, ink, faintInk)
                "textiles" -> drawScarfSilhouette(w, h, ink, faintInk)
                "outerwear" -> drawJacketSilhouette(w, h, ink, faintInk)
                "eyewear" -> drawEyewearSilhouette(w, h, ink, faintInk)
                "luggage" -> drawLuggageSilhouette(w, h, ink, faintInk)
                "writing" -> drawPenSilhouette(w, h, ink, faintInk)
                else -> drawGenericObjectSilhouette(w, h, ink, faintInk)
            }

            // Bright Red Target Box highlighting exact shoot zone
            val red = Color(0xFFD92D20)
            val rx = w * target.leftFrac
            val ry = h * target.topFrac
            val rw = w * target.widthFrac
            val rh = h * target.heightFrac

            val radiusPx = 4.dp.toPx()
            drawRoundRect(
                color = red.copy(alpha = 0.14f),
                topLeft = Offset(rx, ry),
                size = Size(rw, rh),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
            drawRoundRect(
                color = red,
                topLeft = Offset(rx, ry),
                size = Size(rw, rh),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                style = Stroke(width = 2.4.dp.toPx())
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "WHERE TO AIM (RED BOX)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD92D20)
            )
            Text(
                text = target.aimLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF181715)
            )
            Text(
                text = "Hold phone 6–10 cm away so this part fills the camera box.",
                fontSize = 11.sp,
                color = Color(0xFF5C5850)
            )
        }
    }
}

private fun DrawScope.drawHandbagSilhouette(
    w: Float,
    h: Float,
    ink: Color,
    faint: Color,
    quilted: Boolean
) {
    val mainW = 1.8.dp.toPx()
    val faintW = 1.dp.toPx()
    val stroke = Stroke(width = mainW, join = StrokeJoin.Round, cap = StrokeCap.Round)
    val handle = Path().apply {
        moveTo(w * 0.36f, h * 0.30f)
        cubicTo(w * 0.36f, h * 0.12f, w * 0.64f, h * 0.12f, w * 0.64f, h * 0.30f)
    }
    drawPath(handle, ink, style = stroke)

    val body = Path().apply {
        moveTo(w * 0.24f, h * 0.30f)
        lineTo(w * 0.76f, h * 0.30f)
        lineTo(w * 0.80f, h * 0.86f)
        lineTo(w * 0.20f, h * 0.86f)
        close()
    }
    drawPath(body, ink, style = stroke)

    val flap = Path().apply {
        moveTo(w * 0.24f, h * 0.30f)
        lineTo(w * 0.22f, h * 0.64f)
        lineTo(w * 0.78f, h * 0.64f)
        lineTo(w * 0.76f, h * 0.30f)
    }
    drawPath(flap, ink, style = stroke)

    if (quilted) {
        for (i in 1..3) {
            val x = w * (0.24f + i * 0.13f)
            drawLine(faint, Offset(x, h * 0.32f), Offset(x + w * 0.08f, h * 0.62f), strokeWidth = faintW)
            drawLine(faint, Offset(x + w * 0.08f, h * 0.32f), Offset(x, h * 0.62f), strokeWidth = faintW)
        }
    } else {
        drawLine(faint, Offset(w * 0.28f, h * 0.42f), Offset(w * 0.72f, h * 0.42f), strokeWidth = faintW)
    }

    drawRoundRect(
        color = ink,
        topLeft = Offset(w * 0.46f, h * 0.60f),
        size = Size(w * 0.08f, h * 0.10f),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        style = stroke
    )
}

private fun DrawScope.drawWatchSilhouette(w: Float, h: Float, ink: Color, faint: Color, chrono: Boolean) {
    val mainW = 1.8.dp.toPx()
    val faintW = 1.dp.toPx()
    val stroke = Stroke(width = mainW, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawRect(ink, Offset(w * 0.40f, h * 0.10f), Size(w * 0.20f, h * 0.18f), style = stroke)
    drawRect(ink, Offset(w * 0.40f, h * 0.72f), Size(w * 0.20f, h * 0.18f), style = stroke)
    drawCircle(ink, radius = h * 0.25f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
    drawCircle(faint, radius = h * 0.19f, center = Offset(w * 0.50f, h * 0.50f), style = Stroke(faintW))
    drawRect(ink, Offset(w * 0.66f, h * 0.45f), Size(w * 0.05f, h * 0.10f), style = stroke)
    if (chrono) {
        drawRect(ink, Offset(w * 0.64f, h * 0.32f), Size(w * 0.04f, h * 0.06f), style = stroke)
        drawRect(ink, Offset(w * 0.64f, h * 0.62f), Size(w * 0.04f, h * 0.06f), style = stroke)
        drawCircle(faint, radius = h * 0.05f, center = Offset(w * 0.45f, h * 0.50f), style = Stroke(faintW))
        drawCircle(faint, radius = h * 0.05f, center = Offset(w * 0.55f, h * 0.50f), style = Stroke(faintW))
    } else {
        drawRect(faint, Offset(w * 0.71f, h * 0.70f), Size(w * 0.18f, h * 0.16f), style = Stroke(faintW))
    }
    drawLine(ink, Offset(w * 0.50f, h * 0.50f), Offset(w * 0.50f, h * 0.37f), strokeWidth = mainW, cap = StrokeCap.Round)
    drawLine(ink, Offset(w * 0.50f, h * 0.50f), Offset(w * 0.58f, h * 0.53f), strokeWidth = mainW * 0.85f, cap = StrokeCap.Round)
}

private fun DrawScope.drawSneakerSilhouette(w: Float, h: Float, ink: Color, faint: Color) {
    val mainW = 1.8.dp.toPx()
    val faintW = 1.dp.toPx()
    val stroke = Stroke(width = mainW, join = StrokeJoin.Round, cap = StrokeCap.Round)
    val shoe = Path().apply {
        moveTo(w * 0.14f, h * 0.76f)
        lineTo(w * 0.84f, h * 0.76f)
        lineTo(w * 0.86f, h * 0.64f)
        lineTo(w * 0.80f, h * 0.40f)
        lineTo(w * 0.66f, h * 0.42f)
        lineTo(w * 0.48f, h * 0.26f)
        lineTo(w * 0.40f, h * 0.34f)
        lineTo(w * 0.22f, h * 0.56f)
        lineTo(w * 0.14f, h * 0.64f)
        close()
    }
    drawPath(shoe, ink, style = stroke)
    drawLine(faint, Offset(w * 0.15f, h * 0.67f), Offset(w * 0.85f, h * 0.67f), strokeWidth = faintW)
    drawLine(faint, Offset(w * 0.44f, h * 0.38f), Offset(w * 0.52f, h * 0.42f), strokeWidth = faintW)
    drawLine(faint, Offset(w * 0.39f, h * 0.45f), Offset(w * 0.47f, h * 0.49f), strokeWidth = faintW)
}

private fun DrawScope.drawJewelrySilhouette(w: Float, h: Float, ink: Color, faint: Color) {
    val mainW = 1.8.dp.toPx()
    val faintW = 1.dp.toPx()
    drawOval(ink, Offset(w * 0.20f, h * 0.24f), Size(w * 0.60f, h * 0.52f), style = Stroke(width = mainW))
    drawOval(faint, Offset(w * 0.27f, h * 0.31f), Size(w * 0.46f, h * 0.38f), style = Stroke(width = faintW))
    drawCircle(ink, radius = 3.dp.toPx(), center = Offset(w * 0.75f, h * 0.50f), style = Stroke(1.3.dp.toPx()))
    drawCircle(ink, radius = 3.dp.toPx(), center = Offset(w * 0.25f, h * 0.50f), style = Stroke(1.3.dp.toPx()))
}

private fun DrawScope.drawBeltSilhouette(w: Float, h: Float, ink: Color, faint: Color) {
    val mainW = 1.8.dp.toPx()
    val stroke = Stroke(width = mainW, join = StrokeJoin.Round)
    val cr = 4.dp.toPx()
    drawRoundRect(ink, Offset(w * 0.28f, h * 0.38f), Size(w * 0.60f, h * 0.24f), CornerRadius(cr, cr), style = stroke)
    drawRoundRect(ink, Offset(w * 0.16f, h * 0.30f), Size(w * 0.18f, h * 0.40f), CornerRadius(cr, cr), style = stroke)
    listOf(0.72f, 0.78f, 0.83f).forEach { fx ->
        drawCircle(faint, radius = 1.8.dp.toPx(), center = Offset(w * fx, h * 0.50f))
    }
}

private fun DrawScope.drawScarfSilhouette(w: Float, h: Float, ink: Color, faint: Color) {
    val mainW = 1.8.dp.toPx()
    val faintW = 1.dp.toPx()
    drawRect(ink, Offset(w * 0.22f, h * 0.14f), Size(w * 0.56f, h * 0.72f), style = Stroke(width = mainW, join = StrokeJoin.Round))
    drawRect(faint, Offset(w * 0.28f, h * 0.21f), Size(w * 0.44f, h * 0.58f), style = Stroke(width = faintW))
    drawRect(ink, Offset(w * 0.66f, h * 0.69f), Size(w * 0.10f, h * 0.14f), style = Stroke(width = 1.3.dp.toPx()))
}

private fun DrawScope.drawJacketSilhouette(w: Float, h: Float, ink: Color, faint: Color) {
    val mainW = 1.8.dp.toPx()
    val faintW = 1.1.dp.toPx()
    val stroke = Stroke(width = mainW, join = StrokeJoin.Round, cap = StrokeCap.Round)
    val coat = Path().apply {
        moveTo(w * 0.38f, h * 0.16f)
        lineTo(w * 0.62f, h * 0.16f)
        lineTo(w * 0.80f, h * 0.36f)
        lineTo(w * 0.74f, h * 0.74f)
        lineTo(w * 0.66f, h * 0.72f)
        lineTo(w * 0.66f, h * 0.86f)
        lineTo(w * 0.34f, h * 0.86f)
        lineTo(w * 0.34f, h * 0.72f)
        lineTo(w * 0.26f, h * 0.74f)
        lineTo(w * 0.20f, h * 0.36f)
        close()
    }
    drawPath(coat, ink, style = stroke)
    drawLine(faint, Offset(w * 0.50f, h * 0.22f), Offset(w * 0.50f, h * 0.86f), strokeWidth = faintW)
}

private fun DrawScope.drawEyewearSilhouette(w: Float, h: Float, ink: Color, faint: Color) {
    val mainW = 1.8.dp.toPx()
    val faintW = 1.dp.toPx()
    val stroke = Stroke(width = mainW, join = StrokeJoin.Round, cap = StrokeCap.Round)
    val cr = 6.dp.toPx()
    drawRoundRect(ink, Offset(w * 0.16f, h * 0.34f), Size(w * 0.20f, h * 0.32f), CornerRadius(cr, cr), style = stroke)
    drawRoundRect(ink, Offset(w * 0.40f, h * 0.34f), Size(w * 0.20f, h * 0.32f), CornerRadius(cr, cr), style = stroke)
    drawLine(ink, Offset(w * 0.36f, h * 0.42f), Offset(w * 0.40f, h * 0.42f), strokeWidth = mainW)
    drawLine(ink, Offset(w * 0.60f, h * 0.42f), Offset(w * 0.86f, h * 0.50f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    drawLine(faint, Offset(w * 0.60f, h * 0.48f), Offset(w * 0.82f, h * 0.54f), strokeWidth = faintW)
}

private fun DrawScope.drawLuggageSilhouette(w: Float, h: Float, ink: Color, faint: Color) {
    val mainW = 1.8.dp.toPx()
    val faintW = 1.dp.toPx()
    val stroke = Stroke(width = mainW, join = StrokeJoin.Round)
    val cr = 4.dp.toPx()
    drawRect(ink, Offset(w * 0.42f, h * 0.12f), Size(w * 0.16f, h * 0.12f), style = stroke)
    drawRoundRect(ink, Offset(w * 0.28f, h * 0.24f), Size(w * 0.44f, h * 0.58f), CornerRadius(cr, cr), style = stroke)
    drawLine(faint, Offset(w * 0.38f, h * 0.27f), Offset(w * 0.38f, h * 0.79f), strokeWidth = faintW)
    drawLine(faint, Offset(w * 0.50f, h * 0.27f), Offset(w * 0.50f, h * 0.79f), strokeWidth = faintW)
    drawLine(faint, Offset(w * 0.62f, h * 0.27f), Offset(w * 0.62f, h * 0.79f), strokeWidth = faintW)
    drawCircle(ink, radius = 3.dp.toPx(), center = Offset(w * 0.34f, h * 0.87f), style = Stroke(1.4.dp.toPx()))
    drawCircle(ink, radius = 3.dp.toPx(), center = Offset(w * 0.66f, h * 0.87f), style = Stroke(1.4.dp.toPx()))
}

private fun DrawScope.drawPenSilhouette(w: Float, h: Float, ink: Color, faint: Color) {
    val mainW = 1.8.dp.toPx()
    val faintW = 1.dp.toPx()
    val stroke = Stroke(width = mainW, join = StrokeJoin.Round, cap = StrokeCap.Round)
    val cr = 6.dp.toPx()
    drawRoundRect(ink, Offset(w * 0.30f, h * 0.38f), Size(w * 0.54f, h * 0.24f), CornerRadius(cr, cr), style = stroke)
    val nib = Path().apply {
        moveTo(w * 0.30f, h * 0.42f)
        lineTo(w * 0.14f, h * 0.50f)
        lineTo(w * 0.30f, h * 0.58f)
        close()
    }
    drawPath(nib, ink, style = stroke)
    drawLine(faint, Offset(w * 0.18f, h * 0.50f), Offset(w * 0.28f, h * 0.50f), strokeWidth = faintW)
    drawLine(ink, Offset(w * 0.58f, h * 0.38f), Offset(w * 0.58f, h * 0.62f), strokeWidth = mainW)
    drawLine(faint, Offset(w * 0.60f, h * 0.34f), Offset(w * 0.80f, h * 0.34f), strokeWidth = 1.4.dp.toPx())
}

private fun DrawScope.drawGenericObjectSilhouette(w: Float, h: Float, ink: Color, faint: Color) {
    val mainW = 1.8.dp.toPx()
    val faintW = 1.dp.toPx()
    val cr = 6.dp.toPx()
    drawRoundRect(ink, Offset(w * 0.20f, h * 0.22f), Size(w * 0.60f, h * 0.56f), CornerRadius(cr, cr), style = Stroke(width = mainW, join = StrokeJoin.Round))
    drawLine(faint, Offset(w * 0.20f, h * 0.48f), Offset(w * 0.80f, h * 0.48f), strokeWidth = faintW)
    drawCircle(faint, radius = 4.dp.toPx(), center = Offset(w * 0.50f, h * 0.64f), style = Stroke(1.2.dp.toPx()))
}
