package com.loupeatelier.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LoupeBrandBlue = Color(0xFF1D5CE5)
val LoupeBrandBlack = Color(0xFF111111)

@Composable
fun LoupeCircleBadge(
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.2.dp, Color(0xFFDAD5CA), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.45f
            val cy = h * 0.44f
            val bezelRadius = w * 0.27f

            // Folded loupe handle in crisp black
            drawLine(
                color = LoupeBrandBlack,
                start = Offset(cx + bezelRadius * 0.62f, cy + bezelRadius * 0.62f),
                end = Offset(w * 0.78f, h * 0.78f),
                strokeWidth = w * 0.13f,
                cap = StrokeCap.Round
            )
            // Small hinge pin accent on handle
            drawCircle(
                color = Color.White,
                radius = w * 0.032f,
                center = Offset(w * 0.74f, h * 0.74f)
            )

            // Outer black loupe bezel
            drawCircle(
                color = LoupeBrandBlack,
                radius = bezelRadius,
                center = Offset(cx, cy)
            )

            // Royal blue optical ring
            drawCircle(
                color = LoupeBrandBlue,
                radius = bezelRadius * 0.78f,
                center = Offset(cx, cy)
            )

            // Optical lens center
            drawCircle(
                color = Color(0xFFEEF4FF),
                radius = bezelRadius * 0.54f,
                center = Offset(cx, cy)
            )

            // Lens specular glint arc
            drawArc(
                color = Color.White,
                startAngle = 205f,
                sweepAngle = 72f,
                useCenter = false,
                topLeft = Offset(cx - bezelRadius * 0.36f, cy - bezelRadius * 0.36f),
                size = Size(bezelRadius * 0.72f, bezelRadius * 0.72f),
                style = Stroke(width = w * 0.055f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun LoupeAtelierWordmark(
    fontSize: TextUnit = 20.sp,
    badgeSize: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LoupeCircleBadge(size = badgeSize)
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = LoupeBrandBlue, fontWeight = FontWeight.Bold)) {
                    append("Loupe")
                }
                withStyle(SpanStyle(color = LoupeBrandBlack, fontWeight = FontWeight.SemiBold)) {
                    append("Atelier")
                }
            },
            fontSize = fontSize
        )
    }
}
